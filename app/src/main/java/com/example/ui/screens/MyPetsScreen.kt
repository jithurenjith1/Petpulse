package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.model.CustomerProfile
import com.example.data.model.MedicalReport
import com.example.data.model.UserPet
import com.example.data.model.VaccinationRecord
import com.example.ui.theme.*

enum class PetDetailSubmenu {
    CERTIFICATE,
    VACCINATION_MEDICAL,
    FOOD_AND_PLAYS,
    TRAINING,
    HEALTH_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPetsScreen(
    pet: UserPet,
    customer: CustomerProfile,
    vaccinations: List<VaccinationRecord>,
    medicalReports: List<MedicalReport>,
    healthScore: Int,
    onEditPetClick: () -> Unit,
    onToggleVaccine: (VaccinationRecord) -> Unit,
    onAddVaccine: (name: String, date: String, nextDue: String, status: String, doctor: String) -> Unit,
    onAddMedicalReport: (title: String, clinic: String, diagnosis: String, prescription: String) -> Unit,
    onUpdateFoodPlays: (foods: String, plays: String) -> Unit,
    onLoginClick: () -> Unit,
    onSavePetDirectly: (newName: String, newBreed: String, newAgeYears: Int, newGender: String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubmenu by remember { mutableStateOf(PetDetailSubmenu.CERTIFICATE) }
    var showAddVaccineDialog by remember { mutableStateOf(false) }
    var showAddMedicalDialog by remember { mutableStateOf(false) }
    var showAddPreferenceDialog by remember { mutableStateOf(false) }
    var showCertificateUploadToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("my_pets_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Customer Welcome Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("customer_welcome_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BluePrimaryDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Welcome, ${customer.name}!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Member",
                                tint = AccentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This dedicated dashboard manages all care records & schedules for your pets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD1E4FF)
                        )
                    }

                    IconButton(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Switch Customer",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Jane's Hero Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("jane_pet_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pet Avatar Picture — Tap to upload real photo
                        val petPhotoUri = remember { mutableStateOf<Uri?>(null) }
                        val photoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            petPhotoUri.value = uri
                        }
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(2.dp, BluePrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                .clickable { photoPickerLauncher.launch("image/*") }
                        ) {
                            if (petPhotoUri.value != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(petPhotoUri.value),
                                    contentDescription = "${pet.name} Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_dog_jane),
                                    contentDescription = "${pet.name} Photo Tap to upload",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Pet Vital Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BluePrimaryDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BluePrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${pet.gender} • ${pet.species}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Breed: ${pet.breed} • Age: ${pet.ageYears} Yrs ${pet.ageMonths} Mos",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Weight: ${pet.weightKg} kg • Microchip: ${pet.microchipNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rename / Edit Jane Quick Action Button
                            FilledTonalButton(
                                onClick = onEditPetClick,
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("rename_jane_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit / Rename Pet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Pet Submenu Navigation Chips (Certificate, Vaccination, Food & Plays, Training)
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSubmenu.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("pet_submenu_tabs")
            ) {
                PetDetailSubmenu.values().forEach { submenu ->
                    val isSelected = selectedSubmenu == submenu
                    val title = when (submenu) {
                        PetDetailSubmenu.CERTIFICATE -> "📜 1. Certificate"
                        PetDetailSubmenu.VACCINATION_MEDICAL -> "💉 2. Medical & Vaccines"
                        PetDetailSubmenu.FOOD_AND_PLAYS -> "🍖 3. Food & Plays"
                        PetDetailSubmenu.TRAINING -> "🎓 4. Training"
                        PetDetailSubmenu.HEALTH_SETTINGS -> "⚖️ 5. Health & Settings"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSubmenu = submenu },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("tab_${submenu.name.lowercase()}"),
                        text = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // 4. Submenu Content Panels
        when (selectedSubmenu) {
            PetDetailSubmenu.CERTIFICATE -> {
                item {
                    CertificateSubmenuSection(
                        pet = pet,
                        onUploadClick = { showCertificateUploadToast = true }
                    )
                }
            }
            PetDetailSubmenu.VACCINATION_MEDICAL -> {
                item {
                    VaccinationMedicalSubmenuSection(
                        vaccinations = vaccinations,
                        medicalReports = medicalReports,
                        onToggleVaccine = onToggleVaccine,
                        onAddVaccineClick = { showAddVaccineDialog = true },
                        onAddMedicalClick = { showAddMedicalDialog = true }
                    )
                }
            }
            PetDetailSubmenu.FOOD_AND_PLAYS -> {
                item {
                    FoodAndPlaysSubmenuSection(
                        pet = pet,
                        onAddPreferenceClick = { showAddPreferenceDialog = true }
                    )
                }
            }
            PetDetailSubmenu.TRAINING -> {
                item {
                    TrainingSubmenuSection(
                        pet = pet,
                        onEditTraining = onEditPetClick
                    )
                }
            }
            PetDetailSubmenu.HEALTH_SETTINGS -> {
                item {
                    HealthAndSettingsSection(
                        pet = pet,
                        customer = customer,
                        vaccinations = vaccinations,
                        healthScore = healthScore,
                        onEditPetClick = onEditPetClick,
                        onSavePetDirectly = onSavePetDirectly,
                        onShowMessage = onShowMessage
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddVaccineDialog) {
        AddVaccinationRecordDialog(
            onDismiss = { showAddVaccineDialog = false },
            onSave = { name, date, nextDue, status, doc ->
                onAddVaccine(name, date, nextDue, status, doc)
                showAddVaccineDialog = false
            }
        )
    }

    if (showAddMedicalDialog) {
        AddMedicalReportDialog(
            onDismiss = { showAddMedicalDialog = false },
            onSave = { title, clinic, diag, presc ->
                onAddMedicalReport(title, clinic, diag, presc)
                showAddMedicalDialog = false
            }
        )
    }

    if (showAddPreferenceDialog) {
        AddPreferenceItemDialog(
            currentFoods = pet.favoriteFoods,
            currentPlays = pet.favoritePlays,
            onDismiss = { showAddPreferenceDialog = false },
            onSave = { foods, plays ->
                onUpdateFoodPlays(foods, plays)
                showAddPreferenceDialog = false
            }
        )
    }

    if (showCertificateUploadToast) {
        AlertDialog(
            onDismissRequest = { showCertificateUploadToast = false },
            confirmButton = {
                TextButton(onClick = { showCertificateUploadToast = false }) {
                    Text("OK")
                }
            },
            title = { Text("Upload Pet Certificate") },
            text = { Text("Certificate document / photo uploaded successfully! Encrypted and verified by Kennel Registry Council.") },
            icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = BluePrimary) }
        )
    }
}

// ---------------- Submenu 1: Certificate ----------------
@Composable
fun CertificateSubmenuSection(
    pet: UserPet,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("certificate_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Certificate",
                        tint = AccentAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Official Canine Health & Birth Certificate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimaryDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (pet.hasCertificate) "VERIFIED" else "PENDING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Divider()

            // Certificate Details Frame
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9FBFE),
                border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Certificate Reg ID:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.certificateNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimaryDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registered Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${pet.name} (${pet.breed})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Issuing Authority:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.certificateIssuedBy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Issue Date:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.certificateDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Upload or Re-upload Certificate Button
            OutlinedButton(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_certificate_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Document / Photo of Certificate")
            }
        }
    }
}

// ---------------- Submenu 2: Vaccination & Medical ----------------
@Composable
fun VaccinationMedicalSubmenuSection(
    vaccinations: List<VaccinationRecord>,
    medicalReports: List<MedicalReport>,
    onToggleVaccine: (VaccinationRecord) -> Unit,
    onAddVaccineClick: () -> Unit,
    onAddMedicalClick: () -> Unit
) {
    var vaxTab by remember { mutableStateOf(0) } // 0: Completed, 1: Upcoming, 2: Medical Reports

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Segmented filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = vaxTab == 0,
                onClick = { vaxTab = 0 },
                label = { Text("Completed (${vaccinations.count { it.status == "Completed" }})", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = vaxTab == 1,
                onClick = { vaxTab = 1 },
                label = { Text("Upcoming (${vaccinations.count { it.status == "Upcoming" }})", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = vaxTab == 2,
                onClick = { vaxTab = 2 },
                label = { Text("Reports (${medicalReports.size})", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        if (vaxTab == 0 || vaxTab == 1) {
            val targetStatus = if (vaxTab == 0) "Completed" else "Upcoming"
            val filteredVax = vaccinations.filter { it.status == targetStatus }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$targetStatus Vaccinations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        TextButton(onClick = onAddVaccineClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Add Vaccine", fontSize = 12.sp)
                        }
                    }

                    if (filteredVax.isEmpty()) {
                        Text(
                            text = "No $targetStatus vaccinations recorded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        filteredVax.forEach { record ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF7FAFD),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleVaccine(record) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Checkbox(
                                        checked = record.status == "Completed",
                                        onCheckedChange = { onToggleVaccine(record) },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.vaccineName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Administered: ${record.dateGiven} • Due: ${record.nextDueDate}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Vet: ${record.veterinarian} (${record.batchNumber})",
                                            fontSize = 10.sp,
                                            color = BluePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Medical Reports
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Veterinary Medical Reports",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        TextButton(onClick = onAddMedicalClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Add Report", fontSize = 12.sp)
                        }
                    }

                    medicalReports.forEach { report ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF7FAFD),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(report.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BluePrimaryDark)
                                    Text(report.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Clinic: ${report.clinicName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Diagnosis: ${report.diagnosis}", fontSize = 12.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Prescription: ${report.prescription}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Submenu 3: Food & Plays ----------------
@Composable
fun FoodAndPlaysSubmenuSection(
    pet: UserPet,
    onAddPreferenceClick: () -> Unit
) {
    val foodsList = remember(pet.favoriteFoods) {
        pet.favoriteFoods.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val playsList = remember(pet.favoritePlays) {
        pet.favoritePlays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("food_and_plays_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Food & Plays ${pet.name} Likes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimaryDark
                )

                TextButton(onClick = onAddPreferenceClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Likes", fontSize = 12.sp)
                }
            }

            // Food Likes Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🍲 Favorite Meals & Treats", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    foodsList.forEach { food ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = "• $food",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Divider()

            // Plays & Toys Likes Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🎾 Favorite Toys & Play Activities", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    playsList.forEach { play ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE1F5FE)
                        ) {
                            Text(
                                text = "★ $play",
                                fontSize = 12.sp,
                                color = Color(0xFF0277BD),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Submenu 4: Training ----------------
@Composable
fun TrainingSubmenuSection(
    pet: UserPet,
    onEditTraining: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("training_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Training Level & Milestones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimaryDark
                    )
                    Text(
                        text = "Level: ${if (pet.trainingLevel.isNotBlank()) pet.trainingLevel else "Not Specified"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FilledTonalButton(onClick = onEditTraining, shape = RoundedCornerShape(10.dp)) {
                    Text("Update Status", fontSize = 12.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF4F9FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = pet.trainingStatus,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BluePrimaryDark
                    )
                    Text(
                        text = "${pet.name} responds to voice commands and hand markers. Certified gentle companion obedience.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val commandList = listOf(
                        "Sit" to true,
                        "Stay (30s)" to true,
                        "Paw / High Five" to true,
                        "Heel Walk" to true,
                        "Emergency Recall" to true,
                        "Agility Weave" to (pet.trainingLevel == "Advanced")
                    )

                    commandList.forEach { (cmd, isMastered) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "• $cmd", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isMastered) AccentGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = if (isMastered) "MASTERED" else "IN PROGRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMastered) AccentGreen else Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog for adding vaccine
@Composable
fun AddVaccinationRecordDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, date: String, nextDue: String, status: String, doctor: String) -> Unit
) {
    var vaccineName by remember { mutableStateOf("Rabies Booster") }
    var dateGiven by remember { mutableStateOf("Aug 25, 2026") }
    var nextDueDate by remember { mutableStateOf("Aug 25, 2027") }
    var status by remember { mutableStateOf("Completed") }
    var doctor by remember { mutableStateOf("Dr. Sarah Adams") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vaccination Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateGiven,
                    onValueChange = { dateGiven = it },
                    label = { Text("Date Administered") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Next Due Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Veterinarian / Clinic") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "Completed",
                        onClick = { status = "Completed" },
                        label = { Text("Completed") }
                    )
                    FilterChip(
                        selected = status == "Upcoming",
                        onClick = { status = "Upcoming" },
                        label = { Text("Upcoming") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(vaccineName, dateGiven, nextDueDate, status, doctor) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog for adding medical report
@Composable
fun AddMedicalReportDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, clinic: String, diagnosis: String, prescription: String) -> Unit
) {
    var title by remember { mutableStateOf("Wellness Examination") }
    var clinic by remember { mutableStateOf("Metropolitan Pet Hospital") }
    var diagnosis by remember { mutableStateOf("Healthy coat, vitals normal.") }
    var prescription by remember { mutableStateOf("Multivitamins & Omega-3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Report") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Report Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Clinic Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosis / Findings") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text("Prescription & Care Advice") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, clinic, diagnosis, prescription) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog for editing food and plays
@Composable
fun AddPreferenceItemDialog(
    currentFoods: String,
    currentPlays: String,
    onDismiss: () -> Unit,
    onSave: (foods: String, plays: String) -> Unit
) {
    var foods by remember { mutableStateOf(currentFoods) }
    var plays by remember { mutableStateOf(currentPlays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Food & Plays Jane Likes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = foods,
                    onValueChange = { foods = it },
                    label = { Text("Favorite Food (Comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plays,
                    onValueChange = { plays = it },
                    label = { Text("Favorite Plays & Toys (Comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(foods, plays) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---------------- Submenu 5: Health & Settings (moved from Profile) ----------------
@Composable
fun HealthAndSettingsSection(
    pet: UserPet,
    customer: CustomerProfile,
    vaccinations: List<VaccinationRecord>,
    healthScore: Int,
    onEditPetClick: () -> Unit,
    onSavePetDirectly: (newName: String, newBreed: String, newAgeYears: Int, newGender: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var petNameInput by remember(pet.name) { mutableStateOf(pet.name) }
    var petBreedInput by remember(pet.breed) { mutableStateOf(pet.breed) }
    var petAgeInput by remember(pet.ageYears) { mutableStateOf(pet.ageYears.toString()) }
    var petGenderInput by remember(pet.gender) { mutableStateOf(pet.gender) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
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
                    Text(text = "${pet.name}'s Health & Care Statistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BluePrimaryDark)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = AccentGreen.copy(alpha = 0.15f)) {
                    Text(text = "EXCELLENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0F7FF), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Health Index", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$healthScore%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimary)
                        Text("Vitals Optimal", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F8E9), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vaccinations", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${vaccinations.count { it.status == "Completed" }}/${vaccinations.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
                        Text("Up to date", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF8E1), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Est. Monthly", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$68", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                        Text("Food & Care", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF9FBFE), modifier = Modifier.fillMaxWidth()) {
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

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = BluePrimary)
                Text(text = "Settings: Default Pet Configuration", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BluePrimaryDark)
            }
            Text(text = "Customize the pet name and details for this account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = petNameInput, onValueChange = { petNameInput = it }, label = { Text("Pet Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = petBreedInput, onValueChange = { petBreedInput = it }, label = { Text("Breed") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = petGenderInput, onValueChange = { petGenderInput = it }, label = { Text("Gender") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = petAgeInput, onValueChange = { petAgeInput = it }, label = { Text("Age") }, modifier = Modifier.weight(0.8f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(onClick = onEditPetClick) { Text("Full Edit Form", fontSize = 12.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val years = petAgeInput.toIntOrNull() ?: pet.ageYears
                        onSavePetDirectly(petNameInput, petBreedInput, years, petGenderInput)
                        onShowMessage("Pet renamed to '$petNameInput' and settings saved!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) { Text("Save Pet Name", fontSize = 12.sp) }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Safety & Notification Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BluePrimaryDark)
            var notifyLostPets by remember { mutableStateOf(true) }
            var notifyVaccines by remember { mutableStateOf(true) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("5km Radius Lost Pet Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Receive emergency notifications when a pet is lost nearby", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notifyLostPets, onCheckedChange = { notifyLostPets = it }, colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary))
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vaccination & Medication Due Reminders", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Automatic calendar reminders 7 days before due date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notifyVaccines, onCheckedChange = { notifyVaccines = it }, colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary))
            }
        }
    }
}
