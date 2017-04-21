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

class SQLiteClient {

    private final DatabasePool mPool;

    private final SQLiteSchema mSchema;

    SQLiteClient(SQLiteDriver driver, SQLiteSchema schema, String path) {
        mPool = obtainPool(driver, schema, path);
        mSchema = schema;
    }

    private static DatabasePool obtainPool(SQLiteDriver driver, SQLiteSchema schema, String path) {
        if (path == null || path.contains(":memory:")) {
            return new SerializedPool(driver, schema, path);
        } else {
            return new ConcurrentPool(driver, schema, path);
        }
    }

    SQLiteSchema getSchema() {
        return mSchema;
    }

    DatabasePool getDatabasePool() {
        return mPool;
    }

    <T> SQLiteTable<T> resolve(Class<T> clazz) {
        return mSchema.getTable(clazz);
    }

    <T> SQLiteTable<T> resolve(Collection<T> objects) {
        return resolve(Iterators.getType(objects.iterator()));
    }

    <T> long[] insert(SQLiteTable<T> table, Collection<T> objects) {
        final SQLiteDb db = mPool.acquireDatabase(false);
        try {
            db.begin();
            try {
                final long[] ids = table.insert(mSchema, db, objects);
                db.commit();
                return ids;
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> SQLiteIterator select(SQLiteTable<T> table, long[] ids) {
        final SQLiteDb db = mPool.acquireDatabase(true);
        try {
            final StringBuilder selection = new StringBuilder(" WHERE _id IN(");
            StringUtils.append(selection, ids, ", ");
            selection.append(")");
            return table.select(db, selection.toString(), new Object[0]);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> SQLiteIterator select(SQLiteTable<T> table, String selection, Object[] args) {
        final SQLiteDb db = mPool.acquireDatabase(true);
        try {
            return table.select(db, selection, args);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> long[] update(SQLiteTable<T> table, Collection<T> objects) {
        final SQLiteDb db = mPool.acquireDatabase(false);
        try {
            db.begin();
            try {
                final long[] ids = table.update(mSchema, db, objects);
                db.commit();
                return ids;
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> int delete(SQLiteTable<T> table, Collection<T> objects) {
        final SQLiteEntry<T> entry = table.getEntry();
        final long[] ids = new long[objects.size()];
        int index = 0;
        for (T object : objects) {
            ids[index++] = entry.getId(object);
        }
        final StringBuilder selection = new StringBuilder(" WHERE _id IN(");
        StringUtils.append(selection, ids, ", ");
        selection.append(")");
        return delete(table, selection.toString(), new Object[0]);
    }

    <T> int delete(SQLiteTable<T> table, String selection, Object[] args) {
        final SQLiteDb db = mPool.acquireDatabase(false);
        try {
            db.begin();
            try {
                final int affectedRows = table.delete(db, selection, args);
                db.commit();
                return affectedRows;
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

}
