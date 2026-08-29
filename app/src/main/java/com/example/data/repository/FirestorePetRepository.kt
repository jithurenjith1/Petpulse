package com.example.data.repository

import android.util.Log
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

    // Map of pet Long ID -> Firestore doc ID
    private val petDocIdMap = mutableMapOf<Long, String>()
    private var nextLocalId = 1L

    fun getAllUserPets(): Flow<List<UserPet>> = callbackFlow {
        val subscription = petsRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestorePetRepo", "Error getting pets", error)
                close(error)
                return@addSnapshotListener
            }
            val pets = mutableListOf<UserPet>()
            var maxId = 0L
            snapshot?.documents?.forEach { doc ->
                val pet = doc.toObject(UserPet::class.java)
                if (pet != null) {
                    // Use a stable ID from the document
                    val stableId = doc.id.hashCode().toLong().let { 
                        if (it < 0) -it else it 
                    }
                    petDocIdMap[stableId] = doc.id
                    if (stableId > maxId) maxId = stableId
                    pets.add(pet.copy(id = stableId))
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
                    val stableId = doc.id.hashCode().toLong().let { 
                        if (it < 0) -it else it 
                    }
                    if (stableId == petId) {
                        petDocIdMap[petId] = doc.id
                        val pet = doc.toObject(UserPet::class.java)?.copy(id = petId)
                        if (pet != null) {
                            trySend(pet)
                            found = true
                        }
                        break
                    }
                }
                if (!found) trySend(null)
            } catch (e: Exception) {
                trySend(null)
            }
        } else {
            val subscription = petsRef().document(docId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val pet = snapshot?.toObject(UserPet::class.java)?.copy(id = petId)
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
            // Update existing pet
            petsRef().document(existingDocId).set(petMap).await()
        } else {
            // Check if this pet already has a doc (by localId field)
            val snapshot = petsRef().get().await()
            var found = false
            for (doc in snapshot.documents) {
                val stableId = doc.id.hashCode().toLong().let { 
                    if (it < 0) -it else it 
                }
                if (stableId == pet.id) {
                    petDocIdMap[pet.id] = doc.id
                    petsRef().document(doc.id).set(petMap).await()
                    found = true
                    break
                }
            }
            if (!found) {
                // Truly new pet - create new document
                val docRef = petsRef().add(petMap).await()
                val newStableId = docRef.id.hashCode().toLong().let { 
                    if (it < 0) -it else it 
                }
                petDocIdMap[newStableId] = docRef.id
            }
        }
    }

    suspend fun deletePet(petId: Long) {
        val docId = petDocIdMap[petId]
        if (docId != null) {
            // Delete sub-collections first
            deleteVaccinationsForPet(docId)
            deleteMedicalReportsForPet(docId)
            petsRef().document(docId).delete().await()
            petDocIdMap.remove(petId)
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
                if (error != null) { close(error); return@addSnapshotListener }
                val records = snapshot?.documents?.mapNotNull { doc ->
                    val vax = doc.toObject(VaccinationRecord::class.java)
                    vax?.copy(id = doc.id.hashCode().toLong().let { if (it < 0) -it else it })
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
            val stableId = doc.id.hashCode().toLong().let { if (it < 0) -it else it }
            if (stableId == recordId) {
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
                if (error != null) { close(error); return@addSnapshotListener }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    val report = doc.toObject(MedicalReport::class.java)
                    report?.copy(id = doc.id.hashCode().toLong().let { if (it < 0) -it else it })
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
            "notes" to pet.notes
        )
    }
}

