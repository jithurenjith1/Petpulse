package com.petpulse.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.petpulse.app.data.model.Dealer
import com.petpulse.app.data.model.ServiceBooking
import com.petpulse.app.data.model.ShopProduct

private val adminListTypes = listOf("Food", "Medicine", "Grooming")

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
    onAssignDealer: (String, Dealer) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onAddProduct: (String, String, String, Double, String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddDealer: (String, String, String) -> Unit,
    onDeleteDealer: (String) -> Unit,
    onAssignBooking: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateBookingStatus: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text(stringResource(R.string.admin_tab_orders)) })
                    FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text(stringResource(R.string.admin_tab_products)) })
                    FilterChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text(stringResource(R.string.admin_tab_dealers)) })
                    FilterChip(selected = tab == 3, onClick = { tab = 3 }, label = { Text("Bookings") })
                }
                Spacer(Modifier.height(8.dp))
                when (tab) {
                    0 -> OrdersAdminTab(
                        orders = orders,
                        onAssign = { order -> assigningOrder = order },
                        onUpdateStatus = onUpdateStatus
                    )
                    1 -> ProductsAdminTab(products = products, onAdd = onAddProduct, onDelete = onDeleteProduct)
                    2 -> DealersAdminTab(dealers = dealers, onAdd = onAddDealer, onDelete = onDeleteDealer)
                    else -> BookingsAdminTab(
                        bookings = bookings,
                        onAssign = { booking -> assigningBooking = booking },
                        onUpdateStatus = onUpdateBookingStatus
                    )
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
    onUpdateStatus: (String, String) -> Unit
) {
    if (bookings.isEmpty()) {
        Text("No bookings yet. Doctor consultations and trainer requests will appear here.", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(bookings) { booking ->
            AdminBookingCard(booking = booking, onAssign = onAssign, onUpdateStatus = onUpdateStatus)
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
    val typeLabel = if (booking.type == "DOCTOR") "\uD83E\uDE7A Doctor" else "\uD83C\uDF93 Trainer"

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
    onAdd: (String, String, String, Double, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var listType by remember { mutableStateOf("Food") }
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
        if (products.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.admin_no_products),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(products) { p ->
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
