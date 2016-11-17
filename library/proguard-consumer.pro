-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep class rxsqlite.SQLite$$Schema {
    public static final <fields>;
}

-keep class * extends rxsqlite.RxSQLiteTable {
    public <methods>;
    protected <methods>;
}