package com.petpulse.app.ui.screens

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R
import com.petpulse.app.data.model.CommunityPost
import com.petpulse.app.ui.components.ReportDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Wagmiya brand colors
private val CoralPrimary = Color(0xFF6A4C93)
private val CoralLight = Color(0xFFEDE3F8)
private val CreamBg = Color(0xFFFBF6F0)
private val TealAccent = Color(0xFF1D7A6E)
private val DarkText = Color(0xFF241F2B)
private val SosRed = Color(0xFFD62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCommunityScreen(
    posts: List<CommunityPost>,
    currentUid: String?,
    onAddPost: (message: String, petName: String, photoUri: Uri?) -> Unit,
    onDeletePost: (CommunityPost) -> Unit,
    onReportPost: (String, String) -> Unit = { _, _ -> },
    onOpenLostPetAlerts: () -> Unit = {}
) {
    var showNewPostDialog by remember { mutableStateOf(false) }
    // Post awaiting a report (state is hoisted above the LazyColumn, never inside
    // a lazy content lambda).
    var reportingPost by remember { mutableStateOf<CommunityPost?>(null) }

    MaterialTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.community_pet_community),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewPostDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Post")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    // Live lost-pet SOS alerts feed entry
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenLostPetAlerts() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SosRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("LOST PET ALERTS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Lost pets near you — help owners find them (live)", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            }
                            Text("VIEW >", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                if (posts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                Text("No community posts yet", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "Be the first to share a story about your pet! Tap the + button below.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            isOwnPost = post.ownerId == currentUid,
                            onDelete = { onDeletePost(post) },
                            onReport = { reportingPost = post }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showNewPostDialog) {
        NewPostDialog(
            onDismiss = { showNewPostDialog = false },
            onPost = { message, petName, photoUri ->
                onAddPost(message, petName, photoUri)
                showNewPostDialog = false
            }
        )
    }

    reportingPost?.let { target ->
        ReportDialog(
            onDismiss = { reportingPost = null },
            onSubmit = { reason ->
                onReportPost(target.id, reason)
                reportingPost = null
            }
        )
    }
}

@Composable
private fun PostCard(
    post: CommunityPost,
    isOwnPost: Boolean,
    onDelete: () -> Unit,
    onReport: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (post.petName.isNotBlank()) {
                        Text(
                            text = "with ${post.petName}",
                            color = TealAccent,
                            fontSize = 13.sp
                        )
                    }
                }
                Text(
                    text = timeAgoText(post.createdAt),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                if (isOwnPost) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete my post",
                            tint = SosRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    TextButton(onClick = onReport) {
                        Text(
                            stringResource(R.string.report_action),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.message,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            val photoBytes = remember(post.photoData) { decodeCommunityPhoto(post.photoData) }
            // Prefer the hosted Cloudinary URL; fall back to the inline base64 bytes.
            val photoModel: Any? = if (post.photoUrl.isNotBlank()) post.photoUrl else photoBytes
            if (photoModel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = photoModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

/** Decode a stored base64 JPEG into bytes Coil can render (or null). */
private fun decodeCommunityPhoto(photoData: String): ByteArray? {
    if (photoData.isBlank()) return null
    return try {
        Base64.decode(photoData, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

/** "2 hours ago" style label from a timestamp. */
private fun timeAgoText(createdAt: Long): String {
    if (createdAt <= 0L) return "Just now"
    val mins = (System.currentTimeMillis() - createdAt) / 60_000L
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "$mins min ago"
        mins < 60 * 24 -> "${mins / 60} hours ago"
        mins < 60 * 24 * 7 -> "${mins / (60 * 24)} days ago"
        else -> "${mins / (60 * 24 * 7)} weeks ago"
    }
}

@Composable
private fun NewPostDialog(
    onDismiss: () -> Unit,
    onPost: (message: String, petName: String, photoUri: Uri?) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> photoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.community_new_post),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text(stringResource(R.string.community_pet_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.community_what_s_on_your_mind)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (photoUri == null) stringResource(R.string.community_add_photo) else stringResource(R.string.community_change_photo),
                        color = Color.White
                    )
                }
                if (photoUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = photoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        onPost(
                            message.trim(),
                            petName.trim().ifBlank { "my pet" },
                            photoUri
                        )
                    }
                },
                enabled = message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.community_post), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.community_cancel), color = TealAccent)
            }
        }
    )
}
