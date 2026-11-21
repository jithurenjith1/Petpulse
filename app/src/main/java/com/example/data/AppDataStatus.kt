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
    val isSessionExpired: Boolean = false,
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
    private val beginTimes = java.util.concurrent.ConcurrentHashMap<String, Long>()
    val loadingKeys: StateFlow<Set<String>> = _loadingKeys.asStateFlow()

    fun begin(key: String) {
        beginTimes[key] = System.currentTimeMillis()
        _loadingKeys.update { it + key }
    }

    fun end(key: String) {
        beginTimes.remove(key)
        _loadingKeys.update { it - key }
    }

    /**
     * Drops keys that have been pending for too long.
     *
     * A listener whose first read fails now emits nothing (so the previous data is
     * kept instead of being wiped), which means it may never clear its own key.
     * This guard keeps the progress bar from sticking forever.
     */
    fun expireStale(maxAgeMs: Long = 8000L) {
        val now = System.currentTimeMillis()
        val stale = beginTimes.filterValues { now - it > maxAgeMs }.keys.toSet()
        if (stale.isEmpty()) return
        stale.forEach { beginTimes.remove(it) }
        _loadingKeys.update { it - stale }
    }

    fun isLoading(key: String): Boolean = _loadingKeys.value.contains(key)

    private val _issue = MutableStateFlow<DataIssue?>(null)
    val issue: StateFlow<DataIssue?> = _issue.asStateFlow()

    /**
     * Report a read failure. Safe to call from any thread; called from Firestore
     * listener callbacks, which run on the background executor.
     */
    // Last issue shown, used to suppress repeats of the same message.
    @Volatile
    private var lastReport: DataIssue? = null

    fun report(throwable: Throwable?) {
        if (throwable == null) return
        val code = (throwable as? FirebaseFirestoreException)?.code

        // Only a genuinely invalid credential means the session is over.
        // A plain PERMISSION_DENIED is a Firestore rules decision (or a failed
        // App Check), NOT a logout. Treating the two the same is what produced a
        // "Session expired" dialog for a customer who was still signed in.
        val sessionExpired = code == FirebaseFirestoreException.Code.UNAUTHENTICATED
        val denied = code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        val offline = code == FirebaseFirestoreException.Code.UNAVAILABLE

        val message = when {
            sessionExpired -> "Your session has ended. Please sign in again."
            denied -> "Some content isn't available for your account right now."
            offline -> "You're offline — we couldn't refresh this."
            else -> throwable.localizedMessage
                ?.takeIf { it.isNotBlank() }
                ?.let { "Couldn't load: $it" }
                ?: "Couldn't load this right now."
        }

        // One denied listener must not spam the UI with the same line every retry.
        val now = System.currentTimeMillis()
        val previous = lastReport
        if (previous != null && previous.message == message && now - previous.timestamp < 20_000L) {
            return
        }

        val issue = DataIssue(
            message = message,
            isSessionExpired = sessionExpired,
            isOffline = offline
        )
        lastReport = issue
        _issue.value = issue
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
