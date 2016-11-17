package rxsqlite.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
class ColumnSpec {

    final String fieldName;

    final TypeMirror fieldType;

    final String columnName;

    final String columnDef;

    final boolean isPrimaryKey;

    ColumnSpec(Element field, String columnName, String columnDef) {
        this(field, columnName, columnDef, false);
    }

    ColumnSpec(Element field, String columnName, String columnDef, boolean primaryKey) {
        this.fieldName = field.getSimpleName().toString();
        this.fieldType = field.asType();
        this.columnName = columnName;
        this.columnDef = columnDef;
        this.isPrimaryKey = primaryKey;
    }

}
