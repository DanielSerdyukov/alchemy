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
                "import sqlite4a.SQLiteDb;",
                "import sqlite4a.SQLiteRow;",
                "import sqlite4a.SQLiteRowSet;",
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
                "      + \", my_float REAL\"",
                "      + \", string TEXT\"",
                "      + \", bytes BLOB\"",
                "      + \", date \" + mTypes.getType(java.util.Date.class) + \" UNIQUE\"",
                "      + \", enum_type TEXT\"",
                "      + \", UNIQUE(int, short)\"",
                "      + \");\", null);",
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
                "      final SQLiteRowSet rows = stmt.executeSelect();",
                "      while (rows.step()) {",
                "        objects.add(instantiate(rows));",
                "      }",
                "      return Observable.from(objects);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Foo> save(SQLiteDb db, Iterable<Foo> objects) {",
                "    final SQLiteStmt stmt = db.prepare(\"INSERT INTO foo(_id, int, short, double, my_float, string, bytes, date, enum_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);\");",
                "    try {",
                "      for (final Foo object : objects) {",
                "        stmt.clearBindings();",
                "        bindStmtValues(stmt, object);",
                "        stmt.execute();",
                "        object.mLong = db.getLastInsertRowId();",
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
                "        affectedRows += stmt.execute();",
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
                "      return Observable.just(stmt.execute());",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  private Foo instantiate(SQLiteRow row) {",
                "    final Foo object = new Foo();",
                "    object.mLong = (long) row.getColumnLong(0);",
                "    object.mInt = (int) row.getColumnLong(1);",
                "    object.mShort = (short) row.getColumnLong(2);",
                "    object.mDouble = (double) row.getColumnDouble(3);",
                "    object.mFloat = (float) row.getColumnDouble(4);",
                "    object.mString = row.getColumnString(5);",
                "    object.mBytes = row.getColumnBlob(6);",
                "    object.mDate = mTypes.getValue(row, 7, java.util.Date.class);\n",
                "    object.mEnumType = mTypes.getEnumValue(row, 8, rxsqlite.test.Foo.EnumType.class);",
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
                "    stmt.bindDouble(5, object.mFloat);",
                "    stmt.bindString(6, object.mString);",
                "    stmt.bindBlob(7, object.mBytes);",
                "    mTypes.bindValue(stmt, 8, object.mDate);",
                "    if (object.mEnumType != null) {",
                "      stmt.bindString(9, object.mEnumType.name());",
                "    } else {",
                "      stmt.bindNull(9);",
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
