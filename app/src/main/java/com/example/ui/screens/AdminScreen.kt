package com.petpulse.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.petpulse.app.data.model.SupportTicket
import com.petpulse.app.data.model.Dealer
import com.petpulse.app.data.model.ServiceBooking
import com.petpulse.app.data.model.ShopProduct
import com.petpulse.app.data.model.AdminLostPetAlert
import com.petpulse.app.data.model.MarketPet
import com.petpulse.app.data.model.VerifiedDoctor

private val adminListTypes = listOf("Food", "Medicine", "Grooming", "Accessory", "Training", "Subscription", "Boarding")

/**
 * Owner-only panel: Orders -> assign dealer -> mark delivered,
 * Products (add/remove own items), Dealers (add/remove delivery partners),
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
    supportTickets: List<SupportTicket> = emptyList(),
    rescueReports: List<RescueReport> = emptyList(),
    onDeleteListing: (String) -> Unit = {},
    onDeleteSupportTicket: (String) -> Unit = {},
    onDeleteRescueReport: (String) -> Unit = {},
    onDeleteLostAlert: (String) -> Unit = {},
    onAssignDealer: (String, Dealer) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onAddProduct: (String, String, String, Double, String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddDealer: (String, String, String) -> Unit,
    onDeleteDealer: (String) -> Unit,
    onAssignBooking: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateBookingStatus: (String, String) -> Unit = { _, _ -> },
    onAddVet: (String, String, String, String, String, Double, Double) -> Unit = { _, _, _, _, _, _, _ -> },
    onDeleteVet: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var section by remember { mutableStateOf<String?>(null) }
    var showBookings by remember { mutableStateOf(false) }
    var assigningOrder by remember { mutableStateOf<AdminOrder?>(null) }
    var assigningBooking by remember { mutableStateOf<ServiceBooking?>(null) }

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
                    AdminDashboard(onSelect = { section = it; showBookings = false })
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { section = null; showBookings = false }) {
                            Text("<", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(sectionTitle(section), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        "SUBSCRIPTION" -> ProductsAdminTab(products = products, listTypeFilter = "Subscription", onAdd = onAddProduct, onDelete = onDeleteProduct)
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
                        "LISTINGS" -> ListingsAdminTab(listings = listings, onDelete = onDeleteListing)
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
                        else -> DealersAdminTab(dealers = dealers, onAdd = onAddDealer, onDelete = onDeleteDealer)
                    }
                }
            }
        }
    }

    assigningOrder?.let { order ->
        Dialog(onDismissRequest = { assigningOrder = null }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.admin_pick_dealer), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (dealers.isEmpty()) {
                        Text(stringResource(R.string.admin_no_dealers), fontSize = 13.sp)
                    } else {
                        dealers.forEach { dealer ->
                            TextButton(onClick = {
                                onAssignDealer(order.id, dealer)
                                assigningOrder = null
                            }) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(dealer.name, fontWeight = FontWeight.SemiBold)
                                    Text("${dealer.phone} - ${dealer.city}", fontSize = 12.sp, color = Color.Gray)
                                }
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
            onDismiss = { assigningBooking = null },
            onAssign = { name, phone ->
                onAssignBooking(booking.id, name, phone)
                assigningBooking = null
            }
        )
    }
}

/** Dialog: admin enters the doctor/trainer name + phone to confirm a booking. */
@Composable
private fun AssignBookingDialog(
    booking: ServiceBooking,
    onDismiss: () -> Unit,
    onAssign: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(booking.providerName) }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Assign ${if (booking.type == "DOCTOR") "Doctor" else "Trainer"}", fontWeight = FontWeight.Bold)
                Text("${booking.customerName} - ${booking.customerPhone}", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
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
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { if (name.isNotBlank()) onAssign(name.trim(), phone.trim()) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Confirm Booking", fontSize = 12.sp)
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
    "LISTINGS" -> "🐾 Sale & Adoption"
    "ALERTS" -> "🚨 Find My Pet"
    "SUPPORT" -> "🆘 Help & Support"
    "DEALERS" -> "🚚 Dealers"
    else -> "Admin"
}

private fun bookingTypeFor(key: String?): String = when (key) {
    "DOCTOR" -> "DOCTOR"
    "TRAINING" -> "TRAINER"
    "BOARDING" -> "BOARDING"
    else -> "GROOMING"
}

/** Admin v2 home: one card per managed category. */
@Composable
private fun AdminDashboard(onSelect: (String) -> Unit) {
    val sections = listOf(
        "ORDERS" to ("Orders" to "COD orders, assign dealers"),
        "FOOD" to ("Food" to "Pet food catalogue"),
        "MEDICINE" to ("Medicine" to "Pharmacy catalogue"),
        "GROOMING" to ("Grooming" to "Services & bookings"),
        "DOCTOR" to ("Doctor & Health" to "Partner vets & bookings"),
        "TRAINING" to ("Training" to "Programs & bookings"),
        "ACCESSORIES" to ("Accessories" to "Toys, clothing & more"),
        "SUBSCRIPTION" to ("Subscriptions" to "Food plan subscriptions"),
        "BOARDING" to ("Boarding & Sitters" to "Sitters + bookings"),
        "LISTINGS" to ("Sale & Adoption" to "All pet listings"),
        "ALERTS" to ("Find My Pet" to "SOS alerts + GPS trackers"),
        "SUPPORT" to ("Help & Support" to "Customer tickets + rescue reports"),
        "DEALERS" to ("Dealers" to "Delivery partners")
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sections.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    AdminDashboardCard(
                        title = label.first,
                        subtitle = label.second,
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
private fun AdminDashboardCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
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
                            color = if (pet.listingType == "Adoption") Color(0xFF1D7A6E) else Color(0xFFBC5233),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text("${pet.species} • ${pet.breed} • ${pet.age} • ${pet.city}", fontSize = 12.sp, color = Color.Gray)
                    Text("Seller: ${pet.sellerName} (${pet.sellerPhone})", fontSize = 12.sp, color = Color.Gray)
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
                        Text(alert.date, fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("${alert.species} • ${alert.breed}", fontSize = 12.sp, color = Color.Gray)
                    if (alert.location.isNotBlank()) {
                        Text("Last seen: ${alert.location}", fontSize = 12.sp, color = Color.Gray)
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
            Text("Pet: ${booking.petName}", fontSize = 12.sp, color = Color.Gray)
            if (booking.providerName.isNotBlank()) {
                Text("Requested: ${booking.providerName} (${booking.serviceInfo})", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text("${booking.serviceInfo}", fontSize = 12.sp, color = Color.Gray)
            }
            if (booking.dateLabel.isNotBlank() || booking.slot.isNotBlank()) {
                Text("Schedule: ${booking.dateLabel} ${booking.slot}".trim(), fontSize = 12.sp, color = Color.Gray)
            }
            if (booking.notes.isNotBlank()) {
                Text("Notes: ${booking.notes}", fontSize = 12.sp, color = Color.Gray)
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
            Text("${order.address}, ${order.city}", fontSize = 12.sp, color = Color.Gray)
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
            }
        }
    }
}

@Composable
private fun ProductsAdminTab(
    products: List<ShopProduct>,
    listTypeFilter: String? = null,
    onAdd: (String, String, String, Double, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val shown = if (listTypeFilter != null) products.filter { it.listType == listTypeFilter } else products
    var name by remember { mutableStateOf("") }
    var listType by remember { mutableStateOf(listTypeFilter ?: "Food") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
        item {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull()
                    if (name.isNotBlank() && p != null && p > 0) {
                        onAdd(name.trim(), listType, category.trim(), p, description.trim())
                        name = ""
                        category = ""
                        price = ""
                        description = ""
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
                            Text("${p.listType} - ₹ ${p.priceInr.toInt()}", fontSize = 12.sp, color = Color.Gray)
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
                            Text("${d.phone} - ${d.city}", fontSize = 12.sp, color = Color.Gray)
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
    onAdd: (String, String, String, String, String, Double, Double) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var videoFee by remember { mutableStateOf("") }
    var inPersonFee by remember { mutableStateOf("") }

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
                            inPersonFee.trim().toDoubleOrNull() ?: 0.0
                        )
                        name = ""
                        specialization = ""
                        clinicName = ""
                        city = ""
                        phone = ""
                        videoFee = ""
                        inPersonFee = ""
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
                    "No partner vets yet. Add your clinic partners here — they will show in Market → Healthcare and replace the demo doctors.",
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
                            fontSize = 12.sp, color = Color.Gray
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
                        Text("${'$'}{t.category}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatTimestamp(t.createdAt), fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(t.subject, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    if (t.details.isNotBlank()) {
                        Text(t.details, fontSize = 12.sp, color = Color.Gray)
                    }
                    if (t.contact.isNotBlank()) {
                        Text("Contact: ${'$'}{t.contact}", fontSize = 12.sp, color = Color(0xFF1976D2))
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
                        Text("🐕 ${'$'}{r.animalType}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(formatTimestamp(r.createdAt), fontSize = 11.sp, color = Color.Gray)
                    }
                    if (r.description.isNotBlank()) {
                        Text(r.description, fontSize = 12.sp)
                    }
                    if (r.location.isNotBlank()) {
                        Text("Location: ${'$'}{r.location}", fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                    if (r.contact.isNotBlank()) {
                        Text("Contact: ${'$'}{r.contact}", fontSize = 12.sp, color = Color(0xFF1976D2))
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
