package rxsqlite;

import android.support.annotation.NonNull;

import rx.functions.Func1;
import sqlite4a.SQLiteContext;
import sqlite4a.SQLiteException;
import sqlite4a.SQLiteFunc;
import sqlite4a.SQLiteValue;

/**
 * @author Daniel Serdyukov
 */
class CustomFunc implements SQLiteFunc {

    private final Func1<SQLiteValue[], Object> mFunc;

    CustomFunc(@NonNull Func1<SQLiteValue[], Object> func) {
        mFunc = func;
    }

    @Override
    public void call(@NonNull SQLiteContext context, @NonNull SQLiteValue[] values) {
        final Object result = mFunc.call(values);
        if (result != null) {
            if (result instanceof Integer || result instanceof Long) {
                context.resultLong(((Number) result).longValue());
            } else if (result instanceof Double || result instanceof Float) {
                context.resultDouble(((Number) result).doubleValue());
            } else if (result instanceof CharSequence) {
                context.resultText((String) result);
            } else if (result instanceof Boolean) {
                if ((boolean) result) {
                    context.resultLong(1);
                } else {
                    context.resultLong(0);
                }
            } else if (result instanceof byte[]) {
                context.resultBlob((byte[]) result);
            } else {
                throw new SQLiteException("Unsupported sqlite function return type " + result.getClass()
                        + ". Supported only int, long, double, float, boolean, byte[], String");
            }
        }
    }

}
