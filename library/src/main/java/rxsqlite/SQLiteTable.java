package rxsqlite;

import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
abstract class SQLiteTable<T> {

    private final String mName;

    private final String[] mColumns;

    private final boolean mHasRelations;

    public SQLiteTable(String name, String[] columns, boolean hasRelations) {
        mName = name;
        mColumns = columns;
        mHasRelations = hasRelations;
    }

    public long[] insert(RxSQLiteDb db, Collection<T> objects) {
        final StringBuilder sql = new StringBuilder("INSERT INTO ").append(getName());
        Utils.append(sql, " VALUES(", Collections.nCopies(getColumns().length, "?"), ", ");
        sql.append(");");
        final RxSQLiteStmt stmt = db.prepare(sql.toString());
        try {
            if (hasRelations() || objects.size() > 1) {
                return batchInsert(db, stmt, objects);
            }
            return singleInsert(db, stmt, objects);
        } finally {
            stmt.close();
        }
    }

    public abstract T instantiate(RxSQLiteDb db, RxSQLiteCursor cursor);

    protected abstract void create(RxSQLiteDb db);

    protected abstract long getId(T object);

    protected abstract void bind(RxSQLiteStmt stmt, T object);

    protected void bindValue(RxSQLiteStmt stmt, int index, Object value) {
        Types.bindValue(stmt, index, value);
    }

    protected Object getValue(RxSQLiteCursor cursor, int index, Class<?> clazz) {
        return Types.getValue(cursor, index, clazz);
    }

    protected void doAfterInsert(RxSQLiteDb db, T object, long id) {

    }

    Collection<T> select(RxSQLiteDb db, long[] rowIds) {
        final StringBuilder sql = new StringBuilder("SELECT * FROM ").append(getName()).append(" WHERE _id IN(");
        for (int i = 0, last = rowIds.length - 1; i <= last; ++i) {
            sql.append(rowIds[i]);
            if (i < last) {
                sql.append(", ");
            }
        }
        return rawSelect(db, sql.append(");").toString(), Collections.emptyList());
    }

    Collection<T> select(RxSQLiteDb db, String where, Iterable<Object> args) {
        return rawSelect(db, "SELECT * FROM " + getName() + where, args);
    }

    Collection<T> rawSelect(RxSQLiteDb db, String sql, Iterable<Object> args) {
        final RxSQLiteStmt stmt = db.prepare(sql);
        try {
            bindValues(stmt, args);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<T> list = new ArrayList<>();
            while (cursor.step()) {
                list.add(instantiate(db, cursor));
            }
            return list;
        } finally {
            stmt.close();
        }
    }

    int delete(RxSQLiteDb db, long[] rowIds) {
        final StringBuilder sql = new StringBuilder("DELETE FROM ").append(getName()).append(" WHERE _id IN(");
        Utils.append(sql, rowIds, ", ");
        return rawDelete(db, sql.append(");").toString(), Collections.emptyList());
    }

    int delete(RxSQLiteDb db, String where, Iterable<Object> args) {
        return rawDelete(db, "DELETE FROM " + getName() + where, args);
    }

    @VisibleForTesting
    String getName() {
        return mName;
    }

    @VisibleForTesting
    String[] getColumns() {
        return mColumns;
    }

    @VisibleForTesting
    boolean hasRelations() {
        return mHasRelations;
    }

    private int rawDelete(RxSQLiteDb db, String sql, Iterable<Object> args) {
        final RxSQLiteStmt stmt = db.prepare(sql);
        try {
            if (hasRelations()) {
                return batchDelete(db, stmt, args);
            }
            return singleDelete(db, stmt, args);
        } finally {
            stmt.close();
        }
    }

    private long[] singleInsert(RxSQLiteDb db, RxSQLiteStmt stmt, Collection<T> objects) {
        final T object = objects.iterator().next();
        bind(stmt, object);
        final long rowId = stmt.executeInsert();
        doAfterInsert(db, object, rowId);
        return new long[]{rowId};
    }

    private long[] batchInsert(RxSQLiteDb db, RxSQLiteStmt stmt, Collection<T> objects) {
        db.begin();
        try {
            int index = 0;
            final long[] rowIds = new long[objects.size()];
            for (final T object : objects) {
                bind(stmt, object);
                final long rowId = stmt.executeInsert();
                doAfterInsert(db, object, rowId);
                rowIds[index] = rowId;
                stmt.clearBindings();
                ++index;
            }
            db.commit();
            return rowIds;
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    private int singleDelete(RxSQLiteDb db, RxSQLiteStmt stmt, Iterable<Object> args) {
        bindValues(stmt, args);
        return stmt.executeUpdateDelete();
    }

    private int batchDelete(RxSQLiteDb db, RxSQLiteStmt stmt, Iterable<Object> args) {
        bindValues(stmt, args);
        db.begin();
        try {
            final int affectedRows = stmt.executeUpdateDelete();
            db.commit();
            return affectedRows;
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    private void bindValues(RxSQLiteStmt stmt, Iterable<Object> values) {
        int index = 0;
        for (final Object value : values) {
            bindValue(stmt, ++index, value);
        }
    }

}
