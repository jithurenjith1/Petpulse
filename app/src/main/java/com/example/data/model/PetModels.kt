package com.petpulse.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_pets")
data class UserPet(
    // autoGenerate primary key. Default 0L means a freshly-built UserPet is a
    // BRAND-NEW pet: Room generates a new row id (so new pets are APPENDED, never
    // replacing an existing row) and FirestorePetRepository.savePet creates a new
    // document. Existing pets carry their real (non-zero) id.
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
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
    val favoriteFoods: String = "",
    val favoritePlays: String = "",
    val trainingStatus: String = "Basic Completed (Sit, Stay, Paw, Heel)",
    val trainingLevel: String = "Basic", // Basic, Advanced, In Progress, Blank
    val trainingMilestones: String = "Sit, Stay (30s), Paw / High Five, Heel Walk, Emergency Recall",
    val avatarRes: String = "img_dog_jane",
    val photoUri: String = "",
    val notes: String = "Very energetic, friendly with children, loves morning park walks.",
    // Free-text result of the Premium "AI Pet Photo Analysis" feature. Defaulted so
    // both Room and Firestore toObject() keep working for existing documents.
    val aiAnalysis: String = ""
)

/** A real community post, stored in Firestore "community_posts". */
data class CommunityPost(
    val id: String = "",
    val ownerId: String = "",
    val authorName: String = "",
    val petName: String = "",
    val message: String = "",
    val createdAt: Long = 0L
)
data class PetCertificate(
    val id: Long = 0L,
    val petId: Long = 1L,
    val title: String = "",          // "Kennel Club Registration", "Vaccination Certificate"...
    val registrationId: String = "",
    val issuedBy: String = "",
    val issueDate: String = "",
    val photoPaths: List<String> = emptyList(), // local cache files of the stored photos
    val createdAt: Long = 0L
)

@Entity(tableName = "vaccination_records")
data class VaccinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long = 1L,
    val vaccineName: String = "",
    val dateGiven: String = "",
    val nextDueDate: String = "",
    val status: String = "", // "Completed" or "Upcoming"
    val veterinarian: String = "Dr. Sarah Adams (PetCare Clinic)",
    val batchNumber: String = "VAX-2025-08"
)

@Entity(tableName = "medical_reports")
data class MedicalReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long = 1L,
    val title: String = "",
    val clinicName: String = "",
    val date: String = "",
    val diagnosis: String = "",
    val prescription: String = "",
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
    val rewardAmount: String = "₹21,250",
    val contactHelpline: String = "+91 98 555-PET-SOS",
    val reportedTime: String = "20 mins ago",
    val isResolved: Boolean = false,
    val description: String,
    val collarColor: String = "Blue Tag Collar",
    // Second contact number supplied by the owner (Firestore: "alternatePhone").
    val alternatePhone: String = ""
)

/**
 * An "I saw this pet" sighting report submitted by any signed-in user for a
 * lost-pet alert. Stored in the Firestore "found_reports" collection.
 * Every field has a default so Firestore toObject() keeps working.
 */
data class FoundPetReport(
    val id: String = "",
    val alertId: String = "",
    val petName: String = "",
    val photoData: List<String> = emptyList(),
    val location: String = "",
    val finderPhone: String = "",
    val note: String = "",
    val createdAt: Long = 0L,
    val ownerNotified: Boolean = false
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
    val name: String = "Renjith Kumar",
    val email: String = "renjith@wagmiya.app",
    val phone: String = "+91 98470 00000",
    val location: String = "Marine Drive, Kochi",
    val isLoggedIn: Boolean = true,
    val memberSince: String = "2024",
    // Premium gating. "" = free, "care", "premium". Defaults are mandatory so
    // Firestore toObject() keeps working for documents that predate this field.
    val carePlan: String = "",
    // Optional plan expiry as an epoch-millis timestamp (0 = no expiry).
    val carePlanUntil: Long = 0L
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
    val isFeaturedPartner: Boolean = true,
    val verified: Boolean = true,
    val verificationId: String = ""
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
    val verified: Boolean = true,
    val photoUris: List<String> = emptyList(),
    val timeSlots: List<String> = emptyList()
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


