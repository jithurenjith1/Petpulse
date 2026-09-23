package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

// ---------------------------------------------------------------------------
// Theme palette
// ---------------------------------------------------------------------------
private val Purple = Color(0xFFBC5233)
private val PurpleDark = Color(0xFF9C4227)
private val PurpleLight = Color(0xFFD97F5C)
private val Gold = Color(0xFFA87A1F)
private val GoldDark = Color(0xFFA87A1F)
private val OnlineGreen = Color(0xFF2E7D32)
private val ScheduledGrey = Color(0xFF5C554F)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceDark = Color(0xFF221C15)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Color.Black,
    background = SurfaceLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEADDFF),
    onSurfaceVariant = Color(0xFF49454F),
)

private val DarkColors = darkColorScheme(
    primary = PurpleLight,
    onPrimary = Color.Black,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Color.Black,
    background = SurfaceDark,
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF241F30),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3A3147),
    onSurfaceVariant = Color(0xFFCAC4D0),
)

// ---------------------------------------------------------------------------
// Domain models
// ---------------------------------------------------------------------------
private enum class OnlineStatus { ONLINE, SCHEDULED }

private data class Vet(
    val name: String,
    val qualification: String,
    val registration: String,
    val specialisation: String,
    val experienceYears: Int,
    val rating: Double,
    val consultCount: Int,
    val priceRupees: Int,
    val status: OnlineStatus,
    val verified: Boolean = false,
)

private val DemoVets = listOf(
    Vet(
        name = "Dr. Ananya Menon",
        qualification = "BVSc, MVSc",
        registration = "KVC-2018-4291",
        verified = true,
        specialisation = "Small Animal Medicine",
        experienceYears = 8,
        rating = 4.9,
        consultCount = 312,
        priceRupees = 299,
        status = OnlineStatus.ONLINE,
    ),
    Vet(
        name = "Dr. Rajesh Kumar",
        qualification = "BVSc & AH",
        registration = "KVC-2015-3387",
        verified = true,
        specialisation = "Surgery & Orthopedics",
        experienceYears = 12,
        rating = 4.8,
        consultCount = 541,
        priceRupees = 399,
        status = OnlineStatus.ONLINE,
    ),
    Vet(
        name = "Dr. Priya Nair",
        qualification = "BVSc",
        registration = "KVC-2020-5612",
        verified = true,
        specialisation = "Dermatology & Allergies",
        experienceYears = 5,
        rating = 4.7,
        consultCount = 188,
        priceRupees = 249,
        status = OnlineStatus.SCHEDULED,
    ),
    Vet(
        name = "Dr. Mohammed Faizal",
        qualification = "MVSc Medicine",
        registration = "KVC-2012-2214",
        verified = true,
        specialisation = "Internal Medicine, Diabetes & Kidney care",
        experienceYears = 15,
        rating = 4.9,
        consultCount = 720,
        priceRupees = 499,
        status = OnlineStatus.ONLINE,
    ),
    Vet(
        name = "Dr. Lakshmi Warrier",
        qualification = "BVSc, Certified Feline Specialist",
        registration = "KVC-2019-4820",
        verified = true,
        specialisation = "Cats & Exotics",
        experienceYears = 6,
        rating = 4.8,
        consultCount = 234,
        priceRupees = 349,
        status = OnlineStatus.SCHEDULED,
    ),
)

private data class Slot(val label: String)
private val SlotOptions = listOf(
    Slot("Now"),
    Slot("30 min"),
    Slot("1 hour"),
    Slot("6 PM"),
)

// ---------------------------------------------------------------------------
// Root screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetTeleconsultScreen(
    onClose: () -> Unit = {}
) {
    // Apply our Purple/Gold scheme; respects the device dark-mode setting.
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors

    MaterialTheme(colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var bookingVet by remember { mutableStateOf<Vet?>(null) }
            var confirmedMessage by remember { mutableStateOf<String?>(null) }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onClose,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }

                    // 1. Header
                    item { HeaderSection() }

                    // 2. Featured banner
                    item { FeaturedBanner() }

                    // 3 & 4. Vet list
                    items(DemoVets, key = { it.registration }) { vet ->
                        VetCard(
                            vet = vet,
                            onBook = { bookingVet = vet }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            // 6. Booking dialog
            bookingVet?.let { vet ->
                BookingDialog(
                    vet = vet,
                    onDismiss = { bookingVet = null },
                    onConfirm = { slot, petName, symptoms ->
                        confirmedMessage =
                            "Booking confirmed! ${vet.name} will consult $petName at $slot. " +
                                    "You'll receive a call/video link."
                        bookingVet = null
                    }
                )
            }

            // 7. Confirmation dialog
            confirmedMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { confirmedMessage = null },
                    title = { Text(stringResource(R.string.vet_consultation_booked), fontWeight = FontWeight.Bold) },
                    text = { Text(message) },
                    confirmButton = {
                        Button(
                            onClick = { confirmedMessage = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text(stringResource(R.string.vet_got_it)) }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header section
// ---------------------------------------------------------------------------
@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(Gold, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Petpulse",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.vet_vet_teleconsultation),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.vet_connect_with_verified_vets_from_home),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// Featured banner
// ---------------------------------------------------------------------------
@Composable
private fun FeaturedBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Petpulse Care members get 2 free consults/month + priority booking",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Vet card
// ---------------------------------------------------------------------------
@Composable
private fun VetCard(
    vet: Vet,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: name + online status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    if (vet.verified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "KVC Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                OnlineChip(vet.status)
            }

            Spacer(Modifier.height(4.dp))

            // Qualification + registration number
            Text(
                text = "${vet.qualification} (${vet.registration})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            // Specialisation
            Text(
                text = vet.specialisation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(6.dp))

            // Experience + rating + consult count
            Text(
                text = "${vet.experienceYears} yrs exp  •  ★ ${vet.rating}  •  ${vet.consultCount} consults",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Price (gold) + Book button (purple)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${vet.priceRupees}/consult",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Gold
                )
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.vet_book_consultation),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Online status chip
// ---------------------------------------------------------------------------
@Composable
private fun OnlineChip(status: OnlineStatus) {
    val (container, content, label) = when (status) {
        OnlineStatus.ONLINE -> Triple(
            OnlineGreen,
            Color.White,
            "Online now"
        )
        OnlineStatus.SCHEDULED -> Triple(
            ScheduledGrey,
            Color.White,
            "Scheduled"
        )
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content
        ),
        shape = RoundedCornerShape(50)
    )
}

// ---------------------------------------------------------------------------
// Booking dialog
// ---------------------------------------------------------------------------
@Composable
private fun BookingDialog(
    vet: Vet,
    onDismiss: () -> Unit,
    onConfirm: (slot: String, petName: String, symptoms: String) -> Unit
) {
    var selectedSlot by remember { mutableStateOf(SlotOptions.first().label) }
    var petName by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var petNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.vet_book_consultation),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = vet.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.vet_choose_a_slot),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SlotOptions.forEach { slot ->
                        val selected = slot.label == selectedSlot
                        AssistChip(
                            onClick = { selectedSlot = slot.label },
                            label = { Text(slot.label, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) Gold else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = petName,
                    onValueChange = {
                        petName = it
                        petNameError = it.isBlank()
                    },
                    label = { Text(stringResource(R.string.vet_pet_name)) },
                    isError = petNameError,
                    supportingText = {
                        if (petNameError) Text(stringResource(R.string.vet_enter_your_pet_s_name))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text(stringResource(R.string.vet_brief_symptom_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (petName.isBlank()) {
                        petNameError = true
                    } else {
                        onConfirm(selectedSlot, petName.trim(), symptoms.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(stringResource(R.string.vet_confirm_booking)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.vet_cancel)) }
        }
    )
}



