package com.petpulse.app.ui.components

import android.content.Context
import android.content.Intent
import com.petpulse.app.data.model.AdminOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A plain order receipt the customer can save or forward on WhatsApp.
 *
 * This is deliberately NOT a tax invoice. Wagmiya's GST registration is not active
 * yet, so printing "invoice" or a GSTIN on it would be a false document. Once the
 * registration is live and orders are billed in Wagmiya's own name, this is replaced
 * by a real tax invoice - the shape below already has every field that needs.
 */
fun buildOrderReceipt(order: AdminOrder): String {
    val sb = StringBuilder()
    sb.appendLine("WAGMIYA - ORDER RECEIPT")
    sb.appendLine("--------------------------------")
    sb.appendLine("Order   : " + order.orderNumber)
    if (order.createdAt > 0L) {
        val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sb.appendLine("Date    : " + df.format(Date(order.createdAt)))
    }
    if (order.status.isNotBlank()) sb.appendLine("Status  : " + order.status)
    sb.appendLine()
    sb.appendLine("ITEMS")
    var subtotal = 0.0
    for (item in order.items) {
        val line = item.priceInr * item.quantity
        subtotal += line
        sb.appendLine("  " + item.quantity + " x " + item.name)
        sb.appendLine("      Rs." + money(line))
    }
    sb.appendLine()
    sb.appendLine("Subtotal      Rs." + money(subtotal))
    val extra = order.totalInr - subtotal
    if (extra > 0.5) sb.appendLine("Delivery      Rs." + money(extra))
    if (extra < -0.5) sb.appendLine("Discount      -Rs." + money(-extra))
    sb.appendLine("TOTAL         Rs." + money(order.totalInr))
    sb.appendLine("Payment       Cash on delivery")
    sb.appendLine()
    if (order.dealerName.isNotBlank()) {
        sb.appendLine("SUPPLIED BY")
        sb.appendLine("  " + order.dealerName)
        sb.appendLine()
    }
    sb.appendLine("DELIVER TO")
    if (order.customerName.isNotBlank()) sb.appendLine("  " + order.customerName)
    if (order.address.isNotBlank()) sb.appendLine("  " + order.address)
    if (order.city.isNotBlank()) sb.appendLine("  " + order.city)
    sb.appendLine()
    sb.appendLine("This is an order receipt, not a tax invoice.")
    sb.appendLine("Wagmiya  |  +91 95266 32311  |  itspetpulse@gmail.com")
    return sb.toString()
}

/** Opens the Android share sheet with the receipt as plain text. */
fun shareOrderReceipt(context: Context, order: AdminOrder) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Wagmiya order receipt " + order.orderNumber)
        putExtra(Intent.EXTRA_TEXT, buildOrderReceipt(order))
    }
    runCatching {
        context.startActivity(Intent.createChooser(send, "Share receipt"))
    }
}

private fun money(value: Double): String {
    val r = Math.round(value * 100.0) / 100.0
    return if (r % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%,.0f", r)
    } else {
        String.format(Locale.getDefault(), "%,.2f", r)
    }
}
