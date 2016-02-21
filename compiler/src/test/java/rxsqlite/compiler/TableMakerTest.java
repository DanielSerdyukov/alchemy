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
        final JavaFileObject generated = JavaFileObjects.forSourceLines("rxsqlite.Foo$$Table", Arrays.asList(
                "package rxsqlite;",
                "import java.util.ArrayList;",
                "import java.util.List;",
                "import rx.Observable;",
                "import rxsqlite.test.Foo;",
                "import rxsqlite.test.Foo$$Helper;",
                "import sqlite4a.SQLiteDb;",
                "import sqlite4a.SQLiteRowSet;",
                "import sqlite4a.SQLiteStmt;",
                "class Foo$$Table implements RxSQLiteTable<Foo> {",
                "  @Override",
                "  public void create(SQLiteDb db, RxSQLiteBinder binder) {",
                "    db.exec(\"CREATE TABLE IF NOT EXISTS foo(\"",
                "      + \"_id INTEGER PRIMARY KEY ON CONFLICT REPLACE\"",
                "      + \", int INTEGER\"",
                "      + \", short INTEGER\"",
                "      + \", double REAL\"",
                "      + \", my_float REAL\"",
                "      + \", string TEXT\"",
                "      + \", bytes BLOB\"",
                "      + \", date \" + binder.getType(java.util.Date.class) + \" UNIQUE\"",
                "      + \", enum_type TEXT\"",
                "      + \", UNIQUE(int, short)\"",
                "      + \");\", null);",
                "  }",
                "  @Override",
                "  public Observable<Foo> query(SQLiteDb db, String selection, Iterable<Object> bindValues, RxSQLiteBinder binder) {",
                "    final SQLiteStmt stmt = db.prepare(\"SELECT * FROM foo\" + selection);",
                "    try {",
                "      final List<Foo> objects = new ArrayList<>();",
                "      int index = 0;",
                "      for (final Object value : bindValues) {",
                "        binder.bindValue(stmt, ++index, value);",
                "      }",
                "      final RxSQLiteBinderWrapper binderWrapper = new RxSQLiteBinderWrapper(binder);",
                "      final SQLiteRowSet rows = stmt.executeSelect();",
                "      while (rows.step()) {",
                "        objects.add(Foo$$Helper.create(rows, binderWrapper));",
                "      }",
                "      return Observable.from(objects);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Foo> save(SQLiteDb db, Iterable<Foo> objects, RxSQLiteBinder binder) {",
                "    final SQLiteStmt stmt = db.prepare(\"INSERT INTO foo(_id, int, short, double, my_float, string, bytes, date, enum_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);\");",
                "    try {",
                "      final RxSQLiteBinderWrapper binderWrapper = new RxSQLiteBinderWrapper(binder);",
                "      for (final Foo object : objects) {",
                "        stmt.clearBindings();",
                "        Foo$$Helper.bind(stmt, object, binderWrapper);",
                "        stmt.execute();",
                "        Foo$$Helper.setPrimaryKey(object, db.getLastInsertRowId());",
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
                "        Foo$$Helper.bindPrimaryKey(stmt, object);",
                "        affectedRows += stmt.execute();",
                "      }",
                "      return Observable.just(affectedRows);",
                "    } finally {",
                "      stmt.close();",
                "    }",
                "  }",
                "  @Override",
                "  public Observable<Integer> clear(SQLiteDb db, String selection, Iterable<Object> bindValues, RxSQLiteBinder binder) {",
                "    final SQLiteStmt stmt = db.prepare(\"DELETE FROM foo\" + selection);",
                "    try {",
                "      int index = 0;",
                "      for (final Object value : bindValues) {",
                "        binder.bindValue(stmt, ++index, value);",
                "      }",
                "      return Observable.just(stmt.execute());",
                "    } finally {",
                "      stmt.close();",
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
