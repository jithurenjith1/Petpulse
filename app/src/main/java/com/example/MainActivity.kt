package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GroomingServiceItem
import com.example.data.model.MarketPet
import com.example.data.model.MarketProduct
import com.example.data.model.VerifiedDoctor
import com.example.ui.components.PetAppBottomBar
import com.example.ui.screens.MapScreen
import com.example.ui.screens.RealSosScreen
import com.example.ui.components.PetSwitcher
import com.example.ui.components.AddPetDialog
import com.example.ui.components.PetAppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PetViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val authState by authViewModel.authState.collectAsStateWithLifecycle()

                if (authState.isAuthenticated && authState.user != null) {
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
    val currentTab by viewModel.currentMainTab.collectAsStateWithLifecycle()
    val allPets by viewModel.allPets.collectAsStateWithLifecycle()
    val activePetId by viewModel.activePetId.collectAsStateWithLifecycle()
    val activePet by viewModel.activePet.collectAsStateWithLifecycle()
    val customer by viewModel.customerProfile.collectAsStateWithLifecycle()
    val vaccinations by viewModel.vaccinations.collectAsStateWithLifecycle()
    val medicalReports by viewModel.medicalReports.collectAsStateWithLifecycle()

    val speciesList by viewModel.speciesList.collectAsStateWithLifecycle()
    val selectedSpecies by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val exploreSubTab by viewModel.exploreSubTab.collectAsStateWithLifecycle()
    val foodCategory by viewModel.foodCategory.collectAsStateWithLifecycle()
    val accessoryCategory by viewModel.accessoryCategory.collectAsStateWithLifecycle()
    val foodItems by viewModel.foodItems.collectAsStateWithLifecycle()
    val accessoryItems by viewModel.accessoryItems.collectAsStateWithLifecycle()
    val healthCareItems by viewModel.healthCareItems.collectAsStateWithLifecycle()
    val trainingGuides by viewModel.trainingGuides.collectAsStateWithLifecycle()

    val partnerSubTab by viewModel.partnerSubTab.collectAsStateWithLifecycle()
    val boardingType by viewModel.boardingType.collectAsStateWithLifecycle()
    val groomingCenters by viewModel.groomingCenters.collectAsStateWithLifecycle()
    val foodSubscriptions by viewModel.foodSubscriptions.collectAsStateWithLifecycle()
    val boardingSitters by viewModel.boardingSitters.collectAsStateWithLifecycle()
    val lostAlerts by viewModel.lostPetAlerts.collectAsStateWithLifecycle()
    val petListings by viewModel.petListings.collectAsStateWithLifecycle()
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

    val filteredMarketPets by viewModel.filteredMarketPets.collectAsStateWithLifecycle()
    val marketFoods by viewModel.marketFoods.collectAsStateWithLifecycle()
    val marketMedicines by viewModel.marketMedicines.collectAsStateWithLifecycle()
    val groomingServices by viewModel.groomingServices.collectAsStateWithLifecycle()
    val verifiedDoctors by viewModel.verifiedDoctors.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Dialog state controllers
    var showLoginDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showAddListingDialog by remember { mutableStateOf(false) }
    var showMapScreen by remember { mutableStateOf(false) }
    var showSosScreen by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }

    // Marketplace Modal controllers
    var showCartModal by remember { mutableStateOf(false) }
    var showEscrowCheckoutModal by remember { mutableStateOf(false) }
    var showOrderTrackingModal by remember { mutableStateOf(false) }
    var showListPetModal by remember { mutableStateOf(false) }
    var showVetRegisterModal by remember { mutableStateOf(false) }
    var selectedDoctorForBooking by remember { mutableStateOf<VerifiedDoctor?>(null) }

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
                onSosClick = { showSosDialog = true },
                onLoginClick = { showLoginDialog = true }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            PetAppBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setMainTab(it) }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        onLoginClick = { showLoginDialog = true },
                        onSavePetDirectly = { newName, newBreed, newAge, newGender ->
                            viewModel.renameAndConfigurePet(newName, newBreed, newAge, newGender)
                        },
                        onShowMessage = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
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
                        onOpenOrders = { showOrderTrackingModal = true },
                        onOpenListPetModal = { showListPetModal = true },
                        onOpenVetRegisterModal = { showVetRegisterModal = true },
                        onAddToCart = { product ->
                            viewModel.addProductToCart(product)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added '${product.name}' to Kerala cart!")
                            }
                        },
                        onBookDoctor = { doctor ->
                            selectedDoctorForBooking = doctor
                        },
                        onBookGrooming = { service ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Booking van for ${service.title} in $selectedKeralaCity. Our grooming van will arrive at your scheduled slot.")
                            }
                        },
                        onPetSelected = { pet ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Escrow reservation requested for ${pet.name} (${pet.breed}) in ${pet.city}!")
                            }
                        }
                    )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                }

                MainNavTab.EXPLORE_PETS -> {
                    ExplorePetsScreen(
                        speciesList = speciesList,
                        selectedSpecies = selectedSpecies,
                        onSelectSpecies = { viewModel.selectSpecies(it) },
                        currentSubTab = exploreSubTab,
                        onSelectSubTab = { viewModel.setExploreSubTab(it) },
                        foodCategory = foodCategory,
                        onSelectFoodCategory = { viewModel.setFoodCategory(it) },
                        accessoryCategory = accessoryCategory,
                        onSelectAccessoryCategory = { viewModel.setAccessoryCategory(it) },
                        foodItems = foodItems,
                        accessoryItems = accessoryItems,
                        healthCareItems = healthCareItems,
                        trainingGuides = trainingGuides,
                        onItemAction = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                }

                MainNavTab.PARTNERS_SERVICES -> {
                    PartnersServicesScreen(
                        currentSubTab = partnerSubTab,
                        onSelectSubTab = { viewModel.setPartnerSubTab(it) },
                        boardingType = boardingType,
                        onSelectBoardingType = { viewModel.setBoardingType(it) },
                        groomingCenters = groomingCenters,
                        foodSubscriptions = foodSubscriptions,
                        boardingSitters = boardingSitters,
                        lostPetAlerts = lostAlerts,
                        petListings = petListings,
                        petNews = petNews,
                        events = events,
                        onTriggerSosDialog = { showSosDialog = true },
                        onAddListingDialog = { showAddListingDialog = true },
                        onPartnerJoinClick = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onActionNotification = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                }
            }
        }
        }
    }

    if (showMapScreen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMapScreen = false },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFFFF8F3)
        ) {
            MapScreen()
        }
    }

    if (showSosScreen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSosScreen = false },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFFFF8F3)
        ) {
            RealSosScreen(petName = activePet.value.name)
        }
    }

    if (showMapScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapScreen()
            FloatingActionButton(
                onClick = { showMapScreen = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = Color(0xFFE07856)
            ) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showSosScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            RealSosScreen(petName = activePet.value.name)
            FloatingActionButton(
                onClick = { showSosScreen = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = Color(0xFFE63946)
            ) { Text("X", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    // Interactive Dialogs
    if (showLoginDialog) {
        CustomerLoginDialog(
            currentCustomer = customer,
            onDismiss = { showLoginDialog = false },
            onLogin = { name, email, phone ->
                viewModel.updateCustomerProfile(name, email, phone)
                coroutineScope.launch { snackbarHostState.showSnackbar("Welcome back, $name!") }
            },
            onLogout = {
                authViewModel!!.signOut()
                viewModel.updateCustomerProfile("Guest Customer", "guest@janeandpals.com", "+1 (555) 000-0000")
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Updated profile for $name!")
                }
            }
        )
    }

    if (showSosDialog) {
        LostPetSosDialog(
            defaultPetName = activePet.name,
            onDismiss = { showSosDialog = false },
            onBroadcast = { petName, species, breed, location, reward, phone, desc ->
                viewModel.broadcastLostPet(petName, species, breed, location, reward, phone, desc)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🚨 5km SOS Alert Broadcasted for $petName! Nearby users notified.")
                }
            }
        )
    }

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
                showOrderTrackingModal = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🔒 Order #${newOrder.orderId} Placed in Escrow! Delivery OTP: ${newOrder.deliveryOtp}")
                }
            }
        )
    }

    if (showOrderTrackingModal) {
        OrderTimelineTrackingModal(
            orders = escrowOrders,
            onDismiss = { showOrderTrackingModal = false },
            onShowMessage = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    if (showListPetModal) {
        ListPetFormModal(
            onDismiss = { showListPetModal = false },
            onSubmit = { name, species, breed, age, gender, city, isExotic, listingType, price, desc, phone ->
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
                    phone = phone
                )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🐾 Listing for $name submitted with Escrow Protection in $city!")
                }
            }
        )
    }

    if (showVetRegisterModal) {
        RegisterVetFormModal(
            onDismiss = { showVetRegisterModal = false },
            onSubmit = { name, degrees, ksvcNumber, spec, exp, clinic, city, address, videoFee, inPersonFee, phone ->
                viewModel.registerVeterinarian(
                    name = name,
                    degrees = degrees,
                    ksvcRegNumber = ksvcNumber,
                    specialization = spec,
                    experienceYears = exp,
                    clinicName = clinic,
                    clinicCity = city,
                    clinicAddress = address,
                    videoConsultFeeInr = videoFee,
                    inPersonConsultFeeInr = inPersonFee,
                    phone = phone
                )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showMapScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F))
                ) {
                    Text("Nearby Services", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { showSosScreen = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) {
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🩺 Dr. $name registered & verified with KSVC in $city!")
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

    selectedDoctorForBooking?.let { doctor ->
        DoctorBookingModal(
            doctor = doctor,
            defaultPetName = activePet.name,
            onDismiss = { selectedDoctorForBooking = null },
            onConfirm = { consultType, petName, date, slot ->
                val booking = viewModel.bookDoctorConsultation(doctor, consultType, petName, date, slot)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("📅 $consultType booked with ${doctor.name} for $petName on $date at $slot!")
                }
            }
        )
    }
}


