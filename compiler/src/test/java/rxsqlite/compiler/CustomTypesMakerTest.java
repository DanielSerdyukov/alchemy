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
public class CustomTypesMakerTest {

    @Test
    public void testGeneratedSource() throws Exception {
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.CustomTypes",
                Arrays.asList(
                        "package rxsqlite;",
                        "import sqlite4a.SQLiteRow;",
                        "import sqlite4a.SQLiteStmt;",
                        "public class CustomTypes {",
                        "  private final Types mTypes;",
                        "  CustomTypes(Types types) {",
                        "    mTypes = types;",
                        "  }",
                        "  public String getType(Class type) {",
                        "    return mTypes.getType(type);",
                        "  }",
                        "  public void bindValue(SQLiteStmt stmt, int index, Object object) {",
                        "    mTypes.bindValue(stmt, index, object);",
                        "  }",
                        "  public <T> T getValue(SQLiteRow row, int index, Class<T> type) {",
                        "    return mTypes.getValue(row, index, type);",
                        "  }",
                        "  public <T extends Enum<T>> T getEnumValue(SQLiteRow row, int index, Class<T> type) {",
                        "    return mTypes.getEnumValue(row, index, type);",
                        "  }",
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
