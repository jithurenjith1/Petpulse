package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Petpulse brand colors
private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

enum class TipCategory(val label: String, val icon: ImageVector) {
    Nutrition("Nutrition", Icons.Default.HealthAndSafety),
    Exercise("Exercise", Icons.Default.FitnessCenter),
    Grooming("Grooming", Icons.Default.ContentCut),
    Health("Health", Icons.Default.MedicalServices),
    Training("Training", Icons.Default.School),
    Behavior("Behavior", Icons.Default.Psychology)
}

data class CareTip(
    val title: String,
    val description: String,
    val category: TipCategory
)

private val careTips: List<CareTip> = listOf(
    // Nutrition
    CareTip(
        title = "Measure meals, don't free-feed",
        description = "Portion control prevents obesity. Use a measuring cup and follow your vet's daily calorie guidance based on your pet's weight and activity level.",
        category = TipCategory.Nutrition
    ),
    CareTip(
        title = "Keep fresh water available",
        description = "Change water at least twice a day. Cats especially prefer running water — a fountain can boost their daily intake.",
        category = TipCategory.Nutrition
    ),
    CareTip(
        title = "Avoid toxic human foods",
        description = "Chocolate, grapes, onions, garlic, and xylitol (in sugar-free gum) are dangerous for dogs and cats. Keep them out of reach.",
        category = TipCategory.Nutrition
    ),
    CareTip(
        title = "Introduce new food gradually",
        description = "Transition over 7–10 days by mixing increasing amounts of new food with old food to avoid stomach upset.",
        category = TipCategory.Nutrition
    ),

    // Exercise
    CareTip(
        title = "Daily walks for dogs",
        description = "Most dogs need 30–60 minutes of exercise daily. Break it into two walks and vary the route for mental stimulation.",
        category = TipCategory.Exercise
    ),
    CareTip(
        title = "Interactive play for cats",
        description = "Use wand toys or laser pointers for 15–20 minutes a day. Always end laser play by catching a physical toy to avoid frustration.",
        category = TipCategory.Exercise
    ),
    CareTip(
        title = "Puzzle feeders for enrichment",
        description = "Slow-feed bowls and treat puzzles engage your pet's brain and slow down fast eaters. Great for rainy days indoors.",
        category = TipCategory.Exercise
    ),

    // Grooming
    CareTip(
        title = "Brush according to coat type",
        description = "Short coats need weekly brushing; long or double coats need daily brushing to prevent matting and reduce shedding.",
        category = TipCategory.Grooming
    ),
    CareTip(
        title = "Trim nails every 2–4 weeks",
        description = "Overgrown nails cause pain and alter gait. If you hear clicking on hard floors, it's time for a trim.",
        category = TipCategory.Grooming
    ),
    CareTip(
        title = "Bathe only when needed",
        description = "Dogs typically need a bath every 1–3 months; cats rarely need one. Use pet-safe shampoo — human products irritate their skin.",
        category = TipCategory.Grooming
    ),
    CareTip(
        title = "Clean ears weekly",
        description = "Check ears for odour or redness. Wipe with a vet-approved ear cleaner — never insert anything deep into the canal.",
        category = TipCategory.Grooming
    ),

    // Health
    CareTip(
        title = "Schedule annual vet check-ups",
        description = "Yearly exams catch problems early. Senior pets (7+ years) benefit from twice-yearly visits with blood work.",
        category = TipCategory.Health
    ),
    CareTip(
        title = "Stay current on vaccinations",
        description = "Core vaccines protect against deadly diseases. Follow your vet's schedule and keep a record of booster dates.",
        category = TipCategory.Health
    ),
    CareTip(
        title = "Use year-round parasite prevention",
        description = "Flea, tick, and heartworm prevention works best when given consistently, not just in warm months.",
        category = TipCategory.Health
    ),

    // Training
    CareTip(
        title = "Use positive reinforcement",
        description = "Reward desired behaviour with treats, praise, or play. Avoid punishment — it damages trust and increases anxiety.",
        category = TipCategory.Training
    ),
    CareTip(
        title = "Keep sessions short",
        description = "Train in 5–10 minute bursts, 2–3 times a day. Pets lose focus quickly, so end on a success.",
        category = TipCategory.Training
    ),
    CareTip(
        title = "Teach a reliable recall",
        description = "Practice 'come' in low-distraction environments first, rewarding generously. Gradually add distance and distractions.",
        category = TipCategory.Training
    ),

    // Behavior
    CareTip(
        title = "Read body language",
        description = "A wagging tail doesn't always mean happy. Learn your pet's stress signals — lip licking, yawning, flattened ears — to respond early.",
        category = TipCategory.Behavior
    ),
    CareTip(
        title = "Provide a safe retreat",
        description = "Every pet needs a quiet space to decompress. Crate-trained dogs and cats with high hiding spots feel more secure.",
        category = TipCategory.Behavior
    ),
    CareTip(
        title = "Address changes promptly",
        description = "Sudden aggression, hiding, or appetite changes often signal pain or stress. Consult a vet or behaviourist early rather than waiting.",
        category = TipCategory.Behavior
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCareTipsScreen() {
    var selectedCategory by remember { mutableStateOf<TipCategory?>(null) }

    val filteredTips = remember(selectedCategory) {
        if (selectedCategory == null) careTips
        else careTips.filter { it.category == selectedCategory }
    }

    MaterialTheme {
        Scaffold(
            containerColor = CreamBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Pet Care Tips",
                            color = DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CreamBg
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CategoryFilterRow(
                    selected = selectedCategory,
                    onSelect = { category ->
                        selectedCategory =
                            if (selectedCategory == category) null else category
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(filteredTips) { tip ->
                        CareTipCard(tip = tip)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    selected: TipCategory?,
    onSelect: (TipCategory) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TipCategory.entries.toList()) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category.label) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CoralPrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun CareTipCard(tip: CareTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TealAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tip.category.icon,
                    contentDescription = tip.category.label,
                    tint = TealAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.category.label,
                    color = CoralPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tip.title,
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.description,
                    color = DarkText.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

