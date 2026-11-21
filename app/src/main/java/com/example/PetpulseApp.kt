package com.petpulse.app

import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProvider
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.AppCheckToken
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.appcheck.FirebaseAppCheck
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
            // A sideloaded debug APK has no Play Integrity, and the auto-generated
            // debug token only exists in logcat (unreadable from a phone). Pinning the
            // token makes it a known constant that can be registered once in the
            // Firebase console. Release builds never reach this branch.
            FixedDebugAppCheckProviderFactory(DEBUG_APP_CHECK_TOKEN)
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
    }
}

/**
 * Debug-only App Check token.
 *
 * Register this exact value once in Firebase Console -> App Check -> Apps ->
 * (the com.petpulse.app app) -> Manage debug tokens. It is only used when
 * BuildConfig.DEBUG is true; release builds use Play Integrity.
 */
const val DEBUG_APP_CHECK_TOKEN = "3c6b6dc2-2606-4883-899e-648fe3fb154b"

private class FixedDebugAppCheckProvider(
    private val token: String
) : AppCheckProvider {
    private fun freshToken() = AppCheckToken(token, System.currentTimeMillis() + 3_600_000L)

    override fun getToken(): com.google.android.gms.tasks.Task<AppCheckToken> =
        Tasks.forResult(freshToken())

    override fun getLimitedUseToken(): com.google.android.gms.tasks.Task<AppCheckToken> =
        Tasks.forResult(freshToken())
}

private class FixedDebugAppCheckProviderFactory(
    private val token: String
) : AppCheckProviderFactory {
    override fun create(firebaseApp: FirebaseApp): AppCheckProvider =
        FixedDebugAppCheckProvider(token)
}
