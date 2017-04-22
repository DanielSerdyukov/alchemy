-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public class alchemy.sqlcipher.** {
    public static <fields>;
    public <methods>;
}

-dontwarn net.sqlcipher.database.**