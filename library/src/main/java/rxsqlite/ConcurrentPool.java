package rxsqlite;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;

/**
 * @author Daniel Serdyukov
 */
class ConcurrentPool implements SQLitePool {

    private final SQLiteHelper mHelper;

    private final BlockingQueue<RxSQLiteDb> mWritableQueue = new ArrayBlockingQueue<>(1);

    private final BlockingQueue<RxSQLiteDb> mReadableQueue = new LinkedBlockingQueue<>();

    private final AtomicBoolean mPrimaryConnectionOpened = new AtomicBoolean();

    ConcurrentPool(SQLiteHelper helper) {
        mHelper = helper;
    }

    @Override
    public RxSQLiteDb acquireDatabase(boolean writable) {
        if (mPrimaryConnectionOpened.compareAndSet(false, true)) {
            final RxSQLiteDb db = mHelper.openDatabase(true);
            if (!mWritableQueue.offer(db)) {
                db.close();
            }
        }
        if (writable) {
            return acquireWritableDatabase();
        } else {
            return acquireReadableDatabase();
        }
    }

    @Override
    public void releaseDatabase(RxSQLiteDb db) {
        if (db.isReadOnly()) {
            mReadableQueue.offer(db);
        } else if (!mWritableQueue.offer(db)) {
            db.close();
        }
    }

    private RxSQLiteDb acquireWritableDatabase() {
        try {
            return mWritableQueue.take();
        } catch (InterruptedException e) {
            throw new RxSQLiteException(e);
        }
    }

    private RxSQLiteDb acquireReadableDatabase() {
        RxSQLiteDb db = mReadableQueue.poll();
        if (db == null) {
            db = mHelper.openDatabase(false);
        }
        return db;
    }

}
