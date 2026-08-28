package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private var googleSignInClient: GoogleSignInClient? = null

    init {
        val currentUser = auth.currentUser
        _authState.value = _authState.value.copy(user = currentUser, isAuthenticated = currentUser != null)
    }

    fun signInWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _authState.value = AuthState(isLoading = false, user = result.user, isAuthenticated = true)
            }
            .addOnFailureListener { exception ->
                _authState.value = _authState.value.copy(isLoading = false, error = exception.localizedMessage ?: "Sign-in failed")
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _authState.value = AuthState(isLoading = false, user = result.user, isAuthenticated = true)
            }
            .addOnFailureListener { exception ->
                _authState.value = _authState.value.copy(isLoading = false, error = exception.localizedMessage ?: "Sign-up failed")
            }
    }

    fun getGoogleSignInIntent(context: Context): Intent? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("218116951607-j51mjfs8uslriun4id6qtgqg0k7akpjd.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient?.signInIntent
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        if (account == null) {
            _authState.value = _authState.value.copy(isLoading = false, error = "Google sign-in cancelled")
            return
        }
        val idToken = account.idToken
        if (idToken == null) {
            _authState.value = _authState.value.copy(isLoading = false, error = "Failed to get ID token")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                _authState.value = AuthState(isLoading = false, user = result.user, isAuthenticated = true)
            }
            .addOnFailureListener { exception ->
                _authState.value = _authState.value.copy(isLoading = false, error = exception.localizedMessage ?: "Google sign-in failed")
            }
    }

    fun signOut() {
        googleSignInClient?.signOut()
        auth.signOut()
        _authState.value = AuthState()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
