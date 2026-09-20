package com.petpulse.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.petpulse.app.data.model.UserPet
import com.petpulse.app.data.model.VaccinationRecord
import com.petpulse.app.data.model.MedicalReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class FirestorePetRepository {

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

    suspend fun savePet(pet: UserPet) {
        val petMap = petToMap(pet)
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
            }
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
        val docId = petDocIdMap[petId] ?: run {
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

    suspend fun addVaccination(petId: Long, record: VaccinationRecord) {
        val docId = petDocIdMap[petId] ?: return
        val recordMap = mapOf(
            "petId" to petId,
            "vaccineName" to record.vaccineName,
            "dateGiven" to record.dateGiven,
            "nextDueDate" to record.nextDueDate,
            "status" to record.status,
            "veterinarian" to record.veterinarian,
            "batchNumber" to record.batchNumber
        )
        petsRef().document(docId).collection("vaccinations").add(recordMap).await()
    }

    suspend fun updateVaccinationStatus(petId: Long, recordId: Long, newStatus: String) {
        val petDocId = petDocIdMap[petId] ?: return
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

    fun getMedicalReportsForPet(petId: Long): Flow<List<MedicalReport>> = callbackFlow {
        val docId = petDocIdMap[petId] ?: run {
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
        val docId = petDocIdMap[petId] ?: return
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
            "avatarRes" to pet.avatarRes,
            "photoUri" to pet.photoUri,
            "notes" to pet.notes
        )
    }
}

