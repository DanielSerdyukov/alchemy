package rxsqlite.model;

import java.util.Date;
import java.util.List;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteStringList;

@SQLiteObject("all_types")
public class AllTypes {

    @SQLitePk
    private long mColumnLong;

    @SQLiteColumn(index = true)
    private int mColumnInt;

    @SQLiteColumn(index = true, unique = true)
    private short mColumnShort;

    @SQLiteColumn
    private double mColumnDouble;

    @SQLiteColumn
    private boolean mColumnBool;

    @SQLiteColumn("my_float")
    private float mColumnFloat;

    @SQLiteColumn
    private String mColumnString;

    @SQLiteColumn
    private byte[] mColumnBytes;

    @SQLiteColumn(constraint = "UNIQUE")
    private Date mColumnDate;

    @SQLiteColumn
    private EnumType mColumnEnum = EnumType.TEST;

    @SQLiteStringList
    private List<String> mStringList;

    enum EnumType {
        TEST
    }

}
