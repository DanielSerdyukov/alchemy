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

public class Bar$$Table implements RxSQLiteTable<Bar> {
    private final CustomTypes mTypes;

    public Bar$$Table(CustomTypes types) {
        mTypes = types;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS bar("
                + "_id INTEGER PRIMARY KEY ON CONFLICT REPLACE"
                + ", column_string TEXT"
                + ");");
    }

    @Override
    public Observable<Bar> query(SQLiteDb db, String selection, Iterable<Object> bindValues) {
        final SQLiteStmt stmt = db.prepare("SELECT * FROM bar" + selection);
        try {
            final List<Bar> objects = new ArrayList<>();
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
    public Observable<Bar> save(SQLiteDb db, Iterable<Bar> objects) {
        final SQLiteStmt stmt = db.prepare("INSERT INTO bar(_id, column_string) VALUES(?, ?);");
        try {
            for (final Bar object : objects) {
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
    public Observable<Integer> remove(SQLiteDb db, Iterable<Bar> objects) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM bar WHERE _id = ?;");
        try {
            int affectedRows = 0;
            for (final Bar object : objects) {
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
        final SQLiteStmt stmt = db.prepare("DELETE FROM bar" + selection);
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
    public Bar instantiate(SQLiteDb db, SQLiteCursor cursor) {
        final Bar object = new Bar();
        object.mColumnLong = (long) cursor.getColumnLong(0);
        object.mColumnString = cursor.getColumnString(1);
        return object;
    }

    private void bindStmtValues(SQLiteStmt stmt, Bar object) {
        if (object.mColumnLong > 0) {
            stmt.bindLong(1, object.mColumnLong);
        } else {
            stmt.bindNull(1);
        }
        stmt.bindString(2, object.mColumnString);
    }
}