package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FabPosition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Petpulse Care — subscription plans screen
// Material3, pure Compose, no backend.
// ---------------------------------------------------------------------------

private val Purple = Color(0xFF6A4C93)
private val PurpleDark = Color(0xFF8367B0)
private val Gold = Color(0xFFC9A227)
private val GoldDark = Color(0xFFD9B43C)

private enum class BillingPeriod(val label: String) {
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

private data class PetpulsePlan(
    val name: String,
    val tagline: String,
    val monthlyPriceLabel: String,
    val yearlyPriceLabel: String,
    val features: List<String>,
    val isMostPopular: Boolean = false
)

private val Plans = listOf(
    PetpulsePlan(
        name = "Basic",
        tagline = "Free",
        monthlyPriceLabel = "₹0/mo",
        yearlyPriceLabel = "₹0/mo",
        features = listOf(
            "Unlimited AI symptom triage",
            "1 vet consultation/month",
            "Community access",
            "Vaccination reminders"
        )
    ),
    PetpulsePlan(
        name = "Care",
        tagline = "Most Popular",
        monthlyPriceLabel = "₹299/mo",
        yearlyPriceLabel = "₹2,999/yr",
        features = listOf(
            "Everything in Basic",
            "Unlimited AI triage with photo analysis",
            "2 vet consultations/month",
            "10% marketplace discount",
            "Free delivery on food & medicine",
            "Priority SOS"
        ),
        isMostPopular = true
    ),
    PetpulsePlan(
        name = "Premium",
        tagline = "Complete coverage",
        monthlyPriceLabel = "₹499/mo",
        yearlyPriceLabel = "₹4,999/yr",
        features = listOf(
            "Everything in Care",
            "4 vet consultations/month",
            "15% marketplace discount",
            "Insurance claims assistance",
            "AI breed & health analysis",
            "Priority vet response (<10 min)"
        )
    )
)

/** Light/dark aware palette for the screen, anchored on Purple and Gold. */
private class PetpulsePalette(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val purple: Color,
    val gold: Color,
    val onGold: Color,
    val divider: Color,
    val toggleTrack: Color
)

@Composable
private fun petpulsePalette(): PetpulsePalette =
    if (isSystemInDarkTheme()) {
        PetpulsePalette(
            background = Color(0xFF14121A),
            surface = Color(0xFF1E1B26),
            textPrimary = Color(0xFFF2EFF7),
            textSecondary = Color(0xFFA9A3B5),
            purple = PurpleDark,
            gold = GoldDark,
            onGold = Color(0xFF1E1B26),
            divider = Color(0xFF343040),
            toggleTrack = Color(0xFF282434)
        )
    } else {
        PetpulsePalette(
            background = Color(0xFFFAF8FC),
            surface = Color(0xFFFFFFFF),
            textPrimary = Color(0xFF1E1B26),
            textSecondary = Color(0xFF6B6577),
            purple = Purple,
            gold = Gold,
            onGold = Color(0xFF1E1B26),
            divider = Color(0xFFE6E1EE),
            toggleTrack = Color(0xFFECE8F2)
        )
    }

@Composable
fun PetpulseCareScreen(
    onClose: () -> Unit = {}
) {
    val palette = petpulsePalette()
    var billingPeriod by remember { mutableStateOf(BillingPeriod.MONTHLY) }
    var currentPlan by remember { mutableStateOf("Basic") }

    Scaffold(
        containerColor = palette.background,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClose,
                containerColor = palette.purple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Petpulse Care"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ----- Header -------------------------------------------------
            Text(
                text = stringResource(R.string.care_petpulse_care),
                color = palette.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.care_healthcare_subscription_for_your_pets),
                color = palette.textSecondary,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(20.dp))

            // ----- Billing toggle -----------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                BillingToggle(
                    selected = billingPeriod,
                    onSelect = { billingPeriod = it },
                    palette = palette
                )
                if (billingPeriod == BillingPeriod.YEARLY) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.care_save_2_months),
                        color = palette.gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ----- Plan cards ----------------------------------------------
            Plans.forEachIndexed { index, plan ->
                if (index > 0) Spacer(Modifier.height(20.dp))
                PlanCard(
                    plan = plan,
                    billingPeriod = billingPeriod,
                    isCurrentPlan = plan.name == currentPlan,
                    onSubscribe = { currentPlan = plan.name },
                    palette = palette
                )
            }

            Spacer(Modifier.height(32.dp))

            // ----- What's included -----------------------------------------
            WhatsIncludedSection(palette = palette)

            // Bottom room so the FAB never covers the last row.
            Spacer(Modifier.height(96.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Billing toggle
// ---------------------------------------------------------------------------

@Composable
private fun BillingToggle(
    selected: BillingPeriod,
    onSelect: (BillingPeriod) -> Unit,
    palette: PetpulsePalette
) {
    Row(
        modifier = Modifier
            .background(palette.toggleTrack, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        ToggleOption(
            label = BillingPeriod.MONTHLY.label,
            isSelected = selected == BillingPeriod.MONTHLY,
            selectedColor = palette.purple,
            onClick = { onSelect(BillingPeriod.MONTHLY) },
            palette = palette
        )
        ToggleOption(
            label = BillingPeriod.YEARLY.label,
            isSelected = selected == BillingPeriod.YEARLY,
            selectedColor = palette.purple,
            onClick = { onSelect(BillingPeriod.YEARLY) },
            palette = palette
        )
    }
}

@Composable
private fun ToggleOption(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    palette: PetpulsePalette
) {
    val backgroundColor = if (isSelected) selectedColor else Color.Transparent
    val contentColor = if (isSelected) Color.White else palette.textSecondary

    Text(
        text = label,
        color = contentColor,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .widthIn(min = 84.dp)
    )
}

// ---------------------------------------------------------------------------
// Plan card
// ---------------------------------------------------------------------------

@Composable
private fun PlanCard(
    plan: PetpulsePlan,
    billingPeriod: BillingPeriod,
    isCurrentPlan: Boolean,
    onSubscribe: () -> Unit,
    palette: PetpulsePalette
) {
    val accent = if (plan.isMostPopular) palette.gold else palette.purple

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (plan.isMostPopular) 1.5.dp else 0.5.dp,
                    color = if (plan.isMostPopular) palette.gold else palette.divider,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = palette.surface,
                contentColor = palette.textPrimary
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (plan.isMostPopular) 6.dp else 2.dp
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Spacer(Modifier.height(6.dp))

                // ----- Plan name & price ---------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plan.name,
                            color = palette.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plan.tagline,
                            color = if (plan.isMostPopular) palette.gold else palette.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (billingPeriod == BillingPeriod.YEARLY) {
                            plan.yearlyPriceLabel
                        } else {
                            plan.monthlyPriceLabel
                        },
                        color = accent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.divider)
                )
                Spacer(Modifier.height(14.dp))

                // ----- Feature list ---------------------------------------
                plan.features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = feature,
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ----- Action button -------------------------------------
                Button(
                    onClick = onSubscribe,
                    enabled = !isCurrentPlan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = if (plan.isMostPopular) palette.onGold else Color.White,
                        disabledContainerColor = if (plan.isMostPopular) {
                            palette.gold.copy(alpha = 0.35f)
                        } else {
                            palette.purple.copy(alpha = 0.35f)
                        },
                        disabledContentColor = if (plan.isMostPopular) {
                            palette.onGold.copy(alpha = 0.7f)
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        }
                    )
                ) {
                    Text(
                        text = if (isCurrentPlan) "Current Plan" else "Subscribe",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ----- "Most Popular" badge floating over the Care card -----------
        if (plan.isMostPopular) {
            Text(
                text = stringResource(R.string.care_most_popular),
                color = palette.onGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .background(palette.gold, RoundedCornerShape(50))
                    .border(1.dp, palette.gold.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// "What's included" expandable section
// ---------------------------------------------------------------------------

private data class IncludedDetail(
    val title: String,
    val description: String
)

private val IncludedDetails = listOf(
    IncludedDetail(
        title = "AI Symptom Triage",
        description = "Describe your pet's symptoms and get instant guidance on urgency and next steps. Care and Premium plans add photo analysis for rashes, wounds and eye or ear issues."
    ),
    IncludedDetail(
        title = "Vet Consultations",
        description = "Chat or video consultations with licensed veterinarians. Monthly limits: 1 on Basic, 2 on Care, 4 on Premium."
    ),
    IncludedDetail(
        title = "Priority SOS",
        description = "Immediate access to emergency vet support when your pet needs urgent help. Included with Care and above."
    ),
    IncludedDetail(
        title = "Marketplace Discounts",
        description = "Automatic discounts on pet food, medicines and accessories across the Petpulse marketplace: 10% with Care, 15% with Premium."
    ),
    IncludedDetail(
        title = "Delivery & Insurance",
        description = "Care includes free delivery on food and medicine orders. Premium adds end-to-end assistance with insurance claims."
    ),
    IncludedDetail(
        title = "AI Breed & Health Analysis",
        description = "Breed-aware health insights, diet suggestions and early-warning checks tailored to your pet's age and history. Premium only."
    ),
    IncludedDetail(
        title = "Vaccination Reminders",
        description = "Never miss a shot — reminders scheduled to your pet's vaccination calendar on every plan."
    )
)

@Composable
private fun WhatsIncludedSection(palette: PetpulsePalette) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = palette.surface,
            contentColor = palette.textPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.care_what_s_included),
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = palette.purple
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(palette.divider)
                    )
                    Spacer(Modifier.height(8.dp))

                    IncludedDetails.forEachIndexed { index, detail ->
                        if (index > 0) Spacer(Modifier.height(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(palette.purple, CircleShape)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = detail.title,
                                    color = palette.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = detail.description,
                                color = palette.textSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(start = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


