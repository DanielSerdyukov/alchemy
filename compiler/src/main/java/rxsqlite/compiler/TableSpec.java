package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class TableSpec {

    private final List<ColumnSpec> mColumns = new ArrayList<>();

    private final List<RelationSpec> mRelations = new ArrayList<>();

    private TypeElement mOriginElement;

    private String mTableName;

    private String[] mTableConstraints;

    static TableSpec get(Map<TypeElement, TableSpec> specs, TypeElement element) {
        TableSpec spec = specs.get(element);
        if (spec == null) {
            spec = new TableSpec();
            specs.put(element, spec);
        }
        return spec;
    }

    TypeElement getOriginElement() {
        return mOriginElement;
    }

    void setOriginElement(TypeElement element) {
        mOriginElement = element;
    }

    String getTableName() {
        return mTableName;
    }

    void setTableName(String tableName) {
        mTableName = tableName;
    }

    void setTableConstraints(String[] tableConstraints) {
        mTableConstraints = tableConstraints;
    }

    ClassName getClassName() {
        final ClassName originClassName = ClassName.get(mOriginElement);
        return ClassName.get(originClassName.packageName(), originClassName.simpleName() + "$$Table");
    }

    boolean hasPrimaryKey() {
        return !mColumns.isEmpty() && mColumns.get(0).isPrimaryKey;
    }

    void setPk(ColumnSpec columnSpec) {
        mColumns.add(columnSpec);
    }

    ColumnSpec getPk() {
        return mColumns.get(0);
    }

    void addColumn(ColumnSpec columnSpec) {
        mColumns.add(columnSpec);
    }

    List<ColumnSpec> getColumns() {
        return mColumns;
    }

    void addRelation(RelationSpec relationSpec) {
        mRelations.add(relationSpec);
    }

    List<RelationSpec> getRelations() {
        return mRelations;
    }

}
