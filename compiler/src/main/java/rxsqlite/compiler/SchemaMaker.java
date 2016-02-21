package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Daniel Serdyukov
 */
class SchemaMaker {

    public static final String CLASS_NAME = "SQLite$$Schema";

    static JavaFile brewJava(Map<Element, String> schema) {
        final TypeSpec.Builder spec = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);
        brewCreateMethod(spec, schema);
        return JavaFile.builder(TableMaker.PACKAGE_NAME, spec.build())
                .addFileComment("Generated code from RxSQLite. Do not modify!")
                .skipJavaLangImports(true)
                .build();
    }

    static void brewCreateMethod(TypeSpec.Builder typeSpec, Map<Element, String> schema) {
        final MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("rxsqlite", "RxSQLiteClient"), "client");
        for (final Map.Entry<Element, String> entry : schema.entrySet()) {
            methodSpec.addStatement("client.registerTable($T.class, new $T())",
                    ClassName.get(entry.getKey().asType()),
                    ClassName.bestGuess(entry.getValue()));
        }
        typeSpec.addMethod(methodSpec.build());
    }

}
