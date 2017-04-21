-optimizations !code/simplification/arithmetic,!code/simplification/cast,!element/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-keepparameternames
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public class alchemy.sqlite.compiler.** {
    public static <fields>;
    public <methods>;
}

-dontwarn alchemy.sqlite.compiler.**