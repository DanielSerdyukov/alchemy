package rxsqlite.compiler;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;

import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
public class SchemaMakerTest {

    @Test
    public void testGeneratedSource() throws Exception {
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.SQLite$$Schema",
                Arrays.asList(
                        "package rxsqlite;",
                        "import rxsqlite.test.Bar;",
                        "public class SQLite$$Schema {",
                        "    public static void create(RxSQLiteClient client) {",
                        "        client.registerTable(Bar.class, new Bar$$Table());",
                        "    }",
                        "}"
                ));
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(Models.BAR)
                .processedWith(new RxSQLiteProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(generated);
    }

}
