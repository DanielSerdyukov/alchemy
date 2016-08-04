package rxsqlite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import rxsqlite.model.bar.Bar;
import rxsqlite.model.bar.Bar$$Table;
import rxsqlite.models.foo.Foo;
import rxsqlite.models.foo.Foo$$Table;

public class SQLite$$Schema {

    private static final Map<Class<?>, SQLiteTable<?>> TABLES = new HashMap<>();

    public static Map<Class<?>, SQLiteTable<?>> init() {
        TABLES.put(Foo.class, new Foo$$Table());
        TABLES.put(Bar.class, new Bar$$Table());
        return Collections.unmodifiableMap(TABLES);
    }

    @SuppressWarnings("unchecked")
    public static <T> AbstractTable<T> findTable(Class<T> clazz) {
        final SQLiteTable<?> table = TABLES.get(clazz);
        if (table == null) {
            throw new IllegalArgumentException("No such table for " + clazz);
        }
        return (AbstractTable<T>) table;
    }

}