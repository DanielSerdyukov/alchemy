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

package alchemy.android.sqlite;

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteStmt;
import android.database.sqlite.SQLiteDatabase;

class AndroidDb implements SQLiteDb {

    private final SQLiteDatabase mDb;

    AndroidDb(SQLiteDatabase db) {
        mDb = db;
    }

    @Override
    public int getUserVersion() {
        return mDb.getVersion();
    }

    @Override
    public void setUserVersion(int version) {
        mDb.setVersion(version);
    }

    @Override
    public boolean isReadOnly() {
        return mDb.isReadOnly();
    }

    @Override
    public void exec(String sql) {
        mDb.execSQL(sql);
    }

    @Override
    public void begin() {
        mDb.beginTransaction();
    }

    @Override
    public void commit() {
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    @Override
    public void rollback() {
        mDb.endTransaction();
    }

    @Override
    public SQLiteStmt prepare(String sql) {
        if (sql.toLowerCase().startsWith("select")) {
            return new AndroidQuery(mDb, sql);
        }
        return new AndroidStmt(mDb.compileStatement(sql));
    }

    @Override
    public void close() {
        mDb.close();
    }

}
