package com.petpulse.app.ui.components

/**
 * Hides a partner's phone number from the customer.
 *
 * Partners give us their number to run their business, not to be called at 11pm
 * by strangers. Every customer-facing screen shows the masked form, and the
 * customer reaches the partner through a booking instead of a direct dial.
 *
 * "+91 98765 43210" -> "+91 98XXX XX210"
 */
fun maskPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.length < 10) return "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"
    val last10 = digits.takeLast(10)
    val first2 = last10.substring(0, 2)
    val last3 = last10.takeLast(3)
    return "+91 " + first2 + "XXX XX" + last3
}
