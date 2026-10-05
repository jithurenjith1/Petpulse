package com.petpulse.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.content
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

// ---------------------------------------------------------------------------
// Premium-only "AI Pet Photo Analysis".
//
// A pet photo is sent to Gemini through Firebase AI Logic (the `firebase-ai`
// dependency, already declared in app/build.gradle.kts). The model returns a
// short breed + health observation report which the owner can save onto the
// pet profile. Auth is handled entirely by Firebase AI Logic — there is no API
// key anywhere in this file (never hardcode one).
// ---------------------------------------------------------------------------

// ---- Theme colors (kept consistent with the Cream & Purple brand) ----
private val Purple = Color(0xFF6A4C93)
private val PurpleDark = Color(0xFF4E3570)
private val Gold = Color(0xFFA87A1F)
private val Cream = Color(0xFFFBF6F0)
private val DarkBackground = Color(0xFF1C1712)
private val DarkSurface = Color(0xFF211B2B)
private val DarkSurfaceVariant = Color(0xFF302838)
private val OnDark = Color(0xFFF2EEF7)
private val OnDarkMuted = Color(0xFFAFA8BC)

/** Exact analysis prompt requested by the product owner. */
private const val AI_PHOTO_PROMPT =
    "You are a veterinary assistant for a pet app in Kerala, India. " +
        "Look at this pet photo and reply in 4 short lines: " +
        "1) likely breed (or 'Indian Indie / mixed'), " +
        "2) approximate age range, " +
        "3) 2-3 visible health or coat observations, " +
        "4) one clear next step. " +
        "Be careful and never claim a diagnosis."

private const val WHATSAPP_NUMBER = "919526632311"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPetPhotoAnalysisScreen(
    isPremium: Boolean,
    petName: String = "",
    onClose: () -> Unit = {},
    onSaveResult: (breed: String, notes: String) -> Unit = { _, _ -> }
) {
    // Direct check: read carePlan straight from Firestore so the flag is found
    // wherever the owner stored it (admins/{email}, users/{email}, users/{uid}).
    var directPlan by remember { mutableStateOf<String?>(null) }
    var diag by remember { mutableStateOf("checking...") }
    LaunchedEffect(Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: ""
        val uid = auth.currentUser?.uid ?: ""
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val paths = listOf("admins/" + email, "users/" + email, "users/" + uid)
        val log = StringBuilder("email=" + email + " uid=" + uid.take(8))
        for (p in paths) {
            val parts = p.split("/")
            if (parts.size != 2 || parts[1].isBlank()) continue
            try {
                val snap = db.collection(parts[0]).document(parts[1]).get().await()
                val plan = snap.getString("carePlan") ?: ""
                if (plan.isNotBlank()) {
                    directPlan = plan
                    log.append(" | ").append(p).append("='").append(plan).append("'")
                    diag = log.toString()
                    return@LaunchedEffect
                }
                log.append(" | ").append(p).append("=none")
            } catch (e: Exception) {
                log.append(" | ").append(p).append(" ERR:").append(e.message?.take(30))
            }
        }
        diag = log.toString()
    }
    val premium = isPremium || (directPlan?.trim()?.equals("premium", ignoreCase = true) == true)

    if (!premium) {
        PremiumLockScreen(petName = petName, onClose = onClose, diag = diag)
    } else {
        AiPhotoAnalysisContent(petName = petName, onClose = onClose, onSaveResult = onSaveResult)
    }
}

// ---------------------------------------------------------------------------
// Lock screen shown to free users.
// ---------------------------------------------------------------------------
@Composable
private fun PremiumLockScreen(petName: String, onClose: () -> Unit, diag: String = "") {
    val context = LocalContext.current

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Premium feature",
                color = OnDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "AI pet photo analysis is part of Wagmiya Care Plus (Rs499/month). " +
                    "Upgrade to unlock it.",
                color = OnDarkMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "DEBUG: " + diag,
                color = OnDarkMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val enquiry = "Hi Wagmiya! I would like to upgrade to Wagmiya Care Plus " +
                        "(Rs499/month) to unlock AI Pet Photo Analysis" +
                        (if (petName.isNotBlank()) " for my pet $petName" else "") + "."
                    runCatching {
                        val encoded = URLEncoder.encode(enquiry, "UTF-8")
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=$encoded")
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.White)
            ) {
                Text(text = "Ask on WhatsApp", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Premium experience: pick a photo, analyse it, review + save the result.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiPhotoAnalysisContent(
    petName: String,
    onClose: () -> Unit,
    onSaveResult: (breed: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalysing by remember { mutableStateOf(false) }
    var aiText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            aiText = ""
            errorText = ""
            saved = false
        }
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
            Text(
                text = "AI Pet Photo Analysis",
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (petName.isNotBlank()) "For $petName" else "Premium Care Plus",
                color = OnDarkMuted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Powered by Gemini via Firebase AI Logic. Guidance only — not a diagnosis.",
                color = OnDarkMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            // ---- Photo picker + preview ----
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = OnDark
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (photoUri == null) "Pick a pet photo" else "Change photo",
                    color = OnDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            photoUri?.let { uri ->
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected pet photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Analyse button ----
            Button(
                onClick = {
                    val uri = photoUri
                    if (uri == null) {
                        errorText = "Please pick a pet photo first."
                        return@Button
                    }
                    isAnalysing = true
                    errorText = ""
                    aiText = ""
                    saved = false
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                    ?: throw IllegalStateException("Could not read the selected photo.")
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    ?: throw IllegalStateException("Could not decode the selected photo.")
                                val imagePart = ImagePart(bitmap, "image/jpeg")
                                val model = FirebaseAI.getInstance(FirebaseApp.getInstance())
                                    .generativeModel("gemini-2.0-flash")
                                val response = model.generateContent(
                                    content {
                                        text(AI_PHOTO_PROMPT)
                                        part(imagePart)
                                    }
                                )
                                response.text?.trim().orEmpty()
                            }
                            if (result.isBlank()) {
                                errorText = "The AI did not return a result. Please try another photo."
                            } else {
                                aiText = result
                            }
                        } catch (e: Exception) {
                            errorText = "Analysis failed: " +
                                (e.localizedMessage ?: "please check your connection and try again.")
                        } finally {
                            isAnalysing = false
                        }
                    }
                },
                enabled = !isAnalysing && photoUri != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White)
            ) {
                if (isAnalysing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Analysing…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Analyse with AI", fontWeight = FontWeight.Bold)
                }
            }

            if (errorText.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorText,
                    color = Color(0xFFE57373),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ---- Result card ----
            if (aiText.isNotBlank()) {
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "AI Analysis",
                                color = OnDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(text = aiText, color = OnDark, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Disclaimer: guidance only, not a diagnosis.",
                            color = OnDarkMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val breed = aiText.lineSequence()
                                    .map { it.trim() }
                                    .firstOrNull { it.isNotBlank() }
                                    .orEmpty()
                                onSaveResult(breed, aiText)
                                saved = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Save to pet profile", fontWeight = FontWeight.Bold)
                        }
                        if (saved) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Saved to the pet profile.",
                                color = Color(0xFF7CC47F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Your photo is sent securely through Firebase AI Logic. " +
                    "No API key is stored in the app.",
                color = OnDarkMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(80.dp))
        }
    }
}
