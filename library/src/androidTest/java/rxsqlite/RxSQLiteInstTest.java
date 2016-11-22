package rxsqlite;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rxsqlite.model.bar.Bar;
import rxsqlite.model.baz.Baz;
import rxsqlite.model.foo.Foo;
import sqlite4a.SQLite;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteLog;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class RxSQLiteInstTest {

    static {
        SQLite.loadLibrary(InstrumentationRegistry.getContext());
    }

    private BlockingQueue<String> mLogCat;

    private static Foo createFoo(String sv, double dv, byte[] bytes) {
        final Foo foo = new Foo();
        foo.setString(sv);
        foo.setDouble(dv);
        foo.setBytes(bytes);
        return foo;
    }

    @Before
    public void setUp() throws Exception {
        mLogCat = new LinkedBlockingQueue<>();
        RxSQLite.configure()
                .doOnOpen(new Action1<SQLiteDb>() {
                    @Override
                    public void call(SQLiteDb db) {
                        db.setLogger(new SQLiteLog() {
                            @Override
                            public void log(String sql) {
                                Log.e(RxSQLiteInstTest.class.getName(), sql);
                                mLogCat.add(sql);
                            }
                        });
                    }
                })
                .apply();
    }

    @Test
    public void schema() throws Exception {
        final TestSubscriber<Void> subscriber = TestSubscriber.create();
        RxSQLite.exec(new Func1<SQLiteDb, Observable<Void>>() {
            @Override
            public Observable<Void> call(SQLiteDb db) {
                return Observable.empty();
            }
        }).subscribe(subscriber);
        subscriber.assertTerminalEvent();
        subscriber.assertNoErrors();

        Assert.assertThat(mLogCat, IsCollectionContaining.hasItems(
                "PRAGMA foreign_keys = ON;",
                "PRAGMA user_version;",
                "CREATE TABLE IF NOT EXISTS foo (_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, string TEXT, double REAL, bytes BLOB);",
                "CREATE TABLE IF NOT EXISTS bar (_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, uuid TEXT);",
                "CREATE TABLE IF NOT EXISTS baz (_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, uuid TEXT);",
                "CREATE TABLE IF NOT EXISTS bar_mStrings (fk INTEGER REFERENCES bar(_id) ON DELETE CASCADE ON UPDATE CASCADE, value TEXT);",
                "CREATE INDEX IF NOT EXISTS idx_bar_mStrings_fk ON bar_mStrings(fk);",
                "CREATE TABLE IF NOT EXISTS bar_mBaz (lfk INTEGER REFERENCES bar(_id) ON DELETE CASCADE ON UPDATE CASCADE, rfk INTEGER, UNIQUE(lfk, rfk) ON CONFLICT IGNORE);",
                "CREATE TRIGGER IF NOT EXISTS delete_bar_mBaz BEFORE DELETE ON bar_mBaz FOR EACH ROW BEGIN DELETE FROM baz WHERE baz._id = OLD.rfk; END;",
                "CREATE TABLE IF NOT EXISTS bar_mBazList (lfk INTEGER REFERENCES bar(_id) ON DELETE CASCADE ON UPDATE CASCADE, rfk INTEGER, UNIQUE(lfk, rfk) ON CONFLICT IGNORE);",
                "CREATE TRIGGER IF NOT EXISTS delete_bar_mBazList BEFORE DELETE ON bar_mBazList FOR EACH ROW BEGIN DELETE FROM baz WHERE baz._id = OLD.rfk; END;",
                "PRAGMA user_version = 1;"
        ));
    }

    @Test
    public void insert() throws Exception {
        TestSubscriber<Foo> fs = TestSubscriber.create();
        final Foo foo = createFoo("Foo#1", 1.23, new byte[]{4, 5, 6});
        RxSQLite.insert(foo).subscribe(fs);
        fs.awaitTerminalEvent();
        fs.assertNoErrors();
        Assert.assertThat(foo.getId(), Is.is(1L));

        fs = TestSubscriber.create();
        RxSQLite.insert(Arrays.asList(
                createFoo("Foo#2", 2.22, new byte[]{2, 2, 2}),
                createFoo("Foo#3", 3.33, new byte[]{3, 3, 3}),
                createFoo("Foo#4", 4.44, new byte[]{4, 4, 4})
        )).subscribe(fs);
        fs.awaitTerminalEvent();
        fs.assertNoErrors();
        fs.assertValueCount(3);

        TestSubscriber<Bar> bs = TestSubscriber.create();
        RxSQLite.insert(new Bar()).subscribe(bs);
        bs.awaitTerminalEvent();
        bs.assertNoErrors();

        final List<Bar> bars = new ArrayList<>();
        final Bar bar = new Bar();
        bar.setUuid("Bar#2");
        final Baz baz = new Baz();
        baz.setUuid("Bar-2-Baz-1");
        bar.setBaz(baz);
        for (int j = 0; j < 3; ++j) {
            final Baz bazOfList = new Baz();
            bazOfList.setUuid("Bar-2-Baz[" + j + "]");
            bar.addBaz(bazOfList);
            bar.addString("Bar-2-String[" + j + "]");
        }
        bars.add(bar);

        bs = TestSubscriber.create();
        RxSQLite.insert(bars).subscribe(bs);
        bs.awaitTerminalEvent();
        bs.assertNoErrors();

        Assert.assertThat(mLogCat, IsCollectionContaining.hasItems(
                "INSERT INTO foo VALUES(NULL, 'Foo#1', 1.23, x'040506');",
                "SELECT * FROM foo WHERE _id IN(1);",
                "BEGIN;",
                "INSERT INTO foo VALUES(NULL, 'Foo#2', 2.22, x'020202');",
                "INSERT INTO foo VALUES(NULL, 'Foo#3', 3.33, x'030303');",
                "INSERT INTO foo VALUES(NULL, 'Foo#4', 4.44, x'040404');",
                "COMMIT;",
                "SELECT * FROM foo WHERE _id IN(2, 3, 4);",
                "BEGIN;",
                "INSERT INTO bar VALUES(NULL, NULL);",
                "COMMIT;",
                "SELECT * FROM bar WHERE _id IN(1);",
                "SELECT r.* FROM baz AS r, bar_mBaz AS rel WHERE r._id = rel.rfk AND rel.lfk = 1 LIMIT 1;",
                "SELECT r.* FROM baz AS r, bar_mBazList AS rel WHERE r._id = rel.rfk AND rel.lfk = 1;",
                "SELECT value FROM bar_mStrings WHERE fk = 1;",
                "BEGIN;",
                "INSERT INTO bar VALUES(NULL, 'Bar#2');",
                "INSERT INTO baz VALUES(NULL, 'Bar-2-Baz-1');",
                "INSERT INTO bar_mBaz VALUES(2, 1);",
                "INSERT INTO baz VALUES(NULL, 'Bar-2-Baz[0]');",
                "INSERT INTO baz VALUES(NULL, 'Bar-2-Baz[1]');",
                "INSERT INTO baz VALUES(NULL, 'Bar-2-Baz[2]');",
                "INSERT INTO bar_mBazList VALUES(2, 2);",
                "INSERT INTO bar_mBazList VALUES(2, 3);",
                "INSERT INTO bar_mBazList VALUES(2, 4);",
                "INSERT INTO bar_mStrings VALUES(2, 'Bar-2-String[0]');",
                "INSERT INTO bar_mStrings VALUES(2, 'Bar-2-String[1]');",
                "INSERT INTO bar_mStrings VALUES(2, 'Bar-2-String[2]');",
                "COMMIT;",
                "SELECT * FROM bar WHERE _id IN(2);",
                "SELECT r.* FROM baz AS r, bar_mBaz AS rel WHERE r._id = rel.rfk AND rel.lfk = 2 LIMIT 1;",
                "SELECT r.* FROM baz AS r, bar_mBazList AS rel WHERE r._id = rel.rfk AND rel.lfk = 2;",
                "SELECT value FROM bar_mStrings WHERE fk = 2;"
        ));
    }

    @Test
    public void removeDatabase() throws Exception {
        final File databasePath = InstrumentationRegistry.getContext().getDatabasePath("main.db");
        RxSQLite.sLockdown = false;
        RxSQLite.configure()
                .databasePath(databasePath)
                .doOnOpen(new Action1<SQLiteDb>() {
                    @Override
                    public void call(SQLiteDb db) {
                        db.setLogger(new SQLiteLog() {
                            @Override
                            public void log(String sql) {
                                Log.e(RxSQLiteInstTest.class.getName(), sql);
                            }
                        });
                    }
                })
                .apply();

        TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.insert(new Foo()).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        Assert.assertThat(databasePath.exists(), Is.is(true));

        subscriber = TestSubscriber.create();
        RxSQLite.wipe().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        Assert.assertThat(databasePath.exists(), Is.is(false));
    }

    @After
    public void tearDown() throws Exception {
        RxSQLite.sLockdown = false;
    }

}
