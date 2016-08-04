package rxsqlite;

import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteStmt;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteType {

    void bind(RxSQLiteStmt stmt, int index, Object value);

    Object get(RxSQLiteCursor cursor, int index);

}
