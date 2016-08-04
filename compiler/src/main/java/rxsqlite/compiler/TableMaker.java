package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
class TableMaker {

    private final TableSpec mSpec;

    private final TypeElement mOriginatingElement;

    private ClassName mModel;

    TableMaker(TableSpec spec, TypeElement originatingElement) {
        mSpec = spec;
        mOriginatingElement = originatingElement;
        mModel = ClassName.get(originatingElement);
    }

    JavaFile.Builder brewJava() throws Exception {
        mSpec.validate();
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mModel.simpleName() + "$$Table")
                .addOriginatingElement(mOriginatingElement)
                .addModifiers(Modifier.PUBLIC)
                .superclass(Consts.generic(Consts.ABSTRACT_TABLE, mModel))
                .addMethod(makeConstructor())
                .addMethod(makeInstantiateMethod())
                .addMethod(makeCreateMethod())
                .addMethod(makeGetIdMethod())
                .addMethod(makeBindMethod());
        if (mSpec.hasRelations) {
            spec.addMethod(makeDoAfterInsertMethod());
        }
        spec.addMethods(mSpec.privateMethods);
        return JavaFile.builder(mModel.packageName(), spec.build());
    }

    private MethodSpec makeInstantiateMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("instantiate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Consts.R_DB, "db")
                .addParameter(Consts.R_CURSOR, "cursor")
                .returns(mModel)
                .addStatement("final $1T object = new $1T()", mModel);
        int index = 0;
        for (final Map.Entry<String, TypeMirror> entry : mSpec.fieldTypes.entrySet()) {
            final String fieldName = entry.getKey();
            final TypeMirror fieldType = entry.getValue();
            if (Utils.isLongType(fieldType)) {
                builder.addStatement("object.$L = ($T) cursor.getColumnLong($L)",
                        fieldName, TypeName.get(fieldType), index);
            } else if (Utils.isDoubleType(fieldType)) {
                builder.addStatement("object.$L = ($T) cursor.getColumnDouble($L)",
                        fieldName, TypeName.get(fieldType), index);
            } else if (Utils.isBooleanType(fieldType)) {
                builder.addStatement("object.$L = cursor.getColumnLong($L) > 0", fieldName, index);
            } else if (Utils.isStringType(fieldType)) {
                builder.addStatement("object.$L = cursor.getColumnString($L)", fieldName, index);
            } else if (Utils.isBlobType(fieldType)) {
                builder.addStatement("object.$L = cursor.getColumnBlob($L)", fieldName, index);
            } else {
                builder.addStatement("object.$1L = ($2T) getValue(cursor, $3L, $2T.class)",
                        fieldName, TypeName.get(fieldType), index);
            }
            ++index;
        }
        builder.addCode(mSpec.instantiate.build());
        return builder.addStatement("return object").build();
    }

    private MethodSpec makeConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($S, new $T[]{$L}, $L)", mSpec.tableName, Consts.J_STRING,
                        Utils.join(mSpec.columnNames, ", ", "\""), mSpec.hasRelations)
                .build();
    }

    private MethodSpec makeCreateMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Consts.R_DB, "db");
        final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(mSpec.tableName)
                .append("(").append(Utils.join(mSpec.columnDefs, ", "));
        final String constraints = Utils.join(mSpec.constraints, ", ");
        if (!Utils.isEmpty(constraints)) {
            sql.append(", ").append(constraints);
        }
        sql.append(");");
        builder.addStatement("db.exec($S)", sql.toString());
        for (final String indexedColumn : mSpec.indexedColumns) {
            builder.addStatement("db.exec($S)", "CREATE INDEX IF NOT EXISTS " +
                    mSpec.tableName + "_" + indexedColumn +
                    " ON " + mSpec.tableName + "(" + indexedColumn + ");");
        }
        builder.addCode(mSpec.create.build());
        return builder.build();
    }

    private MethodSpec makeGetIdMethod() {
        return MethodSpec.methodBuilder("getId")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(mModel, "object")
                .returns(TypeName.LONG)
                .addStatement("return object.$L", mSpec.pkFieldName)
                .build();
    }

    private MethodSpec makeBindMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Consts.R_STMT, "stmt")
                .addParameter(mModel, "object");
        int index = 1;
        for (final Map.Entry<String, TypeMirror> entry : mSpec.fieldTypes.entrySet()) {
            final String fieldName = entry.getKey();
            if (Utils.equals(mSpec.pkFieldName, fieldName)) {
                builder.beginControlFlow("if (object.$L > 0)", mSpec.pkFieldName)
                        .addStatement("stmt.bindLong($L, object.$L)", index, mSpec.pkFieldName)
                        .endControlFlow();
            } else {
                final TypeMirror fieldType = entry.getValue();
                if (Utils.isLongType(fieldType)) {
                    builder.addStatement("stmt.bindLong($L, object.$L)", index, fieldName);
                } else if (Utils.isDoubleType(fieldType)) {
                    builder.addStatement("stmt.bindDouble($L, object.$L)", index, fieldName);
                } else if (Utils.isBooleanType(fieldType)) {
                    builder.addStatement("stmt.bindLong($L, object.$L ? 1 : 0)", index, fieldName);
                } else if (Utils.isStringType(fieldType)) {
                    builder.addStatement("stmt.bindString($L, object.$L)", index, fieldName);
                } else if (Utils.isBlobType(fieldType)) {
                    builder.addStatement("stmt.bindBlob($L, object.$L)", index, fieldName);
                } else {
                    builder.addStatement("bindValue(stmt, $L, object.$L)", index, fieldName);
                }
            }
            ++index;
        }
        return builder.build();
    }

    private MethodSpec makeDoAfterInsertMethod() {
        return MethodSpec.methodBuilder("doAfterInsert")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Consts.R_DB, "db")
                .addParameter(mModel, "object")
                .addParameter(TypeName.LONG, "id")
                .addCode(mSpec.doAfterInsert.build())
                .build();
    }

}
