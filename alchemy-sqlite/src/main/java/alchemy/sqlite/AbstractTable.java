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

import alchemy.sqlite.platform.*;

import java.util.Collection;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
abstract class AbstractTable<T> implements SQLiteTable<T> {

    private final String mName;

    private final String[] mColumns;

    protected AbstractTable(String name, String[] columns) {
        mName = name;
        mColumns = columns;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long[] insert(SQLiteSchema schema, SQLiteDb db, Collection<T> objects) {
        final StringBuilder sql = new StringBuilder("INSERT INTO ").append(mName).append("(");
        StringUtils.append(sql, mColumns, ", ");
        sql.append(") VALUES(");
        StringUtils.appendNCopies(sql, "?", mColumns.length, ", ");
        sql.append(");");
        final SQLiteStmt stmt = db.prepare(sql.toString());
        try {
            int index = 0;
            final long[] ids = new long[objects.size()];
            final SQLiteEntry<T> entry = getEntry();
            for (T object : objects) {
                entry.bind(schema, stmt, object);
                final long id = stmt.insert();
                ids[index++] = id;
                onInsert(schema, db, object, id);
                stmt.clearBindings();
            }
            return ids;
        } finally {
            stmt.close();
        }
    }

    @Override
    public SQLiteIterator select(SQLiteDb db, String selection, Object[] args) {
        final SQLiteStmt stmt = db.prepare("SELECT * FROM " + mName + selection + ";");
        for (int i = 0; i < args.length; ++i) {
            Binder.bind(stmt, i + 1, args[i]);
        }
        return stmt.select();
    }

    @Override
    public long[] update(SQLiteSchema schema, SQLiteDb db, Collection<T> objects) {
        final StringBuilder sql = new StringBuilder("UPDATE ").append(mName).append(" SET ");
        for (int i = 0, last = mColumns.length - 1; i <= last; ++i) {
            sql.append(mColumns[i]).append(" = ?");
            if (i < last) {
                sql.append(", ");
            }
        }
        sql.append(" WHERE _id = ?;");
        final SQLiteStmt stmt = db.prepare(sql.toString());
        try {
            int index = 0;
            final long[] ids = new long[objects.size()];
            final SQLiteEntry<T> entry = getEntry();
            for (T object : objects) {
                int nextIndex = entry.bind(schema, stmt, object);
                final long id = entry.getId(object);
                System.out.println("SQLite4a: stmt.bindLong(" + nextIndex + ", " + id + ")");
                stmt.bindLong(nextIndex, id);
                stmt.execute();
                ids[index++] = id;
                stmt.clearBindings();
            }
            return ids;
        } finally {
            stmt.close();
        }
    }

    @Override
    public int delete(SQLiteDb db, String selection, Object[] args) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM " + mName + selection + ";");
        try {
            for (int i = 0; i < args.length; ++i) {
                Binder.bind(stmt, i + 1, args[i]);
            }
            return stmt.execute();
        } finally {
            stmt.close();
        }
    }

    protected void onInsert(SQLiteSchema schema, SQLiteDb db, T object, long id) {

    }

}
