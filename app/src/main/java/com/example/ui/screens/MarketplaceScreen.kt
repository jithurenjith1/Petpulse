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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.window.Dialog
import com.petpulse.app.data.model.*
import com.petpulse.app.ui.components.StarRow
import com.petpulse.app.ui.components.WriteReviewDialog
import com.petpulse.app.ui.components.formatRating
import com.petpulse.app.ui.components.formatReviewDate
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
    // Owner-added accessories, mapped through ShopProduct.toMarketProduct() so the
    // photos + description survive (see PetViewModel.marketAccessoriesFiltered).
    accessoryProducts: List<MarketProduct> = emptyList(),
    accessorySubCategory: String = "All",
    onSelectAccessorySubCategory: (String) -> Unit = {},
    onAddAccessoryToCart: (AccessoryItem) -> Unit = {},
    onAddFoodItemToCart: (FoodItem) -> Unit = {},
    onBookHealthCare: (HealthCareItem) -> Unit = {},
    healthCareItems: List<HealthCareItem> = emptyList(),
    trainingGuides: List<TrainingGuide> = emptyList(),
    onGuideItemAction: (String) -> Unit = {},
    currentUid: String? = null,
    onDeleteListing: (MarketPet) -> Unit = {},
    foodPetType: String = "All",
    onSelectFoodPetType: (String) -> Unit = {},
    foodKind: String = "All",
    onSelectFoodKind: (String) -> Unit = {},
    foodStage: String = "All",
    onSelectFoodStage: (String) -> Unit = {},
    reviews: List<Review> = emptyList(),
    myOrders: List<AdminOrder> = emptyList(),
    onSubmitReview: (MarketProduct, Int, String, String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var listingToDelete by remember { mutableStateOf<MarketPet?>(null) }

    // Photo-viewer state is HOISTED above the LazyColumn so no remember(...) ever
    // lives inside a lazy content lambda.
    var productViewerPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var productViewerStart by remember { mutableIntStateOf(0) }
    // Hoisted above the lazy containers: tapping an accessories grid cell opens
    // the product detail page rendered as an overlay below. State is never
    // remembered inside a lazy content lambda.
    var detailProduct by remember { mutableStateOf<MarketProduct?>(null) }
    // Real review aggregates (average + count) keyed by productId, and the
    // customer's DELIVERED orders. Both are computed once HERE - above the lazy
    // containers - so no remember(...) ever lives inside a lazy content lambda.
    val reviewStats = remember(reviews) { buildReviewStats(reviews) }
    val myDeliveredOrders = remember(myOrders) { myOrders.filter { it.status == "DELIVERED" } }

    Box(modifier = modifier.fillMaxSize()) {
    if (currentCategory == MarketplaceCategory.ACCESSORIES) {
        // 2-column product grid (image, name, price) keyed by product id, with a
        // sub-category chip row - the romsnraks-style category page.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("marketplace_accessories_grid"),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                KeralaHeroLocationBar(
                    selectedCity = selectedCity,
                    onSelectCity = onSelectCity,
                    cities = cities,
                    cartItemCount = cartItemCount,
                    onOpenCart = onOpenCart,
                    onOpenOrders = onOpenOrders
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                MarketplaceQuickActionRow(onOpenListPetModal = onOpenListPetModal)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                MarketplaceCategorySelector(
                    selectedCategory = currentCategory,
                    onSelectCategory = onSelectCategory
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                MarketSectionHeader(
                    title = "Pet Accessories",
                    subtitle = "Clothing, toys, wearables & more for your pet",
                    icon = Icons.Default.ShoppingCart
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AccessoryFilterRow(
                    label = stringResource(R.string.market_accessory_filter_label),
                    selected = accessorySubCategory,
                    onSelect = onSelectAccessorySubCategory
                )
            }
            if (accessoryProducts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyMarketState(
                        message = stringResource(R.string.market_accessory_empty_filtered),
                        actionLabel = stringResource(R.string.market_empty_action_clear),
                        onAction = { onSelectAccessorySubCategory("All") }
                    )
                }
            }
            gridItems(accessoryProducts, key = { it.id }) { product ->
                val stat = reviewStats[product.id]
                MarketProductGridCell(
                    product = product,
                    reviewAverage = stat?.average ?: 0.0,
                    reviewCount = stat?.count ?: 0,
                    onClick = { detailProduct = product },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    } else {
    LazyColumn(
        modifier = Modifier
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

        // 2. Quick Action Button (List Pet)
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
                            isOwner = currentUid != null && pet.ownerId == currentUid,
                            onClick = { onPetSelected(pet) },
                            onDelete = { listingToDelete = pet }
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

                item {
                    FoodFilterRow(
                        label = "SHOP BY PET",
                        options = listOf("All", "Dog", "Cat", "Other"),
                        selected = foodPetType,
                        onSelect = onSelectFoodPetType
                    )
                }
                item {
                    FoodFilterRow(
                        label = "FOOD TYPE",
                        options = listOf("All", "Dry", "Wet", "Treats", "Supplements"),
                        selected = foodKind,
                        onSelect = onSelectFoodKind
                    )
                }
                item {
                    FoodFilterRow(
                        label = "LIFE STAGE",
                        options = listOf("All", "Puppy & Kitten", "Adult", "Senior"),
                        selected = foodStage,
                        onSelect = onSelectFoodStage
                    )
                }

                if (foods.isEmpty()) {
                    item {
                        EmptyMarketState(
                            message = "No food products match these filters.",
                            actionLabel = "Clear filters",
                            onAction = {
                                onSelectFoodPetType("All")
                                onSelectFoodKind("All")
                                onSelectFoodStage("All")
                            }
                        )
                    }
                }

                items(foods, key = { it.id }) { food ->
                    val stat = reviewStats[food.id]
                    MarketProductCard(
                        product = food,
                        reviewAverage = stat?.average ?: 0.0,
                        reviewCount = stat?.count ?: 0,
                        onClick = { detailProduct = food },
                        onAddToCart = { onAddToCart(food) },
                        onViewPhotos = { photos, start ->
                            productViewerPhotos = photos
                            productViewerStart = start
                        }
                    )
                }

                // Guide food items — combined into the same Food section
                items(guideFoods, key = { "g_" + it.name }) { item ->
                    FoodItemCard(
                        item = item,
                        onAddToList = {
                            onAddFoodItemToCart(item)
                            onGuideItemAction("Added " + item.name + " to cart")
                        }
                    )
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (medicines.isEmpty()) {
                    item {
                        EmptyMarketState(message = stringResource(R.string.market_empty_no_products))
                    }
                }

                items(medicines, key = { it.id }) { med ->
                    val stat = reviewStats[med.id]
                    MarketProductCard(
                        product = med,
                        reviewAverage = stat?.average ?: 0.0,
                        reviewCount = stat?.count ?: 0,
                        onClick = { detailProduct = med },
                        onAddToCart = { onAddToCart(med) },
                        onViewPhotos = { photos, start ->
                            productViewerPhotos = photos
                            productViewerStart = start
                        }
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

                if (groomingServices.isEmpty()) {
                    item {
                        EmptyMarketState(message = stringResource(R.string.market_empty_no_services))
                    }
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

                if (doctors.isEmpty() && healthCareItems.isEmpty()) {
                    item {
                        EmptyMarketState(message = stringResource(R.string.market_empty_no_vets))
                    }
                }

                items(doctors, key = { it.id }) { doctor ->
                    VerifiedDoctorCard(
                        doctor = doctor,
                        onBook = { onBookDoctor(doctor) }
                    )
                }

                // Healthcare services — combined into the same section
                items(healthCareItems, key = { "h_" + it.title }) { item ->
                    HealthCareItemCard(
                        item = item,
                        onBook = {
                            onBookHealthCare(item)
                            onGuideItemAction("Booking requested for " + item.title)
                        }
                    )
                }
            }

            // Rendered as the 2-column LazyVerticalGrid above (the ACCESSORIES
            // branch at the top of this composable) - nothing to add here.
            MarketplaceCategory.ACCESSORIES -> {}

            MarketplaceCategory.TRAINING -> {
                item {
                    MarketSectionHeader(
                        title = "Training Guides",
                        subtitle = "Basic to advanced training programs for your pet",
                        icon = Icons.Default.School
                    )
                }

                if (trainingGuides.isEmpty()) {
                    item {
                        EmptyMarketState(message = stringResource(R.string.market_empty_no_training))
                    }
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
    }  // end else: non-accessories LazyColumn

    // Product detail overlay - opens when an accessories grid cell is tapped.
    detailProduct?.let { product ->
        val productReviews = reviews.filter { it.productId == product.id }
        val productStat = reviewStats[product.id]
        // Delivered-only gate: the customer may review this product ONLY if one of
        // their own orders with status DELIVERED contains this product. Order line
        // items carry the product NAME (see placeOrder), so match on the name.
        val matchingDeliveredOrder = myDeliveredOrders.firstOrNull { order ->
            order.items.any { it.name.trim().equals(product.name.trim(), ignoreCase = true) }
        }
        MarketProductDetailScreen(
            product = product,
            reviews = productReviews,
            reviewAverage = productStat?.average ?: 0.0,
            reviewCount = productStat?.count ?: 0,
            canReview = matchingDeliveredOrder != null,
            onSubmitReview = { rating, text ->
                onSubmitReview(product, rating, text, matchingDeliveredOrder?.id ?: "")
            },
            onBack = { detailProduct = null },
            onAddToCart = {
                onAddToCart(product)
                detailProduct = null
            },
            onViewPhotos = { photos, start ->
                productViewerPhotos = photos
                productViewerStart = start
            }
        )
    }
    }  // end Box

    if (productViewerPhotos.isNotEmpty()) {
        MarketPhotoViewerDialog(
            photos = productViewerPhotos,
            startIndex = productViewerStart,
            onDismiss = { productViewerPhotos = emptyList() }
        )
    }

    listingToDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Remove Listing") },
            text = { Text("Remove \"${pet.name}\" from the pet market? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteListing(pet)
                        listingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD62828))
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

/** WhatsApp contact for the Wagmiya trainer desk. */
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
                    Text("Trainer On-Demand", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("Certified trainer for home obedience & behavior sessions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRequestTrainer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Request Trainer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val message = "Hi Wagmiya! I would like to know more about your home pet training sessions."
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Kerala Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.market_kerala_pet_care_vet_hub),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                                .background(MaterialTheme.colorScheme.primary)
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
                items(cities, key = { it.id }) { city ->
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
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
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
        contentColor = MaterialTheme.colorScheme.primary,
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
                selectedContentColor = MaterialTheme.colorScheme.primary,
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
                containerColor = if (isExoticsOnly) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant
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
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
    }
}

@Composable
fun MarketPetCard(
    pet: MarketPet,
    isOwner: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    // Cloudinary URLs first; fall back to the base64-decoded local files.
    val petPhotos = if (pet.photoUrls.isNotEmpty()) pet.photoUrls else pet.photoUris
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
                            if (pet.isImportedExotic) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (petPhotos.isNotEmpty()) {
                        AsyncImage(
                            model = petPhotos.first(),
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
                            color = if (pet.listingType == "Adoption") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = pet.listingType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pet.listingType == "Adoption") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
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
                                color = MaterialTheme.colorScheme.primary
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

                if (isOwner) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete my listing",
                            tint = Color(0xFFD62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Extra photos strip (2nd onwards) — replaces single emoji avatar only
            if (petPhotos.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(petPhotos.drop(1)) { uri ->
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
                        color = MaterialTheme.colorScheme.surfaceVariant
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
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "📍 ${pet.city}, Kerala",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (pet.listingType == "Adoption") "Adopt Pet" else "Reserve Pet",
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
    onAddToCart: () -> Unit,
    onClick: () -> Unit = {},
    reviewAverage: Double = 0.0,
    reviewCount: Int = 0,
    onViewPhotos: (List<String>, Int) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Prefer the hosted Cloudinary URLs; fall back to the local base64 photos.
            val productPhotos = if (product.photoUrls.isNotEmpty()) product.photoUrls else product.photoUris
            val photoCount = productPhotos.size

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
                            .size(92.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            .then(
                                if (photoCount > 0) Modifier.clickable { onViewPhotos(productPhotos, 0) } else Modifier
                            )
                            .testTag("product_photo_${product.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoCount > 0) {
                            AsyncImage(
                                model = productPhotos.first(),
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (photoCount > 1) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.market_photo_counter, 1, photoCount),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = if (product.isMedicine) Icons.Default.Medication else Icons.Default.ShoppingBag,
                                contentDescription = product.name,
                                tint = if (product.isMedicine) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = product.brand.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                            text = product.packSize,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (reviewCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFA87A1F),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.review_card_summary, formatRating(reviewAverage), reviewCount),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.review_new_badge),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${product.priceInr.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
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

            // Thumbnail strip for photos 2..N - mirrors the market-pet gallery strip.
            // Each thumbnail opens the full-screen viewer at that index.
            if (photoCount > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(productPhotos.drop(1)) { offset, uri ->
                        val index = offset + 1
                        AsyncImage(
                            model = uri,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onViewPhotos(productPhotos, index) }
                                .testTag("product_thumb_${product.id}_$index")
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
                    color = MaterialTheme.colorScheme.surfaceVariant
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (service.isInHomeVan) "🚐 AT-DOORSTEP GROOMING VAN" else "🏬 CLINIC SPA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.primary
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
                            .background(MaterialTheme.colorScheme.surfaceVariant),
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
                            if (doctor.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "KSVC Verified",
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
                            color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = doctor.availableDays,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = onBook,
                        enabled = doctor.isOnline,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.market_book_slot),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(if (doctor.isOnline) R.string.vet_online_now else R.string.vet_offline),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (doctor.isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!doctor.isOnline) {
                        Text(
                            text = stringResource(R.string.vet_offline_try_later),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
    actionLabel: String = "",
    onAction: () -> Unit = {}
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
            if (actionLabel.isNotBlank()) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(actionLabel, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Horizontal chip filter row used in the Food section. */
@Composable
private fun FoodFilterRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { opt ->
                FilterChip(
                    selected = selected == opt,
                    onClick = { onSelect(opt) },
                    label = { Text(opt, fontSize = 11.sp) }
                )
            }
        }
    }
}

/**
 * Sub-category chip row for the Accessories section. Mirrors [FoodFilterRow]
 * (label + LazyRow of FilterChips) but keeps a canonical English value per chip
 * so filtering still matches [MarketProduct.subCategory] while the visible label
 * stays localisable.
 */
@Composable
private fun AccessoryFilterRow(
    label: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "All" to R.string.market_filter_all,
        "Toys" to R.string.market_accessory_sub_toys,
        "Bed" to R.string.market_accessory_sub_bed,
        "Clothing" to R.string.market_accessory_sub_clothing,
        "Bowls" to R.string.market_accessory_sub_bowls,
        "Cage & Carrier" to R.string.market_accessory_sub_cage,
        "Others" to R.string.market_accessory_sub_others
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                val value = option.first
                val labelRes = option.second
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(stringResource(labelRes), fontSize = 11.sp) }
                )
            }
        }
    }
}

/**
 * One cell of the 2-column accessories grid: square photo (fills the cell),
 * product name (max 2 lines) and the price in bold magenta. No description here -
 * that lives on [MarketProductDetailScreen]. A clean paw placeholder is shown
 * when the product has no photo.
 */
@Composable
fun MarketProductGridCell(
    product: MarketProduct,
    onClick: () -> Unit,
    reviewAverage: Double = 0.0,
    reviewCount: Int = 0,
    modifier: Modifier = Modifier
) {
    // Cloudinary URL first, base64 fallback - same rule as MarketProductCard.
    val photos = if (product.photoUrls.isNotEmpty()) product.photoUrls else product.photoUris
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_grid_cell_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photos.isNotEmpty()) {
                    AsyncImage(
                        model = photos.first(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = stringResource(R.string.market_product_no_photo),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${product.priceInr.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFA87A1F),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = stringResource(R.string.review_card_summary, formatRating(reviewAverage), reviewCount),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.review_new_badge),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Full product detail page (romsnraks-style): large photo + multi-photo viewer,
 * name, price, a short line, Add to Cart, then collapsible Description and
 * Additional Details sections.
 */
@Composable
fun MarketProductDetailScreen(
    product: MarketProduct,
    onBack: () -> Unit,
    onAddToCart: () -> Unit,
    reviews: List<Review> = emptyList(),
    reviewAverage: Double = 0.0,
    reviewCount: Int = 0,
    canReview: Boolean = false,
    onSubmitReview: (Int, String) -> Unit = { _, _ -> },
    onViewPhotos: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val photos = if (product.photoUrls.isNotEmpty()) product.photoUrls else product.photoUris
    val emptyDescription = stringResource(R.string.market_detail_no_description)
    var descriptionExpanded by remember { mutableStateOf(false) }
    var detailsExpanded by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showReviewDialog) {
        WriteReviewDialog(
            title = stringResource(R.string.review_dialog_title),
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, text ->
                onSubmitReview(rating, text)
                showReviewDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("product_detail_${product.id}"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.market_detail_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .then(
                            if (photos.isNotEmpty()) Modifier.clickable { onViewPhotos(photos, 0) }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (photos.isNotEmpty()) {
                        AsyncImage(
                            model = photos.first(),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (photos.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.market_photo_counter, 1, photos.size),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = stringResource(R.string.market_product_no_photo),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                if (photos.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        itemsIndexed(photos) { index, uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onViewPhotos(photos, index) }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "₹${product.priceInr.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.brand + " • " + product.packSize,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.market_add_to_cart),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    CollapsibleDetailSection(
                        title = stringResource(R.string.market_detail_description),
                        expanded = descriptionExpanded,
                        onToggle = { descriptionExpanded = !descriptionExpanded }
                    ) {
                        Text(
                            text = product.description.ifBlank { emptyDescription },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    CollapsibleDetailSection(
                        title = stringResource(R.string.market_detail_additional_details),
                        expanded = detailsExpanded,
                        onToggle = { detailsExpanded = !detailsExpanded }
                    ) {
                        DetailRow(stringResource(R.string.market_detail_brand), product.brand)
                        DetailRow(stringResource(R.string.market_detail_category), product.category)
                        if (product.subCategory.isNotBlank()) {
                            DetailRow(stringResource(R.string.market_detail_type), product.subCategory)
                        }
                        DetailRow(stringResource(R.string.market_detail_pet_type), product.petType)
                        DetailRow(stringResource(R.string.market_detail_pack_size), product.packSize)
                        DetailRow(
                            stringResource(R.string.market_detail_availability),
                            if (product.stockCount > 0) stringResource(R.string.market_detail_in_stock)
                            else stringResource(R.string.market_detail_out_of_stock)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    RatingsAndReviewsSection(
                        reviews = reviews,
                        average = reviewAverage,
                        count = reviewCount,
                        canReview = canReview,
                        onWriteReview = { showReviewDialog = true }
                    )
                }
            }
        }
    }
}

/** A collapsible "Description" / "Additional Details" card with a +/- toggle. */
@Composable
private fun CollapsibleDetailSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/** A label/value row used inside the Additional Details section. */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Full-screen swipeable viewer for a product's uploaded photos.
 *
 * Reuses the same pager pattern as the market-pet gallery / onboarding slides:
 * a HorizontalPager driven by rememberPagerState, a "1/N" counter and a close
 * button. Opened by tapping the product card photo or any thumbnail. Images use
 * ContentScale.Fit so nothing is cropped in the large view.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketPhotoViewerDialog(
    photos: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit = {}
) {
    if (photos.isEmpty()) return

    val safeStart = startIndex.coerceIn(0, photos.size - 1)
    val pagerState = rememberPagerState(initialPage = safeStart, pageCount = { photos.size })

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.market_photo_viewer_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.market_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = productPhotoDescription(page, photos.size),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.market_photo_counter,
                    pagerState.currentPage + 1,
                    photos.size
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Simple accessibility label for a photo page, e.g. "Photo 2 of 3". */
private fun productPhotoDescription(page: Int, total: Int): String = "Photo " + (page + 1) + " of " + total

/** Aggregated star rating for one product id. */
private data class ReviewStat(val average: Double = 0.0, val count: Int = 0)

/** Groups reviews by productId into an average + count map (ratings clamped 1..5). */
private fun buildReviewStats(reviews: List<Review>): Map<String, ReviewStat> {
    val grouped = reviews.filter { it.productId.isNotBlank() }.groupBy { it.productId }
    return grouped.mapValues { (_, list) ->
        val valid = list.filter { it.rating in 1..5 }
        if (valid.isEmpty()) ReviewStat() else ReviewStat(valid.map { it.rating }.average(), valid.size)
    }
}

/**
 * "Ratings & Reviews" card for the product detail page: average stars + count at
 * the top, a write action when the customer has a DELIVERED order for the product
 * (read-only otherwise), then the individual reviews or an honest empty state.
 */
@Composable
private fun RatingsAndReviewsSection(
    reviews: List<Review>,
    average: Double,
    count: Int,
    canReview: Boolean,
    onWriteReview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.review_section_title),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRow(rating = average.toInt(), starSize = 16)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.review_summary, formatRating(average), count),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (canReview) {
                Button(
                    onClick = onWriteReview,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.review_write_action),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.review_verified_only),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (reviews.isEmpty()) {
                Text(
                    text = stringResource(R.string.review_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                reviews.forEach { review ->
                    ReviewRow(review = review)
                }
            }
        }
    }
}

/** One review: author, star row, optional text and date. */
@Composable
private fun ReviewRow(review: Review) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = review.authorName.ifBlank { stringResource(R.string.review_anonymous) },
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatReviewDate(review.createdAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StarRow(rating = review.rating, starSize = 12)
        if (review.text.isNotBlank()) {
            Text(
                text = review.text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
