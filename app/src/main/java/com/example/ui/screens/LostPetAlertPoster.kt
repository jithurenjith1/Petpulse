package com.petpulse.app.ui.screens

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Posts a lost-pet SOS alert to the Firestore "lost_pet_alerts" collection.
 *
 * Every signed-in user sees new alerts in the Lost Pet Alerts feed in real time
 * (the feed uses a live snapshot listener, so an alert posted from one phone
 * appears on all other phones while the app is open).
 *
 * The document satisfies the deployed Firestore security rules, which require
 * the keys "ownerId" (matching the signed-in user) and "petName".
 *
 * Returns true on success, false on failure (e.g. not signed in or write denied).
 */
suspend fun postSosAlert(
    petName: String,
    species: String,
    breed: String,
    contactPhone: String,
    locationLink: String,
    reward: String = ""
): Boolean {
    val user = FirebaseAuth.getInstance().currentUser ?: return false
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    return try {
        FirebaseFirestore.getInstance().collection("lost_pet_alerts")
            .add(
                mapOf(
                    "ownerId" to user.uid,
                    "petName" to petName,
                    "location" to locationLink,
                    "species" to species,
                    "breed" to breed,
                    "lastSeenLocation" to locationLink,
                    "reward" to reward,
                    "contactPhone" to contactPhone,
                    "date" to date
                )
            )
            .await()
        true
    } catch (e: Exception) {
        false
    }
}
