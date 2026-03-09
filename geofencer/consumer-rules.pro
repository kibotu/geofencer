# Geofencer library
-keep class net.kibotu.geofencer.** { *; }
-keep class com.google.android.gms.location.LocationAvailability

# Keep GeofenceAction and LocationAction subclasses (instantiated via reflection)
-keep class * extends net.kibotu.geofencer.GeofenceAction { <init>(); }
-keep class * extends net.kibotu.geofencer.LocationAction { <init>(); }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class net.kibotu.geofencer.**$$serializer { *; }
-keepclassmembers class net.kibotu.geofencer.** {
    *** Companion;
}
-keepclasseswithmembers class net.kibotu.geofencer.** {
    kotlinx.serialization.KSerializer serializer(...);
}
