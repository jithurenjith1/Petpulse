package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_pets")
data class UserPet(
    @PrimaryKey(autoGenerate = true) val id: Long = 1L,
    val name: String = "Jane",
    val species: String = "Dog",
    val breed: String = "Indie",
    val gender: String = "Female",
    val ageYears: Int = 1,
    val ageMonths: Int = 8,
    val weightKg: Double = 14.5,
    val microchipNumber: String = "IND-9842-JANE",
    val hasCertificate: Boolean = true,
    val certificateNumber: String = "CERT-KC-884210",
    val certificateIssuedBy: String = "Kennel Club & Pet Registry Council",
    val certificateDate: String = "Jan 15, 2025",
    val favoriteFoods: String = "Boiled Chicken Breast, Salmon Kibble, Sweet Potato Chews, Peanut Butter Pops",
    val favoritePlays: String = "Frisbee Fetch, Tug of War, Squeaky Duck Toy, Agility Jump",
    val trainingStatus: String = "Basic Completed (Sit, Stay, Paw, Heel)",
    val trainingLevel: String = "Basic", // Basic, Advanced, In Progress, Blank
    val avatarRes: String = "img_dog_jane",
    val notes: String = "Very energetic, friendly with children, loves morning park walks."
)

@Entity(tableName = "vaccination_records")
data class VaccinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long = 1L,
    val vaccineName: String,
    val dateGiven: String,
    val nextDueDate: String,
    val status: String, // "Completed" or "Upcoming"
    val veterinarian: String = "Dr. Sarah Adams (PetCare Clinic)",
    val batchNumber: String = "VAX-2025-08"
)

@Entity(tableName = "medical_reports")
data class MedicalReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long = 1L,
    val title: String,
    val clinicName: String,
    val date: String,
    val diagnosis: String,
    val prescription: String,
    val followUpDate: String? = null
)

@Entity(tableName = "lost_pet_alerts")
data class LostPetAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petName: String,
    val species: String,
    val breed: String,
    val lastSeenLocation: String,
    val distanceKm: Double,
    val rewardAmount: String = "$250",
    val contactHelpline: String = "+1 (800) 555-PET-SOS",
    val reportedTime: String = "20 mins ago",
    val isResolved: Boolean = false,
    val description: String,
    val collarColor: String = "Blue Tag Collar"
)

@Entity(tableName = "pet_listings")
data class PetListing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petName: String,
    val species: String,
    val breed: String,
    val age: String,
    val location: String,
    val description: String,
    val contactNumber: String,
    val listingType: String, // "Adoption" or "Sale"
    val priceEstimate: String = "Free for Adoption",
    val postedBy: String = "Community Member"
)

data class CustomerProfile(
    val name: String = "Alex Morgan",
    val email: String = "alex.morgan@example.com",
    val phone: String = "+1 (555) 019-2834",
    val location: String = "Downtown Metro, Sector 4",
    val isLoggedIn: Boolean = true,
    val memberSince: String = "2024"
)

data class SpeciesCategory(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String
)

data class FoodItem(
    val name: String,
    val subType: String, // "Dry Food", "Wet Food", "Treats"
    val description: String,
    val recommendedPortion: String,
    val estimatedPrice: String,
    val rating: Double = 4.8
)

data class AccessoryItem(
    val name: String,
    val subType: String, // "Clothing", "Toys", "Wearables", "Other"
    val description: String,
    val estimatedPrice: String,
    val material: String
)

data class HealthCareItem(
    val title: String,
    val subType: String, // "Grooming", "Vaccination", "Checkups", "Treatments"
    val description: String,
    val frequencyOrTimeline: String,
    val estimatedCost: String
)

data class TrainingGuide(
    val title: String,
    val level: String, // "Basic", "Advanced"
    val steps: List<String>,
    val tips: String,
    val recommendedAge: String
)

data class GroomingCenter(
    val name: String,
    val tagLine: String,
    val address: String,
    val distance: String,
    val rating: Double,
    val reviewCount: Int,
    val packages: List<String>,
    val startingPrice: String,
    val phone: String,
    val isFeaturedPartner: Boolean = true
)

data class FoodSubscription(
    val title: String,
    val planType: String, // "Monthly" or "Yearly"
    val comboContents: String,
    val brandsIncluded: String,
    val monthlyEstimate: String,
    val savingsTag: String
)

data class BoardingSitter(
    val name: String,
    val sitterType: String, // "Full Day (24hr)", "Per Day Care", "Pet Night Care", "Feed on Time Only"
    val tagline: String,
    val experience: String,
    val rating: Double,
    val priceEstimate: String,
    val features: List<String>,
    val verified: Boolean = true
)

data class PetNewsItem(
    val title: String,
    val source: String,
    val timeAgo: String,
    val summary: String,
    val fullContent: String
)

data class PetEventItem(
    val title: String,
    val category: String,
    val date: String,
    val location: String,
    val entryStatus: String,
    val prizePool: String
)
