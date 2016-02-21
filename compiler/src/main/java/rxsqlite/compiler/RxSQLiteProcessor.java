package rxsqlite.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
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

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

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
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        final Map<TypeElement, TableMaker> classMap = new LinkedHashMap<>();
        final Map<Element, String> schema = new LinkedHashMap<>();
        processSQLiteObject(roundEnv, classMap);
        processSQLitePk(roundEnv, classMap);
        processSQLiteColumn(roundEnv, classMap);

        try {
            BinderMaker.brewJava().writeTo(mFiler); // needs to access package private class RxSQLiteBinder
        } catch (IOException e) {
            Logger.error(processingEnv, null, "Unable to write binder class", e.getMessage());
        }

        for (final Map.Entry<TypeElement, TableMaker> entry : classMap.entrySet()) {
            final TypeElement element = entry.getKey();
            final TableMaker tableClass = entry.getValue();
            try {
                tableClass.brewHelperJava().writeTo(mFiler);
                final JavaFile tableJava = tableClass.brewTableJava();
                tableJava.writeTo(mFiler);
                schema.put(element, tableJava.packageName + "." + tableJava.typeSpec.name);
            } catch (Exception e) {
                Logger.error(processingEnv, element, "Unable to write table class for type %s: %s",
                        element, e.getMessage());
            }
        }

        try {
            SchemaMaker.brewJava(schema).writeTo(mFiler);
        } catch (IOException e) {
            Logger.error(processingEnv, null, "Unable to write schema class", e.getMessage());
        }

        return true;
    }

    private void processSQLiteObject(RoundEnvironment roundEnv, Map<TypeElement, TableMaker> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteObject.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element, classMap)
                        .parseSQLiteObject(element);
            } catch (Exception e) {
                Logger.parsingError(processingEnv, element, SQLiteObject.class, e);
            }
        }
    }

    private void processSQLitePk(RoundEnvironment roundEnv, Map<TypeElement, TableMaker> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLitePk.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element.getEnclosingElement(), classMap)
                        .parseSQLitePk(element);
            } catch (Exception e) {
                Logger.parsingError(processingEnv, element, SQLitePk.class, e);
            }
        }
    }

    private void processSQLiteColumn(RoundEnvironment roundEnv, Map<TypeElement, TableMaker> classMap) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteColumn.class)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                getOrCreateTableClass((TypeElement) element.getEnclosingElement(), classMap)
                        .parseSQLiteColumn(element);
            } catch (Exception e) {
                Logger.parsingError(processingEnv, element, SQLiteColumn.class, e);
            }
        }
    }

    private TableMaker getOrCreateTableClass(TypeElement element, Map<TypeElement, TableMaker> classMap) {
        TableMaker tableClass = classMap.get(element);
        if (tableClass == null) {
            tableClass = new TableMaker(element);
            classMap.put(element, tableClass);
        }
        return tableClass;
    }

}
