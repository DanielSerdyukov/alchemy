package rxsqlite;

import android.support.annotation.NonNull;

import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class RxSQLiteDbImpl implements RxSQLiteDb {

    final SQLiteDb mNativeDb;

    RxSQLiteDbImpl(SQLiteDb rawDb) {
        mNativeDb = rawDb;
    }

    @Override
    public boolean isReadOnly() {
        return mNativeDb.isReadOnly();
    }

    @Override
    public void begin() {
        mNativeDb.begin();
    }

    @Override
    public void commit() {
        mNativeDb.commit();
    }

    @Override
    public void rollback() {
        mNativeDb.rollback();
    }

    @Override
    public boolean inTransaction() {
        return mNativeDb.inTransaction();
    }

    @Override
    public void exec(@NonNull String sql) {
        mNativeDb.exec(sql);
    }

    @Override
    @NonNull
    public RxSQLiteStmt prepare(@NonNull String sql) {
        return new RxSQLiteStmtImpl(mNativeDb.prepare(sql));
    }

    @Override
    public void close() {
        mNativeDb.close();
    }

}
