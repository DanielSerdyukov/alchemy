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

import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteStmt;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

class AndroidQuery implements SQLiteStmt {

    private final SQLiteQuery mQuery;

    private final SQLiteDatabase mDb;

    private final String mSql;

    AndroidQuery(SQLiteDatabase db, String sql) {
        mQuery = SQLiteCompat.newQuery(db, sql);
        mDb = db;
        mSql = sql;
    }

    @Override
    public void bindNull(int index) {
        mQuery.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mQuery.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mQuery.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        mQuery.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mQuery.bindBlob(index, value);
    }

    @Override
    public void clearBindings() {
        mQuery.clearBindings();
    }

    @Override
    public SQLiteIterator select() {
        mDb.acquireReference();
        try {
            final SQLiteCursorDriver driver = SQLiteCompat.newDriver(mDb, mSql);
            return new CursorIterator(new SQLiteCursor(driver, null, mQuery));
        } finally {
            mDb.releaseReference();
        }
    }

    @Override
    public long insert() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        mQuery.close();
    }

}
