package com.petpulse.app

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petpulse.app.data.model.GroomingServiceItem
import com.petpulse.app.data.model.MarketPet
import com.petpulse.app.data.model.MarketProduct
import com.petpulse.app.data.model.VerifiedDoctor
import com.petpulse.app.ui.components.PetAppBottomBar
import com.petpulse.app.ui.screens.MapScreen
import com.petpulse.app.ui.screens.RealSosScreen
import com.petpulse.app.ui.screens.PetInsuranceScreen
import com.petpulse.app.ui.screens.PetCommunityScreen
import com.petpulse.app.ui.screens.LostPetAlertsScreen
import com.petpulse.app.ui.screens.MyOrdersScreen
import com.petpulse.app.ui.screens.PetCareTipsScreen
import com.petpulse.app.ui.screens.AiSymptomCheckerScreen
import com.petpulse.app.ui.screens.VetTeleconsultScreen
import com.petpulse.app.ui.screens.PetpulseCareScreen
import com.petpulse.app.ui.screens.OnboardingScreen
import com.petpulse.app.ui.screens.HealthRecordsHubScreen
import com.petpulse.app.ui.components.PetSwitcher
import com.petpulse.app.ui.components.AddPetDialog
import com.petpulse.app.ui.components.PetAppTopBar
import com.petpulse.app.ui.screens.*
import com.petpulse.app.ui.theme.MyApplicationTheme
import com.petpulse.app.ui.viewmodel.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.petpulse.app.ui.screens.AdminScreen

class MainActivity : AppCompatActivity() {

    private val viewModel: PetViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Android 13+ requires runtime permission for local notifications
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
        setContent {
            // Reactive theme preference - toggling in Settings switches instantly.
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = darkTheme) {
                val authState by authViewModel.authState.collectAsStateWithLifecycle()

                // First-run onboarding: shown once after install, before login.
                val prefs = remember { getSharedPreferences("wagmiya_prefs", MODE_PRIVATE) }
                var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }

                if (showOnboarding) {
                    OnboardingScreen(onFinish = {
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        showOnboarding = false
                    })
                } else if (authState.isAuthenticated && authState.user != null) {
                JaneAndPalsApp(viewModel = viewModel, authViewModel = authViewModel)
                } else {
                    LoginScreen(authViewModel = authViewModel, onAuthSuccess = { })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JaneAndPalsApp(viewModel: PetViewModel, authViewModel: AuthViewModel? = null) {
    // Theme preference - collected here (not just in MainActivity's setContent scope) so the
    // Settings switch recomposes this composable and MyPetsScreen reactively.
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentMainTab.collectAsStateWithLifecycle()
    val allPets by viewModel.allPets.collectAsStateWithLifecycle()
    val activePetId by viewModel.activePetId.collectAsStateWithLifecycle()
    val activePet by viewModel.activePet.collectAsStateWithLifecycle()
    val customer by viewModel.customerProfile.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val adminOrders by viewModel.adminOrders.collectAsStateWithLifecycle()
    val adminDealers by viewModel.adminDealers.collectAsStateWithLifecycle()
    val shopProducts by viewModel.shopProducts.collectAsStateWithLifecycle()
    val adminBookings by viewModel.adminBookings.collectAsStateWithLifecycle()
    val myBookings by viewModel.myBookings.collectAsStateWithLifecycle()
    val adminVets by viewModel.partnerVets.collectAsStateWithLifecycle()
    val vaccinations by viewModel.vaccinations.collectAsStateWithLifecycle()

    val medicalReports by viewModel.medicalReports.collectAsStateWithLifecycle()
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()

    val speciesList by viewModel.speciesList.collectAsStateWithLifecycle()
    val selectedSpecies by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val exploreSubTab by viewModel.exploreSubTab.collectAsStateWithLifecycle()
    val foodCategory by viewModel.foodCategory.collectAsStateWithLifecycle()
    val accessoryCategory by viewModel.accessoryCategory.collectAsStateWithLifecycle()
    val foodItems by viewModel.foodItems.collectAsStateWithLifecycle()
    val accessoryItems by viewModel.accessoryItems.collectAsStateWithLifecycle()
    val healthCareItems by viewModel.healthCareItems.collectAsStateWithLifecycle()
    val trainingGuides by viewModel.trainingGuides.collectAsStateWithLifecycle()
    val marketAccessories by viewModel.marketAccessories.collectAsStateWithLifecycle()
    val marketTrainingGuides by viewModel.marketTrainingGuides.collectAsStateWithLifecycle()
    val marketFoodSubs by viewModel.marketFoodSubscriptions.collectAsStateWithLifecycle()
    val marketBoarding by viewModel.marketBoardingSitters.collectAsStateWithLifecycle()
    val adminListings by viewModel.adminListings.collectAsStateWithLifecycle()
    val adminLostAlerts by viewModel.adminLostAlerts.collectAsStateWithLifecycle()

    val partnerSubTab by viewModel.partnerSubTab.collectAsStateWithLifecycle()
    val boardingType by viewModel.boardingType.collectAsStateWithLifecycle()
    val groomingCenters by viewModel.groomingCenters.collectAsStateWithLifecycle()
    val foodSubscriptions by viewModel.foodSubscriptions.collectAsStateWithLifecycle()
    val boardingSitters by viewModel.boardingSitters.collectAsStateWithLifecycle()
    val lostAlerts by viewModel.lostPetAlerts.collectAsStateWithLifecycle()
    val petListings by viewModel.petListings.collectAsStateWithLifecycle()
val gpsTrackers by viewModel.gpsTrackers.collectAsStateWithLifecycle()
val adminSupportTickets by viewModel.supportTickets.collectAsStateWithLifecycle()
val adminRescueReports by viewModel.rescueReports.collectAsStateWithLifecycle()
val adminFoundReports by viewModel.foundReports.collectAsStateWithLifecycle()
val adminPartnerApplications by viewModel.partnerApplications.collectAsStateWithLifecycle()
val appCtx = LocalContext.current
    val petNews by viewModel.petNews.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val healthScore by viewModel.healthScore.collectAsStateWithLifecycle()

    // Kerala Marketplace states
    val selectedKeralaCity by viewModel.selectedKeralaCity.collectAsStateWithLifecycle()
    val keralaCities by viewModel.keralaCities.collectAsStateWithLifecycle()
    val marketCategory by viewModel.marketCategory.collectAsStateWithLifecycle()
    val isExoticsOnly by viewModel.isExoticsOnly.collectAsStateWithLifecycle()
    val marketSpeciesFilter by viewModel.marketSpeciesFilter.collectAsStateWithLifecycle()
    val isExpressDelivery by viewModel.isExpressDelivery.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartItemCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val escrowOrders by viewModel.escrowOrders.collectAsStateWithLifecycle()
    val myOrders by viewModel.myOrders.collectAsStateWithLifecycle()

    val filteredMarketPets by viewModel.filteredMarketPets.collectAsStateWithLifecycle()
    val marketFoods by viewModel.marketFoodsFiltered.collectAsStateWithLifecycle()
    val foodPetType by viewModel.foodPetType.collectAsStateWithLifecycle()
    val foodKind by viewModel.foodKind.collectAsStateWithLifecycle()
    val foodStage by viewModel.foodStage.collectAsStateWithLifecycle()
    val marketMedicines by viewModel.marketMedicines.collectAsStateWithLifecycle()
    val groomingServices by viewModel.groomingServices.collectAsStateWithLifecycle()
    val verifiedDoctors by viewModel.verifiedDoctors.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Dialog state controllers
    var showLoginDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showAddListingDialog by remember { mutableStateOf(false) }
    var showMapScreen by remember { mutableStateOf(false) }
    var showSosScreen by remember { mutableStateOf(false) }
    var showInsuranceScreen by remember { mutableStateOf(false) }
    var showCommunityScreen by remember { mutableStateOf(false) }
    var showLostPetAlertsScreen by remember { mutableStateOf(false) }
    var showCareTipsScreen by remember { mutableStateOf(false) }
    var showSymptomScreen by remember { mutableStateOf(false) }
    var showAiPhotoScreen by remember { mutableStateOf(false) }
    var showVetScreen by remember { mutableStateOf(false) }
    var showSubscriptionScreen by remember { mutableStateOf(false) }
    var showHealthRecordsScreen by remember { mutableStateOf(false) }
    var exploreSubTabUnused by remember { mutableStateOf(0) } // sub-tabs removed
    var showAddPetDialog by remember { mutableStateOf(false) }

    // Marketplace Modal controllers
    var showCartModal by remember { mutableStateOf(false) }
    var showAdminScreen by remember { mutableStateOf(false) }
    var showEscrowCheckoutModal by remember { mutableStateOf(false) }
    var showMyOrdersScreen by remember { mutableStateOf(false) }
    var showListPetModal by remember { mutableStateOf(false) }
    var selectedDoctorForBooking by remember { mutableStateOf<VerifiedDoctor?>(null) }
    var showTrainerRequestModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_main_scaffold"),
        topBar = {
            PetAppTopBar(
                customer = customer,
                pet = activePet,
                cartItemCount = cartItemCount,
                onCartClick = { showCartModal = true },
                onSosClick = { showSosScreen = true },
                onLoginClick = { showLoginDialog = true }
            )

        },
        bottomBar = {
            PetAppBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setMainTab(it) }
            )

        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )

        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            PetSwitcher(
                pets = allPets,
                activePetId = activePetId,
                onPetSelected = { viewModel.switchPet(it) },
                onAddPetClick = { showAddPetDialog = true }
            )
            // Feature buttons grid — shown ONLY in My Pet section
            if (currentTab == MainNavTab.MY_PETS) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { FeatureButton("Nearby", MaterialTheme.colorScheme.primary) { showMapScreen = true } }
                item { FeatureButton("Insurance", MaterialTheme.colorScheme.primary) { showInsuranceScreen = true } }
                item { FeatureButton("Community", MaterialTheme.colorScheme.primary) { showCommunityScreen = true } }
                item { FeatureButton("Care Tips", MaterialTheme.colorScheme.primary) { showCareTipsScreen = true } }
                item { FeatureButton("AI Triage", Color(0xFFA87A1F)) { showSymptomScreen = true } }
                item { FeatureButton("Vet Online", Color(0xFFA87A1F)) { showVetScreen = true } }
                item { FeatureButton("Care Plan", MaterialTheme.colorScheme.primary) { showSubscriptionScreen = true } }
                item { FeatureButton("Records", MaterialTheme.colorScheme.primary) { showHealthRecordsScreen = true } }
            }
            }

        Crossfade(
            targetState = currentTab,
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                MainNavTab.MY_PETS -> {
                    MyPetsScreen(
                        pet = activePet,
                        customer = customer,
                        vaccinations = vaccinations,
                        medicalReports = medicalReports,
                        healthScore = healthScore,
                        certificates = certificates,
                        onAddCertificate = { title, regId, issuedBy, issueDate, photos ->
                            viewModel.addCertificate(title, regId, issuedBy, issueDate, photos)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("📜 Certificate '$title' uploaded and saved to your account!")
                            }
                        },
                        onDeleteCertificate = { cert ->
                            viewModel.deleteCertificate(cert)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Certificate '${cert.title}' removed.")
                            }
                        },
                        onEditPetClick = { showEditPetDialog = true },
                        onToggleVaccine = { viewModel.toggleVaccinationStatus(it) },
                        onAddVaccine = { name, date, due, status, doc ->
                            viewModel.addVaccinationRecord(name, date, due, status, doc)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Vaccination '$name' recorded!") }
                        },
                        onAddMedicalReport = { title, clinic, diag, presc ->
                            viewModel.addMedicalReport(title, clinic, diag, presc)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Medical report added successfully!") }
                        },
                        onUpdateFoodPlays = { foods, plays ->
                            viewModel.updatePetFavoriteFoodsAndPlays(foods, plays)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Favorite food & plays updated for ${activePet.name}!") }
                        },
                        onSaveTraining = { level, status, milestones ->
                            viewModel.updateTraining(level, status, milestones)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Training updated for ${activePet.name}!") }
                        },
                        trainingPrograms = marketTrainingGuides,
                        onBookTraining = { program ->
                            val msg = "Hello Wagmiya! I want to book training: " + program.title + " (" + program.level + "). Please share details."
                            runCatching {
                                val encoded = java.net.URLEncoder.encode(msg, "UTF-8")
                                appCtx.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://wa.me/919526632311?text=$encoded")
                                    )
                                )
                            }
                        },
                        onLoginClick = { showLoginDialog = true },
                        onSavePetDirectly = { newName, newBreed, newAge, newGender ->
                            viewModel.renameAndConfigurePet(newName, newBreed, newAge, newGender)
                        },
                        onShowMessage = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onDeleteAccount = {
                            viewModel.deleteAccount { success ->
                                if (success) {
                                    authViewModel?.signOut()
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Account deletion failed. Please try again.")
                                    }
                                }
                            }
                        },
                        onDeletePet = {
                            viewModel.deleteCurrentPet()
                            coroutineScope.launch { snackbarHostState.showSnackbar("Pet removed") }
                        },
                        onPetLongPress = { pet ->
                            viewModel.deletePetById(pet.id)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Pet '" + pet.name + "' removed") }
                        },
                        onPhotoSelected = { uri ->
                            viewModel.updatePetPhoto(uri)
                        },
                        isPremium = isPremium,
                        onOpenAiPhoto = { showAiPhotoScreen = true },
                        darkTheme = darkTheme,
                        onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                        pets = allPets,
                        activePetId = activePetId,
                        onPetSelected = { viewModel.switchPet(it) },
                        onAddPetClick = { showAddPetDialog = true }
                    )

                }

                MainNavTab.MARKETPLACE -> {
                    MarketplaceScreen(
                        selectedCity = selectedKeralaCity,
                        onSelectCity = { viewModel.selectKeralaCity(it) },
                        cities = keralaCities,
                        currentCategory = marketCategory,
                        onSelectCategory = { viewModel.setMarketCategory(it) },
                        isExoticsOnly = isExoticsOnly,
                        onToggleExotics = { viewModel.toggleExoticsFilter(it) },
                        speciesFilter = marketSpeciesFilter,
                        onSelectSpeciesFilter = { viewModel.setMarketSpeciesFilter(it) },
                        pets = filteredMarketPets,
                        foods = marketFoods,
                        medicines = marketMedicines,
                        groomingServices = groomingServices,
                        doctors = verifiedDoctors,
                        cartItemCount = cartItemCount,
                        onOpenCart = { showCartModal = true },
                        onOpenOrders = { showMyOrdersScreen = true },
                        onOpenListPetModal = { showListPetModal = true },
                        currentUid = viewModel.currentUid,
                        foodPetType = foodPetType,
                        onSelectFoodPetType = { viewModel.setFoodPetType(it) },
                        foodKind = foodKind,
                        onSelectFoodKind = { viewModel.setFoodKind(it) },
                        foodStage = foodStage,
                        onSelectFoodStage = { viewModel.setFoodStage(it) },
                        onDeleteListing = { pet ->
                            viewModel.deleteMyListing(pet)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🗑️ '${pet.name}' listing removed.")
                            }
                        },
                        onAddToCart = { product ->
                            viewModel.addProductToCart(product)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added '${product.name}' to Kerala cart!")
                            }
                        },
                        onAddAccessoryToCart = { item ->
                            viewModel.addAccessoryToCart(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added '" + item.name + "' to cart!")
                            }
                        },
                        onAddFoodItemToCart = { item ->
                            viewModel.addFoodItemToCart(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added '" + item.name + "' to cart!")
                            }
                        },
                        onBookHealthCare = { item ->
                            viewModel.placeHealthCareBooking(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Booking requested: " + item.title)
                            }
                        },
                        onBookDoctor = { doctor ->
                            selectedDoctorForBooking = doctor
                        },
                        onRequestTrainer = { showTrainerRequestModal = true },
                        onBookGrooming = { service ->
                            viewModel.placeGroomingBooking(service.title, selectedKeralaCity)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("✂️ Grooming requested for ${service.title}! Our team will call you to confirm.")
                            }
                        },
                        onPetSelected = { pet ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Escrow reservation requested for ${pet.name} (${pet.breed}) in ${pet.city}!")
                            }
                        },
                        guideFoods = foodItems,
                        accessories = marketAccessories,
                        healthCareItems = healthCareItems,
                        trainingGuides = marketTrainingGuides,
                        onGuideItemAction = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                MainNavTab.EXPLORE_PETS -> {
                    // Merged into Market tab — no separate screen
                }

                MainNavTab.PARTNERS_SERVICES -> {
                    PartnersServicesScreen(
                        currentSubTab = partnerSubTab,
                        onSelectSubTab = { viewModel.setPartnerSubTab(it) },
                        boardingType = boardingType,
                        onSelectBoardingType = { viewModel.setBoardingType(it) },
                        groomingCenters = groomingCenters,
                        foodSubscriptions = marketFoodSubs,
                        boardingSitters = marketBoarding,
                        lostPetAlerts = lostAlerts,
                        petListings = petListings,
                        gpsTrackers = gpsTrackers,
                        onSubmitSupportTicket = { cat, subj, det, cont, cb ->
                            viewModel.submitSupportTicket(cat, subj, det, cont, cb)
                        },
                        onSubmitRescueReport = { animal, desc, loc, cont, cb ->
                            viewModel.submitRescueReport(animal, desc, loc, cont, cb)
                        },
                        onSubmitPartnerApplication = { kind, name, category, city, phone, planName ->
                            viewModel.submitPartnerApplication(kind, name, category, city, phone, planName)
                        },
                        onBookSitter = { sitterName, sitterType, priceEstimate, date, notes ->
                            viewModel.placeSitterBooking(sitterName, sitterType, priceEstimate, activePet.name, date, notes)
                        },
                        onSubscribePlan = { title, planType, monthlyEstimate ->
                            viewModel.placeSubscriptionBooking(title, planType, monthlyEstimate)
                        },
                        activePetName = activePet.name,
                        petNews = petNews,
                        events = events,
                        onTriggerSosDialog = { showSosScreen = true },
                        onAddListingDialog = { showAddListingDialog = true },
                        onPartnerJoinClick = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onActionNotification = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )

                }
            }
        }
        }
    }

    if (showMapScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapScreen()
            FloatingActionButton(
                onClick = { showMapScreen = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showSosScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            RealSosScreen(
                petName = activePet.name,
                species = activePet.species,
                breed = activePet.breed,
                ownerPhone = customer.phone
            )
            FloatingActionButton(
                onClick = { showSosScreen = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = Color(0xFFA87A1F)
            ) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    // Interactive Dialogs
    if (showLoginDialog) {
        CustomerLoginDialog(
            currentCustomer = customer,
            onDismiss = { showLoginDialog = false },
            isAdmin = isAdmin,
            onOpenAdmin = {
                showLoginDialog = false
                showAdminScreen = true
            },
            onLogin = { name, email, phone ->
                viewModel.updateCustomerProfile(name, email, phone)
                coroutineScope.launch { snackbarHostState.showSnackbar("Welcome back, $name!") }
            },
            onLogout = {
                authViewModel!!.signOut()
                viewModel.updateCustomerProfile("Guest Customer", "guest@wagmiya.app", "+91 98470 00000")
                coroutineScope.launch { snackbarHostState.showSnackbar("Signed out successfully.") }
            }
        )
    }

    if (showEditPetDialog) {
        EditPetProfileDialog(
            pet = activePet,
            onDismiss = { showEditPetDialog = false },
            onSave = { name, breed, gender, ageYears, ageMonths, weightKg, foods, plays, trainingStatus, trainingLevel, notes ->
                viewModel.updateFullPetDetails(
                    name, breed, gender, ageYears, ageMonths, weightKg,
                    foods, plays, trainingStatus, trainingLevel, notes
                )

                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Updated profile for $name!")
                }
            }
        )
    }

    // Old SOS dialog removed — use FeatureButton SOS + Lost Alerts instead

    if (showAddListingDialog) {
        AddPetListingDialog(
            onDismiss = { showAddListingDialog = false },
            onSubmit = { name, species, breed, age, location, type, price, desc, phone ->
                viewModel.addPetListing(name, species, breed, age, location, type, price, desc, phone)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Pet listing for $name posted successfully!")
                }
            }
        )
    }

    // Marketplace Specific Modals
    if (showCartModal) {
        SlideOutCartModal(
            cartItems = cartItems,
            selectedCity = selectedKeralaCity,
            isExpress = isExpressDelivery,
            onToggleExpress = { viewModel.toggleExpressDelivery(it) },
            onUpdateQuantity = { id, delta -> viewModel.updateCartItemQuantity(id, delta) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onClearCart = { viewModel.clearCart() },
            onDismiss = { showCartModal = false },
            onProceedToEscrowCheckout = {
                showCartModal = false
                showEscrowCheckoutModal = true
            }
        )
    }

    if (showEscrowCheckoutModal) {
        SecureEscrowCheckoutModal(
            cartItems = cartItems,
            customer = customer,
            selectedCity = selectedKeralaCity,
            isExpress = isExpressDelivery,
            onDismiss = { showEscrowCheckoutModal = false },
            onConfirmOrder = { city, address, name, phone, paymentMethod ->
                val newOrder = viewModel.placeEscrowOrder(city, address, name, phone, paymentMethod)
                showEscrowCheckoutModal = false
                showMyOrdersScreen = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✅ Order #${newOrder.orderId} placed! Live status tracking is in My Orders.")
                }
            }
        )
    }

    if (showMyOrdersScreen) {
        MyOrdersScreen(
            orders = myOrders,
            bookings = myBookings,
            onConfirmReceived = { id -> viewModel.markBookingReceived(id) },
            onBuyAgain = { order ->
                viewModel.reorderFromOrder(order)
                showMyOrdersScreen = false
                showCartModal = true
            },
            onClose = { showMyOrdersScreen = false }
        )
    }

    if (showAdminScreen) {
        AdminScreen(
            orders = adminOrders,
            dealers = adminDealers,
            products = shopProducts,
            bookings = adminBookings,
            onAssignDealer = { id, dealer -> viewModel.adminAssignDealer(id, dealer) },
            onUpdateStatus = { id, status -> viewModel.adminUpdateOrderStatus(id, status) },
            onAddProduct = { n, lt, c, p, d, pt, ft, ls, uris, vf -> viewModel.adminAddProduct(n, lt, c, p, d, pt, ft, ls, uris, vf) },
            onDeleteProduct = { id -> viewModel.adminDeleteProduct(id) },
            onAddDealer = { n, ph, c -> viewModel.adminAddDealer(n, ph, c) },
            onDeleteDealer = { id -> viewModel.adminDeleteDealer(id) },
            onAssignBooking = { id, name, phone -> viewModel.adminAssignBooking(id, name, phone) },
            onUpdateBookingStatus = { id, status -> viewModel.adminUpdateBookingStatus(id, status) },
            vets = adminVets,
            listings = adminListings,
            lostAlerts = adminLostAlerts,
            foundReports = adminFoundReports,
            onDeleteListing = { id -> viewModel.adminDeleteListing(id) },
            onDeleteLostAlert = { id -> viewModel.adminDeleteLostAlert(id) },
            onDeleteFoundReport = { id -> viewModel.adminDeleteFoundReport(id) },
            onAddVet = { n, sp, c, city, ph, vf, inf, on -> viewModel.adminAddVet(n, sp, c, city, ph, vf, inf, on) },
            onDeleteVet = { id -> viewModel.adminDeleteVet(id) },
            supportTickets = adminSupportTickets,
            rescueReports = adminRescueReports,
            onDeleteSupportTicket = { id -> viewModel.adminDeleteSupportTicket(id) },
            onDeleteRescueReport = { id -> viewModel.adminDeleteRescueReport(id) },
            partnerApplications = adminPartnerApplications,
            onDeletePartnerApplication = { id -> viewModel.adminDeletePartnerApplication(id) },
            onDismiss = { showAdminScreen = false }
        )
    }

    val marketCtx = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.marketPostEvent.collect { ev ->
            if (ev != null) {
                snackbarHostState.showSnackbar(marketCtx.getString(ev))
                viewModel.onMarketPostEventShown()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.commerceEvent.collect { ev ->
            if (ev != null) {
                snackbarHostState.showSnackbar(marketCtx.getString(ev))
                viewModel.onCommerceEventShown()
            }
        }
    }

    if (showListPetModal) {
        val uploadingMsg = stringResource(R.string.market_post_uploading)
        ListPetFormModal(
            onDismiss = { showListPetModal = false },
            onSubmit = { name, species, breed, age, gender, city, isExotic, listingType, price, desc, phone, photos ->
                viewModel.listPetForSaleOrAdoption(
                    name = name,
                    species = species,
                    breed = breed,
                    age = age,
                    gender = gender,
                    city = city,
                    isImportedExotic = isExotic,
                    listingType = listingType,
                    priceInr = price,
                    description = desc,
                    phone = phone,
                    photos = photos
                )

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(uploadingMsg)
                }
            }
        )
    }

    if (showAddPetDialog) {
        AddPetDialog(
            onDismiss = { showAddPetDialog = false },
            onAddPet = { name, species, breed, gender, ageYears, ageMonths ->
                viewModel.addNewPet(name, species, breed, gender, ageYears, ageMonths)
                showAddPetDialog = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Welcome $name to the family!") }
            }
        )
    }


    // === Feature Screen Overlays ===
    if (showInsuranceScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            PetInsuranceScreen()
            FloatingActionButton(onClick = { showInsuranceScreen = false }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), containerColor = MaterialTheme.colorScheme.primary) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
    if (showCommunityScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            PetCommunityScreen(
                posts = communityPosts,
                currentUid = viewModel.currentUid,
                onAddPost = { message, petName ->
                    viewModel.addCommunityPost(message, petName)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("📝 Post shared with the community!")
                    }
                },
                onDeletePost = { post ->
                    viewModel.deleteCommunityPost(post)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Post deleted.")
                    }
                },
                onOpenLostPetAlerts = { showLostPetAlertsScreen = true }
            )
            FloatingActionButton(onClick = { showCommunityScreen = false }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), containerColor = MaterialTheme.colorScheme.primary) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
    if (showLostPetAlertsScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            LostPetAlertsScreen(
                foundReports = adminFoundReports,
                onSubmitFoundReport = { report, photoUri -> viewModel.submitFoundReport(report, photoUri) }
            )
            FloatingActionButton(onClick = { showLostPetAlertsScreen = false }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), containerColor = MaterialTheme.colorScheme.primary) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
    if (showCareTipsScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            PetCareTipsScreen()
            FloatingActionButton(onClick = { showCareTipsScreen = false }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), containerColor = MaterialTheme.colorScheme.primary) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showSymptomScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            AiSymptomCheckerScreen(
                petName = activePet.name,
                onBookVet = { showSymptomScreen = false; showVetScreen = true },
                onClose = { showSymptomScreen = false }
            )
        }
    }

    if (showAiPhotoScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            AiPetPhotoAnalysisScreen(
                isPremium = isPremium,
                petName = activePet.name,
                onClose = { showAiPhotoScreen = false },
                onSaveResult = { breed, notes ->
                    viewModel.saveAiAnalysis(breed, notes)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("AI analysis saved to ${activePet.name}'s profile")
                    }
                }
            )
        }
    }

    if (showVetScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            VetTeleconsultScreen(
                onClose = { showVetScreen = false },
                vets = adminVets,
                onBookVet = { doctor -> selectedDoctorForBooking = doctor }
            )
        }
    }

    if (showSubscriptionScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            PetpulseCareScreen(
                onClose = { showSubscriptionScreen = false }
            )
        }
    }

    if (showHealthRecordsScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            HealthRecordsHubScreen(
                petName = activePet.name,
                onClose = { showHealthRecordsScreen = false }
            )
        }
    }

    selectedDoctorForBooking?.let { doctor ->
        DoctorBookingModal(
            doctor = doctor,
            defaultPetName = activePet.name,
            defaultPhone = customer.phone,
            onDismiss = { selectedDoctorForBooking = null },
            onConfirm = { consultType, petName, phone, date, slot, notes ->
                viewModel.placeDoctorBooking(doctor, consultType, petName, phone, date, slot, notes)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("📅 Booking requested! We will call $phone to confirm your ${doctor.name} appointment for $petName.")
                }
            }
        )
    }

    if (showTrainerRequestModal) {
        TrainerRequestModal(
            defaultPetName = activePet.name,
            defaultPhone = customer.phone,
            onDismiss = { showTrainerRequestModal = false },
            onConfirm = { petName, phone, trainingNeed ->
                viewModel.placeTrainerBooking(petName, phone, trainingNeed)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🎓 Trainer requested! Our team will call you to confirm the schedule.")
                }
            }
        )
    }
}

@Composable
private fun FeatureButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
fun FeatureScreenWrapper(title: String, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        content()
        FloatingActionButton(
            onClick = { },
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Text("X", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
















