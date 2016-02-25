package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class FuncsTest {

    @Mock
    private SQLiteDb mDb;

    @Mock
    private Types mBinder;

    @Mock
    private RxSQLiteTable mTable;

    @Mock
    private RxSQLiteClient mClient;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(mTable).when(mClient).findTable(Mockito.<Class>any());
    }

    @Test
    public void testQuery() throws Exception {
        final String selection = " WHERE _id = ?;";
        final List<Object> bindValues = Collections.singletonList(new Object());
        Funcs.query(mClient, Object.class, selection, bindValues).call(mDb);
        Mockito.verify(mTable).query(mDb, selection, bindValues);
    }

    @Test
    public void testSave() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        Funcs.save(mClient, Collections.emptyList()).call(mDb).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertCompleted();

        final List<Object> objects = Collections.singletonList(new Object());
        Funcs.save(mClient, objects).call(mDb);
        Mockito.verify(mTable).save(mDb, objects);
    }

    @Test
    public void testRemove() throws Exception {
        final TestSubscriber<Object> subscriber = TestSubscriber.create();
        Funcs.remove(mClient, Collections.emptyList()).call(mDb).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertCompleted();

        final List<Object> objects = Collections.singletonList(new Object());
        Funcs.remove(mClient, objects).call(mDb);
        Mockito.verify(mTable).remove(mDb, objects);
    }

    @Test
    public void testClear() throws Exception {
        final String selection = " WHERE _id = ?;";
        final List<Object> bindValues = Collections.singletonList(new Object());
        Funcs.clear(mClient, Object.class, selection, bindValues).call(mDb);
        Mockito.verify(mTable).clear(mDb, selection, bindValues);
    }

}
