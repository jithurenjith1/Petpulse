package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.petpulse.app.data.model.CustomerProfile
import com.petpulse.app.data.model.MedicalReport
import com.petpulse.app.data.model.PetCertificate
import com.petpulse.app.data.model.UserPet
import com.petpulse.app.data.model.VaccinationRecord
import com.petpulse.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
    onDeletePet: () -> Unit = {},
    onPhotoSelected: (String) -> Unit = {},
    certificates: List<PetCertificate> = emptyList(),
    onAddCertificate: (title: String, registrationId: String, issuedBy: String, issueDate: String, photos: List<String>) -> Unit = { _, _, _, _, _ -> },
    onDeleteCertificate: (PetCertificate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubmenu by remember { mutableStateOf(PetDetailSubmenu.CERTIFICATE) }
    var showAddVaccineDialog by remember { mutableStateOf(false) }
    var showAddMedicalDialog by remember { mutableStateOf(false) }
    var showAddPreferenceDialog by remember { mutableStateOf(false) }
    var showAddCertificateDialog by remember { mutableStateOf(false) }
    var certificatePhotoViewer by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("my_pets_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
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
                        // Pet Avatar Picture — saved per-pet to Firestore
                        val photoCtx = androidx.compose.ui.platform.LocalContext.current
                        val photoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri: Uri? ->
                            if (uri != null) {
                                try {
                                    photoCtx.contentResolver.takePersistableUriPermission(
                                        uri,
                                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                } catch (e: Exception) { }
                                onPhotoSelected(uri.toString())
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(2.dp, BluePrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                .clickable { photoPickerLauncher.launch(arrayOf("image/*")) }
                        ) {
                            if (pet.photoUri.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(pet.photoUri)),
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

                            // Edit / Rename + Delete Pet buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                Text(stringResource(R.string.mypets_edit_rename_pet), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA87A1F))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Pet",
                                    tint = Color(0xFFA87A1F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.mypets_remove), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA87A1F))
                            }
                            }
                        }
                    }
                }
            }
        }

        // 2.5 Vaccination Reminder Banner — overdue or due within 3 days
        item {
            VaccinationReminderBanner(vaccinations = vaccinations)
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
                        certificates = certificates,
                        onUploadClick = { showAddCertificateDialog = true },
                        onDeleteCertificate = onDeleteCertificate,
                        onPhotoClick = { path -> certificatePhotoViewer = path }
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
                        medicalReports = medicalReports,
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
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove ${pet.name}?") },
            text = { Text("This will permanently delete ${pet.name} and all associated records. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeletePet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD62828))
                ) { Text(stringResource(R.string.mypets_delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.mypets_cancel)) }
            }
        )
    }

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

    if (showAddCertificateDialog) {
        AddCertificateDialog(
            onDismiss = { showAddCertificateDialog = false },
            onSave = { title, regId, issuedBy, issueDate, photos ->
                onAddCertificate(title, regId, issuedBy, issueDate, photos)
                showAddCertificateDialog = false
            }
        )
    }

    certificatePhotoViewer?.let { path ->
        CertificatePhotoViewer(
            photoPath = path,
            onDismiss = { certificatePhotoViewer = null }
        )
    }
}

// ---------------- Submenu 1: Certificate ----------------
@Composable
fun CertificateSubmenuSection(
    pet: UserPet,
    certificates: List<PetCertificate>,
    onUploadClick: () -> Unit,
    onDeleteCertificate: (PetCertificate) -> Unit,
    onPhotoClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("certificate_section_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Certificate",
                    tint = AccentAmber,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Pet Certificates & Documents",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimaryDark
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (certificates.isEmpty()) "NONE" else "${certificates.size} ON FILE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        if (certificates.isEmpty()) {
            // Empty state
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        "No real certificates uploaded yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkText
                    )
                    Text(
                        "Upload your pet's real certificate photos \u2014 vaccination certificate, KC registration, adoption papers, microchip record. They are saved to your account and travel with you to any phone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Real uploaded certificates
            certificates.forEach { cert ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("certificate_card_${cert.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = cert.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BluePrimaryDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            TextButton(onClick = { onDeleteCertificate(cert) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete certificate",
                                    tint = SosRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", fontSize = 11.sp, color = SosRed)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FBFE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (cert.registrationId.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Registration ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(cert.registrationId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimaryDark)
                                    }
                                }
                                if (cert.issuedBy.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Issued By", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(cert.issuedBy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                    }
                                }
                                if (cert.issueDate.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Issue Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(cert.issueDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${pet.name} (${pet.breed})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Certificate photos — tap to view full screen
                        if (cert.photoPaths.isNotEmpty()) {
                            Text("Certificate Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(cert.photoPaths) { photoPath ->
                                    AsyncImage(
                                        model = photoPath,
                                        contentDescription = "Certificate photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(110.dp, 82.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onPhotoClick(photoPath) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload button (always available)
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
            Text(stringResource(R.string.mypets_upload_document_photo_of_certificate))
        }
    }
}

@Composable
fun AddCertificateDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, registrationId: String, issuedBy: String, issueDate: String, photos: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var registrationId by remember { mutableStateOf("") }
    var issuedBy by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris -> photoUris = uris }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Certificate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Certificate Title *") },
                    placeholder = { Text("e.g. Vaccination Certificate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = registrationId,
                    onValueChange = { registrationId = it },
                    label = { Text("Registration ID (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = issuedBy,
                    onValueChange = { issuedBy = it },
                    label = { Text("Issued By (optional)") },
                    placeholder = { Text("e.g. Kennel Club of India") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Issue Date (optional)") },
                    placeholder = { Text("e.g. 15 Jan 2025") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (photoUris.isEmpty()) "Pick Certificate Photos (up to 3) *"
                        else "${photoUris.size} photo(s) selected — tap to change",
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        registrationId.trim(),
                        issuedBy.trim(),
                        issueDate.trim(),
                        photoUris.map { it.toString() }
                    )
                },
                enabled = title.isNotBlank() && photoUris.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CertificatePhotoViewer(
    photoPath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = photoPath,
                    contentDescription = "Certificate photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
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
                            Text(stringResource(R.string.mypets_add_vaccine), fontSize = 12.sp)
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
                                        if (targetStatus == "Upcoming") {
                                            val daysText = vaccineDueDaysText(record)
                                            if (daysText != null) {
                                                val overdue = daysText.startsWith("Overdue")
                                                Text(
                                                    text = "⚠ $daysText",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (overdue) Color(0xFFD62828) else Color(0xFFA87A1F)
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
                            text = stringResource(R.string.mypets_veterinary_medical_reports),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimaryDark
                        )
                        TextButton(onClick = onAddMedicalClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.mypets_add_report), fontSize = 12.sp)
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
                    Text(stringResource(R.string.mypets_edit_likes), fontSize = 12.sp)
                }
            }

            // Food Likes Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.mypets_favorite_meals_treats), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    Text(stringResource(R.string.mypets_favorite_toys_play_activities), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        text = stringResource(R.string.mypets_training_level_milestones),
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
                    Text(stringResource(R.string.mypets_update_status), fontSize = 12.sp)
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
        title = { Text(stringResource(R.string.mypets_add_vaccination_record)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text(stringResource(R.string.mypets_vaccine_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateGiven,
                    onValueChange = { dateGiven = it },
                    label = { Text(stringResource(R.string.mypets_date_administered)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text(stringResource(R.string.mypets_next_due_date)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text(stringResource(R.string.mypets_veterinarian_clinic)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "Completed",
                        onClick = { status = "Completed" },
                        label = { Text(stringResource(R.string.mypets_completed)) }
                    )
                    FilterChip(
                        selected = status == "Upcoming",
                        onClick = { status = "Upcoming" },
                        label = { Text(stringResource(R.string.mypets_upcoming)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(vaccineName, dateGiven, nextDueDate, status, doctor) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.mypets_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.mypets_cancel)) }
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
        title = { Text(stringResource(R.string.mypets_add_medical_report)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.mypets_report_title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text(stringResource(R.string.mypets_clinic_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text(stringResource(R.string.mypets_diagnosis_findings)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text(stringResource(R.string.mypets_prescription_care_advice)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, clinic, diagnosis, prescription) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.mypets_save_report))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.mypets_cancel)) }
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
        title = { Text(stringResource(R.string.mypets_food_plays_jane_likes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = foods,
                    onValueChange = { foods = it },
                    label = { Text(stringResource(R.string.mypets_favorite_food_comma_separated)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plays,
                    onValueChange = { plays = it },
                    label = { Text(stringResource(R.string.mypets_favorite_plays_toys_comma_separated)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(foods, plays) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.mypets_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.mypets_cancel)) }
        }
    )
}

// ---------------- Submenu 5: Health & Settings (moved from Profile) ----------------
@Composable
fun HealthAndSettingsSection(
    pet: UserPet,
    customer: CustomerProfile,
    vaccinations: List<VaccinationRecord>,
    medicalReports: List<MedicalReport>,
    healthScore: Int,
    onEditPetClick: () -> Unit,
    onSavePetDirectly: (newName: String, newBreed: String, newAgeYears: Int, newGender: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
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
                    Text(text = stringResource(R.string.mypets_excellent), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0F7FF), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.mypets_health_index), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$healthScore%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimary)
                        Text(stringResource(R.string.mypets_vitals_optimal), fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F8E9), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.mypets_vaccinations), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${vaccinations.count { it.status == "Completed" }}/${vaccinations.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
                        Text(stringResource(R.string.mypets_up_to_date), fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF8E1), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Medical Reports", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${medicalReports.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                        Text("On file", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF9FBFE), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vaccines Completed", fontSize = 11.sp)
                        Text("${vaccinations.count { it.status == \"Completed\" }} of ${vaccinations.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.mypets_microchip_tag_status), fontSize = 11.sp)
                        Text("Active (${pet.microchipNumber})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    }
                }
            }
        }
    }



}

// ---------------- Vaccination Reminders (overdue / due soon) ----------------

/** Date formats users may have typed into the free-text Next-Due field. */
private val vaccineDateFormats = listOf(
    "MMM d, yyyy",   // Aug 25, 2027 style (dialog default)
    "MMM d yyyy",
    "d MMMM yyyy",
    "dd/MM/yyyy",
    "dd-MM-yyyy",
    "dd/MM/yy",
    "yyyy-MM-dd"
).map { DateTimeFormatter.ofPattern(it, Locale.ENGLISH) }

/** Best-effort parse of a free-text next-due date; null when unreadable. */
fun parseVaccineDate(raw: String): LocalDate? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    for (fmt in vaccineDateFormats) {
        try {
            return LocalDate.parse(trimmed, fmt)
        } catch (_: Exception) { }
    }
    return null
}

/** Days until the record's next due date (negative = overdue); null when unreadable. */
fun vaccineDueDays(record: VaccinationRecord): Long? {
    val due = parseVaccineDate(record.nextDueDate) ?: return null
    return ChronoUnit.DAYS.between(LocalDate.now(), due)
}

/** Short text like "Due in 2 days" / "Overdue by 5 days" / "Due today"; null when date unreadable. */
fun vaccineDueDaysText(record: VaccinationRecord): String? {
    val days = vaccineDueDays(record) ?: return null
    return when {
        days < 0 -> "Overdue by ${-days} day${if (days == -1L) "" else "s"}!"
        days == 0L -> "Due today!"
        else -> "Due in $days day${if (days == 1L) "" else "s"}"
    }
}

/**
 * Banner at the top of My Pets: shows the MOST urgent non-completed vaccination
 * when it is overdue or due within 3 days. Silent otherwise.
 */
@Composable
fun VaccinationReminderBanner(vaccinations: List<VaccinationRecord>) {
    val mostUrgent = vaccinations
        .filter { it.status != "Completed" }
        .mapNotNull { record ->
            vaccineDueDays(record)?.let { days -> record to days }
        }
        .filter { it.second <= 3 } // due within 3 days or overdue
        .minByOrNull { it.second }
        ?: return

    val (record, days) = mostUrgent
    val overdue = days < 0
    val bg = if (overdue) Color(0xFFF7DCD9) else Color(0xFFF6ECD8)
    val fg = if (overdue) Color(0xFFD62828) else Color(0xFFA87A1F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (overdue) {
                        "Vaccination overdue!"
                    } else {
                        "Vaccination due soon"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = fg
                )
                Text(
                    text = "${record.vaccineName} — " + when {
                        days < 0 -> "overdue by ${-days} day${if (days == -1L) "" else "s"} (${record.nextDueDate})"
                        days == 0L -> "due today! (${record.nextDueDate})"
                        else -> "due in $days day${if (days == 1L) "" else "s"} (${record.nextDueDate})"
                    },
                    fontSize = 11.sp,
                    color = fg
                )
            }
        }
    }
}



