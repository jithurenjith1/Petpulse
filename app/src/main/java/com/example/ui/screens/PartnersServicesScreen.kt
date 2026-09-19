package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PartnerSubTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersServicesScreen(
    currentSubTab: PartnerSubTab,
    onSelectSubTab: (PartnerSubTab) -> Unit,
    boardingType: String,
    onSelectBoardingType: (String) -> Unit,
    groomingCenters: List<GroomingCenter>,
    foodSubscriptions: List<FoodSubscription>,
    boardingSitters: List<BoardingSitter>,
    lostPetAlerts: List<LostPetAlert>,
    petListings: List<PetListing>,
    petNews: PetNewsItem,
    events: List<PetEventItem>,
    onTriggerSosDialog: () -> Unit,
    onAddListingDialog: () -> Unit,
    onPartnerJoinClick: (String) -> Unit,
    onActionNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBusinessPartnerDialog by remember { mutableStateOf(false) }
    var partnerCategoryToJoin by remember { mutableStateOf("Grooming Salon") }
    var showPartnerCategoryMenu by remember { mutableStateOf(false) }
    var showFeaturedPlansDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("partners_services_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Partner Hub Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimaryDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentAmber)
                            Text(
                                text = stringResource(R.string.partners_verified_business_care_network),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Partner grooming salons, curated subscriptions, 24hr sitters (Dora), 5km Lost Pet SOS & GPS tagging.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD1E4FF)
                    )

                    Button(
                        onClick = { showPartnerCategoryMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.partners_join_as_business_partner), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showPartnerCategoryMenu,
                        onDismissRequest = { showPartnerCategoryMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.partners_food_accessories)) }, onClick = { partnerCategoryToJoin = "Food & Accessories"; showPartnerCategoryMenu = false; showBusinessPartnerDialog = true })
                        DropdownMenuItem(text = { Text(stringResource(R.string.partners_pet_trainers)) }, onClick = { partnerCategoryToJoin = "Pet Trainers"; showPartnerCategoryMenu = false; showBusinessPartnerDialog = true })
                        DropdownMenuItem(text = { Text(stringResource(R.string.partners_veterinary_doctors_clinics)) }, onClick = { partnerCategoryToJoin = "Veterinary Doctors & Clinics"; showPartnerCategoryMenu = false; showBusinessPartnerDialog = true })
                        DropdownMenuItem(text = { Text(stringResource(R.string.partners_boarding)) }, onClick = { partnerCategoryToJoin = "Boarding"; showPartnerCategoryMenu = false; showBusinessPartnerDialog = true })
                        DropdownMenuItem(text = { Text(stringResource(R.string.partners_sales)) }, onClick = { partnerCategoryToJoin = "Sales"; showPartnerCategoryMenu = false; showBusinessPartnerDialog = true })
                    }
                }
            }
        }

        // 1b. Featured Listings Promotion (Revenue)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6E8)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC9A227))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFC9A227),
                        modifier = Modifier.size(30.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.partners_get_featured_top_placement),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF8A6D1C)
                        )
                        Text(
                            "Appear at the top of grooming, boarding & vet searches. Plans from ₹999/month.",
                            fontSize = 11.sp,
                            color = Color(0xFF7A6A45)
                        )
                    }
                    FilledTonalButton(
                        onClick = { showFeaturedPlansDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFC9A227),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(stringResource(R.string.partners_promote), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Navigation Horizontal Tabs for Partners & Services
        item {
            ScrollableTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("partner_subtabs")
            ) {
                PartnerSubTab.values().forEach { tab ->
                    val isSelected = currentSubTab == tab
                    val title = when (tab) {
                        PartnerSubTab.GROOMING_CENTERS -> "💈 1. Grooming"
                        PartnerSubTab.FOOD_SUBSCRIPTION -> "📦 2. Subscriptions"
                        PartnerSubTab.PET_BOARDING -> "🏡 3. Boarding & Sitters"
                        PartnerSubTab.FIND_MY_PET -> "🚨 4. Find My Pet / GPS"
                        PartnerSubTab.SALE_AND_ADOPTION -> "🐾 5. Sale & Adoption"
                        PartnerSubTab.NEWS_AND_EVENTS -> "📰 6. News & Events"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectSubTab(tab) },
                        modifier = Modifier.padding(end = 8.dp),
                        text = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isSelected) 3.dp else 0.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // 3. Submenu Dynamic Content
        when (currentSubTab) {
            PartnerSubTab.GROOMING_CENTERS -> {
                // Section 1: Grooming Centers Nearby
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.partners_nearby_partner_grooming_salons),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        Text(
                            text = "Verified grooming entities registered with our partner network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(groomingCenters.sortedByDescending { it.isFeaturedPartner }) { center ->
                    GroomingCenterCard(
                        center = center,
                        onBookClick = { onActionNotification("Booking appointment at ${center.name}") },
                        onCallClick = { onActionNotification("Calling ${center.phone}") }
                    )
                }

                // Partner Join Callout
                item {
                    PartnerJoinCalloutCard(
                        title = "Own a Pet Grooming Center?",
                        description = "Join Jane & Pals as a certified Grooming Business Entity to receive direct customer bookings.",
                        onJoinClick = {
                            partnerCategoryToJoin = "Grooming Salon"
                            showBusinessPartnerDialog = true
                        }
                    )
                }
            }

            PartnerSubTab.FOOD_SUBSCRIPTION -> {
                // Section 2: Food & Accessories Subscription
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.partners_pet_food_combos_accessories_subscriptions),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        Text(
                            text = "Choose monthly or yearly recurring bundles with verified partner brands",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(foodSubscriptions) { sub ->
                    SubscriptionCard(
                        subscription = sub,
                        onSubscribe = { onActionNotification("Selected ${sub.title} (${sub.planType})") }
                    )
                }
            }

            PartnerSubTab.PET_BOARDING -> {
                // Section 3: Pet Boarding (4 Submenus)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.partners_pet_boarding_sitter_network),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )

                        // 4 Boarding Submenus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Full Day (24hr)",
                                "Per Day Care",
                                "Pet Night Care",
                                "Feed on Time Only"
                            ).forEach { type ->
                                FilterChip(
                                    selected = boardingType == type,
                                    onClick = { onSelectBoardingType(type) },
                                    label = { Text(type, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("boarding_filter_$type")
                                )
                            }
                        }
                    }
                }

                val filteredSitters = boardingSitters.filter { it.sitterType == boardingType }
                items(filteredSitters) { sitter ->
                    BoardingSitterCard(
                        sitter = sitter,
                        onBook = { onActionNotification("Booked ${sitter.name} for ${sitter.sitterType}") }
                    )
                }
            }

            PartnerSubTab.FIND_MY_PET -> {
                // Section 4: Find My Pet (5km SOS alert & GPS Tag)
                item {
                    FindMyPetSection(
                        lostAlerts = lostPetAlerts,
                        onBroadcastClick = onTriggerSosDialog,
                        onCallHelpline = { onActionNotification("Connecting to 24/7 Pet Helpline +1 (800) 555-PET-SOS") }
                    )
                }
            }

            PartnerSubTab.SALE_AND_ADOPTION -> {
                // Section 5: Pet for Sale & Adoption
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.partners_adoption_pet_listings),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimaryDark
                            )
                            Text(
                                text = "Post rescue adoptions or browse verified listings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onAddListingDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("post_pet_listing_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.partners_post_pet), fontSize = 12.sp)
                        }
                    }
                }

                items(petListings) { listing ->
                    PetListingCard(
                        listing = listing,
                        onContact = { onActionNotification("Contacting ${listing.postedBy} at ${listing.contactNumber}") }
                    )
                }
            }

            PartnerSubTab.NEWS_AND_EVENTS -> {
                // Section 6: Pet News, Competitions & Trainers on Demand
                item {
                    PetNewsAndEventsSection(
                        news = petNews,
                        events = events,
                        onRegisterEvent = { event -> onActionNotification("Registered for ${event.title}") },
                        onRequestTrainer = { onActionNotification("Trainer On-Demand requested. A certified trainer will call you!") }
                    )
                }
            }
        }
    }

    if (showFeaturedPlansDialog) {
        FeaturedPlansDialog(
            onDismiss = { showFeaturedPlansDialog = false },
            onSubscribe = { planName ->
                showFeaturedPlansDialog = false
                onPartnerJoinClick("Featured plan requested: $planName! Our team will contact you.")
            }
        )
    }

    if (showBusinessPartnerDialog) {
        BusinessPartnerJoinDialog(
            initialCategory = partnerCategoryToJoin,
            onDismiss = { showBusinessPartnerDialog = false },
            onSubmit = { name, category, city, phone ->
                onPartnerJoinClick("Application submitted for $name ($category) in $city!")
                showBusinessPartnerDialog = false
            }
        )
    }
}

// ---------------- 1. Grooming Card ----------------
@Composable
fun GroomingCenterCard(
    center: GroomingCenter,
    onBookClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Show studio photo for Jane's Grooming Studio
            if (center.name.contains("Jane's")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_grooming_studio),
                        contentDescription = "Jane's Grooming Studio Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BluePrimary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.partners_featured_partner),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(center.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BluePrimaryDark)
                        if (center.verified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                    Text(stringResource(R.string.partners_verified), fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Text(center.tagLine, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                    Text("${center.rating}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("(${center.reviewCount})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                Text("${center.address} • ${center.distance}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF4F8FD),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.partners_popular_packages), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    center.packages.forEach { pkg ->
                        Text("• $pkg", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.partners_starting_from), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(center.startingPrice, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BluePrimary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCallClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.partners_call), fontSize = 11.sp)
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(stringResource(R.string.partners_book_spa_slot), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ---------------- 2. Subscriptions Card ----------------
@Composable
fun SubscriptionCard(
    subscription: FoodSubscription,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (subscription.planType == "Yearly") Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "${subscription.planType.uppercase()} RECURRING PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subscription.planType == "Yearly") AccentGreen else BluePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subscription.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = subscription.savingsTag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB76E00),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text("Combo Items: ${subscription.comboContents}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Partner Brands: ${subscription.brandsIncluded}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(subscription.monthlyEstimate, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BluePrimary)

                Button(
                    onClick = onSubscribe,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(stringResource(R.string.partners_select_subscription), fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------- 3. Boarding Sitter Card ----------------
@Composable
fun BoardingSitterCard(
    sitter: BoardingSitter,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(sitter.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
                            if (sitter.verified) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = AccentGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(sitter.experience, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                    Text("${sitter.rating}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Text(sitter.tagline, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF7FAFD),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sitter.features.forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                            Text(feat, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sitter.priceEstimate, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BluePrimary)

                Button(
                    onClick = onBook,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(stringResource(R.string.partners_book_care), fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------- 4. Find My Pet & GPS Tag ----------------
@Composable
fun FindMyPetSection(
    lostAlerts: List<LostPetAlert>,
    onBroadcastClick: () -> Unit,
    onCallHelpline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 5km SOS Alert Broadcast Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(26.dp))
                        Text(stringResource(R.string.partners_find_my_pet_5km_sos_network), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB71C1C))
                    }
                }

                Text(
                    text = "If your pet is lost or stolen, broadcast an instant push notification to all app users within a 5 km radius.",
                    fontSize = 12.sp,
                    color = Color(0xFF4A148C)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBroadcastClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("trigger_sos_5km_dialog")
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.partners_broadcast_5km_sos), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCallHelpline,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.partners_helpline), fontSize = 12.sp)
                    }
                }
            }
        }

        // Sponsored Hardware: Pet GPS Tag Showcase (Dummy model without price)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BluePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.partners_hardware_sponsor_coming_soon),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(stringResource(R.string.partners_smart_collar_gps_tracker_prototype), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
                    }
                }

                // Render image of GPS Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gps_tag),
                        contentDescription = "Pet GPS Tracker Tag Dummy Model",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Real-time satellite tracking, geo-fence escape alarms, and IP68 waterproof design. Sponsored company models & pricing will be announced in upcoming release.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.partners_price_tba_sponsored_partner), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    FilledTonalButton(onClick = {}, shape = RoundedCornerShape(10.dp), modifier = Modifier.height(32.dp)) {
                        Text(stringResource(R.string.partners_notify_on_launch), fontSize = 11.sp)
                    }
                }
            }
        }

        // Active Nearby Lost Pet Alerts
        Text(
            text = "Active Lost Pet Alerts in Your Area (Within 5 km)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = BluePrimaryDark
        )

        lostAlerts.forEach { alert ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${alert.petName} (${alert.breed})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD32F2F))
                        Text("${alert.distanceKm} km away", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                    Text("Last seen: ${alert.lastSeenLocation} • ${alert.reportedTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(alert.description, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reward: ${alert.rewardAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AccentGreen)
                        Button(
                            onClick = onCallHelpline,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(stringResource(R.string.partners_i_saw_this_pet), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 5. Pet Listings (Sale & Adoption) ----------------
@Composable
fun PetListingCard(
    listing: PetListing,
    onContact: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (listing.listingType == "Adoption") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = listing.listingType.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (listing.listingType == "Adoption") AccentGreen else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(listing.petName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
                }

                Text(listing.priceEstimate, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = BluePrimary)
            }

            Text("${listing.species} • ${listing.breed} • ${listing.age}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(listing.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("📍 Location: ${listing.location} (Posted by ${listing.postedBy})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onContact,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.partners_contact_guardian), fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------- 6. News & Events ----------------
@Composable
fun PetNewsAndEventsSection(
    news: PetNewsItem,
    events: List<PetEventItem>,
    onRegisterEvent: (PetEventItem) -> Unit,
    onRequestTrainer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pet News Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BluePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.partners_featured_pet_story),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(news.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
                Text("By ${news.source} • ${news.timeAgo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(news.fullContent, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Trainer on Demand Partner Request Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAFE)),
            border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.SportsScore, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(32.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.partners_trainer_on_demand_partnership), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                    Text("Request an accredited certified trainer for home obedience & behavior sessions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onRequestTrainer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(stringResource(R.string.partners_book_trainer), fontSize = 11.sp)
                }
            }
        }

        // Upcoming Pet Competitions & Events Calendar
        Text(
            text = stringResource(R.string.partners_upcoming_competitions_events_calendar),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = BluePrimaryDark
        )

        events.forEach { event ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BluePrimaryDark)
                    }
                    Text("📅 ${event.date} • 📍 ${event.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prizes: ${event.prizePool}", fontSize = 11.sp, color = AccentAmber, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { onRegisterEvent(event) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(stringResource(R.string.partners_add_to_calendar), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerJoinCalloutCard(
    title: String,
    description: String,
    onJoinClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BluePrimaryDark)
                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(
                onClick = onJoinClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(R.string.partners_apply), fontSize = 11.sp)
            }
        }
    }
}

// Dialog for Business Partner application
@Composable
fun BusinessPartnerJoinDialog(
    initialCategory: String,
    onDismiss: () -> Unit,
    onSubmit: (businessName: String, category: String, city: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initialCategory) }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categoryDescriptions = mapOf(
        "Food & Accessories" to "Register your pet food brand, treat shop, or accessories store on Petpulse.",
        "Pet Trainers" to "Join as a certified pet trainer. Offer obedience, agility, and behavior training.",
        "Veterinary Doctors & Clinics" to "Register your veterinary clinic or practice. Connect with pet owners.",
        "Boarding" to "List your boarding facility, pet daycare, or home sitting service.",
        "Sales" to "Register as a pet sales partner — breeders, pet shops, and adoption centers.",
        "Grooming Salon" to "Register your grooming salon on Petpulse."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join: $category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    categoryDescriptions[category] ?: "Register your business on Petpulse.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.partners_business_store_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.partners_email_address)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.partners_phone_number)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(R.string.partners_city_location)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Registration / License No. (for Verified badge)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.partners_brief_description_of_your_services)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && city.isNotBlank()) {
                        onSubmit(name, category, city, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.partners_submit_application))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.partners_cancel)) }
        }
    )
}



// ------------------------------------------------------------------
// Featured Plans Dialog — promotional placement packages (revenue)
// ------------------------------------------------------------------
@Composable
fun FeaturedPlansDialog(
    onDismiss: () -> Unit,
    onSubscribe: (planName: String) -> Unit
) {
    var selectedPlan by remember { mutableStateOf(1) }

    val plans = listOf(
        Triple("Basic", "₹999/month", listOf(
            "Top placement in 1 category",
            "Verified badge included",
            "7-day visibility analytics",
            "Standard listing support"
        )),
        Triple("Standard", "₹2,499/month", listOf(
            "Top placement in 3 categories",
            "Gold Featured ribbon",
            "30-day analytics dashboard",
            "Priority support",
            "2x profile views (avg)"
        )),
        Triple("Premium", "₹4,999/month", listOf(
            "#1 placement in all categories",
            "Homepage banner spotlight",
            "Unlimited analytics + export",
            "Dedicated account manager",
            "Promotional campaign monthly",
            "Verified + Featured badges"
        ))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.partners_get_featured)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Promote your business to the top of search results. Petpulse users see featured partners first.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                plans.forEachIndexed { index, (name, price, features) ->
                    val isSelected = selectedPlan == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlan = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFAF6E8) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFC9A227) else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFFC9A227),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (index == 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFC9A227)
                                        ) {
                                            Text(
                                                stringResource(R.string.partners_popular),
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    price,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF8A6D1C)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(feature, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubscribe(plans[selectedPlan].first) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A227))
            ) {
                Text("Request: ${plans[selectedPlan].first}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.partners_cancel)) }
        }
    )
}

