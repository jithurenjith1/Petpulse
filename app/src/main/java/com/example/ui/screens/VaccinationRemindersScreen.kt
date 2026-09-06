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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ---- App palette ----
private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

// Status accent colors
private val OverdueRed = Color(0xFFD7263D)
private val UpcomingOrange = Color(0xFFE07856)
private val CompletedGreen = Color(0xFF2A9D8F)

// ---- Model ----
enum class VaccineStatus { Upcoming, Overdue, Completed }

data class VaccinationReminder(
    val id: Int,
    val vaccineName: String,
    val dueDate: LocalDate,
    val veterinarian: String,
    val status: VaccineStatus
)

/**
 * Computes the display status for a reminder that has not been explicitly completed:
 * dates before today are Overdue, otherwise Upcoming.
 */
private fun computeStatus(dueDate: LocalDate, completed: Boolean): VaccineStatus =
    if (completed) VaccineStatus.Completed
    else if (dueDate.isBefore(LocalDate.now())) VaccineStatus.Overdue
    else VaccineStatus.Upcoming

/**
 * Seed data for the active pet's upcoming vaccination reminders.
 * Uses a simple in-memory list — no Firestore / persistence layer required.
 */
private fun seedReminders(): List<VaccinationReminder> {
    val today = LocalDate.now()
    return listOf(
        VaccinationReminder(1, "Rabies Booster", today.minusDays(6), "Dr. Meera Rao", VaccineStatus.Overdue),
        VaccinationReminder(2, "DHPP (Distemper)", today.plusDays(10), "Dr. Meera Rao", VaccineStatus.Upcoming),
        VaccinationReminder(3, "Bordetella", today.plusDays(28), "Dr. Arjun Nair", VaccineStatus.Upcoming),
        VaccinationReminder(4, "Leptospirosis", today.minusDays(40), "Dr. Meera Rao", VaccineStatus.Completed),
        VaccinationReminder(5, "Lyme Disease", today.plusDays(55), "Dr. Arjun Nair", VaccineStatus.Upcoming)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationRemindersScreen() {
    // In-memory mutable list of reminders for the active pet.
    val reminders = remember { mutableStateListOf<VaccinationReminder>().apply { addAll(seedReminders()) } }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CreamBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vaccination Reminders",
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CoralPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(reminders, key = { it.id }) { reminder ->
                VaccinationReminderCard(reminder = reminder)
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, date, vet ->
                val nextId = (reminders.maxOfOrNull { it.id } ?: 0) + 1
                val parsedDate = runCatching {
                    LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrElse { LocalDate.now().plusMonths(1) }
                val status = computeStatus(parsedDate, completed = false)
                reminders.add(
                    VaccinationReminder(
                        id = nextId,
                        vaccineName = name,
                        dueDate = parsedDate,
                        veterinarian = vet,
                        status = status
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun VaccinationReminderCard(reminder: VaccinationReminder) {
    val (statusColor, statusLabel) = when (reminder.status) {
        VaccineStatus.Overdue -> OverdueRed to "Overdue"
        VaccineStatus.Upcoming -> UpcomingOrange to "Upcoming"
        VaccineStatus.Completed -> CompletedGreen to "Completed"
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val today = LocalDate.now()
    val daysText = when (reminder.status) {
        VaccineStatus.Completed -> "Done"
        VaccineStatus.Overdue -> {
            val days = ChronoUnit.DAYS.between(reminder.dueDate, today)
            if (days == 0L) "Due today" else "$days day${if (days == 1L) "" else "s"} overdue"
        }
        VaccineStatus.Upcoming -> {
            val days = ChronoUnit.DAYS.between(today, reminder.dueDate)
            if (days == 0L) "Due today" else "in $days day${if (days == 1L) "" else "s"}"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.vaccineName,
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Due: ${reminder.dueDate.format(dateFormatter)}",
                    color = DarkText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Vet: ${reminder.veterinarian}",
                    color = DarkText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = daysText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (vaccineName: String, date: String, veterinarian: String) -> Unit
) {
    var vaccineName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var veterinarian by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Vaccination Reminder",
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = CoralPrimary,
                        focusedLabelColor = CoralPrimary,
                        cursorColor = CoralPrimary
                    )
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Due date (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = CoralPrimary,
                        focusedLabelColor = CoralPrimary,
                        cursorColor = CoralPrimary
                    )
                )
                OutlinedTextField(
                    value = veterinarian,
                    onValueChange = { veterinarian = it },
                    label = { Text("Veterinarian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = CoralPrimary,
                        focusedLabelColor = CoralPrimary,
                        cursorColor = CoralPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (vaccineName.isNotBlank()) {
                        onAdd(vaccineName.trim(), date.trim(), veterinarian.trim().ifBlank { "—" })
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    contentColor = Color.White
                ),
                enabled = vaccineName.isNotBlank()
            ) {
                Text("Add Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkText.copy(alpha = 0.7f))
            }
        }
    )
}

