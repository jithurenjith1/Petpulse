package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Petpulse brand colors
private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

/**
 * A single post in the pet community feed.
 */
data class CommunityPost(
    val authorName: String,
    val petName: String,
    val message: String,
    val timestamp: String,
    val likeCount: Int,
    val liked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCommunityScreen() {
    // In-memory store seeded with 3 sample posts.
    val posts = remember {
        mutableStateListOf(
            CommunityPost(
                authorName = "Ananya Sharma",
                petName = "Mango",
                message = "Mango finally learned to fetch today! So proud of my golden boy. 🐕",
                timestamp = "2 hours ago",
                likeCount = 24
            ),
            CommunityPost(
                authorName = "Rohan Mehta",
                petName = "Whiskers",
                message = "Whiskers found the warmest spot in the house — right on top of the freshly folded laundry. Classic. 🐱",
                timestamp = "5 hours ago",
                likeCount = 41
            ),
            CommunityPost(
                authorName = "Priya Nair",
                petName = "Biscuit",
                message = "Morning walk with Biscuit by the lake. The sunrise was as golden as his fur. 🌅",
                timestamp = "Yesterday",
                likeCount = 87
            )
        )
    }

    var showNewPostDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            containerColor = CreamBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Pet Community",
                            color = DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CreamBg
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewPostDialog = true },
                    containerColor = CoralPrimary,
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
                items(posts) { post ->
                    PostCard(
                        post = post,
                        onLikeToggle = { current ->
                            val index = posts.indexOf(current)
                            if (index >= 0) {
                                val updated = current.copy(
                                    liked = !current.liked,
                                    likeCount = if (current.liked) current.likeCount - 1
                                    else current.likeCount + 1
                                )
                                posts[index] = updated
                            }
                        }
                    )
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
            onPost = { message, petName ->
                posts.add(
                    0,
                    CommunityPost(
                        authorName = "You",
                        petName = petName,
                        message = message,
                        timestamp = "Just now",
                        likeCount = 0
                    )
                )
                showNewPostDialog = false
            }
        )
    }
}

@Composable
private fun PostCard(
    post: CommunityPost,
    onLikeToggle: (CommunityPost) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        .background(CoralLight),
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
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "with ${post.petName}",
                        color = TealAccent,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = post.timestamp,
                    color = DarkText.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.message,
                color = DarkText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onLikeToggle(post) }) {
                    Icon(
                        imageVector = if (post.liked) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.liked) CoralPrimary else DarkText.copy(alpha = 0.4f)
                    )
                }
                Text(
                    text = "${post.likeCount}",
                    color = if (post.liked) CoralPrimary else DarkText.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun NewPostDialog(
    onDismiss: () -> Unit,
    onPost: (message: String, petName: String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Post",
                color = DarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("What's on your mind?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        onPost(
                            message.trim(),
                            petName.trim().ifBlank { "my pet" }
                        )
                    }
                },
                enabled = message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Post", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TealAccent)
            }
        }
    )
}

