package com.example.data.model

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
    VET_CONSULTATIONS
}

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
    val sellerPhone: String = "+91 98470 12345",
    val isVerifiedBreeder: Boolean = true,
    val description: String,
    val temperament: String = "Playful & Healthy",
    val imageUrl: String = ""
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
    val rating: Double = 4.8,
    val reviewsCount: Int = 124,
    val stockCount: Int = 25,
    val description: String,
    val keyBenefits: List<String> = emptyList(),
    val dosageOrUsage: String = "As recommended on packaging"
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
    val rating: Double = 4.9,
    val reviewsCount: Int = 210,
    val availableDays: String = "Mon - Sat (9:00 AM - 7:00 PM)",
    val phone: String = "+91 94471 88200",
    val isEmergencyAvailable: Boolean = true
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
    val ecoPackagingFeeInr: Double = 10.0,
    val totalInr: Double,
    val deliveryCity: String,
    val deliveryAddress: String,
    val customerName: String,
    val customerPhone: String,
    val paymentMethod: String, // "Cash on Delivery (COD)", "UPI (GPay / PhonePe)", "Cards / Netbanking"
    val isEscrowProtected: Boolean = true,
    val status: OrderStatus = OrderStatus.OUT_FOR_DELIVERY,
    val deliveryOtp: String = "5824",
    val deliveryRiderName: String = "Sreejith K. (Kochi Hub)",
    val deliveryRiderVehicle: String = "KL-07-CB-4412",
    val deliveryRiderPhone: String = "+91 98471 99221",
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
    val meetingLinkOrAddress: String = "https://meet.google.com/jp-vet-kerala"
)
