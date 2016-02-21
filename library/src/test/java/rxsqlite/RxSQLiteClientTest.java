package rxsqlite;

import android.content.Context;
import android.util.Log;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action3;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteException;
import sqlite4a.SQLiteRow;
import sqlite4a.SQLiteRowSet;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class RxSQLiteClientTest {

    @Mock
    private SQLiteDb mDb;

    @Mock
    private SQLiteStmt mStmt;

    @Mock
    private SQLiteRowSet mRowSet;

    @Mock
    private Action1<SQLiteDb> mOnOpen;

    @Mock
    private Action1<SQLiteDb> mOnCreate;

    @Mock
    private Action3<SQLiteDb, Integer, Integer> mOnUpgrade;

    @Mock
    private RxSQLiteBinder mBinder;

    @Mock
    private RxSQLiteType mMockType;

    private RxSQLiteClient mClient;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        MockitoAnnotations.initMocks(this);
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        mClient = Mockito.spy(RxSQLiteClient.builder(databasePath, 2)
                .doOnOpen(mOnOpen)
                .doOnCreate(mOnCreate)
                .doOnUpgrade(mOnUpgrade)
                .registerCustomType(mMockType)
                .build());
        Mockito.doReturn(0).when(mClient).getDatabaseVersion(mDb);
        Mockito.doReturn(mDb).when(mClient).openDatabase(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(mStmt).when(mDb).prepare(Mockito.anyString());
        Mockito.doReturn(mRowSet).when(mStmt).executeSelect();
        Mockito.doReturn(mBinder).when(mClient).getBinder();
    }

    @Test
    public void testMemoryBuilder() throws Exception {
        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory().build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("file::memory:?cache=shared", SQLiteDb.OPEN_READWRITE
                | SQLiteDb.OPEN_CREATE | SQLiteDb.OPEN_URI);
    }

    @Test
    public void testContextBuilder() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");
        Mockito.doReturn(databasePath).when(context).getDatabasePath(Mockito.anyString());

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.builder(context, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE);
    }

    @Test
    public void testFileBuilder() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient
                .builder(databasePath, SQLiteDb.OPEN_READONLY, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READONLY);
    }

    @Test
    public void testFileBuilderWithoutFlags() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.exists()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.builder(databasePath, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureDatabasePathExists() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        RxSQLiteClient.ensureDatabasePathExists(databasePath);
    }

    @Test
    public void testGetDatabaseVersion() throws Exception {
        Mockito.doCallRealMethod().when(mClient).getDatabaseVersion(mDb);
        Mockito.doAnswer(Answers.stmtStep(1)).when(mRowSet).step();
        Mockito.doReturn(1L).when(mRowSet).getColumnLong(0);
        Assert.assertThat(mClient.getDatabaseVersion(mDb), Is.is(1));
        Mockito.verify(mDb).prepare("PRAGMA user_version;");
        Mockito.verify(mStmt).close();
    }

    @Test
    public void testSetDatabaseVersion() throws Exception {
        mClient.setDatabaseVersion(mDb, 2);
        Mockito.verify(mDb).exec("PRAGMA user_version = 2;", null);
    }

    @Test
    public void testQuery() throws Exception {
        Mockito.doAnswer(Answers.stmtStep(3)).when(mRowSet).step();
        final TestSubscriber<String> subscriber = TestSubscriber.create();
        final List<Object> objects = Arrays.<Object>asList(100L, "Joe");
        final Func1 factory = Mockito.mock(Func1.class);
        mClient.query("SELECT * FROM foo WHERE id = ? AND name = ?", objects, factory)
                .toBlocking()
                .subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValueCount(3);
        subscriber.assertCompleted();
        Mockito.verify(mClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.verify(mDb).prepare("SELECT * FROM foo WHERE id = ? AND name = ?");
        Mockito.verify(mBinder).bindValue(mStmt, 1, 100L);
        Mockito.verify(mBinder).bindValue(mStmt, 2, "Joe");
        Mockito.verify(factory, Mockito.times(3)).call(Mockito.any(SQLiteRow.class));
        Mockito.verify(mStmt).close();
        Mockito.verify(mClient).releaseDatabase(Mockito.<Queue>any(), Mockito.<SQLiteDb>any());
    }

    @Test
    public void testExecute() throws Exception {
        final TestSubscriber<Integer> subscriber = TestSubscriber.create();
        final Observable<Integer> observable = Observable.just(123);
        final Func1 func = Mockito.mock(Func1.class);
        Mockito.doReturn(observable).when(func).call(Mockito.any(SQLiteDb.class));
        mClient.execute(func).toBlocking().subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValue(123);
        subscriber.assertCompleted();
        Mockito.verify(mClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.verify(func).call(mDb);
        Mockito.verify(mClient).releaseDatabase(Mockito.<Queue>any(), Mockito.<SQLiteDb>any());
    }

    @Test
    public void testCommitTransaction() throws Exception {
        final TestSubscriber<Integer> subscriber = TestSubscriber.create();
        final Observable<Integer> observable = Observable.just(345);
        final Func1 func = Mockito.mock(Func1.class);
        Mockito.doReturn(observable).when(func).call(Mockito.any(SQLiteDb.class));
        mClient.transaction(func).toBlocking().subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValue(345);
        subscriber.assertCompleted();
        Mockito.verify(mClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.verify(mDb).exec("BEGIN;", null);
        Mockito.verify(func).call(mDb);
        Mockito.verify(mDb).exec("COMMIT;", null);
        Mockito.verify(mClient).releaseDatabase(Mockito.<Queue>any(), Mockito.<SQLiteDb>any());
    }

    @Test
    public void testRollbackTransaction() throws Exception {
        final TestSubscriber<Integer> subscriber = TestSubscriber.create();
        final Func1 func = Mockito.mock(Func1.class);
        Mockito.doThrow(SQLiteException.class).when(func).call(Mockito.<SQLiteDb>any());
        mClient.transaction(func).toBlocking().subscribe(subscriber);
        subscriber.assertError(SQLiteException.class);
        Mockito.verify(mClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.verify(mDb).exec("BEGIN;", null);
        Mockito.verify(func).call(mDb);
        Mockito.verify(mDb).exec("ROLLBACK;", null);
        Mockito.verify(mClient).releaseDatabase(Mockito.<Queue>any(), Mockito.<SQLiteDb>any());
    }

    @Test
    public void testClose() throws Exception {
        mClient.close();
        Mockito.verify(mClient).close(Mockito.<Queue>any());

        final Queue<SQLiteDb> connections = Mockito.spy(new LinkedList<SQLiteDb>());
        connections.add(mDb);
        mClient.close(connections);
        Mockito.verify(mDb).close();
        Mockito.verify(connections).clear();
    }

    @Test
    public void testAcquireDatabase() throws Exception {
        final Queue<SQLiteDb> connections = Mockito.mock(Queue.class);
        mClient.acquireDatabase(connections);
        Mockito.verify(mClient).openAndConfigureDatabase();

        Mockito.doReturn(mDb).when(connections).poll();
        Assert.assertThat(mClient.acquireDatabase(connections), Is.is(mDb));
    }

    @Test
    public void testReleaseDatabase() throws Exception {
        final Queue<SQLiteDb> connections = Mockito.mock(Queue.class);
        mClient.releaseDatabase(connections, mDb);
        Mockito.verify(connections).add(mDb);
    }

    @Test
    public void testOpenAndCreateDatabase() throws Exception {
        mClient.openAndConfigureDatabase();
        Mockito.verify(mClient).dispatchDatabaseOpen(mDb);
        Mockito.verify(mClient).dispatchDatabaseCreate(mDb);
        Mockito.verify(mClient).setDatabaseVersion(mDb, 2);
    }

    @Test
    public void testOpenAndUpgradeDatabase() throws Exception {
        Mockito.doReturn(1).when(mClient).getDatabaseVersion(mDb);
        mClient.openAndConfigureDatabase();
        Mockito.verify(mClient).dispatchDatabaseOpen(mDb);
        Mockito.verify(mClient).dispatchDatabaseUpgrade(mDb, 1, 2);
        Mockito.verify(mClient).setDatabaseVersion(mDb, 2);
    }

    @Test
    public void testDispatchDatabaseOpen() throws Exception {
        mClient.dispatchDatabaseOpen(mDb);
        Mockito.verify(mOnOpen).call(mDb);
    }

    @Test
    public void testDispatchDatabaseCreate() throws Exception {
        mClient.dispatchDatabaseCreate(mDb);
        Mockito.verify(mOnCreate).call(mDb);
    }

    @Test
    public void testCreateAutoGeneratedSchema() throws Exception {
        final RxSQLiteTable table = Mockito.mock(RxSQLiteTable.class);
        final List<RxSQLiteTable<?>> tables = Collections.<RxSQLiteTable<?>>singletonList(table);
        mClient.createAutoGeneratedSchema(mDb, tables);
        Mockito.verify(table).create(mDb, mBinder);
    }

    @Test
    public void testDispatchDatabaseUpgrade() throws Exception {
        mClient.dispatchDatabaseUpgrade(mDb, 2, 3);
        Mockito.verify(mOnUpgrade).call(mDb, 2, 3);
    }

    @Test
    public void testFindTable() throws Exception {
        mClient.registerTable(Object.class, Mockito.mock(RxSQLiteTable.class));
        Assert.assertThat(mClient.findTable(Object.class), IsNull.notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSuchTableForType() throws Exception {
        mClient.findTable(Object.class);
    }

}
