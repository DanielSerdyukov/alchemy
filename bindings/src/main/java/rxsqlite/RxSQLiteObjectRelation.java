package rxsqlite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class RxSQLiteObjectRelation<T> implements RxSQLiteRelation<T> {

    private final String mLTableName;

    private final RxSQLiteTable<T> mRTable;

    private final boolean mCascade;

    private final String mRelTableName;

    public RxSQLiteObjectRelation(String lTableName, String field, RxSQLiteTable<T> rTable, boolean cascade) {
        mLTableName = lTableName;
        mRTable = rTable;
        mCascade = cascade;
        mRelTableName = lTableName + "_" + field;
    }

    @Override
    public void create(RxSQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS " + mRelTableName + " (" +
                "lfk INTEGER REFERENCES " + mLTableName + "(_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "rfk INTEGER, " +
                "UNIQUE(lfk, rfk) ON CONFLICT IGNORE);");
        if (mCascade) {
            db.exec("CREATE TRIGGER IF NOT EXISTS delete_" + mRelTableName +
                    " BEFORE DELETE ON " + mRelTableName +
                    " FOR EACH ROW BEGIN" +
                    " DELETE FROM " + mRTable.getName() + " WHERE " + mRTable.getName() + "._id = OLD.rfk;" +
                    " END;");
        }
    }

    @Override
    public void insert(RxSQLiteDb db, List<T> items, long fk) {
        final long[] rowIds = mRTable.insert(db, items);
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO " + mRelTableName + " VALUES(" + fk + ", ?);");
        try {
            for (final long rowId : rowIds) {
                stmt.bindLong(1, rowId);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    @Override
    public List<T> selectList(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT r.* FROM " + mRTable.getName() + " AS r, " +
                mRelTableName + " AS rel WHERE r._id = rel.rfk AND rel.lfk = " + fk + ";");
        try {
            return toList(db, stmt.executeSelect());
        } finally {
            stmt.close();
        }
    }

    @Override
    public T selectOne(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT r.* FROM " + mRTable.getName() + " AS r, " +
                mRelTableName + " AS rel WHERE r._id = rel.rfk AND rel.lfk = " + fk + " LIMIT 1;");
        try {
            final List<T> list = toList(db, stmt.executeSelect());
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } finally {
            stmt.close();
        }
    }

    private List<T> toList(RxSQLiteDb db, RxSQLiteCursor cursor) {
        final List<T> result = new ArrayList<>(RxSQLiteTable.DEFAULT_CAPACITY);
        while (cursor.step()) {
            final T object = mRTable.instantiate(cursor);
            mRTable.doOnInstantiate(db, object);
            result.add(object);
        }
        return result;
    }

}
