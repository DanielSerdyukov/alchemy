package rxsqlite;

import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import rx.functions.Func2;
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
    public void testRawQuery() throws Exception {
        final String sql = "SELECT * FROM foo WHERE _id = ?;";
        final Object bindArg = new Object();
        Funcs.rawQuery(mClient, Object.class, sql, Collections.singletonList(bindArg)).call(mDb);
        final ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> values = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Func2> func = ArgumentCaptor.forClass(Func2.class);
        Mockito.verify(mClient).query(query.capture(), values.capture(), func.capture());
        Assert.assertThat(query.getValue(), Is.is(sql));
        Assert.assertThat(values.getValue().toArray(), IsArrayContaining.hasItemInArray(bindArg));
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
