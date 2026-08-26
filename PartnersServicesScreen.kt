package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
                                text = "Verified Business & Care Network",
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
                        onClick = {
                            partnerCategoryToJoin = "General Business Entity"
                            showBusinessPartnerDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Join as Business Partner", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            text = "Nearby Partner Grooming Salons",
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

                items(groomingCenters) { center ->
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
                            text = "Pet Food Combos & Accessories Subscriptions",
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
                            text = "Pet Boarding & Sitter Network",
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
                                text = "Adoption & Pet Listings",
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
                            Text("Post Pet", fontSize = 12.sp)
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
                            text = "Featured Partner",
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
                    Text(center.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BluePrimaryDark)
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
                    Text("Popular Packages:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
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
                    Text("Starting from", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Call", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Book Spa Slot", fontSize = 11.sp)
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
                    Text("Select Subscription", fontSize = 11.sp)
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
                    Text("Book Care", fontSize = 11.sp)
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
                        Text("Find My Pet — 5km SOS Network", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB71C1C))
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
                        Text("Broadcast 5km SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCallHelpline,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Helpline", fontSize = 12.sp)
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
                                text = "HARDWARE SPONSOR COMING SOON",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Smart Collar GPS Tracker (Prototype)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BluePrimaryDark)
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
                    Text("Price: TBA (Sponsored Partner)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    FilledTonalButton(onClick = {}, shape = RoundedCornerShape(10.dp), modifier = Modifier.height(32.dp)) {
                        Text("Notify on Launch", fontSize = 11.sp)
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
                            Text("I Saw This Pet", fontSize = 11.sp)
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
                    Text("Contact Guardian", fontSize = 11.sp)
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
                        text = "📰 FEATURED PET STORY",
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
                    Text("Trainer On-Demand (Partnership)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                    Text("Request an accredited certified trainer for home obedience & behavior sessions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onRequestTrainer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Book Trainer", fontSize = 11.sp)
                }
            }
        }

        // Upcoming Pet Competitions & Events Calendar
        Text(
            text = "🏆 Upcoming Competitions & Events Calendar",
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
                            Text("Add to Calendar", fontSize = 10.sp)
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
                Text("Apply", fontSize = 11.sp)
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
    var city by remember { mutableStateOf("Metro District") }
    var phone by remember { mutableStateOf("+1 (555) 000-1122") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join as Business Partner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Register your grooming salon, boarding facility, food brand, or training center on Jane & Pals.", fontSize = 12.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Business / Store Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Business Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Service City / Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Business Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSubmit(name, category, city, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
