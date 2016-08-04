-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep class rxsqlite.SQLite$$Schema {
    public static void init();
}

-keep class * extends rxsqlite.SQLiteTable {
    public <methods>;
    protected <methods>;
}