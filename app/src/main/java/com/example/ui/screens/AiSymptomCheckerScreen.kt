package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Theme colors ----
private val Purple = Color(0xFF6A4C93)
private val Gold = Color(0xFFC9A227)
private val DarkBackground = Color(0xFF121016)
private val DarkSurface = Color(0xFF1E1B24)
private val DarkSurfaceVariant = Color(0xFF272330)
private val OnDark = Color(0xFFF3EEF7)
private val OnDarkMuted = Color(0xFFB8B0C6)

// ---- Triage levels ----
enum class TriageLevel {
    NONE, GREEN, YELLOW, ORANGE, RED
}

private data class TriageResult(
    val level: TriageLevel,
    val title: String,
    val recommendation: String,
    val showBookVet: Boolean
)

// ---- Species ----
private enum class PetSpecies(val label: String) {
    DOG("Dog"),
    CAT("Cat"),
    BIRD("Bird"),
    RABBIT("Rabbit")
}

// ---- Symptoms ----
private enum class Symptom(val label: String) {
    VOMITING("Vomiting"),
    DIARRHEA("Diarrhea"),
    LETHARGY("Lethargy"),
    LOSS_OF_APPETITE("Loss of Appetite"),
    COUGHING("Coughing"),
    SNEEZING("Sneezing"),
    ITCHING("Itching/Scratching"),
    LAMENESS("Lameness"),
    EXCESSIVE_THIRST("Excessive Thirst"),
    WEIGHT_LOSS("Weight Loss"),
    EYE_DISCHARGE("Eye Discharge"),
    EAR_ODOR("Ear Odor"),
    DIFFICULTY_BREATHING("Difficulty Breathing"),
    SEIZURES("Seizures"),
    BLOATED_ABDOMEN("Bloated Abdomen")
}

/**
 * Rules-based triage logic.
 *
 * RED ALERT  - Difficulty Breathing, Seizures, Bloated Abdomen
 * ORANGE     - Vomiting+Diarrhea, Lethargy+Loss of Appetite, Excessive Thirst+Weight Loss
 * YELLOW     - Sneezing, Itching, Eye Discharge, Ear Odor, Coughing alone
 * GREEN      - single mild symptom
 */
private fun analyzeSymptoms(selected: Set<Symptom>): TriageResult {
    if (selected.isEmpty()) {
        return TriageResult(
            level = TriageLevel.NONE,
            title = "No symptoms selected",
            recommendation = "Select one or more symptoms above, then tap Analyze to get a triage suggestion.",
            showBookVet = false
        )
    }

    // RED — emergency symptoms
    val redSet = setOf(
        Symptom.DIFFICULTY_BREATHING,
        Symptom.SEIZURES,
        Symptom.BLOATED_ABDOMEN
    )
    if (selected.any { it in redSet }) {
        return TriageResult(
            level = TriageLevel.RED,
            title = "RED ALERT — Emergency",
            recommendation = "One or more emergency symptoms detected. This may be life-threatening. " +
                "Seek immediate veterinary attention — do not wait.",
            showBookVet = true
        )
    }

    // ORANGE — urgent combinations
    val orangeCombos = listOf(
        setOf(Symptom.VOMITING, Symptom.DIARRHEA),
        setOf(Symptom.LETHARGY, Symptom.LOSS_OF_APPETITE),
        setOf(Symptom.EXCESSIVE_THIRST, Symptom.WEIGHT_LOSS)
    )
    if (orangeCombos.any { combo -> combo.all { it in selected } }) {
        return TriageResult(
            level = TriageLevel.ORANGE,
            title = "ORANGE — See a vet within 24 hours",
            recommendation = "These symptom combinations can indicate dehydration, infection, or " +
                "metabolic issues (e.g. diabetes/kidney disease). Prompt veterinary evaluation " +
                "within 24 hours is recommended.",
            showBookVet = true
        )
    }

    // YELLOW — monitor / home-care-possible symptoms
    val yellowSet = setOf(
        Symptom.SNEEZING,
        Symptom.ITCHING,
        Symptom.EYE_DISCHARGE,
        Symptom.EAR_ODOR,
        Symptom.COUGHING
    )
    val yellowSelected = selected.filter { it in yellowSet }
    if (yellowSelected.isNotEmpty()) {
        // Coughing counts as YELLOW only when it appears alone; if other non-yellow
        // symptoms are present, fall through to GREEN/multi-symptom handling below.
        val hasNonYellow = selected.any { it !in yellowSet }
        if (!hasNonYellow || (selected.size == 1 && selected.first() == Symptom.COUGHING)) {
            val onlyCoughing = selected.size == 1 && selected.first() == Symptom.COUGHING
            val rec = if (onlyCoughing) {
                "An isolated cough may be minor but can also indicate respiratory or cardiac " +
                    "issues. Monitor closely; if it persists beyond a day or worsens, consult a vet."
            } else {
                "These are often manageable with home care and monitoring (e.g. mild allergies, " +
                    "minor irritation). Keep your pet comfortable and hydrated. If symptoms " +
                    "worsen or persist beyond 2–3 days, consult a veterinarian."
            }
            return TriageResult(
                level = TriageLevel.YELLOW,
                title = "YELLOW — Monitor / home care possible",
                recommendation = rec,
                showBookVet = false
            )
        }
    }

    // GREEN — single mild symptom
    if (selected.size == 1) {
        return TriageResult(
            level = TriageLevel.GREEN,
            title = "GREEN — Likely minor",
            recommendation = "A single mild symptom is often not cause for alarm. Keep an eye on " +
                "your pet's behaviour, appetite and energy. If it persists or new symptoms appear, " +
                "re-run the check or contact your vet.",
            showBookVet = false
        )
    }

    // Multiple non-emergency, non-combo symptoms -> cautious YELLOW/GREEN boundary.
    return TriageResult(
        level = TriageLevel.YELLOW,
        title = "YELLOW — Monitor",
        recommendation = "Multiple symptoms are present. None match an emergency pattern, but " +
            "combinations can change quickly. Monitor closely over the next 12–24 hours and " +
            "consult a vet if anything worsens.",
        showBookVet = false
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiSymptomCheckerScreen(
    petName: String = "My Pet",
    onBookVet: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var selectedSpecies by remember { mutableStateOf(PetSpecies.DOG) }
    val selectedSymptoms = remember { mutableStateOf(setOf<Symptom>()) }
    var triageResult by remember { mutableStateOf<TriageResult?>(null) }

    fun toggleSymptom(s: Symptom) {
        val current = selectedSymptoms.value
        selectedSymptoms.value = if (s in current) current - s else current + s
        // Invalidate previous result when inputs change.
        triageResult = null
    }

    Scaffold(
        containerColor = DarkBackground,
        contentColor = OnDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClose,
                containerColor = Purple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(text = "X", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Header ----
            Text(
                text = stringResource(R.string.triage_ai_symptom_checker),
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Triage assistant for $petName",
                color = OnDarkMuted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))

            // ---- Species selector ----
            Text(
                text = stringResource(R.string.triage_select_species),
                color = OnDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PetSpecies.entries.forEach { species ->
                    FilterChip(
                        selected = selectedSpecies == species,
                        onClick = {
                            selectedSpecies = species
                            triageResult = null
                        },
                        label = { Text(species.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant,
                            labelColor = OnDark,
                            selectedContainerColor = Purple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---- Symptom grid ----
            Text(
                text = stringResource(R.string.triage_tap_all_symptoms_that_apply),
                color = OnDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                items(Symptom.entries.toList()) { symptom ->
                    val isSelected = symptom in selectedSymptoms.value
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toggleSymptom(symptom) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Purple.copy(alpha = 0.85f) else DarkSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) Gold else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Gold else DarkSurface)
                                    .border(1.dp, OnDarkMuted, CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = symptom.label,
                                color = if (isSelected) Color.White else OnDark,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---- Analyze button ----
            Button(
                onClick = {
                    triageResult = analyzeSymptoms(selectedSymptoms.value)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.triage_analyze),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(20.dp))

            // ---- Triage result ----
            AnimatedVisibility(
                visible = triageResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                triageResult?.let { result ->
                    TriageResultCard(
                        result = result,
                        onBookVet = onBookVet
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---- Disclaimer ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Text(
                    text = "This is guidance, not a diagnosis. Always consult a veterinarian for serious concerns.",
                    color = OnDarkMuted,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(80.dp)) // room for FAB
        }
    }
}

@Composable
private fun TriageResultCard(
    result: TriageResult,
    onBookVet: () -> Unit
) {
    val (accent, container) = when (result.level) {
        TriageLevel.RED -> Color(0xFFE53935) to Color(0xFF3A1C1C)
        TriageLevel.ORANGE -> Color(0xFFFB8C00) to Color(0xFF332314)
        TriageLevel.YELLOW -> Gold to Color(0xFF2A2616)
        TriageLevel.GREEN -> Color(0xFF43A047) to Color(0xFF142A1A)
        TriageLevel.NONE -> OnDarkMuted to DarkSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Level badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accent)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = result.level.name,
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = result.title,
                color = OnDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = result.recommendation,
                color = OnDark.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            if (result.showBookVet) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBookVet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.triage_book_vet_consultation),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

