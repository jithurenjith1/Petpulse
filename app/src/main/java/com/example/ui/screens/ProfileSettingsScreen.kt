package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CustomerProfile
import com.example.data.model.UserPet
import com.example.data.model.VaccinationRecord
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    customer: CustomerProfile,
    pet: UserPet,
    vaccinations: List<VaccinationRecord>,
    healthScore: Int,
    onEditPetClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSavePetDirectly: (newName: String, newBreed: String, newAgeYears: Int, newGender: String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var petNameInput by remember(pet.name) { mutableStateOf(pet.name) }
    var petBreedInput by remember(pet.breed) { mutableStateOf(pet.breed) }
    var petAgeInput by remember(pet.ageYears) { mutableStateOf(pet.ageYears.toString()) }
    var petGenderInput by remember(pet.gender) { mutableStateOf(pet.gender) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_settings_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Customer Profile Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimaryDark
                            )
                        }
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mobile: ${customer.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onLoginClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(if (customer.isLoggedIn) "Edit" else "Login", fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Pet Health & Care Statistics
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.QueryStats, contentDescription = null, tint = BluePrimary)
                            Text(
                                text = "${pet.name}'s Health & Care Statistics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimaryDark
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "EXCELLENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Divider()

                    // Stat Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 1: Overall Health Score
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0F7FF),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Health Index", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$healthScore%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimary)
                                Text("Vitals Optimal", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Stat 2: Vaccinations
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F8E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Vaccinations", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${vaccinations.count { it.status == "Completed" }}/${vaccinations.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
                                Text("Up to date", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Stat 3: Monthly Expenses
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF8E1),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Est. Monthly", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$68", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                                Text("Food & Care", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Activity and microchip metrics
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF9FBFE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Weekly Exercise & Walks:", fontSize = 11.sp)
                                Text("14 Walks Completed (18.2 km)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Microchip Tag Status:", fontSize = 11.sp)
                                Text("Active (${pet.microchipNumber})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                            }
                        }
                    }
                }
            }
        }

        // 3. Settings Menu: Rename Jane & Configure Default Pet
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("settings_rename_jane_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = BluePrimary)
                            Text(
                                text = "Settings: Default Pet Configuration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimaryDark
                            )
                        }
                    }

                    Text(
                        text = "Customize the default inbuilt pet name (Jane) and details for this account:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = petNameInput,
                        onValueChange = { petNameInput = it },
                        label = { Text("Pet Name (Default: Jane)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_pet_name_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = petBreedInput,
                            onValueChange = { petBreedInput = it },
                            label = { Text("Breed (Default: Indie)") },
                            modifier = Modifier.weight(1f).testTag("settings_breed_field")
                        )
                        OutlinedTextField(
                            value = petGenderInput,
                            onValueChange = { petGenderInput = it },
                            label = { Text("Gender (Female)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = petAgeInput,
                            onValueChange = { petAgeInput = it },
                            label = { Text("Age (Years)") },
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(onClick = onEditPetClick) {
                            Text("Full Edit Form", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val years = petAgeInput.toIntOrNull() ?: pet.ageYears
                                onSavePetDirectly(petNameInput, petBreedInput, years, petGenderInput)
                                onShowMessage("Pet renamed to '$petNameInput' and settings saved!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.testTag("save_settings_rename_button")
                        ) {
                            Text("Save Pet Name", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. App Preferences & Safety Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Safety & Notification Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimaryDark
                    )

                    var notifyLostPets by remember { mutableStateOf(true) }
                    var notifyVaccines by remember { mutableStateOf(true) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("5km Radius Lost Pet Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Receive emergency notifications when a pet is lost nearby", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyLostPets,
                            onCheckedChange = { notifyLostPets = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vaccination & Medication Due Reminders", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Automatic calendar reminders 7 days before due date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyVaccines,
                            onCheckedChange = { notifyVaccines = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                        )
                    }
                }
            }
        }
    }
}
