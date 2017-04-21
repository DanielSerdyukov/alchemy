// Generated code from Alchemy. Do not modify!
package alchemy.sqlite;

import alchemy.AlchemyException;
import alchemy.sqlite.models.groups.Group;
import alchemy.sqlite.models.users.User;
import alchemy.sqlite.platform.SQLiteSchema;
import alchemy.sqlite.platform.SQLiteTable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class DefaultSchema implements SQLiteSchema {
    private static final Map<Class<?>, SQLiteTable<?>> TABLES = new HashMap<>();

    static {
        TABLES.put(Group.class, new Group_Table());
        TABLES.put(User.class, new User_Table());
    }

    private final int mVersion;

    public DefaultSchema(int version) {
        mVersion = version;
    }

    @Override
    public int getVersion() {
        return mVersion;
    }

    @Override
    public <T> SQLiteTable<T> getTable(Class<T> clazz) {
        final SQLiteTable<?> table = TABLES.get(clazz);
        if (table != null) {
            return (SQLiteTable<T>) table;
        }
        throw new AlchemyException("No such table for " + clazz.getCanonicalName());
    }

    @Override
    public Collection<SQLiteTable<?>> getAllTables() {
        return TABLES.values();
    }
}