package rxsqlite;

import java.util.concurrent.locks.ReentrantLock;

import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class InMemoryPool implements SQLitePool {

    private final ReentrantLock mLock = new ReentrantLock();

    private final SQLiteHelper mHelper;

    private RxSQLiteDb mPrimaryDb;

    InMemoryPool(SQLiteHelper helper) {
        mHelper = helper;
    }

    @Override
    public RxSQLiteDb acquireDatabase(boolean writable) {
        if (mPrimaryDb == null) {
            mLock.lock();
            try {
                if (mPrimaryDb == null) {
                    mPrimaryDb = mHelper.openDatabase(true);
                }
            } finally {
                mLock.unlock();
            }
        }
        return mPrimaryDb;
    }

    @Override
    public void releaseDatabase(RxSQLiteDb db) {

    }

}
