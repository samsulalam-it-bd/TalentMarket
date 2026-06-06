# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep standard java/android attributes for debugging and reflection
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, SourceFile, LineNumberTable

# Preserve name-based serialization in Firebase Firestore models
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep all com.example.data classes completely (Room, Entities, Repositories, etc.)
-keep class com.example.data.** { *; }

# Keep serializable classes completely
-keep class * implements java.io.Serializable { *; }

# Keep com.example.ui model/data classes mapped to/from Firestore or local database
-keep class com.example.ui.AdminLog { *; }
-keep class com.example.ui.ReportedPost { *; }
-keep class com.example.ui.SecurityLog { *; }
-keep class com.example.ui.AdminStats { *; }
-keep class com.example.ui.UserAdmin { *; }
-keep class com.example.ui.BoostPurchase { *; }
-keep class com.example.ui.Category { *; }
-keep class com.example.ui.FilterModel { *; }
-keep class com.example.ui.SavedFilter { *; }
-keep class com.example.ui.JobFilter { *; }
-keep class com.example.ui.WorkerFilter { *; }
-keep class com.example.ui.screens.PremiumPlan { *; }
-keep class com.example.ui.JobCategory { *; }
-keep class com.example.ui.FirestoreErrorHandler$IndexErrorInfo { *; }

# Firebase Firestore / Common / Auth and Remote Config rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Android Room rules
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase {
    <init>(...);
}
-keep class com.example.data.TalentDatabase_Impl { *; }
-keep class com.example.data.*Dao_Impl { *; }

# OneSignal rules
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# AdMob rules
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Retrofit & Moshi rules (in case they are used)
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Android Jetpack Compose & Navigation Rules (automatically included by compose but safely reinforced here)
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
