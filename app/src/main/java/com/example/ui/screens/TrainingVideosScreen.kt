package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

enum class VideoSpecies(val label: String) {
    Dogs("Dogs"),
    Cats("Cats"),
    Birds("Birds"),
    Rabbits("Rabbits")
}

enum class Difficulty(val label: String, val color: Color) {
    Basic("Basic", TealAccent),
    Intermediate("Intermediate", CoralPrimary),
    Advanced("Advanced", Color(0xFFD62828))
}

data class TrainingVideo(
    val title: String,
    val duration: String,
    val difficulty: Difficulty,
    val species: VideoSpecies
)

private val trainingVideos: List<TrainingVideo> = listOf(
    // Dogs
    TrainingVideo("Sit, Stay, Lie Down — The Basics", "8:24", Difficulty.Basic, VideoSpecies.Dogs),
    TrainingVideo("Loose-Leash Walking Without Pulling", "12:10", Difficulty.Intermediate, VideoSpecies.Dogs),
    TrainingVideo("Teach Your Dog to Come When Called", "9:45", Difficulty.Intermediate, VideoSpecies.Dogs),
    TrainingVideo("Stop Excessive Barking", "14:30", Difficulty.Advanced, VideoSpecies.Dogs),

    // Cats
    TrainingVideo("Litter Box Training Kittens", "6:15", Difficulty.Basic, VideoSpecies.Cats),
    TrainingVideo("Clicker Training for Cats", "10:05", Difficulty.Intermediate, VideoSpecies.Cats),
    TrainingVideo("Stop Furniture Scratching", "11:20", Difficulty.Advanced, VideoSpecies.Cats),

    // Birds
    TrainingVideo("Step-Up Training for Parrots", "7:40", Difficulty.Basic, VideoSpecies.Birds),
    TrainingVideo("Teaching Your Bird to Talk", "13:55", Difficulty.Advanced, VideoSpecies.Birds),

    // Rabbits
    TrainingVideo("Litter Train Your Rabbit", "9:12", Difficulty.Basic, VideoSpecies.Rabbits),
    TrainingVideo("Clicker Tricks for Rabbits", "11:48", Difficulty.Intermediate, VideoSpecies.Rabbits)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingVideosScreen() {
    var selectedSpecies by remember { mutableStateOf<VideoSpecies?>(null) }

    val filteredVideos = remember(selectedSpecies) {
        if (selectedSpecies == null) trainingVideos
        else trainingVideos.filter { it.species == selectedSpecies }
    }

    MaterialTheme {
        Scaffold(
            containerColor = CreamBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Training Videos",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SpeciesFilterRow(
                    selected = selectedSpecies,
                    onSelect = { species ->
                        selectedSpecies =
                            if (selectedSpecies == species) null else species
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(filteredVideos) { video ->
                        VideoCard(video = video)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeciesFilterRow(
    selected: VideoSpecies?,
    onSelect: (VideoSpecies) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(VideoSpecies.entries.toList()) { species ->
            FilterChip(
                selected = selected == species,
                onClick = { onSelect(species) },
                label = { Text(species.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CoralPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun VideoCard(video: TrainingVideo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail placeholder with gradient + play button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CoralLight, TealAccent.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.species.label.dropLast(1),
                        color = TealAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Difficulty badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(video.difficulty.color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.difficulty.label,
                            color = video.difficulty.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = video.title,
                    color = DarkText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // small duration dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CoralPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.duration,
                        color = DarkText.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

