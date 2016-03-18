package rxsqlite.model;

import java.util.List;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;

@SQLiteObject("foo")
public class Foo {

    @SQLitePk
    private long mColumnLong;

    @SQLiteColumn
    private String mColumnString;

    @SQLiteRelation(onDeleteCascade = true)
    private Bar mBar;

    @SQLiteRelation
    private List<Baz> mBazs;

}
