-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!method/inlining/*,!code/allocation/variable
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-keepparameternames

-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep interface rxsqlite.** { *; }

-keep public class rxsqlite.** {
    public static <fields>;
    public <methods>;
}

-dontwarn rxsqlite.**
-dontwarn com.squareup.**
-dontwarn com.google.auto.**