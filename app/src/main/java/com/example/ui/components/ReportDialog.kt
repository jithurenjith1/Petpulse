package com.petpulse.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.R

// Canonical (stored) reason value -> localised label resource. The value is what
// gets written to Firestore; only the label is translated.
private val reportReasonOptions = listOf(
    "Spam" to R.string.report_reason_spam,
    "Inappropriate content" to R.string.report_reason_inappropriate,
    "Fraud or scam" to R.string.report_reason_fraud,
    "Animal cruelty" to R.string.report_reason_cruelty,
    "Other" to R.string.report_reason_other
)

/**
 * Shared "Report this content" dialog: the user picks ONE short reason and the
 * chosen canonical value is passed back to onSubmit. Used for community posts,
 * market listings and found reports.
 */
@Composable
fun ReportDialog(
    title: String = stringResource(R.string.report_title),
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(reportReasonOptions.first().first) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.report_pick_reason),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                reportReasonOptions.forEach { (value, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = value }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == value,
                            onClick = { selectedReason = value }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(labelRes), fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selectedReason) }) {
                Text(stringResource(R.string.report_submit), color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.admin_cancel))
            }
        }
    )
}
