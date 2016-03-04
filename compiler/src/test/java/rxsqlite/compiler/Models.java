package rxsqlite.compiler;

import com.google.testing.compile.JavaFileObjects;

import java.util.Arrays;

import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
public class Models {

    public static final JavaFileObject FOO = JavaFileObjects.forSourceLines("rxsqlite.test.Foo", Arrays.asList(
            "package rxsqlite.test;",
            "import java.util.Date;",
            "import rxsqlite.annotation.SQLiteObject;",
            "import rxsqlite.annotation.SQLitePk;",
            "import rxsqlite.annotation.SQLiteColumn;",
            "@SQLiteObject(value = \"foo\", constraints = {\"UNIQUE(int, short)\"})",
            "public class Foo {",
            "  @SQLitePk",
            "  private long mLong;",
            "  @SQLiteColumn(index = true)",
            "  private int mInt;",
            "  @SQLiteColumn(index = true, unique = true)",
            "  private short mShort;",
            "  @SQLiteColumn",
            "  private double mDouble;",
            "  @SQLiteColumn",
            "  private boolean mBool;",
            "  @SQLiteColumn(\"my_float\")",
            "  private float mFloat;",
            "  @SQLiteColumn",
            "  private String mString;",
            "  @SQLiteColumn",
            "  private byte[] mBytes;",
            "  @SQLiteColumn(constraint = \"UNIQUE\")",
            "  private Date mDate;",
            "  @SQLiteColumn",
            "  private EnumType mEnumType = EnumType.TEST;",
            "  enum EnumType {",
            "    TEST",
            "  }",
            "}"
    ));

    public static final JavaFileObject BAR = JavaFileObjects.forSourceLines("rxsqlite.test.Bar", Arrays.asList(
            "package rxsqlite.test;",
            "import rxsqlite.annotation.SQLiteObject;",
            "import rxsqlite.annotation.SQLitePk;",
            "@SQLiteObject(\"bar\")",
            "public class Bar {",
            "  @SQLitePk",
            "  private long mLong;",
            "}"
    ));

}
