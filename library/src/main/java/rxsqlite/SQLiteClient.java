package rxsqlite;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.Subject;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;

/**
 * @author Daniel Serdyukov
 */
class SQLiteClient {

    private final SQLitePool mPool;

    private final Map<Class<?>, SQLiteTable<?>> mTables;

    SQLiteClient(SQLitePool pool, Map<Class<?>, SQLiteTable<?>> tables) {
        mPool = pool;
        mTables = tables;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    <T> SQLiteTable<T> findTable(Class<?> clazz) {
        final SQLiteTable<?> table = mTables.get(clazz);
        if (table == null) {
            throw new RxSQLiteException("No such table for " + clazz);
        }
        return (SQLiteTable<T>) table;
    }

    @NonNull
    <T> Collection<T> insert(@NonNull Collection<T> objects, @NonNull Subject<Class<?>, Class<?>> subject) {
        final Class<?> clazz = objects.iterator().next().getClass();
        final SQLiteTable<T> table = findTable(clazz);
        RxSQLiteDb db = mPool.acquireDatabase(true);
        final long[] rowIds;
        try {
            rowIds = table.insert(db, objects);
            if (rowIds.length > 0) {
                subject.onNext(clazz);
            }
        } finally {
            mPool.releaseDatabase(db);
        }
        db = mPool.acquireDatabase(false);
        try {
            return table.select(db, rowIds);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    @NonNull
    <T> Collection<T> select(@NonNull Class<T> type, @NonNull String where, @NonNull Iterable<Object> args) {
        final SQLiteTable<T> table = findTable(type);
        final RxSQLiteDb db = mPool.acquireDatabase(false);
        try {
            return table.select(db, where, args);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    @NonNull
    <T> Collection<T> rawSelect(@NonNull Class<T> type, @NonNull String sql, @NonNull Iterable<Object> args) {
        final SQLiteTable<T> table = findTable(type);
        final RxSQLiteDb db = mPool.acquireDatabase(false);
        try {
            return table.rawSelect(db, sql, args);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> int delete(@NonNull Collection<T> objects, @NonNull Subject<Class<?>, Class<?>> subject) {
        final Class<?> clazz = objects.iterator().next().getClass();
        final SQLiteTable<T> table = findTable(clazz);
        final long[] rowIds = new long[objects.size()];
        int index = 0;
        for (final T object : objects) {
            rowIds[index] = table.getId(object);
            ++index;
        }
        final RxSQLiteDb db = mPool.acquireDatabase(true);
        try {
            final int affectedRows = table.delete(db, rowIds);
            if (affectedRows > 0) {
                subject.onNext(clazz);
            }
            return affectedRows;
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    int delete(@NonNull Class<?> type, @NonNull String where, @NonNull Iterable<Object> args,
            @NonNull Subject<Class<?>, Class<?>> subject) {
        final SQLiteTable<?> table = findTable(type);
        final RxSQLiteDb db = mPool.acquireDatabase(true);
        try {
            final int affectedRows = table.delete(db, where, args);
            if (affectedRows > 0) {
                subject.onNext(type);
            }
            return affectedRows;
        } finally {
            mPool.releaseDatabase(db);
        }
    }

    <T> Observable<T> exec(@NonNull Func1<RxSQLiteDb, Observable<T>> func) {
        final RxSQLiteDb db = mPool.acquireDatabase(true);
        try {
            return func.call(db);
        } finally {
            mPool.releaseDatabase(db);
        }
    }

}
