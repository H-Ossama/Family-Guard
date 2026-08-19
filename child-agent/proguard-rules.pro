# Family-Guard child-agent R8 rules

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class com.parentalguard.**$$serializer { *; }
-keepclassmembers class com.parentalguard.** {
    *** Companion;
}
-keepclasseswithmembers class com.parentalguard.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Our data model + wire protocol must survive name mangling for JSON round-trips.
-keep class com.parentalguard.common.model.** { *; }
-keep class com.parentalguard.common.network.** { *; }
-keep class com.parentalguard.child.** { *; }

# --- Ktor / Netty (reflection-based engine) ---
-dontwarn io.netty.**
-keep class io.netty.** { *; }
-keep class org.slf4j.** { *; }

# java.lang.management is only fully available on newer Android APIs.
-dontwarn java.lang.management.**

# --- kotlinx.coroutines ---
-keep class kotlinx.coroutines.** { *; }
