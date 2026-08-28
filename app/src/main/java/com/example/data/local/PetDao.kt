package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LostPetAlert
import com.example.data.model.MedicalReport
import com.example.data.model.PetListing
import com.example.data.model.UserPet
import com.example.data.model.VaccinationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM user_pets WHERE id = 1 LIMIT 1")
    fun getDefaultPet(): Flow<UserPet?>

    @Query("SELECT * FROM user_pets")
    fun getAllUserPets(): Flow<List<UserPet>>

    @Query("SELECT * FROM user_pets WHERE id = :petId LIMIT 1")
    fun getPetById(petId: Long): Flow<UserPet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: UserPet)

    @Update
    suspend fun updatePet(pet: UserPet)

    // Vaccination
    @Query("SELECT * FROM vaccination_records WHERE petId = :petId ORDER BY id ASC")
    fun getVaccinationsForPet(petId: Long): Flow<List<VaccinationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(record: VaccinationRecord)

    @Query("UPDATE vaccination_records SET status = :newStatus WHERE id = :id")
    suspend fun updateVaccinationStatus(id: Long, newStatus: String)

    // Medical Reports
    @Query("SELECT * FROM medical_reports WHERE petId = :petId ORDER BY id DESC")
    fun getMedicalReportsForPet(petId: Long): Flow<List<MedicalReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalReport(report: MedicalReport)

    // Lost Pet SOS Alerts
    @Query("SELECT * FROM lost_pet_alerts ORDER BY id DESC")
    fun getAllLostPetAlerts(): Flow<List<LostPetAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostPetAlert(alert: LostPetAlert)

    // Pet Listings (Adoption / Sale)
    @Query("SELECT * FROM pet_listings ORDER BY id DESC")
    fun getAllPetListings(): Flow<List<PetListing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetListing(listing: PetListing)

    @Query("DELETE FROM user_pets WHERE id = :petId")
    suspend fun deletePet(petId: Long)

    @Query("DELETE FROM vaccination_records WHERE petId = :petId")
    suspend fun deleteVaccinationsForPet(petId: Long)

    @Query("DELETE FROM medical_reports WHERE petId = :petId")
    suspend fun deleteMedicalReportsForPet(petId: Long)
}

