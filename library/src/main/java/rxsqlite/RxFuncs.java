package rxsqlite;

import android.support.annotation.NonNull;

import java.util.Iterator;

import rx.Observable;
import rx.functions.Func1;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
final class RxFuncs {

    private RxFuncs() {
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<T>> query(@NonNull final RxSQLiteClient client, @NonNull final Class<T> type,
            @NonNull final String selection, @NonNull final Iterable<Object> bindValues,
            @NonNull final RxSQLiteBinder binder) {
        return new Func1<SQLiteDb, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteDb db) {
                return client.<T>findTable(type).query(db, selection, bindValues, binder);
            }
        };
    }

    @NonNull
    static <T> Func1<SQLiteDb, Observable<T>> save(@NonNull final RxSQLiteClient client,
            @NonNull final Iterable<T> objects, @NonNull final RxSQLiteBinder binder) {
        return new Func1<SQLiteDb, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteDb db) {
                final Iterator<T> iterator = objects.iterator();
                if (iterator.hasNext()) {
                    return client.<T>findTable(iterator.next().getClass()).save(db, objects, binder);
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
            @NonNull final Iterable<Object> bindValues, @NonNull final RxSQLiteBinder binder) {
        return new Func1<SQLiteDb, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(SQLiteDb db) {
                return client.findTable(type).clear(db, selection, bindValues, binder);
            }
        };
    }
}
