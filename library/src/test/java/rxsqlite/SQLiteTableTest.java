package rxsqlite;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;
import rxsqlite.bindings.RxSQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteTableTest {

    @Mock
    private RxSQLiteDb mDb;

    @Mock
    private RxSQLiteStmt mStmt;

    @Mock
    private RxSQLiteCursor mCursor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private SQLiteTable<Object> mTable;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(mStmt).when(mDb).prepare(Mockito.anyString());
        Mockito.doReturn(mCursor).when(mStmt).executeSelect();
        Mockito.doReturn("test").when(mTable).getName();
        Mockito.doReturn(new String[]{"_id", "c_string", "c_double", "c_blob"}).when(mTable).getColumns();
    }

    @Test
    public void singleInsert() throws Exception {
        final Object object = new Object();
        Mockito.doAnswer(MockAnswers.sequence(1)).when(mStmt).executeInsert();
        Assert.assertThat(mTable.insert(mDb, Collections.singletonList(object)), IsEqual.equalTo(new long[]{1}));
        Mockito.verify(mDb).prepare("INSERT INTO test VALUES(?, ?, ?, ?);");
        Mockito.verify(mTable).bind(mStmt, object);
        Mockito.verify(mDb, Mockito.never()).begin();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void batchInsert() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object());
        Mockito.doAnswer(MockAnswers.sequence(1)).when(mStmt).executeInsert();
        Assert.assertThat(mTable.insert(mDb, objects), IsEqual.equalTo(new long[]{1, 2}));
        Mockito.verify(mDb).prepare("INSERT INTO test VALUES(?, ?, ?, ?);");
        for (final Object object : objects) {
            Mockito.verify(mTable).bind(mStmt, object);
        }
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void batchInsertRollback() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object());
        Mockito.doThrow(RxSQLiteException.class).when(mStmt).executeInsert();
        try {
            mTable.insert(mDb, objects);
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(RxSQLiteException.class));
        }
        Mockito.verify(mDb).prepare("INSERT INTO test VALUES(?, ?, ?, ?);");
        Mockito.verify(mTable).bind(mStmt, objects.get(0));
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).rollback();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void hasRelationsInsert() throws Exception {
        final Object object = new Object();
        Mockito.doReturn(true).when(mTable).hasRelations();
        Mockito.doAnswer(MockAnswers.sequence(1)).when(mStmt).executeInsert();
        Assert.assertThat(mTable.insert(mDb, Collections.singletonList(object)), IsEqual.equalTo(new long[]{1}));
        Mockito.verify(mDb).prepare("INSERT INTO test VALUES(?, ?, ?, ?);");
        Mockito.verify(mTable).bind(mStmt, object);
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void selectByIds() throws Exception {
        final long[] rowIds = {1, 2, 3};
        final List<Object> expected = new ArrayList<>();
        Mockito.doAnswer(MockAnswers.steps(rowIds.length)).when(mCursor).step();
        Mockito.doAnswer(MockAnswers.instantiate(expected)).when(mTable).instantiate(mDb, mCursor);
        Assert.assertThat(mTable.select(mDb, rowIds), IsEqual.<Object>equalTo(expected));
        Mockito.verify(mDb).prepare("SELECT * FROM test WHERE _id IN(1, 2, 3);");
        Mockito.verify(mStmt).close();
    }

    @Test
    public void selectWhere() throws Exception {
        final Where where = new Where().equalTo("c_string", "foo");
        final List<Object> expected = new ArrayList<>();
        Mockito.doAnswer(MockAnswers.steps(5)).when(mCursor).step();
        Mockito.doAnswer(MockAnswers.instantiate(expected)).when(mTable).instantiate(mDb, mCursor);
        Assert.assertThat(mTable.select(mDb, where.buildSelectSql(), where.getArgs()),
                IsEqual.<Object>equalTo(expected));
        Mockito.verify(mDb).prepare("SELECT * FROM test" + where.buildSelectSql());
        int index = 1;
        for (final Object arg : where.getArgs()) {
            Mockito.verify(mTable).bindValue(mStmt, index, arg);
            ++index;
        }
        Mockito.verify(mStmt).close();
    }

    @Test
    public void rawSelect() throws Exception {
        final List<Object> expected = new ArrayList<>();
        Mockito.doAnswer(MockAnswers.steps(5)).when(mCursor).step();
        Mockito.doAnswer(MockAnswers.instantiate(expected)).when(mTable).instantiate(mDb, mCursor);
        final String sql = "SELECT t.* FROM test AS t WHERE t.c_double = ?;";
        final List<Object> args = Collections.<Object>singletonList(1.23);
        Assert.assertThat(mTable.rawSelect(mDb, sql, args), IsEqual.<Object>equalTo(expected));
        Mockito.verify(mDb).prepare(sql);
        for (int i = 0; i < args.size(); ++i) {
            Mockito.verify(mTable).bindValue(mStmt, i + 1, args.get(i));
        }
        Mockito.verify(mStmt).close();
    }

    @Test
    public void deleteByIds() throws Exception {
        final long[] rowIds = {3, 5, 7};
        Mockito.doReturn(rowIds.length).when(mStmt).executeUpdateDelete();
        Assert.assertThat(mTable.delete(mDb, rowIds), IsEqual.equalTo(rowIds.length));
        Mockito.verify(mDb).prepare("DELETE FROM test WHERE _id IN(3, 5, 7);");
        Mockito.verify(mDb, Mockito.never()).begin();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void deleteWhere() throws Exception {
        final int affectedRows = 21;
        final Where where = new Where().equalTo("c_string", "foo");
        Mockito.doReturn(affectedRows).when(mStmt).executeUpdateDelete();
        Assert.assertThat(mTable.delete(mDb, where.buildDeleteSql(), where.getArgs()), Is.is(affectedRows));
        Mockito.verify(mDb).prepare("DELETE FROM test" + where.buildDeleteSql());
        int index = 1;
        for (final Object arg : where.getArgs()) {
            Mockito.verify(mTable).bindValue(mStmt, index, arg);
            ++index;
        }
        Mockito.verify(mDb, Mockito.never()).begin();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void hasRelationsDelete() throws Exception {
        final long[] rowIds = {3, 5, 7};
        Mockito.doReturn(true).when(mTable).hasRelations();
        Mockito.doReturn(rowIds.length).when(mStmt).executeUpdateDelete();
        Assert.assertThat(mTable.delete(mDb, rowIds), IsEqual.equalTo(rowIds.length));
        Mockito.verify(mDb).prepare("DELETE FROM test WHERE _id IN(3, 5, 7);");
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).commit();
        Mockito.verify(mDb, Mockito.never()).rollback();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void deleteRollback() throws Exception {
        Mockito.doReturn(true).when(mTable).hasRelations();
        Mockito.doThrow(RxSQLiteException.class).when(mStmt).executeUpdateDelete();
        try {
            mTable.delete(mDb, new long[]{1, 2, 3});
        } catch (Exception e) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(RxSQLiteException.class));
        }
        Mockito.verify(mDb).begin();
        Mockito.verify(mDb).rollback();
        Mockito.verify(mDb, Mockito.never()).commit();
        Mockito.verify(mStmt).close();
    }

}