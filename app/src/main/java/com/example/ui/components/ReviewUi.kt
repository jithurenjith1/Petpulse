package com.petpulse.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

/** Gold used for filled stars across the ratings & reviews UI. */
private val ReviewStarGold = Color(0xFFA87A1F)

/** Formats an average rating to one decimal place, e.g. 4.5. */
fun formatRating(value: Double): String = (round(value * 10.0) / 10.0).toString()

/** Formats a review timestamp as "dd MMM yyyy" (empty string when unset). */
fun formatReviewDate(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

/** A row of five stars with [rating] of them filled. */
@Composable
fun StarRow(rating: Int, starSize: Int = 14) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= rating) ReviewStarGold else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

/**
 * Star picker + optional written review. Shared by product reviews (product detail)
 * and dealer / delivery-partner reviews (My Orders). Submit stays disabled until a
 * star (1-5) is chosen.
 */
@Composable
fun WriteReviewDialog(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.review_rating_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(R.string.review_star_content_description, i),
                                tint = if (i <= rating) ReviewStarGold else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.review_text_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, text.trim()) },
                enabled = rating in 1..5
            ) {
                Text(stringResource(R.string.review_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.community_cancel))
            }
        }
    )
}
