/*
 * Copyright (C) 2017 exzogeni.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alchemy.sqlite;

import alchemy.AlchemyException;
import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteDriver;
import alchemy.sqlite.platform.SQLiteSchema;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

class ConcurrentPool extends DatabasePool {

    private final ReentrantLock mWriteLock = new ReentrantLock();

    private final BlockingQueue<SQLiteDb> mReadableQueue;

    private final AtomicInteger mRemainingCapacity;

    private volatile SQLiteDb mWritableDb;

    ConcurrentPool(SQLiteDriver driver, SQLiteSchema schema, String path) {
        super(driver, schema, path);
        mReadableQueue = new ArrayBlockingQueue<>(calculatePoolSize());
        mRemainingCapacity = new AtomicInteger(mReadableQueue.remainingCapacity());
    }

    private static int calculatePoolSize() {
        return Runtime.getRuntime().availableProcessors() * 2 + 1;
    }

    @Override
    SQLiteDb acquireDatabase(boolean readOnly) {
        ensureWritableDatabaseOpen();
        if (readOnly) {
            SQLiteDb db = mReadableQueue.poll();
            if (db == null) {
                if (mRemainingCapacity.decrementAndGet() >= 0) {
                    db = openDatabase(true, false);
                } else {
                    try {
                        db = mReadableQueue.take();
                    } catch (InterruptedException e) {
                        throw new AlchemyException(e);
                    }
                }
            }
            return db;
        } else {
            mWriteLock.lock();
            return mWritableDb;
        }
    }

    @Override
    void releaseDatabase(SQLiteDb db) {
        if (db.isReadOnly()) {
            if (!mReadableQueue.offer(db)) {
                db.close();
            }
        } else {
            mWriteLock.unlock();
        }
    }

    private void ensureWritableDatabaseOpen() {
        mWriteLock.lock();
        try {
            if (mWritableDb == null) {
                mWritableDb = openDatabase(false, false);
            }
        } finally {
            mWriteLock.unlock();
        }
    }

}
