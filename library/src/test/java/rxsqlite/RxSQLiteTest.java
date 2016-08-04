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
import java.util.concurrent.TimeUnit;

import rx.functions.Func1;
import rx.observers.TestSubscriber;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class RxSQLiteTest {

    @Mock
    private SQLiteClient mClient;

    @Before
    public void setUp() throws Exception {
        RxSQLite.setClient(mClient);
    }

    @Test
    public void insertOne() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        final Object object = new Object();
        RxSQLite.insert(object).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).insert(Collections.singletonList(object), RxSQLite.ON_CHANGE);
    }

    @Test
    public void insertCollection() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        final List<Object> objects = Arrays.asList(new Object(), new Object());
        RxSQLite.insert(objects).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).insert(objects, RxSQLite.ON_CHANGE);
    }

    @Test
    public void selectAll() throws Exception {
        final Where where = new Where();
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.select(Object.class).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).select(Object.class, where.buildSelectSql(), where.getArgs());
    }

    @Test
    public void selectWhere() throws Exception {
        final Where where = new Where().equalTo("name", "test");
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.select(Object.class, where).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).select(Object.class, where.buildSelectSql(), where.getArgs());
    }

    @Test
    public void rawSelect() throws Exception {
        final String sql = "SELECT * FROM test WHERE foo = ? AND bar = ?;";
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.rawSelect(Object.class, sql, "test", 100L).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).rawSelect(Object.class, sql, Arrays.<Object>asList("test", 100L));
    }

    @Test
    public void deleteOne() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        final Object object = new Object();
        RxSQLite.delete(object).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).delete(Collections.singletonList(object), RxSQLite.ON_CHANGE);
    }

    @Test
    public void deleteCollection() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        final List<Object> objects = Arrays.asList(new Object(), new Object());
        RxSQLite.delete(objects).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).delete(objects, RxSQLite.ON_CHANGE);
    }

    @Test
    public void deleteAll() throws Exception {
        final Where where = new Where();
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(Object.class).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).delete(Object.class, where.buildSelectSql(), where.getArgs(), RxSQLite.ON_CHANGE);
    }

    @Test
    public void deleteWhere() throws Exception {
        final Where where = new Where().equalTo("name", "test");
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.delete(Object.class, where).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).delete(Object.class, where.buildSelectSql(), where.getArgs(), RxSQLite.ON_CHANGE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exec() throws Exception {
        final Func1 func = Mockito.mock(Func1.class);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        RxSQLite.exec(func).subscribe(subscriber);
        subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        Mockito.verify(mClient).exec(func);
    }

}
