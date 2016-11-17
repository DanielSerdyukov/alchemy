package rxsqlite.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import rxsqlite.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
class SQLitePkRule implements AnnotationRule {

    @Override
    public void process(Map<TypeElement, TableSpec> specs, Element element) throws Exception {
        if (!Literals.isLong(element.asType())) {
            throw new IllegalArgumentException("PRIMARY KEY must be long");
        }
        final TableSpec tableSpec = TableSpec.get(specs, (TypeElement) element.getEnclosingElement());
        if (tableSpec.hasPrimaryKey()) {
            throw new IllegalArgumentException("Table already has a PRIMARY KEY");
        }
        final SQLitePk annotation = element.getAnnotation(SQLitePk.class);
        final String constraints = annotation.constraints();
        String columnDef = "INTEGER PRIMARY KEY";
        if (!constraints.isEmpty()) {
            columnDef += " " + constraints;
        }
        tableSpec.setPk(new ColumnSpec(element, "_id", columnDef, true));
        Literals.fixFieldAccess(element);
    }

}
