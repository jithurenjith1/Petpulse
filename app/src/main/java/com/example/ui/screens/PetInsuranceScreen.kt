package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- App palette ----
private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

// ---- Model ----
data class InsurancePlan(
    val id: Int,
    val name: String,
    val pricePerMonth: Int,
    val coverageAmount: Int,
    val features: List<String>
)

private val insurancePlans = listOf(
    InsurancePlan(
        id = 1,
        name = "Basic Coverage",
        pricePerMonth = 299,
        coverageAmount = 50_000,
        features = listOf(
            "Accident coverage up to ₹50,000",
            "Outpatient consultations",
            "Diagnostics & lab tests",
            "Cashless at 500+ clinics"
        )
    ),
    InsurancePlan(
        id = 2,
        name = "Standard",
        pricePerMonth = 599,
        coverageAmount = 150_000,
        features = listOf(
            "Accident + illness coverage up to ₹1,50,000",
            "Surgeries & hospitalization",
            "Specialist consultations",
            "Cashless at 2,000+ clinics",
            "Annual wellness checkup"
        )
    ),
    InsurancePlan(
        id = 3,
        name = "Premium",
        pricePerMonth = 999,
        coverageAmount = 500_000,
        features = listOf(
            "Comprehensive coverage up to ₹5,00,000",
            "Surgeries, ICU & chronic care",
            "Dental & grooming included",
            "Cashless at 4,000+ clinics",
            "Quarterly wellness checkups",
            "24x7 tele-vet support"
        )
    )
)

private fun formatRupee(amount: Int): String = "₹" + "%,d".format(amount)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetInsuranceScreen() {
    var selectedPlanId by remember { mutableStateOf(2) } // Standard selected by default

    Scaffold(
        containerColor = CreamBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pet Insurance",
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(insurancePlans, key = { it.id }) { plan ->
                InsurancePlanCard(
                    plan = plan,
                    isSelected = plan.id == selectedPlanId,
                    onGetInsured = { selectedPlanId = plan.id }
                )
            }
        }
    }
}

@Composable
private fun InsurancePlanCard(
    plan: InsurancePlan,
    isSelected: Boolean,
    onGetInsured: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) TealAccent else Color.Transparent,
        label = "insuranceBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CoralLight.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        color = DarkText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Coverage: ${formatRupee(plan.coverageAmount)}",
                        color = TealAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatRupee(plan.pricePerMonth),
                    color = CoralPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "/month",
                    color = DarkText.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Features
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TealAccent)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = feature,
                            color = DarkText.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onGetInsured,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) TealAccent else CoralPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isSelected) "Insured" else "Get Insured",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

