-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public interface rxsqlite.** { *; }

-keep public class rxsqlite.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
    native <methods>;
}

-keep interface rxsqlite.RxSQLiteTable { *; }
-keep class rxsqlite.RxSQLiteBinder { *; }
-keep class rxsqlite.RxSQLiteClient {
    void registerTable(java.lang.Class, rxsqlite.RxSQLiteTable);
}
