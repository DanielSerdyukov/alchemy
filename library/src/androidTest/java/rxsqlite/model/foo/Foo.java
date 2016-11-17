package rxsqlite.model.foo;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("foo")
public class Foo {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private String mString;

    @SQLiteColumn
    private double mDouble;

    @SQLiteColumn
    private byte[] mBytes;

    public long getId() {
        return mId;
    }

    public void setString(String value) {
        mString = value;
    }

    public void setDouble(double value) {
        mDouble = value;
    }

    public void setBytes(byte[] value) {
        mBytes = value;
    }

}
