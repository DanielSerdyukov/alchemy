// Generated code from RxSQLite. Do not modify!
package rxsqlite;

import rxsqlite.model.AllTypes;
import rxsqlite.model.AllTypes$$Table;

public class SQLite$$Schema {
    public static void create(RxSQLiteClient client, Types types) {
        final CustomTypes customTypes = new CustomTypes(types);
        client.registerTable(AllTypes.class, new AllTypes$$Table(customTypes));
    }
}