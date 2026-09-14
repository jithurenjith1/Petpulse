package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Theme colors
private val PurplePrimary = Color(0xFF6A4C93)
private val GoldAccent = Color(0xFFC9A227)
private val GreenOk = Color(0xFF4CAF50)
private val RedAlert = Color(0xFFE63946)
private val DarkBg = Color(0xFF121016)
private val DarkSurface = Color(0xFF1E1B24)
private val LightBg = Color(0xFFFDFCFF)
private val LightSurface = Color(0xFFFFFFFF)

// Timeline entry types
private enum class EntryType { VACCINE, WEIGHT, DEWORMING, VET_VISIT }

private data class TimelineEntry(
    val type: EntryType,
    val title: String,
    val date: String,
    val notes: String
)

private fun entryColor(t: EntryType): Color = when (t) {
    EntryType.VACCINE -> PurplePrimary
    EntryType.WEIGHT -> GoldAccent
    EntryType.DEWORMING -> GreenOk
    EntryType.VET_VISIT -> RedAlert
}

private fun entryIcon(t: EntryType) = when (t) {
    EntryType.VACCINE -> Icons.Filled.Vaccines
    EntryType.WEIGHT -> Icons.Filled.Scale
    EntryType.DEWORMING -> Icons.Filled.Medication
    EntryType.VET_VISIT -> Icons.Filled.MonitorHeart
}

@Composable
fun HealthRecordsHubScreen(
    petName: String = "My Pet",
    onClose: () -> Unit = {}
) {
    var isDark by remember { mutableStateOf(true) }
    val bgColor = if (isDark) DarkBg else LightBg
    val surfaceColor = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color(0xFFF3EEF7) else Color(0xFF1A1A1A)
    val mutedColor = if (isDark) Color(0xFFB8B0C6) else Color(0xFF666666)

    // Demo timeline data (in-memory)
    val timeline = remember {
        mutableStateListOf(
            TimelineEntry(EntryType.WEIGHT, "Weight Check", "10 Sep 2026", "14.5 kg, healthy range"),
            TimelineEntry(EntryType.DEWORMING, "Deworming", "1 Sep 2026", "Drontal Plus, next due Dec 2026"),
            TimelineEntry(EntryType.VACCINE, "Rabies Vaccination", "15 Aug 2026", "Next due: Aug 2027"),
            TimelineEntry(EntryType.VET_VISIT, "Vet Visit — Annual Checkup", "20 Aug 2026", "All clear, healthy"),
            TimelineEntry(EntryType.VACCINE, "Parvo Vaccination", "15 Aug 2026", "Booster dose"),
            TimelineEntry(EntryType.WEIGHT, "Weight Check", "1 Aug 2026", "13.8 kg, slight underweight")
        )
    }

    val allergies = remember { mutableStateListOf("Chicken protein (mild)") }

    var showAddWeight by remember { mutableStateOf(false) }
    var showAddDeworming by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showAddAllergy by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            Text("Health Records", color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("$petName — complete medical timeline", color = mutedColor, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Weight", "14.5 kg", "Last: 10 Sep", entryColor(EntryType.WEIGHT), surfaceColor, textColor, mutedColor, Modifier.weight(1f))
                StatCard("Vaccines", "3/3", "Next: Aug 2027", entryColor(EntryType.VACCINE), surfaceColor, textColor, mutedColor, Modifier.weight(1f))
                StatCard("Vet Visits", "2", "Last: 20 Aug", entryColor(EntryType.VET_VISIT), surfaceColor, textColor, mutedColor, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            // Add Entry Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showAddWeight = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary)
                ) {
                    Icon(Icons.Filled.Scale, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Weight", fontSize = 11.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { showAddDeworming = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenOk)
                ) {
                    Icon(Icons.Filled.Medication, contentDescription = null, tint = GreenOk, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Deworming", fontSize = 11.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { showShare = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Text("Share", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))

            // Timeline
            Text("Medical Timeline", color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            timeline.forEach { entry ->
                TimelineCard(entry, surfaceColor, textColor, mutedColor)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Allergies & Conditions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allergies & Conditions", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddAllergy = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Allergy", tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    if (allergies.isEmpty()) {
                        Text("None recorded", color = mutedColor, fontSize = 12.sp)
                    } else {
                        allergies.forEach { allergy ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(GoldAccent, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(allergy, color = textColor, fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Close FAB
        FloatingActionButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            containerColor = PurplePrimary,
            contentColor = Color.White
        ) { Text("X", fontWeight = FontWeight.Bold) }
    }

    // ---- Dialogs ----

    if (showAddWeight) {
        AddWeightDialog(
            onDismiss = { showAddWeight = false },
            onSave = { w, d ->
                timeline.add(0, TimelineEntry(EntryType.WEIGHT, "Weight Check", d, "$w kg"))
                showAddWeight = false
            }
        )
    }

    if (showAddDeworming) {
        AddDewormingDialog(
            onDismiss = { showAddDeworming = false },
            onSave = { med, d, next ->
                timeline.add(0, TimelineEntry(EntryType.DEWORMING, "Deworming", d, "$med, next due $next"))
                showAddDeworming = false
            }
        )
    }

    if (showShare) {
        ShareRecordsDialog(
            petName = petName,
            onDismiss = { showShare = false }
        )
    }

    if (showAddAllergy) {
        AddAllergyDialog(
            onDismiss = { showAddAllergy = false },
            onSave = { allergy ->
                allergies.add(allergy)
                showAddAllergy = false
            }
        )
    }
}

// ---- Stat Card ----
@Composable
private fun StatCard(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    surface: Color,
    textColor: Color,
    mutedColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(modifier = Modifier.size(8.dp).background(accent, CircleShape))
            Spacer(Modifier.height(6.dp))
            Text(label, color = mutedColor, fontSize = 11.sp)
            Text(value, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = mutedColor, fontSize = 10.sp)
        }
    }
}

// ---- Timeline Card ----
@Composable
private fun TimelineCard(
    entry: TimelineEntry,
    surface: Color,
    textColor: Color,
    mutedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(entryColor(entry.type), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(entry.date, color = mutedColor, fontSize = 11.sp)
                Text(entry.notes, color = mutedColor, fontSize = 11.sp)
            }

            Icon(
                imageVector = entryIcon(entry.type),
                contentDescription = null,
                tint = entryColor(entry.type).copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ---- Add Weight Dialog ----
@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (weight: String, date: String) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (weight.isNotBlank()) onSave(weight.trim(), date.ifBlank { "Today" }) },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---- Add Deworming Dialog ----
@Composable
private fun AddDewormingDialog(
    onDismiss: () -> Unit,
    onSave: (medicine: String, date: String, nextDue: String) -> Unit
) {
    var medicine by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today") }
    var nextDue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Deworming Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = medicine,
                    onValueChange = { medicine = it },
                    label = { Text("Medicine Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date Given") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextDue,
                    onValueChange = { nextDue = it },
                    label = { Text("Next Due Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (medicine.isNotBlank()) {
                        onSave(medicine.trim(), date.ifBlank { "Today" }, nextDue.ifBlank { "3 months" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenOk)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---- Share Records Dialog ----
@Composable
private fun ShareRecordsDialog(
    petName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share $petName's Records") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "A shareable link will be generated. Your vet can view all records without installing the app.",
                    fontSize = 13.sp
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0ECF5))
                ) {
                    Text(
                        "petpulse.app/records/ABC123",
                        fontSize = 12.sp,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) { Text("Copy Link") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ---- Add Allergy Dialog ----
@Composable
private fun AddAllergyDialog(
    onDismiss: () -> Unit,
    onSave: (allergy: String) -> Unit
) {
    var allergy by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Allergy / Condition") },
        text = {
            OutlinedTextField(
                value = allergy,
                onValueChange = { allergy = it },
                label = { Text("Allergy or condition") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (allergy.isNotBlank()) onSave(allergy.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) { Text("Add", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

