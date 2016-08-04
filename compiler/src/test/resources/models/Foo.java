package rxsqlite.models.foo;

import java.util.Date;
import java.util.List;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;
import rxsqlite.annotation.SQLiteStringList;
import rxsqlite.model.bar.Bar;

@SQLiteObject(value = "foo", constraints = "UNIQUE(key_1, key_2)")
public class Foo {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private double mCDouble;

    @SQLiteColumn(constraint = "UNIQUE ON CONFLICT FAIL")
    private String mUuid;

    @SQLiteColumn
    private boolean mCBool;

    @SQLiteColumn
    private byte[] mCBlob;

    @SQLiteColumn(type = "INTEGER")
    private Date mDate;

    @SQLiteColumn(index = true)
    private float mCFloat;

    @SQLiteColumn("key_1")
    private String mKey1;

    @SQLiteColumn("key_2")
    private int mKey2;

    @SQLiteRelation(onDeleteCascade = false)
    private Bar mOneToOneRel1;

    @SQLiteRelation
    private Bar mOneToOneRel2;

    @SQLiteRelation
    private List<Bar> mOneToManyRel;

    @SQLiteRelation(onDeleteCascade = false)
    private List<String> mStringRel1;

    @SQLiteRelation
    private List<String> mStringRel2;

    @SQLiteStringList
    private List<String> mFallbackStrings;

}