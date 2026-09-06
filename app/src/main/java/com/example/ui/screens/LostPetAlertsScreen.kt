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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Petpulse brand colors
private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)
private val SosRed = Color(0xFFD62828)

/**
 * A lost pet alert document mirrored from Firestore collection "lost_pet_alerts".
 * Field names match the Firestore document keys.
 */
data class LostPetAlertItem(
    val petName: String = "",
    val species: String = "",
    val breed: String = "",
    val lastSeenLocation: String = "",
    val reward: String = "",
    val contactPhone: String = "",
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
                doc.toObject<LostPetAlert>()
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
    return snapshot.documents.mapNotNull { it.toObject<LostPetAlert>() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetAlertsScreen() {
    // Collect the Firestore-backed flow. Falls back to sample data so the screen
    // is previewable without a live Firebase project.
    val flow = remember { lostPetAlertsFlow() }
    val uiState by flow.collectAsState(initial = LostPetUiState.Loading)

    MaterialTheme {
        Scaffold(
            containerColor = CreamBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Lost Pet Alerts",
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
                                text = "No lost pet alerts right now. 🐾",
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
                                    LostPetAlertCard(alert = alert)
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LostPetAlertCard(alert: LostPetAlert) {
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
                        text = "SOS",
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
            Spacer(modifier = Modifier.height(6.dp))
            AlertDetailRow(
                icon = Icons.Default.Phone,
                label = "Contact",
                value = alert.contactPhone
            )
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

