package rxsqlite;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.Collection;
import java.util.Collections;

import sqlite4a.SQLiteRow;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
class Types {

    private final Collection<RxSQLiteType> mTypes;

    Types(@NonNull Collection<RxSQLiteType> types) {
        mTypes = Collections.unmodifiableCollection(types);
    }

    @NonNull
    String getType(Class<?> type) {
        for (final RxSQLiteType customType : mTypes) {
            if (customType.isAssignable(type)) {
                return customType.getType(type);
            }
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    void bindValue(@NonNull SQLiteStmt stmt, @IntRange(from = 1) int index, @Nullable Object value) {
        if (value == null) {
            stmt.bindNull(index);
        } else if (value instanceof Integer || value instanceof Long
                || value instanceof Short) {
            stmt.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Double || value instanceof Float) {
            stmt.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof CharSequence) {
            stmt.bindString(index, value.toString());
        } else if (value instanceof Boolean) {
            if ((boolean) value) {
                stmt.bindLong(index, 1);
            } else {
                stmt.bindLong(index, 0);
            }
        } else if (value instanceof byte[]) {
            stmt.bindBlob(index, (byte[]) value);
        } else {
            bindCustomValue(stmt, index, value);
        }
    }

    @Nullable
    <T> T getValue(@NonNull SQLiteRow row, @IntRange(from = 1) int index, @NonNull Class<T> type) {
        for (final RxSQLiteType customType : mTypes) {
            if (customType.isAssignable(type)) {
                return customType.getValue(row, index);
            }
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    @Nullable
    <T extends Enum<T>> T getEnumValue(@NonNull SQLiteRow row, @IntRange(from = 1) int index, @NonNull Class<T> type) {
        final String value = row.getColumnString(index);
        if (value != null) {
            return Enum.valueOf(type, value);
        }
        return null;
    }

    @VisibleForTesting
    void bindCustomValue(@NonNull SQLiteStmt stmt, @IntRange(from = 1) int index, @NonNull Object value) {
        final Class<?> type = value.getClass();
        for (final RxSQLiteType customType : mTypes) {
            if (customType.isAssignable(type)) {
                customType.bindValue(stmt, index, value);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }


}
