package com.petpulse.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petpulse.app.data.local.PetDatabase
import com.petpulse.app.data.model.*
import com.petpulse.app.data.repository.MarketplaceRepository
import com.petpulse.app.data.repository.PetRepository
import com.petpulse.app.data.repository.FirestorePetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri
import com.petpulse.app.R
import com.petpulse.app.data.repository.FirestoreMarketplaceRepository
import com.petpulse.app.data.repository.FirestoreCommerceRepository

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
    private val firestoreMarketRepo = FirestoreMarketplaceRepository(application)
    private val commerceRepo = FirestoreCommerceRepository()

    init {
        val db = PetDatabase.getInstance(application)
        repository = PetRepository(db.petDao())

        // Live marketplace: every user sees the same Firestore-backed listings.
        viewModelScope.launch {
            firestoreMarketRepo.observeMarketPets().collect { remote ->
                _marketPetsList.value = remote
            }
        }
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

    private val _marketPetsList = MutableStateFlow<List<MarketPet>>(emptyList())

    // One-shot feedback after posting a listing (R.string id or null)
    private val _marketPostEvent = MutableStateFlow<Int?>(null)
    val marketPostEvent: StateFlow<Int?> = _marketPostEvent.asStateFlow()
    fun onMarketPostEventShown() { _marketPostEvent.value = null }

    // One-shot feedback for orders and admin actions (R.string id or null)
    private val _commerceEvent = MutableStateFlow<Int?>(null)
    val commerceEvent: StateFlow<Int?> = _commerceEvent.asStateFlow()
    fun onCommerceEventShown() { _commerceEvent.value = null }

    // Admin gate + admin data (non-admins simply see empty lists)
    val isAdmin: StateFlow<Boolean> = commerceRepo.observeIsAdmin()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val adminOrders: StateFlow<List<AdminOrder>> = commerceRepo.observeOrders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Live orders of the signed-in customer (My Orders screen)
    val myOrders: StateFlow<List<AdminOrder>> = commerceRepo.observeMyOrders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Live service bookings (doctor consults + trainer requests) — admin & customer
    val adminBookings: StateFlow<List<ServiceBooking>> = commerceRepo.observeBookings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val myBookings: StateFlow<List<ServiceBooking>> = commerceRepo.observeMyBookings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adminDealers: StateFlow<List<Dealer>> = commerceRepo.observeDealers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val shopProducts: StateFlow<List<ShopProduct>> = commerceRepo.observeProducts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
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

    val marketFoods: StateFlow<List<MarketProduct>> = commerceRepo.observeProducts()
        .map { remote -> remote.filter { it.listType == "Food" }.map { it.toMarketProduct() } + marketplaceRepo.getMarketFoods() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getMarketFoods())

    val marketMedicines: StateFlow<List<MarketProduct>> = commerceRepo.observeProducts()
        .map { remote -> remote.filter { it.listType == "Medicine" }.map { it.toMarketProduct() } + marketplaceRepo.getMarketMedicines() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getMarketMedicines())

    val groomingServices: StateFlow<List<GroomingServiceItem>> = commerceRepo.observeProducts()
        .map { remote ->
            remote.filter { it.listType == "Grooming" }.map {
                GroomingServiceItem(
                    id = it.id,
                    title = it.name,
                    subTitle = it.description.take(60),
                    priceInr = it.priceInr,
                    originalPriceInr = it.priceInr,
                    perks = emptyList(),
                    description = it.description
                )
            } + marketplaceRepo.getGroomingServices()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getGroomingServices())

    private val _verifiedDoctorsList = MutableStateFlow<List<VerifiedDoctor>>(marketplaceRepo.getVerifiedDoctors())

    /** Real partner vets added by the owner (Firestore "vets" collection). */
    val partnerVets: StateFlow<List<VerifiedDoctor>> = commerceRepo.observeVets()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // When partner vets exist they REPLACE the sample demo doctors; otherwise the
    // demo list is shown so the Healthcare tab is never empty. Fresh in-session vet
    // registrations are always kept on top.
    val verifiedDoctors: StateFlow<List<VerifiedDoctor>> = combine(
        _verifiedDoctorsList, partnerVets, _selectedKeralaCity
    ) { local, partners, city ->
        val base = if (partners.isEmpty()) local
        else partners + local.filter { it.id.startsWith("vet_reg_") }
        if (city == "All Kerala") base
        else base.filter { it.clinicCity.equals(city, ignoreCase = true) }
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
        phone: String,
        photos: List<String> = emptyList()
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
            phone = phone,
            photos = photos
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
    /** "Buy Again" — loads a past order's items back into the cart. */
    fun reorderFromOrder(order: AdminOrder) {
        val items = order.items.map { snap ->
            CartItem(
                id = "cart_reorder_${System.currentTimeMillis()}_${(100..999).random()}",
                itemId = "reorder_${snap.name.lowercase().replace(" ", "_")}",
                title = snap.name,
                subtitle = "",
                priceInr = snap.priceInr,
                quantity = snap.quantity
            )
        }
        if (items.isNotEmpty()) _cartItems.value = items
    }

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
        // v4 model: flat ₹40 delivery under ₹500, FREE at/above ₹500 (all Kerala cities)
        val baseFee = if (subtotal >= 500.0) 0.0 else 40.0
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
        val total = subtotal + deliveryFee
        val randomOtp = (1000..9999).random().toString()
        val orderNum = (10000..99999).random()

        val newOrder = EscrowOrder(
            orderId = "ORD-KL-$orderNum",
            items = items,
            subtotalInr = subtotal,
            deliveryFeeInr = deliveryFee,
            ecoPackagingFeeInr = 0.0,
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

        // Also save the REAL order to Firestore so the owner sees it in Admin
        viewModelScope.launch {
            val result = commerceRepo.placeOrder(
                orderNumber = newOrder.orderId,
                items = items.map { OrderItemSnap(it.title, it.priceInr, it.quantity) },
                totalInr = total,
                customerName = newOrder.customerName,
                customerPhone = newOrder.customerPhone,
                address = newOrder.deliveryAddress,
                city = newOrder.deliveryCity
            )
            _commerceEvent.value = when {
                result.exceptionOrNull()?.message == "NOT_SIGNED_IN" -> R.string.order_sign_in_to_place
                result.isSuccess -> R.string.order_placed_msg
                else -> R.string.order_failed_msg
            }
        }
        return newOrder
    }

    // ---------- Admin operations (Firestore rules enforce owner-only) ----------
    fun adminAssignDealer(orderId: String, dealer: Dealer) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.assignDealer(orderId, dealer).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.updateOrderStatus(orderId, status).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    // ---------- Service bookings (doctor + trainer) ----------

    /** Customer confirms a doctor consultation → real Firestore booking. */
    fun placeDoctorBooking(
        doctor: VerifiedDoctor,
        consultType: String,
        petName: String,
        customerPhone: String,
        date: String,
        slot: String,
        notes: String
    ) {
        val fee = if (consultType.contains("Video")) doctor.videoConsultFeeInr else doctor.inPersonConsultFeeInr
        val booking = ServiceBooking(
            id = "",
            type = "DOCTOR",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = customerPhone.ifBlank { _customerProfile.value.phone },
            petName = petName,
            providerName = doctor.name,
            serviceInfo = consultType,
            dateLabel = date,
            slot = slot,
            notes = notes,
            feeInr = fee,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Customer requests a trainer on-demand → real Firestore booking. */
    fun placeTrainerBooking(petName: String, customerPhone: String, trainingNeed: String) {
        val booking = ServiceBooking(
            id = "",
            type = "TRAINER",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = customerPhone.ifBlank { _customerProfile.value.phone },
            petName = petName,
            providerName = "",
            serviceInfo = "Home Training Visit",
            dateLabel = "",
            slot = "",
            notes = trainingNeed,
            feeInr = 0.0,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Admin assigns a doctor/trainer to a booking → status CONFIRMED. */
    fun adminAssignBooking(bookingId: String, name: String, phone: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.assignBooking(bookingId, name, phone).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminUpdateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.updateBookingStatus(bookingId, status).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminAddProduct(name: String, listType: String, category: String, priceInr: Double, description: String) {
        viewModelScope.launch {
            val p = ShopProduct(id = "", name = name, listType = listType, category = category, priceInr = priceInr, description = description)
            _commerceEvent.value = if (commerceRepo.addProduct(p).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteProduct(productId).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminAddDealer(name: String, phone: String, city: String) {
        viewModelScope.launch {
            val d = Dealer(id = "", name = name, phone = phone, city = city)
            _commerceEvent.value = if (commerceRepo.addDealer(d).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminDeleteDealer(dealerId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteDealer(dealerId).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    // ---------- Partner vet management (Admin panel → Vets tab) ----------

    fun adminAddVet(
        name: String,
        specialization: String,
        clinicName: String,
        city: String,
        phone: String,
        videoFee: Double,
        inPersonFee: Double
    ) {
        viewModelScope.launch {
            val vet = VerifiedDoctor(
                id = "",
                name = if (name.startsWith("Dr.")) name else "Dr. $name",
                degrees = "BVSc & AH",
                ksvcRegNumber = "",
                specialization = specialization.ifBlank { "Veterinary Physician" },
                experienceYears = 5,
                clinicName = clinicName.ifBlank { "$name Pet Care Clinic" },
                clinicCity = city.ifBlank { "Kochi" },
                clinicAddress = "${clinicName.ifBlank { "$name Pet Care Clinic" }}, ${city.ifBlank { "Kochi" }}, Kerala",
                videoConsultFeeInr = if (videoFee <= 0) 349.0 else videoFee,
                inPersonConsultFeeInr = if (inPersonFee <= 0) 499.0 else inPersonFee,
                phone = phone
            )
            _commerceEvent.value = if (commerceRepo.addVet(vet).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminDeleteVet(vetId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteVet(vetId).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
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
        phone: String,
        photos: List<String> = emptyList()
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
            description = description.ifBlank { "Loving and healthy pet looking for a wonderful home." },
            photoUris = photos
        )
        viewModelScope.launch {
            val photoUriList = photos.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            val result = firestoreMarketRepo.postListing(newMarketPet, photoUriList)
            _marketPostEvent.value = when {
                result.exceptionOrNull()?.message == "NOT_SIGNED_IN" -> R.string.market_sign_in_to_post
                result.isSuccess -> R.string.market_post_success
                else -> R.string.market_post_failed
            }
        }
    }

    /** Current Firebase user id (null when signed out) — used to tag "my listings". */
    val currentUid: String?
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    /** Removes one of MY market listings (Firestore rules enforce ownership). */
    fun deleteMyListing(pet: MarketPet) {
        viewModelScope.launch {
            firestoreMarketRepo.deleteListing(pet.id)
        }
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

    fun updatePetPhoto(photoUri: String) {
        viewModelScope.launch {
            val current = activePet.value
            val updated = current.copy(photoUri = photoUri)
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
            try {
                firestoreRepo.savePet(newPet)
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error saving new pet", e)
            }
            // Wait briefly for Firestore to sync, then switch to newest pet
            try {
                kotlinx.coroutines.delay(500)
                val pets = firestoreRepo.getAllUserPets().first()
                _activePetId.value = pets.lastOrNull()?.id ?: 1L
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error switching to new pet", e)
            }
        }
    }

    fun deleteCurrentPet() {
        viewModelScope.launch {
            try {
                val currentId = activePet.value.id
                firestoreRepo.deletePet(currentId)
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error deleting pet", e)
            }
            // Switch to first available pet
            try {
                val pets = firestoreRepo.getAllUserPets().first()
                _activePetId.value = pets.firstOrNull()?.id ?: 1L
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error switching after delete", e)
            }
        }
    }

}





