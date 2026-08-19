# Family-Guard parent-controller R8 rules (minify disabled by default)

-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class com.parentalguard.**$$serializer { *; }
-keepclassmembers class com.parentalguard.** {
    *** Companion;
}
-keepclasseswithmembers class com.parentalguard.** {
    kotlinx.serialization.KSerializer serializer(...);
}
