package rxsqlite;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class TypesTest {

    @Mock
    private RxSQLiteStmt mStmt;

    @Mock
    private RxSQLiteCursor mCursor;

    @Before
    public void setUp() throws Exception {
        Types.clear();
    }

    @Test
    public void bindNull() throws Exception {
        Types.bindValue(mStmt, 1, null);
        Mockito.verify(mStmt).bindNull(1);
    }

    @Test
    public void bindInt() throws Exception {
        Types.bindValue(mStmt, 1, 100);
        Mockito.verify(mStmt).bindLong(1, 100);
    }

    @Test
    public void bindLong() throws Exception {
        Types.bindValue(mStmt, 1, 1000L);
        Mockito.verify(mStmt).bindLong(1, 1000L);
    }

    @Test
    public void bindShort() throws Exception {
        Types.bindValue(mStmt, 1, (short) 10);
        Mockito.verify(mStmt).bindLong(1, 10);
    }

    @Test
    public void bindDouble() throws Exception {
        Types.bindValue(mStmt, 1, 1.23);
        Mockito.verify(mStmt).bindDouble(1, 1.23);
    }

    @Test
    public void bindFloat() throws Exception {
        Types.bindValue(mStmt, 1, 3.45f);
        Mockito.verify(mStmt).bindDouble(1, 3.45f);
    }

    @Test
    public void bindString() throws Exception {
        Types.bindValue(mStmt, 1, "test");
        Mockito.verify(mStmt).bindString(1, "test");
    }

    @Test
    public void bindTrue() throws Exception {
        Types.bindValue(mStmt, 1, true);
        Mockito.verify(mStmt).bindLong(1, 1);
    }

    @Test
    public void bindFalse() throws Exception {
        Types.bindValue(mStmt, 1, false);
        Mockito.verify(mStmt).bindLong(1, 0);
    }

    @Test
    public void bindBlob() throws Exception {
        Types.bindValue(mStmt, 1, new byte[]{1, 2, 3});
        Mockito.verify(mStmt).bindBlob(1, new byte[]{1, 2, 3});
    }

    @Test
    public void bindEnum() throws Exception {
        Types.bindValue(mStmt, 1, Role.ADMIN);
        Mockito.verify(mStmt).bindString(1, Role.ADMIN.name());
    }

    @Test
    public void bindCustomValue() throws Exception {
        final SQLiteType type = Mockito.mock(SQLiteType.class);
        Types.create(Date.class, type);
        final Date value = new Date();
        Types.bindValue(mStmt, 1, value);
        Mockito.verify(type).bind(mStmt, 1, value);
    }

    @Test
    public void bindCustomAssignableValue() throws Exception {
        final SQLiteType type = Mockito.mock(SQLiteType.class);
        Types.create(Number.class, type);
        final BigDecimal value = new BigDecimal(1L);
        Types.bindValue(mStmt, 1, value);
        Mockito.verify(type).bind(mStmt, 1, value);
    }

    @Test
    public void getEnumValue() throws Exception {
        Mockito.doReturn("USER").when(mCursor).getColumnString(1);
        Assert.assertThat(Types.getValue(mCursor, 1, Role.class), Is.<Object>is(Role.USER));

        Mockito.doReturn(null).when(mCursor).getColumnString(2);
        Assert.assertThat(Types.getValue(mCursor, 2, Role.class), IsNull.nullValue());
    }

    @Test
    public void getCustomValue() throws Exception {
        final SQLiteType type = Mockito.mock(SQLiteType.class);
        Types.create(Date.class, type);
        Types.getValue(mCursor, 1, Date.class);
        Mockito.verify(type).get(mCursor, 1);
    }

    @Test
    public void getCustomAssignableValue() throws Exception {
        final SQLiteType type = Mockito.mock(SQLiteType.class);
        Types.create(Number.class, type);
        Types.getValue(mCursor, 1, BigInteger.class);
        Mockito.verify(type).get(mCursor, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findUnsupportedType() throws Exception {
        Types.findType(Void.class);
    }

    enum Role {USER, ADMIN}

}