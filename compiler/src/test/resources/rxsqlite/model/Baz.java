package rxsqlite.model;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("baz")
public class Baz {

    @SQLitePk
    private long mColumnLong;

    @SQLiteColumn
    private String mColumnString;

}
