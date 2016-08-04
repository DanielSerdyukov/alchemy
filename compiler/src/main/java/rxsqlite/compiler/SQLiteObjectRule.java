package rxsqlite.compiler;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import rxsqlite.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectRule extends AnnotationRule {

    SQLiteObjectRule(ProcessingEnvironment pEnv) {
        super(pEnv);
    }

    @Override
    public TypeElement getOriginType(Element element) {
        return (TypeElement) element;
    }

    @Override
    public void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception {
        final SQLiteObject annotation = element.getAnnotation(SQLiteObject.class);
        spec.tableName = resolveTableName(element);
        spec.constraints.addAll(Arrays.asList(annotation.constraints()));
    }

}
