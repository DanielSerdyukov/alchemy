-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public class rxsqlite.annotation.** { *; }

-keep public class rxsqlite.compiler.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
}

-dontwarn com.google.auto.**
-dontwarn com.squareup.**
-dontwarn rx.**
