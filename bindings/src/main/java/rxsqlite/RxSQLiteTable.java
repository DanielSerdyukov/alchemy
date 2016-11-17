package rxsqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public abstract class RxSQLiteTable<T> {

    static final int DEFAULT_CAPACITY = 128;

    private final Map<String, RxSQLiteRelation<?>> mRelations = new HashMap<>();

    private final String mName;

    private final String[] mColumns;

    protected RxSQLiteTable(String name, String[] columns) {
        mName = name;
        mColumns = columns;
    }

    protected String getName() {
        return mName;
    }

    protected String[] getColumns() {
        return mColumns;
    }

    protected abstract void create(RxSQLiteDb db);

    protected void doOnCreate(RxSQLiteDb db) {
        for (final RxSQLiteRelation<?> relation : mRelations.values()) {
            relation.create(db);
        }
    }

    protected void putRelation(String fieldName, RxSQLiteRelation<?> relation) {
        mRelations.put(fieldName, relation);
    }

    protected boolean hasRelations() {
        return !mRelations.isEmpty();
    }

    @SuppressWarnings("unchecked")
    protected <V> RxSQLiteRelation<V> findRelation(String fieldName) {
        final RxSQLiteRelation<?> relation = mRelations.get(fieldName);
        if (relation == null) {
            throw new RxSQLiteException("No such relation for field '" + fieldName + "'");
        }
        return (RxSQLiteRelation<V>) relation;
    }

    protected long[] insert(RxSQLiteDb db, List<T> items) {
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO " + getName() +
                " VALUES(" + Strings.joinNCopies("?", ", ", getColumns().length) + ");");
        try {
            final long[] rowIds = new long[items.size()];
            int index = 0;
            for (final T item : items) {
                bindObject(stmt, item);
                final long rowId = stmt.executeInsert();
                setObjectId(item, rowId);
                doOnInsert(db, item, rowId);
                stmt.clearBindings();
                rowIds[index++] = rowId;
            }
            return rowIds;
        } finally {
            stmt.close();
        }
    }

    protected Iterable<T> select(RxSQLiteDb db, long[] rowIds) {
        final StringBuilder where = new StringBuilder(" WHERE _id IN(");
        Strings.join(where, rowIds, ", ");
        where.append(");");
        return select(db, where.toString(), Collections.emptyList());
    }

    protected Iterable<T> select(RxSQLiteDb db, String where, Iterable<Object> args) {
        final RxSQLiteStmt stmt = db.prepare("SELECT * FROM " + getName() + where);
        try {
            int index = 0;
            for (final Object arg : args) {
                RxSQLiteTypes.bindValue(stmt, ++index, arg);
            }
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<T> result = new ArrayList<>(DEFAULT_CAPACITY);
            while (cursor.step()) {
                final T object = instantiate(cursor);
                doOnInstantiate(db, object);
                result.add(object);
            }
            return result;
        } finally {
            stmt.close();
        }
    }

    protected int delete(RxSQLiteDb db, List<T> items) {
        final StringBuilder where = new StringBuilder(" WHERE _id IN(");
        final Iterator<T> iterator = items.iterator();
        while (iterator.hasNext()) {
            where.append(getObjectId(iterator.next()));
            if (iterator.hasNext()) {
                where.append(", ");
            }
        }
        where.append(");");
        return delete(db, where.toString(), Collections.emptyList());
    }

    protected int delete(RxSQLiteDb db, String where, Iterable<Object> args) {
        final RxSQLiteStmt stmt = db.prepare("DELETE FROM " + getName() + where);
        try {
            int index = 0;
            for (final Object arg : args) {
                RxSQLiteTypes.bindValue(stmt, ++index, arg);
            }
            return stmt.executeUpdateDelete();
        } finally {
            stmt.close();
        }
    }

    protected abstract long getObjectId(T object);

    protected abstract void setObjectId(T object, long id);

    protected abstract void bindObject(RxSQLiteStmt stmt, T object);

    protected void doOnInsert(RxSQLiteDb db, T object, long id) {

    }

    protected abstract T instantiate(RxSQLiteCursor cursor);

    protected void doOnInstantiate(RxSQLiteDb db, T object) {

    }

}
