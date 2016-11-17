package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class RxSQLiteTableTest {

    @Mock
    private RxSQLiteDb mDb;

    @Mock
    private RxSQLiteStmt mStmt;

    @Mock
    private RxSQLiteCursor mCursor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private RxSQLiteTable<Object> mTable;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn("test").when(mTable).getName();
        Mockito.doReturn(new String[]{"foo", "bar", "baz"}).when(mTable).getColumns();
        Mockito.doReturn(mStmt).when(mDb).prepare(Mockito.anyString());
        Mockito.doReturn(mCursor).when(mStmt).executeSelect();
    }

    @Test
    public void insert() throws Exception {
        mTable.insert(mDb, Arrays.asList(new Object(), new Object()));
        Mockito.verify(mDb).prepare("INSERT INTO test VALUES(?, ?, ?);");
        Mockito.verify(mStmt, Mockito.times(2)).executeInsert();
        Mockito.verify(mStmt, Mockito.times(2)).clearBindings();
        Mockito.verify(mStmt).close();
    }

    @Test
    public void selectByIds() throws Exception {
        mTable.select(mDb, new long[]{1, 2, 3});
        Mockito.verify(mDb).prepare("SELECT * FROM test WHERE _id IN(1, 2, 3);");
        Mockito.verify(mStmt).close();
    }

    @Test
    public void deleteList() throws Exception {
        final AtomicLong sequence = new AtomicLong();
        Mockito.doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return sequence.incrementAndGet();
            }
        }).when(mTable).getObjectId(Mockito.any());
        mTable.delete(mDb, Arrays.asList(new Object(), new Object()));
        Mockito.verify(mDb).prepare("DELETE FROM test WHERE _id IN(1, 2);");
        Mockito.verify(mStmt).close();
    }

}