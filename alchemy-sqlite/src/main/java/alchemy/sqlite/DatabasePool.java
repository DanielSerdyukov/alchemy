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
import alchemy.sqlite.platform.SQLiteTable;

import java.util.Collection;

abstract class DatabasePool {

    private final SQLiteDriver mDriver;

    private final SQLiteSchema mSchema;

    private final String mPath;

    DatabasePool(SQLiteDriver driver, SQLiteSchema schema, String path) {
        mDriver = driver;
        mSchema = schema;
        mPath = path;
    }

    abstract SQLiteDb acquireDatabase(boolean readOnly);

    abstract void releaseDatabase(SQLiteDb db);

    SQLiteDb openDatabase(boolean readOnly, boolean fullMutex) {
        final SQLiteDb db = mDriver.open(mPath, readOnly, fullMutex);
        if (!readOnly) {
            final int userVersion = db.getUserVersion();
            final int schemaVersion = mSchema.getVersion();
            if (userVersion == 0) {
                createSchema(db, mSchema.getAllTables());
                db.setUserVersion(schemaVersion);
            } else if (userVersion != schemaVersion) {
                applyMigrations(db, userVersion, schemaVersion);
                db.setUserVersion(schemaVersion);
            }
        }
        return db;
    }

    private void createSchema(SQLiteDb db, Collection<SQLiteTable<?>> tables) {
        for (SQLiteTable<?> table : tables) {
            table.create(db);
        }
    }

    private void applyMigrations(SQLiteDb db, int oldVersion, int newVersion) {
        // TODO: 12.04.17 schema migration
    }

}
