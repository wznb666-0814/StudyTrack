# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in 'proguard-android-optimize.txt' which is shipped with the Android SDK.

# Keep Room generated classes
-keep class com.example.studytrack.data.local.dao.** { *; }
-keep class com.example.studytrack.data.local.entity.** { *; }

# Keep ViewModel classes
-keep class com.example.studytrack.viewmodel.** { *; }

# Keep Compose related classes
-keep class androidx.compose.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Hilt/Dagger rules
-keep class com.example.studytrack.di.** { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**
