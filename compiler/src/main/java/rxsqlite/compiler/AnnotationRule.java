package rxsqlite.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
interface AnnotationRule {

    void process(Map<TypeElement, TableSpec> specs, Element element) throws Exception;

}
