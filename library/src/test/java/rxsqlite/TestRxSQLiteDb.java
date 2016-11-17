package rxsqlite;

import java.util.concurrent.atomic.AtomicBoolean;

import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
public class TestRxSQLiteDb extends RxSQLiteDbImpl {

    private final AtomicBoolean mInTransaction = new AtomicBoolean();

    TestRxSQLiteDb(SQLiteDb rawDb) {
        super(rawDb);
    }

    @Override
    public void begin() {
        mInTransaction.set(true);
    }

    @Override
    public void commit() {
        mInTransaction.set(false);
    }

    @Override
    public void rollback() {
        mInTransaction.set(false);
    }

    @Override
    public boolean inTransaction() {
        return mInTransaction.get();
    }

}
