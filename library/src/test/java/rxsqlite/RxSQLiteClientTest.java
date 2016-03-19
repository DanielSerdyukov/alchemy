package rxsqlite;

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
import rx.functions.Func2;
import rx.observers.TestSubscriber;
import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteException;
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
    private SQLiteCursor mCursor;

    @Mock
    private Action1<SQLiteDb> mOnOpen;

    @Mock
    private Action1<SQLiteDb> mOnCreate;

    @Mock
    private Action3<SQLiteDb, Integer, Integer> mOnUpgrade;

    @Mock
    private Types mTypes;

    private RxSQLiteClient mClient;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        MockitoAnnotations.initMocks(this);
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        mClient = Mockito.spy(new RxSQLiteClient(RxSQLiteClient
                .builder(databasePath, 2)
                .doOnOpen(mOnOpen)
                .doOnCreate(mOnCreate)
                .doOnUpgrade(mOnUpgrade), mTypes));
        Mockito.doReturn(0).when(mClient).getDatabaseVersion(mDb);
        Mockito.doReturn(mDb).when(mClient).openDatabase(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(mStmt).when(mDb).prepare(Mockito.anyString());
        Mockito.doReturn(mCursor).when(mStmt).executeQuery();
    }

    @Test
    public void testGetDatabaseVersion() throws Exception {
        Mockito.doCallRealMethod().when(mClient).getDatabaseVersion(mDb);
        Mockito.doAnswer(Answers.stmtStep(1)).when(mCursor).step();
        Mockito.doReturn(1L).when(mCursor).getColumnLong(0);
        Assert.assertThat(mClient.getDatabaseVersion(mDb), Is.is(1));
        Mockito.verify(mDb).prepare("PRAGMA user_version;");
        Mockito.verify(mStmt).close();
    }

    @Test
    public void testSetDatabaseVersion() throws Exception {
        mClient.setDatabaseVersion(mDb, 2);
        Mockito.verify(mDb).exec("PRAGMA user_version = 2;");
    }

    @Test
    public void testQuery() throws Exception {
        Mockito.doAnswer(Answers.stmtStep(3)).when(mCursor).step();
        final TestSubscriber<String> subscriber = TestSubscriber.create();
        final List<Object> objects = Arrays.<Object>asList(100L, "Joe");
        final Func2 factory = Mockito.mock(Func2.class);
        mClient.query("SELECT * FROM foo WHERE id = ? AND name = ?", objects, factory)
                .toBlocking()
                .subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValueCount(3);
        subscriber.assertCompleted();
        Mockito.verify(mClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.verify(mDb).prepare("SELECT * FROM foo WHERE id = ? AND name = ?");
        Mockito.verify(mTypes).bindValue(mStmt, 1, 100L);
        Mockito.verify(mTypes).bindValue(mStmt, 2, "Joe");
        Mockito.verify(factory, Mockito.times(3)).call(Mockito.<SQLiteDb>any(), Mockito.<SQLiteCursor>any());
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
        Mockito.verify(mDb).begin();
        Mockito.verify(func).call(mDb);
        Mockito.verify(mDb).commit();
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
        Mockito.verify(mDb).begin();
        Mockito.verify(func).call(mDb);
        Mockito.verify(mDb).rollback();
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
        Mockito.verify(table).create(mDb);
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
