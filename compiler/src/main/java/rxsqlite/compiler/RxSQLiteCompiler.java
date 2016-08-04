package rxsqlite.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.PrintWriter;
import java.io.StringWriter;
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

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;
import rxsqlite.annotation.SQLiteStringList;

/**
 * @author Daniel Serdyukov
 */
@AutoService(Processor.class)
public class RxSQLiteCompiler extends AbstractProcessor {

    private final Map<Class<? extends Annotation>, AnnotationRule> mRules = new LinkedHashMap<>();

    private Filer mFiler;

    @Override
    @SuppressWarnings("deprecation")
    public synchronized void init(ProcessingEnvironment pEnv) {
        super.init(pEnv);
        mFiler = pEnv.getFiler();
        mRules.put(SQLiteObject.class, new SQLiteObjectRule(pEnv));
        mRules.put(SQLitePk.class, new SQLitePkRule(pEnv));
        mRules.put(SQLiteColumn.class, new SQLiteColumnRule(pEnv));
        mRules.put(SQLiteRelation.class, new SQLiteRelationRule(pEnv));
        mRules.put(SQLiteStringList.class, new SQLiteStringListRule(pEnv));
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
        final Map<TypeElement, TableSpec> specs = new HashMap<>();
        for (final Map.Entry<Class<? extends Annotation>, AnnotationRule> rule : mRules.entrySet()) {
            final Set<? extends Element> elements = rEnv.getElementsAnnotatedWith(rule.getKey());
            process(specs, elements, rule.getValue());
        }
        try {
            writeJavaFile(makeAbstractTable());
        } catch (Exception e) {
            error(e);
        }
        final Map<TypeElement, JavaFile> tables = new HashMap<>();
        for (final Map.Entry<TypeElement, TableSpec> entry : specs.entrySet()) {
            try {
                tables.put(entry.getKey(), writeJavaFile(new TableMaker(entry.getValue(), entry.getKey()).brewJava()));
            } catch (Exception e) {
                error(entry.getKey(), e);
            }
        }
        try {
            writeJavaFile(makeSQLiteSchema(tables));
        } catch (Exception e) {
            error(e);
        }
        return true;
    }

    private void process(Map<TypeElement, TableSpec> specs, Set<? extends Element> elements,
            AnnotationRule rule) {
        for (final Element element : elements) {
            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }
            try {
                rule.process(TableSpec.make(specs, rule.getOriginType(element)), element, specs);
            } catch (Exception e) {
                error(element, e);
            }
        }
    }

    private JavaFile.Builder makeAbstractTable() throws Exception {
        return JavaFile.builder(Consts.PACKAGE, TypeSpec.classBuilder(Consts.ABSTRACT_TABLE.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(Consts.T_VAR)
                .superclass(Consts.generic(Consts.R_TABLE, "T"))
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Consts.J_STRING, "name")
                        .addParameter(ArrayTypeName.of(Consts.J_STRING), "columns")
                        .addParameter(TypeName.BOOLEAN, "hasRelations")
                        .addStatement("super(name, columns, hasRelations)")
                        .build())
                .build());
    }

    private JavaFile.Builder makeSQLiteSchema(Map<TypeElement, JavaFile> tables) throws Exception {
        final TypeSpec.Builder schema = TypeSpec.classBuilder(Consts.SQLITE_SCHEMA.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(Consts.mapOf(Consts.J_CLASS_W, Consts.wildcard(Consts.R_TABLE)), "TABLES",
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", Consts.J_HASH_MAP)
                        .build());
        final MethodSpec.Builder init = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Consts.mapOf(Consts.J_CLASS_W, Consts.wildcard(Consts.R_TABLE)));
        for (final Map.Entry<TypeElement, JavaFile> entry : tables.entrySet()) {
            final JavaFile javaFile = entry.getValue();
            init.addStatement("TABLES.put($T.class, new $T())", ClassName.get(entry.getKey()),
                    ClassName.bestGuess(javaFile.packageName + "." + javaFile.typeSpec.name));
        }
        init.addStatement("return $T.unmodifiableMap(TABLES)", Consts.J_COLLECTIONS);
        schema.addMethod(init.build());
        final TypeVariableName typeVar = TypeVariableName.get("T");
        schema.addMethod(MethodSpec.methodBuilder("findTable")
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(typeVar)
                .addParameter(Consts.generic(Consts.J_CLASS, typeVar), "clazz")
                .returns(Consts.generic(Consts.ABSTRACT_TABLE, typeVar))
                .addStatement("final $T table = TABLES.get(clazz)", Consts.wildcard(Consts.R_TABLE))
                .beginControlFlow("if (table == null)")
                .addStatement("throw new IllegalArgumentException($S + clazz)", "No such table for ")
                .endControlFlow()
                .addStatement("return ($T) table", Consts.generic(Consts.ABSTRACT_TABLE, typeVar))
                .build());
        return JavaFile.builder(Consts.PACKAGE, schema.build());
    }

    private JavaFile writeJavaFile(JavaFile.Builder builder) throws Exception {
        final JavaFile javaFile = builder.skipJavaLangImports(true)
                .addFileComment("Generated code from RxSQLite. Do not modify!")
                .build();
        javaFile.writeTo(mFiler);
        return javaFile;
    }

    private void error(Element element, Exception e) {
        final StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, stackTrace.toString(), element);
    }

    private void error(Exception e) {
        final StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, stackTrace.toString());
    }

}
