package com.petpulse.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.petpulse.app.R
import com.petpulse.app.data.model.AdminOrder
import com.petpulse.app.data.model.RescueReport
import com.petpulse.app.data.model.PartnerApplication
import com.petpulse.app.data.model.SupportTicket
import com.petpulse.app.data.model.Dealer
import com.petpulse.app.data.model.ServiceBooking
import com.petpulse.app.data.model.ShopProduct
import com.petpulse.app.data.model.AdminLostPetAlert
import com.petpulse.app.data.model.FoundPetReport
import com.petpulse.app.data.model.MarketPet
import com.petpulse.app.data.model.VerifiedDoctor
import com.petpulse.app.data.model.PartnerCommission
import coil.compose.AsyncImage

private val adminListTypes = listOf("Food", "Medicine", "Grooming", "Accessory", "Training", "Subscription", "Boarding", "Trainer")

/**
 * Owner-only panel: Orders -> assign dealer -> mark delivered,
 * Products (add/remove own items), Dealers (the partner shops that fulfil & deliver),
 * Bookings (doctor consults + trainer requests -> assign -> confirm/complete).
 */
@Composable
fun AdminScreen(
    orders: List<AdminOrder>,
    dealers: List<Dealer>,
    products: List<ShopProduct>,
    bookings: List<ServiceBooking> = emptyList(),
    vets: List<VerifiedDoctor> = emptyList(),
    listings: List<MarketPet> = emptyList(),
    lostAlerts: List<AdminLostPetAlert> = emptyList(),
    foundReports: List<FoundPetReport> = emptyList(),
    supportTickets: List<SupportTicket> = emptyList(),
    rescueReports: List<RescueReport> = emptyList(),
    partnerApplications: List<PartnerApplication> = emptyList(),
    partnerCommissions: List<PartnerCommission> = emptyList(),
    onSetPartnerCommission: (PartnerCommission) -> Unit = {},
    onSetPartnerDuesPaid: (PartnerCommission, Boolean) -> Unit = { _, _ -> },
    onDeleteListing: (String) -> Unit = {},
    onDeleteSupportTicket: (String) -> Unit = {},
    onDeleteRescueReport: (String) -> Unit = {},
    onDeletePartnerApplication: (String) -> Unit = {},
    onDeleteLostAlert: (String) -> Unit = {},
    onDeleteFoundReport: (String) -> Unit = {},
    onAssignDealer: (String, Dealer) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onAddProduct: (String, String, String, Double, String, String, String, String, List<Uri>, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddDealer: (String, String, String) -> Unit,
    onDeleteDealer: (String) -> Unit,
    onAssignBooking: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateBookingStatus: (String, String) -> Unit = { _, _ -> },
    onAddVet: (String, String, String, String, String, Double, Double, Boolean, Boolean) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onDeleteVet: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var section by remember { mutableStateOf<String?>(null) }
    var showBookings by remember { mutableStateOf(false) }
    var assigningOrder by remember { mutableStateOf<AdminOrder?>(null) }
    var assigningBooking by remember { mutableStateOf<ServiceBooking?>(null) }
    var editingPartner by remember { mutableStateOf<PartnerLedgerRow?>(null) }

    // Partner Ledger rows are derived from the existing dealers / vets / bookings
    // data merged with the admin's saved commission settings.
    val partnerRows = remember(dealers, vets, orders, bookings, partnerCommissions) {
        buildPartnerLedgerRows(dealers, vets, orders, bookings, partnerCommissions)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.92f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.admin_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) { Text("X", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(8.dp))
                if (section == null) {
                    val pendingCounts = mapOf(
                        "ORDERS" to orders.count { it.status == "NEW" || it.status == "ASSIGNED" },
                        "DOCTOR" to bookings.count { it.type == "DOCTOR" && (it.status == "NEW" || it.status == "CONFIRMED") },
                        "GROOMING" to bookings.count { it.type == "GROOMING" && (it.status == "NEW" || it.status == "CONFIRMED") },
                        "TRAINING" to bookings.count { it.type == "TRAINER" && (it.status == "NEW" || it.status == "CONFIRMED") },
                        "BOARDING" to bookings.count { it.type == "BOARDING" && (it.status == "NEW" || it.status == "CONFIRMED") },
                        "SUBSCRIPTION" to bookings.count { it.type == "SUBSCRIPTION" && (it.status == "NEW" || it.status == "CONFIRMED") },
                        "ALERTS" to lostAlerts.size,
                        "FOUND" to foundReports.size,
                        "SUPPORT" to (supportTickets.size + rescueReports.size),
                        "PARTNERS" to partnerApplications.size,
                        "LISTINGS" to listings.size
                    )
                    // Totals for the catalogue / partner cards the admin also manages.
                    // These are plain collection sizes (not pending work), so they
                    // badge their card but are NOT added to the "items pending" total.
                    val catalogueCounts = mapOf(
                        "FOOD" to products.count { it.listType == "Food" },
                        "MEDICINE" to products.count { it.listType == "Medicine" },
                        "ACCESSORIES" to products.count { it.listType == "Accessory" },
                        "TRAINERS" to products.count { it.listType == "Trainer" },
                        "DEALERS" to dealers.size,
                        "LEDGER" to partnerRows.size
                    )
                    AdminDashboard(
                        pendingCounts = pendingCounts + catalogueCounts,
                        pendingTotal = pendingCounts.values.sum(),
                        onSelect = { section = it; showBookings = false }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { section = null; showBookings = false }) {
                            Text("<", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (section == "LEDGER") stringResource(R.string.admin_ledger_section) else sectionTitle(section),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    when (section) {
                        "GROOMING", "DOCTOR", "TRAINING" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !showBookings,
                                    onClick = { showBookings = false },
                                    label = { Text(if (section == "DOCTOR") "Vets" else if (section == "GROOMING") "Services" else "Programs", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showBookings,
                                    onClick = { showBookings = true },
                                    label = { Text("Bookings", fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (showBookings) {
                                BookingsAdminTab(
                                    bookings = bookings,
                                    onAssign = { booking -> assigningBooking = booking },
                                    onUpdateStatus = onUpdateBookingStatus,
                                    typeFilter = bookingTypeFor(section)
                                )
                            } else {
                                when (section) {
                                    "DOCTOR" -> VetsAdminTab(vets = vets, onAdd = onAddVet, onDelete = onDeleteVet)
                                    "TRAINING" -> ProductsAdminTab(products = products, listTypeFilter = "Training", onAdd = onAddProduct, onDelete = onDeleteProduct)
                                    else -> ProductsAdminTab(products = products, listTypeFilter = "Grooming", onAdd = onAddProduct, onDelete = onDeleteProduct)
                                }
                            }
                        }
                        "ORDERS" -> OrdersAdminTab(
                            orders = orders,
                            onAssign = { order -> assigningOrder = order },
                            onUpdateStatus = onUpdateStatus
                        )
                        "FOOD" -> ProductsAdminTab(products = products, listTypeFilter = "Food", onAdd = onAddProduct, onDelete = onDeleteProduct)
                        "MEDICINE" -> ProductsAdminTab(products = products, listTypeFilter = "Medicine", onAdd = onAddProduct, onDelete = onDeleteProduct)
                        "ACCESSORIES" -> ProductsAdminTab(products = products, listTypeFilter = "Accessory", onAdd = onAddProduct, onDelete = onDeleteProduct)
                        "SUBSCRIPTION" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !showBookings,
                                    onClick = { showBookings = false },
                                    label = { Text("Plans", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showBookings,
                                    onClick = { showBookings = true },
                                    label = { Text("Subscriptions", fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (showBookings) {
                                BookingsAdminTab(
                                    bookings = bookings,
                                    onAssign = { booking -> assigningBooking = booking },
                                    onUpdateStatus = onUpdateBookingStatus,
                                    typeFilter = "SUBSCRIPTION"
                                )
                            } else {
                                ProductsAdminTab(products = products, listTypeFilter = "Subscription", onAdd = onAddProduct, onDelete = onDeleteProduct)
                            }
                        }
                        "BOARDING" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !showBookings,
                                    onClick = { showBookings = false },
                                    label = { Text("Sitters", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showBookings,
                                    onClick = { showBookings = true },
                                    label = { Text("Bookings", fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (showBookings) {
                                BookingsAdminTab(
                                    bookings = bookings,
                                    onAssign = { booking -> assigningBooking = booking },
                                    onUpdateStatus = onUpdateBookingStatus,
                                    typeFilter = "BOARDING"
                                )
                            } else {
                                ProductsAdminTab(products = products, listTypeFilter = "Boarding", onAdd = onAddProduct, onDelete = onDeleteProduct)
                            }
                        }
                        "TRAINERS" -> ProductsAdminTab(products = products, listTypeFilter = "Trainer", onAdd = onAddProduct, onDelete = onDeleteProduct)
                        "LISTINGS" -> ListingsAdminTab(listings = listings, onDelete = onDeleteListing)
                        "PARTNERS" -> PartnersAdminTab(applications = partnerApplications, onDelete = onDeletePartnerApplication)
                        "SUPPORT" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !showBookings,
                                    onClick = { showBookings = false },
                                    label = { Text("Tickets", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showBookings,
                                    onClick = { showBookings = true },
                                    label = { Text("Rescue Reports", fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (showBookings) {
                                RescueReportsAdminTab(reports = rescueReports, onDelete = onDeleteRescueReport)
                            } else {
                                SupportTicketsAdminTab(tickets = supportTickets, onDelete = onDeleteSupportTicket)
                            }
                        }
                        "ALERTS" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !showBookings,
                                    onClick = { showBookings = false },
                                    label = { Text("SOS Alerts", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = showBookings,
                                    onClick = { showBookings = true },
                                    label = { Text("GPS Trackers", fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (showBookings) {
                                ProductsAdminTab(products = products, listTypeFilter = "GPS", onAdd = onAddProduct, onDelete = onDeleteProduct)
                            } else {
                                LostAlertsAdminTab(alerts = lostAlerts, onDelete = onDeleteLostAlert)
                            }
                        }
                        "FOUND" -> FoundReportsAdminTab(reports = foundReports, onDelete = onDeleteFoundReport)
                        "LEDGER" -> PartnerLedgerTab(
                            rows = partnerRows,
                            unattributedOrders = orders.count { it.dealerName.isBlank() },
                            unattributedBookings = bookings.count {
                                it.assignedName.isBlank() &&
                                    (it.type == "DOCTOR" || it.type == "TRAINER" || it.type == "BOARDING" || it.type == "GROOMING")
                            },
                            onEditPercent = { row -> editingPartner = row },
                            onTogglePaid = { row -> onSetPartnerDuesPaid(row.toCommission(), !row.duesPaid) }
                        )
                        else -> DealersAdminTab(dealers = dealers, onAdd = onAddDealer, onDelete = onDeleteDealer)
                    }
                }
            }
        }
    }

    assigningOrder?.let { order ->
        Dialog(onDismissRequest = { assigningOrder = null }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                val dctx = LocalContext.current
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.admin_pick_dealer), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (dealers.isEmpty()) {
                        Text(stringResource(R.string.admin_no_dealers), fontSize = 13.sp)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                        dealers.forEach { dealer ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dealer.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(dealer.phone + "  -  " + dealer.city, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = {
                                    try {
                                        dctx.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_DIAL,
                                                android.net.Uri.parse("tel:" + dealer.phone)
                                            )
                                        )
                                    } catch (_: Exception) { }
                                }) {
                                    Text("Call", fontSize = 12.sp, color = Color(0xFF1976D2))
                                }
                                Button(
                                    onClick = {
                                        onAssignDealer(order.id, dealer)
                                        assigningOrder = null
                                    },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Assign", fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        }
                    }
                }
            }
        }
    }

    assigningBooking?.let { booking ->
        AssignBookingDialog(
            booking = booking,
            vets = vets,
            dealers = dealers,
            onDismiss = { assigningBooking = null },
            onAssign = { name, phone ->
                onAssignBooking(booking.id, name, phone)
                assigningBooking = null
            }
        )
    }

    editingPartner?.let { row ->
        PartnerCommissionDialog(
            row = row,
            onDismiss = { editingPartner = null },
            onSave = { percent ->
                onSetPartnerCommission(row.toCommission().copy(commissionPercent = percent))
                editingPartner = null
            }
        )
    }
}

/**
 * Dialog: admin picks the provider (auto-fetched from saved vets / partners, scrollable)
 * or types the name + phone manually, then confirms the booking.
 */
@Composable
private fun AssignBookingDialog(
    booking: ServiceBooking,
    vets: List<VerifiedDoctor>,
    dealers: List<Dealer>,
    onDismiss: () -> Unit,
    onAssign: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(booking.providerName) }
    var phone by remember { mutableStateOf("") }

    // Candidates auto-fetched from the saved data for this booking type.
    val candidates: List<Triple<String, String, String>> = if (booking.type == "DOCTOR") {
        vets.map { Triple(it.name, it.phone, it.specialization + "  -  " + it.clinicCity) }
    } else {
        dealers.map { Triple(it.name, it.phone, it.city) }
    }
    val title = when (booking.type) {
        "DOCTOR" -> "Assign Doctor"
        "TRAINER" -> "Assign Trainer"
        "BOARDING" -> "Assign Sitter"
        "GROOMING" -> "Assign Groomer"
        "SUBSCRIPTION" -> "Assign Partner"
        else -> "Assign Provider"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(booking.customerName + " - " + booking.customerPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (booking.providerName.isNotBlank()) {
                    Text("Customer requested: " + booking.providerName, fontSize = 12.sp, color = Color(0xFFA87A1F))
                }
                Spacer(Modifier.height(10.dp))

                if (candidates.isNotEmpty()) {
                    Text(
                        text = if (booking.type == "DOCTOR") "Pick from your saved vets" else "Pick from your saved partners",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 190.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        candidates.forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.first, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(c.third, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (c.second.isNotBlank()) {
                                        Text(c.second, fontSize = 11.sp, color = Color(0xFF1976D2))
                                    }
                                }
                                TextButton(onClick = { name = c.first; phone = c.second }) {
                                    Text("Use", fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Text(
                        "No saved providers yet. Add vets/partners in the admin panel, or type the name below.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "On confirm: booking status becomes CONFIRMED and this name + phone are saved on the booking.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { if (name.isNotBlank()) onAssign(name.trim(), phone.trim()) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Confirm & Assign", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsAdminTab(
    bookings: List<ServiceBooking>,
    onAssign: (ServiceBooking) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    typeFilter: String? = null
) {
    val shown = if (typeFilter != null) bookings.filter { it.type == typeFilter } else bookings
    if (shown.isEmpty()) {
        Text("No bookings yet. New requests will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(shown) { booking ->
            AdminBookingCard(booking = booking, onAssign = onAssign, onUpdateStatus = onUpdateStatus)
        }
    }
}

private fun sectionTitle(key: String?): String = when (key) {
    "ORDERS" -> "📦 Orders"
    "FOOD" -> "🍲 Food"
    "MEDICINE" -> "💊 Medicine"
    "GROOMING" -> "✂️ Grooming"
    "DOCTOR" -> "🩺 Doctor & Health"
    "TRAINING" -> "🎓 Training"
    "ACCESSORIES" -> "🛍️ Accessories"
    "SUBSCRIPTION" -> "🔁 Subscriptions"
    "BOARDING" -> "🏡 Boarding & Sitters"
    "TRAINERS" -> "Trainers & Behaviour"
    "LISTINGS" -> "🐾 Sale & Adoption"
    "ALERTS" -> "🚨 Find My Pet"
    "FOUND" -> "🐾 Found Reports"
    "SUPPORT" -> "🆘 Help & Support"
    "PARTNERS" -> "🤝 Partner Applications"
    "DEALERS" -> "🚚 Dealers"
    "LEDGER" -> "Partner Ledger"
    else -> "Admin"
}

private fun bookingTypeFor(key: String?): String = when (key) {
    "DOCTOR" -> "DOCTOR"
    "TRAINING" -> "TRAINER"
    "BOARDING" -> "BOARDING"
    "GROOMING" -> "GROOMING"
    else -> "GROOMING"
}

/** Admin v2 home: one card per managed category. */
@Composable
private fun AdminDashboard(
    pendingCounts: Map<String, Int> = emptyMap(),
    pendingTotal: Int? = null,
    onSelect: (String) -> Unit
) {
    val sections = listOf(
        "ORDERS" to ("Orders" to stringResource(R.string.admin_orders_subtitle)),
        "FOOD" to ("Food" to "Pet food catalogue"),
        "MEDICINE" to ("Medicine" to "Pharmacy catalogue"),
        "GROOMING" to ("Grooming" to "Services & bookings"),
        "DOCTOR" to ("Doctor & Health" to "Partner vets & bookings"),
        "TRAINING" to ("Training" to "Programs & bookings"),
        "ACCESSORIES" to ("Accessories" to "Toys, clothing & more"),
        "SUBSCRIPTION" to ("Subscriptions" to "Food plan subscriptions"),
        "BOARDING" to ("Boarding & Sitters" to "Sitters + bookings"),
        "TRAINERS" to ("Trainers & Behaviour" to "Trainer & behaviourist partners"),
        "LISTINGS" to ("Sale & Adoption" to "All pet listings"),
        "ALERTS" to ("Find My Pet" to "SOS alerts + GPS trackers"),
        "FOUND" to ("Found Reports" to "Sighting reports from users"),
        "SUPPORT" to ("Help & Support" to "Customer tickets + rescue reports"),
        "PARTNERS" to ("Partner Applications" to "Business joins + featured plans"),
        "LEDGER" to (stringResource(R.string.admin_ledger_title_card) to stringResource(R.string.admin_ledger_subtitle)),
        "DEALERS" to ("Dealers" to stringResource(R.string.admin_dealers_subtitle))
    )
    val totalPending = pendingTotal ?: pendingCounts.values.sum()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (totalPending > 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔔 " + totalPending + " items pending / in progress - tap a card below",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        items(sections.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    AdminDashboardCard(
                        title = label.first,
                        subtitle = label.second,
                        badge = pendingCounts[key] ?: 0,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(key) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AdminDashboardCard(title: String, subtitle: String, badge: Int = 0, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                if (badge > 0) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD32F2F)) {
                        Text(
                            text = badge.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** Business partner joining forms + featured-plan requests submitted from the app. */
@Composable
private fun PartnersAdminTab(applications: List<PartnerApplication>, onDelete: (String) -> Unit) {
    if (applications.isEmpty()) {
        Text("No partner applications yet. Business join forms and featured-plan requests will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    val context = LocalContext.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(applications, key = { it.id }) { a ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤝 " + a.kind, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatTimestamp(a.createdAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (a.name.isNotBlank()) Text(a.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (a.category.isNotBlank()) Text("Category: " + a.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (a.city.isNotBlank()) Text("City: " + a.city, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (a.planName.isNotBlank()) Text("Plan: " + a.planName, fontSize = 12.sp, color = Color(0xFFA87A1F), fontWeight = FontWeight.SemiBold)
                    if (a.phone.isNotBlank()) Text("Phone: " + a.phone, fontSize = 12.sp, color = Color(0xFF1976D2))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (a.phone.isNotBlank()) {
                            TextButton(onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + a.phone)))
                                } catch (_: Exception) { }
                            }) {
                                Text("Call", fontSize = 12.sp, color = Color(0xFF1976D2))
                            }
                        }
                        TextButton(onClick = { onDelete(a.id) }) {
                            Text("Delete", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

/** All marketplace listings (sale + adoption) with delete for moderation. */
@Composable
private fun ListingsAdminTab(listings: List<MarketPet>, onDelete: (String) -> Unit) {
    if (listings.isEmpty()) {
        Text("No listings yet. Pets listed for sale or adoption by users will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listings, key = { it.id }) { pet ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${if (pet.listingType == "Adoption") "🤝" else "💰"} ${pet.name}",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Text(
                            if (pet.listingType == "Adoption") "Adoption" else "₹ ${pet.priceInr.toInt()}",
                            color = if (pet.listingType == "Adoption") Color(0xFF1D7A6E) else MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text("${pet.species} • ${pet.breed} • ${pet.age} • ${pet.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Seller: ${pet.sellerName} (${pet.sellerPhone})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onDelete(pet.id) }) {
                        Text("Delete listing", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

/** All lost-pet SOS alerts with delete (pet found / spam moderation). */
@Composable
private fun LostAlertsAdminTab(alerts: List<AdminLostPetAlert>, onDelete: (String) -> Unit) {
    if (alerts.isEmpty()) {
        Text("No lost-pet alerts. SOS alerts posted by users will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(alerts, key = { it.id }) { alert ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚨 ${alert.petName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(alert.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${alert.species} • ${alert.breed}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (alert.location.isNotBlank()) {
                        Text("Last seen: ${alert.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (alert.reward.isNotBlank()) {
                        Text("Reward: ${alert.reward}", fontSize = 12.sp, color = Color(0xFFA87A1F))
                    }
                    Text("Contact: ${alert.contactPhone}", fontSize = 12.sp, color = Color(0xFF1976D2))
                    TextButton(onClick = { onDelete(alert.id) }) {
                        Text("Delete alert", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

/** "I saw this pet" sighting reports — photo, where/when, finder's phone, Call + WhatsApp + delete. */
@Composable
private fun FoundReportsAdminTab(reports: List<FoundPetReport>, onDelete: (String) -> Unit) {
    if (reports.isEmpty()) {
        Text("No sighting reports yet. \"I saw this pet\" reports from users will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(reports, key = { it.id }) { report ->
            val context = LocalContext.current
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val photo = report.photoData.firstOrNull()
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = "Sighting photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Text("🐾 ${report.petName.ifBlank { "Lost pet" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (report.location.isNotBlank()) {
                        Text("Where seen: ${report.location}", fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                    Text("When: ${formatTimestamp(report.createdAt)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (report.note.isNotBlank()) {
                        Text("Note: ${report.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Finder phone: ${report.finderPhone}", fontSize = 12.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (report.finderPhone.isNotBlank()) {
                            TextButton(onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${report.finderPhone}")))
                                } catch (_: Exception) { }
                            }) {
                                Text("Call", fontSize = 12.sp, color = Color(0xFF1976D2))
                            }
                            TextButton(onClick = {
                                try {
                                    val msg = "Wagmiya: Thank you for reporting a sighting of ${report.petName.ifBlank { "a lost pet" }} at ${report.location}. Can you share more details?"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappJobUrl(report.finderPhone, msg))))
                                } catch (_: Exception) { }
                            }) {
                                Text("WhatsApp", fontSize = 12.sp, color = Color(0xFF25D366))
                            }
                        }
                        TextButton(onClick = { onDelete(report.id) }) {
                            Text("Delete report", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBookingCard(
    booking: ServiceBooking,
    onAssign: (ServiceBooking) -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    val context = LocalContext.current
    val statusColor = when (booking.status) {
        "NEW" -> Color(0xFFF57C00)
        "CONFIRMED" -> Color(0xFF1976D2)
        "COMPLETED" -> Color(0xFF388E3C)
        else -> Color(0xFFD32F2F)
    }
    val statusLabel = when (booking.status) {
        "NEW" -> "NEW"
        "CONFIRMED" -> "CONFIRMED"
        "COMPLETED" -> "COMPLETED"
        else -> "CANCELLED"
    }
    val typeLabel = when (booking.type) {
        "DOCTOR" -> "\uD83E\uDE7A Doctor"
        "GROOMING" -> "\u2702\uFE0F Grooming"
        "BOARDING" -> "\uD83C\uDFE1 Boarding / Sitter"
        "SUBSCRIPTION" -> "\uD83D\uDD01 Subscription"
        else -> "\uD83C\uDF93 Trainer"
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(typeLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("${booking.customerName} - ${booking.customerPhone}", fontSize = 13.sp)
            Text("Pet: ${booking.petName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (booking.providerName.isNotBlank()) {
                Text("Requested: ${booking.providerName} (${booking.serviceInfo})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("${booking.serviceInfo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (booking.dateLabel.isNotBlank() || booking.slot.isNotBlank()) {
                Text("Schedule: ${booking.dateLabel} ${booking.slot}".trim(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (booking.notes.isNotBlank()) {
                Text("Notes: ${booking.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (booking.feeInr > 0) {
                Text("Fee: ₹ ${booking.feeInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            if (booking.assignedName.isNotBlank()) {
                Text("Assigned: ${booking.assignedName} (${booking.assignedPhone})", fontSize = 12.sp, color = Color(0xFF1976D2))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (booking.status == "NEW") {
                    Button(onClick = { onAssign(booking) }) {
                        Text("Assign", fontSize = 12.sp)
                    }
                }
                if (booking.status == "CONFIRMED") {
                    OutlinedButton(onClick = { onUpdateStatus(booking.id, "COMPLETED") }) {
                        Text("Mark Completed", fontSize = 12.sp)
                    }
                    TextButton(onClick = { onUpdateStatus(booking.id, "CANCELLED") }) {
                        Text("Cancel", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
                if (booking.assignedPhone.isNotBlank()) {
                    TextButton(onClick = {
                        try {
                            val msg = "New Wagmiya service job\n" +
                                "Type: " + booking.type + "\n" +
                                "Customer: " + booking.customerName + " - " + booking.customerPhone + "\n" +
                                "Pet: " + booking.petName + "\n" +
                                "Service: " + booking.serviceInfo + "\n" +
                                (if (booking.dateLabel.isNotBlank()) "Schedule: " + booking.dateLabel + " " + booking.slot + "\n" else "") +
                                (if (booking.notes.isNotBlank()) "Notes: " + booking.notes + "\n" else "") +
                                "Fee: Rs " + booking.feeInr.toInt() + " (collect from customer)\n" +
                                "Please complete the service and reply DONE."
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(whatsappJobUrl(booking.assignedPhone, msg))
                                )
                            )
                        } catch (_: Exception) { }
                    }) {
                        Text("Send job to partner", fontSize = 12.sp, color = Color(0xFF25D366))
                    }
                }
                if (booking.customerPhone.isNotBlank()) {
                    TextButton(onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerPhone}"))
                            )
                        } catch (_: Exception) { }
                    }) {
                        Text("Call Customer", fontSize = 12.sp, color = Color(0xFF1976D2))
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersAdminTab(
    orders: List<AdminOrder>,
    onAssign: (AdminOrder) -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    if (orders.isEmpty()) {
        Text(stringResource(R.string.admin_no_orders), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { order ->
            AdminOrderCard(order = order, onAssign = onAssign, onUpdateStatus = onUpdateStatus)
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: AdminOrder,
    onAssign: (AdminOrder) -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    val context = LocalContext.current
    val statusColor = when (order.status) {
        "NEW" -> Color(0xFFF57C00)
        "ASSIGNED" -> Color(0xFF1976D2)
        "DELIVERED" -> Color(0xFF388E3C)
        else -> Color(0xFFD32F2F)
    }
    val statusLabel = stringResource(
        when (order.status) {
            "NEW" -> R.string.admin_status_new
            "ASSIGNED" -> R.string.admin_status_assigned
            "DELIVERED" -> R.string.admin_status_delivered
            else -> R.string.admin_status_cancelled
        }
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("${order.customerName} - ${order.customerPhone}", fontSize = 13.sp)
            Text("${order.address}, ${order.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${order.items.sumOf { it.quantity }} ${stringResource(R.string.admin_items)} - ₹ ${order.totalInr.toInt()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (order.dealerName.isNotBlank()) {
                Text("${order.dealerName} (${order.dealerPhone})", fontSize = 12.sp, color = Color(0xFF1976D2))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.status == "NEW") {
                    Button(onClick = { onAssign(order) }) {
                        Text(stringResource(R.string.admin_assign_dealer), fontSize = 12.sp)
                    }
                }
                if (order.status == "ASSIGNED") {
                    OutlinedButton(onClick = { onUpdateStatus(order.id, "DELIVERED") }) {
                        Text(stringResource(R.string.admin_mark_delivered), fontSize = 12.sp)
                    }
                    TextButton(onClick = { onUpdateStatus(order.id, "CANCELLED") }) {
                        Text(stringResource(R.string.admin_cancel_order), fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
                if (order.dealerPhone.isNotBlank()) {
                    TextButton(onClick = {
                        try {
                            val msg = "New Wagmiya delivery job\n" +
                                "Order #" + order.orderNumber + "\n" +
                                "Customer: " + order.customerName + " - " + order.customerPhone + "\n" +
                                "Address: " + order.address + ", " + order.city + "\n" +
                                "Items: " + order.items.sumOf { it.quantity } + " - Rs " + order.totalInr.toInt() + " (collect COD)\n" +
                                "Please deliver and reply DONE."
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(whatsappJobUrl(order.dealerPhone, msg))
                                )
                            )
                        } catch (_: Exception) { }
                    }) {
                        Text(stringResource(R.string.admin_send_job_dealer), fontSize = 12.sp, color = Color(0xFF25D366))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsAdminTab(
    products: List<ShopProduct>,
    listTypeFilter: String? = null,
    onAdd: (String, String, String, Double, String, String, String, String, List<Uri>, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val shown = if (listTypeFilter != null) products.filter { it.listType == listTypeFilter } else products
    var name by remember { mutableStateOf("") }
    var listType by remember { mutableStateOf(listTypeFilter ?: "Food") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("All") }
    var foodType by remember { mutableStateOf("All") }
    var lifeStage by remember { mutableStateOf("All") }
    var placePhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var verified by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris -> placePhotos = uris }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.admin_product_name)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = category, onValueChange = { category = it },
                label = { Text(stringResource(R.string.admin_category)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text(stringResource(R.string.admin_price_inr)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text(stringResource(R.string.admin_description)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )
        }
        if (listTypeFilter == null) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    adminListTypes.forEach { lt ->
                        FilterChip(
                            selected = listType == lt,
                            onClick = { listType = lt },
                            label = { Text(lt, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
        if (listType == "Food") {
            item {
                Column {
                    Text("Shop by pet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("All", "Dog", "Cat", "Other").forEach { v ->
                            FilterChip(selected = petType == v, onClick = { petType = v }, label = { Text(v, fontSize = 11.sp) })
                        }
                    }
                }
            }
            item {
                Column {
                    Text("Food type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("All", "Dry", "Wet", "Treats", "Supplements").forEach { v ->
                            FilterChip(selected = foodType == v, onClick = { foodType = v }, label = { Text(v, fontSize = 11.sp) })
                        }
                    }
                }
            }
            item {
                Column {
                    Text("Life stage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("All", "Puppy & Kitten", "Adult", "Senior").forEach { v ->
                            FilterChip(selected = lifeStage == v, onClick = { lifeStage = v }, label = { Text(v, fontSize = 11.sp) })
                        }
                    }
                }
            }
        }
        item {
            Column {
                Text(
                    if (listType == "Boarding") "Place photos (min 3)" else "Product photos (up to 3)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (listType == "Boarding") "House / flat, cage, sleeping and playing area"
                    else "Clear photos help customers buy - food, toys, medicine, accessories",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Button(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(if (placePhotos.isEmpty()) "Add Photos" else placePhotos.size.toString() + " selected")
                }
                if (photoError && listType == "Boarding" && placePhotos.size < 3) {
                    Text("Please add at least 3 photos.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
                if (placePhotos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(placePhotos) { u ->
                            AsyncImage(
                                model = u,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }
        }
        if (listType == "Boarding") {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(checked = verified, onCheckedChange = { verified = it })
                    Text("Verified sitter / walker", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull()
                    if (name.isNotBlank() && p != null && p > 0) {
                        if (listType == "Boarding" && placePhotos.size < 3) {
                            photoError = true
                        } else {
                            onAdd(name.trim(), listType, category.trim(), p, description.trim(), petType, foodType, lifeStage, placePhotos, verified)
                            photoError = false
                            placePhotos = emptyList()
                            verified = false
                            name = ""
                            category = ""
                            price = ""
                            description = ""
                        }
                    }
                },
                enabled = name.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(stringResource(R.string.admin_add_product))
            }
        }
        item { Divider() }
        if (shown.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.admin_no_products),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(shown) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${p.listType} - ₹ ${p.priceInr.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onDelete(p.id) }) {
                            Text(stringResource(R.string.admin_delete), fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealersAdminTab(
    dealers: List<Dealer>,
    onAdd: (String, String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.admin_dealer_name)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text(stringResource(R.string.admin_phone)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text(stringResource(R.string.admin_city)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(name.trim(), phone.trim(), city.trim().ifBlank { "Kochi" })
                        name = ""
                        phone = ""
                        city = ""
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text(stringResource(R.string.admin_add))
            }
        }
        item { Divider() }
        if (dealers.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.admin_no_dealers),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(dealers) { d ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${d.phone} - ${d.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onDelete(d.id) }) {
                            Text(stringResource(R.string.admin_delete), fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VetsAdminTab(
    vets: List<VerifiedDoctor>,
    onAdd: (String, String, String, String, String, Double, Double, Boolean, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var videoFee by remember { mutableStateOf("") }
    var inPersonFee by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(true) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Add Partner Vet", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Vet Name (Dr. added automatically)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = specialization, onValueChange = { specialization = it },
                label = { Text("Specialization (e.g. Canine & Feline Surgeon)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = clinicName, onValueChange = { clinicName = it },
                label = { Text("Clinic Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text("City (Kochi, Thrissur...)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = videoFee, onValueChange = { videoFee = it },
                    label = { Text("Video Fee ₹") },
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = inPersonFee, onValueChange = { inPersonFee = it },
                    label = { Text("Clinic Fee ₹") },
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(checked = isOnline, onCheckedChange = { isOnline = it })
                Text("Online now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(checked = isVerified, onCheckedChange = { isVerified = it })
                Text(stringResource(R.string.admin_verified_provider), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(
                            name.trim(),
                            specialization.trim(),
                            clinicName.trim(),
                            city.trim().ifBlank { "Kochi" },
                            phone.trim(),
                            videoFee.trim().toDoubleOrNull() ?: 0.0,
                            inPersonFee.trim().toDoubleOrNull() ?: 0.0,
                            isOnline,
                            isVerified
                        )
                        name = ""
                        specialization = ""
                        clinicName = ""
                        city = ""
                        phone = ""
                        videoFee = ""
                        inPersonFee = ""
                        isOnline = false
                        isVerified = true
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text(stringResource(R.string.admin_add))
            }
        }
        item { Divider() }
        if (vets.isEmpty()) {
            item {
                Text(
                    "No partner vets yet. Add your clinic partners here - they show in Services > Doctors and in the AI symptom checker.",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(vets) { vet ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(vet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { onDelete(vet.id) }) {
                                Text(stringResource(R.string.admin_delete), fontSize = 12.sp, color = Color(0xFFD32F2F))
                            }
                        }
                        Text("${vet.specialization} • ${vet.clinicName}, ${vet.clinicCity}", fontSize = 12.sp)
                        Text(
                            "Video ₹${vet.videoConsultFeeInr.toInt()} • Clinic ₹${vet.inPersonConsultFeeInr.toInt()} • ${vet.phone}",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportTicketsAdminTab(tickets: List<SupportTicket>, onDelete: (String) -> Unit) {
    if (tickets.isEmpty()) {
        Text("No support tickets yet. Problems reported by users will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tickets, key = { it.id }) { t ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${t.category}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatTimestamp(t.createdAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(t.subject, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    if (t.details.isNotBlank()) {
                        Text(t.details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (t.contact.isNotBlank()) {
                        Text("Contact: ${t.contact}", fontSize = 12.sp, color = Color(0xFF1976D2))
                    }
                    TextButton(onClick = { onDelete(t.id) }) {
                        Text("Delete ticket", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
private fun RescueReportsAdminTab(reports: List<RescueReport>, onDelete: (String) -> Unit) {
    if (reports.isEmpty()) {
        Text("No rescue reports yet. Animal-in-need reports from users will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(reports, key = { it.id }) { r ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🐕 ${r.animalType}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatTimestamp(r.createdAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (r.description.isNotBlank()) {
                        Text(r.description, fontSize = 12.sp)
                    }
                    if (r.location.isNotBlank()) {
                        Text("Location: ${r.location}", fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                    if (r.contact.isNotBlank()) {
                        Text("Contact: ${r.contact}", fontSize = 12.sp, color = Color(0xFF1976D2))
                    }
                    TextButton(onClick = { onDelete(r.id) }) {
                        Text("Delete report", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return ""
    return java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(millis))
}

/** WhatsApp deep link to a dealer/partner phone with a prefilled job message. */
private fun whatsappJobUrl(phone: String, message: String): String {
    val digits = phone.filter { it.isDigit() }
    val encoded = java.net.URLEncoder.encode(message, "UTF-8")
    return "https://wa.me/" + digits + "?text=" + encoded
}

// ===================== Partner Ledger (admin commission tracking) =====================

/** A partner discovered from the existing dealers / vets / bookings data. */
private data class PartnerSeed(
    val key: String,
    val name: String,
    val typeCode: String,
    val phone: String
)

/** One rendered Partner Ledger row: partner + attributed business + commission. */
private data class PartnerLedgerRow(
    val key: String,
    val name: String,
    val typeCode: String,
    val phone: String,
    val attributedCount: Int,
    val commissionPercent: Double,
    val duesPaid: Boolean,
    val commissionMonthInr: Double
) {
    fun toCommission(): PartnerCommission = PartnerCommission(
        id = key,
        partnerName = name,
        partnerType = typeCode,
        phone = phone,
        commissionPercent = commissionPercent,
        duesPaid = duesPaid,
        updatedAt = 0L
    )
}

private fun partnerKey(typeCode: String, name: String, phone: String): String =
    typeCode + "|" + name.trim().lowercase() + "|" + normalizePhone(phone)

private fun normalizePhone(phone: String): String = phone.filter { it.isDigit() }

private fun formatPercentNumber(percent: Double): String =
    if (percent == Math.floor(percent)) percent.toInt().toString() else percent.toString()

/** Rupee amount with Indian digit grouping, e.g. 1234567 -> ₹12,34,567. */
private fun formatIndianRupees(amount: Double): String = "₹" + groupIndian(Math.round(amount))

private fun groupIndian(value: Long): String {
    val negative = value < 0
    val digits = Math.abs(value).toString()
    val grouped = if (digits.length <= 3) {
        digits
    } else {
        val last3 = digits.substring(digits.length - 3)
        val rest = digits.substring(0, digits.length - 3)
        val sb = StringBuilder()
        var i = rest.length
        while (i > 0) {
            val start = maxOf(0, i - 2)
            if (sb.isNotEmpty()) sb.insert(0, ',')
            sb.insert(0, rest.substring(start, i))
            i = start
        }
        sb.append(',').append(last3).toString()
    }
    return (if (negative) "-" else "") + grouped
}

/** Epoch millis of 00:00 on the 1st of the current month (device timezone). */
private fun startOfCurrentMonthMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/**
 * Builds the Partner Ledger rows. Partners come ONLY from data the app already has:
 * the "dealers" collection, the "vets" collection, and the provider names assigned
 * on bookings. Business is attributed to a partner by matching the assigned dealer /
 * provider NAME or PHONE on orders and bookings - nothing is invented, and a partner
 * with no attribution simply shows a count of 0.
 */
private fun buildPartnerLedgerRows(
    dealers: List<Dealer>,
    vets: List<VerifiedDoctor>,
    orders: List<AdminOrder>,
    bookings: List<ServiceBooking>,
    saved: List<PartnerCommission>
): List<PartnerLedgerRow> {
    val savedByKey = saved.associateBy { it.id }
    val seeds = LinkedHashMap<String, PartnerSeed>()

    fun seedOf(name: String, typeCode: String, phone: String) {
        if (name.isBlank()) return
        val key = partnerKey(typeCode, name, phone)
        if (!seeds.containsKey(key)) {
            seeds[key] = PartnerSeed(key, name.trim(), typeCode, phone.trim())
        }
    }

    dealers.forEach { seedOf(it.name, "DEALER", it.phone) }
    vets.forEach { seedOf(it.name, "VET", it.phone) }
    bookings.forEach { b ->
        if (b.assignedName.isNotBlank()) {
            val code = when (b.type) {
                "DOCTOR" -> "VET"
                "TRAINER" -> "TRAINER"
                "BOARDING" -> "SITTER"
                "GROOMING" -> "GROOMER"
                else -> "OTHER"
            }
            seedOf(b.assignedName, code, b.assignedPhone)
        }
    }

    val monthStart = startOfCurrentMonthMillis()

    fun matches(seed: PartnerSeed, name: String, phone: String): Boolean {
        val nameMatch = name.isNotBlank() && name.trim().equals(seed.name, ignoreCase = true)
        val phoneMatch = seed.phone.isNotBlank() && phone.isNotBlank() &&
            normalizePhone(phone) == normalizePhone(seed.phone)
        return nameMatch || phoneMatch
    }

    return seeds.values.map { seed ->
        val savedEntry = savedByKey[seed.key]
        val percent = savedEntry?.commissionPercent ?: 10.0
        val paid = savedEntry?.duesPaid ?: false

        val matchedOrders = orders.filter { matches(seed, it.dealerName, it.dealerPhone) }
        val matchedBookings = bookings.filter { matches(seed, it.assignedName, it.assignedPhone) }

        val orderValueMonth = matchedOrders.filter { it.createdAt in monthStart..Long.MAX_VALUE }.sumOf { it.totalInr }
        val bookingValueMonth = matchedBookings.filter { it.createdAt in monthStart..Long.MAX_VALUE }.sumOf { it.feeInr }
        val commission = (orderValueMonth + bookingValueMonth) * percent / 100.0

        PartnerLedgerRow(
            key = seed.key,
            name = seed.name,
            typeCode = seed.typeCode,
            phone = seed.phone,
            attributedCount = matchedOrders.size + matchedBookings.size,
            commissionPercent = percent,
            duesPaid = paid,
            commissionMonthInr = commission
        )
    }.sortedBy { it.name.lowercase() }
}

@Composable
private fun partnerTypeLabel(typeCode: String): String = when (typeCode) {
    "DEALER" -> stringResource(R.string.ledger_type_dealer)
    "VET" -> stringResource(R.string.ledger_type_vet)
    "SITTER" -> stringResource(R.string.ledger_type_sitter)
    "TRAINER" -> stringResource(R.string.ledger_type_trainer)
    "GROOMER" -> stringResource(R.string.ledger_type_groomer)
    else -> stringResource(R.string.ledger_type_other)
}

/** Admin "Partner Ledger" section: summary strip + one row per partner. */
@Composable
private fun PartnerLedgerTab(
    rows: List<PartnerLedgerRow>,
    unattributedOrders: Int,
    unattributedBookings: Int,
    onEditPercent: (PartnerLedgerRow) -> Unit,
    onTogglePaid: (PartnerLedgerRow) -> Unit
) {
    val totalMonth = rows.sumOf { it.commissionMonthInr }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.admin_ledger_summary_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatIndianRupees(totalMonth),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.admin_ledger_note),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (unattributedOrders > 0 || unattributedBookings > 0) {
            item {
                Text(
                    text = stringResource(R.string.admin_ledger_note_counts, unattributedOrders, unattributedBookings),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (rows.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.admin_ledger_empty),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(rows, key = { it.key }) { row ->
                PartnerLedgerRowCard(row = row, onEditPercent = onEditPercent, onTogglePaid = onTogglePaid)
            }
        }
    }
}

@Composable
private fun PartnerLedgerRowCard(
    row: PartnerLedgerRow,
    onEditPercent: (PartnerLedgerRow) -> Unit,
    onTogglePaid: (PartnerLedgerRow) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(row.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = partnerTypeLabel(row.typeCode) + "  •  " + stringResource(R.string.admin_ledger_count_value, row.attributedCount),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (row.phone.isNotBlank()) {
                        Text(row.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (row.duesPaid) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(
                            text = stringResource(R.string.admin_ledger_paid_chip),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.admin_ledger_percent_value, formatPercentNumber(row.commissionPercent)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.admin_ledger_month_amount, formatIndianRupees(row.commissionMonthInr)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onEditPercent(row) }) {
                        Text(stringResource(R.string.admin_ledger_edit_percent), fontSize = 12.sp)
                    }
                    TextButton(onClick = { onTogglePaid(row) }) {
                        Text(
                            text = stringResource(if (row.duesPaid) R.string.admin_ledger_mark_unpaid else R.string.admin_ledger_mark_paid),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerCommissionDialog(
    row: PartnerLedgerRow,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var percentText by remember { mutableStateOf(formatPercentNumber(row.commissionPercent)) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.admin_ledger_dialog_title), fontWeight = FontWeight.Bold)
                Text(
                    text = row.name + "  -  " + partnerTypeLabel(row.typeCode),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = percentText,
                    onValueChange = { percentText = it },
                    label = { Text(stringResource(R.string.admin_ledger_dialog_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.admin_ledger_cancel)) }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            val parsed = percentText.trim().toDoubleOrNull()
                            if (parsed != null) onSave(parsed.coerceIn(0.0, 100.0))
                        },
                        enabled = percentText.trim().toDoubleOrNull() != null
                    ) {
                        Text(stringResource(R.string.admin_ledger_save), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
