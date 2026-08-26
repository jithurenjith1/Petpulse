package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExploreSubTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePetsScreen(
    speciesList: List<SpeciesCategory>,
    selectedSpecies: String,
    onSelectSpecies: (String) -> Unit,
    currentSubTab: ExploreSubTab,
    onSelectSubTab: (ExploreSubTab) -> Unit,
    foodCategory: String,
    onSelectFoodCategory: (String) -> Unit,
    accessoryCategory: String,
    onSelectAccessoryCategory: (String) -> Unit,
    foodItems: List<FoodItem>,
    accessoryItems: List<AccessoryItem>,
    healthCareItems: List<HealthCareItem>,
    trainingGuides: List<TrainingGuide>,
    onItemAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSpeciesObj = speciesList.find { it.id == selectedSpecies } ?: speciesList.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("explore_pets_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Header & Species Horizontal Selector
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Pet Species & Care Encyclopedia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimaryDark
                )
                Text(
                    text = "Select your pet type to personalize food, accessories, health & training",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Horizontal Species Selector: Dogs, Cats, Birds, Fishes, Rabbit, Hamster, Exotic Species
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("species_selector_row"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(speciesList) { species ->
                    val isSelected = species.id == selectedSpecies
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) 6.dp else 2.dp,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectSpecies(species.id) }
                            .testTag("species_chip_${species.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = species.iconEmoji,
                                fontSize = 20.sp
                            )
                            Column {
                                Text(
                                    text = species.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSelected) "Active" else "Select",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Species Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentSpeciesObj.iconEmoji, fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentSpeciesObj.name} Care Portal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        Text(
                            text = currentSpeciesObj.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. 4 Main Sub-Sections: 1 Food, 2 Accessories, 3 Health Care, 4 Training
        item {
            ScrollableTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("explore_subtabs")
            ) {
                ExploreSubTab.values().forEach { subTab ->
                    val isSelected = currentSubTab == subTab
                    val title = when (subTab) {
                        ExploreSubTab.FOOD -> "🍲 1. Food"
                        ExploreSubTab.ACCESSORIES -> "🧸 2. Accessories"
                        ExploreSubTab.HEALTH_CARE -> "🩺 3. Health Care"
                        ExploreSubTab.TRAINING -> "🎓 4. Training"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectSubTab(subTab) },
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
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // 4. Submenu Content Display
        when (currentSubTab) {
            ExploreSubTab.FOOD -> {
                // Food Sub-filter: All, Dry Food, Wet Food, Treats
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Dry Food", "Wet Food", "Treats").forEach { cat ->
                            FilterChip(
                                selected = foodCategory == cat,
                                onClick = { onSelectFoodCategory(cat) },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("food_filter_$cat")
                            )
                        }
                    }
                }

                val filteredFood = if (foodCategory == "All") foodItems else foodItems.filter { it.subType == foodCategory }
                items(filteredFood) { item ->
                    FoodItemCard(item = item, onAddToList = { onItemAction("Added ${item.name} to cart/diet plan") })
                }
            }

            ExploreSubTab.ACCESSORIES -> {
                // Accessories Sub-filter: All, Clothing, Toys, Wearables, Other
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Clothing", "Toys", "Wearables", "Other").forEach { cat ->
                            FilterChip(
                                selected = accessoryCategory == cat,
                                onClick = { onSelectAccessoryCategory(cat) },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("accessory_filter_$cat")
                            )
                        }
                    }
                }

                val filteredAccessories = if (accessoryCategory == "All") accessoryItems else accessoryItems.filter { it.subType == accessoryCategory }
                items(filteredAccessories) { item ->
                    AccessoryItemCard(item = item, onBuy = { onItemAction("Selected ${item.name} ($${item.estimatedPrice})") })
                }
            }

            ExploreSubTab.HEALTH_CARE -> {
                items(healthCareItems) { item ->
                    HealthCareItemCard(item = item, onBook = { onItemAction("Booking appointment for ${item.title}") })
                }
            }

            ExploreSubTab.TRAINING -> {
                items(trainingGuides) { guide ->
                    TrainingGuideCard(guide = guide)
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(item: FoodItem, onAddToList: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = item.subType.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                }
                Text(
                    text = item.estimatedPrice,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = BluePrimary
                )
            }

            Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F4F8)
                ) {
                    Text(
                        text = "Portion: ${item.recommendedPortion}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Button(
                    onClick = onAddToList,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Diet", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AccessoryItemCard(item: AccessoryItem, onBuy: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE1F5FE)
                    ) {
                        Text(
                            text = item.subType.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                }
                Text(
                    text = item.estimatedPrice,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = BluePrimary
                )
            }

            Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Material: ${item.material}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                FilledTonalButton(
                    onClick = onBuy,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Details & Order", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun HealthCareItemCard(item: HealthCareItem, onBook: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = BluePrimary)
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BluePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.subType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Timeline: ${item.frequencyOrTimeline}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Cost: ${item.estimatedCost}", fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBook,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Book Care", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TrainingGuideCard(guide: TrainingGuide) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.School, contentDescription = null, tint = AccentAmber)
                    Text(guide.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimaryDark)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (guide.level == "Basic") Color(0xFFE8F5E9) else Color(0xFFEDE7F6)
                ) {
                    Text(
                        text = "${guide.level.uppercase()} LEVEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (guide.level == "Basic") AccentGreen else AccentPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text("Recommended Age: ${guide.recommendedAge}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Step-by-Step Routine:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    guide.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF8E1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                    Text("Expert Tip: ${guide.tips}", fontSize = 11.sp, color = Color(0xFF6D4C41))
                }
            }
        }
    }
}
