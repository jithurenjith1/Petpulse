package com.petpulse.app.ui.screens

import androidx.compose.ui.res.stringResource
import com.petpulse.app.R

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.petpulse.app.data.model.*
import com.petpulse.app.ui.theme.*

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
    // v4 model: flat ₹40 delivery under ₹500, FREE at/above ₹500 (all Kerala cities)
    val freeThreshold = 500.0
    val baseFee = if (subtotal >= freeThreshold) 0.0 else 40.0
    val deliveryFee = if (subtotal == 0.0) 0.0 else baseFee + if (isExpress) 50.0 else 0.0
    val total = subtotal + deliveryFee

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
                            text = stringResource(R.string.modals_kerala_delivery_cart),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = onClearCart) {
                                Text(stringResource(R.string.modals_clear), color = Color(0xFFD32F2F), fontSize = 12.sp)
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
                                text = stringResource(R.string.modals_your_cart_is_empty),
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
                                        text = stringResource(R.string.modals_s2_hour_express_delivery),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.modals_direct_hub_courier_in_kochi_tvm_50),
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
                                                text = stringResource(R.string.modals_rx_required),
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
                                Text(stringResource(R.string.modals_items_subtotal), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text(stringResource(R.string.modals_petpulse_escrow_protection), fontSize = 12.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.modals_free), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.modals_total_amount), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
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
    // v4 model: flat ₹40 delivery under ₹500, FREE at/above ₹500 (all Kerala cities)
    val freeThreshold = 500.0
    val deliveryFee = if (subtotal >= freeThreshold) {
        if (isExpress) 50.0 else 0.0
    } else {
        if (isExpress) 40.0 + 50.0 else 40.0
    }
    val total = subtotal + deliveryFee

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
                                text = stringResource(R.string.modals_secure_escrow_checkout),
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
                                    text = stringResource(R.string.modals_petpulse_kerala_escrow_protection),
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
                        text = stringResource(R.string.modals_s1_kerala_delivery_address),
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
                        label = { Text(stringResource(R.string.modals_customer_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(stringResource(R.string.modals_contact_phone_91)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = streetAddress,
                        onValueChange = { streetAddress = it },
                        label = { Text(stringResource(R.string.modals_house_flat_no_street_landmark)) },
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
                                    text = stringResource(R.string.modals_veterinary_prescription_required),
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
                        text = stringResource(R.string.modals_s2_escrow_payment_method),
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
                                Text(stringResource(R.string.modals_total_escrow_amount), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${total.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimaryDark)
                                Text(
                                    "Items ₹${subtotal.toInt()} • Delivery: " + if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                                    fontSize = 11.sp,
                                    color = if (deliveryFee == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                Text(stringResource(R.string.modals_lock_place_order), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            text = stringResource(R.string.modals_kerala_orders_live_timeline),
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
                border = BorderStroke(1.dp, Color(0xFFF0D9A8))
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
                            text = stringResource(R.string.modals_doorstep_escrow_otp),
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
                    Text(stringResource(R.string.modals_call_rider), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.modals_live_order_timeline),
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
                text = stringResource(R.string.modals_items) + order.items.joinToString { "${it.title} x${it.quantity}" },
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
    onSubmit: (name: String, species: String, breed: String, age: String, gender: String, city: String, isExotic: Boolean, listingType: String, price: Double, desc: String, phone: String, photos: List<String>) -> Unit
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
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris -> photoUris = uris }

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
                            text = stringResource(R.string.modals_list_a_pet_for_sale_adoption),
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
                                Text(stringResource(R.string.modals_imported_exotic_pet), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(stringResource(R.string.modals_check_for_macaw_husky_iguana_persian_etc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isExotic, onCheckedChange = { isExotic = it })
                        }
                    }
                }

                // Pet Photos (multiple upload for better sales listings)
                item {
                    Text(stringResource(R.string.modals_pet_photos_up_to_5_tap_a_photo_to_remove), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoUris.isEmpty()) "Add Photos" else "${photoUris.size}/3 selected")
                    }
                    if (photoUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(photoUris.size) { idx ->
                                AsyncImage(
                                    model = photoUris[idx],
                                    contentDescription = "Pet photo ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            photoUris = photoUris.filterIndexed { i, _ -> i != idx }
                                        }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text(stringResource(R.string.modals_pet_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text(stringResource(R.string.modals_species), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        label = { Text(stringResource(R.string.modals_breed_e_g_siberian_husky_indie_persian)) },
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
                            label = { Text(stringResource(R.string.modals_age)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text(stringResource(R.string.modals_gender)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Text(stringResource(R.string.modals_kerala_city), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Text(stringResource(R.string.modals_listing_type), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = listingType == "Sale",
                            onClick = { listingType = "Sale" },
                            label = { Text(stringResource(R.string.modals_sale_inr), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = listingType == "Adoption",
                            onClick = { listingType = "Adoption" },
                            label = { Text(stringResource(R.string.modals_free_for_adoption), fontSize = 12.sp) }
                        )
                    }
                }

                if (listingType == "Sale") {
                    item {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text(stringResource(R.string.modals_price_in_inr)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.modals_contact_phone_91)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.modals_pet_description_temperament)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )
                }

                item {
                    Button(
                        onClick = {
                            val priceVal = priceText.toDoubleOrNull() ?: 0.0
                            onSubmit(petName, species, breed, age, gender, city, isExotic, listingType, priceVal, description, phone, photoUris.map { it.toString() })
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
                        Text(stringResource(R.string.modals_post_pet_listing_with_escrow_protection), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                text = stringResource(R.string.modals_kerala_vet_doctor_registration),
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
                        label = { Text(stringResource(R.string.modals_doctor_full_name_e_g_dr_anoop_kumar)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = ksvcNumber,
                        onValueChange = { ksvcNumber = it },
                        label = { Text(stringResource(R.string.modals_kerala_state_vet_council_ksvc_reg_number)) },
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
                            label = { Text(stringResource(R.string.modals_qualifications)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = experienceText,
                            onValueChange = { experienceText = it },
                            label = { Text(stringResource(R.string.modals_exp_years)) },
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
                        label = { Text(stringResource(R.string.modals_hospital_clinic_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text(stringResource(R.string.modals_city_district_in_kerala), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        label = { Text(stringResource(R.string.modals_clinic_address_landmark)) },
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
                            label = { Text(stringResource(R.string.modals_video_fee)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = inPersonFeeText,
                            onValueChange = { inPersonFeeText = it },
                            label = { Text(stringResource(R.string.modals_clinic_fee)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.modals_official_phone_whatsapp_91)) },
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
                        Text(stringResource(R.string.modals_register_verify_ksvc_profile), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    defaultPhone: String,
    onDismiss: () -> Unit,
    onConfirm: (consultType: String, petName: String, phone: String, date: String, slot: String, notes: String) -> Unit
) {
    var consultType by remember { mutableStateOf("Video Consultation") }
    var petName by remember { mutableStateOf(defaultPetName) }
    var customerPhone by remember { mutableStateOf(defaultPhone) }
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
                                text = stringResource(R.string.modals_book_doctor_appointment),
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
                    Text(stringResource(R.string.modals_s1_choose_consultation_mode), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                Text(stringResource(R.string.modals_video_consult), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                Text(stringResource(R.string.modals_in_clinic_visit), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${doctor.inPersonConsultFeeInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00796B))
                            }
                        }
                    }
                }

                // Date Picker
                item {
                    Text(stringResource(R.string.modals_s2_select_date), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    Text(stringResource(R.string.modals_s3_available_time_slot), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

                // Pet Name, Phone & Problem
                item {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text(stringResource(R.string.modals_pet_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Your Phone Number") },
                        placeholder = { Text("10-digit mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = problemNotes,
                        onValueChange = { problemNotes = it },
                        label = { Text(stringResource(R.string.modals_symptoms_reason_for_consultation)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }

                // Confirm Button
                item {
                    Button(
                        onClick = {
                            onConfirm(consultType, petName, customerPhone, selectedDate, selectedSlot, problemNotes)
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

/**
 * Trainer On-Demand request dialog — creates a real Firestore booking
 * that appears in the admin panel, plus the admin can call the customer.
 */
@Composable
fun TrainerRequestModal(
    defaultPetName: String,
    defaultPhone: String,
    onDismiss: () -> Unit,
    onConfirm: (petName: String, phone: String, trainingNeed: String) -> Unit
) {
    var petName by remember { mutableStateOf(defaultPetName) }
    var customerPhone by remember { mutableStateOf(defaultPhone) }
    var trainingNeed by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Request a Trainer", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Certified trainer for home obedience & behavior sessions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Your Phone Number") },
                    placeholder = { Text("10-digit mobile number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = trainingNeed,
                    onValueChange = { trainingNeed = it },
                    label = { Text("Training Needed (optional)") },
                    placeholder = { Text("e.g. Basic obedience, potty training, barking control") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onConfirm(petName, customerPhone, trainingNeed)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Trainer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Our team will call you to confirm the trainer and schedule.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
