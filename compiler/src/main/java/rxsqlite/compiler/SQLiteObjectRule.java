package rxsqlite.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import rxsqlite.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectRule implements AnnotationRule {

    @Override
    public void process(Map<TypeElement, TableSpec> specs, Element element) throws Exception {
        final TypeElement originElement = (TypeElement) element;
        final TableSpec tableSpec = TableSpec.get(specs, originElement);
        tableSpec.setOriginElement(originElement);
        final SQLiteObject annotation = element.getAnnotation(SQLiteObject.class);
        tableSpec.setTableName(annotation.value());
        tableSpec.setTableConstraints(annotation.constraints());
    }

}
