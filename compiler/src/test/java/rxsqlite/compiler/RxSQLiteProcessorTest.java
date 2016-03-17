package rxsqlite.compiler;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLiteProcessorTest {

    @Test
    public void testAllTypes() throws Exception {
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forResource("rxsqlite/model/AllTypes.java"))
                .processedWith(new RxSQLiteProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(
                        JavaFileObjects.forResource("rxsqlite/model/AllTypes$$Table.java"),
                        JavaFileObjects.forResource("rxsqlite/CustomTypes.java")
                );
    }

    @Test
    public void testOneToOneRelation() throws Exception {
        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("rxsqlite/model/Foo.java"),
                        JavaFileObjects.forResource("rxsqlite/model/Bar.java"),
                        JavaFileObjects.forResource("rxsqlite/model/Baz.java")
                ))
                .processedWith(new RxSQLiteProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(
                        JavaFileObjects.forResource("rxsqlite/model/Foo$$Table.java"),
                        JavaFileObjects.forResource("rxsqlite/model/Bar$$Table.java"),
                        JavaFileObjects.forResource("rxsqlite/model/Baz$$Table.java")
                );
    }

}
