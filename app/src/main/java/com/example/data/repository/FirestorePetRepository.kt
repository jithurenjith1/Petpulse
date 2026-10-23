package com.petpulse.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.petpulse.app.data.model.CustomerProfile
import com.petpulse.app.data.model.UserPet
import com.petpulse.app.data.model.VaccinationRecord
import com.petpulse.app.data.model.MedicalReport
import com.petpulse.app.data.model.PetCertificate
import com.petpulse.app.data.model.PetTag
import com.petpulse.app.data.model.TagScan
import com.petpulse.app.data.remote.CloudinaryUploader
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
                    val pet = petFromDoc(doc)
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
                            petFromDoc(doc)?.copy(id = petId)
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
                    snapshot?.let { petFromDoc(it) }?.copy(id = petId)
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
                        val photoUrls = (doc.get("photoUrls") as? List<*>)?.filterIsInstance<String>().orEmpty()
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
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            photoUrls = photoUrls
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
        // Cloudinary first: store good-size hosted URLs. Only photos whose upload
        // fails fall back to the old base64-in-document behaviour, so nothing
        // breaks before the owner pastes his Cloudinary values.
        val urls = mutableListOf<String>()
        val photos = mutableListOf<String>()
        for (uri in photoUris.take(CERT_MAX_PHOTOS)) {
            val url = CloudinaryUploader.uploadImage(appContext, uri).getOrNull()
            if (url != null) urls.add(url)
            else compressCertToBase64(uri)?.let { photos.add(it) }
        }
        petsRef().document(docId).collection("certificates").add(
            mapOf(
                "title" to cert.title,
                "registrationId" to cert.registrationId,
                "issuedBy" to cert.issuedBy,
                "issueDate" to cert.issueDate,
                "photoData" to photos,
                "photoUrls" to urls,
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

    /** Decodes a cloud-side base64 pet photo into a cache file for Coil. */
    private fun petPhotoFileFor(docId: String, base64Data: String): String {
        return try {
            val dir = File(appContext.cacheDir, "pet_photos").apply { mkdirs() }
            // Name the file after the data so a changed photo never reuses a stale
            // cache file, while an unchanged photo is decoded only once.
            val f = File(dir, "${docId}_${base64Data.hashCode()}.jpg")
            if (!f.exists()) {
                val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
                f.writeBytes(bytes)
            }
            f.absolutePath
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "pet photo decode failed for $docId", e)
            ""
        }
    }

    /**
     * Build a UserPet from a Firestore pet document. `photoUrl` is read explicitly
     * and, when there is no hosted URL but the cloud-side base64 `photoData` is
     * present, that base64 is decoded into a cache file and used as the local photo
     * source. This is what lets a freshly reinstalled app restore the pet photo from
     * the cloud even when the Cloudinary upload produced no hosted URL. The stored
     * `photoUri` (a device content:// URI) is only a last-resort fallback.
     */
    private fun petFromDoc(doc: DocumentSnapshot): UserPet? {
        val pet = doc.toObject(UserPet::class.java) ?: return null
        if (pet.photoUrl.isNotBlank()) return pet
        if (pet.photoData.isBlank()) return pet
        val restored = petPhotoFileFor(doc.id, pet.photoData)
        return if (restored.isBlank()) pet else pet.copy(photoUri = restored)
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
            "photoUrl" to pet.photoUrl,
            "photoData" to pet.photoData,
            "notes" to pet.notes,
            "aiAnalysis" to pet.aiAnalysis,
            "qrPublicId" to pet.qrPublicId
        )
    }

    // ================= PET QR TAG (public lost-pet tag) =================

    /**
     * Create or refresh the PUBLIC, non-sensitive "pet_tags" document for a pet.
     * The document id is the random publicId (never the pet id or the uid), so the
     * page cannot be enumerated. Only safe fields are written: publicId, petName,
     * species, breed, photoData, lost, ownerUid, createdAt. The owner's phone /
     * email / address and the pet's microchip are NEVER written here. On an update
     * the existing `lost` flag and `createdAt` are preserved.
     */
    suspend fun ensurePetTag(pet: UserPet, publicId: String) {
        val ownerUid = auth.currentUser?.uid ?: return
        if (publicId.isBlank()) return
        try {
            val ref = db.collection("pet_tags").document(publicId)
            val existing = ref.get().await()
            // Cloudinary first: reuse the already-hosted pet photo URL when present,
            // else try to upload the pet's local photo. Base64 is the fallback.
            val uploaded = when {
                pet.photoUrl.isNotBlank() -> pet.photoUrl
                pet.photoUri.isBlank() -> ""
                else -> CloudinaryUploader.uploadImage(appContext, Uri.parse(pet.photoUri)).getOrNull() ?: ""
            }
            val photo = if (uploaded.isBlank()) photoBase64For(pet) else ""
            if (existing.exists()) {
                ref.update(
                    mapOf(
                        "petName" to pet.name,
                        "species" to pet.species,
                        "breed" to pet.breed,
                        "photoData" to photo,
                        "photoUrl" to uploaded
                    )
                ).await()
            } else {
                ref.set(
                    mapOf(
                        "publicId" to publicId,
                        "petName" to pet.name,
                        "species" to pet.species,
                        "breed" to pet.breed,
                        "photoData" to photo,
                        "photoUrl" to uploaded,
                        "lost" to false,
                        "ownerUid" to ownerUid,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "ensurePetTag failed for $publicId", e)
        }
    }

    /** Flip the public "lost" flag on a pet tag (owner-only write). */
    suspend fun setTagLost(publicId: String, lost: Boolean) {
        if (publicId.isBlank()) return
        try {
            db.collection("pet_tags").document(publicId).update("lost", lost).await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "setTagLost failed for $publicId", e)
        }
    }

    /** Live public tag document for a publicId (null while it does not exist). */
    fun observePetTag(publicId: String): Flow<PetTag?> = callbackFlow {
        if (publicId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val subscription = db.collection("pet_tags").document(publicId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Pet tag listener error", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val tag = try {
                    snapshot?.toObject(PetTag::class.java)
                } catch (e: Exception) {
                    Log.e("FirestorePetRepo", "Malformed pet tag", e)
                    null
                }
                trySend(tag)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Live "someone scanned your tag" notifications for the signed-in owner.
     * Reads only the owner's own tag_scans rows (rules allow owner-only reads).
     */
    fun observeTagScans(): Flow<List<TagScan>> = callbackFlow {
        val ownerUid = auth.currentUser?.uid
        if (ownerUid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = db.collection("tag_scans")
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestorePetRepo", "Tag scan listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val scans = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        TagScan(
                            id = doc.id,
                            publicId = doc.getString("publicId") ?: "",
                            ownerUid = doc.getString("ownerUid") ?: "",
                            message = doc.getString("message") ?: "",
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e("FirestorePetRepo", "Malformed tag scan doc ${doc.id}", e)
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(scans)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Best-effort base64 JPEG of the pet's photo for the public page ("" when the
     * pet has no photo). The app stores the photo as a content/file URI, so it is
     * compressed here the same way certificate photos are.
     */
    private fun photoBase64For(pet: UserPet): String {
        // Prefer the cloud-side base64 copy already stored on the pet document: it
        // is the only usable source after a reinstall, when the local photoUri is a
        // dead device URI.
        if (pet.photoData.isNotBlank()) return pet.photoData
        val uriStr = pet.photoUri
        if (uriStr.isBlank()) return ""
        return try {
            compressCertToBase64(Uri.parse(uriStr)) ?: ""
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "pet photo base64 failed", e)
            ""
        }
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
                // lastLat / lastLng are mapped straight off the document by
                // toObject(), so the saved coordinates survive every profile read
                // and are never dropped by a profile save.
                // The care plan lives ONLY on the caller's own users/{uid} document,
                // read above. The old fallback tried users/{email} then
                // admins/{email}, but firestore.rules allows a users/{uid} read only
                // when request.auth.uid == uid, so users/{email} was always denied and
                // the fallback silently did nothing. Removed instead of weakened -
                // the users/{uid} read above is the one that actually works.
                trySend(base)
            }
        awaitClose { subscription.remove() }
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
                    "carePlanUntil" to profile.carePlanUntil,
                    "fcmToken" to profile.fcmToken,
                    // Preserve the saved coordinates so a profile save (a full
                    // overwrite, not a merge) never wipes the last known location.
                    "lastLat" to profile.lastLat,
                    "lastLng" to profile.lastLng,
                    // The 5 km SOS-alert opt-out. The users/{uid} owner-update rule
                    // only pins carePlan / carePlanUntil, so this ordinary profile
                    // field is already allowed - no rules change needed.
                    "sosRadiusEnabled" to profile.sosRadiusEnabled
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "saveCustomerProfile failed", e)
        }
    }

    /**
     * Persist ONLY the FCM registration token on users/{uid}. Uses a merge write
     * so the rest of the profile document is left untouched. Called from
     * WagmiyaMessagingService.onNewToken and after sign-in. Silently no-ops when
     * nobody is signed in (the token is saved on the next sign-in instead).
     */
    suspend fun saveFcmToken(token: String) {
        if (token.isBlank()) return
        val currentUid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(currentUid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "saveFcmToken failed", e)
        }
    }

    /**
     * Persist the caller's last known coordinates on users/{uid} with a MERGE
     * write so the rest of the profile document is left untouched. Silently
     * no-ops when nobody is signed in or the coordinates are unusable (not a
     * finite number, 0,0, or out of range). Never throws.
     */
    suspend fun saveLastLocation(lat: Double, lng: Double) {
        if (lat.isNaN() || lng.isNaN()) return
        if (lat == 0.0 && lng == 0.0) return
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) return
        val currentUid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(currentUid)
                .set(mapOf("lastLat" to lat, "lastLng" to lng), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "saveLastLocation failed", e)
        }
    }

    /**
     * Account deletion (Google Play requirement): removes ALL of the signed-in
     * user's OWN data from Firestore and then deletes the Firebase Auth account.
     *
     * Owner fields verified in the app AND in firestore.rules:
     *   pets (+ vaccinations / certificates / medical_reports) -> users/{uid}/...
     *   users/{uid}                 -> the profile document itself
     *   community_posts             -> ownerId
     *   market_listings             -> ownerId
     *   orders                      -> ownerId
     *   bookings                    -> ownerId
     *   pet_tags                    -> ownerUid  (deleted by its KNOWN document id)
     *   tag_scans                   -> ownerUid  (queried by ownerUid)
     *   lost_pet_alerts             -> ownerId   (queried by ownerId)
     *
     * Only the user's OWN documents are ever touched - never another user's.
     *
     * Returns success only when every step succeeded. A failure anywhere -
     * including the Auth-account deletion - is reported as a failure so the UI
     * never claims success when part of the deletion failed.
     */
    suspend fun deleteAllUserData(): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            val userRoot = db.collection("users").document(currentUid)
            // 1. pets + their subcollections + their PUBLIC pet_tags documents
            val pets = userRoot.collection("pets").get().await()
            for (petDoc in pets.documents) {
                for (sub in listOf("vaccinations", "certificates", "medical_reports")) {
                    val subDocs = petDoc.reference.collection(sub).get().await()
                    for (d in subDocs.documents) d.reference.delete().await()
                }
                // The pet's public QR tag lives at "pet_tags/{qrPublicId}" with
                // ownerUid == me. pet_tags allows an OWNER DELETE but NOT a list
                // (allow list: if false), so it is deleted by its KNOWN document
                // id (read off the pet document) - a whereEqualTo() query would be
                // denied. Guarded with a get() because deleting a NON-EXISTENT
                // document makes the rules evaluate resource.data on null and the
                // delete would be denied.
                val publicId = petDoc.getString("qrPublicId") ?: ""
                if (publicId.isNotBlank()) {
                    val tagRef = db.collection("pet_tags").document(publicId)
                    if (tagRef.get().await().exists()) {
                        tagRef.delete().await()
                    }
                }
                petDoc.reference.delete().await()
            }
            // 2. user profile document
            userRoot.delete().await()
            // 3. own community posts (ownerId)
            val posts = db.collection("community_posts").whereEqualTo("ownerId", currentUid).get().await()
            for (d in posts.documents) d.reference.delete().await()
            // 4. own market listings (ownerId)
            val listings = db.collection("market_listings").whereEqualTo("ownerId", currentUid).get().await()
            for (d in listings.documents) d.reference.delete().await()
            // 5. own orders + bookings (ownerId)
            val orders = db.collection("orders").whereEqualTo("ownerId", currentUid).get().await()
            for (d in orders.documents) d.reference.delete().await()
            val bookings = db.collection("bookings").whereEqualTo("ownerId", currentUid).get().await()
            for (d in bookings.documents) d.reference.delete().await()
            // 6. own tag_scans (ownerUid)
            val tagScans = db.collection("tag_scans").whereEqualTo("ownerUid", currentUid).get().await()
            for (d in tagScans.documents) d.reference.delete().await()
            // 7. own lost_pet_alerts (ownerId)
            val lostPetAlerts = db.collection("lost_pet_alerts").whereEqualTo("ownerId", currentUid).get().await()
            for (d in lostPetAlerts.documents) d.reference.delete().await()
            // 8. the Firebase Auth account itself. Required for a complete deletion,
            //    so a failure here is reported honestly (Firebase may demand a
            //    recent login) instead of being swallowed.
            val user = auth.currentUser
            if (user == null) {
                return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
            }
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestorePetRepo", "deleteAllUserData failed", e)
            Result.failure(e)
        }
    }
}
