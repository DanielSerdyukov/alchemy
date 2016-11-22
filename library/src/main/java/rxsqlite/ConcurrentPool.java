package rxsqlite;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Daniel Serdyukov
 */
class ConcurrentPool implements Pool {

    private final BlockingQueue<RxSQLiteDbImpl> mWritableQueue = new ArrayBlockingQueue<>(1);

    private final BlockingQueue<RxSQLiteDbImpl> mReadableQueue = new LinkedBlockingQueue<>();

    private final List<RxSQLiteDbImpl> mActiveHandles = new CopyOnWriteArrayList<>();

    private final ReentrantLock mShutdownLock = new ReentrantLock();

    private final AtomicBoolean mPrimaryConnectionOpened = new AtomicBoolean();

    private final Connection mConnection;

    ConcurrentPool(Connection connection) {
        mConnection = connection;
    }

    @Override
    public RxSQLiteDbImpl acquireDatabase(boolean writable) {
        mShutdownLock.lock();
        try {
            if (mPrimaryConnectionOpened.compareAndSet(false, true)) {
                final RxSQLiteDbImpl db = mConnection.openDatabase(true);
                mWritableQueue.offer(db);
                mActiveHandles.add(db);
            }
            if (writable) {
                try {
                    return mWritableQueue.take();
                } catch (InterruptedException e) {
                    throw new RxSQLiteException(e);
                }
            }
            RxSQLiteDbImpl db = mReadableQueue.poll();
            if (db == null) {
                db = mConnection.openDatabase(false);
                mActiveHandles.add(db);
            }
            return db;
        } finally {
            mShutdownLock.unlock();
        }
    }

    @Override
    public void releaseDatabase(RxSQLiteDbImpl db) {
        if (db.isReadOnly()) {
            if (!mReadableQueue.offer(db)) {
                db.close();
            }
        } else {
            if (!mWritableQueue.offer(db)) {
                db.close();
            }
        }
    }

    @Override
    public void removeDatabase() {
        for (final RxSQLiteDbImpl db : mActiveHandles) {
            db.close();
        }
        mReadableQueue.clear();
        mWritableQueue.clear();
        mActiveHandles.clear();
        mConnection.removeDatabase();
        mPrimaryConnectionOpened.set(false);
    }

    @Override
    public void lock() {
        mShutdownLock.lock();
    }

    @Override
    public void unlock() {
        mShutdownLock.unlock();
    }

}
