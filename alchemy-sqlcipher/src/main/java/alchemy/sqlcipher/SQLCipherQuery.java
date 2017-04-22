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

package alchemy.sqlcipher;

import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteStmt;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.Set;
import java.util.TreeSet;

class SQLCipherQuery implements SQLiteStmt {

    private final Set<BindArg> mBindArgs = new TreeSet<>();

    private final SQLiteDatabase mDb;

    private final String mSql;

    SQLCipherQuery(SQLiteDatabase db, String sql) {
        mDb = db;
        mSql = sql;
    }

    @Override
    public void bindNull(int index) {
        mBindArgs.add(new BindArg(index, null));
    }

    @Override
    public void bindLong(int index, long value) {
        mBindArgs.add(new BindArg(index, value));
    }

    @Override
    public void bindDouble(int index, double value) {
        mBindArgs.add(new BindArg(index, value));
    }

    @Override
    public void bindString(int index, String value) {
        mBindArgs.add(new BindArg(index, value));
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mBindArgs.add(new BindArg(index, value));
    }

    @Override
    public void clearBindings() {
        mBindArgs.clear();
    }

    @Override
    public SQLiteIterator select() {
        final Object[] args = new Object[mBindArgs.size()];
        for (final BindArg arg : mBindArgs) {
            args[arg.mIndex - 1] = arg.mValue;
        }
        return new CursorIterator(mDb.rawQuery(mSql, args));
    }

    @Override
    public long insert() {
        throw new UnsupportedOperationException("sql = " + mSql);
    }

    @Override
    public int execute() {
        throw new UnsupportedOperationException("sql = " + mSql);
    }

    @Override
    public void close() {
        mBindArgs.clear();
    }

    private static class BindArg implements Comparable<BindArg> {

        final int mIndex;

        final Object mValue;

        BindArg(int index, Object value) {
            mIndex = index;
            mValue = value;
        }

        @Override
        public int compareTo(BindArg o) {
            return (mIndex < o.mIndex) ? -1 : ((mIndex == o.mIndex) ? 0 : 1);
        }

    }

}
