package com.petpulse.app.data

import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

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

    /**
     * Keys of listeners that have been started but have not produced their first
     * emission yet. This is the only honest way to tell "still loading" apart from
     * "loaded, and genuinely empty" — both used to look like an empty list.
     */
    private val _loadingKeys = MutableStateFlow<Set<String>>(emptySet())
    val loadingKeys: StateFlow<Set<String>> = _loadingKeys.asStateFlow()

    fun begin(key: String) {
        _loadingKeys.update { it + key }
    }

    fun end(key: String) {
        _loadingKeys.update { it - key }
    }

    fun isLoading(key: String): Boolean = _loadingKeys.value.contains(key)

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

/**
 * Marks a repository flow as "loading" until its first value arrives.
 *
 * callbackFlow never completes, so the flag is cleared on the first emission and
 * again in `finally` (when the collector leaves the screen), which keeps the
 * indicator from getting stuck if a listener is torn down before it emits.
 */
fun <T> Flow<T>.trackLoading(key: String): Flow<T> = flow {
    val source = this@trackLoading
    AppDataStatus.begin(key)
    var firstEmission = true
    try {
        source.collect { value ->
            if (firstEmission) {
                firstEmission = false
                AppDataStatus.end(key)
            }
            emit(value)
        }
    } finally {
        AppDataStatus.end(key)
    }
}
