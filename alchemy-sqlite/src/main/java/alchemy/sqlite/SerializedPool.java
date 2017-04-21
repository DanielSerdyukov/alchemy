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

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteDriver;
import alchemy.sqlite.platform.SQLiteSchema;

import java.util.concurrent.locks.ReentrantLock;

class SerializedPool extends DatabasePool {

    private final ReentrantLock mLock = new ReentrantLock();

    private volatile SQLiteDb mDatabase;

    SerializedPool(SQLiteDriver driver, SQLiteSchema schema, String path) {
        super(driver, schema, path);
    }

    @Override
    SQLiteDb acquireDatabase(boolean readOnly) {
        if (mDatabase == null) {
            mLock.lock();
            try {
                if (mDatabase == null) {
                    mDatabase = openDatabase(false, true);
                }
            } finally {
                mLock.unlock();
            }
        }
        return mDatabase;
    }

    @Override
    void releaseDatabase(SQLiteDb db) {

    }

}
