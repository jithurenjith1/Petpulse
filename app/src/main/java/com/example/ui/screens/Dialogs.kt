package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.example.data.model.CustomerProfile
import com.example.data.model.UserPet
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryDark

@Composable
fun CustomerLoginDialog(
    currentCustomer: CustomerProfile,
    onDismiss: () -> Unit,
    onLogin: (name: String, email: String, phone: String) -> Unit,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf(currentCustomer.name) }
    var email by remember { mutableStateOf(currentCustomer.email) }
    var phone by remember { mutableStateOf(currentCustomer.phone) }

    val currentLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val langSelected = when {
        currentLangTag.startsWith("ml") -> "ml"
        currentLangTag.startsWith("en") -> "en"
        else -> "system"
    }
    fun applyLang(tag: String) {
        AppCompatDelegate.setApplicationLocales(
            if (tag == "system") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("customer_login_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Customer Account",
                        tint = BluePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = if (currentCustomer.isLoggedIn) "Customer Profile" else "Customer Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        Text(
                            text = stringResource(R.string.dialogs_access_your_pet_s_records_partner_bookings),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialogs_customer_guardian_name)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_name_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.dialogs_email_address)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.dialogs_mobile_number)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input")
                )

                Divider()

                // App Language
                Text(
                    text = stringResource(R.string.settings_app_language),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimaryDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = langSelected == "system",
                        onClick = { applyLang("system") },
                        label = { Text(stringResource(R.string.lang_system_default), fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = langSelected == "en",
                        onClick = { applyLang("en") },
                        label = { Text("English", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = langSelected == "ml",
                        onClick = { applyLang("ml") },
                        label = { Text("മലയാളം", fontSize = 11.sp) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentCustomer.isLoggedIn) {
                        TextButton(
                            onClick = {
                                onLogout()
                                onDismiss()
                            }
                        ) {
                            Text(stringResource(R.string.dialogs_log_out), color = Color(0xFFD32F2F))
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialogs_cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onLogin(name, email, phone)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("login_submit_button")
                    ) {
                        Text(stringResource(R.string.dialogs_save_enter))
                    }
                }
            }
        }
    }
}

@Composable
fun EditPetProfileDialog(
    pet: UserPet,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        breed: String,
        gender: String,
        ageYears: Int,
        ageMonths: Int,
        weightKg: Double,
        favoriteFoods: String,
        favoritePlays: String,
        trainingStatus: String,
        trainingLevel: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    var breed by remember { mutableStateOf(pet.breed) }
    var gender by remember { mutableStateOf(pet.gender) }
    var ageYears by remember { mutableStateOf(pet.ageYears.toString()) }
    var ageMonths by remember { mutableStateOf(pet.ageMonths.toString()) }
    var weightKg by remember { mutableStateOf(pet.weightKg.toString()) }
    var favoriteFoods by remember { mutableStateOf(pet.favoriteFoods) }
    var favoritePlays by remember { mutableStateOf(pet.favoritePlays) }
    var trainingLevel by remember { mutableStateOf(pet.trainingLevel) }
    var trainingStatus by remember { mutableStateOf(pet.trainingStatus) }
    var notes by remember { mutableStateOf(pet.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_pet_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Pet",
                        tint = BluePrimary
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.dialogs_edit_pet_profile),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        Text(
                            text = "Rename Jane or change breed, age & preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialogs_pet_name_default_jane)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_pet_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text(stringResource(R.string.dialogs_breed_e_g_indie)) },
                        modifier = Modifier.weight(1f).testTag("edit_pet_breed")
                    )

                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text(stringResource(R.string.dialogs_gender_female_male)) },
                        modifier = Modifier.weight(1f).testTag("edit_pet_gender")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = { ageYears = it },
                        label = { Text(stringResource(R.string.dialogs_age_years)) },
                        modifier = Modifier.weight(1f).testTag("edit_pet_age_years")
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { ageMonths = it },
                        label = { Text(stringResource(R.string.dialogs_age_months)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        label = { Text(stringResource(R.string.dialogs_weight_kg)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = favoriteFoods,
                    onValueChange = { favoriteFoods = it },
                    label = { Text(stringResource(R.string.dialogs_food_pet_likes_comma_separated)) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_pet_foods")
                )

                OutlinedTextField(
                    value = favoritePlays,
                    onValueChange = { favoritePlays = it },
                    label = { Text(stringResource(R.string.dialogs_plays_toys_pet_likes)) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_pet_plays")
                )

                Text(
                    text = stringResource(R.string.dialogs_training_level_completed),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Basic", "Advanced", "In Progress", "None").forEach { lvl ->
                        FilterChip(
                            selected = trainingLevel == lvl,
                            onClick = {
                                trainingLevel = lvl
                                trainingStatus = when (lvl) {
                                    "Basic" -> "Basic Completed (Sit, Stay, Paw, Heel)"
                                    "Advanced" -> "Advanced Completed (Agility, Recall, Scent)"
                                    "In Progress" -> "Enrolled in Obedience Class"
                                    else -> "Not Started"
                                }
                            },
                            label = { Text(lvl, fontSize = 12.sp) },
                            modifier = Modifier.testTag("training_chip_$lvl")
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.dialogs_care_notes_temperament)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialogs_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val years = ageYears.toIntOrNull() ?: 1
                            val months = ageMonths.toIntOrNull() ?: 8
                            val weight = weightKg.toDoubleOrNull() ?: 14.5
                            onSave(
                                name, breed, gender, years, months, weight,
                                favoriteFoods, favoritePlays, trainingStatus, trainingLevel, notes
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("save_pet_details_button")
                    ) {
                        Text(stringResource(R.string.dialogs_save_changes))
                    }
                }
            }
        }
    }
}

@Composable
fun LostPetSosDialog(
    defaultPetName: String,
    onDismiss: () -> Unit,
    onBroadcast: (
        petName: String,
        species: String,
        breed: String,
        location: String,
        reward: String,
        phone: String,
        description: String
    ) -> Unit
) {
    var petName by remember { mutableStateOf(defaultPetName) }
    var species by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("Indie") }
    var location by remember { mutableStateOf("City Central Park, North Gate") }
    var reward by remember { mutableStateOf("₹21,250") }
    var phone by remember { mutableStateOf("+1 (800) 555-PET-SOS") }
    var description by remember { mutableStateOf("Wearing a blue collar with Jane tag. Responds to whistle and friendly.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("lost_pet_sos_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(30.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.dialogs_find_my_pet_5km_broadcast),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = "Instantly alert all app users & partner vets within 5 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text(stringResource(R.string.dialogs_lost_pet_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sos_pet_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text(stringResource(R.string.dialogs_species)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text(stringResource(R.string.dialogs_breed)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.dialogs_last_seen_location_cross_streets)) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("sos_location")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = reward,
                        onValueChange = { reward = it },
                        label = { Text(stringResource(R.string.dialogs_reward_amount)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.dialogs_emergency_contact)) },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.dialogs_description_distinguishing_features)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Push notifications and live map pins will be broadcasted to all guardians within 5km radius.",
                            fontSize = 11.sp,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialogs_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onBroadcast(petName, species, breed, location, reward, phone, description)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.testTag("broadcast_sos_submit_button")
                    ) {
                        Text(stringResource(R.string.dialogs_send_5km_broadcast))
                    }
                }
            }
        }
    }
}

@Composable
fun AddPetListingDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        petName: String,
        species: String,
        breed: String,
        age: String,
        location: String,
        listingType: String,
        price: String,
        description: String,
        phone: String
    ) -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("Indie Pup") }
    var age by remember { mutableStateOf("4 Months") }
    var location by remember { mutableStateOf("Metro Rescue Hub") }
    var listingType by remember { mutableStateOf("Adoption") }
    var price by remember { mutableStateOf("₹0") }
    var description by remember { mutableStateOf("Loving, playful, vaccinated, looking for a caring home.") }
    var phone by remember { mutableStateOf("+1 (555) 789-0123") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_pet_listing_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialogs_post_pet_for_adoption_or_sale),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimaryDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = listingType == "Adoption",
                        onClick = { listingType = "Adoption" },
                        label = { Text(stringResource(R.string.dialogs_adoption_free_rescue)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = listingType == "Sale",
                        onClick = { listingType = "Sale" },
                        label = { Text(stringResource(R.string.dialogs_pet_for_sale)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text(stringResource(R.string.dialogs_pet_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("listing_pet_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text(stringResource(R.string.dialogs_species_dog_cat_etc)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text(stringResource(R.string.dialogs_breed)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text(stringResource(R.string.dialogs_age)) },
                        modifier = Modifier.weight(1f)
                    )
                    if (listingType == "Sale") {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price ($)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.dialogs_location_with_without_login)) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.dialogs_contact_phone_helpline)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.dialogs_description_medical_status)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialogs_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (petName.isNotBlank()) {
                                onSubmit(petName, species, breed, age, location, listingType, price, description, phone)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("submit_pet_listing_btn")
                    ) {
                        Text(stringResource(R.string.dialogs_publish_listing))
                    }
                }
            }
        }
    }
}
