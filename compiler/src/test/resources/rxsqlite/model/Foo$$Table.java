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

public class Foo$$Table implements RxSQLiteTable<Foo> {
    private final CustomTypes mTypes;

    public Foo$$Table(CustomTypes types) {
        mTypes = types;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS foo("
                + "_id INTEGER PRIMARY KEY ON CONFLICT REPLACE"
                + ", column_string TEXT"
                + ");");
        db.exec("CREATE TABLE IF NOT EXISTS foo_bar_rel(foo_id INTEGER, bar_id INTEGER, FOREIGN KEY(foo_id) REFERENCES foo(_id), FOREIGN KEY(bar_id) REFERENCES bar(_id));");
    }

    @Override
    public Observable<Foo> query(SQLiteDb db, String selection, Iterable<Object> bindValues) {
        final SQLiteStmt stmt = db.prepare("SELECT * FROM foo" + selection);
        try {
            final List<Foo> objects = new ArrayList<>();
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
    public Observable<Foo> save(SQLiteDb db, Iterable<Foo> objects) {
        final SQLiteStmt stmt = db.prepare("INSERT INTO foo(_id, column_string) VALUES(?, ?);");
        try {
            for (final Foo object : objects) {
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
    public Observable<Integer> remove(SQLiteDb db, Iterable<Foo> objects) {
        final SQLiteStmt stmt = db.prepare("DELETE FROM foo WHERE _id = ?;");
        try {
            int affectedRows = 0;
            for (final Foo object : objects) {
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
        final SQLiteStmt stmt = db.prepare("DELETE FROM foo" + selection);
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
    public Foo instantiate(SQLiteDb db, SQLiteCursor cursor) {
        final Foo object = new Foo();
        object.mColumnLong = (long) cursor.getColumnLong(0);
        object.mColumnString = cursor.getColumnString(1);
        return object;
    }

    private void bindStmtValues(SQLiteStmt stmt, Foo object) {
        if (object.mColumnLong > 0) {
            stmt.bindLong(1, object.mColumnLong);
        } else {
            stmt.bindNull(1);
        }
        stmt.bindString(2, object.mColumnString);
    }

}