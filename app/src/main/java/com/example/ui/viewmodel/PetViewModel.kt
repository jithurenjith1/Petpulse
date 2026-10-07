package com.petpulse.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petpulse.app.data.local.PetDatabase
import com.petpulse.app.data.model.*
import com.petpulse.app.data.repository.MarketplaceRepository
import com.petpulse.app.data.repository.PetRepository
import com.petpulse.app.data.repository.FirestorePetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import android.net.Uri
import com.petpulse.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    NEWS_AND_EVENTS,
    SUPPORT
}

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PetRepository
    private val marketplaceRepo: MarketplaceRepository = MarketplaceRepository()
    private val firestoreRepo: FirestorePetRepository = FirestorePetRepository(getApplication())
    private val firestoreMarketRepo = FirestoreMarketplaceRepository(application)
    private val commerceRepo = FirestoreCommerceRepository(application)

    // ---- Theme preference (persisted in the existing "wagmiya_prefs" file) ----
    // Default false = light theme. Exposed as a StateFlow so MainActivity can
    // recompose the instant the user flips the Settings switch.
    private val themePrefs =
        getApplication<Application>().getSharedPreferences("wagmiya_prefs", Context.MODE_PRIVATE)
    private val _darkTheme = MutableStateFlow(themePrefs.getBoolean("dark_theme", false))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        themePrefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    init {
        val db = PetDatabase.getInstance(application)
        repository = PetRepository(db.petDao())

        // Live marketplace: every user sees the same Firestore-backed listings.
        viewModelScope.launch {
            firestoreMarketRepo.observeMarketPets().collect { remote ->
                _marketPetsList.value = remote
            }
        }

        // Pull the Premium gating fields (carePlan / carePlanUntil) from the
        // customer's users/{uid} document. Only the plan fields are taken from
        // Firestore so the local identity (name/email/phone) is never clobbered.
        viewModelScope.launch {
            firestoreRepo.observeCustomerProfile().collect { remote ->
                val current = _customerProfile.value
                _customerProfile.value = current.copy(
                    carePlan = remote.carePlan,
                    carePlanUntil = remote.carePlanUntil
                )
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

    // Food categorisation filters (Shop by pet / Food type / Life stage)
    private val _foodPetType = MutableStateFlow("All")
    val foodPetType: StateFlow<String> = _foodPetType.asStateFlow()
    private val _foodKind = MutableStateFlow("All")
    val foodKind: StateFlow<String> = _foodKind.asStateFlow()
    private val _foodStage = MutableStateFlow("All")
    val foodStage: StateFlow<String> = _foodStage.asStateFlow()

    private val _accessoryCategory = MutableStateFlow("All")
    val accessoryCategory: StateFlow<String> = _accessoryCategory.asStateFlow()

    private val _partnerSubTab = MutableStateFlow(PartnerSubTab.GROOMING_CENTERS)
    val partnerSubTab: StateFlow<PartnerSubTab> = _partnerSubTab.asStateFlow()

    private val _boardingType = MutableStateFlow("Full Day (24hr)")
    val boardingType: StateFlow<String> = _boardingType.asStateFlow()

    // Customer profile state
    private val _customerProfile = MutableStateFlow(CustomerProfile())
    val customerProfile: StateFlow<CustomerProfile> = _customerProfile.asStateFlow()

    /**
     * Premium gating. True when the customer's profile document (users/{uid})
     * carries carePlan == "premium". Derived from [customerProfile] so the whole
     * UI reacts the moment the plan changes.
     */
    val isPremium: StateFlow<Boolean> = customerProfile
        .map { it.carePlan.trim().equals("premium", ignoreCase = true) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Convenience synchronous check for call sites that cannot collect the flow. */
    fun isPremiumFeatureAllowed(): Boolean = isPremium.value

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

    private val _activeOrders = MutableStateFlow<List<EscrowOrder>>(emptyList())
    val activeOrders: StateFlow<List<EscrowOrder>> = _activeOrders.asStateFlow()
    val escrowOrders: StateFlow<List<EscrowOrder>> = _activeOrders.asStateFlow()

    private val _doctorBookings = MutableStateFlow<List<DoctorBooking>>(emptyList())
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

    // GPS tracker collars (admin-managed products with listType = "GPS")
    val gpsTrackers: StateFlow<List<ShopProduct>> = commerceRepo.observeProducts()
        .map { remote -> remote.filter { it.listType == "GPS" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Help & Support tickets and animal rescue reports (admin sees all)
    val supportTickets: StateFlow<List<SupportTicket>> = commerceRepo.observeSupportTickets()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rescueReports: StateFlow<List<RescueReport>> = commerceRepo.observeRescueReports()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // "I saw this pet" sighting reports. Admin sees all details; the public SOS
    // page only checks whether a report exists for an alert (never shows the finder).
    val foundReports: StateFlow<List<FoundPetReport>> = commerceRepo.observeFoundReports()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Partner applications (business join forms + featured plan requests) - admin reviews
    val partnerApplications: StateFlow<List<PartnerApplication>> = commerceRepo.observePartnerApplications()
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredMarketPets: StateFlow<List<MarketPet>> = marketPets

    val keralaCities: StateFlow<List<KeralaCity>> = flowOf(marketplaceRepo.getKeralaCities())
        .stateIn(viewModelScope, SharingStarted.Eagerly, marketplaceRepo.getKeralaCities())

    val marketFoods: StateFlow<List<MarketProduct>> = commerceRepo.observeProducts()
        .map { remote -> remote.filter { it.listType == "Food" }.map { it.toMarketProduct() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Food list filtered by the Shop-by-pet / Food-type / Life-stage chips. */
    val marketFoodsFiltered: StateFlow<List<MarketProduct>> =
        combine(commerceRepo.observeProducts(), _foodPetType, _foodKind, _foodStage) { prods, pet, kind, stage ->
            prods.filter { it.listType == "Food" }
                .filter { pet == "All" || it.petType == pet }
                .filter { kind == "All" || it.foodType == kind }
                .filter { stage == "All" || it.lifeStage == stage }
                .map { it.toMarketProduct() }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val marketMedicines: StateFlow<List<MarketProduct>> = commerceRepo.observeProducts()
        .map { remote -> remote.filter { it.listType == "Medicine" }.map { it.toMarketProduct() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _verifiedDoctorsList = MutableStateFlow<List<VerifiedDoctor>>(emptyList())

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Emits the currently signed-in uid and re-emits whenever the auth state
    // changes (sign-in / sign-out). The Firestore repository resolves uid() ONCE
    // per subscription, so without this the pets listener can latch onto
    // users/anonymous/pets (bound before the user signs in) and stay empty forever
    // — which leaves the PetSwitcher with no pets to show and makes the app look
    // like it only ever holds a single (active) pet.
    private val signedInUid: Flow<String> = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.uid ?: "anonymous")
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val allPets: StateFlow<List<UserPet>> = signedInUid
        .flatMapLatest { firestoreRepo.getAllUserPets() }
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

    // Real certificates uploaded for the active pet (photos + details)
    val certificates: StateFlow<List<PetCertificate>> = _activePetId
        .flatMapLatest { petId -> firestoreRepo.getCertificatesForPet(petId) }
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

    init {
        // CRITICAL FIX: on a cold start _activePetId keeps the default 1L, which
        // never matches a real Firestore pet id — every vaccination / medical /
        // certificate save then silently no-ops. Watch the pets list and make
        // sure the active id always points at a real pet.
        viewModelScope.launch {
            // Collect the SHARED allPets StateFlow (same listener that fills
            // petDocIdMap) - a second getAllUserPets() instance could die
            // independently and leave the active pet id stuck on the placeholder.
            allPets.collect { pets ->
                if (pets.isNotEmpty() && pets.none { it.id == _activePetId.value }) {
                    _activePetId.value = pets.first().id
                }
            }
        }
    }

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

    val foodItems: StateFlow<List<FoodItem>> = flowOf(emptyList<FoodItem>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val accessoryItems: StateFlow<List<AccessoryItem>> = flowOf(emptyList<AccessoryItem>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val healthCareItems: StateFlow<List<HealthCareItem>> = flowOf(emptyList<HealthCareItem>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val trainingGuides: StateFlow<List<TrainingGuide>> = flowOf(emptyList<TrainingGuide>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Grooming salons: owner-added partners only (Products with listType "Grooming"). */
    val groomingCenters: StateFlow<List<GroomingCenter>> = commerceRepo.observeProducts()
        .map { remote ->
            remote.filter { it.listType == "Grooming" }.map { p ->
                GroomingCenter(
                    name = p.name,
                    tagLine = p.description.take(60),
                    address = p.category.ifBlank { "Kerala" },
                    distance = "",
                    rating = 4.5,
                    reviewCount = 0,
                    packages = p.description.split("|").map { it.trim() }.filter { it.isNotEmpty() },
                    startingPrice = "₹ ${p.priceInr.toInt()}",
                    phone = ""
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val foodSubscriptions: StateFlow<List<FoodSubscription>> = flowOf(emptyList<FoodSubscription>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val boardingSitters: StateFlow<List<BoardingSitter>> = flowOf(emptyList<BoardingSitter>())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------- Admin Panel v2: owner-managed catalogue across categories ----------

    /** ALL marketplace listings (sale + adoption), unfiltered — for the admin panel. */
    val adminListings: StateFlow<List<MarketPet>> = firestoreMarketRepo.observeMarketPets()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** All lost-pet SOS alerts — for the admin panel. */
    val adminLostAlerts: StateFlow<List<AdminLostPetAlert>> = commerceRepo.observeLostPetAlerts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Accessories: owner-added products only. */
    val marketAccessories: StateFlow<List<AccessoryItem>> =
        commerceRepo.observeProducts().map { remote ->
            remote.filter { it.listType == "Accessory" }.map { p ->
                AccessoryItem(
                    name = p.name,
                    subType = p.category.ifBlank { "Other" },
                    description = p.description,
                    estimatedPrice = "₹ ${p.priceInr.toInt()}",
                    material = ""
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Training programs: owner-added programs only. */
    val marketTrainingGuides: StateFlow<List<TrainingGuide>> =
        commerceRepo.observeProducts().map { remote ->
            remote.filter { it.listType == "Training" }.map { p ->
                TrainingGuide(
                    title = p.name,
                    level = p.category.ifBlank { "Basic" },
                    steps = p.description.split("|").map { it.trim() }.filter { it.isNotEmpty() },
                    tips = "",
                    recommendedAge = "All ages"
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Food subscriptions: owner-added plans only. */
    val marketFoodSubscriptions: StateFlow<List<FoodSubscription>> =
        commerceRepo.observeProducts().map { remote ->
            remote.filter { it.listType == "Subscription" }.map { p ->
                FoodSubscription(
                    title = p.name,
                    planType = p.category.ifBlank { "Monthly" },
                    comboContents = p.description,
                    brandsIncluded = "Wagmiya partners",
                    monthlyEstimate = "₹ ${p.priceInr.toInt()}/month",
                    savingsTag = ""
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Boarding & sitters: owner-added partners only. */
    val marketBoardingSitters: StateFlow<List<BoardingSitter>> =
        commerceRepo.observeProducts().map { remote ->
            val knownSitterTypes = listOf("Full Day (24hr)", "Per Day Care", "Pet Night Care", "Feed on Time Only", "Pet Walker")
            remote.filter { it.listType == "Boarding" }.map { p ->
                BoardingSitter(
                    name = p.name,
                    // Respect the admin's category when it matches a filter tab,
                    // otherwise default to the first tab so the sitter is never hidden.
                    sitterType = p.category.trim().takeIf { it in knownSitterTypes } ?: "Full Day (24hr)",
                    tagline = p.description.take(60),
                    experience = "Verified partner",
                    rating = 4.5,
                    priceEstimate = "₹ ${p.priceInr.toInt()}",
                    features = p.description.split("|").map { it.trim() }.filter { it.isNotEmpty() },
                    verified = p.verified,
                    photoUris = p.photoUris,
                    timeSlots = if (p.category.trim() == "Pet Walker")
                        listOf("Morning 6-9", "Afternoon 12-3", "Evening 4-7") else emptyList()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setFoodPetType(v: String) { _foodPetType.value = v }
    fun setFoodKind(v: String) { _foodKind.value = v }
    fun setFoodStage(v: String) { _foodStage.value = v }

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

    /** Add an accessory (toys, wearables, clothing...) to the cart. */
    fun addAccessoryToCart(item: AccessoryItem) {
        val price = item.estimatedPrice.filter { ch -> ch.isDigit() || ch == '.' }.toDoubleOrNull() ?: 0.0
        if (price <= 0.0) return
        addToCart(
            itemId = "acc_" + item.name,
            title = item.name,
            subtitle = item.subType.ifBlank { "Accessory" },
            priceInr = price
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

    /** Customer books a boarding sitter → real booking; admin assigns the sitter and collects the fee. */
    fun placeSitterBooking(sitterName: String, sitterType: String, priceEstimate: String, petName: String, date: String, notes: String) {
        val booking = ServiceBooking(
            id = "",
            type = "BOARDING",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = _customerProfile.value.phone,
            petName = petName,
            providerName = sitterName,
            serviceInfo = sitterType,
            dateLabel = date,
            slot = "",
            notes = notes,
            feeInr = parseRupees(priceEstimate),
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Customer subscribes to a food/accessory plan → real booking the admin fulfils. */
    fun placeSubscriptionBooking(planTitle: String, planType: String, monthlyEstimate: String) {
        val booking = ServiceBooking(
            id = "",
            type = "SUBSCRIPTION",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = _customerProfile.value.phone,
            petName = activePet.value.name,
            providerName = planTitle,
            serviceInfo = planType,
            dateLabel = "",
            slot = "",
            notes = "",
            feeInr = parseRupees(monthlyEstimate),
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Add a guide food item to the cart. */
    fun addFoodItemToCart(item: FoodItem) {
        val price = parseRupees(item.estimatedPrice)
        if (price <= 0.0) return
        addToCart(
            itemId = "food_" + item.name,
            title = item.name,
            subtitle = item.subType.ifBlank { "Food" },
            priceInr = price
        )
    }

    /** Customer requests a health care service → real booking (admin confirms + assigns). */
    fun placeHealthCareBooking(item: HealthCareItem) {
        val booking = ServiceBooking(
            id = "",
            type = "DOCTOR",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = _customerProfile.value.phone,
            petName = activePet.value.name,
            providerName = "",
            serviceInfo = item.title + " (" + item.subType + ")",
            dateLabel = "",
            slot = "",
            notes = item.frequencyOrTimeline,
            feeInr = parseRupees(item.estimatedCost),
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** First number group in a price string like "₹3,850 / 24 Hours" → 3850.0 */
    private fun parseRupees(text: String): Double {
        val m = Regex("[0-9][0-9,]*").find(text) ?: return 0.0
        return m.value.replace(",", "").toDoubleOrNull() ?: 0.0
    }

    /** Customer requests a grooming service → real booking (admin confirms via Bookings tab). */
    fun placeGroomingBooking(serviceTitle: String, city: String) {
        val booking = ServiceBooking(
            id = "",
            type = "GROOMING",
            ownerId = "",
            customerName = _customerProfile.value.name,
            customerPhone = _customerProfile.value.phone,
            petName = activePet.value.name,
            providerName = "",
            serviceInfo = serviceTitle,
            dateLabel = "",
            slot = "",
            notes = "City: $city",
            feeInr = 0.0,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    // Community posts (Firestore-backed, real)
    val communityPosts: StateFlow<List<CommunityPost>> = commerceRepo.observeCommunityPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCommunityPost(message: String, petName: String) {
        viewModelScope.launch {
            commerceRepo.addCommunityPost(
                _customerProfile.value.name.ifBlank { "Pet Lover" },
                petName,
                message
            )
        }
    }

    fun deleteCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            commerceRepo.deleteCommunityPost(post.id)
        }
    }

    /** Admin assigns a doctor/trainer to a booking → status CONFIRMED. */
    fun adminAssignBooking(bookingId: String, name: String, phone: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.assignBooking(bookingId, name, phone).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Customer marks their own booking as received -> COMPLETED (owner is allowed by rules). */
    fun markBookingReceived(bookingId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.updateBookingStatus(bookingId, "COMPLETED").isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminUpdateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.updateBookingStatus(bookingId, status).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminAddProduct(
        name: String, listType: String, category: String, priceInr: Double, description: String,
        petType: String = "All", foodType: String = "All", lifeStage: String = "All",
        photoUris: List<Uri> = emptyList(), verified: Boolean = false
    ) {
        viewModelScope.launch {
            val p = ShopProduct(
                id = "", name = name, listType = listType, category = category,
                priceInr = priceInr, description = description,
                petType = petType, foodType = foodType, lifeStage = lifeStage,
                verified = verified
            )
            _commerceEvent.value = if (commerceRepo.addProduct(p, photoUris).isSuccess) R.string.admin_saved else R.string.admin_failed
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
        inPersonFee: Double,
        isOnline: Boolean = false
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
                phone = phone,
                isOnline = isOnline
            )
            _commerceEvent.value = if (commerceRepo.addVet(vet).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Admin: remove any marketplace listing (sale/adoption moderation). */
    fun adminDeleteListing(listingId: String) {
        viewModelScope.launch {
            firestoreMarketRepo.deleteListing(listingId)
        }
    }

    /** Admin: remove a lost-pet SOS alert (e.g. pet found or spam). */
    fun adminDeleteLostAlert(alertId: String) {
        viewModelScope.launch {
            commerceRepo.deleteLostPetAlert(alertId)
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
            val current = currentPetOrNull() ?: return@launch
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
            val current = currentPetOrNull() ?: return@launch
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
            val current = currentPetOrNull() ?: return@launch
            val updated = current.copy(
                favoriteFoods = foods,
                favoritePlays = plays
            )
            firestoreRepo.savePet(updated)
        }
    }

    /** Save the pet's training level, status text and mastered milestones. */
    fun updateTraining(level: String, status: String, milestones: String) {
        viewModelScope.launch {
            val current = currentPetOrNull() ?: return@launch
            val updated = current.copy(
                trainingLevel = level,
                trainingStatus = status,
                trainingMilestones = milestones
            )
            firestoreRepo.savePet(updated)
        }
    }

    fun updatePetPhoto(photoUri: String) {
        viewModelScope.launch {
            val current = currentPetOrNull() ?: return@launch
            val updated = current.copy(photoUri = photoUri)
            firestoreRepo.savePet(updated)
        }
    }

    /**
     * Saves the Premium AI Pet Photo Analysis result into the pet's profile.
     * The breed guess is prepended to the free-text [UserPet.aiAnalysis] field
     * alongside the full observations returned by the model.
     */
    fun saveAiAnalysis(breed: String, notes: String) {
        viewModelScope.launch {
            val current = currentPetOrNull() ?: return@launch
            val summary = buildString {
                if (breed.isNotBlank()) append("Likely breed: ").append(breed.trim()).append('\n')
                append(notes.trim())
            }.trim()
            val updated = current.copy(aiAnalysis = summary)
            firestoreRepo.savePet(updated)
        }
    }

    fun toggleVaccinationStatus(record: VaccinationRecord) {
        viewModelScope.launch {
            val newStatus = if (record.status == "Completed") "Upcoming" else "Completed"
            firestoreRepo.updateVaccinationStatus(_activePetId.value, record.id, newStatus)
        }
    }

    fun addVaccinationRecord(name: String, date: String, nextDue: String, status: String, doctor: String) {
        viewModelScope.launch {
            val petId = _activePetId.value
            val vax = VaccinationRecord(
                petId = petId,
                vaccineName = name,
                dateGiven = date,
                nextDueDate = nextDue,
                status = status,
                veterinarian = doctor,
                batchNumber = "VAX-${(1000..9999).random()}"
            )
            firestoreRepo.addVaccination(petId, vax)
        }
    }

    fun addCertificate(title: String, registrationId: String, issuedBy: String, issueDate: String, photos: List<String>) {
        viewModelScope.launch {
            val petId = _activePetId.value
            val cert = PetCertificate(
                id = 0L,
                petId = petId,
                title = title,
                registrationId = registrationId,
                issuedBy = issuedBy,
                issueDate = issueDate,
                createdAt = System.currentTimeMillis()
            )
            firestoreRepo.addCertificate(
                petId,
                cert,
                photos.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            )
        }
    }

    fun deleteCertificate(cert: PetCertificate) {
        viewModelScope.launch {
            firestoreRepo.deleteCertificate(cert.petId, cert.id)
        }
    }

    fun addMedicalReport(title: String, clinic: String, diagnosis: String, prescription: String) {
        viewModelScope.launch {
            val petId = _activePetId.value
            val report = MedicalReport(
                petId = petId,
                title = title,
                clinicName = clinic,
                date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date()),
                diagnosis = diagnosis,
                prescription = prescription
            )
            firestoreRepo.addMedicalReport(petId, report)
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
        val existingPlan = _customerProfile.value.carePlan
        val existingUntil = _customerProfile.value.carePlanUntil
        val updated = CustomerProfile(
            name = name.ifBlank { "Alex Morgan" },
            email = email.ifBlank { "alex.morgan@example.com" },
            phone = phone.ifBlank { "+91 98470 12345" },
            location = "Kochi, Kerala",
            isLoggedIn = true,
            carePlan = existingPlan,
            carePlanUntil = existingUntil
        )
        _customerProfile.value = updated
        // Persist the profile (including the carePlan gating fields) so the
        // owner can manage the plan from the Firebase console.
        viewModelScope.launch {
            try {
                firestoreRepo.saveCustomerProfile(updated)
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "saveCustomerProfile failed", e)
            }
        }
    }

    // ================= MULTI-PET SUPPORT =================
    
    /**
     * Resolve the ACTIVE pet reliably. The selected pet id (_activePetId) is always
     * correct because the pet tabs set it directly — but the activePet StateFlow
     * can still hold the DEMO placeholder (UserPet() with id 0L) when its
     * Firestore lookup has not emitted yet. All saves must go through this so
     * they never write to the non-existent placeholder pet id.
     */
    private suspend fun currentPetOrNull(): UserPet? {
        val id = _activePetId.value
        if (activePet.value.id == id) return activePet.value
        return try {
            firestoreRepo.getPetById(id).first()
        } catch (e: Exception) {
            android.util.Log.e("PetViewModel", "currentPetOrNull failed for id $id", e)
            null
        }
    }

    /** Account deletion (Google Play requirement): wipes all user data then deletes the account. */
    fun deleteAccount(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = firestoreRepo.deleteAllUserData()
            onDone(result.isSuccess)
        }
    }

    fun submitSupportTicket(category: String, subject: String, details: String, contact: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = commerceRepo.submitSupportTicket(
                SupportTicket(category = category, subject = subject, details = details, contact = contact)
            )
            onDone(result.isSuccess)
        }
    }

    fun submitRescueReport(animalType: String, description: String, location: String, contact: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = commerceRepo.submitRescueReport(
                RescueReport(animalType = animalType, description = description, location = location, contact = contact)
            )
            onDone(result.isSuccess)
        }
    }

    /** Any signed-in user reports a sighting of a lost pet (photo required). */
    fun submitFoundReport(report: FoundPetReport, photoUri: Uri?, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = commerceRepo.submitFoundReport(report, photoUri)
            onDone(result.isSuccess)
        }
    }

    fun submitPartnerApplication(kind: String, name: String, category: String, city: String, phone: String, planName: String) {
        viewModelScope.launch {
            commerceRepo.submitPartnerApplication(
                PartnerApplication(
                    kind = kind,
                    name = name,
                    category = category,
                    city = city,
                    phone = phone,
                    planName = planName
                )
            )
        }
    }

    fun adminDeletePartnerApplication(id: String) {
        viewModelScope.launch { commerceRepo.deletePartnerApplication(id) }
    }

    fun adminDeleteSupportTicket(id: String) {
        viewModelScope.launch { commerceRepo.deleteSupportTicket(id) }
    }

    fun adminDeleteRescueReport(id: String) {
        viewModelScope.launch { commerceRepo.deleteRescueReport(id) }
    }

    fun adminDeleteFoundReport(id: String) {
        viewModelScope.launch { commerceRepo.deleteFoundReport(id) }
    }

    fun switchPet(petId: Long) {
        _activePetId.value = petId
    }

    fun addNewPet(name: String, species: String, breed: String, gender: String, ageYears: Int, ageMonths: Int) {
        viewModelScope.launch {
            // Force a brand-new primary key. A fresh UserPet must have id 0 so it is
            // APPENDED (Room auto-generates a new row; FirestorePetRepository.savePet
            // creates a NEW document) instead of overwriting an existing pet.
            val newPet = UserPet(
                id = 0L,
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
                // savePet appends a NEW document and returns its stable id, so the
                // freshly-added pet can be selected directly — no guessing from an
                // unordered list.
                val newId = firestoreRepo.savePet(newPet)
                if (newId > 0L) _activePetId.value = newId
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error saving new pet", e)
            }
        }
    }

    fun deleteCurrentPet() {
        viewModelScope.launch {
            try {
                val currentId = _activePetId.value
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

    /** Delete a SPECIFIC pet by id - used by the long-press on a pet chip. */
    fun deletePetById(petId: Long) {
        _activePetId.value = petId
        deleteCurrentPet()
    }

}





