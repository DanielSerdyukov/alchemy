-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public interface rxsqlite.** { *; }

-keep public class rxsqlite.** {
    public static <fields>;
    public <methods>;
}

-keep class rxsqlite.SQLiteTable {
    public <methods>;
    protected <methods>;
}