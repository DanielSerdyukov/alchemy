package rxsqlite;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteClientTest {

    @Mock
    private SQLitePool mPool;

    @Mock
    private RxSQLiteDb mWritableDb;

    @Mock
    private RxSQLiteDb mReadableDb;

    @Mock
    private SQLiteTable<Object> mTable;

    private SQLiteClient mClient;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(mWritableDb).when(mPool).acquireDatabase(true);
        Mockito.doReturn(mReadableDb).when(mPool).acquireDatabase(false);
        final Map<Class<?>, SQLiteTable<?>> tables = new HashMap<>();
        tables.put(Object.class, mTable);
        mClient = new SQLiteClient(mPool, tables);
    }

    @Test
    public void insert() throws Exception {
        final List<Object> insert = Arrays.asList(new Object(), new Object());
        final List<Object> expected = Arrays.asList(new Object(), new Object());
        final long[] rowIds = {1, 2};
        Mockito.doReturn(rowIds).when(mTable).insert(mWritableDb, insert);
        Mockito.doReturn(expected).when(mTable).select(mReadableDb, rowIds);
        Assert.assertThat(mClient.insert(insert, RxSQLite.ON_CHANGE), IsEqual.<Object>equalTo(expected));
    }

    @Test
    public void select() throws Exception {
        final Where where = new Where();
        final List<Object> expected = Arrays.asList(new Object(), new Object());
        Mockito.doReturn(expected).when(mTable).select(mReadableDb, where.buildSelectSql(), where.getArgs());
        Assert.assertThat(mClient.select(Object.class, where.buildSelectSql(), where.getArgs()),
                IsEqual.<Object>equalTo(expected));
    }

    @Test
    public void rawSelect() throws Exception {
        final List<Object> expected = Arrays.asList(new Object(), new Object());
        final String sql = "SELECT * FROM foo;";
        final List<Object> args = Collections.emptyList();
        Mockito.doReturn(expected).when(mTable).rawSelect(mReadableDb, sql, args);
        Assert.assertThat(mClient.rawSelect(Object.class, sql, args), IsEqual.<Object>equalTo(expected));
    }

    @Test
    public void delete() throws Exception {
        final int affectedRows = 10;
        final List<Object> delete = Arrays.asList(new Object(), new Object());
        Mockito.doAnswer(MockAnswers.sequence(1)).when(mTable).getId(Mockito.anyObject());
        Mockito.doReturn(affectedRows).when(mTable).delete(mWritableDb, new long[]{1, 2});
        Assert.assertThat(mClient.delete(delete, RxSQLite.ON_CHANGE), Is.is(affectedRows));
    }

    @Test
    public void deleteWhere() throws Exception {
        final int affectedRows = 10;
        final Where where = new Where();
        Mockito.doReturn(affectedRows).when(mTable).delete(mWritableDb, where.buildDeleteSql(), where.getArgs());
        Assert.assertThat(mClient.delete(Object.class, where.buildDeleteSql(), where.getArgs(), RxSQLite.ON_CHANGE),
                IsEqual.equalTo(affectedRows));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exec() throws Exception {
        final Observable<Object> expected = Observable.empty();
        final Func1 func = Mockito.mock(Func1.class);
        Mockito.doReturn(expected).when(func).call(mWritableDb);
        Assert.assertThat(mClient.exec(func), Is.<Object>is(expected));
    }

    @Test(expected = RxSQLiteException.class)
    public void noSuchTable() throws Exception {
        mClient.findTable(Void.class);
    }

}