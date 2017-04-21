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
import alchemy.sqlite.platform.SQLiteRow;
import android.database.Cursor;

import java.util.NoSuchElementException;

class CursorIterator implements SQLiteIterator, SQLiteRow {

    private final Cursor mCursor;

    private boolean mHasNext;

    CursorIterator(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public boolean hasNext() {
        if (!mHasNext) {
            if (mCursor.isBeforeFirst()) {
                mHasNext = mCursor.moveToFirst();
            } else {
                mHasNext = mCursor.moveToNext();
            }
        }
        return mHasNext;
    }

    @Override
    public SQLiteRow next() {
        if (mHasNext) {
            mHasNext = false;
            return this;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void close() {
        mCursor.close();
    }

    @Override
    public long getColumnLong(int index) {
        return mCursor.getLong(index);
    }

    @Override
    public double getColumnDouble(int index) {
        return mCursor.getDouble(index);
    }

    @Override
    public String getColumnString(int index) {
        return mCursor.getString(index);
    }

    @Override
    public byte[] getColumnBlob(int index) {
        return mCursor.getBlob(index);
    }

}
