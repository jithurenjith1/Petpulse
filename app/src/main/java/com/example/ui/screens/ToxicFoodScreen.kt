package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Toxic Food Check — an offline reference of foods and substances that are
// dangerous to pets, with a severity and first-aid guidance. No network, no
// Firestore: the list lives in this file. English strings live in
// values/strings.xml, Malayalam in values-ml/strings.xml.
// ---------------------------------------------------------------------------

/** Wagmiya pet helpline — the same business number used elsewhere in the app. */
private const val ToxicHelpWhatsAppDigits = "919526632311"

private val EmergencyRed = Color(0xFFD62828)
private val ToxicOrange = Color(0xFFE65100)
private val CautionAmber = Color(0xFFF9A825)

/** Danger level of a substance; carries its label resource and badge colour. */
private enum class ToxicSeverity(val labelRes: Int, val color: Color) {
    EMERGENCY(R.string.toxic_severity_emergency, EmergencyRed),
    TOXIC(R.string.toxic_severity_toxic, ToxicOrange),
    CAUTION(R.string.toxic_severity_caution, CautionAmber)
}

/** One dangerous food / substance: all text is a string resource (en + ml). */
private data class ToxicEntry(
    val nameRes: Int,
    val happensRes: Int,
    val actionRes: Int,
    val severity: ToxicSeverity
)

private val ToxicEntries: List<ToxicEntry> = listOf(
    ToxicEntry(R.string.toxic_item_chocolate_name, R.string.toxic_item_chocolate_happens, R.string.toxic_item_chocolate_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_onion_name, R.string.toxic_item_onion_happens, R.string.toxic_item_onion_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_garlic_name, R.string.toxic_item_garlic_happens, R.string.toxic_item_garlic_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_grapes_name, R.string.toxic_item_grapes_happens, R.string.toxic_item_grapes_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_xylitol_name, R.string.toxic_item_xylitol_happens, R.string.toxic_item_xylitol_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_avocado_name, R.string.toxic_item_avocado_happens, R.string.toxic_item_avocado_action, ToxicSeverity.CAUTION),
    ToxicEntry(R.string.toxic_item_alcohol_name, R.string.toxic_item_alcohol_happens, R.string.toxic_item_alcohol_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_caffeine_name, R.string.toxic_item_caffeine_happens, R.string.toxic_item_caffeine_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_macadamia_name, R.string.toxic_item_macadamia_happens, R.string.toxic_item_macadamia_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_milk_name, R.string.toxic_item_milk_happens, R.string.toxic_item_milk_action, ToxicSeverity.CAUTION),
    ToxicEntry(R.string.toxic_item_raw_bones_name, R.string.toxic_item_raw_bones_happens, R.string.toxic_item_raw_bones_action, ToxicSeverity.CAUTION),
    ToxicEntry(R.string.toxic_item_salt_name, R.string.toxic_item_salt_happens, R.string.toxic_item_salt_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_citrus_name, R.string.toxic_item_citrus_happens, R.string.toxic_item_citrus_action, ToxicSeverity.CAUTION),
    ToxicEntry(R.string.toxic_item_coconut_name, R.string.toxic_item_coconut_happens, R.string.toxic_item_coconut_action, ToxicSeverity.CAUTION),
    ToxicEntry(R.string.toxic_item_raw_yeast_dough_name, R.string.toxic_item_raw_yeast_dough_happens, R.string.toxic_item_raw_yeast_dough_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_artificial_sweetener_name, R.string.toxic_item_artificial_sweetener_happens, R.string.toxic_item_artificial_sweetener_action, ToxicSeverity.EMERGENCY),
    ToxicEntry(R.string.toxic_item_nutmeg_name, R.string.toxic_item_nutmeg_happens, R.string.toxic_item_nutmeg_action, ToxicSeverity.TOXIC),
    ToxicEntry(R.string.toxic_item_cooked_bones_name, R.string.toxic_item_cooked_bones_happens, R.string.toxic_item_cooked_bones_action, ToxicSeverity.CAUTION)
)

@Composable
fun ToxicFoodScreen(onClose: () -> Unit = {}) {
    val ctx = LocalContext.current
    var query by remember { mutableStateOf("") }

    // Filtering happens in the composable body (NOT inside the LazyColumn lambda).
    val needle = query.trim().lowercase()
    val visibleEntries: List<ToxicEntry> = if (needle.isEmpty()) {
        ToxicEntries
    } else {
        ToxicEntries.filter { entry ->
            ctx.getString(entry.nameRes).lowercase().contains(needle)
        }
    }

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
                    text = stringResource(R.string.toxic_food_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.toxic_food_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prominent emergency contact block.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmergencyRed)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(
                                text = stringResource(R.string.toxic_food_emergency_note),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    ctx.startActivity(
                                        Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:+" + ToxicHelpWhatsAppDigits)
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmergencyRed)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.toxic_food_emergency_call), fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = {
                                runCatching {
                                    val msg = ctx.getString(R.string.toxic_food_title)
                                    val encoded = java.net.URLEncoder.encode(msg, "UTF-8")
                                    ctx.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://wa.me/" + ToxicHelpWhatsAppDigits + "?text=" + encoded)
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.toxic_food_whatsapp), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Search box.
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text(stringResource(R.string.toxic_food_search_hint)) }
                )
            }

            if (visibleEntries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.toxic_food_no_results),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            items(visibleEntries) { entry ->
                ToxicFoodRow(entry)
            }
        }
    }
}

@Composable
private fun ToxicFoodRow(entry: ToxicEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(entry.nameRes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = entry.severity.color) {
                    Text(
                        text = stringResource(entry.severity.labelRes),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.toxic_food_what_happens) + ": " + stringResource(entry.happensRes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.toxic_food_what_to_do) + ": " + stringResource(entry.actionRes),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = entry.severity.color
            )
        }
    }
}
