package rxsqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class SQLiteClient {

    final Map<Class, RxSQLiteTable> mTables = new HashMap<>();

    private final Pool mPool;

    SQLiteClient(Pool pool) {
        mPool = pool;
    }

    <T> Iterable<T> insert(List<T> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        final Class<?> type = items.get(0).getClass();
        final RxSQLiteTable<T> table = findTable(type);
        final RxSQLiteDbImpl db = mPool.acquireDatabase(true);
        try {
            try {
                if (items.size() > 1 || table.hasRelations()) {
                    db.begin();
                }
                final long[] rowIds = table.insert(db, items);
                if (db.inTransaction()) {
                    db.commit();
                }
                RxSQLite.notifyChange(type);
                return table.select(db, rowIds);
            } catch (Exception e) {
                if (db.inTransaction()) {
                    db.rollback();
                }
                throw new RxSQLiteException(e);
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> Iterable<T> select(Class<T> type, String where, Iterable<Object> args) {
        final RxSQLiteTable<T> table = findTable(type);
        final RxSQLiteDbImpl db = mPool.acquireDatabase(false);
        try {
            return table.select(db, where, args);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> int delete(List<T> items) {
        if (items.isEmpty()) {
            return 0;
        }
        final Class<?> type = items.get(0).getClass();
        final RxSQLiteTable<T> table = findTable(type);
        final RxSQLiteDbImpl db = mPool.acquireDatabase(true);
        try {
            try {
                if (items.size() > 1 || table.hasRelations()) {
                    db.begin();
                }
                final int affectedRows = table.delete(db, items);
                if (db.inTransaction()) {
                    db.commit();
                }
                RxSQLite.notifyChange(type);
                return affectedRows;
            } catch (Exception e) {
                if (db.inTransaction()) {
                    db.rollback();
                }
                throw new RxSQLiteException(e);
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    int delete(Class<?> type, String where, Iterable<Object> args) {
        final RxSQLiteTable<?> table = findTable(type);
        final RxSQLiteDbImpl db = mPool.acquireDatabase(true);
        try {
            return table.delete(db, where, args);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> Observable<T> exec(Func1<SQLiteDb, Observable<T>> factory) {
        final RxSQLiteDbImpl db = mPool.acquireDatabase(true);
        try {
            return factory.call(db.mNativeDb);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    void removeDatabase() {
        mPool.lock();
        try {
            mPool.removeDatabase();
        } finally {
            mPool.unlock();
        }
    }

    Iterable<String> tables() {
        final RxSQLiteDbImpl db = mPool.acquireDatabase(false);
        try {
            final RxSQLiteStmt stmt = db.prepare("SELECT name FROM sqlite_master WHERE type='table';");
            try {
                final RxSQLiteCursor cursor = stmt.executeSelect();
                final List<String> tables = new ArrayList<>();
                while (cursor.step()) {
                    tables.add(cursor.getColumnString(0));
                }
                return tables;
            } finally {
                stmt.close();
            }
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> RxSQLiteTable<T> findTable(Class<?> type) {
        final RxSQLiteTable<?> table = mTables.get(type);
        if (table == null) {
            throw new RxSQLiteException("No such table for " + type.getCanonicalName());
        }
        return (RxSQLiteTable<T>) table;
    }

}
