package rxsqlite.compiler;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLiteCompilerTest {

    @Test
    public void generatedFiles() throws Exception {
        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("models/Foo.java"),
                        JavaFileObjects.forResource("models/Bar.java")
                ))
                .processedWith(new RxSQLiteCompiler())
                .compilesWithoutError()
                .and()
                .generatesSources(
                        JavaFileObjects.forResource("generated/Foo$$Table.java"),
                        JavaFileObjects.forResource("generated/Bar$$Table.java"),
                        JavaFileObjects.forResource("generated/SQLite$$Schema.java")
                );
    }

}