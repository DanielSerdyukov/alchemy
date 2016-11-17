package rxsqlite;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLite {

    private static final Subject<Class<?>, Class<?>> ON_CHANGE = PublishSubject.create();

    private static final Func1<Object, Boolean> NON_NULL = new Func1<Object, Boolean>() {
        @Override
        public Boolean call(Object o) {
            return o != null;
        }
    };

    private static final int BUFFER_SIZE = 2000;

    private static final Where WHERE_ALL = new Where();

    static volatile boolean sLockdown;

    static volatile SQLiteClient sClient;

    public static Config configure() {
        return new InternalConfig(new SQLiteDriver());
    }

    @NonNull
    public static <T> Observable<T> insert(T object) {
        return insert(Collections.singletonList(object));
    }

    @NonNull
    public static <T> Observable<T> insert(Iterable<T> objects) {
        return Observable.from(objects)
                .filter(NON_NULL)
                .buffer(BUFFER_SIZE)
                .flatMap(new Func1<List<T>, Observable<T>>() {
                    @Override
                    public Observable<T> call(List<T> items) {
                        return Observable.from(requireClient().insert(items));
                    }
                });
    }

    @NonNull
    public static <T> Observable<T> select(Class<T> type) {
        return select(type, WHERE_ALL);
    }

    @NonNull
    public static <T> Observable<T> select(final Class<T> type, final Where where) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return Observable.from(requireClient().select(type, where.buildSelectSql(), where.getArgs()));
            }
        });
    }

    @NonNull
    public static <T> Observable<Integer> delete(T object) {
        return delete(Collections.singletonList(object));
    }

    @NonNull
    public static <T> Observable<Integer> delete(Iterable<T> objects) {
        return Observable.from(objects)
                .filter(NON_NULL)
                .buffer(BUFFER_SIZE)
                .flatMap(new Func1<List<T>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<T> items) {
                        return Observable.just(requireClient().delete(items));
                    }
                });
    }

    @NonNull
    public static Observable<Integer> delete(Class<?> type) {
        return delete(type, WHERE_ALL);
    }

    @NonNull
    public static Observable<Integer> delete(final Class<?> type, final Where where) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(requireClient().delete(type, where.buildSelectSql(), where.getArgs()));
            }
        });
    }

    @NonNull
    public static <T> Observable<T> exec(final Func1<SQLiteDb, Observable<T>> factory) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return requireClient().exec(factory);
            }
        });
    }

    @NonNull
    public static Observable<Class<?>> onChange(@NonNull final Class<?> type) {
        return ON_CHANGE.asObservable()
                .filter(new Func1<Class<?>, Boolean>() {
                    @Override
                    public Boolean call(Class<?> changed) {
                        return type.equals(changed);
                    }
                });
    }

    public static void notifyChange(Class<?> type) {
        ON_CHANGE.onNext(type);
    }

    private static SQLiteClient requireClient() {
        if (sClient == null) {
            throw new NullPointerException("Call RxSQLite.init first");
        }
        return sClient;
    }

    public interface Config {

        Config databasePath(File path);

        Config databaseVersion(int version);

        Config doOnOpen(Action1<SQLiteDb> action);

        Config doOnCreate(Action1<SQLiteDb> action);

        Config doOnUpgrade(Action3<SQLiteDb, Integer, Integer> action);

        void apply();

    }

}
