package rxsqlite.model.bar;

import java.util.ArrayList;
import java.util.List;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;
import rxsqlite.model.baz.Baz;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("bar")
public class Bar {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private String mUuid;

    @SQLiteRelation
    private Baz mBaz;

    @SQLiteRelation
    private List<Baz> mBazList = new ArrayList<>();

    @SQLiteRelation
    private List<String> mStrings = new ArrayList<>();

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public void setBaz(Baz baz) {
        mBaz = baz;
    }

    public void addBaz(Baz baz) {
        mBazList.add(baz);
    }

    public void addString(String value) {
        mStrings.add(value);
    }

}
