package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import rxsqlite.RxSQLiteCursor;
import rxsqlite.RxSQLiteDb;
import rxsqlite.RxSQLiteObjectRelation;
import rxsqlite.RxSQLiteStmt;
import rxsqlite.RxSQLiteStringRelation;
import rxsqlite.RxSQLiteTypes;

/**
 * @author Daniel Serdyukov
 */
class TableMaker {

    private final TableSpec mTableSpec;

    TableMaker(TableSpec spec) {
        mTableSpec = spec;
    }

    JavaFile brewJavaFile() throws Exception {
        final TypeElement originElement = mTableSpec.getOriginElement();
        final ClassName originClass = ClassName.get(originElement);
        final ClassName tableClass = mTableSpec.getClassName();
        final TypeSpec.Builder spec = TypeSpec.classBuilder(tableClass.simpleName())
                .addOriginatingElement(originElement)
                .addModifiers(Modifier.PUBLIC)
                .superclass(Literals.baseTable(originClass))
                .addField(makeInstanceField())
                .addMethod(makeConstructor())
                .addMethod(makeCreateMethod())
                .addMethod(makeGetObjectIdMethod())
                .addMethod(makeSetObjectIdMethod())
                .addMethod(makeBindObjectMethod())
                .addMethod(makeInstantiateMethod());
        if (!mTableSpec.getRelations().isEmpty()) {
            spec.addMethod(makeDoOnInsertMethod());
            spec.addMethod(makeDoOnInstantiateMethod());
        }
        return JavaFile.builder(originClass.packageName(), spec.build())
                .addFileComment("Generated code from RxSQLite. Do not modify!")
                .skipJavaLangImports(true)
                .build();
    }

    private FieldSpec makeInstanceField() {
        return FieldSpec.builder(mTableSpec.getClassName(), "INSTANCE",
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", mTableSpec.getClassName())
                .build();
    }

    private MethodSpec makeConstructor() {
        final StringBuilder columnNames = new StringBuilder();
        final Iterator<ColumnSpec> iterator = mTableSpec.getColumns().iterator();
        while (iterator.hasNext()) {
            columnNames.append('"').append(iterator.next().columnName).append('"');
            if (iterator.hasNext()) {
                columnNames.append(", ");
            }
        }
        final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($S, new $T[]{$L})", mTableSpec.getTableName(),
                        ClassName.get(String.class), columnNames.toString());
        for (final RelationSpec relationSpec : mTableSpec.getRelations()) {
            if (relationSpec.isStringList()) {
                builder.addStatement("putRelation($1S, new $2T($3S, $1S, $4L))",
                        relationSpec.fieldName, RxSQLiteStringRelation.class,
                        relationSpec.lTableName, relationSpec.cascade);
            } else {
                builder.addStatement("putRelation($1S, new $2T<>($3S, $1S, $4T.INSTANCE, $5L))",
                        relationSpec.fieldName, RxSQLiteObjectRelation.class, relationSpec.lTableName,
                        relationSpec.rTableClass, relationSpec.cascade);
            }
        }
        return builder.build();
    }

    private MethodSpec makeCreateMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(RxSQLiteDb.class, "db");
        final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(mTableSpec.getTableName())
                .append(" (");
        final Iterator<ColumnSpec> iterator = mTableSpec.getColumns().iterator();
        while (iterator.hasNext()) {
            final ColumnSpec columnSpec = iterator.next();
            sql.append(columnSpec.columnName).append(" ").append(columnSpec.columnDef);
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        sql.append(");");
        builder.addStatement("db.exec($S)", sql.toString());
        return builder.build();
    }

    private MethodSpec makeGetObjectIdMethod() {
        return MethodSpec.methodBuilder("getObjectId")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.LONG)
                .addParameter(ClassName.get(mTableSpec.getOriginElement()), "object")
                .addStatement("return object.$L", mTableSpec.getPk().fieldName)
                .build();
    }

    private MethodSpec makeSetObjectIdMethod() {
        return MethodSpec.methodBuilder("setObjectId")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ClassName.get(mTableSpec.getOriginElement()), "object")
                .addParameter(TypeName.LONG, "id")
                .addStatement("object.$L = id", mTableSpec.getPk().fieldName)
                .build();
    }

    private MethodSpec makeBindObjectMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("bindObject")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(RxSQLiteStmt.class, "stmt")
                .addParameter(ClassName.get(mTableSpec.getOriginElement()), "object");
        int index = 1;
        for (final ColumnSpec columnSpec : mTableSpec.getColumns()) {
            if (columnSpec.isPrimaryKey) {
                if (TypeKind.DECLARED == columnSpec.fieldType.getKind()) {
                    builder.beginControlFlow("if (object.$1L != null && object.$1L > 0)", columnSpec.fieldName)
                            .addStatement("stmt.bindLong($L, object.$L)", index, columnSpec.fieldName)
                            .endControlFlow();
                } else {
                    builder.beginControlFlow("if (object.$L > 0)", columnSpec.fieldName)
                            .addStatement("stmt.bindLong($L, object.$L)", index, columnSpec.fieldName)
                            .endControlFlow();
                }
            } else {
                if (Literals.isNullable(columnSpec.fieldType)) {
                    builder.beginControlFlow("if (object.$1L != null)", columnSpec.fieldName);
                }
                if (Literals.isLong(columnSpec.fieldType)
                        || Literals.isInt(columnSpec.fieldType)
                        || Literals.isShort(columnSpec.fieldType)) {
                    builder.addStatement("stmt.bindLong($L, object.$L)", index, columnSpec.fieldName);
                } else if (Literals.isDouble(columnSpec.fieldType)
                        || Literals.isFloat(columnSpec.fieldType)) {
                    builder.addStatement("stmt.bindDouble($L, object.$L)", index, columnSpec.fieldName);
                } else if (Literals.isByteArray(columnSpec.fieldType)) {
                    builder.addStatement("stmt.bindBlob($L, object.$L)", index, columnSpec.fieldName);
                } else if (Literals.isString(columnSpec.fieldType)) {
                    builder.addStatement("stmt.bindString($L, object.$L)", index, columnSpec.fieldName);
                } else if (Literals.isDate(columnSpec.fieldType)) {
                    builder.addStatement("stmt.bindLong($L, object.$L.getTime())", index, columnSpec.fieldName);
                } else {
                    builder.addStatement("$T.bindValue(stmt, $L, object.$L)",
                            RxSQLiteTypes.class, index, columnSpec.fieldName);
                }
                if (Literals.isNullable(columnSpec.fieldType)) {
                    builder.endControlFlow();
                }
            }
            ++index;
        }
        return builder.build();
    }

    private MethodSpec makeInstantiateMethod() {
        final ClassName originClass = ClassName.get(mTableSpec.getOriginElement());
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("instantiate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(originClass)
                .addParameter(RxSQLiteCursor.class, "cursor");
        builder.addStatement("final $1T object = new $1T()", originClass);
        int index = 0;
        for (final ColumnSpec columnSpec : mTableSpec.getColumns()) {
            if (Literals.isLong(columnSpec.fieldType)
                    || Literals.isInt(columnSpec.fieldType)
                    || Literals.isShort(columnSpec.fieldType)) {
                builder.addStatement("object.$L = ($T) cursor.getColumnLong($L)",
                        columnSpec.fieldName, columnSpec.fieldType, index);
            } else if (Literals.isDouble(columnSpec.fieldType)
                    || Literals.isFloat(columnSpec.fieldType)) {
                builder.addStatement("object.$L = ($T) cursor.getColumnDouble($L)", columnSpec.fieldName,
                        columnSpec.fieldType, index);
            } else if (Literals.isByteArray(columnSpec.fieldType)) {
                builder.addStatement("object.$L = cursor.getColumnBlob($L)", columnSpec.fieldName, index);
            } else if (Literals.isString(columnSpec.fieldType)) {
                builder.addStatement("object.$L = cursor.getColumnString($L)", columnSpec.fieldName, index);
            } else if (Literals.isDate(columnSpec.fieldType)) {
                builder.addStatement("object.$L = new $T(cursor.getColumnLong($L))",
                        columnSpec.fieldName, Date.class, index);
            } else {
                builder.addStatement("object.$1L = ($4T) $2T.getValue(cursor, $3L, $4T.class)",
                        columnSpec.fieldName, RxSQLiteTypes.class, index, columnSpec.fieldType);
            }
            ++index;
        }
        builder.addStatement("return object");
        return builder.build();
    }

    private MethodSpec makeDoOnInsertMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("doOnInsert")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(RxSQLiteDb.class, "db")
                .addParameter(ClassName.get(mTableSpec.getOriginElement()), "object")
                .addParameter(TypeName.LONG, "id")
                .addStatement("super.doOnInsert(db, object, id)");
        for (final RelationSpec relationSpec : mTableSpec.getRelations()) {
            if (relationSpec.oneToOne) {
                builder.beginControlFlow("if (object.$L != null)", relationSpec.fieldName);
                builder.addStatement("this.<$1T>findRelation($2S).insert(db, $3T.singletonList(object.$2L), id)",
                        relationSpec.rClass, relationSpec.fieldName, Collections.class);
                builder.endControlFlow();
            } else {
                builder.beginControlFlow("if (object.$1L != null && !object.$1L.isEmpty())", relationSpec.fieldName);
                builder.addStatement("this.<$1T>findRelation($2S).insert(db, object.$2L, id)",
                        relationSpec.rClass, relationSpec.fieldName);
                builder.endControlFlow();
            }
        }
        return builder.build();
    }

    private MethodSpec makeDoOnInstantiateMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("doOnInstantiate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(RxSQLiteDb.class, "db")
                .addParameter(ClassName.get(mTableSpec.getOriginElement()), "object")
                .addStatement("super.doOnInstantiate(db, object)");
        for (final RelationSpec relationSpec : mTableSpec.getRelations()) {
            if (relationSpec.oneToOne) {
                builder.addStatement("object.$1L = this.<$2T>findRelation($1S).selectOne(db, object.$3L)",
                        relationSpec.fieldName, relationSpec.rClass, mTableSpec.getPk().fieldName);
            } else {
                builder.addStatement("object.$1L = this.<$2T>findRelation($1S).selectList(db, object.$3L)",
                        relationSpec.fieldName, relationSpec.rClass, mTableSpec.getPk().fieldName);
            }
        }
        return builder.build();
    }

}
