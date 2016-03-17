package rxsqlite.model;

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

    @SQLiteRelation
    private Bar mBar;

    /*@SQLiteRelation
    private List<Baz> mBazs;*/

}
