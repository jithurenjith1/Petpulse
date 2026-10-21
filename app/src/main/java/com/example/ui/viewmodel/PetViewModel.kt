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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.petpulse.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.petpulse.app.data.repository.FirestoreMarketplaceRepository
import com.petpulse.app.data.repository.FirestoreCommerceRepository
import com.petpulse.app.data.remote.CloudinaryUploader
import com.petpulse.app.ui.screens.LostPetAlertItem
import com.petpulse.app.ui.screens.LostPetUiState

enum class MainNavTab {
    MY_PETS,
    MARKETPLACE,
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
    SUPPORT,
    TRAINERS
}

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        // Cloud-side fallback pet-photo encoding: long edge ~1400 px, <=150 KB.
        private const val PET_PHOTO_MAX_DIM = 1400
        private const val PET_PHOTO_BUDGET_BYTES = 150_000
    }

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

        // Real signed-in identity. Take name / email / phone from the Firebase
        // Auth user (never a hard-coded demo person) and track the sign-in flag.
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            auth.addAuthStateListener { fa ->
                val user = fa.currentUser
                val current = _customerProfile.value
                _customerProfile.value = current.copy(
                    name = user?.displayName?.takeIf { it.isNotBlank() } ?: current.name,
                    email = user?.email?.takeIf { it.isNotBlank() } ?: current.email,
                    phone = user?.phoneNumber?.takeIf { it.isNotBlank() } ?: current.phone,
                    isLoggedIn = user != null
                )
            }
        }

        // Merge the users/{uid} profile document (name / email / phone / location
        // plus the carePlan gating fields). A Firestore value is used only when it
        // is non-blank, so an absent document never invents an identity.
        viewModelScope.launch {
            firestoreRepo.observeCustomerProfile().collect { remote ->
                val current = _customerProfile.value
                _customerProfile.value = current.copy(
                    name = remote.name.takeIf { it.isNotBlank() } ?: current.name,
                    email = remote.email.takeIf { it.isNotBlank() } ?: current.email,
                    phone = remote.phone.takeIf { it.isNotBlank() } ?: current.phone,
                    location = remote.location.takeIf { it.isNotBlank() } ?: current.location,
                    carePlan = remote.carePlan,
                    carePlanUntil = remote.carePlanUntil,
                    fcmToken = remote.fcmToken.takeIf { it.isNotBlank() } ?: current.fcmToken
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
    // Accessories sub-category chip (Toys / Bed / Clothing / Bowls /
    // Cage & Carrier / Others). Mirrors the Food filter state above.
    private val _accessorySubCategory = MutableStateFlow("All")
    val accessorySubCategory: StateFlow<String> = _accessorySubCategory.asStateFlow()

    private val _accessoryCategory = MutableStateFlow("All")
    val accessoryCategory: StateFlow<String> = _accessoryCategory.asStateFlow()

    private val _partnerSubTab = MutableStateFlow(PartnerSubTab.GROOMING_CENTERS)
    val partnerSubTab: StateFlow<PartnerSubTab> = _partnerSubTab.asStateFlow()

    private val _boardingType = MutableStateFlow("Full Day (24hr)")
    val boardingType: StateFlow<String> = _boardingType.asStateFlow()

    // Trainers & Behaviour: the selected speciality chip. Values must match the
    // "category" an admin types when adding a Trainer partner (Trainer /
    // Behaviourist / Obedience). Defaults to the first chip.
    private val _trainerSpeciality = MutableStateFlow("Trainer")
    val trainerSpeciality: StateFlow<String> = _trainerSpeciality.asStateFlow()

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

    // Emits the currently signed-in uid and re-emits whenever the auth state
    // changes (sign-in / sign-out). The commerce repository resolves
    // auth.currentUser ONCE per subscription, so an orders listener bound before
    // the user signs in would latch onto an unauthenticated (permission-denied)
    // path and stay empty forever. Every auth-dependent stream below is driven
    // off this flow via flatMapLatest so it re-binds the moment the user signs
    // in or out.
    private val signedInUid: Flow<String> = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.uid ?: "anonymous")
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Admin gate + admin data (non-admins simply see empty lists)
    val isAdmin: StateFlow<Boolean> = commerceRepo.observeIsAdmin()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val adminOrders: StateFlow<List<AdminOrder>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observeOrders() }
            else flowOf(emptyList<AdminOrder>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Live orders of the signed-in customer (My Orders screen)
    val myOrders: StateFlow<List<AdminOrder>> = signedInUid
        .flatMapLatest { commerceRepo.observeMyOrders() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Live service bookings (doctor consults + trainer requests) — admin & customer.
    // Re-binds on every auth change (same pattern as orders) so a listener created
    // before sign-in does not latch onto a permission-denied, empty-forever path.
    val adminBookings: StateFlow<List<ServiceBooking>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observeBookings() }
            else flowOf(emptyList<ServiceBooking>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val myBookings: StateFlow<List<ServiceBooking>> = signedInUid
        .flatMapLatest { commerceRepo.observeMyBookings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Customer reviews for products + dealers (public read, signed-in create).
    val reviews: StateFlow<List<Review>> = commerceRepo.observeReviews()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adminDealers: StateFlow<List<Dealer>> = commerceRepo.observeDealers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Admin commission settings per partner (Partner Ledger). Re-binds after
    // sign-in because rules only allow the admin to read this collection.
    val partnerCommissions: StateFlow<List<PartnerCommission>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observePartnerCommissions() }
            else flowOf(emptyList<PartnerCommission>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // One shared upstream products listener for every derived catalogue below.
    // Previously each list opened its own full-collection Firestore listener
    // (12 duplicate listeners, ~550 wasted reads per cold start); they all now
    // map/filter from this single shared flow.
    private val allShopProducts: Flow<List<ShopProduct>> = commerceRepo.observeProducts()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val shopProducts: StateFlow<List<ShopProduct>> = allShopProducts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // GPS tracker collars (admin-managed products with listType = "GPS")
    val gpsTrackers: StateFlow<List<ShopProduct>> = allShopProducts
        .map { remote -> remote.filter { it.listType == "GPS" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Help & Support tickets and animal rescue reports (admin sees all).
    // All four re-bind after sign-in (rules only allow the admin to read them).
    val supportTickets: StateFlow<List<SupportTicket>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observeSupportTickets() }
            else flowOf(emptyList<SupportTicket>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rescueReports: StateFlow<List<RescueReport>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observeRescueReports() }
            else flowOf(emptyList<RescueReport>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // "I saw this pet" sighting reports. Admin sees all details; the public SOS
    // page only checks whether a report exists for an alert (never shows the finder).
    val foundReports: StateFlow<List<FoundPetReport>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observeFoundReports() }
            else flowOf(emptyList<FoundPetReport>())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Partner applications (business join forms + featured plan requests) - admin reviews
    val partnerApplications: StateFlow<List<PartnerApplication>> = isAdmin
        .flatMapLatest { admin ->
            if (admin) signedInUid.flatMapLatest { commerceRepo.observePartnerApplications() }
            else flowOf(emptyList<PartnerApplication>())
        }
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

    val marketFoods: StateFlow<List<MarketProduct>> = allShopProducts
        .map { remote -> remote.filter { it.listType == "Food" }.map { it.toMarketProduct() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Food list filtered by the Shop-by-pet / Food-type / Life-stage chips. */
    val marketFoodsFiltered: StateFlow<List<MarketProduct>> =
        combine(allShopProducts, _foodPetType, _foodKind, _foodStage) { prods, pet, kind, stage ->
            prods.filter { it.listType == "Food" }
                .filter { pet == "All" || it.petType == pet }
                .filter { kind == "All" || it.foodType == kind }
                .filter { stage == "All" || it.lifeStage == stage }
                .map { it.toMarketProduct() }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val marketMedicines: StateFlow<List<MarketProduct>> = allShopProducts
        .map { remote -> remote.filter { it.listType == "Medicine" }.map { it.toMarketProduct() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Accessories grid: owner-added products (listType "Accessory") mapped through
     * [ShopProduct.toMarketProduct] so the photos (photoUrls / photoUris) and the
     * description survive the trip. Filtered by the selected sub-category chip,
     * exactly like [marketFoodsFiltered] is filtered by the Food chips.
     */
    val marketAccessoriesFiltered: StateFlow<List<MarketProduct>> =
        combine(allShopProducts, _accessorySubCategory) { prods, sub ->
            prods.filter { it.listType == "Accessory" }
                .filter { sub == "All" || it.subCategory == sub }
                .map { it.toMarketProduct() }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val groomingServices: StateFlow<List<GroomingServiceItem>> = allShopProducts
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

    /** Real partner vets added by the owner (Firestore "vets" collection). */
    val partnerVets: StateFlow<List<VerifiedDoctor>> = commerceRepo.observeVets()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // The Healthcare tab lists the owner-added partner vets, narrowed to the
    // selected Kerala city. (Vet self-registration was dead code and was removed.)
    val verifiedDoctors: StateFlow<List<VerifiedDoctor>> = combine(
        partnerVets, _selectedKeralaCity
    ) { partners, city ->
        if (city == "All Kerala") partners
        else partners.filter { it.clinicCity.equals(city, ignoreCase = true) }
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

    // NOTE: signedInUid is declared near the top of this class (above adminOrders
    // / myOrders) so every auth-reactive stream - pets AND orders - re-binds after
    // sign-in. See the declaration there.

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

    // ---- PET QR TAG: public lost-pet tag ----

    // "Someone scanned your tag" notifications for the signed-in owner. Auth-reactive:
    // re-binds on sign-in / sign-out (same signedInUid.flatMapLatest pattern as
    // orders and pets).
    val tagScans: StateFlow<List<TagScan>> = signedInUid
        .flatMapLatest { firestoreRepo.observeTagScans() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // The PUBLIC tag document for the ACTIVE pet (null until a tag is created).
    val activePetTag: StateFlow<PetTag?> = activePet
        .map { it.qrPublicId }
        .distinctUntilChanged()
        .flatMapLatest { publicId ->
            if (publicId.isBlank()) flowOf<PetTag?>(null) else firestoreRepo.observePetTag(publicId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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

    // Live SOS alerts come from Firestore (lost_pet_alerts), mapped to the local
    // model. The old Room-backed table was never written, so this feed was
    // permanently empty even when real SOS alerts existed in Firestore.
    //
    // SINGLE source of truth: this is the ONE Firestore listener on
    // lost_pet_alerts. The public feed (lostPetAlerts), the screen state
    // (lostPetAlertsUiState) and the admin panel (adminLostAlerts) all derive
    // from it, so they can never disagree and only one listener is ever open.
    // Ordered by date descending - the same order the screen always used.
    private val lostAlertsSource: StateFlow<List<AdminLostPetAlert>> =
        commerceRepo.observeLostPetAlerts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lostPetAlerts: StateFlow<List<LostPetAlert>> = lostAlertsSource
        .map { alerts ->
            alerts.map { a ->
                LostPetAlert(
                    petName = a.petName,
                    species = a.species,
                    breed = a.breed,
                    lastSeenLocation = a.location,
                    distanceKm = 0.0,
                    rewardAmount = a.reward,
                    contactHelpline = a.contactPhone,
                    reportedTime = a.date,
                    description = "",
                    alternatePhone = a.alternatePhone
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for the Lost Pet Alerts screen, derived from the SAME single
    // listener above, so the screen and the admin feed always show identical
    // data. Maps the Firestore fields straight across - nothing else changes.
    val lostPetAlertsUiState: StateFlow<LostPetUiState> = lostAlertsSource
        .map { alerts ->
            LostPetUiState.Success(
                alerts.map { a ->
                    LostPetAlertItem(
                        id = a.id,
                        petName = a.petName,
                        species = a.species,
                        breed = a.breed,
                        lastSeenLocation = a.location,
                        reward = a.reward,
                        contactPhone = a.contactPhone,
                        alternatePhone = a.alternatePhone,
                        date = a.date,
                        priority = a.priority
                    )
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LostPetUiState.Loading
        )

    // Sale & Adoption feed now mirrors the SAME Firestore market_listings the Market
    // tab uses. The old Room table was local-only: listings posted there were
    // invisible to every other user and to the admin panel.
    val petListings: StateFlow<List<PetListing>> = _marketPetsList
        .map { pets ->
            pets.map { p ->
                PetListing(
                    petName = p.name,
                    species = p.species,
                    breed = p.breed,
                    age = p.age,
                    location = p.city,
                    description = p.description,
                    contactNumber = p.sellerPhone,
                    listingType = p.listingType,
                    priceEstimate = if (p.listingType == "Adoption") "Free for Adoption"
                        else "₹ ${p.priceInr.toInt()}",
                    postedBy = p.sellerName.ifBlank { "Community Member" }
                )
            }
        }
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
    val groomingCenters: StateFlow<List<GroomingCenter>> = allShopProducts
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

    /** ALL marketplace listings (sale + adoption), unfiltered — for the admin panel.
     *  Shares the single market_listings listener that feeds [marketPets] instead of
     *  opening a second whole-collection listener on the same collection. */
    val adminListings: StateFlow<List<MarketPet>> = _marketPetsList.asStateFlow()

    /** All lost-pet SOS alerts — for the admin panel. Shares the single
     *  lost_pet_alerts listener that feeds the public feed (lostAlertsSource)
     *  instead of opening a second whole-collection listener on the same data. */
    val adminLostAlerts: StateFlow<List<AdminLostPetAlert>> = lostAlertsSource

    /** Accessories: owner-added products only. */
    val marketAccessories: StateFlow<List<AccessoryItem>> =
        allShopProducts.map { remote ->
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
        allShopProducts.map { remote ->
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
        allShopProducts.map { remote ->
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
        allShopProducts.map { remote ->
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
                    photoUrls = p.photoUrls,
                    timeSlots = if (p.category.trim() == "Pet Walker")
                        listOf("Morning 6-9", "Afternoon 12-3", "Evening 4-7") else emptyList()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Trainers & behaviourists: owner-added partners only (Products with
     * listType "Trainer"), reusing the same model and admin-managed collection
     * that powers boarding / sitters / walkers.
     */
    val marketTrainerPartners: StateFlow<List<BoardingSitter>> =
        allShopProducts.map { remote ->
            val knownSpecialities = listOf("Trainer", "Behaviourist", "Obedience")
            remote.filter { it.listType == "Trainer" }.map { p ->
                BoardingSitter(
                    name = p.name,
                    // Respect the admin's category when it matches a filter chip,
                    // otherwise default to the first chip so the partner is never hidden.
                    sitterType = p.category.trim().takeIf { it in knownSpecialities } ?: "Trainer",
                    tagline = p.description.take(60),
                    experience = "Verified partner",
                    rating = 4.5,
                    priceEstimate = "₹ ${p.priceInr.toInt()}",
                    features = p.description.split("|").map { it.trim() }.filter { it.isNotEmpty() },
                    verified = p.verified,
                    photoUris = p.photoUris,
                    photoUrls = p.photoUrls
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Health score derived from the pet's OWN records (vaccinations, certificates
    // and medical reports). When there is not enough data the score is 0, which the
    // UI renders as an honest "Not enough data yet" state - never a fabricated 95.
    val healthScore: StateFlow<Int> = combine(vaccinations, certificates, medicalReports) { vax, certs, reports ->
        if (vax.isEmpty() && certs.isEmpty() && reports.isEmpty()) {
            0
        } else {
            val completed = vax.count { it.status == "Completed" }
            val vaxRatio = if (vax.isEmpty()) 0.0 else completed.toDouble() / vax.size.toDouble()
            val score = 60.0 + vaxRatio * 30.0 +
                (if (certs.isNotEmpty()) 5.0 else 0.0) +
                (if (reports.isNotEmpty()) 5.0 else 0.0)
            score.toInt().coerceIn(0, 100)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
    fun setAccessorySubCategory(v: String) { _accessorySubCategory.value = v }

    fun setAccessoryCategory(cat: String) {
        _accessoryCategory.value = cat
    }

    fun setPartnerSubTab(tab: PartnerSubTab) {
        _partnerSubTab.value = tab
    }

    fun setBoardingType(type: String) {
        _boardingType.value = type
    }

    fun setTrainerSpeciality(type: String) {
        _trainerSpeciality.value = type
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
        // Honest per-plan delivery: Premium is always free; Care is free only when
        // the subtotal is above ₹500; Basic/unknown pays the standard ₹40. Express
        // is an optional paid upgrade (+₹50) added on top of the base fee.
        val plan = _customerProfile.value.carePlan.trim().lowercase()
        val baseFee = when {
            plan == "premium" -> 0.0
            plan == "care" && subtotal > 500.0 -> 0.0
            else -> 40.0
        }
        return if (express) baseFee + 50.0 else baseFee
    }

    /** Plan discount percent actually applied to marketplace orders (0 / 10 / 15). */
    private fun planDiscountPercent(): Int = when (_customerProfile.value.carePlan.trim().lowercase()) {
        "premium" -> 15
        "care" -> 10
        else -> 0
    }

    // Duplicate-submit guard for checkout / booking confirms. True while an
    // order or booking write is in flight; the confirm buttons read this to
    // disable themselves and ignore further taps. Cleared in a finally block.
    private val _placing = MutableStateFlow(false)
    val placing: StateFlow<Boolean> = _placing.asStateFlow()

    // Escrow Order Placement
    fun placeEscrowOrder(
        city: String,
        address: String,
        name: String,
        phone: String,
        paymentMethod: String,
        addAccidentCover: Boolean = false
    ): EscrowOrder {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.priceInr * it.quantity }
        val deliveryFee = calculateDeliveryFee(subtotal, city, _isExpressDelivery.value)
        // The plan discount is applied for real: Care 10%, Premium 15%, otherwise 0%.
        val discountPercent = planDiscountPercent()
        val discountAmount = subtotal * discountPercent / 100.0
        val total = subtotal - discountAmount + deliveryFee
        val orderNum = (10000..99999).random()

        val newOrder = EscrowOrder(
            orderId = "ORD-KL-$orderNum",
            items = items,
            subtotalInr = subtotal,
            deliveryFeeInr = deliveryFee,
            discountInr = discountAmount,
            ecoPackagingFeeInr = 0.0,
            totalInr = total,
            deliveryCity = city.ifBlank { "Kochi" },
            deliveryAddress = address.ifBlank { "Doorstep, Kerala" },
            customerName = name.ifBlank { _customerProfile.value.name },
            customerPhone = phone.ifBlank { _customerProfile.value.phone },
            paymentMethod = paymentMethod,
            isEscrowProtected = false,
            status = OrderStatus.MERCHANT_CONFIRMED,
            deliveryOtp = "",
            deliveryRiderName = "",
            deliveryRiderVehicle = "",
            deliveryRiderPhone = "",
            orderDate = "Just now",
            timeline = listOf(
                OrderTimelineEvent(
                    title = "Order Placed",
                    description = "Your order has been received.",
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
                    title = "Packed & Sealed",
                    description = "Safe transit with tamper-evident packaging.",
                    timestamp = "Upcoming",
                    isCompleted = false
                ),
                OrderTimelineEvent(
                    title = "Out for Doorstep Delivery",
                    description = "Our delivery partner will contact you before arrival.",
                    timestamp = "Estimated in 45-60 mins",
                    isCompleted = false
                ),
                OrderTimelineEvent(
                    title = "Delivered",
                    description = "Please pay the delivery partner in cash on delivery.",
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
            _placing.value = true
            try {
                val result = commerceRepo.placeOrder(
                    orderNumber = newOrder.orderId,
                    items = items.map { OrderItemSnap(it.title, it.priceInr, it.quantity) },
                    totalInr = total,
                    discountInr = discountAmount,
                    customerName = newOrder.customerName,
                    customerPhone = newOrder.customerPhone,
                    address = newOrder.deliveryAddress,
                    city = newOrder.deliveryCity,
                    accidentCover = addAccidentCover
                )
                _commerceEvent.value = when {
                    result.exceptionOrNull()?.message == "NOT_SIGNED_IN" -> R.string.order_sign_in_to_place
                    result.isSuccess -> R.string.order_placed_msg
                    else -> R.string.order_failed_msg
                }
            } finally {
                _placing.value = false
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

    /** Admin removes one finished order from the Orders tab. */
    fun adminDeleteOrder(orderId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteOrder(orderId).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Admin removes several finished orders at once (Reset / clear-finished). */
    fun adminDeleteOrders(orderIds: List<String>) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteOrders(orderIds).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Customer removes one of their own finished orders (My Orders tab). */
    fun deleteMyOrder(orderId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteOrder(orderId).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Customer clears all of their own finished orders at once. */
    fun deleteMyOrders(orderIds: List<String>) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteOrders(orderIds).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Customer submits a product review (reachable only for a DELIVERED order). */
    fun submitProductReview(productId: String, rating: Int, text: String, orderId: String) {
        viewModelScope.launch {
            val review = Review(
                id = "",
                productId = productId,
                dealerName = "",
                ownerId = "",
                authorName = _customerProfile.value.name.ifBlank { "Pet Lover" },
                rating = rating,
                text = text,
                orderId = orderId,
                createdAt = 0L
            )
            _commerceEvent.value = if (commerceRepo.addReview(review).isSuccess) R.string.review_submitted else R.string.review_failed
        }
    }

    /** Customer submits a dealer / delivery-partner review after a DELIVERED order. */
    fun submitDealerReview(dealerName: String, rating: Int, text: String, orderId: String) {
        viewModelScope.launch {
            val review = Review(
                id = "",
                productId = "",
                dealerName = dealerName,
                ownerId = "",
                authorName = _customerProfile.value.name.ifBlank { "Pet Lover" },
                rating = rating,
                text = text,
                orderId = orderId,
                createdAt = 0L
            )
            _commerceEvent.value = if (commerceRepo.addReview(review).isSuccess) R.string.review_submitted else R.string.review_failed
        }
    }

    /** Admin deletes an abusive review (rules enforce owner-only delete). */
    fun adminDeleteReview(reviewId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteReview(reviewId).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Save the admin's commission percent (and paid flag) for one partner. */
    fun setPartnerCommission(entry: PartnerCommission) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.upsertPartnerCommission(entry).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    /** Mark a partner's commission dues as paid (or unpaid again). */
    fun setPartnerDuesPaid(entry: PartnerCommission, paid: Boolean) {
        viewModelScope.launch {
            val updated = entry.copy(duesPaid = paid)
            _commerceEvent.value = if (commerceRepo.upsertPartnerCommission(updated).isSuccess) R.string.admin_saved else R.string.admin_failed
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
            _placing.value = true
            try {
                _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
            } finally {
                _placing.value = false
            }
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
            _placing.value = true
            try {
                _commerceEvent.value = if (commerceRepo.placeBooking(booking).isSuccess) R.string.admin_saved else R.string.admin_failed
            } finally {
                _placing.value = false
            }
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

    fun addCommunityPost(message: String, petName: String, photoUri: Uri? = null) {
        viewModelScope.launch {
            commerceRepo.addCommunityPost(
                _customerProfile.value.name.ifBlank { "Pet Lover" },
                petName,
                message,
                photoUri
            )
        }
    }

    fun deleteCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            commerceRepo.deleteCommunityPost(post.id)
        }
    }

    // News & Events (Firestore-backed "news_posts", public read; admin write)
    val newsPosts: StateFlow<List<NewsPost>> = commerceRepo.observeNewsPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun adminAddNewsPost(
        title: String,
        body: String,
        category: String,
        author: String,
        link: String,
        photoUri: Uri? = null
    ) {
        viewModelScope.launch {
            val post = NewsPost(
                id = "",
                title = title,
                body = body,
                category = category,
                author = author.ifBlank { "Wagmiya" },
                photoData = "",
                link = link,
                createdAt = System.currentTimeMillis()
            )
            _commerceEvent.value = if (commerceRepo.addNewsPost(post, photoUri).isSuccess) R.string.admin_saved else R.string.admin_failed
        }
    }

    fun adminDeleteNewsPost(postId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteNewsPost(postId).isSuccess) R.string.admin_saved else R.string.admin_failed
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

    /** Admin removes one finished booking from a bookings tab. */
    fun adminDeleteBooking(bookingId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteBooking(bookingId).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Admin removes several finished bookings at once (Reset / clear-finished). */
    fun adminDeleteBookings(bookingIds: List<String>) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteBookings(bookingIds).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Customer removes one of their own finished bookings (My Bookings tab). */
    fun deleteMyBooking(bookingId: String) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteBooking(bookingId).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    /** Customer clears all of their own finished bookings at once. */
    fun deleteMyBookings(bookingIds: List<String>) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.deleteBookings(bookingIds).isSuccess) R.string.admin_deleted else R.string.admin_delete_failed
        }
    }

    fun adminAddProduct(
        name: String, listType: String, category: String, priceInr: Double, description: String,
        petType: String = "All", foodType: String = "All", lifeStage: String = "All",
        subCategory: String = "",
        photoUris: List<Uri> = emptyList(), verified: Boolean = false
    ) {
        viewModelScope.launch {
            val p = ShopProduct(
                id = "", name = name, listType = listType, category = category,
                priceInr = priceInr, description = description,
                petType = petType, foodType = foodType, lifeStage = lifeStage,
                subCategory = subCategory,
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
        isOnline: Boolean = false,
        isVerified: Boolean = true
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
                isOnline = isOnline,
                isVerified = isVerified
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

    /** Admin: toggle ONE partner vet's online/offline availability from the list. */
    fun adminSetVetOnline(vetId: String, isOnline: Boolean) {
        viewModelScope.launch {
            _commerceEvent.value = if (commerceRepo.setVetOnline(vetId, isOnline).isSuccess) R.string.admin_saved else R.string.admin_failed
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

    // Business actions for My Pet
    fun renameAndConfigurePet(name: String, breed: String, ageYears: Int, gender: String) {
        viewModelScope.launch {
            val current = currentPetOrNull() ?: return@launch
            val updated = current.copy(
                name = name.ifBlank { "" },
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
                name = name.ifBlank { "" },
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
            // Cloudinary first: host a good-size copy and remember its URL. The local
            // photoUri is kept as the offline / fallback source, so the user never
            // sees a broken image if the upload fails.
            val uploaded = CloudinaryUploader
                .uploadImage(getApplication(), Uri.parse(photoUri))
                .getOrNull()
                .orEmpty()
            // A hosted URL is the ideal cloud-side copy. When the upload yields
            // nothing (offline, preset rejection), store the photo itself as base64
            // in Firestore instead, so a CLOUD-SIDE copy ALWAYS exists and survives an
            // uninstall (the device photoUri does not). The previous base64 is kept if
            // this re-compression fails, and dropped once a hosted URL exists.
            val photoData = if (uploaded.isBlank()) {
                petPhotoBase64(Uri.parse(photoUri)).ifBlank { current.photoData }
            } else {
                ""
            }
            val updated = current.copy(photoUri = photoUri, photoUrl = uploaded, photoData = photoData)
            firestoreRepo.savePet(updated)
        }
    }

    /**
     * Downscale + JPEG-compress a picked photo to base64 (long edge ~1400 px, target
     * <=150 KB) for the cloud-side fallback copy stored in [UserPet.photoData].
     * Returns "" when the photo cannot be read or compressed.
     */
    private suspend fun petPhotoBase64(uri: Uri): String {
        return withContext(Dispatchers.Default) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > PET_PHOTO_MAX_DIM * 2) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val decoded = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
                if (decoded == null) {
                    ""
                } else {
                    val scale = minOf(1f, PET_PHOTO_MAX_DIM.toFloat() / maxOf(decoded.width, decoded.height, 1))
                    val bmp = if (scale < 1f) {
                        Bitmap.createScaledBitmap(
                            decoded,
                            (decoded.width * scale).toInt().coerceAtLeast(1),
                            (decoded.height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else decoded
                    val out = ByteArrayOutputStream()
                    var quality = 78
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    while (out.size() > PET_PHOTO_BUDGET_BYTES && quality > 30) {
                        quality -= 16
                        out.reset()
                        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                    Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "pet photo base64 failed", e)
                ""
            }
        }
    }

    /**
     * Ensure the active pet has a random, non-guessable public QR id and a matching
     * public "pet_tags" document. The existing id is reused when present, so the
     * same tag is never regenerated (and the printed code keeps working).
     */
    fun ensureActivePetQrTag() {
        viewModelScope.launch {
            val pet = currentPetOrNull() ?: return@launch
            val publicId = pet.qrPublicId.ifBlank {
                java.util.UUID.randomUUID().toString().replace("-", "")
            }
            if (pet.qrPublicId.isBlank()) {
                firestoreRepo.savePet(pet.copy(qrPublicId = publicId))
            }
            firestoreRepo.ensurePetTag(pet, publicId)
        }
    }

    /** Flip the public "lost" flag on the active pet's tag. */
    fun setActivePetTagLost(lost: Boolean) {
        viewModelScope.launch {
            val pet = currentPetOrNull() ?: return@launch
            val publicId = pet.qrPublicId
            if (publicId.isBlank()) return@launch
            firestoreRepo.setTagLost(publicId, lost)
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
        // Persist to the SAME Firestore market_listings collection the Market tab
        // uses, so the listing is visible to every user and to the admin panel.
        // (This previously wrote a local Room table nobody else could see.)
        submitOwnerMarketPetListing(
            name = petName,
            species = species,
            breed = breed,
            age = age,
            gender = "Unknown",
            city = location,
            isExotic = false,
            listingType = listingType,
            priceInr = parseRupees(price),
            description = description,
            phone = phone,
            photos = emptyList()
        )
    }

    fun updateCustomerProfile(name: String, email: String, phone: String) {
        val existingPlan = _customerProfile.value.carePlan
        val existingUntil = _customerProfile.value.carePlanUntil
        val updated = CustomerProfile(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            location = _customerProfile.value.location,
            isLoggedIn = true,
            memberSince = _customerProfile.value.memberSince,
            carePlan = existingPlan,
            carePlanUntil = existingUntil,
            // Preserve the FCM token so a profile save never wipes it.
            fcmToken = _customerProfile.value.fcmToken
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
     * Resolve the ACTIVE pet for a WRITE, materialising the placeholder when needed.
     * The selected pet id (_activePetId) is always correct because the pet tabs set
     * it directly — but when the account has no saved pet yet the UI shows the DEMO
     * placeholder (UserPet() with id 0L) and activePet never resolves to a real
     * document. A save (e.g. a newly picked photo) would then silently write to a
     * document that does not exist, so the change never sticks. In that case we
     * FIRST create a real Firestore document for the pet, point _activePetId at it,
     * and return it so the caller's write lands on a genuine document.
     */
    private suspend fun currentPetOrNull(): UserPet? {
        val id = _activePetId.value
        if (activePet.value.id == id && id > 0L) return activePet.value
        val existing = try {
            firestoreRepo.getPetById(id).first()
        } catch (e: Exception) {
            android.util.Log.e("PetViewModel", "currentPetOrNull failed for id $id", e)
            null
        }
        if (existing != null) return existing
        // No real document for the active pet: the UI is showing the placeholder.
        // Materialise it into a genuine saved pet so this edit persists instead of
        // being lost (and never overwritten by the placeholder on the next read).
        return try {
            val blank = UserPet(
                id = 0L,
                name = "",
                species = "",
                breed = "",
                gender = "",
                ageYears = 0,
                ageMonths = 0,
                weightKg = 0.0,
                microchipNumber = "",
                hasCertificate = false,
                certificateNumber = "",
                certificateIssuedBy = "",
                certificateDate = "",
                favoriteFoods = "",
                favoritePlays = "",
                trainingStatus = "",
                trainingLevel = "",
                trainingMilestones = "",
                avatarRes = "",
                photoUri = "",
                notes = "",
                aiAnalysis = ""
            )
            val newId = firestoreRepo.savePet(blank)
            if (newId > 0L) {
                _activePetId.value = newId
                blank.copy(id = newId)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("PetViewModel", "Failed to materialise placeholder pet", e)
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
                trainingMilestones = "",
                avatarRes = "",
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





