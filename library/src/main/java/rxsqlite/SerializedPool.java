package rxsqlite;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Daniel Serdyukov
 */
class SerializedPool implements Pool {

    private final ReentrantLock mLock = new ReentrantLock();

    private final Connection mConnection;

    private volatile RxSQLiteDbImpl mDb;

    SerializedPool(Connection connection) {
        mConnection = connection;
    }

    @Override
    public RxSQLiteDbImpl acquireDatabase(boolean writable) {
        mLock.lock();
        try {
            if (mDb == null) {
                mDb = mConnection.openDatabase(true);
            }
            return mDb;
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public void releaseDatabase(RxSQLiteDbImpl db) {

    }

    @Override
    public void removeDatabase() {
        if (mDb != null) {
            mDb.close();
        }
        mConnection.removeDatabase();
    }

    @Override
    public void lock() {
        mLock.lock();
    }

    @Override
    public void unlock() {
        mLock.unlock();
    }

}
