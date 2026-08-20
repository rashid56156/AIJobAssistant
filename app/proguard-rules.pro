# Keep kotlinx.serialization generated serializers; R8 can't see into the
# compiler-generated companion objects' reflection usage otherwise, and
# losing these would silently break Gemini response parsing only in release
# builds - exactly the kind of bug that's painful to track down.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sample.aijobassistant.**$$serializer { *; }
-keepclassmembers class com.sample.aijobassistant.** {
    *** Companion;
}
-keepclasseswithmembers class com.sample.aijobassistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}
