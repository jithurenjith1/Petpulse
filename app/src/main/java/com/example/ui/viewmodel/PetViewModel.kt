package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PetDatabase
import com.example.data.model.*
import com.example.data.repository.MarketplaceRepository
import com.example.data.repository.PetRepository
import com.example.data.repository.FirestorePetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class MainNavTab {
    MY_PETS,
    MARKETPLACE,
    EXPLORE_PETS,
    PARTNERS_SERVICES,
}

enum class ExploreSubTab {
    FOOD,
    ACCESSORIES,
    HEALTH_CARE,
    TRAINING
}

enum class PartnerSubTab {
    GROOMING_CENTERS,
    FOOD_SUBSCRIPTION,
    PET_BOARDING,
    FIND_MY_PET,
    SALE_AND_ADOPTION,
    NEWS_AND_EVENTS
}

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PetRepository
    private val marketplaceRepo: MarketplaceRepository = MarketplaceRepository()
    private val firestoreRepo: FirestorePetRepository = FirestorePetRepository()

    init {
        val db = PetDatabase.getInstance(application)
        repository = PetRepository(db.petDao())
    }

    // Navigation and UI state
    private val _currentMainTab = MutableStateFlow(MainNavTab.MY_PETS)
    val currentMainTab: StateFlow<MainNavTab> = _currentMainTab.asStateFlow()

    private val _selectedSpecies = MutableStateFlow("dogs")
    val selectedSpecies: StateFlow<String> = _selectedSpecies.asStateFlow()

    private val _exploreSubTab = MutableStateFlow(ExploreSubTab.FOOD)
    val exploreSubTab: StateFlow<ExploreSubTab> = _exploreSubTab.asStateFlow()

    private val _foodCategory = MutableStateFlow("All")
    val foodCategory: StateFlow<String> = _foodCategory.asStateFlow()

    private val _accessoryCategory = MutableStateFlow("All")
    val accessoryCategory: StateFlow<String> = _accessoryCategory.asStateFlow()

    private val _partnerSubTab = MutableStateFlow(PartnerSubTab.GROOMING_CENTERS)
    val partnerSubTab: StateFlow<PartnerSubTab> = _partnerSubTab.asStateFlow()

    private val _boardingType = MutableStateFlow("Full Day (24hr)")
    val boardingType: StateFlow<String> = _boardingType.asStateFlow()

    // Customer profile state
    private val _customerProfile = MutableStateFlow(CustomerProfile())
    val customerProfile: StateFlow<CustomerProfile> = _customerProfile.asStateFlow()

    // ================= KERALA MARKETPLACE STATE =================
    private val _selectedKeralaCity = MutableStateFlow("All Kerala")
    val selectedKeralaCity: StateFlow<String> = _selectedKeralaCity.asStateFlow()

    private val _marketplaceCategory = MutableStateFlow(MarketplaceCategory.PET_LISTINGS)
    val marketplaceCategory: StateFlow<MarketplaceCategory> = _marketplaceCategory.asStateFlow()
    val marketCategory: StateFlow<MarketplaceCategory> = _marketplaceCategory.asStateFlow()

    private val _isExoticsFilterOnly = MutableStateFlow(false)
    val isExoticsFilterOnly: StateFlow<Boolean> = _isExoticsFilterOnly.asStateFlow()
    val isExoticsOnly: StateFlow<Boolean> = _isExoticsFilterOnly.asStateFlow()

    private val _marketPetSpeciesFilter = MutableStateFlow("All")
    val marketPetSpeciesFilter: StateFlow<String> = _marketPetSpeciesFilter.asStateFlow()
    val marketSpeciesFilter: StateFlow<String> = _marketPetSpeciesFilter.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isExpressDelivery = MutableStateFlow(false)
    val isExpressDelivery: StateFlow<Boolean> = _isExpressDelivery.asStateFlow()

    private val _activeOrders = MutableStateFlow<List<EscrowOrder>>(marketplaceRepo.getInitialEscrowOrders())
    val activeOrders: StateFlow<List<EscrowOrder>> = _activeOrders.asStateFlow()
    val escrowOrders: StateFlow<List<EscrowOrder>> = _activeOrders.asStateFlow()

    private val _doctorBookings = MutableStateFlow<List<DoctorBooking>>(marketplaceRepo.getInitialDoctorBookings())
    val doctorBookings: StateFlow<List<DoctorBooking>> = _doctorBookings.asStateFlow()

    private val _marketPetsList = MutableStateFlow<List<MarketPet>>(marketplaceRepo.getMarketPets())
    val marketPets: StateFlow<List<MarketPet>> = combine(
        _marketPetsList,
        _selectedKeralaCity,
        _isExoticsFilterOnly,
        _marketPetSpeciesFilter
    ) { allPets, city, exoticsOnly, speciesFilter ->
        allPets.filter { pet ->
            val matchesCity = city == "All Kerala" || pet.city.equals(city, ignoreCase = true)
            val matchesExotic = if (exoticsOnly) pet.isImportedExotic else true
            val matchesSpecies = if (speciesFilter == "All") true else pet.species.equals(speciesFilter, ignoreCase = true)
            matchesCity && matchesExotic && matchesSpecies
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), marketplaceRepo.getMarketPets())
    val filteredMarketPets: StateFlow<List<MarketPet>> = marketPets

    val keralaCities: StateFlow<List<KeralaCity>> = flowOf(marketplaceRepo.getKeralaCities())
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getKeralaCities())

    val marketFoods: StateFlow<List<MarketProduct>> = flowOf(marketplaceRepo.getMarketFoods())
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getMarketFoods())

    val marketMedicines: StateFlow<List<MarketProduct>> = flowOf(marketplaceRepo.getMarketMedicines())
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getMarketMedicines())

    val groomingServices: StateFlow<List<GroomingServiceItem>> = flowOf(marketplaceRepo.getGroomingServices())
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getGroomingServices())

    private val _verifiedDoctorsList = MutableStateFlow<List<VerifiedDoctor>>(marketplaceRepo.getVerifiedDoctors())
    val verifiedDoctors: StateFlow<List<VerifiedDoctor>> = combine(
        _verifiedDoctorsList,
        _selectedKeralaCity
    ) { doctors, city ->
        if (city == "All Kerala") doctors
        else doctors.filter { it.clinicCity.equals(city, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), marketplaceRepo.getVerifiedDoctors())

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.priceInr * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Multi-pet support
    private val _activePetId = MutableStateFlow(1L)
    val activePetId: StateFlow<Long> = _activePetId.asStateFlow()

    val allPets: StateFlow<List<UserPet>> = firestoreRepo.getAllUserPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active pet - dynamically based on _activePetId
    val activePet: StateFlow<UserPet> = _activePetId
        .flatMapLatest { petId -> firestoreRepo.getPetById(petId) }
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPet()
        )

    val vaccinations: StateFlow<List<VaccinationRecord>> = _activePetId
        .flatMapLatest { petId -> firestoreRepo.getVaccinationsForPet(petId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val medicalReports: StateFlow<List<MedicalReport>> = _activePetId
        .flatMapLatest { petId -> firestoreRepo.getMedicalReportsForPet(petId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lostPetAlerts: StateFlow<List<LostPetAlert>> = repository.lostPetAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val petListings: StateFlow<List<PetListing>> = repository.petListings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val speciesList: StateFlow<List<SpeciesCategory>> = flowOf(repository.getSpeciesCategories())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getSpeciesCategories())

    val foodItems: StateFlow<List<FoodItem>> = _selectedSpecies.map { speciesId ->
        repository.getFoodForSpecies(speciesId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getFoodForSpecies("dogs"))

    val accessoryItems: StateFlow<List<AccessoryItem>> = _selectedSpecies.map { speciesId ->
        repository.getAccessoriesForSpecies(speciesId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getAccessoriesForSpecies("dogs"))

    val healthCareItems: StateFlow<List<HealthCareItem>> = _selectedSpecies.map { speciesId ->
        repository.getHealthCareForSpecies(speciesId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getHealthCareForSpecies("dogs"))

    val trainingGuides: StateFlow<List<TrainingGuide>> = _selectedSpecies.map { speciesId ->
        repository.getTrainingGuidesForSpecies(speciesId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getTrainingGuidesForSpecies("dogs"))

    val groomingCenters: StateFlow<List<GroomingCenter>> = flowOf(repository.getFeaturedGroomingCenters())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getFeaturedGroomingCenters())

    val foodSubscriptions: StateFlow<List<FoodSubscription>> = flowOf(repository.getFoodSubscriptionPlans())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getFoodSubscriptionPlans())

    val boardingSitters: StateFlow<List<BoardingSitter>> = flowOf(repository.getBoardingSitters())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getBoardingSitters())

    val petNews: StateFlow<PetNewsItem> = flowOf(repository.getPetNews())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getPetNews())

    val events: StateFlow<List<PetEventItem>> = flowOf(repository.getUpcomingEvents())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getUpcomingEvents())

    // Quick Stats Calculation
    val healthScore: StateFlow<Int> = vaccinations.map { list ->
        if (list.isEmpty()) 95
        else {
            val completed = list.count { it.status == "Completed" }
            val ratio = (completed.toFloat() / list.size.toFloat()) * 100
            ratio.toInt().coerceIn(60, 100)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 95)

    // Navigation setters
    fun setMainTab(tab: MainNavTab) {
        _currentMainTab.value = tab
    }

    fun selectSpecies(speciesId: String) {
        _selectedSpecies.value = speciesId
    }

    fun setExploreSubTab(subTab: ExploreSubTab) {
        _exploreSubTab.value = subTab
    }

    fun setFoodCategory(cat: String) {
        _foodCategory.value = cat
    }

    fun setAccessoryCategory(cat: String) {
        _accessoryCategory.value = cat
    }

    fun setPartnerSubTab(tab: PartnerSubTab) {
        _partnerSubTab.value = tab
    }

    fun setBoardingType(type: String) {
        _boardingType.value = type
    }

    // Marketplace setters & helpers
    fun setKeralaCity(city: String) {
        _selectedKeralaCity.value = city
    }

    fun selectKeralaCity(city: String) = setKeralaCity(city)

    fun setMarketCategory(category: MarketplaceCategory) {
        _marketplaceCategory.value = category
    }

    fun setMarketplaceCategory(category: MarketplaceCategory) = setMarketCategory(category)

    fun toggleExoticsFilter(onlyExotics: Boolean) {
        _isExoticsFilterOnly.value = onlyExotics
    }

    fun setMarketSpeciesFilter(species: String) {
        _marketPetSpeciesFilter.value = species
    }

    fun setMarketPetSpeciesFilter(species: String) = setMarketSpeciesFilter(species)

    fun toggleExpressDelivery(enabled: Boolean) {
        _isExpressDelivery.value = enabled
    }

    // Cart Operations
    fun addProductToCart(product: MarketProduct) {
        addToCart(
            itemId = product.id,
            title = product.name,
            subtitle = "${product.brand} • ${product.packSize}",
            priceInr = product.priceInr,
            isMedicine = product.isMedicine,
            prescriptionRequired = product.prescriptionRequired
        )
    }

    fun updateCartItemQuantity(cartItemId: String, delta: Int) = updateCartQuantity(cartItemId, delta)

    fun listPetForSaleOrAdoption(
        name: String,
        species: String,
        breed: String,
        age: String,
        gender: String,
        city: String,
        isImportedExotic: Boolean,
        listingType: String,
        priceInr: Double,
        description: String,
        phone: String
    ) {
        submitOwnerMarketPetListing(
            name = name,
            species = species,
            breed = breed,
            age = age,
            gender = gender,
            city = city,
            isExotic = isImportedExotic,
            listingType = listingType,
            priceInr = priceInr,
            description = description,
            phone = phone
        )
    }

    fun registerVeterinarian(
        name: String,
        degrees: String,
        ksvcRegNumber: String,
        specialization: String,
        experienceYears: Int,
        clinicName: String,
        clinicCity: String,
        clinicAddress: String,
        videoConsultFeeInr: Double,
        inPersonConsultFeeInr: Double,
        phone: String
    ) {
        submitVetRegistration(
            name = name,
            degrees = degrees,
            ksvcNumber = ksvcRegNumber,
            specialization = specialization,
            experience = experienceYears,
            clinicName = clinicName,
            city = clinicCity,
            address = clinicAddress,
            videoFee = videoConsultFeeInr,
            inPersonFee = inPersonConsultFeeInr,
            phone = phone
        )
    }

    fun bookDoctorConsultation(
        doctor: VerifiedDoctor,
        consultType: String,
        petName: String,
        date: String,
        slot: String
    ): DoctorBooking = bookDoctorAppointment(doctor, consultType, petName, date, slot)

    // Cart Operations
    fun addToCart(
        itemId: String,
        title: String,
        subtitle: String,
        priceInr: Double,
        isMedicine: Boolean = false,
        prescriptionRequired: Boolean = false
    ) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.itemId == itemId }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(
                CartItem(
                    id = "cart_${System.currentTimeMillis()}_${(100..999).random()}",
                    itemId = itemId,
                    title = title,
                    subtitle = subtitle,
                    priceInr = priceInr,
                    isMedicine = isMedicine,
                    prescriptionRequired = prescriptionRequired,
                    quantity = 1
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(cartItemId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == cartItemId }
        if (index >= 0) {
            val updatedQty = current[index].quantity + delta
            if (updatedQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = updatedQty)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(cartItemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.id == cartItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun calculateDeliveryFee(subtotal: Double, city: String, express: Boolean): Double {
        if (subtotal <= 0.0) return 0.0
        val baseFee = when (city.lowercase()) {
            "kochi" -> if (subtotal >= 499.0) 0.0 else 35.0
            "thrissur" -> if (subtotal >= 499.0) 0.0 else 40.0
            "trivandrum" -> if (subtotal >= 599.0) 0.0 else 45.0
            "kozhikode" -> if (subtotal >= 599.0) 0.0 else 50.0
            else -> if (subtotal >= 599.0) 0.0 else 49.0
        }
        return if (express) baseFee + 50.0 else baseFee
    }

    // Escrow Order Placement
    fun placeEscrowOrder(
        city: String,
        address: String,
        name: String,
        phone: String,
        paymentMethod: String
    ): EscrowOrder {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.priceInr * it.quantity }
        val deliveryFee = calculateDeliveryFee(subtotal, city, _isExpressDelivery.value)
        val total = subtotal + deliveryFee + 10.0 // 10 eco charge
        val randomOtp = (1000..9999).random().toString()
        val orderNum = (10000..99999).random()

        val newOrder = EscrowOrder(
            orderId = "ORD-KL-$orderNum",
            items = items,
            subtotalInr = subtotal,
            deliveryFeeInr = deliveryFee,
            ecoPackagingFeeInr = 10.0,
            totalInr = total,
            deliveryCity = city.ifBlank { "Kochi" },
            deliveryAddress = address.ifBlank { "Doorstep, Kerala" },
            customerName = name.ifBlank { _customerProfile.value.name },
            customerPhone = phone.ifBlank { _customerProfile.value.phone },
            paymentMethod = paymentMethod,
            isEscrowProtected = true,
            status = OrderStatus.MERCHANT_CONFIRMED,
            deliveryOtp = randomOtp,
            deliveryRiderName = "Sreejith K. ($city Hub)",
            deliveryRiderVehicle = "KL-07-CB-4412",
            deliveryRiderPhone = "+91 98471 99221",
            orderDate = "Just now",
            timeline = listOf(
                OrderTimelineEvent(
                    title = "Order Placed & Escrow Secured",
                    description = "₹${total.toInt()} held in Jane & Pals Kerala Escrow Shield.",
                    timestamp = "Just now",
                    isCompleted = true
                ),
                OrderTimelineEvent(
                    title = "Confirmed by $city Partner Hub",
                    description = "Order accepted and packing started.",
                    timestamp = "1 min ago",
                    isCompleted = true,
                    isCurrent = true
                ),
                OrderTimelineEvent(
                    title = "Packed & Sealed with Quality Seal",
                    description = "Safe transit with tamper-evident packaging.",
                    timestamp = "Upcoming",
                    isCompleted = false
                ),
                OrderTimelineEvent(
                    title = "Out for Doorstep Delivery",
                    description = "Live rider assignment. Share OTP $randomOtp upon inspection.",
                    timestamp = "Estimated in 45-60 mins",
                    isCompleted = false
                ),
                OrderTimelineEvent(
                    title = "Delivered & Escrow Released",
                    description = "Seller paid only after customer confirms satisfaction.",
                    timestamp = "Pending Delivery",
                    isCompleted = false
                )
            )
        )

        val updatedOrders = listOf(newOrder) + _activeOrders.value
        _activeOrders.value = updatedOrders
        clearCart()
        return newOrder
    }

    // Doctor Consultation Booking
    fun bookDoctorAppointment(
        doctor: VerifiedDoctor,
        consultType: String,
        petName: String,
        date: String,
        slot: String
    ): DoctorBooking {
        val fee = if (consultType.contains("Video")) doctor.videoConsultFeeInr else doctor.inPersonConsultFeeInr
        val booking = DoctorBooking(
            bookingId = "BKG-VET-${(1000..9999).random()}",
            doctorName = doctor.name,
            doctorSpecialization = doctor.specialization,
            clinicName = doctor.clinicName,
            city = doctor.clinicCity,
            consultType = consultType,
            petName = petName.ifBlank { activePet.value.name },
            date = date.ifBlank { "Tomorrow" },
            timeSlot = slot.ifBlank { "10:30 AM - 11:00 AM" },
            feeInr = fee,
            status = "Confirmed",
            meetingLinkOrAddress = if (consultType.contains("Video")) "https://meet.google.com/jp-vet-${doctor.clinicCity.lowercase()}" else doctor.clinicAddress
        )
        _doctorBookings.value = listOf(booking) + _doctorBookings.value
        return booking
    }

    // List Pet by Owner
    fun submitOwnerMarketPetListing(
        name: String,
        species: String,
        breed: String,
        age: String,
        gender: String,
        city: String,
        isExotic: Boolean,
        listingType: String,
        priceInr: Double,
        description: String,
        phone: String
    ) {
        val newMarketPet = MarketPet(
            id = "pet_owner_${System.currentTimeMillis()}",
            name = name.ifBlank { "Buddy" },
            species = species.ifBlank { "Dog" },
            breed = breed.ifBlank { "Breed Not Specified" },
            age = age.ifBlank { "3 Months" },
            gender = gender.ifBlank { "Male" },
            city = city.ifBlank { "Kochi" },
            isImportedExotic = isExotic,
            importCountry = if (isExotic) "Exotic / Imported" else null,
            listingType = listingType,
            priceInr = if (listingType == "Adoption") 0.0 else priceInr,
            isVaccinated = true,
            isMicrochipped = true,
            certificationDetails = "Kerala Pet Health Card Verified",
            sellerName = _customerProfile.value.name,
            sellerPhone = phone.ifBlank { _customerProfile.value.phone },
            isVerifiedBreeder = false,
            description = description.ifBlank { "Loving and healthy pet looking for a wonderful home." }
        )
        _marketPetsList.value = listOf(newMarketPet) + _marketPetsList.value
    }

    // Vet Registration
    fun submitVetRegistration(
        name: String,
        degrees: String,
        ksvcNumber: String,
        specialization: String,
        experience: Int,
        clinicName: String,
        city: String,
        address: String,
        videoFee: Double,
        inPersonFee: Double,
        phone: String
    ) {
        val newVet = VerifiedDoctor(
            id = "vet_reg_${System.currentTimeMillis()}",
            name = if (name.startsWith("Dr.")) name else "Dr. $name",
            degrees = degrees.ifBlank { "BVSc & AH" },
            ksvcRegNumber = ksvcNumber.ifBlank { "KSVC/2024/${(1000..9999).random()}" },
            specialization = specialization.ifBlank { "Veterinary Physician" },
            experienceYears = if (experience <= 0) 5 else experience,
            clinicName = clinicName.ifBlank { "$name Pet Care Clinic" },
            clinicCity = city.ifBlank { "Kochi" },
            clinicAddress = address.ifBlank { "Main Road, $city, Kerala" },
            videoConsultFeeInr = if (videoFee <= 0) 349.0 else videoFee,
            inPersonConsultFeeInr = if (inPersonFee <= 0) 499.0 else inPersonFee,
            rating = 5.0,
            reviewsCount = 1,
            availableDays = "Mon - Sat (9:00 AM - 7:00 PM)",
            phone = phone.ifBlank { "+91 98470 00000" },
            isEmergencyAvailable = true
        )
        _verifiedDoctorsList.value = listOf(newVet) + _verifiedDoctorsList.value
    }

    // Business actions for My Pet
    fun renameAndConfigurePet(name: String, breed: String, ageYears: Int, gender: String) {
        viewModelScope.launch {
            val current = activePet.value
            val updated = current.copy(
                name = name.ifBlank { "Jane" },
                breed = breed.ifBlank { "Indie" },
                ageYears = ageYears,
                gender = gender.ifBlank { "Female" }
            )
            firestoreRepo.savePet(updated)
        }
    }

    fun updateFullPetDetails(
        name: String,
        breed: String,
        gender: String,
        ageYears: Int,
        ageMonths: Int,
        weightKg: Double,
        favoriteFoods: String,
        favoritePlays: String,
        trainingStatus: String,
        trainingLevel: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = activePet.value
            val updated = current.copy(
                name = name.ifBlank { "Jane" },
                breed = breed.ifBlank { "Indie" },
                gender = gender.ifBlank { "Female" },
                ageYears = ageYears,
                ageMonths = ageMonths,
                weightKg = weightKg,
                favoriteFoods = favoriteFoods,
                favoritePlays = favoritePlays,
                trainingStatus = trainingStatus,
                trainingLevel = trainingLevel,
                notes = notes
            )
            firestoreRepo.savePet(updated)
        }
    }

    fun updatePetFavoriteFoodsAndPlays(foods: String, plays: String) {
        viewModelScope.launch {
            val current = activePet.value
            val updated = current.copy(
                favoriteFoods = foods,
                favoritePlays = plays
            )
            firestoreRepo.savePet(updated)
        }
    }

    fun toggleVaccinationStatus(record: VaccinationRecord) {
        viewModelScope.launch {
            val newStatus = if (record.status == "Completed") "Upcoming" else "Completed"
            firestoreRepo.updateVaccinationStatus(activePet.value.id, record.id, newStatus)
        }
    }

    fun addVaccinationRecord(name: String, date: String, nextDue: String, status: String, doctor: String) {
        viewModelScope.launch {
            val vax = VaccinationRecord(
                petId = activePet.value.id,
                vaccineName = name,
                dateGiven = date,
                nextDueDate = nextDue,
                status = status,
                veterinarian = doctor,
                batchNumber = "VAX-${(1000..9999).random()}"
            )
            firestoreRepo.addVaccination(activePet.value.id, vax)
        }
    }

    fun addMedicalReport(title: String, clinic: String, diagnosis: String, prescription: String) {
        viewModelScope.launch {
            val report = MedicalReport(
                petId = activePet.value.id,
                title = title,
                clinicName = clinic,
                date = "Aug 25, 2026",
                diagnosis = diagnosis,
                prescription = prescription
            )
            firestoreRepo.addMedicalReport(activePet.value.id, report)
        }
    }

    fun broadcastLostPet(
        petName: String,
        species: String,
        breed: String,
        location: String,
        reward: String,
        phone: String,
        description: String
    ) {
        viewModelScope.launch {
            val alert = LostPetAlert(
                petName = petName,
                species = species,
                breed = breed,
                lastSeenLocation = location,
                distanceKm = 0.4,
                rewardAmount = reward.ifEmpty { "₹5,000" },
                contactHelpline = phone.ifEmpty { "+91 (800) 555-KERALA" },
                reportedTime = "Just now",
                isResolved = false,
                description = description
            )
            repository.reportLostPet(alert)
        }
    }

    fun addPetListing(
        petName: String,
        species: String,
        breed: String,
        age: String,
        location: String,
        listingType: String,
        price: String,
        description: String,
        phone: String
    ) {
        viewModelScope.launch {
            val listing = PetListing(
                petName = petName,
                species = species,
                breed = breed,
                age = age,
                location = location,
                description = description,
                contactNumber = phone,
                listingType = listingType,
                priceEstimate = if (listingType == "Adoption") "Free for Adoption" else price,
                postedBy = _customerProfile.value.name
            )
            repository.addPetListing(listing)
        }
    }

    fun updateCustomerProfile(name: String, email: String, phone: String) {
        _customerProfile.value = CustomerProfile(
            name = name.ifBlank { "Alex Morgan" },
            email = email.ifBlank { "alex.morgan@example.com" },
            phone = phone.ifBlank { "+91 98470 12345" },
            location = "Kochi, Kerala",
            isLoggedIn = true
        )
    }

    // ================= MULTI-PET SUPPORT =================
    
    fun switchPet(petId: Long) {
        _activePetId.value = petId
    }

    fun addNewPet(name: String, species: String, breed: String, gender: String, ageYears: Int, ageMonths: Int) {
        viewModelScope.launch {
            val newPet = UserPet(
                name = name.ifBlank { "New Pet" },
                species = species,
                breed = breed.ifBlank { "Mixed" },
                gender = gender.ifBlank { "Unknown" },
                ageYears = ageYears,
                ageMonths = ageMonths,
                weightKg = 0.0,
                microchipNumber = "",
                hasCertificate = false,
                certificateNumber = "",
                certificateIssuedBy = "",
                certificateDate = "",
                favoriteFoods = "",
                favoritePlays = "",
                trainingStatus = "",
                trainingLevel = "Basic",
                avatarRes = "img_dog_jane",
                notes = ""
            )
            firestoreRepo.savePet(newPet)
            // Wait briefly for Firestore to sync, then switch to newest pet
            kotlinx.coroutines.delay(500)
            val pets = firestoreRepo.getAllUserPets().first()
            _activePetId.value = pets.lastOrNull()?.id ?: 1L
        }
    }

    fun deleteCurrentPet() {
        viewModelScope.launch {
            val currentId = activePet.value.id
            firestoreRepo.deletePet(currentId)
            // Switch to first available pet
            val pets = firestoreRepo.getAllUserPets().first()
            _activePetId.value = pets.firstOrNull()?.id ?: 1L
        }
    }

}



