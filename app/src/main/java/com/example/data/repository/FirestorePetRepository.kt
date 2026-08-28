package com.example.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.data.model.UserPet
import com.example.data.model.VaccinationRecord
import com.example.data.model.MedicalReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePetRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun uid(): String = auth.currentUser?.uid ?: "anonymous"

    private fun petsRef() = db.collection("users").document(uid()).collection("pets")

    private fun petDoc(petId: String) = petsRef().document(petId)

    fun getAllUserPets(): Flow<List<UserPet>> = callbackFlow {
        val subscription = petsRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val pets = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserPet::class.java)?.copy(id = doc.id.hashCode().toLong())
            } ?: emptyList()
            trySend(pets)
        }
        awaitClose { subscription.remove() }
    }

    fun getPetById(petId: Long): Flow<UserPet?> = callbackFlow {
        val petDocId = findPetDocId(petId)
        val subscription = petsRef().document(petDocId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val pet = snapshot?.toObject(UserPet::class.java)?.copy(id = petId)
            trySend(pet)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun savePet(pet: UserPet) {
        val petMap = petToMap(pet)
        if (pet.id <= 0L) {
            // New pet - add with auto-generated ID
            val docRef = petsRef().add(petMap).await()
        } else {
            // Existing pet - find and update
            val petDocId = findPetDocId(pet.id)
            petsRef().document(petDocId).set(petMap).await()
        }
    }

    suspend fun deletePet(petId: Long) {
        val petDocId = findPetDocId(petId)
        // Delete sub-collections first
        deleteVaccinationsForPet(petId)
        deleteMedicalReportsForPet(petId)
        petsRef().document(petDocId).delete().await()
    }

    fun getVaccinationsForPet(petId: Long): Flow<List<VaccinationRecord>> = callbackFlow {
        val petDocId = findPetDocId(petId)
        val subscription = petsRef().document(petDocId)
            .collection("vaccinations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val records = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VaccinationRecord::class.java)?.copy(id = doc.id.hashCode().toLong())
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addVaccination(petId: Long, record: VaccinationRecord) {
        val petDocId = findPetDocId(petId)
        val recordMap = mapOf(
            "petId" to petId,
            "vaccineName" to record.vaccineName,
            "dateGiven" to record.dateGiven,
            "nextDueDate" to record.nextDueDate,
            "status" to record.status,
            "veterinarian" to record.veterinarian,
            "batchNumber" to record.batchNumber
        )
        petsRef().document(petDocId).collection("vaccinations").add(recordMap).await()
    }

    suspend fun updateVaccinationStatus(petId: Long, recordId: Long, newStatus: String) {
        val petDocId = findPetDocId(petId)
        val vaxDocId = findVaxDocId(petId, recordId)
        petsRef().document(petDocId).collection("vaccinations").document(vaxDocId)
            .update("status", newStatus).await()
    }

    private suspend fun deleteVaccinationsForPet(petId: Long) {
        val petDocId = findPetDocId(petId)
        val snapshot = petsRef().document(petDocId).collection("vaccinations").get().await()
        snapshot.documents.forEach { doc -> doc.reference.delete().await() }
    }

    fun getMedicalReportsForPet(petId: Long): Flow<List<MedicalReport>> = callbackFlow {
        val petDocId = findPetDocId(petId)
        val subscription = petsRef().document(petDocId)
            .collection("medical_reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MedicalReport::class.java)?.copy(id = doc.id.hashCode().toLong())
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addMedicalReport(petId: Long, report: MedicalReport) {
        val petDocId = findPetDocId(petId)
        val reportMap = mapOf(
            "petId" to petId,
            "title" to report.title,
            "clinicName" to report.clinicName,
            "date" to report.date,
            "diagnosis" to report.diagnosis,
            "prescription" to report.prescription
        )
        petsRef().document(petDocId).collection("medical_reports").add(reportMap).await()
    }

    private suspend fun deleteMedicalReportsForPet(petId: Long) {
        val petDocId = findPetDocId(petId)
        val snapshot = petsRef().document(petDocId).collection("medical_reports").get().await()
        snapshot.documents.forEach { doc -> doc.reference.delete().await() }
    }

    // Helper: find Firestore doc ID for a pet based on Long ID
    private var petIdCache = mutableMapOf<Long, String>()

    private suspend fun findPetDocId(petId: Long): String {
        petIdCache[petId]?.let { return it }
        val snapshot = petsRef().get().await()
        // Try to find by matching - we store the long id as a field
        for (doc in snapshot.documents) {
            val storedId = doc.getLong("localId") ?: 0L
            if (storedId == petId) {
                petIdCache[petId] = doc.id
                return doc.id
            }
        }
        // If not found, return petId as string (for new pets)
        val fallback = petId.toString()
        petIdCache[petId] = fallback
        return fallback
    }

    private var vaxIdCache = mutableMapOf<Long, String>()

    private suspend fun findVaxDocId(petId: Long, recordId: Long): String {
        vaxIdCache[recordId]?.let { return it }
        val petDocId = findPetDocId(petId)
        val snapshot = petsRef().document(petDocId).collection("vaccinations").get().await()
        for (doc in snapshot.documents) {
            val storedId = doc.getLong("localId") ?: 0L
            if (storedId == recordId) {
                vaxIdCache[recordId] = doc.id
                return doc.id
            }
        }
        val fallback = recordId.toString()
        vaxIdCache[recordId] = fallback
        return fallback
    }

    private fun petToMap(pet: UserPet): Map<String, Any> {
        return mapOf(
            "localId" to pet.id,
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
            "notes" to pet.notes
        )
    }
}

