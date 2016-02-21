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

import java.util.Date;

import sqlite4a.SQLiteRow;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class RxSQLiteBinderTest {

    @Mock
    private SQLiteStmt mStmt;

    @Mock
    private SQLiteRow mRow;

    private RxSQLiteBinder mBinder;

    @Before
    public void setUp() throws Exception {
        mBinder = Mockito.spy(new RxSQLiteBinder());
    }

    @Test
    public void testGetType() throws Exception {
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        Mockito.doReturn("INTEGER").when(type).getType(Date.class);
        for (int i = 0; i < 5; ++i) {
            mBinder.registerType(Mockito.mock(RxSQLiteType.class));
        }
        mBinder.registerType(type);
        Assert.assertThat(mBinder.getType(Date.class), Is.is("INTEGER"));
    }

    @Test
    public void testGetValue() throws Exception {
        final Date expected = new Date();
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        Mockito.doReturn(expected).when(type).getValue(mRow, 1);
        for (int i = 0; i < 5; ++i) {
            mBinder.registerType(Mockito.mock(RxSQLiteType.class));
        }
        mBinder.registerType(type);
        Assert.assertThat(mBinder.getValue(mRow, 1, Date.class), Is.is(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnsupportedValue() throws Exception {
        mBinder.getValue(mRow, 1, Bar.class);
    }

    @Test
    public void testGetEnumValue() throws Exception {
        Mockito.doReturn("VALUE").when(mRow).getColumnString(1);
        Assert.assertThat(mBinder.getEnumValue(mRow, 1, Bar.class), Is.is(Bar.VALUE));

        Mockito.doReturn(null).when(mRow).getColumnString(1);
        Assert.assertThat(mBinder.getEnumValue(mRow, 1, Bar.class), IsNull.nullValue());
    }

    @Test
    public void testBindNull() throws Exception {
        mBinder.bindValue(mStmt, 1, null);
        Mockito.verify(mStmt).bindNull(1);
    }

    @Test
    public void testBindDouble() throws Exception {
        mBinder.bindValue(mStmt, 2, 1.23);
        Mockito.verify(mStmt).bindDouble(2, 1.23);
        mBinder.bindValue(mStmt, 2, 4.56f);
        Mockito.verify(mStmt).bindDouble(2, 4.56f);
    }

    @Test
    public void testBindLong() throws Exception {
        mBinder.bindValue(mStmt, 3, 100);
        Mockito.verify(mStmt).bindLong(3, 100);
        mBinder.bindValue(mStmt, 3, 200L);
        Mockito.verify(mStmt).bindLong(3, 200L);
    }

    @Test
    public void testBindBoolean() throws Exception {
        mBinder.bindValue(mStmt, 4, true);
        Mockito.verify(mStmt).bindLong(4, 1);
        mBinder.bindValue(mStmt, 4, false);
        Mockito.verify(mStmt).bindLong(4, 0);
    }

    @Test
    public void testBindString() throws Exception {
        mBinder.bindValue(mStmt, 5, "test");
        Mockito.verify(mStmt).bindString(5, "test");
    }

    @Test
    public void testBindBlob() throws Exception {
        mBinder.bindValue(mStmt, 5, new byte[]{1, 2, 3});
    }

    @Test
    public void testBindCustom() throws Exception {
        final Date expected = new Date();
        final RxSQLiteType type = Mockito.mock(RxSQLiteType.class);
        Mockito.doReturn(true).when(type).isAssignable(Date.class);
        for (int i = 0; i < 5; ++i) {
            mBinder.registerType(Mockito.mock(RxSQLiteType.class));
        }
        mBinder.registerType(type);
        mBinder.bindValue(mStmt, 6, expected);
        Mockito.verify(type).bindValue(mStmt, 6, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedBindValue() throws Exception {
        mBinder.bindValue(mStmt, 7, Bar.VALUE);
    }

    enum Bar {
        VALUE
    }

}
