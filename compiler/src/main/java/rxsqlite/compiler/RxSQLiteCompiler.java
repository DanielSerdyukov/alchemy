package rxsqlite.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import rxsqlite.RxSQLiteTable;
import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
@AutoService(Processor.class)
public class RxSQLiteCompiler extends AbstractProcessor {

    private final Map<Class<? extends Annotation>, AnnotationRule> mRules = new LinkedHashMap<>();

    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mFiler = env.getFiler();
        mRules.put(SQLiteObject.class, new SQLiteObjectRule());
        mRules.put(SQLitePk.class, new SQLitePkRule());
        mRules.put(SQLiteColumn.class, new SQLiteColumnRule());
        mRules.put(SQLiteRelation.class, new SQLiteRelationRule());

        Literals.sTypes = env.getTypeUtils();
        Literals.sElements = env.getElementUtils();
        Literals.sTrees = Trees.instance(env);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> types = new LinkedHashSet<>();
        for (final Class<? extends Annotation> clazz : mRules.keySet()) {
            types.add(clazz.getCanonicalName());
        }
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment rEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Map<TypeElement, TableSpec> specs = new LinkedHashMap<>();
        for (final Map.Entry<Class<? extends Annotation>, AnnotationRule> entry : mRules.entrySet()) {
            final Set<? extends Element> elements = rEnv.getElementsAnnotatedWith(entry.getKey());
            for (final Element element : elements) {
                if (!SuperficialValidation.validateElement(element)) {
                    continue;
                }
                try {
                    entry.getValue().process(specs, element);
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
                }
            }
        }

        final Map<TypeElement, JavaFile> javaFiles = new LinkedHashMap<>();
        for (final Map.Entry<TypeElement, TableSpec> entry : specs.entrySet()) {
            final TypeElement typeElement = entry.getKey();
            final TableSpec tableSpec = entry.getValue();
            try {
                final JavaFile javaFile = new TableMaker(tableSpec).brewJavaFile();
                javaFiles.put(typeElement, javaFile);
                javaFile.writeTo(mFiler);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), typeElement);
            }
        }

        brewSchemaJava(javaFiles);

        return true;
    }


    private void brewSchemaJava(Map<TypeElement, JavaFile> tables) {
        try {
            final CodeBlock.Builder codeBlock = CodeBlock.builder();
            for (final Map.Entry<TypeElement, JavaFile> entry : tables.entrySet()) {
                final JavaFile javaFile = entry.getValue();
                codeBlock.addStatement("TABLES.put($T.class, $T.INSTANCE)",
                        ClassName.get(entry.getKey()), ClassName.get(javaFile.packageName, javaFile.typeSpec.name));
            }
            JavaFile.builder("rxsqlite", TypeSpec.classBuilder("SQLite$$Schema")
                    .addModifiers(Modifier.PUBLIC)
                    .addField(FieldSpec.builder(ParameterizedTypeName.get(Map.class, Class.class, RxSQLiteTable.class),
                            "TABLES",
                            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("new $T<>()", HashMap.class)
                            .build())
                    .addStaticBlock(codeBlock.build())
                    .build())
                    .addFileComment("Generated code from RxSQLite. Do not modify!")
                    .skipJavaLangImports(true)
                    .build()
                    .writeTo(mFiler);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

}
