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
public class TableMakerTest {

    @Test
    public void testGeneratedSource() throws Exception {
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.test.Foo$$Table", Arrays.asList(
                "package rxsqlite.test;",
                "import java.util.ArrayList;",
                "import java.util.List;",
                "import rx.Observable;",
                "import rxsqlite.CustomTypes;",
                "import rxsqlite.RxSQLiteTable;",
                "import sqlite4a.SQLiteCursor;",
                "import sqlite4a.SQLiteDb;",
                "import sqlite4a.SQLiteStmt;",
                "public class Foo$$Table implements RxSQLiteTable<Foo> {",
                "  private final CustomTypes mTypes;",
                "  public Foo$$Table(CustomTypes types) {",
                "    mTypes = types;",
                "  }",
                "  @Override",
                "  public void create(SQLiteDb db) {",
                "    db.exec(\"CREATE TABLE IF NOT EXISTS foo(\"",
                "      + \"_id INTEGER PRIMARY KEY ON CONFLICT REPLACE\"",
                "      + \", int INTEGER\"",
                "      + \", short INTEGER\"",
                "      + \", double REAL\"",
                "      + \", bool INTEGER\"",
                "      + \", my_float REAL\"",
                "      + \", string TEXT\"",
                "      + \", bytes BLOB\"",
                "      + \", date \" + mTypes.getType(java.util.Date.class) + \" UNIQUE\"",
                "      + \", enum_type TEXT\"",
                "      + \", UNIQUE(int, short)\"",
                "      + \");\");",
                "    db.exec(\"CREATE INDEX IF NOT EXISTS foo_idx0 ON foo(int);\");",
                "    db.exec(\"CREATE UNIQUE INDEX IF NOT EXISTS foo_idx1 ON foo(short);\");",
                "  }",
                "  @Override",
                "  public Observable<Foo> query(SQLiteDb db, String selection, Iterable<Object> bindValues) {",
                "    final SQLiteStmt stmt = db.prepare(\"SELECT * FROM foo\" + selection);",
                "    try {",
                "      final List<Foo> objects = new ArrayList<>();",
                "      int index = 0;",
                "      for (final Object value : bindValues) {",
                "        mTypes.bindValue(stmt, ++index, value);",
                "      }",
                "      final SQLiteCursor cursor = stmt.executeQuery();",
                "      while (cursor.step()) {",
                "        objects.add(instantiate(cursor));",
                "      }",
                "      return Observable.from(objects);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Foo> save(SQLiteDb db, Iterable<Foo> objects) {",
                "    final SQLiteStmt stmt = db.prepare(\"INSERT INTO foo(_id, int, short, double, bool, my_float, string, bytes, date, enum_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);\");",
                "    try {",
                "      for (final Foo object : objects) {",
                "        stmt.clearBindings();",
                "        bindStmtValues(stmt, object);",
                "        object.mLong = stmt.executeInsert();",
                "      }",
                "      return Observable.from(objects);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Integer> remove(SQLiteDb db, Iterable<Foo> objects) {",
                "    final SQLiteStmt stmt = db.prepare(\"DELETE FROM foo WHERE _id = ?;\");",
                "    try {",
                "      int affectedRows = 0;",
                "      for (final Foo object : objects) {",
                "        stmt.clearBindings();",
                "        stmt.bindLong(1, object.mLong);",
                "        affectedRows += stmt.executeUpdateDelete();",
                "      }",
                "      return Observable.just(affectedRows);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Integer> clear(SQLiteDb db, String selection, Iterable<Object> bindValues) {",
                "    final SQLiteStmt stmt = db.prepare(\"DELETE FROM foo\" + selection);",
                "    try {",
                "      int index = 0;",
                "      for (final Object value : bindValues) {",
                "        mTypes.bindValue(stmt, ++index, value);",
                "      }",
                "      return Observable.just(stmt.executeUpdateDelete());",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  private Foo instantiate(SQLiteCursor cursor) {",
                "    final Foo object = new Foo();",
                "    object.mLong = (long) cursor.getColumnLong(0);",
                "    object.mInt = (int) cursor.getColumnLong(1);",
                "    object.mShort = (short) cursor.getColumnLong(2);",
                "    object.mDouble = (double) cursor.getColumnDouble(3);",
                "    object.mBool = cursor.getColumnLong(4) > 0;",
                "    object.mFloat = (float) cursor.getColumnDouble(5);",
                "    object.mString = cursor.getColumnString(6);",
                "    object.mBytes = cursor.getColumnBlob(7);",
                "    object.mDate = mTypes.getValue(cursor, 8, java.util.Date.class);\n",
                "    object.mEnumType = mTypes.getEnumValue(cursor, 9, rxsqlite.test.Foo.EnumType.class);",
                "    return object;",
                "  }",
                "  private void bindStmtValues(SQLiteStmt stmt, Foo object) {",
                "    if (object.mLong > 0) {",
                "      stmt.bindLong(1, object.mLong);",
                "    } else {",
                "      stmt.bindNull(1);",
                "    }",
                "    stmt.bindLong(2, object.mInt);",
                "    stmt.bindLong(3, object.mShort);",
                "    stmt.bindDouble(4, object.mDouble);",
                "    stmt.bindLong(5, object.mBool ? 1 : 0);",
                "    stmt.bindDouble(6, object.mFloat);",
                "    stmt.bindString(7, object.mString);",
                "    stmt.bindBlob(8, object.mBytes);",
                "    mTypes.bindValue(stmt, 9, object.mDate);",
                "    if (object.mEnumType != null) {",
                "      stmt.bindString(10, object.mEnumType.name());",
                "    } else {",
                "      stmt.bindNull(10);",
                "    }",
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

    @Test
    public void testNotAnnotatedWithSQLiteObject() throws Exception {
        final JavaFileObject source = JavaFileObjects.forSourceLines("rxsqlite.Baz", Arrays.asList(
                "package rxsqlite.test;",
                "import rxsqlite.annotation.SQLiteObject;",
                "import rxsqlite.annotation.SQLitePk;",
                "public class Bar {",
                "  @SQLitePk",
                "  private long mLong;",
                "}"
        ));
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(source)
                .processedWith(new RxSQLiteProcessor())
                .failsToCompile();
    }

    @Test
    public void testHasNoPrimaryKey() throws Exception {
        final JavaFileObject source = JavaFileObjects.forSourceLines("rxsqlite.Baz", Arrays.asList(
                "package rxsqlite.test;",
                "import rxsqlite.annotation.SQLiteObject;",
                "import rxsqlite.annotation.SQLitePk;",
                "@SQLiteObject(\"baz\")",
                "public class Bar {",
                "  private long mLong;",
                "}"
        ));
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(source)
                .processedWith(new RxSQLiteProcessor())
                .failsToCompile();
    }

    @Test
    public void testPrimaryKeyMustBeLong() throws Exception {
        final JavaFileObject source = JavaFileObjects.forSourceLines("rxsqlite.Baz", Arrays.asList(
                "package rxsqlite.test;",
                "import rxsqlite.annotation.SQLiteObject;",
                "import rxsqlite.annotation.SQLitePk;",
                "@SQLiteObject(\"baz\")",
                "public class Bar {",
                "  @SQLitePk",
                "  private String mLong;",
                "}"
        ));
        Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(source)
                .processedWith(new RxSQLiteProcessor())
                .failsToCompile();
    }

}
