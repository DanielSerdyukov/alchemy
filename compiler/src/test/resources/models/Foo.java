package rxsqlite.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import rxsqlite.annotation.SQLiteColumn;
import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLitePk;
import rxsqlite.annotation.SQLiteRelation;
import rxsqlite.model.bar.Bar;

@SQLiteObject("foo")
public class Foo {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private Long mLong;

    @SQLiteColumn("int_val")
    private int mInt;

    @SQLiteColumn(constraints = "UNIQUE")
    private short mShort;

    @SQLiteColumn
    private double mDouble;

    @SQLiteColumn
    private float mFloat;

    @SQLiteColumn
    private byte[] mBytes;

    @SQLiteColumn
    private String mString;

    @SQLiteColumn
    private Date mDate;

    @SQLiteColumn
    private BigDecimal mBigDec;

    @SQLiteRelation
    private Bar mBar;

    @SQLiteRelation(cascade = false)
    private List<Bar> mBars;

    @SQLiteRelation
    private List<String> mStrings;

}
