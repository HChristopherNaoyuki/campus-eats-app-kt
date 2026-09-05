# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\naoyu\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any project specific keep rules here:

# Firebase Authentication and Realtime Database
-keepattributes *Annotation*
-keepattributes Signature

# Room Persistence Library
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Moshi JSON Library
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *

# Retrofit Networking Library
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Kotlin Serialization
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
}
