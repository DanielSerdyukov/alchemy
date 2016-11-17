package rxsqlite;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Action3;
import sqlite4a.SQLite;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class Connection {

    final List<Action1<SQLiteDb>> mOnOpen = new ArrayList<>();

    final List<Action1<SQLiteDb>> mOnCreate = new ArrayList<>();

    final List<Action3<SQLiteDb, Integer, Integer>> mOnUpgrade = new ArrayList<>();

    final List<RxSQLiteTable> mTables = new ArrayList<>();

    private final SQLiteDriver mDriver;

    boolean mInMemory = true;

    String mDatabasePath = ":memory:";

    int mDatabaseVersion = 1;

    Connection(SQLiteDriver driver) {
        mDriver = driver;
    }

    RxSQLiteDbImpl openDatabase(boolean primary) {
        int flags = 0;
        if (mInMemory) {
            flags |= SQLite.OPEN_FULLMUTEX;
        } else {
            flags |= SQLite.OPEN_NOMUTEX;
        }
        if (primary) {
            flags |= (SQLite.OPEN_CREATE | SQLite.OPEN_READWRITE);
        } else {
            flags |= SQLite.OPEN_READONLY;
        }
        return openDatabase(primary, flags);
    }

    private RxSQLiteDbImpl openDatabase(boolean primary, int flags) {
        final RxSQLiteDbImpl db = mDriver.open(mDatabasePath, flags);
        dispatchOpen(db);
        if (primary) {
            final int oldVersion = db.mNativeDb.getUserVersion();
            if (oldVersion == 0) {
                dispatchCreate(db);
            } else if (oldVersion != mDatabaseVersion) {
                dispatchUpgrade(db, oldVersion);
            }
        }
        return db;
    }

    private void dispatchOpen(RxSQLiteDbImpl db) {
        for (final Action1<SQLiteDb> action : mOnOpen) {
            action.call(db.mNativeDb);
        }
        db.mNativeDb.exec("PRAGMA foreign_keys = ON;");
    }

    private void dispatchCreate(RxSQLiteDbImpl db) {
        for (final RxSQLiteTable table : mTables) {
            table.create(db);
            table.doOnCreate(db);
        }
        for (final Action1<SQLiteDb> onCreate : mOnCreate) {
            onCreate.call(db.mNativeDb);
        }
        db.mNativeDb.setUserVersion(mDatabaseVersion);
    }

    private void dispatchUpgrade(RxSQLiteDbImpl db, int oldVersion) {
        for (final Action3<SQLiteDb, Integer, Integer> onUpgrade : mOnUpgrade) {
            onUpgrade.call(db.mNativeDb, oldVersion, mDatabaseVersion);
        }
        db.mNativeDb.setUserVersion(mDatabaseVersion);
    }

}
