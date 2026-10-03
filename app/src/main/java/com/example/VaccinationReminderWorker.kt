package com.petpulse.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Daily background check for vaccinations that are due soon or overdue.
 * Posts a LOCAL notification per pet - works without FCM (free Firebase plan).
 * Reads the signed-in user's pets from Firestore and parses nextDueDate.
 */
class VaccinationReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.success()
            val db = FirebaseFirestore.getInstance()
            val pets = db.collection("users").document(uid).collection("pets").get().await()
            val today = LocalDate.now()
            for (petDoc in pets.documents) {
                val petName = petDoc.getString("name") ?: continue
                val vax = petDoc.reference.collection("vaccinations").get().await()
                var dueSoon: String? = null
                var overdue: String? = null
                for (v in vax.documents) {
                    if ((v.getString("status") ?: "") == "Completed") continue
                    val vaccineName = v.getString("vaccineName") ?: continue
                    val dueRaw = v.getString("nextDueDate") ?: continue
                    val due = parseDate(dueRaw) ?: continue
                    val days = ChronoUnit.DAYS.between(today, due)
                    if (days < 0) {
                        overdue = vaccineName
                    } else if (days <= 3 && dueSoon == null) {
                        dueSoon = vaccineName
                    }
                }
                val message = when {
                    overdue != null -> "$petName: $overdue vaccination is OVERDUE. Please book a vet visit."
                    dueSoon != null -> "$petName: $dueSoon vaccination is due soon. Book a vet visit."
                    else -> continue
                }
                notifyPet(petName, message)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("VaxReminder", "Reminder check failed", e)
            Result.retry()
        }
    }

    /** Best-effort parse of the free-text next-due date (same formats as the app UI). */
    private fun parseDate(raw: String): LocalDate? {
        val formats = listOf(
            "MMM d, yyyy", "MMM d yyyy", "d MMMM yyyy",
            "dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "yyyy-MM-dd"
        )
        for (f in formats) {
            try {
                return LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern(f, Locale.ENGLISH))
            } catch (_: Exception) { }
        }
        return null
    }

    private fun notifyPet(petName: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Vaccination Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Wagmiya - $petName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()
        nm.notify(petName.hashCode(), notif)
    }

    companion object {
        const val CHANNEL_ID = "petpulse_vaccination_reminders"
    }
}
