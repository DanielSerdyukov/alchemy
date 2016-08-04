package rxsqlite;

import java.util.HashMap;
import java.util.Map;

import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
class Types {

    private static final Map<Class<?>, SQLiteType> TYPES = new HashMap<>();

    static void create(Class<?> clazz, SQLiteType type) {
        TYPES.put(clazz, type);
    }

    static void bindValue(RxSQLiteStmt stmt, int index, Object value) {
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
        } else if (value instanceof Enum) {
            stmt.bindString(index, ((Enum) value).name());
        } else {
            findType(value.getClass()).bind(stmt, index, value);
        }
    }

    @SuppressWarnings("unchecked")
    static Object getValue(RxSQLiteCursor cursor, int index, Class clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            final String value = cursor.getColumnString(index);
            if (value != null && !value.isEmpty()) {
                return Enum.valueOf(clazz, value);
            }
            return null;
        }
        return findType(clazz).get(cursor, index);
    }

    static SQLiteType findType(Class<?> clazz) {
        final SQLiteType type = TYPES.get(clazz);
        if (type != null) {
            return type;
        }
        for (final Map.Entry<Class<?>, SQLiteType> entry : TYPES.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Unsupported type " + clazz);
    }

    static void clear() {
        TYPES.clear();
    }

}
