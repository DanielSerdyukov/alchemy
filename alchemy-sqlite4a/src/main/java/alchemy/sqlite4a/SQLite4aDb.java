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
import alchemy.sqlite.platform.SQLiteStmt;

class SQLite4aDb implements SQLiteDb {

    private final sqlite4a.SQLiteDb mDb;

    SQLite4aDb(sqlite4a.SQLiteDb db) {
        mDb = db;
    }

    @Override
    public int getUserVersion() {
        return mDb.execForNumber("PRAGMA user_version;").intValue();
    }

    @Override
    public void setUserVersion(int version) {
        mDb.exec("PRAGMA user_version = " + version + ";");
    }

    @Override
    public boolean isReadOnly() {
        return mDb.isReadOnly();
    }

    @Override
    public void exec(String sql) {
        mDb.exec(sql);
    }

    @Override
    public void begin() {
        mDb.exec("BEGIN;");
    }

    @Override
    public void commit() {
        mDb.exec("COMMIT;");
    }

    @Override
    public void rollback() {
        mDb.exec("ROLLBACK;");
    }

    @Override
    public SQLiteStmt prepare(String sql) {
        return new SQLite4aStmt(mDb.prepare(sql));
    }

    @Override
    public void close() {
        mDb.close();
    }

}
