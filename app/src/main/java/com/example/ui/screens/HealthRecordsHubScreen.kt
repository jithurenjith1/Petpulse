package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stethoscope
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Theme colors
// ---------------------------------------------------------------------------
private val PurplePrimary = Color(0xFF6A4C93)
private val GoldAccent = Color(0xFFC9A227)
private val VaccineColor = Color(0xFF6A4C93)
private val WeightColor = Color(0xFFC9A227)
private val DewormingColor = Color(0xFF2E7D32)
private val VetVisitColor = Color(0xFFC62828)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.White,
    background = Color(0xFFF7F5FA),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB39DDB),
    onPrimary = Color(0xFF2C1B45),
    secondary = Color(0xFFE6C64E),
    onSecondary = Color(0xFF1A1500),
    background = Color(0xFF14121A),
    surface = Color(0xFF1F1B2A),
    onBackground = Color(0xFFEDEAF3),
    onSurface = Color(0xFFEDEAF3)
)

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------
private enum class EntryType(val color: Color, val icon: ImageVector) {
    VACCINE(VaccineColor, Icons.Filled.Vaccines),
    WEIGHT(WeightColor, Icons.Filled.MonitorWeight),
    DEWORMING(DewormingColor, Icons.Filled.Medication),
    VET_VISIT(VetVisitColor, Icons.Filled.Stethoscope)
}

private data class HealthEntry(
    val title: String,
    val date: String,
    val notes: String,
    val type: EntryType
)

private fun defaultTimeline(): List<HealthEntry> = listOf(
    HealthEntry("Weight Check", "10 Sep 2026", "14.5 kg, healthy range", EntryType.WEIGHT),
    HealthEntry("Deworming", "1 Sep 2026", "Drontal Plus, next due Dec 2026", EntryType.DEWORMING),
    HealthEntry("Vet Visit — Annual Checkup", "20 Aug 2026", "All clear, healthy", EntryType.VET_VISIT),
    HealthEntry("Rabies Vaccination", "15 Aug 2026", "Next due: Aug 2027", EntryType.VACCINE),
    HealthEntry("Parvo Vaccination", "15 Aug 2026", "Booster dose", EntryType.VACCINE),
    HealthEntry("Weight Check", "1 Aug 2026", "13.8 kg, slight underweight", EntryType.WEIGHT)
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@Composable
fun HealthRecordsHubScreen(
    petName: String = "My Pet",
    onClose: () -> Unit = {}
) {
    val timeline = remember { mutableStateListOf<HealthEntry>().apply { addAll(defaultTimeline()) } }
    val allergies = remember { mutableStateListOf("Chicken allergy", "Sensitive stomach") }

    var showWeightDialog by remember { mutableStateOf(false) }
    var showDewormingDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showAllergyDialog by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 96.dp)
            ) {
                Header(petName = petName)

                Spacer(Modifier.height(16.dp))
                QuickStatsRow()

                Spacer(Modifier.height(16.dp))
                AddEntrySection(
                    onAddWeight = { showWeightDialog = true },
                    onAddDeworming = { showDewormingDialog = true },
                    onShare = { showShareDialog = true }
                )

                Spacer(Modifier.height(20.dp))
                Text(
                    "Medical Timeline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                TimelineList(timeline = timeline)

                Spacer(Modifier.height(20.dp))
                AllergiesCard(
                    allergies = allergies,
                    onAdd = { showAllergyDialog = true }
                )
            }

            // Close FAB
            FloatingActionButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text("X", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        // Dialogs
        if (showWeightDialog) {
            AddWeightDialog(
                onDismiss = { showWeightDialog = false },
                onSave = { weightKg, date ->
                    timeline.add(
                        0,
                        HealthEntry(
                            title = "Weight Check",
                            date = date,
                            notes = "$weightKg kg, recorded",
                            type = EntryType.WEIGHT
                        )
                    )
                    showWeightDialog = false
                }
            )
        }

        if (showDewormingDialog) {
            AddDewormingDialog(
                onDismiss = { showDewormingDialog = false },
                onSave = { medicine, date, nextDue ->
                    timeline.add(
                        0,
                        HealthEntry(
                            title = "Deworming",
                            date = date,
                            notes = "$medicine, next due $nextDue",
                            type = EntryType.DEWORMING
                        )
                    )
                    showDewormingDialog = false
                }
            )
        }

        if (showShareDialog) {
            ShareRecordsDialog(
                petName = petName,
                onDismiss = { showShareDialog = false }
            )
        }

        if (showAllergyDialog) {
            AddAllergyDialog(
                onDismiss = { showAllergyDialog = false },
                onAdd = { name ->
                    allergies.add(name)
                    showAllergyDialog = false
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun Header(petName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = PurplePrimary,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "Health Records",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                petName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PurplePrimary
            )
            Text(
                "Complete medical timeline",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick stats row
// ---------------------------------------------------------------------------
@Composable
private fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Weight",
            value = "14.5 kg",
            sub = "Last: 10 Sep 2026",
            accent = GoldAccent
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Vaccines",
            value = "3/3",
            sub = "Next due: Aug 2027",
            accent = PurplePrimary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Vet Visits",
            value = "2",
            sub = "Last: 20 Aug 2026",
            accent = VetVisitColor
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                sub,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Add entry section
// ---------------------------------------------------------------------------
@Composable
private fun AddEntrySection(
    onAddWeight: () -> Unit,
    onAddDeworming: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Add Weight",
            icon = Icons.Filled.MonitorWeight,
            accent = GoldAccent,
            onClick = onAddWeight
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Add Deworming",
            icon = Icons.Filled.Medication,
            accent = DewormingColor,
            onClick = onAddDeworming
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Share Records",
            icon = Icons.Filled.Share,
            accent = PurplePrimary,
            onClick = onShare
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.6f))
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------
// Timeline
// ---------------------------------------------------------------------------
@Composable
private fun TimelineList(timeline: List<HealthEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(timeline) { entry -> TimelineCard(entry) }
    }
}

@Composable
private fun TimelineCard(entry: HealthEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored vertical bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(entry.type.color)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    entry.date,
                    fontSize = 12.sp,
                    color = entry.type.color,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    entry.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Right icon
            Surface(
                shape = CircleShape,
                color = entry.type.color.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        entry.type.icon,
                        contentDescription = null,
                        tint = entry.type.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Allergies & Conditions card
// ---------------------------------------------------------------------------
@Composable
private fun AllergiesCard(
    allergies: List<String>,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Allergies & Conditions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
            Spacer(Modifier.height(8.dp))
            if (allergies.isEmpty()) {
                Text(
                    "No known allergies or conditions.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                allergies.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(item, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add Weight Dialog
// ---------------------------------------------------------------------------
@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (weightKg: String, date: String) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ),
        confirmButton = {
            Button(
                onClick = { if (weight.isNotBlank()) onSave(weight.trim(), date.trim().ifBlank { "Today" }) },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ---------------------------------------------------------------------------
// Add Deworming Dialog
// ---------------------------------------------------------------------------
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
        title = { Text("Add Deworming", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = medicine,
                    onValueChange = { medicine = it },
                    label = { Text("Medicine name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = nextDue,
                    onValueChange = { nextDue = it },
                    label = { Text("Next due date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (medicine.isNotBlank()) {
                        onSave(
                            medicine.trim(),
                            date.trim().ifBlank { "Today" },
                            nextDue.trim().ifBlank { "Not set" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DewormingColor)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ---------------------------------------------------------------------------
// Share Records Dialog
// ---------------------------------------------------------------------------
@Composable
private fun ShareRecordsDialog(
    petName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share $petName's records", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "A shareable link will be generated. Vet can view all records without installing the app.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Link")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Add Allergy Dialog
// ---------------------------------------------------------------------------
@Composable
private fun AddAllergyDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Allergy / Condition", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Allergy or condition") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

