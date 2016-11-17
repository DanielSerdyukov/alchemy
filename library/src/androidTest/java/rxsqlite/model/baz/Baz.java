package rxsqlite.model.baz;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("baz")
public class Baz {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private String mUuid;

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

}
