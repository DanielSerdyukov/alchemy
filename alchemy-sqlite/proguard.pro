-optimizations !code/simplification/arithmetic,!code/simplification/cast,!element/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-keepparameternames
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep interface alchemy.sqlite.** { *; }
-keep public class alchemy.sqlite.** {
    public static <fields>;
    public <methods>;
}
-keep class alchemy.sqlite.AbstractTable {
    protected <init>(...);
    public <methods>;
    protected <methods>;
}
-keepclassmembers class alchemy.sqlite.** {
    public <methods>;
}

-dontwarn alchemy.sqlite.**