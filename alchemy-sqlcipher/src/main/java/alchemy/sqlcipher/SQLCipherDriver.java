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

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteDriver;
import net.sqlcipher.database.SQLiteDatabase;

class SQLCipherDriver implements SQLiteDriver {

    private final String mPassword;

    SQLCipherDriver(String password) {
        mPassword = password;
    }

    @Override
    public SQLiteDb open(String path, boolean readOnly, boolean fullMutex) {
        int flags = 0;
        if (readOnly) {
            flags |= SQLiteDatabase.OPEN_READONLY;
        } else {
            flags |= SQLiteDatabase.OPEN_READWRITE;
            flags |= SQLiteDatabase.CREATE_IF_NECESSARY;
        }
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(path, mPassword, null, flags);
        /*final SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        } else {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }*/
        return new SQLCipherDb(db);
    }

}
