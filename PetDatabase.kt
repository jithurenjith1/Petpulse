package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.LostPetAlert
import com.example.data.model.MedicalReport
import com.example.data.model.PetListing
import com.example.data.model.UserPet
import com.example.data.model.VaccinationRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserPet::class,
        VaccinationRecord::class,
        MedicalReport::class,
        LostPetAlert::class,
        PetListing::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao

    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null

        fun getInstance(context: Context): PetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetDatabase::class.java,
                    "jane_pals_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial data asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).petDao()
                                populateInitialData(dao)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: PetDao) {
            // Default Inbuilt Pet "Jane"
            dao.insertOrUpdatePet(
                UserPet(
                    id = 1L,
                    name = "Jane",
                    species = "Dog",
                    breed = "Indie",
                    gender = "Female",
                    ageYears = 1,
                    ageMonths = 8,
                    weightKg = 14.5,
                    microchipNumber = "IND-9842-JANE",
                    hasCertificate = true,
                    certificateNumber = "IND-K9-884210-CERT",
                    certificateIssuedBy = "National Canine Registry & Health Authority",
                    certificateDate = "March 12, 2025",
                    favoriteFoods = "Boiled Chicken Breast, Salmon Kibble, Sweet Potato Chews, Peanut Butter Pops",
                    favoritePlays = "Frisbee Fetch, Tug of War Rope, Squeaky Rubber Duck, Agility Hurdle",
                    trainingStatus = "Basic Commands Mastered (Sit, Stay, Paw, Heel, Come)",
                    trainingLevel = "Basic",
                    avatarRes = "img_dog_jane",
                    notes = "Gentle, active Indie dog. Loves outdoor morning runs and playing fetch in the park."
                )
            )

            // Default Vaccinations
            dao.insertVaccination(
                VaccinationRecord(
                    id = 1L,
                    petId = 1L,
                    vaccineName = "Rabies Anti-Viral (3-Year)",
                    dateGiven = "Jan 10, 2025",
                    nextDueDate = "Jan 10, 2028",
                    status = "Completed",
                    veterinarian = "Dr. Sarah Adams, DVM",
                    batchNumber = "RAB-2025-99B"
                )
            )
            dao.insertVaccination(
                VaccinationRecord(
                    id = 2L,
                    petId = 1L,
                    vaccineName = "DHPP (Distemper, Hepatitis, Parvo, Parainfluenza)",
                    dateGiven = "Feb 14, 2025",
                    nextDueDate = "Feb 14, 2026",
                    status = "Completed",
                    veterinarian = "Dr. Sarah Adams, DVM",
                    batchNumber = "DHPP-884-X"
                )
            )
            dao.insertVaccination(
                VaccinationRecord(
                    id = 3L,
                    petId = 1L,
                    vaccineName = "Bordetella (Kennel Cough Booster)",
                    dateGiven = "Scheduled",
                    nextDueDate = "Sep 15, 2026",
                    status = "Upcoming",
                    veterinarian = "PetCare Central Clinic",
                    batchNumber = "PENDING-09"
                )
            )
            dao.insertVaccination(
                VaccinationRecord(
                    id = 4L,
                    petId = 1L,
                    vaccineName = "Annual Deworming & Heartworm Preventative",
                    dateGiven = "Scheduled",
                    nextDueDate = "Oct 05, 2026",
                    status = "Upcoming",
                    veterinarian = "PetCare Central Clinic",
                    batchNumber = "PENDING-10"
                )
            )

            // Medical Reports
            dao.insertMedicalReport(
                MedicalReport(
                    id = 1L,
                    petId = 1L,
                    title = "Annual Comprehensive Health Screening",
                    clinicName = "Metropolitan Pet Hospital",
                    date = "Jan 15, 2026",
                    diagnosis = "Excellent cardiovascular health, clear dental check, clear lungs and coat.",
                    prescription = "Daily Omega-3 Salmon Oil Supplement (5ml) & routine exercise."
                )
            )
            dao.insertMedicalReport(
                MedicalReport(
                    id = 2L,
                    petId = 1L,
                    title = "Post-Adoption Microchip & DNA Wellness",
                    clinicName = "Companion Health Center",
                    date = "Oct 20, 2025",
                    diagnosis = "Microchip ID scanned successfully. Indie lineage confirmed with robust natural immunity.",
                    prescription = "Balanced protein-rich kibbles and clean water."
                )
            )

            // Lost Pet Alerts (5km nearby community network)
            dao.insertLostPetAlert(
                LostPetAlert(
                    id = 1L,
                    petName = "Rocky",
                    species = "Dog",
                    breed = "Golden Retriever",
                    lastSeenLocation = "Maple Creek Park (Near North Gate)",
                    distanceKm = 1.8,
                    rewardAmount = "$300",
                    contactHelpline = "+1 (800) 555-PET-SOS",
                    reportedTime = "35 mins ago",
                    isResolved = false,
                    description = "Wearing a blue reflective collar with bell. Very friendly, responds to whistle.",
                    collarColor = "Blue Reflective Collar"
                )
            )
            dao.insertLostPetAlert(
                LostPetAlert(
                    id = 2L,
                    petName = "Luna",
                    species = "Cat",
                    breed = "Siamese Mix",
                    lastSeenLocation = "Greenwood Boulevard, Block C",
                    distanceKm = 3.2,
                    rewardAmount = "$150",
                    contactHelpline = "+1 (800) 555-PET-SOS",
                    reportedTime = "2 hours ago",
                    isResolved = false,
                    description = "Cream color coat with dark ears and blue eyes. Slightly timid.",
                    collarColor = "Pink Floral Collar"
                )
            )

            // Pet Listings (For Sale & Adoption)
            dao.insertPetListing(
                PetListing(
                    id = 1L,
                    petName = "Milo & Bella",
                    species = "Dog",
                    breed = "Indie Rescued Puppies",
                    age = "3 Months",
                    location = "City Rescue Shelter (2.5 km away)",
                    description = "Two loving rescued Indie siblings, vaccinated, dewormed, looking for warm forever home.",
                    contactNumber = "+1 (555) 789-0123",
                    listingType = "Adoption",
                    priceEstimate = "Free (Adoption Fee Waived)",
                    postedBy = "Hope Paws Rescue"
                )
            )
            dao.insertPetListing(
                PetListing(
                    id = 2L,
                    petName = "Oliver",
                    species = "Cat",
                    breed = "British Shorthair",
                    age = "5 Months",
                    location = "Westfield Pet Hub",
                    description = "Certified pedigree, litter-trained, playful demeanor with complete health certificate.",
                    contactNumber = "+1 (555) 890-1234",
                    listingType = "Sale",
                    priceEstimate = "$450 (Contact Partner Breeder)",
                    postedBy = "Blue Ribbon Felines"
                )
            )
            dao.insertPetListing(
                PetListing(
                    id = 3L,
                    petName = "Sunny & Skye",
                    species = "Birds",
                    breed = "Budgerigar Pair",
                    age = "8 Months",
                    location = "Eastside Aviary Community",
                    description = "Hand-tamed cheerful pair of budgies, includes spacious flight cage & toys.",
                    contactNumber = "+1 (555) 345-6789",
                    listingType = "Adoption",
                    priceEstimate = "Free for Pet Lovers",
                    postedBy = "Urban Wings Sanctuary"
                )
            )
        }
    }
}
