package rxsqlite;

import java.util.HashMap;
import java.util.Map;

import rxsqlite.model.Foo;
import rxsqlite.model.Foo$$Table;
import rxsqlite.model.bar.Bar;
import rxsqlite.model.bar.Bar$$Table;

public class SQLite$$Schema {

    public static final Map<Class, RxSQLiteTable> TABLES = new HashMap<>();

    static {
        TABLES.put(Foo.class, Foo$$Table.INSTANCE);
        TABLES.put(Bar.class, Bar$$Table.INSTANCE);
    }

}