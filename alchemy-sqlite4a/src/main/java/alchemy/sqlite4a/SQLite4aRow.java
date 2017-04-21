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

package alchemy.sqlite4a;

import alchemy.sqlite.platform.SQLiteRow;

class SQLite4aRow implements SQLiteRow {

    private final sqlite4a.SQLiteRow mRow;

    SQLite4aRow(sqlite4a.SQLiteRow row) {
        mRow = row;
    }

    @Override
    public long getColumnLong(int index) {
        return mRow.getColumnLong(index);
    }

    @Override
    public double getColumnDouble(int index) {
        return mRow.getColumnDouble(index);
    }

    @Override
    public String getColumnString(int index) {
        return mRow.getColumnString(index);
    }

    @Override
    public byte[] getColumnBlob(int index) {
        return mRow.getColumnBlob(index);
    }

}
