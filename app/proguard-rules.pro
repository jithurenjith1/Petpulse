# ---------------------------------------------------------------------------
# Project-specific ProGuard / R8 rules for Wagmiya (com.petpulse.app).
#
# minifyEnabled = true is enabled for the release build type (see
# app/build.gradle.kts -> buildTypes.release.proguardFiles), so these keep
# rules are REQUIRED. Firestore deserialises documents reflectively through
# DocumentSnapshot.toObject(...), which silently returns null / empty objects
# if R8 renames or strips the model fields or the no-arg constructor.
# ---------------------------------------------------------------------------

# --- Firestore / Room data models (loaded reflectively via toObject()) ---
-keep class com.petpulse.app.data.model.** { *; }

# --- Documents deserialised from other packages ---
# LostPetAlertItem is defined in the ui.screens package but is still created
# reflectively by DocumentSnapshot.toObject<LostPetAlertItem>().
-keep class com.petpulse.app.ui.screens.LostPetAlertItem { *; }

# --- Generic signatures + annotations that reflective (de)serialisation needs ---
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses

# --- Firestore field mapping (@PropertyName) ---
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# --- Room: keep the generated database implementation and entities ---
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# --- Firebase AI Logic (Gemini) types are constructed by the SDK ---
-keep class com.google.firebase.ai.** { *; }
-dontwarn com.google.firebase.ai.**

# --- Networking libraries: silence optional-dependency warnings ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
