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

import alchemy.AlchemyException;
import alchemy.sqlite.platform.SQLiteStmt;

class Binder {

    static void bind(SQLiteStmt stmt, int index, Object value) {
        if (value == null) {
            stmt.bindNull(index);
        } else if (value instanceof Integer
                || value instanceof Long
                || value instanceof Short) {
            stmt.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Number) {
            stmt.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof String) {
            stmt.bindString(index, (String) value);
        } else if (value instanceof byte[]) {
            stmt.bindBlob(index, (byte[]) value);
        } else {
            throw new AlchemyException("Unsupported SQLite type");
        }
    }

}
