package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteException;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class RxSQLiteTest {

    @Mock
    private Pool mPool;

    @Mock
    private RxSQLiteTable<Object> mTable;

    private RxSQLiteDbImpl mDb;

    @Before
    public void setUp() throws Exception {
        mDb = Mockito.spy(new TestRxSQLiteDb(Mockito.mock(SQLiteDb.class)));
        Mockito.doReturn(mDb).when(mPool).acquireDatabase(Mockito.anyBoolean());
        final SQLiteClient client = new SQLiteClient(mPool);
        client.mTables.put(Object.class, mTable);
        RxSQLite.sClient = client;
    }

    @Test
    public void insertEmptyList() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.insert(Collections.emptyList()).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        Mockito.verify(mPool, Mockito.never()).acquireDatabase(Mockito.anyBoolean());
    }

    @Test
    public void insertSingletonList() throws Exception {
        final List<Object> items = Collections.singletonList(new Object());
        final long[] rowIds = new long[]{1};
        final Object selected = new Object();
        Mockito.doReturn(rowIds).when(mTable).insert(mDb, items);
        Mockito.doReturn(Collections.singletonList(selected)).when(mTable).select(mDb, rowIds);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.insert(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValue(selected);
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb, Mockito.never()).begin();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void insertList() throws Exception {
        final List<Object> items = Arrays.asList(new Object(), new Object());
        final long[] rowIds = new long[]{1, 2};
        final List<Object> selected = Arrays.asList(new Object(), new Object());
        Mockito.doReturn(rowIds).when(mTable).insert(mDb, items);
        Mockito.doReturn(selected).when(mTable).select(mDb, rowIds);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.insert(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(selected.toArray());
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void insertListRollback() throws Exception {
        final List<Object> items = Arrays.asList(new Object(), new Object());
        Mockito.doThrow(SQLiteException.class).when(mTable).insert(mDb, items);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.insert(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertError(RxSQLiteException.class);
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).rollback();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void selectWhere() throws Exception {
        final List<Object> selected = Arrays.asList(new Object(), new Object());
        final Where where = new Where().equalTo("name", "test");
        Mockito.doReturn(selected).when(mTable).select(mDb, where.buildSelectSql(), where.getArgs());
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.select(Object.class, where).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(selected.toArray());
        Mockito.verify(mPool).acquireDatabase(false);
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void deleteEmptyList() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(Collections.emptyList()).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        Mockito.verify(mPool, Mockito.never()).acquireDatabase(Mockito.anyBoolean());
    }

    @Test
    public void deleteSingletonList() throws Exception {
        final List<Object> items = Collections.singletonList(new Object());
        Mockito.doReturn(items.size()).when(mTable).delete(mDb, items);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValue(items.size());
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb, Mockito.never()).begin();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void deleteList() throws Exception {
        final List<Object> items = Arrays.asList(new Object(), new Object());
        Mockito.doReturn(items.size()).when(mTable).delete(mDb, items);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(items.size());
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void deleteListRollback() throws Exception {
        final List<Object> items = Arrays.asList(new Object(), new Object());
        Mockito.doThrow(SQLiteException.class).when(mTable).delete(mDb, items);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(items).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertError(RxSQLiteException.class);
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).rollback();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

    @Test
    public void deleteWhere() throws Exception {
        final Where where = new Where().equalTo("name", "test");
        Mockito.doReturn(5).when(mTable).delete(mDb, where.buildSelectSql(), where.getArgs());
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(Object.class, where).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(5);
        Mockito.verify(mPool).acquireDatabase(true);
        Mockito.verify(mPool).releaseDatabase(mDb);
    }

}