package com.petpulse.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.petpulse.app.data.model.CustomerProfile
import com.petpulse.app.data.model.UserPet
import com.petpulse.app.data.model.VaccinationRecord
import com.petpulse.app.data.model.MedicalReport
import com.petpulse.app.data.model.PetCertificate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FirestorePetRepository(private val appContext: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun uid(): String = auth.currentUser?.uid ?: "anonymous"

    private fun petsRef() = db.collection("users").document(uid()).collection("pets")

    // Thread-safe map of pet Long ID -> Firestore doc ID
    private val petDocIdMap = ConcurrentHashMap<Long, String>()
    private var nextLocalId = 1L

    private fun stableIdOf(docId: String): Long {
        val h = docId.hashCode().toLong()
        return if (h < 0) -h else h
    }

    /**
     * Resolve the Firestore doc id for a pet's stable id.
     * If the in-memory map is cold (e.g. app restarted straight into the pet
     * screen), query the pets collection once and fill the map — so saves and
     * listeners always work instead of silently doing nothing.
     */
    private suspend fun awaitDocIdFor(petId: Long): String? {
        petDocIdMap[petId]?.let { return it }
        return try {
            val snapshot = petsRef().get().await()
            for (doc in snapshot.documents) {
                petDocIdMap[stableIdOf(doc.id)] = doc.id
            }
            petDocIdMap[petId]
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "Failed to resolve pet doc for id $petId", e)
            null
        }
    }

    fun getAllUserPets(): Flow<List<UserPet>> = callbackFlow {
        val subscription = petsRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Log but NEVER close with error — that crashes the app
                Log.e("FirestorePetRepo", "Error getting pets", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val pets = mutableListOf<UserPet>()
            var maxId = 0L
            snapshot?.documents?.forEach { doc ->
                try {
                    val pet = doc.toObject(UserPet::class.java)
                    if (pet != null) {
                        val stableId = stableIdOf(doc.id)
                        petDocIdMap[stableId] = doc.id
                        if (stableId > maxId) maxId = stableId
                        pets.add(pet.copy(id = stableId))
                    }
                } catch (e: Exception) {
                    // Skip malformed docs instead of crashing
                    Log.e("FirestorePetRepo", "Skipping malformed pet doc ${doc.id}", e)
                }
            }
            nextLocalId = maxId + 1
            trySend(pets)
        }
        awaitClose { subscription.remove() }
    }

    fun getPetById(petId: Long): Flow<UserPet?> = callbackFlow {
        val docId = petDocIdMap[petId]
        if (docId == null) {
            // Try to find it by querying all pets
            try {
                val snapshot = petsRef().get().await()
                var found = false
                for (doc in snapshot.documents) {
                    val stableId = stableIdOf(doc.id)
                    if (stableId == petId) {
                        petDocIdMap[petId] = doc.id
                        val pet = try {
                            doc.toObject(UserPet::class.java)?.copy(id = petId)
                        } catch (e: Exception) {
                            Log.e("FirestorePetRepo", "Malformed pet doc ${doc.id}", e)
                            null
                        }
                        if (pet != null) {
                            trySend(pet)
                            found = true
                        }
                        break
                    }
                }
                if (!found) trySend(null)
            } catch (e: Exception) {
                // Emit null instead of crashing — UI falls back to previous pet
                Log.e("FirestorePetRepo", "Error querying pets", e)
                trySend(null)
            }
        } else {
            val subscription = petsRef().document(docId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // NEVER close with error — crash source. Log and emit null instead.
                    Log.e("FirestorePetRepo", "Pet listener error", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val pet = try {
                    snapshot?.toObject(UserPet::class.java)?.copy(id = petId)
                } catch (e: Exception) {
                    Log.e("FirestorePetRepo", "Malformed pet snapshot", e)
                    null
                }
                trySend(pet)
            }
            awaitClose { subscription.remove() }
            return@callbackFlow
        }
        awaitClose { }
    }

    /**
     * Persists a pet and returns its stable id. A pet with id <= 0 is a brand-new
     * pet: it is always APPENDED as a new document (never overwriting an existing
     * one) and the freshly-generated stable id is returned so the caller can select
     * the new pet directly. Existing pets (id > 0) are updated in place and their
     * own id is returned.
     */
    suspend fun savePet(pet: UserPet): Long {
        val petMap = petToMap(pet)
        if (pet.id <= 0L) {
            // Brand-new pet (PetViewModel.addNewPet sets id = 0). Always create a NEW
            // document so adding a second/third pet never overwrites an existing one.
            val docRef = petsRef().add(petMap).await()
            val newId = stableIdOf(docRef.id)
            petDocIdMap[newId] = docRef.id
            return newId
        } else {
            val existingDocId = petDocIdMap[pet.id]

            if (existingDocId != null) {
                petsRef().document(existingDocId).set(petMap).await()
            } else {
                val snapshot = petsRef().get().await()
                var found = false
                for (doc in snapshot.documents) {
                    val stableId = stableIdOf(doc.id)
                    if (stableId == pet.id) {
                        petDocIdMap[pet.id] = doc.id
                        petsRef().document(doc.id).set(petMap).await()
                        found = true
                        break
                    }
                }
                if (!found) {
                    val docRef = petsRef().add(petMap).await()
                    val newStableId = stableIdOf(docRef.id)
                    petDocIdMap[newStableId] = docRef.id
                    return newStableId
                }
            }
            return pet.id
        }
    }

    suspend fun deletePet(petId: Long) {
        val docId = petDocIdMap[petId]
        if (docId != null) {
            try {
                deleteVaccinationsForPet(docId)
            } catch (e: Exception) {
                Log.e("FirestorePetRepo", "Error deleting vaccinations", e)
            }
            try {
                deleteMedicalReportsForPet(docId)
            } catch (e: Exception) {
                Log.e("FirestorePetRepo", "Error deleting medical reports", e)
            }
            petsRef().document(docId).delete().await()
            petDocIdMap.remove(petId)
        } else {
            // Pet not in map — find by scanning
            try {
                val snapshot = petsRef().get().await()
                for (doc in snapshot.documents) {
                    if (stableIdOf(doc.id) == petId) {
                        petsRef().document(doc.id).delete().await()
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestorePetRepo", "Error deleting pet", e)
            }
        }
    }

    fun getVaccinationsForPet(petId: Long): Flow<List<VaccinationRecord>> = callbackFlow {
        val docId = awaitDocIdForWithRetry(petId)
        if (docId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = petsRef().document(docId)
            .collection("vaccinations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Vaccination listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val vax = doc.toObject(VaccinationRecord::class.java)
                        vax?.copy(id = stableIdOf(doc.id))
                    } catch (e: Exception) {
                        Log.e("FirestorePetRepo", "Malformed vaccination doc", e)
                        null
                    }
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * DIAGNOSTIC VERSION: returns null on success, otherwise a human-readable
     * reason string so the UI can show WHY a vaccination save failed.
     */
    /**
     * Retry pet-doc resolution for up to ~6 seconds. A cold id map or a slow
     * first Firestore query must NOT dead-end a read flow with an empty list.
     */
    private suspend fun awaitDocIdForWithRetry(petId: Long): String? {
        repeat(10) {
            val id = awaitDocIdFor(petId)
            if (id != null) return id
            delay(600)
        }
        return null
    }

    suspend fun addVaccination(petId: Long, record: VaccinationRecord) {
        val docId = awaitDocIdFor(petId) ?: return
        val recordMap = mapOf(
            "petId" to petId,
            "vaccineName" to record.vaccineName,
            "dateGiven" to record.dateGiven,
            "nextDueDate" to record.nextDueDate,
            "status" to record.status,
            "veterinarian" to record.veterinarian,
            "batchNumber" to record.batchNumber
        )
        try {
            petsRef().document(docId).collection("vaccinations").add(recordMap).await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "addVaccination write failed", e)
        }
    }

    suspend fun updateVaccinationStatus(petId: Long, recordId: Long, newStatus: String) {
        val petDocId = awaitDocIdFor(petId) ?: return
        val snapshot = petsRef().document(petDocId).collection("vaccinations").get().await()
        for (doc in snapshot.documents) {
            if (stableIdOf(doc.id) == recordId) {
                petsRef().document(petDocId).collection("vaccinations").document(doc.id)
                    .update("status", newStatus).await()
                break
            }
        }
    }

    private suspend fun deleteVaccinationsForPet(petDocId: String) {
        val snapshot = petsRef().document(petDocId).collection("vaccinations").get().await()
        snapshot.documents.forEach { doc -> doc.reference.delete().await() }
    }

    // ---------- real certificates (photos stored as compressed base64) ----------

    companion object {
        private const val CERT_MAX_PHOTOS = 3
        private const val CERT_MAX_DIM = 1400
        private const val CERT_PHOTO_BUDGET_BYTES = 180_000
    }

    fun getCertificatesForPet(petId: Long): Flow<List<PetCertificate>> = callbackFlow {
        val docId = awaitDocIdForWithRetry(petId)
        if (docId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = petsRef().document(docId)
            .collection("certificates")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Certificate listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val certs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val photoData = (doc.get("photoData") as? List<*>)?.filterIsInstance<String>().orEmpty()
                        PetCertificate(
                            id = stableIdOf(doc.id),
                            petId = petId,
                            title = doc.getString("title") ?: "Certificate",
                            registrationId = doc.getString("registrationId") ?: "",
                            issuedBy = doc.getString("issuedBy") ?: "",
                            issueDate = doc.getString("issueDate") ?: "",
                            photoPaths = photoData.mapIndexed { i, b64 ->
                                certPhotoFileFor(doc.id, i, b64)
                            }.filter { it.isNotBlank() },
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e("FirestorePetRepo", "Malformed certificate doc", e)
                        null
                    }
                } ?: emptyList()
                trySend(certs)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addCertificate(petId: Long, cert: PetCertificate, photoUris: List<Uri>) {
        val docId = awaitDocIdFor(petId) ?: return
        val photos = photoUris.take(CERT_MAX_PHOTOS).mapNotNull { compressCertToBase64(it) }
        petsRef().document(docId).collection("certificates").add(
            mapOf(
                "title" to cert.title,
                "registrationId" to cert.registrationId,
                "issuedBy" to cert.issuedBy,
                "issueDate" to cert.issueDate,
                "photoData" to photos,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun deleteCertificate(petId: Long, certificateId: Long) {
        val petDocId = awaitDocIdFor(petId) ?: return
        val snapshot = petsRef().document(petDocId).collection("certificates").get().await()
        for (doc in snapshot.documents) {
            if (stableIdOf(doc.id) == certificateId) {
                petsRef().document(petDocId).collection("certificates").document(doc.id).delete().await()
                break
            }
        }
    }

    /** Decodes a base64 certificate photo into a cache file for Coil. */
    private fun certPhotoFileFor(docId: String, index: Int, base64Data: String): String {
        return try {
            val dir = File(appContext.cacheDir, "cert_photos").apply { mkdirs() }
            val f = File(dir, "${docId}_$index.jpg")
            if (!f.exists()) {
                val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
                f.writeBytes(bytes)
            }
            f.absolutePath
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "cert photo decode failed for $docId/$index", e)
            ""
        }
    }

    /** Downscale + JPEG-compress a certificate photo, return base64 (or null). */
    private fun compressCertToBase64(uri: Uri): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > CERT_MAX_DIM * 2) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            val scale = minOf(1f, CERT_MAX_DIM.toFloat() / maxOf(decoded.width, decoded.height, 1))
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else decoded

            val out = ByteArrayOutputStream()
            var quality = 78
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
            while (out.size() > CERT_PHOTO_BUDGET_BYTES && quality > 30) {
                quality -= 16
                out.reset()
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "cert photo compress failed", e)
            null
        }
    }

    fun getMedicalReportsForPet(petId: Long): Flow<List<MedicalReport>> = callbackFlow {
        val docId = awaitDocIdForWithRetry(petId)
        if (docId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = petsRef().document(docId)
            .collection("medical_reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Medical reports listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val report = doc.toObject(MedicalReport::class.java)
                        report?.copy(id = stableIdOf(doc.id))
                    } catch (e: Exception) {
                        Log.e("FirestorePetRepo", "Malformed medical report doc", e)
                        null
                    }
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addMedicalReport(petId: Long, report: MedicalReport) {
        val docId = awaitDocIdFor(petId) ?: return
        val reportMap = mapOf(
            "petId" to petId,
            "title" to report.title,
            "clinicName" to report.clinicName,
            "date" to report.date,
            "diagnosis" to report.diagnosis,
            "prescription" to report.prescription
        )
        petsRef().document(docId).collection("medical_reports").add(reportMap).await()
    }

    private suspend fun deleteMedicalReportsForPet(petDocId: String) {
        val snapshot = petsRef().document(petDocId).collection("medical_reports").get().await()
        snapshot.documents.forEach { doc -> doc.reference.delete().await() }
    }

    private fun petToMap(pet: UserPet): Map<String, Any> {
        return mapOf(
            "name" to pet.name,
            "species" to pet.species,
            "breed" to pet.breed,
            "gender" to pet.gender,
            "ageYears" to pet.ageYears,
            "ageMonths" to pet.ageMonths,
            "weightKg" to pet.weightKg,
            "microchipNumber" to pet.microchipNumber,
            "hasCertificate" to pet.hasCertificate,
            "certificateNumber" to pet.certificateNumber,
            "certificateIssuedBy" to pet.certificateIssuedBy,
            "certificateDate" to pet.certificateDate,
            "favoriteFoods" to pet.favoriteFoods,
            "favoritePlays" to pet.favoritePlays,
            "trainingStatus" to pet.trainingStatus,
            "trainingLevel" to pet.trainingLevel,
            "trainingMilestones" to pet.trainingMilestones,
            "avatarRes" to pet.avatarRes,
            "photoUri" to pet.photoUri,
            "notes" to pet.notes,
            "aiAnalysis" to pet.aiAnalysis
        )
    }

    // ================= CUSTOMER PROFILE (Premium gating) =================

    /**
     * Live customer profile stored at users/{uid}. The owner flips
     * `carePlan` to "premium" for a user in the Firebase console; this flow
     * surfaces that value (plus `carePlanUntil`) to the app so the Premium
     * screens can gate themselves. Never throws — a missing doc or a rules
     * denial simply emits the default profile.
     */
    fun observeCustomerProfile(): Flow<CustomerProfile> = callbackFlow {
        val subscription = db.collection("users").document(uid())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Customer profile listener error", error)
                    trySend(CustomerProfile())
                    return@addSnapshotListener
                }
                val profile = try {
                    snapshot?.toObject(CustomerProfile::class.java)
                } catch (e: Exception) {
                    Log.e("FirestorePetRepo", "Malformed customer profile", e)
                    null
                }
                val base = profile ?: CustomerProfile()
                if (base.carePlan.isNotBlank()) {
                    trySend(base)
                } else {
                    // The plan may have been set on an email-keyed document instead of
                    // users/{uid}. Try users/{email}, then admins/{email}.
                    lookupCarePlan { plan, until ->
                        trySend(
                            if (plan.isBlank()) base
                            else base.copy(carePlan = plan, carePlanUntil = until)
                        )
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Fallback lookup for carePlan / carePlanUntil. Checks the email-keyed documents
     * (users/{email} then admins/{email}) so the premium flag is found wherever the
     * owner set it.
     */
    private fun lookupCarePlan(onResult: (String, Long) -> Unit) {
        val email = com.google.firebase.auth.FirebaseAuth.getInstance()
            .currentUser?.email
        if (email.isNullOrBlank()) { onResult("", 0L); return }
        val candidates = listOf(
            db.collection("users").document(email),
            db.collection("admins").document(email)
        )
        fun tryAt(i: Int) {
            if (i >= candidates.size) { onResult("", 0L); return }
            candidates[i].get()
                .addOnSuccessListener { snap ->
                    val plan = snap.getString("carePlan") ?: ""
                    if (plan.isNotBlank()) {
                        onResult(plan, snap.getLong("carePlanUntil") ?: 0L)
                    } else {
                        tryAt(i + 1)
                    }
                }
                .addOnFailureListener { tryAt(i + 1) }
        }
        tryAt(0)
    }

    /** Persists the customer profile, including the `carePlan` / `carePlanUntil` gating fields. */
    suspend fun saveCustomerProfile(profile: CustomerProfile) {
        try {
            db.collection("users").document(uid()).set(
                mapOf(
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "location" to profile.location,
                    "isLoggedIn" to profile.isLoggedIn,
                    "memberSince" to profile.memberSince,
                    "carePlan" to profile.carePlan,
                    "carePlanUntil" to profile.carePlanUntil
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "saveCustomerProfile failed", e)
        }
    }

    /**
     * Account deletion (Google Play requirement): removes ALL user data from
     * Firestore (pets + subcollections, profile, posts, listings, orders,
     * bookings) and finally deletes the Firebase Auth account itself.
     * Returns success when the Firestore data was removed (auth deletion is
     * best-effort because Firebase may require a recent login).
     */
    suspend fun deleteAllUserData(): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            val userRoot = db.collection("users").document(currentUid)
            // 1. pets + their subcollections
            val pets = userRoot.collection("pets").get().await()
            for (petDoc in pets.documents) {
                for (sub in listOf("vaccinations", "certificates", "medical_reports")) {
                    val subDocs = petDoc.reference.collection(sub).get().await()
                    for (d in subDocs.documents) d.reference.delete().await()
                }
                petDoc.reference.delete().await()
            }
            // 2. user profile document
            userRoot.delete().await()
            // 3. own community posts
            val posts = db.collection("community_posts").whereEqualTo("ownerId", currentUid).get().await()
            for (d in posts.documents) d.reference.delete().await()
            // 4. own market listings
            val listings = db.collection("market_listings").whereEqualTo("ownerId", currentUid).get().await()
            for (d in listings.documents) d.reference.delete().await()
            // 5. own orders + bookings
            val orders = db.collection("orders").whereEqualTo("ownerId", currentUid).get().await()
            for (d in orders.documents) d.reference.delete().await()
            val bookings = db.collection("bookings").whereEqualTo("ownerId", currentUid).get().await()
            for (d in bookings.documents) d.reference.delete().await()
            // 6. the Firebase Auth account (best-effort)
            try {
                auth.currentUser?.delete()?.await()
            } catch (e: Exception) {
                Log.e("FirestorePetRepo", "Auth account delete failed (data already removed)", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "deleteAllUserData failed", e)
            Result.failure(e)
        }
    }
}
