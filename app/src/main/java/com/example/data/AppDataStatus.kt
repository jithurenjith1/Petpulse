package com.petpulse.app.data

import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One line of communication from the data layer to the UI.
 *
 * Before this existed, every Firestore listener caught its error, logged it and
 * emitted emptyList(). The user then saw the same thing for "catalogue is empty"
 * and "your read failed", which is why a broken connection looked like a broken app.
 */
data class DataIssue(
    val message: String,
    val isPermissionDenied: Boolean = false,
    val isOffline: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object AppDataStatus {

    private val _issue = MutableStateFlow<DataIssue?>(null)
    val issue: StateFlow<DataIssue?> = _issue.asStateFlow()

    /**
     * Report a read failure. Safe to call from any thread; called from Firestore
     * listener callbacks, which run on the background executor.
     */
    fun report(throwable: Throwable?) {
        if (throwable == null) return
        val code = (throwable as? FirebaseFirestoreException)?.code
        val permissionDenied = code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
            code == FirebaseFirestoreException.Code.UNAUTHENTICATED
        val offline = code == FirebaseFirestoreException.Code.UNAVAILABLE
        val message = when {
            permissionDenied -> "You no longer have access to this. Please sign in again."
            offline -> "You're offline — we couldn't refresh this."
            else -> throwable.localizedMessage
                ?.takeIf { it.isNotBlank() }
                ?.let { "Couldn't load: $it" }
                ?: "Couldn't load this right now."
        }
        _issue.value = DataIssue(
            message = message,
            isPermissionDenied = permissionDenied,
            isOffline = offline
        )
    }

    fun clear() {
        _issue.value = null
    }
}
