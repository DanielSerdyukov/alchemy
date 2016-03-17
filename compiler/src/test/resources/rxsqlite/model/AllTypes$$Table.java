// Generated code from RxSQLite. Do not modify!
package rxsqlite.model;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rxsqlite.CustomTypes;
import rxsqlite.RxSQLiteTable;
import sqlite4a.SQLiteCursor;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteStmt;

public class AllTypes$$Table implements RxSQLiteTable<AllTypes> {
    private final CustomTypes mTypes;

    public AllTypes$$Table(CustomTypes types) {
        mTypes = types;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS all_types("
                + "_id INTEGER PRIMARY KEY ON CONFLICT REPLACE"
                + ", column_int INTEGER"
                + ", column_short INTEGER"
                + ", column_double REAL"
                + ", column_bool INTEGER"
                + ", my_float REAL"
                + ", column_string TEXT"
                + ", column_bytes BLOB"
                + ", column_date " + mTypes.getType(java.util.Date.class) + " UNIQUE"
                + ", column_enum TEXT"
                + ");");
        db.exec("CREATE INDEX IF NOT EXISTS all_types_idx0 ON all_types(column_int);");
        db.exec("CREATE UNIQUE INDEX IF NOT EXISTS all_types_idx1 ON all_types(column_short);");
    }

    @Override
    public Observable<AllTypes> query(SQLiteDb db, String selection, Iterable<Object> bindValues) {
        final SQLiteStmt stmt = db.prepare("SELECT * FROM all_types" + selection);
        try {
            final List<AllTypes> objects = new ArrayList<>();
            int index = 0;
            for (final Object value : bindValues) {
                mTypes.bindValue(stmt, ++index, value);
            }
            final SQLiteCursor cursor = stmt.executeQuery();
            while (cursor.step()) {
                objects.add(instantiate(db, cursor));
            }
            return Observable.from(objects);
        } finally {
            stmt.close();
        }
    }

    @Override
    public Observable<AllTypes> save(SQLiteDb db, Iterable<AllTypes> objects) {
        final SQLiteStmt stmt = db.prepare("INSERT INTO all_types(_id, column_int, column_short, column_double, column_bool, my_float, column_string, column_bytes, column_date, column_enum) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        try {
            for (final AllTypes object : objects) {
                stmt.clearBindings();
                bindStmtValues(stmt, object);
                object.mColumnLong = stmt.executeInsert();
            }
            return Observable.from(objects);
        } finally {
            stmt.close();
        }
    }

    @Override
    public Observable<Integer> remove(SQLiteDb db, Iterable<AllTypes> objects) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM all_types WHERE _id = ?;");
        try {
            int affectedRows = 0;
            for (final AllTypes object : objects) {
                stmt.clearBindings();
                stmt.bindLong(1, object.mColumnLong);
                affectedRows += stmt.executeUpdateDelete();
            }
            return Observable.just(affectedRows);
        } finally {
            stmt.close();
        }
    }

    @Override
    public Observable<Integer> clear(SQLiteDb db, String selection, Iterable<Object> bindValues) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM all_types" + selection);
        try {
            int index = 0;
            for (final Object value : bindValues) {
                mTypes.bindValue(stmt, ++index, value);
            }
            return Observable.just(stmt.executeUpdateDelete());
        } finally {
            stmt.close();
        }
    }

    @Override
    public AllTypes instantiate(SQLiteDb db, SQLiteCursor cursor) {
        final AllTypes object = new AllTypes();
        object.mColumnLong = (long) cursor.getColumnLong(0);
        object.mColumnInt = (int) cursor.getColumnLong(1);
        object.mColumnShort = (short) cursor.getColumnLong(2);
        object.mColumnDouble = (double) cursor.getColumnDouble(3);
        object.mColumnBool = cursor.getColumnLong(4) > 0;
        object.mColumnFloat = (float) cursor.getColumnDouble(5);
        object.mColumnString = cursor.getColumnString(6);
        object.mColumnBytes = cursor.getColumnBlob(7);
        object.mColumnDate = mTypes.getValue(cursor, 8, java.util.Date.class);
        object.mColumnEnum = mTypes.getEnumValue(cursor, 9, rxsqlite.model.AllTypes.EnumType.class);
        return object;
    }

    private void bindStmtValues(SQLiteStmt stmt, AllTypes object) {
        if (object.mColumnLong > 0) {
            stmt.bindLong(1, object.mColumnLong);
        } else {
            stmt.bindNull(1);
        }
        stmt.bindLong(2, object.mColumnInt);
        stmt.bindLong(3, object.mColumnShort);
        stmt.bindDouble(4, object.mColumnDouble);
        stmt.bindLong(5, object.mColumnBool ? 1 : 0);
        stmt.bindDouble(6, object.mColumnFloat);
        stmt.bindString(7, object.mColumnString);
        stmt.bindBlob(8, object.mColumnBytes);
        mTypes.bindValue(stmt, 9, object.mColumnDate);
        if (object.mColumnEnum != null) {
            stmt.bindString(10, object.mColumnEnum.name());
        } else {
            stmt.bindNull(10);
        }
    }
}