package rxsqlite.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * @author Daniel Serdyukov
 */
class Logger {

    static void error(ProcessingEnvironment processingEnv, Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        if (element != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
        }
    }

    static void parsingError(ProcessingEnvironment processingEnv, Element element,
                             Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(processingEnv, element, "Unable to parse @%s.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

}
