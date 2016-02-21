package rxsqlite;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.functions.Func1;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
public final class RxSQLite {

    private static final AtomicReference<RxSQLiteClient> CLIENT = new AtomicReference<>();

    private static final RxSQLiteWhere EMPTY_WHERE = new RxSQLiteWhere();

    private RxSQLite() {
    }

    public static void register(@NonNull RxSQLiteClient client) {
        CLIENT.compareAndSet(null, client);
    }

    @NonNull
    public static <T> Observable<T> query(@NonNull Class<T> type) {
        return query(type, EMPTY_WHERE);
    }

    @NonNull
    public static <T> Observable<T> query(@NonNull final Class<T> type, @NonNull final RxSQLiteWhere where) {
        final RxSQLiteClient client = requireClientNotNull();
        return client.execute(RxFuncs.query(client, type, where.toSelectSql(),
                where.getBindValues(), client.getBinder()));
    }

    @NonNull
    public static <T> Observable<T> save(@NonNull T object) {
        final RxSQLiteClient client = requireClientNotNull();
        return client.execute(RxFuncs.save(client, Collections.singletonList(object), client.getBinder()));
    }

    @NonNull
    public static <T> Observable<T> saveAll(@NonNull Collection<T> objects) {
        final RxSQLiteClient client = requireClientNotNull();
        if (objects.isEmpty()) {
            return Observable.empty();
        } else if (objects.size() == 1) {
            return client.execute(RxFuncs.save(client, objects, client.getBinder()));
        } else {
            return client.transaction(RxFuncs.save(client, objects, client.getBinder()));
        }
    }

    @NonNull
    public static <T> Observable<Integer> remove(@NonNull T object) {
        final RxSQLiteClient client = requireClientNotNull();
        return client.execute(RxFuncs.remove(client, Collections.singletonList(object)));
    }

    @NonNull
    public static <T> Observable<Integer> removeAll(@NonNull Collection<T> objects) {
        final RxSQLiteClient client = requireClientNotNull();
        if (objects.isEmpty()) {
            return Observable.empty();
        } else if (objects.size() == 1) {
            return client.execute(RxFuncs.remove(client, objects));
        } else {
            return client.transaction(RxFuncs.remove(client, objects));
        }
    }

    @NonNull
    public static Observable<Integer> clear(@NonNull Class<?> type) {
        return clear(type, EMPTY_WHERE);
    }

    @NonNull
    public static Observable<Integer> clear(@NonNull Class<?> type, @NonNull RxSQLiteWhere where) {
        final RxSQLiteClient client = requireClientNotNull();
        return client.execute(RxFuncs.clear(client, type, where.toDeleteSql(), where.getBindValues(),
                client.getBinder()));
    }

    @NonNull
    public static <T> Observable<T> execute(@NonNull Func1<SQLiteDb, Observable<T>> factory) {
        return requireClientNotNull().execute(factory);
    }

    @VisibleForTesting
    static RxSQLiteClient requireClientNotNull() {
        final RxSQLiteClient client = acquireClient();
        if (client == null) {
            throw new NullPointerException("RxSQLiteClient not registered, "
                    + "call RxSQLite.register in your Application.onCreate");
        }
        return client;
    }

    @VisibleForTesting
    static RxSQLiteClient acquireClient() {
        return CLIENT.get();
    }

}
