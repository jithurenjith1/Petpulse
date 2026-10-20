package com.petpulse.app.data.model

data class KeralaCity(
    val id: String,
    val name: String,
    val district: String,
    val deliveryHub: String,
    val expressAvailable: Boolean = true
)

enum class MarketplaceCategory {
    PET_LISTINGS,
    FOOD,
    MEDICINES,
    GROOMING_SERVICES,
    VET_CONSULTATIONS,
    ACCESSORIES,
    TRAINING
}

/**
 * A lost-pet SOS alert shown in the admin panel (Firestore "lost_pet_alerts").
 * Field names match the document keys written by postSosAlert().
 */
data class AdminLostPetAlert(
    val id: String = "",
    val petName: String = "",
    val species: String = "",
    val breed: String = "",
    val location: String = "",
    val reward: String = "",
    val contactPhone: String = "",
    // Owner's alternate contact number (Firestore: "alternatePhone").
    val alternatePhone: String = "",
    val date: String = "",
    // True when the owner's Care/Premium plan boosted this alert. Defaulted so
    // Firestore reads keep working for documents that predate the field.
    val priority: Boolean = false
)

data class MarketPet(
    val id: String,
    val name: String,
    val species: String, // Dog, Cat, Bird, Reptile, Rabbit
    val breed: String,
    val age: String,
    val gender: String,
    val city: String, // Kochi, Trivandrum, Kozhikode, Thrissur
    val isImportedExotic: Boolean,
    val importCountry: String? = null,
    val listingType: String, // "Sale" or "Adoption"
    val priceInr: Double, // 0.0 for adoption
    val originalPriceInr: Double? = null,
    val isVaccinated: Boolean = true,
    val isMicrochipped: Boolean = true,
    val certificationDetails: String = "Kerala Kennel / Quarantine Verified",
    val sellerName: String,
    val sellerPhone: String = "",
    val isVerifiedBreeder: Boolean = true,
    val description: String,
    val temperament: String = "Playful & Healthy",
    val photoUris: List<String> = emptyList(), // multiple photos per listing
    // Cloudinary URLs of the listing photos (empty = fall back to photoUris).
    // Defaulted so Firestore toObject() keeps working for listings that predate
    // this field.
    val photoUrls: List<String> = emptyList(),
    val ownerId: String = "",
    val createdAt: Long = 0L
)

data class MarketProduct(
    val id: String,
    val name: String,
    val brand: String,
    val category: String, // "Dry Kibble", "Wet Food", "Fresh Meat", "Treats", "Dewormer", "Tick & Flea", "Antibiotics", "Supplements"
    val isMedicine: Boolean = false,
    val prescriptionRequired: Boolean = false,
    val packSize: String, // "3 kg", "100 ml", "10 Tablets"
    val priceInr: Double,
    val originalPriceInr: Double,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val stockCount: Int = 25,
    val description: String,
    val keyBenefits: List<String> = emptyList(),
    val dosageOrUsage: String = "As recommended on packaging",
    val petType: String = "All",
    val foodType: String = "All",
    val lifeStage: String = "All",
    val photoUris: List<String> = emptyList(),
    // Cloudinary URLs of the product photos (empty = fall back to photoUris).
    val photoUrls: List<String> = emptyList(),
    // Sub-category chip for the Accessories section (Toys / Bed / Clothing /
    // Bowls / Cage & Carrier / Others). Defaulted so Firestore toObject() and
    // existing constructions keep working.
    val subCategory: String = ""
)

data class GroomingServiceItem(
    val id: String,
    val title: String,
    val subTitle: String,
    val isInHomeVan: Boolean = true,
    val durationMinutes: Int = 60,
    val priceInr: Double,
    val originalPriceInr: Double,
    val availableCities: List<String> = listOf("Kochi", "Trivandrum", "Kozhikode", "Thrissur"),
    val perks: List<String>,
    val description: String,
    val rating: Double = 4.9
)

data class VerifiedDoctor(
    val id: String,
    val name: String,
    val degrees: String, // "BVSc & AH, MVSc Surgery"
    val ksvcRegNumber: String, // "KSVC/2018/4821"
    val specialization: String, // "Canine & Feline Surgeon", "Avian & Exotic Specialist"
    val experienceYears: Int,
    val clinicName: String,
    val clinicCity: String,
    val clinicAddress: String,
    val videoConsultFeeInr: Double = 399.0,
    val inPersonConsultFeeInr: Double = 599.0,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val availableDays: String = "Mon - Sat (9:00 AM - 7:00 PM)",
    val phone: String = "",
    val isEmergencyAvailable: Boolean = true,
    val isOnline: Boolean = false,
    // Admin-controlled ID-verification. Defaults to false: a vet is shown as
    // ID-verified ONLY when the admin has actually set isVerified on the
    // document - never from a fabricated default. Defaults are mandatory for
    // Firestore toObject().
    val isVerified: Boolean = false
)

data class CartItem(
    val id: String,
    val itemId: String,
    val title: String,
    val subtitle: String,
    val priceInr: Double,
    val isMedicine: Boolean = false,
    val prescriptionRequired: Boolean = false,
    val quantity: Int = 1
)

enum class OrderStatus {
    ESCROW_FUNDED,
    MERCHANT_CONFIRMED,
    PACKED_DISPATCHED,
    OUT_FOR_DELIVERY,
    DELIVERED_RELEASED,
    CANCELLED
}

data class OrderTimelineEvent(
    val title: String,
    val description: String,
    val timestamp: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean = false
)

data class EscrowOrder(
    val orderId: String,
    val items: List<CartItem>,
    val subtotalInr: Double,
    val deliveryFeeInr: Double,
    // Plan discount actually applied to the order total (Care 10% / Premium 15%).
    // Defaulted so existing constructions and Firestore toObject() keep working.
    val discountInr: Double = 0.0,
    val ecoPackagingFeeInr: Double = 10.0,
    val totalInr: Double,
    val deliveryCity: String,
    val deliveryAddress: String,
    val customerName: String,
    val customerPhone: String,
    val paymentMethod: String, // "Cash on Delivery (COD)" — the app has no online payment SDK
    val isEscrowProtected: Boolean = false,
    val status: OrderStatus = OrderStatus.OUT_FOR_DELIVERY,
    val deliveryOtp: String = "",
    val deliveryRiderName: String = "",
    val deliveryRiderVehicle: String = "",
    val deliveryRiderPhone: String = "",
    val orderDate: String,
    val timeline: List<OrderTimelineEvent>
)

data class DoctorBooking(
    val bookingId: String,
    val doctorName: String,
    val doctorSpecialization: String,
    val clinicName: String,
    val city: String,
    val consultType: String, // "Video Consultation" or "In-Person Clinic Visit"
    val petName: String,
    val date: String,
    val timeSlot: String,
    val feeInr: Double,
    val status: String = "Confirmed",
    val meetingLinkOrAddress: String = ""
)


// ---------- Owner-managed shop (Firestore-backed) ----------

data class ShopProduct(
    val id: String,
    val name: String,
    val listType: String, // "Food", "Medicine", "Grooming"
    val category: String = "General",
    val priceInr: Double,
    val description: String = "",
    // --- Food categorisation (defaults keep Firestore toObject() working) ---
    val petType: String = "All",    // Dog / Cat / Other / All
    val foodType: String = "All",   // Dry / Wet / Treats / Supplements / All
    val lifeStage: String = "All",  // Puppy & Kitten / Adult / Senior / All
    // --- Sitter / walker trust (defaults keep Firestore toObject() working) ---
    val verified: Boolean = false,
    val photoUris: List<String> = emptyList(),
    // Cloudinary URLs of the product / sitter-place photos
    // (empty = fall back to photoUris).
    val photoUrls: List<String> = emptyList(),
    // Accessories sub-category chip (see MarketProduct.subCategory).
    // Defaulted so Firestore toObject() keeps working for older documents.
    val subCategory: String = ""
)

fun ShopProduct.toMarketProduct() = MarketProduct(
    id = id,
    name = name,
    brand = "Wagmiya",
    category = category.ifBlank { "General" },
    isMedicine = listType == "Medicine",
    packSize = "1 Unit",
    priceInr = priceInr,
    originalPriceInr = priceInr,
    description = description,
    petType = petType,
    foodType = foodType,
    lifeStage = lifeStage,
    photoUris = photoUris,
    photoUrls = photoUrls,
    subCategory = subCategory
)

data class Dealer(
    val id: String,
    val name: String,
    val phone: String,
    val city: String = "Kochi"
)

data class OrderItemSnap(
    val name: String,
    val priceInr: Double,
    val quantity: Int
)

data class AdminOrder(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val city: String,
    val items: List<OrderItemSnap>,
    val totalInr: Double,
    val status: String = "NEW",
    val dealerName: String = "",
    val dealerPhone: String = "",
    val createdAt: Long = 0L,
    // Optional accident-cover add-on chosen at checkout. Recorded on the order
    // document ("accidentCover") so the team can follow up and activate it via the
    // existing enquiry. Defaulted so Firestore toObject() keeps working for orders
    // that predate this field.
    val accidentCover: Boolean = false,
    // The signed-in customer's uid that placed the order (Firestore: "ownerId").
    // Used by the admin Customers list to group orders per real customer.
    // Defaulted so Firestore toObject() keeps working for documents that predate
    // this field.
    val ownerId: String = ""
)

/**
 * Service booking (doctor consultation or trainer on-demand request).
 * Stored in the Firestore "bookings" collection — same pattern as orders.
 */
data class ServiceBooking(
    val id: String,
    val type: String,            // "DOCTOR" | "TRAINER"
    val ownerId: String,
    val customerName: String,
    val customerPhone: String,
    val petName: String,
    val providerName: String,   // doctor name for doctor bookings ("" for trainer)
    val serviceInfo: String,    // "Video Consultation" / "In-Person Clinic Visit" / "Home Training Visit"
    val dateLabel: String,      // "Today" / "Tomorrow" / ...
    val slot: String,           // "10:30 AM - 11:00 AM"
    val notes: String,          // symptoms or training need
    val feeInr: Double,
    val status: String = "NEW", // NEW | CONFIRMED | COMPLETED | CANCELLED
    val assignedName: String = "",
    val assignedPhone: String = "",
    val createdAt: Long = 0L
)

// Help & Support ticket submitted by a user (admin reads/resolves in panel).
data class SupportTicket(
    val id: String = "",
    val category: String = "Other",
    val subject: String = "",
    val details: String = "",
    val contact: String = "",
    val createdAt: Long = 0L,
    val status: String = "New"
)

// Animal rescue / cruelty report submitted by a user (admin coordinates rescue).
data class RescueReport(
    val id: String = "",
    val animalType: String = "",
    val description: String = "",
    val location: String = "",
    val contact: String = "",
    val createdAt: Long = 0L,
    val status: String = "New"
)

// Business partner joining form / featured-plan request (admin reviews these).
data class PartnerApplication(
    val id: String = "",
    val kind: String = "Business Partner", // "Business Partner" | "Featured Plan"
    val name: String = "",
    val category: String = "",
    val city: String = "",
    val phone: String = "",
    val planName: String = "",
    val createdAt: Long = 0L,
    val status: String = "NEW"
)

/**
 * Admin-configured commission settings for a partner in the Wagmiya Partner Ledger.
 * Stored in the Firestore "partner_ledger" collection, keyed by a stable id built
 * from the partner's type, name and phone so a single partner keeps one record.
 * This is NOT a partner model (dealers/vets already exist) - it only holds the
 * admin's commission percent and the paid/unpaid dues flag. Every field has a
 * default so Firestore toObject() keeps working.
 */
data class PartnerCommission(
    val id: String = "",              // stable key: "TYPE|name|phone" (lowercased)
    val partnerName: String = "",
    val partnerType: String = "",      // DEALER | VET | SITTER | TRAINER | GROOMER | OTHER
    val phone: String = "",
    val commissionPercent: Double = 10.0,
    val duesPaid: Boolean = false,
    val updatedAt: Long = 0L
)

/**
 * A customer review for a product or a dealer / service provider.
 * Stored in the Firestore "reviews" collection. Every field carries a default so
 * Firestore toObject() keeps working for documents that predate a field.
 * productId is set for product reviews, dealerName for dealer reviews.
 */
data class Review(
    val id: String = "",
    val productId: String = "",
    val dealerName: String = "",
    val ownerId: String = "",
    val authorName: String = "",
    val rating: Int = 0,
    val text: String = "",
    val orderId: String = "",
    val createdAt: Long = 0L
)
