package rxsqlite.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import rxsqlite.RxSQLiteCursor;
import rxsqlite.RxSQLiteDb;
import rxsqlite.RxSQLiteObjectRelation;
import rxsqlite.RxSQLiteStmt;
import rxsqlite.RxSQLiteStringRelation;
import rxsqlite.RxSQLiteTable;
import rxsqlite.RxSQLiteTypes;
import rxsqlite.model.bar.Bar;
import rxsqlite.model.bar.Bar$$Table;

public class Foo$$Table extends RxSQLiteTable<Foo> {

    public static final Foo$$Table INSTANCE = new Foo$$Table();

    private Foo$$Table() {
        super("foo", new String[]{"_id", "long", "int_val", "short", "double", "float", "bytes", "string", "date", "big_dec"});
        putRelation("mBar", new RxSQLiteObjectRelation<>("foo", "mBar", Bar$$Table.INSTANCE, true));
        putRelation("mBars", new RxSQLiteObjectRelation<>("foo", "mBars", Bar$$Table.INSTANCE, false));
        putRelation("mStrings", new RxSQLiteStringRelation("foo", "mStrings", true));
    }

    @Override
    protected void create(RxSQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS foo (_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, long INTEGER, int_val INTEGER, short INTEGER UNIQUE, double REAL, float REAL, bytes BLOB, string TEXT, date INTEGER, big_dec TEXT);");
    }

    @Override
    protected long getObjectId(Foo object) {
        return object.mId;
    }

    @Override
    protected void setObjectId(Foo object, long id) {
        object.mId = id;
    }

    @Override
    protected void bindObject(RxSQLiteStmt stmt, Foo object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        }
        if (object.mLong != null) {
            stmt.bindLong(2, object.mLong);
        }
        stmt.bindLong(3, object.mInt);
        stmt.bindLong(4, object.mShort);
        stmt.bindDouble(5, object.mDouble);
        stmt.bindDouble(6, object.mFloat);
        if (object.mBytes != null) {
            stmt.bindBlob(7, object.mBytes);
        }
        if (object.mString != null) {
            stmt.bindString(8, object.mString);
        }
        if (object.mDate != null) {
            stmt.bindLong(9, object.mDate.getTime());
        }
        if (object.mBigDec != null) {
            RxSQLiteTypes.bindValue(stmt, 10, object.mBigDec);
        }
    }

    @Override
    protected Foo instantiate(RxSQLiteCursor cursor) {
        final Foo object = new Foo();
        object.mId = (long) cursor.getColumnLong(0);
        object.mLong = (Long) cursor.getColumnLong(1);
        object.mInt = (int) cursor.getColumnLong(2);
        object.mShort = (short) cursor.getColumnLong(3);
        object.mDouble = (double) cursor.getColumnDouble(4);
        object.mFloat = (float) cursor.getColumnDouble(5);
        object.mBytes = cursor.getColumnBlob(6);
        object.mString = cursor.getColumnString(7);
        object.mDate = new Date(cursor.getColumnLong(8));
        object.mBigDec = (BigDecimal) RxSQLiteTypes.getValue(cursor, 9, BigDecimal.class);
        return object;
    }

    @Override
    protected void doOnInsert(RxSQLiteDb db, Foo object, long id) {
        super.doOnInsert(db, object, id);
        if (object.mBar != null) {
            this.<Bar>findRelation("mBar").insert(db, Collections.singletonList(object.mBar), id);
        }
        if (object.mBars != null && !object.mBars.isEmpty()) {
            this.<Bar>findRelation("mBars").insert(db, object.mBars, id);
        }
        if (object.mStrings != null && !object.mStrings.isEmpty()) {
            this.<String>findRelation("mStrings").insert(db, object.mStrings, id);
        }
    }

    @Override
    protected void doOnInstantiate(RxSQLiteDb db, Foo object) {
        super.doOnInstantiate(db, object);
        object.mBar = this.<Bar>findRelation("mBar").selectOne(db, object.mId);
        object.mBars = this.<Bar>findRelation("mBars").selectList(db, object.mId);
        object.mStrings = this.<String>findRelation("mStrings").selectList(db, object.mId);
    }

}
