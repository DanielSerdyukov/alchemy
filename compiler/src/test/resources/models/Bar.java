package rxsqlite.model.bar;

import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

@SQLiteObject("bar")
public class Bar {

    @SQLitePk
    private long mId;

}
