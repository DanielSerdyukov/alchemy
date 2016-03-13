package rxsqlite;

import org.hamcrest.number.IsCloseTo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import rx.functions.Func1;
import sqlite4a.SQLiteContext;
import sqlite4a.SQLiteException;
import sqlite4a.SQLiteValue;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomFuncTest {

    private final SQLiteValue[] mValues = new SQLiteValue[0];

    @Mock
    private SQLiteContext mContext;

    @Mock
    private Func1<SQLiteValue[], Object> mFunc;

    private CustomFunc mCustomFunc;

    @Before
    public void setUp() throws Exception {
        mCustomFunc = new CustomFunc(mFunc);
    }

    @Test
    public void testNullValue() throws Exception {
        mCustomFunc.call(mContext, mValues);
        Mockito.verifyZeroInteractions(mContext);
    }

    @Test
    public void testIntValue() throws Exception {
        Mockito.doReturn(100).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultLong(100);
    }

    @Test
    public void testLongValue() throws Exception {
        Mockito.doReturn(100500L).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultLong(100500L);
    }

    @Test
    public void testDoubleValue() throws Exception {
        Mockito.doReturn(1.23).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        Mockito.verify(mContext).resultDouble(captor.capture());
        Assert.assertThat(captor.getValue(), IsCloseTo.closeTo(1.23, 0.0));
    }

    @Test
    public void testFloatValue() throws Exception {
        Mockito.doReturn(4.56f).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        Mockito.verify(mContext).resultDouble(captor.capture());
        Assert.assertThat(captor.getValue(), IsCloseTo.closeTo(4.56, 1e-5));
    }

    @Test
    public void testBooleanTrueValue() throws Exception {
        Mockito.doReturn(true).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultLong(1);
    }

    @Test
    public void testBooleanFalseValue() throws Exception {
        Mockito.doReturn(false).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultLong(0);
    }

    @Test
    public void testStringValue() throws Exception {
        Mockito.doReturn("test").when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultText("test");
    }

    @Test
    public void testBlobValue() throws Exception {
        Mockito.doReturn(new byte[]{1, 2, 3}).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
        Mockito.verify(mContext).resultBlob(new byte[]{1, 2, 3});
    }

    @Test(expected = SQLiteException.class)
    public void testUnsupportedReturnType() throws Exception {
        Mockito.doReturn(new Date()).when(mFunc)
                .call(Mockito.<SQLiteValue[]>any());
        mCustomFunc.call(mContext, mValues);
    }

}