package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class RxSQLiteObjectRelationTest {

    @Mock
    private RxSQLiteDb mDb;

    @Mock
    private RxSQLiteTable<?> mRTable;

    private RxSQLiteRelation mRelation;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn("bar").when(mRTable).getName();
        mRelation = new RxSQLiteObjectRelation<>("foo", "mBar", mRTable, true);
    }

    @Test
    public void create() throws Exception {
        mRelation.create(mDb);
        Mockito.verify(mDb).exec("CREATE TABLE IF NOT EXISTS foo_mBar (" +
                "lfk INTEGER REFERENCES foo(_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "rfk INTEGER, UNIQUE(lfk, rfk) ON CONFLICT IGNORE);");
        Mockito.verify(mDb).exec("CREATE TRIGGER IF NOT EXISTS delete_foo_mBar " +
                "BEFORE DELETE ON foo_mBar " +
                "FOR EACH ROW BEGIN " +
                "DELETE FROM bar WHERE bar._id = OLD.rfk; " +
                "END;");
    }

}