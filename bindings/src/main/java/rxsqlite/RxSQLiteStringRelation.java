package rxsqlite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class RxSQLiteStringRelation implements RxSQLiteRelation<String> {

    private final String mLTableName;

    private final boolean mCascade;

    private final String mRelTableName;

    public RxSQLiteStringRelation(String lTable, String field, boolean cascade) {
        mLTableName = lTable;
        mCascade = cascade;
        mRelTableName = lTable + "_" + field;
    }

    @Override
    public void create(RxSQLiteDb db) {
        final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(mRelTableName)
                .append(" (fk INTEGER REFERENCES ")
                .append(mLTableName).append("(_id)");
        if (mCascade) {
            sql.append(" ON DELETE CASCADE");
        }
        sql.append(" ON UPDATE CASCADE, value TEXT);");
        db.exec(sql.toString());
        db.exec("CREATE INDEX IF NOT EXISTS idx_" + mRelTableName + "_fk ON " + mRelTableName + "(fk);");
    }

    @Override
    public void insert(RxSQLiteDb db, List<String> values, long fk) {
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO " + mRelTableName + " VALUES(" + fk + ", ?);");
        try {
            for (final String value : values) {
                stmt.bindString(1, value);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    @Override
    public List<String> selectList(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT value FROM " + mRelTableName + " WHERE fk = " + fk + ";");
        try {
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<String> stringList = new ArrayList<>();
            while (cursor.step()) {
                stringList.add(cursor.getColumnString(0));
            }
            return stringList;
        } finally {
            stmt.close();
        }
    }

    @Override
    public String selectOne(RxSQLiteDb db, long fk) {
        throw new UnsupportedOperationException("String list relation not support single value select");
    }

}
