package rxsqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Action3;
import rxsqlite.bindings.RxSQLiteBinding;
import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class SQLiteHelper {

    static final int OPEN_READONLY = 0x00000001;

    static final int OPEN_READWRITE = 0x00000002;

    static final int OPEN_CREATE = 0x00000004;

    static final int OPEN_NOMUTEX = 0x00008000;

    static final int OPEN_FULLMUTEX = 0x00010000;

    private final RxSQLiteBinding mBinding;

    private String mPath;

    private int mVersion;

    private boolean mInMemory;

    private List<Action1<RxSQLiteDb>> mOnOpen = Collections.emptyList();

    private List<Action1<RxSQLiteDb>> mOnCreate = Collections.emptyList();

    private List<Action3<RxSQLiteDb, Integer, Integer>> mOnUpgrade = Collections.emptyList();

    private SQLiteHelper(RxSQLiteBinding binding) {
        mBinding = binding;
    }

    static SQLiteHelper create(RxSQLiteBinding binding, SQLiteConfig config) {
        final SQLiteHelper helper = new SQLiteHelper(binding);
        helper.mPath = config.mPath;
        helper.mVersion = config.mVersion;
        helper.mInMemory = config.mInMemory;
        helper.mOnOpen = new ArrayList<>(config.mOnOpen);
        helper.mOnCreate = new ArrayList<>(config.mOnCreate);
        helper.mOnUpgrade = new ArrayList<>(config.mOnUpgrade);
        return helper;
    }

    RxSQLiteDb openDatabase(boolean writable) {
        String path = mPath;
        int flags = OPEN_NOMUTEX;
        if (writable) {
            flags |= (OPEN_CREATE | OPEN_READWRITE);
        } else {
            flags |= OPEN_READONLY;
        }
        if (mInMemory) {
            path = ":memory:";
            flags = (OPEN_CREATE | OPEN_READWRITE | OPEN_FULLMUTEX);
        }
        final RxSQLiteDb db = mBinding.openDatabase(path, flags);
        dispatchOpen(db);
        if (writable) {
            final int oldVersion = db.getUserVersion();
            final int newVersion = Math.max(mVersion, 1);
            if (oldVersion == 0) {
                dispatchCreate(db, newVersion);
            } else {
                dispatchUpgrade(db, oldVersion, newVersion);
            }
        }
        return db;
    }

    private void dispatchOpen(RxSQLiteDb db) {
        for (final Action1<RxSQLiteDb> action : mOnOpen) {
            action.call(db);
        }
    }

    private void dispatchCreate(RxSQLiteDb db, int version) {
        for (final Action1<RxSQLiteDb> action : mOnCreate) {
            action.call(db);
        }
        db.setUserVersion(version);
    }

    private void dispatchUpgrade(RxSQLiteDb db, int oldVersion, int newVersion) {
        for (final Action3<RxSQLiteDb, Integer, Integer> action : mOnUpgrade) {
            action.call(db, oldVersion, newVersion);
        }
        db.setUserVersion(newVersion);
    }

}
