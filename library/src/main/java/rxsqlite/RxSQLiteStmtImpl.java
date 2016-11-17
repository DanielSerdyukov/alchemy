package rxsqlite;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
class RxSQLiteStmtImpl implements RxSQLiteStmt {

    private final SQLiteStmt mStmt;

    RxSQLiteStmtImpl(SQLiteStmt stmt) {
        mStmt = stmt;
    }

    @Override
    public void bindNull(int index) {
        mStmt.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mStmt.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mStmt.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, @Nullable String value) {
        mStmt.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, @Nullable byte[] value) {
        mStmt.bindBlob(index, value);
    }

    @Override
    public void clearBindings() {
        mStmt.clearBindings();
    }

    @Override
    public long executeInsert() {
        return mStmt.executeInsert();
    }

    @Override
    @NonNull
    public RxSQLiteCursor executeSelect() {
        return new RxSQLiteCursorImpl(mStmt.executeSelect());
    }

    @Override
    public int executeUpdateDelete() {
        return mStmt.executeUpdateDelete();
    }

    @Override
    public void close() {
        mStmt.close();
    }

}
