package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPet

private val CoralPrimary = Color(0xFFE07856)
private val CoralLight = Color(0xFFF4A88C)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

@Composable
fun PetSwitcher(
    pets: List<UserPet>,
    activePetId: Long,
    onPetSelected: (Long) -> Unit,
    onAddPetClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pets) { pet ->
            PetChip(
                pet = pet,
                isSelected = pet.id == activePetId,
                onClick = { onPetSelected(pet.id) }
            )
        }
        item {
            AddPetChip(onClick = onAddPetClick)
        }
    }
}

@Composable
private fun PetChip(pet: UserPet, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) CoralPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CoralPrimary else Color(0xFFE0D5CC)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(0.3f) else CreamBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else CoralPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = pet.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else DarkText
            )
        }
    }
}

@Composable
private fun AddPetChip(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0D5CC))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Pet",
                tint = TealAccent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Add Pet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TealAccent
            )
        }
    }
}

@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAddPet: (name: String, species: String, breed: String, gender: String, ageYears: Int, ageMonths: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var ageYears by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }

    val speciesOptions = listOf("Dog", "Cat", "Bird", "Fish", "Rabbit", "Hamster", "Exotic")
    val genderOptions = listOf("Male", "Female")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add New Pet", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkText)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Pet Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary, unfocusedBorderColor = Color(0xFFE0D5CC)),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Species", fontSize = 13.sp, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(speciesOptions) { s ->
                        FilterChip(
                            selected = species == s,
                            onClick = { species = s },
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = breed, onValueChange = { breed = it },
                    label = { Text("Breed") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary, unfocusedBorderColor = Color(0xFFE0D5CC)),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gender", fontSize = 13.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    genderOptions.forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageYears, onValueChange = { ageYears = it },
                        label = { Text("Years") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary, unfocusedBorderColor = Color(0xFFE0D5CC)),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMonths, onValueChange = { ageMonths = it },
                        label = { Text("Months") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralPrimary, unfocusedBorderColor = Color(0xFFE0D5CC)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddPet(
                            name.trim(),
                            species,
                            breed.trim(),
                            gender,
                            ageYears.toIntOrNull() ?: 0,
                            ageMonths.toIntOrNull() ?: 0
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Add Pet", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

