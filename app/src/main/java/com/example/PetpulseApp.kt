package com.petpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        // Create the FCM notification channel BEFORE any message can arrive, so a
        // background / notification-payload push is never dropped for a missing
        // channel.
        createNotificationChannel()
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
     * Create the default FCM notification channel as early as possible (API 26+).
     *
     * The manifest declares this channel id via
     * `com.google.firebase.messaging.default_notification_channel_id`, and
     * WagmiyaMessagingService uses the same id, so a background /
     * notification-payload push is delivered into a channel that already exists
     * instead of being silently dropped. Creating an existing channel again is a
     * no-op, so the service keeps its own creation as a harmless safety net.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            getString(R.string.fcm_default_channel_id),
            getString(R.string.fcm_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
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
