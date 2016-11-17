package rxsqlite;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daniel Serdyukov
 */
class ConcurrentPool implements Pool {

    private final BlockingQueue<RxSQLiteDbImpl> mWritableQueue = new ArrayBlockingQueue<>(1);

    private final BlockingQueue<RxSQLiteDbImpl> mReadableQueue = new LinkedBlockingQueue<>();

    private final AtomicBoolean mPrimaryConnectionOpened = new AtomicBoolean();

    private final Connection mConnection;

    ConcurrentPool(Connection connection) {
        mConnection = connection;
    }

    @Override
    public RxSQLiteDbImpl acquireDatabase(boolean writable) {
        if (mPrimaryConnectionOpened.compareAndSet(false, true)) {
            mWritableQueue.offer(mConnection.openDatabase(true));
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
        }
        return db;
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

}
