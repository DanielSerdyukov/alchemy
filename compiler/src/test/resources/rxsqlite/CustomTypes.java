// Generated code from RxSQLite. Do not modify!
package rxsqlite;

import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteStmt;

public class CustomTypes {
    private final Types mTypes;

    CustomTypes(Types types) {
        mTypes = types;
    }

    public String getType(Class type) {
        return mTypes.getType(type);
    }

    public void bindValue(SQLiteStmt stmt, int index, Object object) {
        mTypes.bindValue(stmt, index, object);
    }

    public <T> T getValue(SQLiteCursor cursor, int index, Class<T> type) {
        return mTypes.getValue(cursor, index, type);
    }

    public <T extends Enum<T>> T getEnumValue(SQLiteCursor cursor, int index, Class<T> type) {
        return mTypes.getEnumValue(cursor, index, type);
    }
}