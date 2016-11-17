package rxsqlite;

import sqlite4a.SQLiteCursor;

/**
 * @author Daniel Serdyukov
 */
class RxSQLiteCursorImpl implements RxSQLiteCursor {

    private final SQLiteCursor mCursor;

    RxSQLiteCursorImpl(SQLiteCursor cursor) {
        mCursor = cursor;
    }

    @Override
    public byte[] getColumnBlob(int index) {
        return mCursor.getColumnBlob(index);
    }

    @Override
    public String getColumnString(int index) {
        return mCursor.getColumnString(index);
    }

    @Override
    public double getColumnDouble(int index) {
        return mCursor.getColumnDouble(index);
    }

    @Override
    public long getColumnLong(int index) {
        return mCursor.getColumnLong(index);
    }

    @Override
    public boolean step() {
        return mCursor.step();
    }

}
