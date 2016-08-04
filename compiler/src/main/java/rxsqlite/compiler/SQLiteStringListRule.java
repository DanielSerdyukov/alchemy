package rxsqlite.compiler;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class SQLiteStringListRule extends SQLiteRelationRule {


    SQLiteStringListRule(ProcessingEnvironment pEnv) {
        super(pEnv);
    }

    @Override
    void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception {
        element.accept(this, null); // mark accessible
        final List<Element> types = SQLiteRelationRule.resolveTypes(element.asType());
        if (types.size() != 2
                || !isAssignable(types.get(0), List.class)
                || !isAssignable(types.get(1), String.class)) {
            throw new IllegalArgumentException("Only List<String> supported as relation");
        }
        processStringListRelation(spec, element, true);
    }

}
