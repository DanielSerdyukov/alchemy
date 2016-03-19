package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import rxsqlite.annotation.SQLiteStringList;

/**
 * @author Daniel Serdyukov
 */
class StringList implements Relation {

    private final String mTableName;

    private final String mFieldName;

    private final String mColumnName;

    StringList(String tableName, Element field) {
        mTableName = tableName;
        mFieldName = field.getSimpleName().toString();
        final SQLiteStringList annotation = field.getAnnotation(SQLiteStringList.class);
        String columnName = annotation.value();
        if (Utils.isEmpty(columnName)) {
            columnName = Utils.getColumnName(mFieldName);
        }
        mColumnName = columnName;
    }

    @Override
    public void appendToCreate(MethodSpec.Builder methodSpec) {
        methodSpec.addStatement("db.exec(\"CREATE TABLE IF NOT EXISTS $1L_$2L_values($1L_id INTEGER, value TEXT);\")",
                mTableName, mColumnName);
        methodSpec.addStatement("db.exec(\"CREATE INDEX IF NOT EXISTS $1L_$2L_values_idx " +
                "ON $1L_$2L_values($1L_id);\")", mTableName, mColumnName);
        methodSpec.addStatement("db.exec(\"CREATE TRIGGER IF NOT EXISTS delete_$1L_$2L_values " +
                "AFTER DELETE ON $1L "
                + "FOR EACH ROW "
                + "BEGIN "
                + "DELETE FROM $1L_$2L_values WHERE $1L_id = OLD._id; "
                + "END"
                + ";\")", mTableName, mColumnName);
    }

    @Override
    public void appendToSave(MethodSpec.Builder methodSpec, String pkField) {
        methodSpec.addStatement("$L(db, object.$L, object.$L)", getSaveMethodName(), mFieldName, pkField);
    }

    @Override
    public void appendToInstantiate(MethodSpec.Builder methodSpec, String pkField) {
        methodSpec.addStatement("object.$L = $L(db, object.$L)", mFieldName, getQueryMethodName(), pkField);
    }

    @Override
    public void brewSaveRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getSaveMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(ParameterizedTypeName.get(
                        ClassName.get(Iterable.class),
                        ClassName.get(String.class)
                ), "values")
                .addParameter(TypeName.LONG, "pk")
                .beginControlFlow("if (values == null)")
                .addStatement("return")
                .endControlFlow()
                .addStatement("final $1T stmt = db.prepare(\"INSERT INTO $2L_$3L_values VALUES(?, ?);\")",
                        Consts.SQLITE_STMT, mTableName, mColumnName)
                .beginControlFlow("try")
                .beginControlFlow("for (final String value : values)")
                .addStatement("stmt.clearBindings()")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("stmt.bindString(2, value)")
                .addStatement("stmt.executeInsert()")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build());
    }

    @Override
    public void brewQueryRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getQueryMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(TypeName.LONG, "pk")
                .returns(ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.get(String.class)
                ))
                .addStatement("final $T values = new $T<>()", ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.get(String.class)
                ), ClassName.get(ArrayList.class))
                .addStatement("final $1T stmt = db.prepare(\"SELECT value FROM $2L_$3L_values WHERE $2L_id = ?;\")",
                        Consts.SQLITE_STMT, mTableName, mColumnName)
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("final $T cursor = stmt.executeQuery()", Consts.SQLITE_CURSOR)
                .beginControlFlow("while (cursor.step())")
                .addStatement("values.add(cursor.getColumnString(0))")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .addStatement("return values")
                .build());
    }

    private String getSaveMethodName() {
        return "save" + Utils.getCanonicalName(mFieldName) + "Values";
    }

    private String getQueryMethodName() {
        return "query" + Utils.getCanonicalName(mFieldName) + "Values";
    }

}
