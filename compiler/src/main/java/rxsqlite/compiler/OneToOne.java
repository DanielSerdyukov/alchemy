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

    private OneToOne(Element field, String tableName, Element relType, String relTableName, boolean onDeleteCascade) {
        mField = field;
        mTableName = tableName;
        mRelType = relType;
        mRelTableName = relTableName;
        mOnDeleteCascade = onDeleteCascade;
    }

    static OneToOne parse(String tableName, Element field, Element relType) throws Exception {
        final SQLiteObject table = relType.getAnnotation(SQLiteObject.class);
        if (table == null) {
            throw new IllegalArgumentException(relType + " not annotated with @"
                    + SQLiteObject.class.getCanonicalName());
        }
        Utils.setAccessible(field);
        final SQLiteRelation relation = field.getAnnotation(SQLiteRelation.class);
        return new OneToOne(field, tableName, relType, table.value(), relation.onDeleteCascade());
    }

    @Override
    public void appendToCreate(MethodSpec.Builder methodSpec) {
        methodSpec.addStatement("db.exec(\"CREATE TABLE IF NOT EXISTS $1L_$2L_rel("
                + "$1L_id INTEGER, "
                + "$2L_id INTEGER, "
                + "FOREIGN KEY($1L_id) REFERENCES $1L(_id), "
                + "FOREIGN KEY($2L_id) REFERENCES $2L(_id)"
                + ");\")", mTableName, mRelTableName);
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
    public void brewSaveRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getSaveMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(ClassName.get(mRelType.asType()), "rel")
                .addParameter(TypeName.LONG, "pk")
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

    private String getSaveMethodName() {
        return "save" + mRelType.getSimpleName() + "Relation";
    }

}
