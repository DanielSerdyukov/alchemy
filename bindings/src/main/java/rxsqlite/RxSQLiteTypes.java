package rxsqlite;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLiteTypes {

    private static final ConcurrentMap<Class<?>, Adapter> TYPES = new ConcurrentHashMap<>();

    public static void register(Class<?> clazz, Adapter type) {
        TYPES.put(clazz, type);
    }

    public static void bindValue(RxSQLiteStmt stmt, int index, Object value) {
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
            findCustomType(value.getClass()).bindValue(stmt, index, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static Object getValue(RxSQLiteCursor cursor, int index, Class type) {
        if (type.isEnum()) {
            final String value = cursor.getColumnString(index);
            if (value != null && !value.isEmpty()) {
                return Enum.valueOf(type, value);
            }
            return null;
        }
        return findCustomType(type).getValue(cursor, index);
    }

    static void clear() {
        TYPES.clear();
    }

    private static Adapter findCustomType(Class<?> clazz) {
        final Adapter type = TYPES.get(clazz);
        if (type != null) {
            return type;
        }
        for (final Map.Entry<Class<?>, Adapter> entry : TYPES.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Unsupported type " + clazz);
    }

    public interface Adapter {

        void bindValue(RxSQLiteStmt stmt, int index, Object value);

        Object getValue(RxSQLiteCursor cursor, int index);

    }

}
