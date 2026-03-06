# Geofencer library
-keep class com.sprotte.geofencer.** { *; }
-keep class com.google.android.gms.location.LocationAvailability

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.sprotte.geofencer.**$$serializer { *; }
-keepclassmembers class com.sprotte.geofencer.** {
    *** Companion;
}
-keepclasseswithmembers class com.sprotte.geofencer.** {
    kotlinx.serialization.KSerializer serializer(...);
}
