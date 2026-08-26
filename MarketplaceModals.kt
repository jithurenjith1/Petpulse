package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*

// ================= 1. SLIDE-OUT CART MODAL / SHEET =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideOutCartModal(
    cartItems: List<CartItem>,
    selectedCity: String,
    isExpress: Boolean,
    onToggleExpress: (Boolean) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onDismiss: () -> Unit,
    onProceedToEscrowCheckout: () -> Unit
) {
    val subtotal = cartItems.sumOf { it.priceInr * it.quantity }
    val freeThreshold = if (selectedCity.lowercase() in listOf("kochi", "thrissur")) 499.0 else 599.0
    val deliveryFee = if (subtotal == 0.0) 0.0 else if (subtotal >= freeThreshold) {
        if (isExpress) 50.0 else 0.0
    } else {
        val base = when (selectedCity.lowercase()) {
            "kochi" -> 35.0
            "thrissur" -> 40.0
            "trivandrum" -> 45.0
            "kozhikode" -> 50.0
            else -> 45.0
        }
        if (isExpress) base + 50.0 else base
    }
    val ecoFee = if (cartItems.isNotEmpty()) 10.0 else 0.0
    val total = subtotal + deliveryFee + ecoFee

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("slide_out_cart_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = BluePrimary
                        )
                        Text(
                            text = "Kerala Delivery Cart",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = onClearCart) {
                                Text("Clear", color = Color(0xFFD32F2F), fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Your Cart is Empty",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Add food, medicines, or supplies from the Kerala marketplace.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    // Free delivery progress banner
                    val amountRemaining = (freeThreshold - subtotal).coerceAtLeast(0.0)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (amountRemaining == 0.0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (amountRemaining == 0.0) "🎉" else "🚚", fontSize = 16.sp)
                            Text(
                                text = if (amountRemaining == 0.0)
                                    "You've unlocked FREE Standard Delivery in $selectedCity!"
                                else
                                    "Add ₹${amountRemaining.toInt()} more for FREE delivery in $selectedCity!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amountRemaining == 0.0) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Express Delivery checkbox
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "2-Hour Express Delivery",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Direct hub courier in Kochi / TVM (+₹50)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isExpress,
                                onCheckedChange = onToggleExpress,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cartItems, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.subtitle} • ₹${item.priceInr.toInt()} each",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (item.prescriptionRequired) {
                                            Text(
                                                text = "Rx Required",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.id, -1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = { onUpdateQuantity(item.id, 1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(BluePrimary)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Price Breakdown
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Subtotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee ($selectedCity)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (deliveryFee == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kerala Biodegradable Packaging", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${ecoFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Jane & Pals Escrow Protection", fontSize = 12.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                                Text("FREE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("₹${total.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimaryDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkout Button
                    Button(
                        onClick = onProceedToEscrowCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Proceed to Escrow Checkout (₹${total.toInt()})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ================= 2. SECURE ESCROW CHECKOUT MODAL =================
@Composable
fun SecureEscrowCheckoutModal(
    cartItems: List<CartItem>,
    customer: CustomerProfile,
    selectedCity: String,
    isExpress: Boolean,
    onDismiss: () -> Unit,
    onConfirmOrder: (city: String, address: String, name: String, phone: String, paymentMethod: String) -> Unit
) {
    var deliveryCity by remember { mutableStateOf(if (selectedCity == "All Kerala") "Kochi" else selectedCity) }
    var streetAddress by remember { mutableStateOf(if (customer.location.isNotBlank()) customer.location else "Door No 12/B, MG Road, Kerala") }
    var customerName by remember { mutableStateOf(customer.name) }
    var customerPhone by remember { mutableStateOf(customer.phone) }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery (COD)") }
    var prescriptionAttached by remember { mutableStateOf(false) }

    val hasMedicinesWithRx = cartItems.any { it.prescriptionRequired }
    val subtotal = cartItems.sumOf { it.priceInr * it.quantity }
    val freeThreshold = if (deliveryCity.lowercase() in listOf("kochi", "thrissur")) 499.0 else 599.0
    val deliveryFee = if (subtotal >= freeThreshold) {
        if (isExpress) 50.0 else 0.0
    } else {
        val base = when (deliveryCity.lowercase()) {
            "kochi" -> 35.0
            "thrissur" -> 40.0
            "trivandrum" -> 45.0
            "kozhikode" -> 50.0
            else -> 45.0
        }
        if (isExpress) base + 50.0 else base
    }
    val total = subtotal + deliveryFee + 10.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .testTag("secure_escrow_checkout_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
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
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Escrow Shield",
                                tint = Color(0xFF00796B),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Secure Escrow Checkout",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Escrow Guarantee Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        border = BorderStroke(1.dp, Color(0xFF80CBC4))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Jane & Pals Kerala Escrow Protection",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                                Text(
                                    text = "Your payment is held safely in escrow. Funds are released to the vendor ONLY AFTER doorstep inspection with your delivery OTP.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF004D40)
                                )
                            }
                        }
                    }
                }

                // Delivery Destination
                item {
                    Text(
                        text = "1. Kerala Delivery Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // City choice
                    val keralaCities = listOf("Kochi", "Trivandrum", "Kozhikode", "Thrissur")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(keralaCities) { city ->
                            val isSelected = deliveryCity.equals(city, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { deliveryCity = city },
                                label = { Text(city, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Contact Phone (+91)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = streetAddress,
                        onValueChange = { streetAddress = it },
                        label = { Text("House / Flat No., Street, Landmark") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }

                // Prescription Upload (if medicine requiring Rx)
                if (hasMedicinesWithRx) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📋 Veterinary Prescription Required",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "Your order contains antibiotics/prescription meds. Attach your KSVC doctor's prescription slip.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF5D4037)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { prescriptionAttached = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (prescriptionAttached) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                ) {
                                    Icon(if (prescriptionAttached) Icons.Default.CheckCircle else Icons.Default.UploadFile, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (prescriptionAttached) "Prescription Attached (KSVC Verified)" else "Attach Doctor Prescription",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Text(
                        text = "2. Escrow Payment Method",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    val paymentOptions = listOf(
                        "Cash on Delivery (COD)" to "Pay at doorstep after checking items (Verified via OTP)",
                        "UPI (GPay / PhonePe / Paytm)" to "Instant UPI transfer held in Kerala Escrow Vault",
                        "Credit / Debit Card" to "Visa, MasterCard, RuPay with 256-bit encryption",
                        "Kerala Netbanking (SBI / Federal)" to "Direct bank transfer from Federal Bank, SIB, SBI"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        paymentOptions.forEach { (method, desc) ->
                            val isSelected = paymentMethod == method
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = method },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) BluePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BluePrimary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { paymentMethod = method },
                                        colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = method,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Final Summary
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Escrow Amount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${total.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimaryDark)
                            }

                            Button(
                                onClick = {
                                    onConfirmOrder(deliveryCity, streetAddress, customerName, customerPhone, paymentMethod)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lock & Place Order", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= 3. ORDER TIMELINE TRACKING MODAL =================
@Composable
fun OrderTimelineTrackingModal(
    orders: List<EscrowOrder>,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .testTag("order_timeline_tracking_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = BluePrimary
                        )
                        Text(
                            text = "Kerala Orders & Live Timeline",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active orders yet. Place an escrow order from the marketplace!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(orders, key = { it.orderId }) { order ->
                            OrderCardWithTimeline(
                                order = order,
                                onShowMessage = onShowMessage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardWithTimeline(
    order: EscrowOrder,
    onShowMessage: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = BluePrimary
                    )
                    Text(
                        text = "${order.orderDate} • ${order.deliveryCity}, Kerala",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "₹${order.totalInr.toInt()} (${order.paymentMethod.take(8)}..)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OTP Badge for Delivery / Escrow release
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(1.dp, Color(0xFFFFE082))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Doorstep Escrow OTP:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                    }
                    Text(
                        text = order.deliveryOtp,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100),
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rider info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(order.deliveryRiderName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(order.deliveryRiderVehicle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedButton(
                    onClick = { onShowMessage("Connecting to rider helpline: ${order.deliveryRiderPhone}") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Rider", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Live Order Timeline:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stepper
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.timeline.forEachIndexed { index, event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (event.isCurrent) Color(0xFF2E7D32)
                                    else if (event.isCompleted) BluePrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (event.isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.title,
                                    fontWeight = if (event.isCurrent || event.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (event.isCurrent) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = event.timestamp,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = event.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items summary
            Text(
                text = "Items: " + order.items.joinToString { "${it.title} x${it.quantity}" },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

// ================= 4. FORM FOR PET OWNERS TO LIST PETS =================
@Composable
fun ListPetFormModal(
    onDismiss: () -> Unit,
    onSubmit: (name: String, species: String, breed: String, age: String, gender: String, city: String, isExotic: Boolean, listingType: String, price: Double, desc: String, phone: String) -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("3 Months") }
    var gender by remember { mutableStateOf("Male") }
    var city by remember { mutableStateOf("Kochi") }
    var isExotic by remember { mutableStateOf(false) }
    var listingType by remember { mutableStateOf("Sale") }
    var priceText by remember { mutableStateOf("15000") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 98470 00000") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .testTag("list_pet_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "List a Pet for Sale / Adoption",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Exotic toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isExotic) Color(0xFFEDE7F6) else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Imported / Exotic Pet?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Check for Macaw, Husky, Iguana, Persian, etc.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isExotic, onCheckedChange = { isExotic = it })
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Pet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text("Species:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val speciesOptions = listOf("Dog", "Cat", "Bird", "Reptile", "Rabbit")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(speciesOptions) { sp ->
                            FilterChip(
                                selected = species == sp,
                                onClick = { species = sp },
                                label = { Text(sp, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed (e.g. Siberian Husky, Indie, Persian)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Text("Kerala City:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val cities = listOf("Kochi", "Trivandrum", "Kozhikode", "Thrissur")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cities) { c ->
                            FilterChip(
                                selected = city == c,
                                onClick = { city = c },
                                label = { Text(c, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Listing Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = listingType == "Sale",
                            onClick = { listingType = "Sale" },
                            label = { Text("Sale (INR ₹)", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = listingType == "Adoption",
                            onClick = { listingType = "Adoption" },
                            label = { Text("Free for Adoption", fontSize = 12.sp) }
                        )
                    }
                }

                if (listingType == "Sale") {
                    item {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price in INR (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone (+91)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Pet Description & Temperament") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )
                }

                item {
                    Button(
                        onClick = {
                            val priceVal = priceText.toDoubleOrNull() ?: 0.0
                            onSubmit(petName, species, breed, age, gender, city, isExotic, listingType, priceVal, description, phone)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post Pet Listing with Escrow Protection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ================= 5. FORM FOR VETS TO REGISTER =================
@Composable
fun RegisterVetFormModal(
    onDismiss: () -> Unit,
    onSubmit: (name: String, degrees: String, ksvcNumber: String, spec: String, exp: Int, clinic: String, city: String, address: String, videoFee: Double, inPersonFee: Double, phone: String) -> Unit
) {
    var doctorName by remember { mutableStateOf("") }
    var degrees by remember { mutableStateOf("BVSc & AH, MVSc") }
    var ksvcNumber by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("Small Animal Physician") }
    var experienceText by remember { mutableStateOf("8") }
    var clinicName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Kochi") }
    var address by remember { mutableStateOf("") }
    var videoFeeText by remember { mutableStateOf("399") }
    var inPersonFeeText by remember { mutableStateOf("599") }
    var phone by remember { mutableStateOf("+91 94470 00000") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .testTag("register_vet_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = Color(0xFF00796B))
                            Text(
                                text = "Kerala Vet Doctor Registration",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Doctor Full Name (e.g. Dr. Anoop Kumar)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = ksvcNumber,
                        onValueChange = { ksvcNumber = it },
                        label = { Text("Kerala State Vet Council (KSVC) Reg Number") },
                        placeholder = { Text("e.g. KSVC/2019/4821") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = degrees,
                            onValueChange = { degrees = it },
                            label = { Text("Qualifications") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = experienceText,
                            onValueChange = { experienceText = it },
                            label = { Text("Exp (Years)") },
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { specialization = it },
                        label = { Text("Specialization (Surgery, Dermatology, Exotics)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Hospital / Clinic Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text("City / District in Kerala:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val cities = listOf("Kochi", "Trivandrum", "Kozhikode", "Thrissur")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cities) { c ->
                            FilterChip(
                                selected = city == c,
                                onClick = { city = c },
                                label = { Text(c, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Clinic Address & Landmark") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = videoFeeText,
                            onValueChange = { videoFeeText = it },
                            label = { Text("Video Fee (₹)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = inPersonFeeText,
                            onValueChange = { inPersonFeeText = it },
                            label = { Text("Clinic Fee (₹)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Official Phone / WhatsApp (+91)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            val exp = experienceText.toIntOrNull() ?: 5
                            val vFee = videoFeeText.toDoubleOrNull() ?: 399.0
                            val inFee = inPersonFeeText.toDoubleOrNull() ?: 599.0
                            onSubmit(doctorName, degrees, ksvcNumber, specialization, exp, clinicName, city, address, vFee, inFee, phone)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Register & Verify KSVC Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ================= 6. DOCTOR BOOKING MODAL =================
@Composable
fun DoctorBookingModal(
    doctor: VerifiedDoctor,
    defaultPetName: String,
    onDismiss: () -> Unit,
    onConfirm: (consultType: String, petName: String, date: String, slot: String) -> Unit
) {
    var consultType by remember { mutableStateOf("Video Consultation") }
    var petName by remember { mutableStateOf(defaultPetName) }
    var selectedDate by remember { mutableStateOf("Tomorrow") }
    var selectedSlot by remember { mutableStateOf("10:30 AM - 11:00 AM") }
    var problemNotes by remember { mutableStateOf("") }

    val fee = if (consultType.contains("Video")) doctor.videoConsultFeeInr else doctor.inPersonConsultFeeInr

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("doctor_booking_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Book Doctor Appointment",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${doctor.name} (${doctor.clinicCity})",
                                fontSize = 12.sp,
                                color = BluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Mode Selector
                item {
                    Text("1. Choose Consultation Mode:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { consultType = "Video Consultation" },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (consultType.contains("Video")) BluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (consultType.contains("Video")) BluePrimary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = BluePrimary)
                                Text("Video Consult", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${doctor.videoConsultFeeInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimaryDark)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { consultType = "In-Person Clinic Visit" },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (consultType.contains("In-Person")) BluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (consultType.contains("In-Person")) BluePrimary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF00796B))
                                Text("In-Clinic Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${doctor.inPersonConsultFeeInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00796B))
                            }
                        }
                    }
                }

                // Date Picker
                item {
                    Text("2. Select Date:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    val dates = listOf("Today", "Tomorrow", "In 2 Days", "Saturday")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(dates) { d ->
                            FilterChip(
                                selected = selectedDate == d,
                                onClick = { selectedDate = d },
                                label = { Text(d, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Slot Picker
                item {
                    Text("3. Available Time Slot:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    val slots = listOf("10:00 AM - 10:30 AM", "10:30 AM - 11:00 AM", "03:00 PM - 03:30 PM", "04:30 PM - 05:00 PM", "06:00 PM - 06:30 PM")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        slots.forEach { slot ->
                            val isSelected = selectedSlot == slot
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSlot = slot },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) BluePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, if (isSelected) BluePrimary else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { selectedSlot = slot })
                                    Text(slot, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                // Pet Name & Problem
                item {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Pet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = problemNotes,
                        onValueChange = { problemNotes = it },
                        label = { Text("Symptoms / Reason for Consultation") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }

                // Confirm Button
                item {
                    Button(
                        onClick = {
                            onConfirm(consultType, petName, selectedDate, selectedSlot)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Booking (₹${fee.toInt()})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
