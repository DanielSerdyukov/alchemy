package rxsqlite.compiler;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;

import rxsqlite.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
class OneToOne {

    private final Element mField;

    private final Element mRelType;

    private final String mTableName;

    private final String mRelTableName;

    private OneToOne(Element field, String tableName, Element relType, String relTableName) {
        mField = field;
        mTableName = tableName;
        mRelType = relType;
        mRelTableName = relTableName;
    }

    static OneToOne parse(String tableName, Element field, Element relType) throws Exception {
        final SQLiteObject table = relType.getAnnotation(SQLiteObject.class);
        if (table == null) {
            throw new IllegalArgumentException(relType + " not annotated with @"
                    + SQLiteObject.class.getCanonicalName());
        }
        return new OneToOne(field, tableName, relType, table.value());
    }

    void appendToCreate(MethodSpec.Builder builder) {
        builder.addStatement("db.exec(\"CREATE TABLE IF NOT EXISTS $1L_$2L_rel("
                + "$1L_id INTEGER, "
                + "$2L_id INTEGER, "
                + "FOREIGN KEY($1L_id) REFERENCES $1L(_id), "
                + "FOREIGN KEY($2L_id) REFERENCES $2L(_id)"
                + ");\")", mTableName, mRelTableName);
    }

}
