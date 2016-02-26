-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public interface rxsqlite.** { *; }

-keep public class rxsqlite.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
    native <methods>;
}

-keep class rxsqlite.RxSQLiteClient {
    void registerTable(java.lang.Class, rxsqlite.RxSQLiteTable);
}
-keep class rxsqlite.Types { *; }
