package rxsqlite;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteType {

    String INTEGER = "INTEGER";
    String REAL = "REAL";
    String BLOB = "BLOB";
    String TEXT = "TEXT";

    boolean isAssignable(@NonNull Class<?> type);

    @SupportedTypes
    String getType(@NonNull Class<?> type);

    @Nullable
    <T> T getValue(@NonNull SQLiteCursor stmt, @IntRange(from = 1) int index);

    void bindValue(@NonNull SQLiteStmt stmt, @IntRange(from = 1) int index, @NonNull Object value);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({INTEGER, REAL, BLOB, TEXT})
    @interface SupportedTypes {

    }

}
