package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Service Price Card — indicative Kerala price ranges for common pet services.
// The list is in-file (no Firestore). English strings live in values/strings.xml,
// Malayalam in values-ml/strings.xml.
// ---------------------------------------------------------------------------

/** One service line: name, a short grey note, and an indicative price range. */
private data class PriceItem(
    val nameRes: Int,
    val noteRes: Int,
    val rangeRes: Int
)

private val PriceItems: List<PriceItem> = listOf(
    PriceItem(R.string.price_item_vet_consult_name, R.string.price_item_vet_consult_note, R.string.price_item_vet_consult_range),
    PriceItem(R.string.price_item_vaccination_name, R.string.price_item_vaccination_note, R.string.price_item_vaccination_range),
    PriceItem(R.string.price_item_deworming_name, R.string.price_item_deworming_note, R.string.price_item_deworming_range),
    PriceItem(R.string.price_item_grooming_name, R.string.price_item_grooming_note, R.string.price_item_grooming_range),
    PriceItem(R.string.price_item_boarding_name, R.string.price_item_boarding_note, R.string.price_item_boarding_range),
    PriceItem(R.string.price_item_minor_surgery_name, R.string.price_item_minor_surgery_note, R.string.price_item_minor_surgery_range),
    PriceItem(R.string.price_item_spay_neuter_name, R.string.price_item_spay_neuter_note, R.string.price_item_spay_neuter_range),
    PriceItem(R.string.price_item_dental_name, R.string.price_item_dental_note, R.string.price_item_dental_range),
    PriceItem(R.string.price_item_blood_test_name, R.string.price_item_blood_test_note, R.string.price_item_blood_test_range),
    PriceItem(R.string.price_item_microchip_name, R.string.price_item_microchip_note, R.string.price_item_microchip_range),
    PriceItem(R.string.price_item_tick_flea_name, R.string.price_item_tick_flea_note, R.string.price_item_tick_flea_range),
    PriceItem(R.string.price_item_emergency_name, R.string.price_item_emergency_note, R.string.price_item_emergency_range),
    PriceItem(R.string.price_item_ambulance_name, R.string.price_item_ambulance_note, R.string.price_item_ambulance_range),
    PriceItem(R.string.price_item_home_visit_name, R.string.price_item_home_visit_note, R.string.price_item_home_visit_range),
    PriceItem(R.string.price_item_training_name, R.string.price_item_training_note, R.string.price_item_training_range)
)

@Composable
fun ServicePriceCardScreen(
    onClose: () -> Unit = {},
    onOpenServices: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClose,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.records_close))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.price_card_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.price_card_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(PriceItems) { item ->
                PriceRow(item)
            }

            // Disclaimer.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.price_card_disclaimer),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onOpenServices,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.price_card_open_services), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(item: PriceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(item.nameRes),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(item.noteRes),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(item.rangeRes),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
