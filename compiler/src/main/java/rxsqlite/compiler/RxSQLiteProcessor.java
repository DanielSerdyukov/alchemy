package rxsqlite.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
@AutoService(Processor.class)
public class RxSQLiteProcessor extends AbstractProcessor {

    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        Utils.init(env);
        mFiler = env.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> types = new LinkedHashSet<>();
        types.add(SQLiteObject.class.getCanonicalName());
        types.add(SQLitePk.class.getCanonicalName());
        types.add(SQLiteColumn.class.getCanonicalName());
        types.add(SQLiteRelation.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        final Map<TypeElement, Table> classMap = new LinkedHashMap<>();
        final Map<Element, String> schema = new LinkedHashMap<>();
        processSQLiteObject(roundEnv, classMap);
        processSQLitePk(roundEnv, classMap);
        processSQLiteColumn(roundEnv, classMap);
        processSQLiteRelation(roundEnv, classMap);

        try {
            CustomTypes.brewJava().writeTo(mFiler); // needs to access package private class RxSQLiteBinder
        } catch (IOException e) {
            error(null, "Unable to write CustomTypes class", e.getMessage());
        }

        for (final Map.Entry<TypeElement, Table> entry : classMap.entrySet()) {
            final TypeElement element = entry.getKey();
            final Table tableClass = entry.getValue();
            try {
                final JavaFile tableJava = tableClass.brewTableJava();
                tableJava.writeTo(mFiler);
                schema.put(element, tableJava.packageName + "." + tableJava.typeSpec.name);
            } catch (Exception e) {
                error(element, "Unable to write table class for type %s: %s", element, e.getMessage());
            }
        }

        try {
            Schema.brewJava(schema).writeTo(mFiler);
        } catch (IOException e) {
            error(null, "Unable to write schema class", e.getMessage());
        }

        return true;
    }

    private void processSQLiteObject(RoundEnvironment roundEnv, Map<TypeElement, Table> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteObject.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element, classMap)
                        .parseSQLiteObject(element);
            } catch (Exception e) {
                parsingError(element, SQLiteObject.class, e);
            }
        }
    }

    private void processSQLitePk(RoundEnvironment roundEnv, Map<TypeElement, Table> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLitePk.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element.getEnclosingElement(), classMap)
                        .parseSQLitePk(element);
            } catch (Exception e) {
                parsingError(element, SQLitePk.class, e);
            }
        }
    }

    private void processSQLiteColumn(RoundEnvironment roundEnv, Map<TypeElement, Table> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteColumn.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element.getEnclosingElement(), classMap)
                        .parseSQLiteColumn(element);
            } catch (Exception e) {
                parsingError(element, SQLiteColumn.class, e);
            }
        }
    }

    private void processSQLiteRelation(RoundEnvironment roundEnv, Map<TypeElement, Table> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteRelation.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element.getEnclosingElement(), classMap)
                        .parseSQLiteRelation(element);
            } catch (Exception e) {
                parsingError(element, SQLiteRelation.class, e);
            }
        }
    }

    private Table getOrCreateTableClass(TypeElement element, Map<TypeElement, Table> classMap) {
        Table tableClass = classMap.get(element);
        if (tableClass == null) {
            tableClass = new Table(element);
            classMap.put(element, tableClass);
        }
        return tableClass;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        if (element != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
        }
    }

    private void parsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

}
