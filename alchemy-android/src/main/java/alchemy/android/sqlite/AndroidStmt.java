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
import android.database.sqlite.SQLiteStatement;

class AndroidStmt implements SQLiteStmt {

    private final SQLiteStatement mStmt;

    AndroidStmt(SQLiteStatement stmt) {
        mStmt = stmt;
    }

    @Override
    public void bindNull(int index) {
        mStmt.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mStmt.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mStmt.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        mStmt.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mStmt.bindBlob(index, value);
    }

    @Override
    public void clearBindings() {
        mStmt.clearBindings();
    }

    @Override
    public SQLiteIterator select() {
        return null;
    }

    @Override
    public long insert() {
        return mStmt.executeInsert();
    }

    @Override
    public int execute() {
        return mStmt.executeUpdateDelete();
    }

    @Override
    public void close() {
        mStmt.close();
    }

}
