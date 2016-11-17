package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;

/**
 * @author Daniel Serdyukov
 */
class RelationSpec {

    final String lTableName;

    final ClassName rTableClass;

    final ClassName rClass;

    final String fieldName;

    final boolean oneToOne;

    final boolean cascade;

    RelationSpec(String lTableName, String fieldName, boolean oneToOne, boolean cascade) {
        this(lTableName, null, ClassName.get(String.class), fieldName, oneToOne, cascade);
    }

    RelationSpec(String lTableName, ClassName rTableClass, ClassName rClass, String fieldName,
            boolean oneToOne, boolean cascade) {
        this.lTableName = lTableName;
        this.rTableClass = rTableClass;
        this.rClass = rClass;
        this.fieldName = fieldName;
        this.oneToOne = oneToOne;
        this.cascade = cascade;
    }

    boolean isStringList() {
        return rTableClass == null && !oneToOne;
    }

}
