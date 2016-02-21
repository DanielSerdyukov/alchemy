-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public class rxsqlite.annotation.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
    native <methods>;
}

-keep public class rxsqlite.compiler.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
    native <methods>;
}

-dontwarn com.google.auto.**
-dontwarn com.squareup.**
-dontwarn rx.**
-keepclassmembers class rxsqlite.compiler.Utils {
    void setAccessible(javax.lang.model.element.Element);
}
