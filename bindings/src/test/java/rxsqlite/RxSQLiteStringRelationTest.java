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
public class RxSQLiteStringRelationTest {

    @Mock
    private RxSQLiteDb mDb;

    private RxSQLiteRelation mRelation;

    @Before
    public void setUp() throws Exception {
        mRelation = new RxSQLiteStringRelation("foo", "mStrings", true);
    }

    @Test
    public void create() throws Exception {
        mRelation.create(mDb);
        Mockito.verify(mDb).exec("CREATE TABLE IF NOT EXISTS foo_mStrings (fk INTEGER REFERENCES foo(_id)" +
                " ON DELETE CASCADE ON UPDATE CASCADE, value TEXT);");
    }

}