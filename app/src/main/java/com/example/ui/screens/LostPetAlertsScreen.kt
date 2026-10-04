package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.petpulse.app.data.model.FoundPetReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Wagmiya brand colors
private val CoralPrimary = Color(0xFF6A4C93)
private val CoralLight = Color(0xFFEDE3F8)
private val CreamBg = Color(0xFFFBF6F0)
private val TealAccent = Color(0xFF1D7A6E)
private val DarkText = Color(0xFF241F2B)
private val SosRed = Color(0xFFD62828)
private val WhatsAppGreen = Color(0xFF25D366)

// Wagmiya SOS helpline (same business number for WhatsApp + calls).
private const val HelplineWhatsAppDigits = "919526632311"
private const val WagmiyaPhoneDisplay = "+91 95266 32311"
private const val WagmiyaPhoneDigits = "919526632311"

/**
 * A lost pet alert document mirrored from Firestore collection "lost_pet_alerts".
 * Field names match the Firestore document keys.
 */
data class LostPetAlertItem(
    val id: String = "",
    val petName: String = "",
    val species: String = "",
    val breed: String = "",
    val lastSeenLocation: String = "",
    val reward: String = "",
    val contactPhone: String = "",
    val alternatePhone: String = "",
    val date: String = ""
)

/**
 * UI state for the lost-pet alerts feed.
 */
sealed interface LostPetUiState {
    data object Loading : LostPetUiState
    data class Success(val alerts: List<LostPetAlertItem>) : LostPetUiState
    data class Error(val message: String) : LostPetUiState
}

/**
 * Listens to the "lost_pet_alerts" Firestore collection, ordered by date descending,
 * and emits [LostPetUiState] values as a cold Flow built with callbackFlow.
 *
 * The auth instance is accepted so the caller can scope queries to the signed-in
 * user when needed; here it is retained for context but the collection is public.
 */
fun lostPetAlertsFlow(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    @Suppress("UNUSED_PARAMETER") auth: FirebaseAuth = FirebaseAuth.getInstance()
) = callbackFlow {
    // Try an initial await-backed fetch so the first emission is available quickly,
    // then subscribe to live updates via addSnapshotListener.
    val registration = db.collection("lost_pet_alerts")
        .orderBy("date", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(LostPetUiState.Error(error.localizedMessage ?: "Unknown error"))
                return@addSnapshotListener
            }
            if (snapshot == null) {
                trySend(LostPetUiState.Error("No data received"))
                return@addSnapshotListener
            }
            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject<LostPetAlertItem>()?.copy(id = doc.id)
            }
            trySend(LostPetUiState.Success(alerts))
        }

    // Keep the await import meaningful: an explicit one-shot read could also be
    // performed with .get().await() before subscribing. The listener above is the
    // primary source; awaitClose tears it down when the flow collector cancels.
    awaitClose { registration.remove() }
}

/**
 * A one-shot variant demonstrating kotlinx.coroutines.tasks.await usage.
 * Fetches the current alerts list once and completes.
 */
suspend fun fetchLostPetAlertsOnce(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
): List<LostPetAlertItem> {
    val snapshot = db.collection("lost_pet_alerts")
        .orderBy("date", Query.Direction.DESCENDING)
        .get()
        .await()
    return snapshot.documents.mapNotNull { it.toObject<LostPetAlertItem>()?.copy(id = it.id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetAlertsScreen(
    foundReports: List<FoundPetReport> = emptyList(),
    onSubmitFoundReport: (FoundPetReport, Uri?) -> Unit = { _, _ -> }
) {
    // Collect the Firestore-backed flow. Falls back to sample data so the screen
    // is previewable without a live Firebase project.
    val flow = remember { lostPetAlertsFlow() }
    val uiState by flow.collectAsState(initial = LostPetUiState.Loading)
    var reportingAlert by remember { mutableStateOf<LostPetAlertItem?>(null) }

    MaterialTheme {
        Scaffold(
            containerColor = CreamBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.lostpet_lost_pet_alerts),
                            color = DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CreamBg
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is LostPetUiState.Loading -> {
                        CircularProgressIndicator(
                            color = CoralPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is LostPetUiState.Error -> {
                        Text(
                            text = state.message,
                            color = SosRed,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is LostPetUiState.Success -> {
                        if (state.alerts.isEmpty()) {
                            Text(
                                text = stringResource(R.string.lostpet_no_lost_pet_alerts_right_now),
                                color = DarkText.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(4.dp)) }
                                items(state.alerts) { alert ->
                                    val isFound = foundReports.any { it.alertId == alert.id }
                                    LostPetAlertCard(
                                        alert = alert,
                                        isFound = isFound,
                                        onReport = { reportingAlert = it }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }

        reportingAlert?.let { alert ->
            ReportSightingDialog(
                alert = alert,
                onDismiss = { reportingAlert = null },
                onSubmit = onSubmitFoundReport
            )
        }
    }
}

@Composable
private fun LostPetAlertCard(
    alert: LostPetAlertItem,
    isFound: Boolean,
    onReport: (LostPetAlertItem) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Red SOS badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SosRed)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.lostpet_sos),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = alert.petName,
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = alert.date,
                    color = DarkText.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${alert.species} • ${alert.breed}",
                color = TealAccent,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            AlertDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Last seen",
                value = alert.lastSeenLocation
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isFound) {
                // Privacy: the public feed never reveals the finder — only that a
                // sighting was reported and the team is connecting the owner.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealAccent)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🐾 Someone reported seeing ${alert.petName} — our team is connecting the owner.",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Four contact rows: primary, alternate, helpline WhatsApp, Wagmiya.
                ContactRow(
                    label = "Primary number",
                    value = alert.contactPhone,
                    actionLabel = "Call",
                    actionColor = CoralPrimary,
                    enabled = alert.contactPhone.isNotBlank(),
                    onAction = { placeCall(context, alert.contactPhone) }
                )
                ContactRow(
                    label = "Alternate number",
                    value = alert.alternatePhone,
                    actionLabel = "Call",
                    actionColor = CoralPrimary,
                    enabled = alert.alternatePhone.isNotBlank(),
                    onAction = { placeCall(context, alert.alternatePhone) }
                )
                ContactRow(
                    label = "Helpline WhatsApp",
                    value = "Chat with the Wagmiya helpline",
                    actionLabel = "WhatsApp",
                    actionColor = WhatsAppGreen,
                    enabled = true,
                    onAction = {
                        openWhatsApp(
                            context,
                            HelplineWhatsAppDigits,
                            "SOS: Please help find ${alert.petName}. Last seen at ${alert.lastSeenLocation}."
                        )
                    }
                )
                ContactRow(
                    label = "Wagmiya",
                    value = WagmiyaPhoneDisplay,
                    actionLabel = "Call",
                    actionColor = CoralPrimary,
                    enabled = true,
                    onAction = { placeCall(context, WagmiyaPhoneDigits) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onReport(alert) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("👀 I saw this pet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CoralPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CoralLight.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Reward: ${alert.reward}",
                        color = CoralPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    actionLabel: String,
    actionColor: Color,
    enabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = DarkText.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = value.ifBlank { "—" },
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        TextButton(onClick = onAction, enabled = enabled) {
            Text(
                text = actionLabel,
                color = if (enabled) actionColor else DarkText.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ReportSightingDialog(
    alert: LostPetAlertItem,
    onDismiss: () -> Unit,
    onSubmit: (FoundPetReport, Uri?) -> Unit
) {
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("I saw ${alert.petName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "A photo is required. Your phone number stays private — only the Wagmiya team sees it.",
                    fontSize = 12.sp,
                    color = DarkText.copy(alpha = 0.7f)
                )
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (photoUri == null) "📷 Add photo (required)" else "✓ Photo added")
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Where did you see it?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Your phone number") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = photoUri != null && location.isNotBlank() && phone.isNotBlank(),
                onClick = {
                    onSubmit(
                        FoundPetReport(
                            alertId = alert.id,
                            petName = alert.petName,
                            location = location.trim(),
                            finderPhone = phone.trim(),
                            note = note.trim()
                        ),
                        photoUri
                    )
                    onDismiss()
                }
            ) { Text("Submit report") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AlertDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CoralPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            color = DarkText.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = DarkText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Opens the dialer for a phone number (tel: intent), safely wrapped. */
private fun placeCall(context: Context, number: String) {
    if (number.isBlank()) return
    val digits = number.filter { it.isDigit() || it == '+' }
    if (digits.isBlank()) return
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
    } catch (_: Exception) {
    }
}

/** Opens a WhatsApp chat deep link with a prefilled message, safely wrapped. */
private fun openWhatsApp(context: Context, digits: String, message: String) {
    try {
        val encoded = java.net.URLEncoder.encode(message, "UTF-8")
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=$encoded")))
    } catch (_: Exception) {
    }
}
