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
public class HelperMakerTest {

    @Test
    public void testGeneratedSource() throws Exception {
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.test.Foo$$Helper",
                Arrays.asList(
                        "package rxsqlite.test;",
                        "import rxsqlite.RxSQLiteBinderWrapper;",
                        "import sqlite4a.SQLiteRow;",
                        "import sqlite4a.SQLiteStmt;",
                        "public class Foo$$Helper {",
                        "  public static Foo create(SQLiteRow row, RxSQLiteBinderWrapper binder) {",
                        "    final Foo object = new Foo();",
                        "    object.mLong = (long) row.getColumnLong(0);",
                        "    object.mInt = (int) row.getColumnLong(1);",
                        "    object.mShort = (short) row.getColumnLong(2);",
                        "    object.mDouble = (double) row.getColumnDouble(3);",
                        "    object.mFloat = (float) row.getColumnDouble(4);",
                        "    object.mString = row.getColumnString(5);",
                        "    object.mBytes = row.getColumnBlob(6);",
                        "    object.mDate = binder.getValue(row, 7, java.util.Date.class);\n",
                        "    object.mEnumType = binder.getEnumValue(row, 8, rxsqlite.test.Foo.EnumType.class);",
                        "    return object;",
                        "  }",
                        "  public static void bind(SQLiteStmt stmt, Foo object, RxSQLiteBinderWrapper binder) {",
                        "    if (object.mLong > 0) {",
                        "      stmt.bindLong(1, object.mLong);",
                        "    } else {",
                        "      stmt.bindNull(1);",
                        "    }",
                        "    stmt.bindLong(2, object.mInt);",
                        "    stmt.bindLong(3, object.mShort);",
                        "    stmt.bindDouble(4, object.mDouble);",
                        "    stmt.bindDouble(5, object.mFloat);",
                        "    stmt.bindString(6, object.mString);",
                        "    stmt.bindBlob(7, object.mBytes);",
                        "    binder.bindValue(stmt, 8, object.mDate);",
                        "    if (object.mEnumType != null) {",
                        "      stmt.bindString(9, object.mEnumType.name());",
                        "    } else {",
                        "      stmt.bindNull(9);",
                        "    }",
                        "  }",
                        "  public static void bindPrimaryKey(SQLiteStmt stmt, Foo object) {",
                        "    stmt.bindLong(1, object.mLong);",
                        "  }",
                        "  public static void setPrimaryKey(Foo object, long rowId) {",
                        "    object.mLong = rowId;",
                        "  }",
                        "}"
                ));
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(Models.FOO)
                .processedWith(new RxSQLiteProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(generated);
    }

}
