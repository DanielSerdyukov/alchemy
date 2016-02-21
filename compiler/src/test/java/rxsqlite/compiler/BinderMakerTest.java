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
public class BinderMakerTest {

    @Test
    public void testGeneratedSource() throws Exception {
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.RxSQLiteBinderWrapper",
                Arrays.asList(
                        "package rxsqlite;",
                        "import sqlite4a.SQLiteRow;",
                        "import sqlite4a.SQLiteStmt;",
                        "public class RxSQLiteBinderWrapper {",
                        "  private final RxSQLiteBinder mBinder;",
                        "  RxSQLiteBinderWrapper(RxSQLiteBinder binder) {",
                        "    mBinder = binder;",
                        "  }",
                        "  public void bindValue(SQLiteStmt stmt, int index, Object object) {",
                        "    mBinder.bindValue(stmt, index, object);",
                        "  }",
                        "  public <T> T getValue(SQLiteRow row, int index, Class<T> type) {",
                        "    return mBinder.getValue(row, index, type);",
                        "  }",
                        "  public <T extends Enum<T>> T getEnumValue(SQLiteRow row, int index, Class<T> type) {",
                        "    return mBinder.getEnumValue(row, index, type);",
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
