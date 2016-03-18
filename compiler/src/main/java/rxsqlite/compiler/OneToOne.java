package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
class OneToOne implements Relation {

    private final Element mField;

    private final Element mRelType;

    private final String mTableName;

    private final String mRelTableName;

    private final boolean mOnDeleteCascade;

    OneToOne(String tableName, Element field, Element relType) {
        final SQLiteObject relTable = relType.getAnnotation(SQLiteObject.class);
        if (relTable == null) {
            throw new IllegalArgumentException(relType + " not annotated with @"
                    + SQLiteObject.class.getCanonicalName());
        }
        mField = field;
        mTableName = tableName;
        mRelType = relType;
        mRelTableName = relTable.value();
        mOnDeleteCascade = field.getAnnotation(SQLiteRelation.class).onDeleteCascade();
        Utils.setAccessible(mField);
    }

    @Override
    public void appendToCreate(MethodSpec.Builder methodSpec) {
        methodSpec.addStatement("db.exec(\"CREATE TABLE IF NOT EXISTS $1L_$2L_rel("
                + "$1L_id INTEGER, "
                + "$2L_id INTEGER, "
                + "FOREIGN KEY($1L_id) REFERENCES $1L(_id) ON DELETE CASCADE ON UPDATE CASCADE, "
                + "FOREIGN KEY($2L_id) REFERENCES $2L(_id) ON DELETE CASCADE ON UPDATE CASCADE"
                + ");\")", mTableName, mRelTableName);
        methodSpec.addStatement("db.exec(\"CREATE INDEX IF NOT EXISTS $1L_$2L_rel_$1L_idx"
                + " ON $1L_$2L_rel($1L_id);\")", mTableName, mRelTableName);
        if (mOnDeleteCascade) {
            methodSpec.addStatement("db.exec(\"CREATE TRIGGER IF NOT EXISTS delete_$2L_after_$1L "
                    + "AFTER DELETE ON $1L_$2L_rel "
                    + "FOR EACH ROW "
                    + "BEGIN "
                    + "DELETE FROM $2L WHERE _id = OLD.$2L_id; "
                    + "END"
                    + ";\")", mTableName, mRelTableName);
        }
    }

    @Override
    public void appendToSave(MethodSpec.Builder methodSpec, String pkField) {
        methodSpec.addStatement("$L(db, object.$L, object.$L)", getSaveMethodName(), mField.getSimpleName(), pkField);
    }

    @Override
    public void appendToInstantiate(MethodSpec.Builder methodSpec, String pkField) {
        methodSpec.addStatement("object.$L = $L(db, object.$L)", mField.getSimpleName(), getQueryMethodName(), pkField);
    }

    @Override
    public void brewSaveRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getSaveMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(ClassName.get(mRelType.asType()), "rel")
                .addParameter(TypeName.LONG, "pk")
                .beginControlFlow("if (rel == null)")
                .addStatement("return")
                .endControlFlow()
                .addStatement("final $T relIds = new $T(mTypes).blockingSave(db, $T.singletonList(rel))",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Long.class)),
                        ClassName.bestGuess(Table.getTableClassName(mRelType)),
                        ClassName.get(Collections.class))
                .beginControlFlow("if (!relIds.isEmpty())")
                .addStatement("final $1T stmt = db.prepare(\"INSERT INTO $2L_$3L_rel($2L_id, $3L_id) VALUES(?, ?);\")",
                        Consts.SQLITE_STMT, mTableName, mRelTableName)
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("stmt.bindLong(2, relIds.get(0))")
                .addStatement("stmt.executeInsert()")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .endControlFlow()
                .build());
    }

    @Override
    public void brewQueryRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getQueryMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(TypeName.LONG, "pk")
                .returns(ClassName.get(mRelType.asType()))
                .addStatement("final $1T stmt = db.prepare(\"SELECT $3L.* FROM $3L, $2L_$3L_rel"
                                + " WHERE $3L._id=$2L_$3L_rel.$3L_id AND $2L_$3L_rel.$2L_id = ?;\")",
                        Consts.SQLITE_STMT, mTableName, mRelTableName)
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("final $T cursor = stmt.executeQuery()", Consts.SQLITE_CURSOR)
                .beginControlFlow("if (cursor.step())")
                .addStatement("return new $T(mTypes).instantiate(db, cursor)",
                        ClassName.bestGuess(Table.getTableClassName(mRelType)))
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .addStatement("return null")
                .build());
    }

    protected Element getRelType() {
        return mRelType;
    }

    protected String getTableName() {
        return mTableName;
    }

    protected String getRelTableName() {
        return mRelTableName;
    }

    protected String getSaveMethodName() {
        return "save" + mRelType.getSimpleName() + "Relation";
    }

    protected String getQueryMethodName() {
        return "query" + mRelType.getSimpleName() + "Relation";
    }

}
