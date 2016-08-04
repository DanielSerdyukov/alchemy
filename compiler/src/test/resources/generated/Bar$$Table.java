package rxsqlite.model.bar;

import rxsqlite.AbstractTable;
import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteStmt;

public class Bar$$Table extends AbstractTable<Bar> {

    public Bar$$Table() {
        super("bar", new String[]{"_id", "c_string"}, false);
    }

    @Override
    public Bar instantiate(RxSQLiteDb db, RxSQLiteCursor cursor) {
        final Bar object = new Bar();
        object.mId = (long) cursor.getColumnLong(0);
        object.mCString = cursor.getColumnString(1);
        return object;
    }

    @Override
    protected void create(RxSQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS bar(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, c_string TEXT);");
    }

    @Override
    protected long getId(Bar object) {
        return object.mId;
    }

    @Override
    protected void bind(RxSQLiteStmt stmt, Bar object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        }
        stmt.bindString(2, object.mCString);
    }

}