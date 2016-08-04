package rxsqlite;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;
import rx.functions.Action3;
import rxsqlite.bindings.RxSQLiteBinding;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteException;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteConfig {

    private static final List<String> KNOWN_BINDINGS = Collections.singletonList(
            "rxsqlite.sqlite4a.SQLite4aBinding"
    );

    final String mPath;

    final int mVersion;

    final boolean mInMemory;

    final List<Action1<RxSQLiteDb>> mOnOpen = new ArrayList<>();

    final List<Action1<RxSQLiteDb>> mOnCreate = new ArrayList<>();

    final List<Action3<RxSQLiteDb, Integer, Integer>> mOnUpgrade = new ArrayList<>();

    private RxSQLiteBinding mBinding;

    private SQLiteConfig(@NonNull String path, int version, boolean inMemory) {
        mPath = path;
        mVersion = version;
        mInMemory = inMemory;
    }

    @NonNull
    public static SQLiteConfig memory() {
        return new SQLiteConfig(":memory:", 1, true);
    }

    @NonNull
    public static SQLiteConfig create(@NonNull File path) {
        return create(path, 1);
    }

    @NonNull
    public static SQLiteConfig create(@NonNull File path, @IntRange(from = 1) int version) {
        final File dir = path.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RxSQLiteException("Database directory not exists");
        }
        return new SQLiteConfig(path.getAbsolutePath(), version, false);
    }

    @NonNull
    private static RxSQLiteBinding createKnownBinding() {
        for (final String knownBinding : KNOWN_BINDINGS) {
            try {
                return (RxSQLiteBinding) Class.forName(knownBinding).newInstance();
            } catch (Exception ignored) {
            }
        }
        throw new RxSQLiteException("No such known sqlite bindings");
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static Map<Class<?>, SQLiteTable<?>> loadTables() {
        try {
            return (Map<Class<?>, SQLiteTable<?>>) Class.forName("rxsqlite.SQLite$$Schema")
                    .getDeclaredMethod("init")
                    .invoke(null);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @NonNull
    public SQLiteConfig binding(@NonNull RxSQLiteBinding binding) {
        mBinding = binding;
        return this;
    }

    @NonNull
    public SQLiteConfig enableTracing() {
        return doOnOpen(new Action1<RxSQLiteDb>() {
            @Override
            public void call(RxSQLiteDb db) {
                db.enableTracing();
            }
        });
    }

    @NonNull
    public SQLiteConfig createType(@NonNull Class<?> clazz, @NonNull SQLiteType type) {
        Types.create(clazz, type);
        return this;
    }

    @NonNull
    public SQLiteConfig doOnOpen(@NonNull Action1<RxSQLiteDb> action) {
        mOnOpen.add(action);
        return this;
    }

    @NonNull
    public SQLiteConfig doOnCreate(@NonNull Action1<RxSQLiteDb> action) {
        mOnCreate.add(action);
        return this;
    }

    @NonNull
    public SQLiteConfig doOnUpgrade(@NonNull Action3<RxSQLiteDb, Integer, Integer> action) {
        mOnUpgrade.add(action);
        return this;
    }

    @NonNull
    SQLiteClient buildClient(@NonNull Context context) {
        RxSQLiteBinding binding = mBinding;
        if (binding == null) {
            binding = createKnownBinding();
        }
        binding.loadLibrary(context);
        return new SQLiteClient(createPool(binding), loadTables());
    }

    @VisibleForTesting
    SQLitePool createPool(RxSQLiteBinding binding) {
        final SQLiteHelper helper = SQLiteHelper.create(binding, this);
        if (mInMemory) {
            return new InMemoryPool(helper);
        } else {
            return new ConcurrentPool(helper);
        }
    }

}
