package com.petpulse.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import java.util.concurrent.TimeUnit

/**
 * Application entry point. Enables Firestore persistent disk caching so
 * repeated app opens do not consume the free daily read quota, and schedules
 * the daily local vaccination reminder check (no FCM needed - free plan).
 */
class PetpulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        installAppCheck()
        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        val request = PeriodicWorkRequestBuilder<VaccinationReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "petpulse_vaccination_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Firebase App Check is declared in the project but must be installed here.
     * Debug builds use the Debug provider (register the debug token in the
     * Firebase console); release builds use Play Integrity so that only genuine
     * installs from Google Play can call Firebase backends.
     */
    private fun installAppCheck() {
        val factory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
    }
}
