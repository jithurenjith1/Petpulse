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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GroomingServiceItem
import com.example.data.model.MarketPet
import com.example.data.model.MarketProduct
import com.example.data.model.VerifiedDoctor
import com.example.ui.components.PetAppBottomBar
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
                    PetpulseApp(viewModel = viewModel, authViewModel = authViewModel)
                } else {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = { }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetpulseApp(viewModel: PetViewModel, authViewModel: AuthViewModel? = null) {
    val currentTab by viewModel.currentMainTab.collectAsStateWithLifecycle()
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PetAppTopBar(
                currentTab = currentTab,
                cartItemCount = cartItemCount,
                onTabSelected = { viewModel.setMainTab(it) }
            )

            Crossfade(
                targetState = currentTab,
                modifier = Modifier.weight(1f),
                label = "mainTab"
            ) { tab ->
                when (tab) {
                    MainTab.MY_PET -> MyPetsScreen(viewModel = viewModel)
                    MainTab.EXPLORE -> ExploreScreen(viewModel = viewModel)
                    MainTab.MARKET -> MarketScreen(viewModel = viewModel)
                    MainTab.PARTNERS -> PartnersScreen(viewModel = viewModel)
                }
            }

            PetAppBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setMainTab(it) }
            )
        }
    }
}
