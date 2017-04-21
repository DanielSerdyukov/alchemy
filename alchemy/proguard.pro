-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-keepparameternames
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep interface alchemy.** { *; }
-keep public class alchemy.** {
    public static <fields>;
    public <methods>;
}

-dontwarn alchemy.**