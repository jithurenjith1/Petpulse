package com.petpulse.app.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.petpulse.app.R
import com.petpulse.app.data.repository.FirestorePetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Receives Firebase Cloud Messaging events for the app.
 *
 * - [onNewToken] persists the device registration token to the signed-in user's
 *   document (`users/{uid}.fcmToken`) so the sender job can target the device.
 *   When nobody is signed in the token is ignored here and saved on the next
 *   sign-in instead (see AuthViewModel).
 * - [onMessageReceived] posts a simple local notification for a data or
 *   notification message so an SOS / care alert is visible in the shade.
 *
 * The default notification channel id is declared in AndroidManifest.xml via
 * `com.google.firebase.messaging.default_notification_channel_id` so a
 * notification-payload message also lands in the right channel.
 */
class WagmiyaMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Fire-and-forget: persist on the signed-in user's document. If no user
        // is signed in yet, FirestorePetRepository.saveFcmToken() no-ops.
        serviceScope.launch {
            try {
                FirestorePetRepository(applicationContext).saveFcmToken(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save FCM token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"]
            ?: message.notification?.title
            ?: getString(R.string.fcm_default_title)
        val body = message.data["body"]
            ?: message.notification?.body
            ?: getString(R.string.fcm_default_body)
        showNotification(title, body)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /** Post a minimal notification using the app's existing launcher icon. */
    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.fcm_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted on Android 13+ - ignore quietly.
            Log.e(TAG, "Notification not permitted", e)
        }
    }

    companion object {
        private const val TAG = "WagmiyaMessaging"
        private const val CHANNEL_ID = "wagmiya_default_channel"
        private const val NOTIFICATION_ID = 9001
    }
}
