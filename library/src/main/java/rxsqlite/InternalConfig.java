package rxsqlite;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import rx.functions.Action1;
import rx.functions.Action3;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class InternalConfig implements RxSQLite.Config {

    private final Connection mConnection;

    InternalConfig(SQLiteDriver driver) {
        mConnection = new Connection(driver);
    }

    @Override
    public RxSQLite.Config databasePath(File path) {
        final File dir = path.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RxSQLiteException("database dir not exists and can't be created");
        }
        try {
            mConnection.mDatabasePath = path.getCanonicalPath();
            mConnection.mInMemory = false;
        } catch (IOException e) {
            throw new RxSQLiteException(e);
        }
        return this;
    }

    @Override
    public RxSQLite.Config databaseVersion(int version) {
        mConnection.mDatabaseVersion = version;
        return this;
    }

    @Override
    public RxSQLite.Config doOnOpen(Action1<SQLiteDb> action) {
        mConnection.mOnOpen.add(action);
        return this;
    }

    @Override
    public RxSQLite.Config doOnCreate(Action1<SQLiteDb> action) {
        mConnection.mOnCreate.add(action);
        return this;
    }

    @Override
    public RxSQLite.Config doOnUpgrade(Action3<SQLiteDb, Integer, Integer> action) {
        mConnection.mOnUpgrade.add(action);
        return this;
    }

    @Override
    @SuppressWarnings({"TryWithIdenticalCatches", "unchecked"})
    public void apply() {
        if (RxSQLite.sLockdown) {
            return;
        }
        Map<Class, RxSQLiteTable> tables = Collections.emptyMap();
        try {
            tables = (Map<Class, RxSQLiteTable>) Class.forName("rxsqlite.SQLite$$Schema")
                    .getDeclaredField("TABLES").get(null);
        } catch (IllegalAccessException ignored) {
            Log.e(InternalConfig.class.getName(), ignored.getMessage(), ignored);
        } catch (NoSuchFieldException ignored) {
            Log.e(InternalConfig.class.getName(), ignored.getMessage(), ignored);
        } catch (ClassNotFoundException ignored) {
            Log.e(InternalConfig.class.getName(), ignored.getMessage(), ignored);
        }

        mConnection.mTables.addAll(tables.values());

        final Pool pool;
        if (mConnection.mInMemory) {
            pool = new SerializedPool(mConnection);
        } else {
            pool = new ConcurrentPool(mConnection);
        }
        final SQLiteClient client = new SQLiteClient(pool);
        client.mTables.putAll(tables);
        RxSQLite.sClient = client;
        RxSQLite.sLockdown = true;
    }

}
