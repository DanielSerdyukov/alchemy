package rxsqlite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLite {

    static final Subject<Class<?>, Class<?>> ON_CHANGE = PublishSubject.create();

    private static final AtomicReference<SQLiteClient> CLIENT_REF = new AtomicReference<>();

    private static final Where ALL = new Where();

    private RxSQLite() {
    }

    public static void init(@NonNull Context context, @NonNull SQLiteConfig config) {
        CLIENT_REF.compareAndSet(null, config.buildClient(context.getApplicationContext()));
    }

    @NonNull
    public static <T> Observable<T> insert(@NonNull T object) {
        return insert(Collections.singletonList(object));
    }

    @NonNull
    public static <T> Observable<T> insert(@NonNull final Collection<T> objects) {
        return client().flatMap(new Func1<SQLiteClient, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteClient client) {
                if (objects.isEmpty()) {
                    return Observable.empty();
                }
                return Observable.from(client.insert(objects, ON_CHANGE));
            }
        });
    }

    @NonNull
    public static <T> Observable<T> select(@NonNull Class<T> type) {
        return select(type, ALL);
    }

    @NonNull
    public static <T> Observable<T> select(@NonNull final Class<T> type, @NonNull final Where where) {
        return client().flatMap(new Func1<SQLiteClient, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteClient client) {
                return Observable.from(client.select(type, where.buildSelectSql(), where.getArgs()));
            }
        });
    }

    @NonNull
    public static <T> Observable<T> rawSelect(@NonNull final Class<T> type, @NonNull final String sql,
            final Object... args) {
        return client().flatMap(new Func1<SQLiteClient, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteClient client) {
                return Observable.from(client.rawSelect(type, sql, Arrays.asList(args)));
            }
        });
    }

    @NonNull
    public static Observable<Integer> delete(@NonNull Class<?> type) {
        return delete(type, ALL);
    }

    @NonNull
    public static Observable<Integer> delete(@NonNull final Class<?> type, @NonNull final Where where) {
        return client().flatMap(new Func1<SQLiteClient, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(SQLiteClient client) {
                return Observable.just(client.delete(type, where.buildDeleteSql(), where.getArgs(), ON_CHANGE));
            }
        });
    }

    @NonNull
    public static <T> Observable<Integer> delete(@NonNull T object) {
        return delete(Collections.singletonList(object));
    }

    @NonNull
    public static <T> Observable<Integer> delete(@NonNull final Collection<T> objects) {
        return client().flatMap(new Func1<SQLiteClient, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(SQLiteClient client) {
                if (objects.isEmpty()) {
                    return Observable.empty();
                }
                return Observable.just(client.delete(objects, ON_CHANGE));
            }
        });
    }

    @NonNull
    public static <T> Observable<T> exec(@NonNull final Func1<RxSQLiteDb, Observable<T>> func) {
        return client().flatMap(new Func1<SQLiteClient, Observable<T>>() {
            @Override
            public Observable<T> call(SQLiteClient client) {
                return client.exec(func);
            }
        });
    }

    @NonNull
    public static Subscription onChange(@NonNull final Class<?> type, final Action0 action) {
        return ON_CHANGE.filter(new Func1<Class<?>, Boolean>() {
            @Override
            public Boolean call(Class<?> clazz) {
                return clazz.equals(type);
            }
        }).subscribe(new Action1<Class<?>>() {
            @Override
            public void call(Class<?> clazz) {
                action.call();
            }
        });
    }

    @VisibleForTesting
    static void setClient(SQLiteClient client) {
        CLIENT_REF.set(client);
    }

    private static Observable<SQLiteClient> client() {
        return Observable.defer(new Func0<Observable<SQLiteClient>>() {
            @Override
            public Observable<SQLiteClient> call() {
                final SQLiteClient client = CLIENT_REF.get();
                if (client == null) {
                    throw new RxSQLiteException("SQLite client is null. Call RxSQLite.init first.");
                }
                return Observable.just(client);
            }
        });
    }

}
