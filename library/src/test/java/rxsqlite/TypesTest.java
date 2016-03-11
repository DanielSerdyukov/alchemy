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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class TypesTest {

    @Mock
    private SQLiteStmt mStmt;

    @Mock
    private SQLiteCursor mCursor;

    private Types mTypes;

    @Before
    public void setUp() throws Exception {
        mTypes = new Types(Collections.<RxSQLiteType>emptyList());
    }

    @Test
    public void testGetType() throws Exception {
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        Mockito.doReturn("INTEGER").when(type).getType(Date.class);
        final Types types = new Types(Arrays.asList(
                Mockito.mock(RxSQLiteType.class),
                Mockito.mock(RxSQLiteType.class),
                type
        ));
        Assert.assertThat(types.getType(Date.class), Is.is("INTEGER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnsupportedType() throws Exception {
        new Types(Collections.<RxSQLiteType>emptyList()).getType(Date.class);
    }

    @Test
    public void testGetValue() throws Exception {
        final Date expected = new Date();
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        Mockito.doReturn(expected).when(type).getValue(mCursor, 1);
        final Types types = new Types(Arrays.asList(
                Mockito.mock(RxSQLiteType.class),
                Mockito.mock(RxSQLiteType.class),
                type
        ));
        Assert.assertThat(types.getValue(mCursor, 1, Date.class), Is.is(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnsupportedValue() throws Exception {
        new Types(Collections.<RxSQLiteType>emptyList()).getValue(mCursor, 1, Bar.class);
    }

    @Test
    public void testGetEnumValue() throws Exception {
        Mockito.doReturn("VALUE").when(mCursor).getColumnString(1);
        Assert.assertThat(mTypes.getEnumValue(mCursor, 1, Bar.class), Is.is(Bar.VALUE));

        Mockito.doReturn(null).when(mCursor).getColumnString(1);
        Assert.assertThat(mTypes.getEnumValue(mCursor, 1, Bar.class), IsNull.nullValue());
    }

    @Test
    public void testBindNull() throws Exception {
        mTypes.bindValue(mStmt, 1, null);
        Mockito.verify(mStmt).bindNull(1);
    }

    @Test
    public void testBindDouble() throws Exception {
        mTypes.bindValue(mStmt, 2, 1.23);
        Mockito.verify(mStmt).bindDouble(2, 1.23);
        mTypes.bindValue(mStmt, 2, 4.56f);
        Mockito.verify(mStmt).bindDouble(2, 4.56f);
    }

    @Test
    public void testBindLong() throws Exception {
        mTypes.bindValue(mStmt, 3, 100);
        Mockito.verify(mStmt).bindLong(3, 100);
        mTypes.bindValue(mStmt, 3, 200L);
        Mockito.verify(mStmt).bindLong(3, 200L);
    }

    @Test
    public void testBindBoolean() throws Exception {
        mTypes.bindValue(mStmt, 4, true);
        Mockito.verify(mStmt).bindLong(4, 1);
        mTypes.bindValue(mStmt, 4, false);
        Mockito.verify(mStmt).bindLong(4, 0);
    }

    @Test
    public void testBindString() throws Exception {
        mTypes.bindValue(mStmt, 5, "test");
        Mockito.verify(mStmt).bindString(5, "test");
    }

    @Test
    public void testBindBlob() throws Exception {
        mTypes.bindValue(mStmt, 5, new byte[]{1, 2, 3});
    }

    @Test
    public void testBindCustom() throws Exception {
        final Date expected = new Date();
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        Mockito.doReturn(expected).when(type).getValue(mCursor, 1);
        final Types types = new Types(Arrays.asList(
                Mockito.mock(RxSQLiteType.class),
                Mockito.mock(RxSQLiteType.class),
                type
        ));
        types.bindValue(mStmt, 6, expected);
        Mockito.verify(type).bindValue(mStmt, 6, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedBindValue() throws Exception {
        mTypes.bindValue(mStmt, 7, Bar.VALUE);
    }

    enum Bar {
        VALUE
    }

}
