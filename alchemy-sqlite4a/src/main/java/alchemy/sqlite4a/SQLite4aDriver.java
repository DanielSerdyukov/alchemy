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

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteDriver;
import sqlite4a.SQLite;

class SQLite4aDriver implements SQLiteDriver {

    private final SQLite4aHook mHook;

    SQLite4aDriver(SQLite4aHook hook) {
        mHook = hook;
    }

    @Override
    public SQLiteDb open(String path, boolean readOnly, boolean fullMutex) {
        int flags = 0;
        if (readOnly) {
            flags |= SQLite.OPEN_READONLY;
        } else {
            flags |= SQLite.OPEN_READWRITE;
            flags |= SQLite.OPEN_CREATE;
        }
        if (fullMutex) {
            flags |= SQLite.OPEN_FULLMUTEX;
        } else {
            flags |= SQLite.OPEN_NOMUTEX;
        }
        final sqlite4a.SQLiteDb db = SQLite.open(path, flags);
        mHook.onDatabaseOpen(db);
        db.exec("PRAGMA foreign_keys = ON;");
        return new SQLite4aDb(db);
    }

}
