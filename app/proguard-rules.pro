# Keep kotlinx.serialization generated serializers for the API DTOs.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ai.opennote.android.data.** {
    *** Companion;
}
-keepclasseswithmembers class ai.opennote.android.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
