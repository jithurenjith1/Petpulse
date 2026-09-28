package com.petpulse.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
}
