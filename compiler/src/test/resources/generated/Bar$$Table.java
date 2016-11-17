package rxsqlite.model.bar;

import rxsqlite.RxSQLiteCursor;
import rxsqlite.RxSQLiteDb;
import rxsqlite.RxSQLiteStmt;
import rxsqlite.RxSQLiteTable;

public class Bar$$Table extends RxSQLiteTable<Bar> {

    public static final Bar$$Table INSTANCE = new Bar$$Table();

    private Foo$$Table() {
        super("bar", new String[]{"_id"});
    }

    @Override
    protected void create(RxSQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS bar (_id INTEGER PRIMARY KEY ON CONFLICT REPLACE);");
    }

    @Override
    protected long getObjectId(Bar object) {
        return object.mId;
    }

    @Override
    protected void setObjectId(Bar object, long id) {
        object.mId = id;
    }

    @Override
    protected void bindObject(RxSQLiteStmt stmt, Bar object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        }
    }

    @Override
    protected Bar instantiate(RxSQLiteCursor cursor) {
        final Bar object = new Bar();
        object.mId = (long) cursor.getColumnLong(0);
        return object;
    }

}
