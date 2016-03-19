package rxsqlite;

import android.support.annotation.NonNull;

import java.util.Iterator;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
final class Funcs {

    private Funcs() {
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<T>> query(@NonNull final RxSQLiteClient client, @NonNull final Class<T> type,
            @NonNull final String selection, @NonNull final Iterable<Object> bindValues) {
        return new Func1<SQLiteDb, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteDb db) {
                return client.<T>findTable(type).query(db, selection, bindValues);
            }
        };
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<T>> rawQuery(@NonNull final RxSQLiteClient client,
            @NonNull final Class<T> type, @NonNull final String sql, @NonNull final Iterable<Object> bindValues) {
        return new Func1<SQLiteDb, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteDb db) {
                final RxSQLiteTable<T> table = client.findTable(type);
                return client.query(sql, bindValues, new Func2<SQLiteDb, SQLiteCursor, T>() {
                    @Override
                    public T call(SQLiteDb db, SQLiteCursor cursor) {
                        return table.instantiate(db, cursor);
                    }
                });
            }
        };
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<T>> save(@NonNull final RxSQLiteClient client,
            @NonNull final Iterable<T> objects) {
        return new Func1<SQLiteDb, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteDb db) {
                final Iterator<T> iterator = objects.iterator();
                if (iterator.hasNext()) {
                    return client.<T>findTable(iterator.next().getClass()).save(db, objects);
                } else {
                    return Observable.empty();
                }
            }
        };
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<Integer>> remove(@NonNull final RxSQLiteClient client,
            @NonNull final Iterable<T> objects) {
        return new Func1<SQLiteDb, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(SQLiteDb db) {
                final Iterator<T> iterator = objects.iterator();
                if (iterator.hasNext()) {
                    return client.<T>findTable(iterator.next().getClass()).remove(db, objects);
                } else {
                    return Observable.empty();
                }
            }
        };
    }

    @NonNull
    static Func1<SQLiteDb, Observable<Integer>> clear(@NonNull final RxSQLiteClient client,
            @NonNull final Class<?> type, @NonNull final String selection,
            @NonNull final Iterable<Object> bindValues) {
        return new Func1<SQLiteDb, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(SQLiteDb db) {
                return client.findTable(type).clear(db, selection, bindValues);
            }
        };
    }

}
