# Family-Guard :common consumer rules (applied to consuming apps)

-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class com.parentalguard.common.**$$serializer { *; }
-keepclassmembers class com.parentalguard.common.** {
    *** Companion;
}
-keepclasseswithmembers class com.parentalguard.common.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.parentalguard.common.model.** { *; }
-keep class com.parentalguard.common.network.** { *; }
