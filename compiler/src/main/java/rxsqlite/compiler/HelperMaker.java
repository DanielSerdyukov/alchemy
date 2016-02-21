package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Iterator;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
class HelperMaker {

    private static final String SUFFIX = "$$Helper";

    static JavaFile brewJava(ClassName modelClass, Map<String, TypeMirror> columnTypes,
                             Map<String, String> columnNames) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(modelClass.simpleName() + SUFFIX)
                .addModifiers(Modifier.PUBLIC);
        brewCreateMethod(builder, modelClass, columnTypes);
        brewBindMethod(builder, modelClass, columnTypes, columnNames);
        brewBindPrimaryKeyMethod(builder, modelClass, columnNames);
        brewSetPrimaryKeyMethod(builder, modelClass, columnNames);
        return JavaFile.builder(modelClass.packageName(), builder.build())
                .addFileComment("Generated code from RxSQLite. Do not modify!")
                .skipJavaLangImports(true)
                .build();
    }

    static ClassName className(ClassName modelClass) {
        return ClassName.get(modelClass.packageName(), modelClass.simpleName() + SUFFIX);
    }

    private static void brewCreateMethod(TypeSpec.Builder builder, ClassName modelClass,
                                         Map<String, TypeMirror> columnTypes) {
        final MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TableMaker.SQLITE_ROW, "row")
                .addParameter(BinderMaker.className(), "binder")
                .returns(modelClass);
        methodSpec.addStatement("final $1T object = new $1T()", modelClass);
        int index = 0;
        for (final Map.Entry<String, TypeMirror> entry : columnTypes.entrySet()) {
            final String fieldName = entry.getKey();
            final TypeMirror type = entry.getValue();
            if (Utils.isLongType(type)) {
                methodSpec.addStatement("object.$L = ($L) row.getColumnLong($L)", fieldName, type, index);
            } else if (Utils.isDoubleType(type)) {
                methodSpec.addStatement("object.$L = ($L) row.getColumnDouble($L)", fieldName, type, index);
            } else if (Utils.isStringType(type)) {
                methodSpec.addStatement("object.$L = row.getColumnString($L)", fieldName, index);
            } else if (Utils.isBlobType(type)) {
                methodSpec.addStatement("object.$L = row.getColumnBlob($L)", fieldName, index);
            } else if (Utils.isEnumType(type)) {
                methodSpec.addStatement("object.$L = binder.getEnumValue(row, $L, $L.class)", fieldName, index, type);
            } else {
                methodSpec.addStatement("object.$L = binder.getValue(row, $L, $L.class)", fieldName, index, type);
            }
            ++index;
        }
        methodSpec.addStatement("return object");
        builder.addMethod(methodSpec.build());
    }

    private static void brewBindMethod(TypeSpec.Builder builder, ClassName modelClass,
                                       Map<String, TypeMirror> columnTypes,
                                       Map<String, String> columnNames) {
        final MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TableMaker.SQLITE_STMT, "stmt")
                .addParameter(modelClass, "object")
                .addParameter(BinderMaker.className(), "binder");
        final Iterator<Map.Entry<String, String>> iterator = columnNames.entrySet().iterator();
        int index = 1;
        if (iterator.hasNext()) {
            final Map.Entry<String, String> primaryKey = iterator.next();
            final String primaryKeyField = primaryKey.getValue();
            methodSpec.beginControlFlow("if (object.$L > 0)", primaryKeyField);
            methodSpec.addStatement("stmt.bindLong($L, object.$L)", index, primaryKeyField);
            methodSpec.nextControlFlow("else");
            methodSpec.addStatement("stmt.bindNull($L)", index);
            methodSpec.endControlFlow();
        }
        while (iterator.hasNext()) {
            ++index;
            final Map.Entry<String, String> column = iterator.next();
            final String fieldName = column.getValue();
            final TypeMirror type = columnTypes.get(fieldName);
            if (Utils.isLongType(type)) {
                methodSpec.addStatement("stmt.bindLong($L, object.$L)", index, fieldName);
            } else if (Utils.isDoubleType(type)) {
                methodSpec.addStatement("stmt.bindDouble($L, object.$L)", index, fieldName);
            } else if (Utils.isStringType(type)) {
                methodSpec.addStatement("stmt.bindString($L, object.$L)", index, fieldName);
            } else if (Utils.isBlobType(type)) {
                methodSpec.addStatement("stmt.bindBlob($L, object.$L)", index, fieldName);
            } else if (Utils.isEnumType(type)) {
                methodSpec.beginControlFlow("if (object.$L != null)", fieldName);
                methodSpec.addStatement("stmt.bindString($L, object.$L.name())", index, fieldName);
                methodSpec.nextControlFlow("else");
                methodSpec.addStatement("stmt.bindNull($L)", index);
                methodSpec.endControlFlow();
            } else {
                methodSpec.addStatement("binder.bindValue(stmt, $L, object.$L)", index, fieldName);
            }
        }
        builder.addMethod(methodSpec.build());
    }

    private static void brewBindPrimaryKeyMethod(TypeSpec.Builder builder, ClassName modelClass,
                                                 Map<String, String> columnNames) {
        builder.addMethod(MethodSpec.methodBuilder("bindPrimaryKey")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TableMaker.SQLITE_STMT, "stmt")
                .addParameter(modelClass, "object")
                .addStatement("stmt.bindLong(1, object.$L)", columnNames.values().iterator().next())
                .build());
    }

    private static void brewSetPrimaryKeyMethod(TypeSpec.Builder builder, ClassName modelClass,
                                                Map<String, String> columnNames) {
        builder.addMethod(MethodSpec.methodBuilder("setPrimaryKey")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(modelClass, "object")
                .addParameter(TypeName.LONG, "rowId")
                .addStatement("object.$L = rowId", columnNames.values().iterator().next())
                .build());
    }

}
