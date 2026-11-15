package com.petpulse.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.data.AppDataStatus

/**
 * Shared state views for the whole app.
 *
 * Every screen should be able to answer four questions: is it loading, did it fail,
 * is the phone offline, and is there simply nothing here. Before this file only the
 * last one existed, so a failed Firestore listener and an empty catalogue looked
 * identical to the user.
 */

// ---------------------------------------------------------------- LOADING
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading…"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------- ERROR
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    message: String = "We could not load this right now. Please try again.",
    retryLabel: String = "Try again",
    onRetry: (() -> Unit)? = null
) {
    StateScaffold(
        modifier = modifier,
        emoji = "⚠️",
        title = title,
        message = message,
        actionLabel = retryLabel,
        onAction = onRetry
    )
}

// ---------------------------------------------------------------- NO INTERNET
@Composable
fun NoInternetState(
    modifier: Modifier = Modifier,
    title: String = "You're offline",
    message: String = "Check your mobile data or Wi-Fi, then try again.",
    onRetry: (() -> Unit)? = null
) {
    StateScaffold(
        modifier = modifier,
        emoji = "📡",
        title = title,
        message = message,
        actionLabel = "Try again",
        onAction = onRetry
    )
}

// ---------------------------------------------------------------- PERMISSION DENIED
@Composable
fun PermissionDeniedState(
    modifier: Modifier = Modifier,
    title: String = "Permission needed",
    message: String = "Wagmiya needs this permission to do that. You can allow it in Settings.",
    actionLabel: String = "Open settings",
    onOpenSettings: (() -> Unit)? = null
) {
    StateScaffold(
        modifier = modifier,
        emoji = "🔒",
        title = title,
        message = message,
        actionLabel = actionLabel,
        onAction = onOpenSettings
    )
}

// ---------------------------------------------------------------- SUCCESS
@Composable
fun SuccessState(
    modifier: Modifier = Modifier,
    title: String = "All done",
    message: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StateScaffold(
        modifier = modifier,
        emoji = "✅",
        title = title,
        message = message,
        actionLabel = actionLabel,
        onAction = onAction
    )
}

// ---------------------------------------------------------------- shared body
@Composable
private fun StateScaffold(
    modifier: Modifier,
    emoji: String,
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 30.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (message.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- LOADING BAR
/**
 * Thin indeterminate bar shown while any repository listener is still waiting for
 * its first snapshot. Sits under the top bar, costs 3dp when idle (it renders
 * nothing at all), and never blocks the screen.
 */
@Composable
fun LoadingBar(modifier: Modifier = Modifier) {
    val loadingKeys by AppDataStatus.loadingKeys.collectAsState()

    // Self-healing: a listener that fails before its first snapshot emits nothing,
    // so it can never clear its own key. Drop stale keys after a few seconds.
    LaunchedEffect(loadingKeys) {
        if (loadingKeys.isNotEmpty()) {
            kotlinx.coroutines.delay(8000)
            AppDataStatus.expireStale(8000)
        }
    }

    if (loadingKeys.isEmpty()) return

    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    )
}

// ---------------------------------------------------------------- OFFLINE BANNER
/**
 * Slim, non-blocking banner pinned to the top of the app shell.
 * Renders nothing while the device is online.
 */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    val isOnline = rememberIsOnline()
    if (isOnline) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFB3261E)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📡", fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "You're offline — some things may not load",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------- PARTIAL DATA
/**
 * Honest footnote for lists that are capped (Firestore listen limits, "showing first N").
 * Never claim the list is complete when it is not.
 */
@Composable
fun PartialDataNote(
    text: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (onSeeAll != null) {
                Text(
                    text = "See all",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(onClick = onSeeAll)
                        .padding(start = 10.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- SESSION EXPIRED
@Composable
fun SessionExpiredDialog(
    onSignIn: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Session expired") },
        text = {
            Text(
                "For your security you were signed out. Sign in again to continue.",
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onSignIn) { Text("Sign in") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )
}

// ---------------------------------------------------------------- INLINE FIELD ERROR
@Composable
fun InlineFieldError(
    message: String?,
    modifier: Modifier = Modifier
) {
    if (message.isNullOrBlank()) return
    Text(
        text = message,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(top = 4.dp)
    )
}

// ---------------------------------------------------------------- INLINE FORM BANNER
@Composable
fun FormErrorBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    if (message.isNullOrBlank()) return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
