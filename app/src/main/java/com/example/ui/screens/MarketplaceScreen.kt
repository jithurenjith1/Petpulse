package com.petpulse.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.res.stringResource
import com.petpulse.app.R


import androidx.compose.animation.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.data.model.*
import com.petpulse.app.ui.theme.*

@Composable
fun MarketplaceScreen(
    selectedCity: String,
    onSelectCity: (String) -> Unit,
    cities: List<KeralaCity>,
    currentCategory: MarketplaceCategory,
    onSelectCategory: (MarketplaceCategory) -> Unit,
    isExoticsOnly: Boolean,
    onToggleExotics: (Boolean) -> Unit,
    speciesFilter: String,
    onSelectSpeciesFilter: (String) -> Unit,
    pets: List<MarketPet>,
    foods: List<MarketProduct>,
    medicines: List<MarketProduct>,
    groomingServices: List<GroomingServiceItem>,
    doctors: List<VerifiedDoctor>,
    cartItemCount: Int,
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenListPetModal: () -> Unit,
    onAddToCart: (MarketProduct) -> Unit,
    onBookDoctor: (VerifiedDoctor) -> Unit,
    onBookGrooming: (GroomingServiceItem) -> Unit,
    onRequestTrainer: () -> Unit = {},
    onPetSelected: (MarketPet) -> Unit,
    guideFoods: List<FoodItem> = emptyList(),
    accessories: List<AccessoryItem> = emptyList(),
    healthCareItems: List<HealthCareItem> = emptyList(),
    trainingGuides: List<TrainingGuide> = emptyList(),
    onGuideItemAction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("kerala_marketplace_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Kerala Hub Banner & City Selector
        item {
            KeralaHeroLocationBar(
                selectedCity = selectedCity,
                onSelectCity = onSelectCity,
                cities = cities,
                cartItemCount = cartItemCount,
                onOpenCart = onOpenCart,
                onOpenOrders = onOpenOrders
            )
        }

        // 2. Quick Action Buttons (List Pet & Vet Register)
        item {
            MarketplaceQuickActionRow(
                onOpenListPetModal = onOpenListPetModal,
            )
        }

        // 3. Category Tabs (Pet Listings, Food, Medicines, Grooming, Doctors)
        item {
            MarketplaceCategorySelector(
                selectedCategory = currentCategory,
                onSelectCategory = onSelectCategory
            )
        }

        // 4. Dynamic Content based on selected Category
        when (currentCategory) {
            MarketplaceCategory.PET_LISTINGS -> {
                item {
                    PetListingsHeader(
                        isExoticsOnly = isExoticsOnly,
                        onToggleExotics = onToggleExotics,
                        selectedSpecies = speciesFilter,
                        onSelectSpecies = onSelectSpeciesFilter
                    )
                }

                if (pets.isEmpty()) {
                    item {
                        if (!isExoticsOnly && speciesFilter == "All") {
                            EmptyMarketState(
                                message = stringResource(R.string.market_empty_no_listings),
                                actionLabel = stringResource(R.string.market_empty_action_post),
                                onAction = onOpenListPetModal
                            )
                        } else {
                            EmptyMarketState(
                                message = stringResource(R.string.market_empty_filtered, selectedCity),
                                actionLabel = stringResource(R.string.market_empty_action_clear),
                                onAction = {
                                    onToggleExotics(false)
                                    onSelectSpeciesFilter("All")
                                }
                            )
                        }
                    }
                } else {
                    items(pets, key = { it.id }) { pet ->
                        MarketPetCard(
                            pet = pet,
                            onClick = { onPetSelected(pet) }
                        )
                    }
                }
            }

            MarketplaceCategory.FOOD -> {
                item {
                    MarketSectionHeader(
                        title = "Kerala Pet Food & Nutrition",
                        subtitle = "Authentic premium kibble, gravies & fresh Kochi farm meat packs with same-day delivery in ₹",
                        icon = Icons.Default.Restaurant
                    )
                }

                items(foods, key = { it.id }) { food ->
                    MarketProductCard(
                        product = food,
                        onAddToCart = { onAddToCart(food) }
                    )
                }

                // Guide food items — combined into the same Food section
                items(guideFoods, key = { "g_" + it.name }) { item ->
                    FoodItemCard(item = item, onAddToList = { onGuideItemAction("Added " + item.name + " to cart/diet plan") })
                }
            }

            MarketplaceCategory.MEDICINES -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        MarketSectionHeader(
                            title = "Kerala Veterinary Pharmacy",
                            subtitle = "Authorized tick/flea prevention, dewormers, vitamins & prescription meds in INR (₹)",
                            icon = Icons.Default.MedicalServices
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = "Prescription Notice",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.market_kerala_state_animal_pharmacy_guidelines),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "All medicines are dispatched in climate-controlled tamper-evident pouches from licensed Kerala veterinary stockists.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }
                    }
                }

                items(medicines, key = { it.id }) { med ->
                    MarketProductCard(
                        product = med,
                        onAddToCart = { onAddToCart(med) }
                    )
                }
            }

            MarketplaceCategory.GROOMING_SERVICES -> {
                item {
                    MarketSectionHeader(
                        title = "In-Home Grooming & Pet Spa Vans",
                        subtitle = "Mobile AC grooming vans arrive at your doorstep in Kochi, Trivandrum, Kozhikode & Thrissur",
                        icon = Icons.Default.ContentCut
                    )
                }

                items(groomingServices, key = { it.id }) { service ->
                    GroomingServiceCard(
                        service = service,
                        onBook = { onBookGrooming(service) }
                    )
                }
            }

            MarketplaceCategory.VET_CONSULTATIONS -> {
                item {
                    MarketSectionHeader(
                        title = "Consult Doctor & Healthcare",
                        subtitle = "KSVC certified vets for tele-consults + vaccinations, checkups & treatments",
                        icon = Icons.Default.LocalHospital
                    )
                }

                items(doctors, key = { it.id }) { doctor ->
                    VerifiedDoctorCard(
                        doctor = doctor,
                        onBook = { onBookDoctor(doctor) }
                    )
                }

                // Healthcare services — combined into the same section
                items(healthCareItems, key = { "h_" + it.title }) { item ->
                    HealthCareItemCard(item = item, onBook = { onGuideItemAction("Booking appointment for " + item.title) })
                }
            }

            MarketplaceCategory.ACCESSORIES -> {
                item {
                    MarketSectionHeader(
                        title = "Pet Accessories",
                        subtitle = "Clothing, toys, wearables & more for your pet",
                        icon = Icons.Default.ShoppingCart
                    )
                }

                items(accessories, key = { "a_" + it.name }) { item ->
                    AccessoryItemCard(item = item, onBuy = { onGuideItemAction("Selected " + item.name + " (" + item.estimatedPrice + ")") })
                }
            }

            MarketplaceCategory.TRAINING -> {
                item {
                    MarketSectionHeader(
                        title = "Training Guides",
                        subtitle = "Basic to advanced training programs for your pet",
                        icon = Icons.Default.School
                    )
                }

                items(trainingGuides, key = { "t_" + it.title }) { guide ->
                    TrainingGuideCard(guide = guide)
                }

                item {
                    TrainerOnDemandCard(onRequestTrainer = onRequestTrainer)
                }
            }
        }
    }
}

/** WhatsApp contact for the Petpulse trainer desk. */
private const val TRAINER_WHATSAPP_NUMBER = "919526632311"

/**
 * Trainer On-Demand card in Market → Training:
 * "Request Trainer" creates a real Firestore booking (goes to the admin panel),
 * and the WhatsApp button chats directly with the trainer desk.
 */
@Composable
fun TrainerOnDemandCard(onRequestTrainer: () -> Unit) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TealLight),
        border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.SportsScore, contentDescription = null, tint = TealAccent, modifier = Modifier.size(30.dp))
                Column {
                    Text("Trainer On-Demand", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                    Text("Certified trainer for home obedience & behavior sessions", fontSize = 11.sp, color = TextGray)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRequestTrainer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Request Trainer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val message = "Hi Petpulse! I would like to know more about your home pet training sessions."
                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/$TRAINER_WHATSAPP_NUMBER?text=${Uri.encode(message)}")
                                )
                            )
                        } catch (_: Exception) { }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Text(
                "Request a trainer and our team will call you to confirm the schedule.",
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

@Composable
fun KeralaHeroLocationBar(
    selectedCity: String,
    onSelectCity: (String) -> Unit,
    cities: List<KeralaCity>,
    cartItemCount: Int,
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BluePrimary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Kerala Location",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.market_kerala_pet_care_vet_hub),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = if (selectedCity == "All Kerala") "Statewide Kerala (₹ INR)" else "$selectedCity, Kerala (₹ INR)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Orders Timeline Button
                    IconButton(
                        onClick = onOpenOrders,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Track Orders",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Cart Button with badge
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ) {
                                    Text("$cartItemCount")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenCart,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BluePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Slide Out Cart",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kerala City selection chips
            Text(
                text = stringResource(R.string.market_select_delivery_service_region),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cities) { city ->
                    val isSelected = selectedCity.equals(city.name, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCity(city.name) },
                        label = {
                            Text(
                                text = city.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MarketplaceQuickActionRow(
    onOpenListPetModal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onOpenListPetModal,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BluePrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.market_list_a_pet_owner),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MarketplaceCategorySelector(
    selectedCategory: MarketplaceCategory,
    onSelectCategory: (MarketplaceCategory) -> Unit
) {
    val categories = listOf(
        MarketplaceCategory.PET_LISTINGS to ("Pets" to Icons.Default.Pets),
        MarketplaceCategory.FOOD to ("Food" to Icons.Default.Restaurant),
        MarketplaceCategory.MEDICINES to ("Medicines" to Icons.Default.MedicalServices),
        MarketplaceCategory.GROOMING_SERVICES to ("Grooming" to Icons.Default.ContentCut),
        MarketplaceCategory.VET_CONSULTATIONS to ("Doctor & Health" to Icons.Default.LocalHospital),
        MarketplaceCategory.ACCESSORIES to ("Accessories" to Icons.Default.ShoppingCart),
        MarketplaceCategory.TRAINING to ("Training" to Icons.Default.School)
    )

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = BluePrimary,
        divider = {}
    ) {
        categories.forEach { (cat, info) ->
            val isSelected = selectedCategory == cat
            Tab(
                selected = isSelected,
                onClick = { onSelectCategory(cat) },
                text = {
                    Text(
                        text = info.first,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = info.second,
                        contentDescription = info.first,
                        modifier = Modifier.size(20.dp)
                    )
                },
                selectedContentColor = BluePrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PetListingsHeader(
    isExoticsOnly: Boolean,
    onToggleExotics: (Boolean) -> Unit,
    selectedSpecies: String,
    onSelectSpecies: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Exotics Filter Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isExoticsOnly) Color(0xFFEDE7F6) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isExoticsOnly) Color(0xFF673AB7) else Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isExoticsOnly) "🦜" else "🐾",
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.market_imported_exotic_pets_filter),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isExoticsOnly) Color(0xFF512DA8) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Macaws, Siberian Huskies, Persians, Bearded Dragons, CITES cleared",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isExoticsOnly,
                    onCheckedChange = onToggleExotics,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF673AB7)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Species Filter Chips
        val speciesList = listOf("All", "Dog", "Cat", "Bird", "Reptile", "Rabbit")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(speciesList) { species ->
                val isSelected = selectedSpecies.equals(species, ignoreCase = true)
                SuggestionChip(
                    onClick = { onSelectSpecies(species) },
                    label = {
                        Text(
                            text = species,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) BluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        labelColor = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
    }
}

@Composable
fun MarketPetCard(
    pet: MarketPet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("market_pet_card_${pet.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (pet.isImportedExotic) Color(0xFFF3E5F5) else BluePrimary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (pet.photoUris.isNotEmpty()) {
                        AsyncImage(
                            model = pet.photoUris.first(),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = when (pet.species.lowercase()) {
                                "dog" -> "🐶"
                                "cat" -> "🐱"
                                "bird" -> "🦜"
                                "reptile" -> "🦎"
                                "rabbit" -> "🐰"
                                else -> "🐾"
                            },
                            fontSize = 24.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pet.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (pet.listingType == "Adoption") Color(0xFFE8F5E9) else BluePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = pet.listingType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pet.listingType == "Adoption") Color(0xFF2E7D32) else BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${pet.breed} • ${pet.age} • ${pet.gender}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (pet.listingType == "Adoption") {
                        Text(
                            text = stringResource(R.string.market_free),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = stringResource(R.string.market_adoption_in_kerala),
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${pet.priceInr.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = BluePrimaryDark
                            )
                            if (pet.originalPriceInr != null && pet.originalPriceInr > pet.priceInr) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${pet.originalPriceInr.toInt()}",
                                    fontSize = 11.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Extra photos strip (2nd onwards) — replaces single emoji avatar only
            if (pet.photoUris.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(pet.photoUris.drop(1)) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(88.dp, 66.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (pet.isImportedExotic) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEDE7F6)
                    ) {
                        Text(
                            text = "✨ ${pet.importCountry ?: "Imported/Exotic"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF512DA8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE0F2F1)
                ) {
                    Text(
                        text = "📍 ${pet.city}, Kerala",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00796B),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Text(
                        text = stringResource(R.string.market_escrow_protected),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF57F17),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pet.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seller: ${pet.sellerName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text(
                        text = if (pet.listingType == "Adoption") "Adopt Pet" else "Reserve with Escrow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MarketProductCard(
    product: MarketProduct,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (product.isMedicine) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (product.isMedicine) Icons.Default.Medication else Icons.Default.ShoppingBag,
                            contentDescription = product.name,
                            tint = if (product.isMedicine) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = product.brand.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${product.packSize} • ⭐ ${product.rating} (${product.reviewsCount})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${product.priceInr.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = BluePrimaryDark
                    )
                    if (product.originalPriceInr > product.priceInr) {
                        Text(
                            text = "₹${product.originalPriceInr.toInt()}",
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (product.prescriptionRequired) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = stringResource(R.string.market_prescription_required_attach_at_checkout),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Express delivery",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.market_s2_4_hr_delivery_in_kerala),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.market_add_to_cart),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GroomingServiceCard(
    service: GroomingServiceItem,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE0F7FA)
                    ) {
                        Text(
                            text = if (service.isInHomeVan) "🚐 AT-DOORSTEP GROOMING VAN" else "🏬 CLINIC SPA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF006064),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = service.subTitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${service.priceInr.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BluePrimaryDark
                    )
                    Text(
                        text = "${service.durationMinutes} mins",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Perks checklist
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                service.perks.take(4).forEach { perk ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = perk,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.market_cities_kochi_tvm_kozhikode_thrissur),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onBook,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text(
                        text = stringResource(R.string.market_book_doorstep_van),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VerifiedDoctorCard(
    doctor: VerifiedDoctor,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("doctor_card_${doctor.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = doctor.name,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = doctor.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "KSVC Verified",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${doctor.degrees} • ${doctor.experienceYears}+ yrs exp",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = doctor.specialization,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Council Reg: ${doctor.ksvcRegNumber}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📍 ${doctor.clinicCity}, Kerala",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Video: ₹${doctor.videoConsultFeeInr.toInt()} | Clinic: ₹${doctor.inPersonConsultFeeInr.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimaryDark
                    )
                    Text(
                        text = doctor.availableDays,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onBook,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text(
                        text = stringResource(R.string.market_book_slot),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MarketSectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyMarketState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🔍",
                fontSize = 32.sp
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(actionLabel, fontSize = 12.sp)
            }
        }
    }
}

