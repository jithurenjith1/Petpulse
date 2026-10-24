package com.petpulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.R
import com.petpulse.app.data.model.VaccinationRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ---- Status accent colors ----
private val OverdueRed = Color(0xFFD62828)
private val UpcomingOrange = Color(0xFF6A4C93)
private val CompletedGreen = Color(0xFF1D7A6E)

// ---- Model ----
enum class VaccineStatus { Upcoming, Overdue, Completed }

/**
 * A vaccination reminder built from the active pet's REAL records
 * (users/{uid}/pets/{petId}/vaccinations). Nothing here is seeded or demo data:
 * an account with no saved vaccinations renders the honest empty state.
 */
data class VaccinationReminder(
    val id: Long = 0L,
    val vaccineName: String = "",
    // Parsed next-due date. Null when the stored next-due string is missing or
    // uses an unrecognised format; [rawDueDate] is then shown verbatim instead.
    val dueDate: LocalDate? = null,
    val rawDueDate: String = "",
    val veterinarian: String = "",
    val status: VaccineStatus = VaccineStatus.Upcoming
)

private val DisplayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

/**
 * Best-effort parse of the free-text next-due date, mirroring the formats the
 * VaccinationReminderWorker accepts. Returns null when none of them match.
 */
private fun parseVaccinationDate(raw: String): LocalDate? {
    val formats = listOf(
        "MMM d, yyyy", "MMM d yyyy", "d MMMM yyyy",
        "dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "yyyy-MM-dd"
    )
    for (f in formats) {
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern(f, Locale.ENGLISH))
        } catch (_: Exception) {
        }
    }
    return null
}

/**
 * Maps a real VaccinationRecord to a display reminder. Status is derived from the
 * record's own status plus its next-due date: a record already marked Completed
 * stays Completed, an uncompleted record whose due date has passed is Overdue,
 * otherwise Upcoming. No value is invented.
 */
private fun toReminder(record: VaccinationRecord): VaccinationReminder {
    val completed = record.status.equals("Completed", ignoreCase = true)
    val due = parseVaccinationDate(record.nextDueDate)
    val status = when {
        completed -> VaccineStatus.Completed
        due != null && due.isBefore(LocalDate.now()) -> VaccineStatus.Overdue
        else -> VaccineStatus.Upcoming
    }
    return VaccinationReminder(
        id = record.id,
        vaccineName = record.vaccineName,
        dueDate = due,
        rawDueDate = record.nextDueDate,
        veterinarian = record.veterinarian,
        status = status
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationRemindersScreen(vaccinations: List<VaccinationRecord>) {
    // Built straight from the active pet's Firestore records; recomputed whenever
    // the list changes. Hoisted above the LazyColumn, never inside its lambda.
    val reminders = remember(vaccinations) { vaccinations.map { toReminder(it) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.vaxrem_vaccination_reminders),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (reminders.isEmpty()) {
            // Honest empty state: no invented reminders, no demo cards.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.vaxrem_empty),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    VaccinationReminderCard(reminder = reminder)
                }
            }
        }
    }
}

@Composable
private fun VaccinationReminderCard(reminder: VaccinationReminder) {
    val statusColor = when (reminder.status) {
        VaccineStatus.Overdue -> OverdueRed
        VaccineStatus.Upcoming -> UpcomingOrange
        VaccineStatus.Completed -> CompletedGreen
    }
    val statusLabel = when (reminder.status) {
        VaccineStatus.Overdue -> stringResource(R.string.vaxrem_status_overdue)
        VaccineStatus.Upcoming -> stringResource(R.string.vaxrem_status_upcoming)
        VaccineStatus.Completed -> stringResource(R.string.vaxrem_status_completed)
    }

    val due = reminder.dueDate
    val today = LocalDate.now()
    val dueLine = when {
        due != null -> stringResource(R.string.vaxrem_due_label, due.format(DisplayDateFormatter))
        reminder.rawDueDate.isNotBlank() -> stringResource(R.string.vaxrem_due_label, reminder.rawDueDate)
        else -> null
    }
    val daysText: String? = when {
        reminder.status == VaccineStatus.Completed -> stringResource(R.string.vaxrem_done)
        due == null -> null
        reminder.status == VaccineStatus.Overdue -> {
            val days = ChronoUnit.DAYS.between(due, today)
            when {
                days == 0L -> stringResource(R.string.vaxrem_due_today)
                days == 1L -> stringResource(R.string.vaxrem_one_day_overdue)
                else -> stringResource(R.string.vaxrem_days_overdue, days)
            }
        }
        else -> {
            val days = ChronoUnit.DAYS.between(today, due)
            when {
                days == 0L -> stringResource(R.string.vaxrem_due_today)
                days == 1L -> stringResource(R.string.vaxrem_in_one_day)
                else -> stringResource(R.string.vaxrem_in_days, days)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (dueLine != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dueLine,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                if (reminder.veterinarian.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.vaxrem_vet_label, reminder.veterinarian),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
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
                if (daysText != null) {
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
}
