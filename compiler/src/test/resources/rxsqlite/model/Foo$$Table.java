// Generated code from RxSQLite. Do not modify!
package rxsqlite.model;

import java.util.ArrayList;
import java.util.Collections;
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
        db.exec("CREATE TABLE IF NOT EXISTS foo_bar_rel(foo_id INTEGER, bar_id INTEGER, FOREIGN KEY(foo_id) REFERENCES foo(_id) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(bar_id) REFERENCES bar(_id) ON DELETE CASCADE ON UPDATE CASCADE);");
        db.exec("CREATE INDEX IF NOT EXISTS foo_bar_rel_foo_idx ON foo_bar_rel(foo_id);");
        db.exec("CREATE TRIGGER IF NOT EXISTS delete_bar_after_foo AFTER DELETE ON foo_bar_rel FOR EACH ROW BEGIN DELETE FROM bar WHERE _id = OLD.bar_id; END;");
        db.exec("CREATE TABLE IF NOT EXISTS foo_baz_rel(foo_id INTEGER, baz_id INTEGER, FOREIGN KEY(foo_id) REFERENCES foo(_id) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(baz_id) REFERENCES baz(_id) ON DELETE CASCADE ON UPDATE CASCADE);");
        db.exec("CREATE INDEX IF NOT EXISTS foo_baz_rel_foo_idx ON foo_baz_rel(foo_id);");
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
        blockingSave(db, objects);
        return Observable.from(objects);
    }

    @Override
    public List<Long> blockingSave(SQLiteDb db, Iterable<Foo> objects) {
        final SQLiteStmt stmt = db.prepare("INSERT INTO foo(_id, column_string) VALUES(?, ?);");
        try {
            final List<Long> rowIds = new ArrayList<>();
            for (final Foo object : objects) {
                stmt.clearBindings();
                bindStmtValues(stmt, object);
                object.mColumnLong = stmt.executeInsert();
                rowIds.add(object.mColumnLong);
                saveBarRelation(db, object.mBar, object.mColumnLong);
                saveBazRelation(db, object.mBazs, object.mColumnLong);
            }
            return rowIds;
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
        object.mBar = queryBarRelation(db, object.mColumnLong);
        object.mBazs = queryBazRelation(db, object.mColumnLong);
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

    private void saveBarRelation(SQLiteDb db, Bar rel, long pk) {
        if (rel == null) {
            return;
        }
        final List<Long> relIds = new Bar$$Table(mTypes).blockingSave(db, Collections.singletonList(rel));
        if (!relIds.isEmpty()) {
            final SQLiteStmt stmt = db.prepare("INSERT INTO foo_bar_rel(foo_id, bar_id) VALUES(?, ?);");
            try {
                stmt.bindLong(1, pk);
                stmt.bindLong(2, relIds.get(0));
                stmt.executeInsert();
            } finally {
                stmt.close();
            }
        }
    }

    private Bar queryBarRelation(SQLiteDb db, long pk) {
        final SQLiteStmt stmt = db.prepare("SELECT bar.* FROM bar, foo_bar_rel WHERE bar._id=foo_bar_rel.bar_id AND foo_bar_rel.foo_id = ?;");
        try {
            stmt.bindLong(1, pk);
            final SQLiteCursor cursor = stmt.executeQuery();
            if (cursor.step()) {
                return new Bar$$Table(mTypes).instantiate(db, cursor);
            }
        } finally {
            stmt.close();
        }
        return null;
    }

    private void saveBazRelation(SQLiteDb db, Iterable<Baz> rel, long pk) {
        if (rel == null) {
            return;
        }
        final List<Long> relIds = new Baz$$Table(mTypes).blockingSave(db, rel);
        if (!relIds.isEmpty()) {
            final SQLiteStmt stmt = db.prepare("INSERT INTO foo_baz_rel(foo_id, baz_id) VALUES(?, ?);");
            try {
                for (final Long relId : relIds) {
                    stmt.clearBindings();
                    stmt.bindLong(1, pk);
                    stmt.bindLong(2, relId);
                    stmt.executeInsert();
                }
            } finally {
                stmt.close();
            }
        }
    }

    private List<Baz> queryBazRelation(SQLiteDb db, long pk) {
        final List<Baz> objects = new ArrayList<>();
        final SQLiteStmt stmt = db.prepare("SELECT baz.* FROM baz, foo_baz_rel WHERE baz._id=foo_baz_rel.baz_id AND foo_baz_rel.foo_id = ?;");
        try {
            stmt.bindLong(1, pk);
            final SQLiteCursor cursor = stmt.executeQuery();
            final Baz$$Table table = new Baz$$Table(mTypes);
            while (cursor.step()) {
                objects.add(table.instantiate(db, cursor));
            }
        } finally {
            stmt.close();
        }
        return objects;
    }

}