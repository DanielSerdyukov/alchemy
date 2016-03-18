package rxsqlite.model;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

@SQLiteObject("bar")
public class Bar {

    @SQLitePk
    private long mColumnLong;

    @SQLiteColumn
    private String mColumnString;

}
