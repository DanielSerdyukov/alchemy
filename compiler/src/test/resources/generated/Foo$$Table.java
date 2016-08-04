package rxsqlite.models.foo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import rxsqlite.AbstractTable;
import rxsqlite.SQLite$$Schema;
import rxsqlite.bindings.RxSQLiteCursor;
import rxsqlite.bindings.RxSQLiteDb;
import rxsqlite.bindings.RxSQLiteStmt;
import rxsqlite.model.bar.Bar;

public class Foo$$Table extends AbstractTable<Foo> {

    public Foo$$Table() {
        super("foo", new String[]{"_id", "c_double", "uuid", "c_bool", "c_blob", "date", "c_float", "key_1", "key_2"}, true);
    }

    @Override
    public Foo instantiate(RxSQLiteDb db, RxSQLiteCursor cursor) {
        final Foo object = new Foo();
        object.mId = (long) cursor.getColumnLong(0);
        object.mCDouble = (double) cursor.getColumnDouble(1);
        object.mUuid = cursor.getColumnString(2);
        object.mCBool = cursor.getColumnLong(3) > 0;
        object.mCBlob = cursor.getColumnBlob(4);
        object.mDate = (Date) getValue(cursor, 5, Date.class);
        object.mCFloat = (float) cursor.getColumnDouble(6);
        object.mKey1 = cursor.getColumnString(7);
        object.mKey2 = (int) cursor.getColumnLong(8);
        object.mOneToOneRel1 = select_mOneToOneRel1(db, object.mId);
        object.mOneToOneRel2 = select_mOneToOneRel2(db, object.mId);
        object.mOneToManyRel = select_mOneToManyRel(db, object.mId);
        object.mStringRel1 = select_mStringRel1(db, object.mId);
        object.mStringRel2 = select_mStringRel2(db, object.mId);
        object.mFallbackStrings = select_mFallbackStrings(db, object.mId);
        return object;
    }

    @Override
    protected void create(RxSQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS foo(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, c_double REAL, uuid TEXT UNIQUE ON CONFLICT FAIL, c_bool INTEGER, c_blob BLOB, date INTEGER, c_float REAL, key_1 TEXT, key_2 INTEGER, UNIQUE(key_1, key_2));");
        db.exec("CREATE INDEX IF NOT EXISTS foo_c_float ON foo(c_float);");
        db.exec("CREATE TABLE IF NOT EXISTS foo_one_to_one_rel1(fk1 INTEGER, fk2 INTEGER, UNIQUE(fk1, fk2) ON CONFLICT NONE, FOREIGN KEY(fk1) REFERENCES foo(_id) ON DELETE CASCADE);");
        db.exec("CREATE TABLE IF NOT EXISTS foo_one_to_one_rel2(fk1 INTEGER, fk2 INTEGER, UNIQUE(fk1, fk2) ON CONFLICT NONE, FOREIGN KEY(fk1) REFERENCES foo(_id) ON DELETE CASCADE);");
        db.exec("CREATE TRIGGER IF NOT EXISTS delete_one_to_one_rel2 AFTER DELETE ON foo_one_to_one_rel2 FOR EACH ROW BEGIN DELETE FROM bar WHERE bar._id = OLD.fk2; END;");
        db.exec("CREATE TABLE IF NOT EXISTS foo_one_to_many_rel(fk1 INTEGER, fk2 INTEGER, UNIQUE(fk1, fk2) ON CONFLICT NONE, FOREIGN KEY(fk1) REFERENCES foo(_id) ON DELETE CASCADE);");
        db.exec("CREATE TRIGGER IF NOT EXISTS delete_one_to_many_rel AFTER DELETE ON foo_one_to_many_rel FOR EACH ROW BEGIN DELETE FROM bar WHERE bar._id = OLD.fk2; END;");
        db.exec("CREATE TABLE IF NOT EXISTS foo_string_rel1(fk INTEGER, value TEXT, FOREIGN KEY(fk) REFERENCES foo(_id) ON DELETE NO ACTION);");
        db.exec("CREATE TABLE IF NOT EXISTS foo_string_rel2(fk INTEGER, value TEXT, FOREIGN KEY(fk) REFERENCES foo(_id) ON DELETE CASCADE);");
        db.exec("CREATE TABLE IF NOT EXISTS foo_fallback_strings(fk INTEGER, value TEXT, FOREIGN KEY(fk) REFERENCES foo(_id) ON DELETE CASCADE);");
    }

    @Override
    protected long getId(Foo object) {
        return object.mId;
    }

    @Override
    protected void bind(RxSQLiteStmt stmt, Foo object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        }
        stmt.bindDouble(2, object.mCDouble);
        stmt.bindString(3, object.mUuid);
        stmt.bindLong(4, object.mCBool ? 1 : 0);
        stmt.bindBlob(5, object.mCBlob);
        bindValue(stmt, 6, object.mDate);
        stmt.bindDouble(7, object.mCFloat);
        stmt.bindString(8, object.mKey1);
        stmt.bindLong(9, object.mKey2);
    }

    @Override
    protected void doAfterInsert(RxSQLiteDb db, Foo object, long id) {
        insert_mOneToOneRel1(db, object.mOneToOneRel1, id);
        insert_mOneToOneRel2(db, object.mOneToOneRel2, id);
        insert_mOneToManyRel(db, object.mOneToManyRel, id);
        insert_mStringRel1(db, object.mStringRel1, id);
        insert_mStringRel2(db, object.mStringRel2, id);
        insert_mFallbackStrings(db, object.mFallbackStrings, id);
    }

    private void insert_mOneToOneRel1(RxSQLiteDb db, Bar rel, long fk) {
        final long[] rowIds = SQLite$$Schema.<Bar>findTable(Bar.class).insert(db, Collections.singletonList(rel));
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_one_to_one_rel1 VALUES(?, ?);");
        try {
            stmt.bindLong(1, fk);
            stmt.bindLong(2, rowIds[0]);
            stmt.executeInsert();
        } finally {
            stmt.close();
        }
    }

    private Bar select_mOneToOneRel1(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT m.* FROM bar AS m, foo_one_to_one_rel1 AS r WHERE m._id = r.fk2 AND r.fk1 = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            if (cursor.step()) {
                return SQLite$$Schema.<Bar>findTable(Bar.class).instantiate(db, cursor);
            }
            return null;
        } finally {
            stmt.close();
        }
    }

    private void insert_mOneToOneRel2(RxSQLiteDb db, Bar rel, long fk) {
        final long[] rowIds = SQLite$$Schema.<Bar>findTable(Bar.class).insert(db, Collections.singletonList(rel));
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_one_to_one_rel2 VALUES(?, ?);");
        try {
            stmt.bindLong(1, fk);
            stmt.bindLong(2, rowIds[0]);
            stmt.executeInsert();
        } finally {
            stmt.close();
        }
    }

    private Bar select_mOneToOneRel2(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT m.* FROM bar AS m, foo_one_to_one_rel2 AS r WHERE m._id = r.fk2 AND r.fk1 = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            if (cursor.step()) {
                return SQLite$$Schema.<Bar>findTable(Bar.class).instantiate(db, cursor);
            }
            return null;
        } finally {
            stmt.close();
        }
    }

    private void insert_mOneToManyRel(RxSQLiteDb db, List<Bar> rel, long fk) {
        final long[] rowIds = SQLite$$Schema.<Bar>findTable(Bar.class).insert(db, rel);
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_one_to_many_rel VALUES(?, ?);");
        try {
            for (final long rowId : rowIds) {
                stmt.bindLong(1, fk);
                stmt.bindLong(2, rowId);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    private List<Bar> select_mOneToManyRel(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT m.* FROM bar AS m, foo_one_to_many_rel AS r WHERE m._id = r.fk2 AND r.fk1 = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final AbstractTable<Bar> table = SQLite$$Schema.findTable(Bar.class);
            final List<Bar> objects = new ArrayList<>();
            while (cursor.step()) {
                objects.add(table.instantiate(db, cursor));
            }
            return objects;
        } finally {
            stmt.close();
        }
    }

    private void insert_mStringRel1(RxSQLiteDb db, List<String> values, long fk) {
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_string_rel1 VALUES(?, ?);");
        try {
            for (final String value : values) {
                stmt.bindLong(1, fk);
                stmt.bindString(2, value);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    private List<String> select_mStringRel1(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT value FROM foo_string_rel1 WHERE fk = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<String> values = new ArrayList<>();
            while (cursor.step()) {
                values.add(cursor.getColumnString(0));
            }
            return values;
        } finally {
            stmt.close();
        }
    }

    private void insert_mStringRel2(RxSQLiteDb db, List<String> values, long fk) {
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_string_rel2 VALUES(?, ?);");
        try {
            for (final String value : values) {
                stmt.bindLong(1, fk);
                stmt.bindString(2, value);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    private List<String> select_mStringRel2(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT value FROM foo_string_rel2 WHERE fk = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<String> values = new ArrayList<>();
            while (cursor.step()) {
                values.add(cursor.getColumnString(0));
            }
            return values;
        } finally {
            stmt.close();
        }
    }

    private void insert_mFallbackStrings(RxSQLiteDb db, List<String> values, long fk) {
        final RxSQLiteStmt stmt = db.prepare("INSERT INTO foo_fallback_strings VALUES(?, ?);");
        try {
            for (final String value : values) {
                stmt.bindLong(1, fk);
                stmt.bindString(2, value);
                stmt.executeInsert();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    private List<String> select_mFallbackStrings(RxSQLiteDb db, long fk) {
        final RxSQLiteStmt stmt = db.prepare("SELECT value FROM foo_fallback_strings WHERE fk = ?;");
        try {
            stmt.bindLong(1, fk);
            final RxSQLiteCursor cursor = stmt.executeSelect();
            final List<String> values = new ArrayList<>();
            while (cursor.step()) {
                values.add(cursor.getColumnString(0));
            }
            return values;
        } finally {
            stmt.close();
        }
    }

}