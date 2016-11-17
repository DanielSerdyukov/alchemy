-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*
-optimizations !method/inlining/*,!code/allocation/variable
-keepparameternames

-keep public interface rxsqlite.** { *; }

-keep public class rxsqlite.** {
    public static <fields>;
    public <methods>;
}

-dontwarn java.lang.invoke.*

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}