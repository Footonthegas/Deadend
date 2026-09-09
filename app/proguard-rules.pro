# Add project specific ProGuard rules here.
# Keep ONNX Runtime classes
-keep class ai.onnxruntime.** { *; }
-keep class ai.onnxruntime.**$* { *; }

# Keep osmdroid classes
-keep class org.osmdroid.** { *; }
-keep class org.osmdroid.**$* { *; }

# Keep Google Play Services location classes
-keep class com.google.android.gms.location.** { *; }

# Keep model classes used by reflection/Room
-keep class com.sih26168.app.** { *; }

# Keep data classes
-keepclassmembers class com.sih26168.app.** {
    public *;
}
