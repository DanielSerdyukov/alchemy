package rxsqlite;

import android.support.annotation.NonNull;

import rx.Observable;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteTable<T> {

    void create(@NonNull SQLiteDb db);

    @NonNull
    Observable<T> query(@NonNull SQLiteDb db, @NonNull String selection, @NonNull Iterable<Object> bindValues);

    @NonNull
    Observable<T> save(@NonNull SQLiteDb db, @NonNull Iterable<T> objects);

    @NonNull
    Observable<Integer> remove(@NonNull SQLiteDb db, Iterable<T> objects);

    @NonNull
    Observable<Integer> clear(@NonNull SQLiteDb db, @NonNull String selection, @NonNull Iterable<Object> bindValues);

}
