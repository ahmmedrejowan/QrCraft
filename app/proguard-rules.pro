# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep custom exceptions for better error tracking
-keep public class * extends java.lang.Exception

# ===== Kotlinx Serialization =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keep,includedescriptorclasses class com.rejown.qrcraft.**$$serializer { *; }
-keepclassmembers class com.rejown.qrcraft.** {
    *** Companion;
}
-keepclasseswithmembers class com.rejown.qrcraft.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===== Room Database =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities
-keep class com.rejown.qrcraft.data.local.entity.** { *; }

# ===== Koin DI =====
-keepnames class org.koin.core.** { *; }
-keepnames class org.koin.android.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }

# Keep Koin modules
-keep class com.rejown.qrcraft.di.** { *; }

# ===== ML Kit & ZXing =====
-keep class com.google.mlkit.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.zxing.**

# ===== CameraX =====
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ===== Jetpack Compose =====
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn androidx.compose.**

# ===== DataStore =====
-keep class androidx.datastore.*.** { *; }

# ===== Coil Image Loading =====
-keep class coil.** { *; }
-keep interface coil.** { *; }
-keep class * implements coil.decode.Decoder

# ===== Timber Logging =====
-dontwarn org.jetbrains.annotations.**
-keep class timber.log.** { *; }

# ===== Data Classes & Models =====
# Keep all data classes with their properties
-keep class com.rejown.qrcraft.data.** { *; }
-keep class com.rejown.qrcraft.domain.** { *; }

# ===== Parcelable =====
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ===== Enums =====
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ===== General Android =====
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ===== Optimization Settings =====
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove logging in release (optional - uncomment if desired)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }

# Remove Timber logging in release (optional - uncomment if desired)
# -assumenosideeffects class timber.log.Timber {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
