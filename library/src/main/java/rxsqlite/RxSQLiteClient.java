package rxsqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.functions.Func1;
import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteStmt;
import sqlite4a.SQLiteValue;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class RxSQLiteClient implements Closeable {

    private static final String APP_MAIN_DB = "main.db";

    private static final String SHARED_MEMORY = "file::memory:?cache=shared";

    private final Queue<SQLiteDb> mConnections = new ConcurrentLinkedQueue<>();

    private final ConcurrentMap<Class<?>, RxSQLiteTable<?>> mTables = new ConcurrentHashMap<>();

    private final String mDatabasePath;

    @SQLiteDb.OpenFlags
    private final int mOpenFlags;

    private final int mUserVersion;

    private final List<Action1<SQLiteDb>> mOnOpen;

    private final List<Action1<SQLiteDb>> mOnCreate;

    private final List<Action3<SQLiteDb, Integer, Integer>> mOnUpgrade;

    private final Types mTypes;

    private RxSQLiteClient(@NonNull Builder builder) {
        this(builder, new Types(builder.mTypes));
    }

    @VisibleForTesting
    RxSQLiteClient(@NonNull Builder builder, @NonNull Types types) {
        mDatabasePath = builder.mDatabasePath;
        mOpenFlags = builder.mOpenFlags;
        mUserVersion = builder.mUserVersion;
        mOnOpen = Collections.unmodifiableList(builder.mOnOpen);
        mOnCreate = Collections.unmodifiableList(builder.mOnCreate);
        mOnUpgrade = Collections.unmodifiableList(builder.mOnUpgrade);
        mTypes = types;
    }

    @NonNull
    public static Builder memory() {
        return new Builder(SHARED_MEMORY, SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE | SQLiteDb.OPEN_URI, 1);
    }

    @NonNull
    public static Builder builder(@NonNull Context context, @IntRange(from = 1) int version) {
        return builder(context.getDatabasePath(APP_MAIN_DB), version);
    }

    @NonNull
    public static Builder builder(@NonNull File path, @IntRange(from = 1) int version) {
        return builder(path, SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE, version);
    }

    @NonNull
    public static Builder builder(@NonNull File path, @SQLiteDb.OpenFlags int flags, @IntRange(from = 1) int version) {
        ensureDatabasePathExists(path);
        return new Builder(path.getAbsolutePath(), flags, version);
    }

    @VisibleForTesting
    static void ensureDatabasePathExists(@NonNull File path) {
        final File dir = path.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalArgumentException("Can't open database dir");
        }
    }

    @NonNull
    public <T> Observable<T> query(@NonNull final String sql, @NonNull final Iterable<Object> bindValues,
                                   @NonNull final Func1<SQLiteCursor, T> factory) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                final SQLiteDb db = acquireDatabase(mConnections);
                try {
                    final SQLiteStmt stmt = db.prepare(sql);
                    try {
                        int index = 0;
                        for (final Object value : bindValues) {
                            mTypes.bindValue(stmt, ++index, value);
                        }
                        final SQLiteCursor rows = stmt.executeQuery();
                        while (rows.step()) {
                            subscriber.onNext(factory.call(rows));
                        }
                        subscriber.onCompleted();
                    } finally {
                        stmt.close();
                    }
                } finally {
                    releaseDatabase(mConnections, db);
                }
            }
        });
    }

    @NonNull
    public <T> Observable<T> execute(@NonNull final Func1<SQLiteDb, Observable<T>> factory) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                final SQLiteDb db = acquireDatabase(mConnections);
                try {
                    return factory.call(db);
                } finally {
                    releaseDatabase(mConnections, db);
                }
            }
        });
    }

    @NonNull
    public <T> Observable<T> transaction(@NonNull final Func1<SQLiteDb, Observable<T>> factory) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                final SQLiteDb db = acquireDatabase(mConnections);
                try {
                    db.exec("BEGIN;");
                    final Observable<T> observable = factory.call(db);
                    db.exec("COMMIT;");
                    return observable;
                } catch (Throwable e) {
                    db.exec("ROLLBACK;");
                    return Observable.error(e);
                } finally {
                    releaseDatabase(mConnections, db);
                }
            }
        });
    }

    @Override
    public void close() {
        close(mConnections);
    }

    @Keep
    <T> void registerTable(@NonNull Class<T> type, @NonNull RxSQLiteTable<T> table) {
        mTables.putIfAbsent(type, table);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    <T> RxSQLiteTable<T> findTable(@NonNull Class<?> type) {
        final RxSQLiteTable<?> table = mTables.get(type);
        if (table == null) {
            throw new IllegalArgumentException("No such table for type "
                    + type.getCanonicalName());
        }
        return (RxSQLiteTable<T>) table;
    }

    @NonNull
    @VisibleForTesting
    SQLiteDb acquireDatabase(@NonNull Queue<SQLiteDb> connections) {
        final SQLiteDb db = connections.poll();
        if (db == null) {
            return openAndConfigureDatabase();
        }
        return db;
    }

    @VisibleForTesting
    void releaseDatabase(@NonNull Queue<SQLiteDb> connections, @NonNull SQLiteDb db) {
        connections.add(db);
    }

    @VisibleForTesting
    int getDatabaseVersion(@NonNull SQLiteDb db) {
        final SQLiteStmt stmt = db.prepare("PRAGMA user_version;");
        try {
            final SQLiteCursor rows = stmt.executeQuery();
            if (rows.step()) {
                return (int) rows.getColumnLong(0);
            }
        } finally {
            stmt.close();
        }
        return 0;
    }

    @VisibleForTesting
    void setDatabaseVersion(@NonNull SQLiteDb db, int version) {
        db.exec("PRAGMA user_version = " + version + ";");
    }

    @NonNull
    @VisibleForTesting
    SQLiteDb openAndConfigureDatabase() {
        final SQLiteDb db = openDatabase(mDatabasePath, mOpenFlags);
        dispatchDatabaseOpen(db);
        final int version = getDatabaseVersion(db);
        if (version == 0) {
            dispatchDatabaseCreate(db);
        } else if (mUserVersion > version) {
            dispatchDatabaseUpgrade(db, version, mUserVersion);
        }
        setDatabaseVersion(db, mUserVersion);
        return db;
    }

    @NonNull
    @VisibleForTesting
    SQLiteDb openDatabase(@NonNull String databasePath, @SQLiteDb.OpenFlags int flags) {
        return SQLiteDb.open(databasePath, flags);
    }


    @VisibleForTesting
    void dispatchDatabaseOpen(@NonNull SQLiteDb db) {
        for (final Action1<SQLiteDb> action : mOnOpen) {
            action.call(db);
        }
    }

    @VisibleForTesting
    void dispatchDatabaseCreate(@NonNull SQLiteDb db) {
        createAutoGeneratedSchema(db, mTables.values());
        for (final Action1<SQLiteDb> action : mOnCreate) {
            action.call(db);
        }
    }

    @VisibleForTesting
    void createAutoGeneratedSchema(@NonNull SQLiteDb db, @NonNull Iterable<RxSQLiteTable<?>> tables) {
        for (final RxSQLiteTable<?> table : tables) {
            table.create(db);
        }
    }

    @VisibleForTesting
    void dispatchDatabaseUpgrade(@NonNull SQLiteDb db, int oldVersion, int newVersion) {
        for (final Action3<SQLiteDb, Integer, Integer> action : mOnUpgrade) {
            action.call(db, oldVersion, newVersion);
        }
    }

    @VisibleForTesting
    void close(@NonNull Queue<SQLiteDb> connections) {
        for (final SQLiteDb db : connections) {
            db.close();
        }
        connections.clear();
    }

    public static class Builder {

        private final List<RxSQLiteType> mTypes = new ArrayList<>();

        private final String mDatabasePath;

        @SQLiteDb.OpenFlags
        private final int mOpenFlags;

        private final int mUserVersion;

        private final List<Action1<SQLiteDb>> mOnOpen = new ArrayList<>();

        private final List<Action1<SQLiteDb>> mOnCreate = new ArrayList<>();

        private final List<Action3<SQLiteDb, Integer, Integer>> mOnUpgrade = new ArrayList<>();

        private Builder(@NonNull String databasePath, @SQLiteDb.OpenFlags int flags, @IntRange(from = 1) int version) {
            mDatabasePath = databasePath;
            mOpenFlags = flags;
            mUserVersion = version;
        }

        @NonNull
        public Builder enableTracing() {
            return doOnOpen(new Action1<SQLiteDb>() {
                @Override
                public void call(SQLiteDb db) {
                    db.enableTracing();
                }
            });
        }

        @NonNull
        public Builder createCollation(@NonNull final String name, @NonNull final Comparator<String> collation) {
            return doOnOpen(new Action1<SQLiteDb>() {
                @Override
                public void call(SQLiteDb db) {
                    db.createCollation(name, collation);
                }
            });
        }

        @NonNull
        public Builder createFunction(@NonNull final String name, final int numArgs,
                                      final Func1<SQLiteValue[], Object> func) {
            return doOnOpen(new Action1<SQLiteDb>() {
                @Override
                public void call(SQLiteDb db) {
                    db.createFunction(name, numArgs, new CustomFunc(func));
                }
            });
        }

        @NonNull
        public Builder doOnOpen(@NonNull Action1<SQLiteDb> onOpen) {
            mOnOpen.add(onOpen);
            return this;
        }

        @NonNull
        public Builder doOnCreate(@NonNull Action1<SQLiteDb> onCreate) {
            mOnCreate.add(onCreate);
            return this;
        }

        @NonNull
        public Builder doOnUpgrade(@NonNull Action3<SQLiteDb, Integer, Integer> onUpgrade) {
            mOnUpgrade.add(onUpgrade);
            return this;
        }

        @NonNull
        public Builder registerCustomType(@NonNull RxSQLiteType type) {
            mTypes.add(type);
            return this;
        }

        @NonNull
        @SuppressLint("NewApi")
        public RxSQLiteClient build() {
            final RxSQLiteClient client = new RxSQLiteClient(this);
            try {
                final Class<?> schema = Class.forName("rxsqlite.SQLite$$Schema");
                schema.getDeclaredMethod("create", RxSQLiteClient.class, Types.class)
                        .invoke(null, client, client.mTypes);
            } catch (ClassNotFoundException | NoSuchMethodException
                    | InvocationTargetException | IllegalAccessException e) {
                Log.e(RxSQLite.class.getSimpleName(), e.getMessage());
            }
            return client;
        }

    }

}
