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

public class Baz$$Table implements RxSQLiteTable<Baz> {
    private final CustomTypes mTypes;

    public Baz$$Table(CustomTypes types) {
        mTypes = types;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS baz("
                + "_id INTEGER PRIMARY KEY ON CONFLICT REPLACE"
                + ", column_string TEXT"
                + ");");
    }

    @Override
    public Observable<Baz> query(SQLiteDb db, String selection, Iterable<Object> bindValues) {
        final SQLiteStmt stmt = db.prepare("SELECT * FROM baz" + selection);
        try {
            final List<Baz> objects = new ArrayList<>();
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
    public Observable<Baz> save(SQLiteDb db, Iterable<Baz> objects) {
        blockingSave(db, objects);
        return Observable.from(objects);
    }

    @Override
    public List<Long> blockingSave(SQLiteDb db, Iterable<Baz> objects) {
        final SQLiteStmt stmt = db.prepare("INSERT INTO baz(_id, column_string) VALUES(?, ?);");
        try {
            final List<Long> rowIds = new ArrayList<>();
            for (final Baz object : objects) {
                stmt.clearBindings();
                bindStmtValues(stmt, object);
                object.mColumnLong = stmt.executeInsert();
                rowIds.add(object.mColumnLong);
            }
            return rowIds;
        } finally {
            stmt.close();
        }
    }

    @Override
    public Observable<Integer> remove(SQLiteDb db, Iterable<Baz> objects) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM baz WHERE _id = ?;");
        try {
            int affectedRows = 0;
            for (final Baz object : objects) {
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
        final SQLiteStmt stmt = db.prepare("DELETE FROM baz" + selection);
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
    public Baz instantiate(SQLiteDb db, SQLiteCursor cursor) {
        final Baz object = new Baz();
        object.mColumnLong = (long) cursor.getColumnLong(0);
        object.mColumnString = cursor.getColumnString(1);
        return object;
    }

    private void bindStmtValues(SQLiteStmt stmt, Baz object) {
        if (object.mColumnLong > 0) {
            stmt.bindLong(1, object.mColumnLong);
        } else {
            stmt.bindNull(1);
        }
        stmt.bindString(2, object.mColumnString);
    }
}