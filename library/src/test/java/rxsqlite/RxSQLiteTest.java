package rxsqlite;

import android.text.TextUtils;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;

import rx.functions.Func1;
import rx.observers.TestSubscriber;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteRowSet;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, TextUtils.class})
public class RxSQLiteTest {

    private static RxSQLiteClient sClient;

    @Mock
    private SQLiteDb mDb;

    @Mock
    private SQLiteStmt mStmt;

    @Mock
    private SQLiteRowSet mRowSet;

    @Mock
    private RxSQLiteBinder mBinder;

    @Mock
    private RxSQLiteTable mTable;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString()))
                .thenAnswer(Answers.isEmpty());
        PowerMockito.when(TextUtils.join(Mockito.anyString(), Mockito.anyCollection()))
                .thenAnswer(Answers.joinIterable());
        if (sClient == null) {
            sClient = Mockito.spy(RxSQLiteClient.memory().build());
            RxSQLite.register(sClient);
        }
        MockitoAnnotations.initMocks(this);
        Mockito.reset(sClient);
        Mockito.doReturn(mDb).when(sClient).acquireDatabase(Mockito.<Queue>any());
        Mockito.doReturn(mTable).when(sClient).findTable(Object.class);
        Mockito.doReturn(mBinder).when(sClient).getBinder();
        Mockito.doReturn(mStmt).when(mDb).prepare(Mockito.anyString());
        Mockito.doReturn(mRowSet).when(mStmt).executeSelect();
    }

    @Test
    public void testQuery() throws Exception {
        RxSQLite.query(Object.class).toBlocking().subscribe(TestSubscriber.create());
        Mockito.verify(mTable).query(mDb, ";", Collections.emptyList(), mBinder);
    }

    @Test
    public void testQueryWithWhere() throws Exception {
        RxSQLite.query(Object.class, new RxSQLiteWhere()
                .between("age", 18, 25))
                .toBlocking()
                .subscribe(TestSubscriber.create());
        Mockito.verify(mTable).query(mDb, " WHERE age BETWEEN ? AND ?;", Arrays.<Object>asList(18, 25), mBinder);
    }

    @Test
    public void testSave() throws Exception {
        final Object expected = new Object();
        RxSQLite.save(expected).toBlocking().subscribe(TestSubscriber.create());
        Mockito.verify(mTable).save(mDb, Collections.singletonList(expected), mBinder);
    }

    @Test
    public void testSaveAll() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.saveAll(Collections
                .emptyList())
                .toBlocking()
                .subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertCompleted();

        RxSQLite.saveAll(Collections
                .singletonList(new Object()))
                .toBlocking()
                .subscribe(subscriber);
        Mockito.verify(sClient).execute(Mockito.<Func1>any());

        RxSQLite.saveAll(Arrays.asList(new Object(), new Object(), new Object()))
                .toBlocking()
                .subscribe(subscriber);
        Mockito.verify(sClient).transaction(Mockito.<Func1>any());
    }

    @Test
    public void testRemove() throws Exception {
        final Object expected = new Object();
        RxSQLite.remove(expected).toBlocking().subscribe(TestSubscriber.create());
        Mockito.verify(mTable).remove(mDb, Collections.singletonList(expected));
    }

    @Test
    public void testRemoveAll() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.removeAll(Collections
                .emptyList())
                .toBlocking()
                .subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertCompleted();

        RxSQLite.removeAll(Collections
                .singletonList(new Object()))
                .toBlocking()
                .subscribe(subscriber);
        Mockito.verify(sClient).execute(Mockito.<Func1>any());

        RxSQLite.removeAll(Arrays.asList(new Object(), new Object(), new Object()))
                .toBlocking()
                .subscribe(subscriber);
        Mockito.verify(sClient).transaction(Mockito.<Func1>any());
    }

    @Test
    public void testClear() throws Exception {
        RxSQLite.clear(Object.class).toBlocking().subscribe(TestSubscriber.create());
        Mockito.verify(mTable).clear(mDb, ";", Collections.emptyList(), mBinder);
    }

    @Test
    public void testClearWithWhere() throws Exception {
        RxSQLite.clear(Object.class, new RxSQLiteWhere()
                .between("age", 18, 25))
                .toBlocking()
                .subscribe(TestSubscriber.create());
        Mockito.verify(mTable).clear(mDb, " WHERE age BETWEEN ? AND ?;", Arrays.<Object>asList(18, 25), mBinder);
    }

    @Test
    public void testExecute() throws Exception {
        final Func1 factory = Mockito.mock(Func1.class);
        RxSQLite.execute(factory).toBlocking().subscribe(TestSubscriber.create());
        Mockito.verify(sClient).execute(factory);
    }

}
