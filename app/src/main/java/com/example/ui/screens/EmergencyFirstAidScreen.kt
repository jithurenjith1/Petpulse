package com.petpulse.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.R
import com.petpulse.app.data.model.VerifiedDoctor

// ---------------------------------------------------------------------------
// Emergency & First Aid - our own rework of the old "Toxic Food Check" screen.
//
// It is a first-aid companion, not a diagnosis tool. Every path on this screen
// carries the same disclaimer and pushes the user towards a vet. It never tells
// anyone to give a medicine or a dose.
//
// All user-facing text lives in values/strings.xml (English) and
// values-ml/strings.xml (Malayalam). The emergency list itself is offline data
// in this file; the vet list comes from the app's OWN partner-vet data that the
// owner manages in the admin console - never from any third-party directory.
// ---------------------------------------------------------------------------

/** One emergency: a chip label plus the first-aid steps to show when tapped. */
private data class EmergencyType(
    val id: String,
    val labelRes: Int,
    val stepRes: List<Int>,
    // Critical = the "call a vet now" action becomes the most prominent thing.
    val isCritical: Boolean = false,
    // Poisoning-style cases also surface the existing Toxic Food list.
    val opensToxicFood: Boolean = false
)

private val EmergencyTypes: List<EmergencyType> = listOf(
    EmergencyType(
        id = "bleeding",
        labelRes = R.string.emerg_type_bleeding,
        stepRes = listOf(
            R.string.emerg_steps_bleeding_1,
            R.string.emerg_steps_bleeding_2,
            R.string.emerg_steps_bleeding_3,
            R.string.emerg_steps_bleeding_4,
            R.string.emerg_steps_bleeding_5,
            R.string.emerg_steps_bleeding_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "not_breathing",
        labelRes = R.string.emerg_type_not_breathing,
        stepRes = listOf(
            R.string.emerg_steps_not_breathing_1,
            R.string.emerg_steps_not_breathing_2,
            R.string.emerg_steps_not_breathing_3,
            R.string.emerg_steps_not_breathing_4,
            R.string.emerg_steps_not_breathing_5,
            R.string.emerg_steps_not_breathing_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "seizure",
        labelRes = R.string.emerg_type_seizure,
        stepRes = listOf(
            R.string.emerg_steps_seizure_1,
            R.string.emerg_steps_seizure_2,
            R.string.emerg_steps_seizure_3,
            R.string.emerg_steps_seizure_4,
            R.string.emerg_steps_seizure_5,
            R.string.emerg_steps_seizure_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "vomiting",
        labelRes = R.string.emerg_type_vomiting,
        stepRes = listOf(
            R.string.emerg_steps_vomiting_1,
            R.string.emerg_steps_vomiting_2,
            R.string.emerg_steps_vomiting_3,
            R.string.emerg_steps_vomiting_4,
            R.string.emerg_steps_vomiting_5,
            R.string.emerg_steps_vomiting_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "not_eating",
        labelRes = R.string.emerg_type_not_eating,
        stepRes = listOf(
            R.string.emerg_steps_not_eating_1,
            R.string.emerg_steps_not_eating_2,
            R.string.emerg_steps_not_eating_3,
            R.string.emerg_steps_not_eating_4,
            R.string.emerg_steps_not_eating_5,
            R.string.emerg_steps_not_eating_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "limping",
        labelRes = R.string.emerg_type_limping,
        stepRes = listOf(
            R.string.emerg_steps_limping_1,
            R.string.emerg_steps_limping_2,
            R.string.emerg_steps_limping_3,
            R.string.emerg_steps_limping_4,
            R.string.emerg_steps_limping_5,
            R.string.emerg_steps_limping_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "collapse",
        labelRes = R.string.emerg_type_collapse,
        stepRes = listOf(
            R.string.emerg_steps_collapse_1,
            R.string.emerg_steps_collapse_2,
            R.string.emerg_steps_collapse_3,
            R.string.emerg_steps_collapse_4,
            R.string.emerg_steps_collapse_5,
            R.string.emerg_steps_collapse_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "poisoning",
        labelRes = R.string.emerg_type_poisoning,
        stepRes = listOf(
            R.string.emerg_steps_poisoning_1,
            R.string.emerg_steps_poisoning_2,
            R.string.emerg_steps_poisoning_3,
            R.string.emerg_steps_poisoning_4,
            R.string.emerg_steps_poisoning_5,
            R.string.emerg_steps_poisoning_6,
        ),
        isCritical = true,
        opensToxicFood = true
    ),
    EmergencyType(
        id = "dog_bite",
        labelRes = R.string.emerg_type_dog_bite,
        stepRes = listOf(
            R.string.emerg_steps_dog_bite_1,
            R.string.emerg_steps_dog_bite_2,
            R.string.emerg_steps_dog_bite_3,
            R.string.emerg_steps_dog_bite_4,
            R.string.emerg_steps_dog_bite_5,
            R.string.emerg_steps_dog_bite_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "plant_toxin",
        labelRes = R.string.emerg_type_plant_toxin,
        stepRes = listOf(
            R.string.emerg_steps_plant_toxin_1,
            R.string.emerg_steps_plant_toxin_2,
            R.string.emerg_steps_plant_toxin_3,
            R.string.emerg_steps_plant_toxin_4,
            R.string.emerg_steps_plant_toxin_5,
            R.string.emerg_steps_plant_toxin_6,
        ),
        isCritical = false,
        opensToxicFood = true
    ),
    EmergencyType(
        id = "blood_in_stool",
        labelRes = R.string.emerg_type_blood_in_stool,
        stepRes = listOf(
            R.string.emerg_steps_blood_in_stool_1,
            R.string.emerg_steps_blood_in_stool_2,
            R.string.emerg_steps_blood_in_stool_3,
            R.string.emerg_steps_blood_in_stool_4,
            R.string.emerg_steps_blood_in_stool_5,
            R.string.emerg_steps_blood_in_stool_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "heatstroke",
        labelRes = R.string.emerg_type_heatstroke,
        stepRes = listOf(
            R.string.emerg_steps_heatstroke_1,
            R.string.emerg_steps_heatstroke_2,
            R.string.emerg_steps_heatstroke_3,
            R.string.emerg_steps_heatstroke_4,
            R.string.emerg_steps_heatstroke_5,
            R.string.emerg_steps_heatstroke_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "choking",
        labelRes = R.string.emerg_type_choking,
        stepRes = listOf(
            R.string.emerg_steps_choking_1,
            R.string.emerg_steps_choking_2,
            R.string.emerg_steps_choking_3,
            R.string.emerg_steps_choking_4,
            R.string.emerg_steps_choking_5,
            R.string.emerg_steps_choking_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "eye_injury",
        labelRes = R.string.emerg_type_eye_injury,
        stepRes = listOf(
            R.string.emerg_steps_eye_injury_1,
            R.string.emerg_steps_eye_injury_2,
            R.string.emerg_steps_eye_injury_3,
            R.string.emerg_steps_eye_injury_4,
            R.string.emerg_steps_eye_injury_5,
            R.string.emerg_steps_eye_injury_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "allergic_reaction",
        labelRes = R.string.emerg_type_allergic_reaction,
        stepRes = listOf(
            R.string.emerg_steps_allergic_reaction_1,
            R.string.emerg_steps_allergic_reaction_2,
            R.string.emerg_steps_allergic_reaction_3,
            R.string.emerg_steps_allergic_reaction_4,
            R.string.emerg_steps_allergic_reaction_5,
            R.string.emerg_steps_allergic_reaction_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "tick_emergency",
        labelRes = R.string.emerg_type_tick_emergency,
        stepRes = listOf(
            R.string.emerg_steps_tick_emergency_1,
            R.string.emerg_steps_tick_emergency_2,
            R.string.emerg_steps_tick_emergency_3,
            R.string.emerg_steps_tick_emergency_4,
            R.string.emerg_steps_tick_emergency_5,
            R.string.emerg_steps_tick_emergency_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "fracture",
        labelRes = R.string.emerg_type_fracture,
        stepRes = listOf(
            R.string.emerg_steps_fracture_1,
            R.string.emerg_steps_fracture_2,
            R.string.emerg_steps_fracture_3,
            R.string.emerg_steps_fracture_4,
            R.string.emerg_steps_fracture_5,
            R.string.emerg_steps_fracture_6,
        ),
        isCritical = false,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "drowning",
        labelRes = R.string.emerg_type_drowning,
        stepRes = listOf(
            R.string.emerg_steps_drowning_1,
            R.string.emerg_steps_drowning_2,
            R.string.emerg_steps_drowning_3,
            R.string.emerg_steps_drowning_4,
            R.string.emerg_steps_drowning_5,
            R.string.emerg_steps_drowning_6,
        ),
        isCritical = true,
        opensToxicFood = false
    ),
    EmergencyType(
        id = "other",
        labelRes = R.string.emerg_type_other,
        stepRes = listOf(
            R.string.emerg_steps_other_1,
            R.string.emerg_steps_other_2,
            R.string.emerg_steps_other_3,
            R.string.emerg_steps_other_4,
            R.string.emerg_steps_other_5,
            R.string.emerg_steps_other_6,
        ),
        isCritical = false,
        opensToxicFood = false
    )
)

@Composable
fun EmergencyFirstAidScreen(
    onClose: () -> Unit = {},
    vets: List<VerifiedDoctor> = emptyList(),
    savedVet: VerifiedDoctor? = null,
    onOpenToxicFood: () -> Unit = {}
) {
    val ctx = LocalContext.current
    // State is hoisted ABOVE the LazyColumn: nothing here uses remember() inside
    // a LazyColumn / LazyRow content lambda.
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selectedType: EmergencyType? = EmergencyTypes.firstOrNull { it.id == selectedId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClose,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.emerg_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.emerg_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Disclaimer, top of screen.
            item { DisclaimerBanner() }

            item {
                Text(
                    text = stringResource(R.string.emerg_choose_label),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Row of tappable emergency chips (horizontal scroll).
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EmergencyTypes) { type ->
                        FilterChip(
                            selected = type.id == selectedId,
                            onClick = { selectedId = type.id },
                            label = { Text(stringResource(type.labelRes), fontSize = 13.sp) }
                        )
                    }
                }
            }

            // The selected emergency's first-aid steps.
            item {
                val current = selectedType
                if (current == null) {
                    Text(
                        text = stringResource(R.string.emerg_steps_empty),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    EmergencyDetailCard(
                        type = current,
                        onCallVetNow = { dialNumber(ctx, "+" + HelplineWhatsAppDigits) },
                        onOpenToxicFood = onOpenToxicFood
                    )
                }
            }

            // Prominent "call a vet" action, always available.
            item { CallVetCard(ctx = ctx, savedVet = savedVet) }

            // Our own partner vets (from the app's admin console data).
            item {
                Text(
                    text = stringResource(R.string.emerg_nearest_vets),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.emerg_nearest_vets_note),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = stringResource(R.string.emerg_no_vets),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(vets) { vet ->
                    VetCallRow(vet = vet, onCall = { dialNumber(ctx, vet.phone) })
                }
            }

            // The existing Toxic Food list stays reachable from here.
            item {
                OutlinedButton(
                    onClick = onOpenToxicFood,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.emerg_toxic_food_button), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.emerg_toxic_food_note),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** The shared first-aid disclaimer, shown on the screen and above the steps. */
@Composable
private fun DisclaimerBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.emerg_disclaimer),
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** One emergency's card: a critical call button, the disclaimer and the steps. */
@Composable
private fun EmergencyDetailCard(
    type: EmergencyType,
    onCallVetNow: () -> Unit,
    onOpenToxicFood: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(type.labelRes),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Breathing / collapse / bleeding cases: make "call a vet now" the
            // single most prominent element on the screen.
            if (type.isCritical) {
                Button(
                    onClick = onCallVetNow,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.emerg_call_now), fontWeight = FontWeight.Bold)
                }
            }

            // Disclaimer repeated immediately above the steps.
            Text(
                text = stringResource(R.string.emerg_disclaimer),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = stringResource(R.string.emerg_steps_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            type.stepRes.forEachIndexed { index, stepRes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = stringResource(stepRes),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Poisoning-style cases also open the existing Toxic Food list.
            if (type.opensToxicFood) {
                OutlinedButton(
                    onClick = onOpenToxicFood,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.emerg_toxic_food_for_type), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Call-a-vet: the app helpline, plus the selected vet when there is one. */
@Composable
private fun CallVetCard(ctx: Context, savedVet: VerifiedDoctor?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { dialNumber(ctx, "+" + HelplineWhatsAppDigits) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.emerg_call_helpline), fontWeight = FontWeight.Bold)
            }

            // Only offered when the app actually has a selected vet - never invented.
            if (savedVet != null) {
                OutlinedButton(
                    onClick = { dialNumber(ctx, savedVet.phone) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.emerg_call_saved_vet) + ": " + savedVet.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                text = stringResource(R.string.emerg_disclaimer),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** One partner-vet row: name, city and a call button. */
@Composable
private fun VetCallRow(vet: VerifiedDoctor, onCall: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vet.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = vet.clinicCity,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCall,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.emerg_call), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Opens the phone dialer with a number. Never a medicine - a call only. */
private fun dialNumber(ctx: Context, raw: String) {
    val cleaned = raw.filter { it.isDigit() || it == '+' }
    if (cleaned.isEmpty()) {
        return
    }
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cleaned)))
    }
}
