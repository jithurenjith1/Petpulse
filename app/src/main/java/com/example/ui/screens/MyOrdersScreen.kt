package com.petpulse.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.data.model.AdminOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---- App palette ----
private val CoralPrimary = Color(0xFFBC5233)
private val CreamBg = Color(0xFFFBF6F0)
private val TealAccent = Color(0xFF1D7A6E)
private val TealLight = Color(0xFFD6EBE6)
private val AmberGold = Color(0xFFA87A1F)
private val AmberLight = Color(0xFFF6ECD8)
private val DarkText = Color(0xFF272220)
private val TextGray = Color(0xFF5C554F)
private val BorderColor = Color(0xFFE9DED4)
private val DeliveredGreen = Color(0xFF2E7D32)
private val GreenLight = Color(0xFFDDEEDC)

/**
 * My Orders screen — shows the signed-in customer's real Firestore orders
 * with LIVE status (Placed / Out for Delivery / Delivered) and a "Buy Again"
 * button that loads a past order's items back into the cart.
 */
@Composable
fun MyOrdersScreen(
    orders: List<AdminOrder>,
    onBuyAgain: (AdminOrder) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Surface(color = CreamBg, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkText)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("My Orders", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Live status updates", color = TealLight, fontSize = 11.sp)
                }
            }

            if (orders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = BorderColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No orders yet", color = DarkText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Your orders will appear here with live\ndelivery status after you shop. \uD83D\uDC3E",
                        color = TextGray,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        MyOrderCard(
                            order = order,
                            onBuyAgain = { onBuyAgain(order) },
                            onCallDealer = { phone ->
                                if (phone.isNotBlank()) {
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        )
                                    } catch (_: Exception) { }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyOrderCard(
    order: AdminOrder,
    onBuyAgain: () -> Unit,
    onCallDealer: (String) -> Unit
) {
    val dateText = if (order.createdAt > 0) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
    } else ""

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: order number + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "#${order.orderNumber}",
                        color = DarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (dateText.isNotEmpty()) {
                        Text(dateText, color = TextGray, fontSize = 11.sp)
                    }
                }
                StatusChip(status = order.status)
            }

            Spacer(Modifier.height(10.dp))

            // Items
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.name}  ×${item.quantity}",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("₹${(item.priceInr * item.quantity).toInt()}", color = TextGray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total (COD)", color = DarkText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("₹${order.totalInr.toInt()}", color = CoralPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Dealer row when assigned
            if (order.status == "ASSIGNED" && order.dealerName.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TealLight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delivery partner: ${order.dealerName}", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Your order is on the way!", color = TealAccent, fontSize = 11.sp)
                    }
                    if (order.dealerPhone.isNotBlank()) {
                        Button(
                            onClick = { onCallDealer(order.dealerPhone) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Buy Again
            Button(
                onClick = onBuyAgain,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buy Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, bg, fg) = when (status) {
        "NEW" -> Triple("Order Placed", AmberLight, AmberGold)
        "ASSIGNED" -> Triple("Out for Delivery", TealLight, TealAccent)
        "DELIVERED" -> Triple("Delivered \u2713", GreenLight, DeliveredGreen)
        else -> Triple(status, AmberLight, AmberGold)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = fg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
