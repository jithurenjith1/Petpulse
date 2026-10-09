package com.petpulse.app.ui.components

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.petpulse.app.data.model.UserPet

private val CoralPrimary = Color(0xFF6A4C93)
private val CoralLight = Color(0xFFEDE3F8)
private val CreamBg = Color(0xFFFBF6F0)
private val TealAccent = Color(0xFF1D7A6E)
private val DarkText = Color(0xFF241F2B)

@Composable
fun PetSwitcher(
    pets: List<UserPet>,
    activePetId: Long,
    onPetSelected: (Long) -> Unit,
    onAddPetClick: () -> Unit,
    onPetLongPress: (UserPet) -> Unit = {}
) {
    var petToRemove by remember { mutableStateOf<UserPet?>(null) }

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
                onClick = { onPetSelected(pet.id) },
                onLongClick = { petToRemove = pet }
            )
        }
        item {
            AddPetChip(onClick = onAddPetClick)
        }
    }

    petToRemove?.let { target ->
        AlertDialog(
            onDismissRequest = { petToRemove = null },
            title = { Text("Remove ${target.name}?") },
            text = { Text("This will permanently delete ${target.name} and all associated records. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onPetLongPress(target)
                    petToRemove = null
                }) { Text(stringResource(R.string.mypets_delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { petToRemove = null }) { Text(stringResource(R.string.mypets_cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PetChip(pet: UserPet, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                    .background(if (isSelected) Color.White.copy(0.3f) else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = pet.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                text = stringResource(R.string.switcher_add_pet),
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.switcher_add_new_pet), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    label = { Text(stringResource(R.string.switcher_pet_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.switcher_species), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(speciesOptions) { s ->
                        FilterChip(
                            selected = species == s,
                            onClick = { species = s },
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = breed, onValueChange = { breed = it },
                    label = { Text(stringResource(R.string.switcher_breed)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.switcher_gender), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    genderOptions.forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
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
                        label = { Text(stringResource(R.string.switcher_years)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMonths, onValueChange = { ageMonths = it },
                        label = { Text(stringResource(R.string.switcher_months)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.switcher_add_pet), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

