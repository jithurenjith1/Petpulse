package com.example.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

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

    init {
        val currentUser = auth.currentUser
        _authState.value = _authState.value.copy(
            user = currentUser,
            isAuthenticated = currentUser != null
        )
    }

    fun signInWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _authState.value = AuthState(
                    isLoading = false,
                    user = result.user,
                    isAuthenticated = true
                )
            }
            .addOnFailureListener { exception ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = exception.localizedMessage ?: "Sign-in failed"
                )
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _authState.value = AuthState(
                    isLoading = false,
                    user = result.user,
                    isAuthenticated = true
                )
            }
            .addOnFailureListener { exception ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = exception.localizedMessage ?: "Sign-up failed"
                )
            }
    }

    suspend fun signInWithGoogle(context: Context) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId("218116951607")
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            val idToken = credential.data.getString("googleIdToken")
                ?: throw Exception("No ID token found in credential")

            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()

            _authState.value = AuthState(
                isLoading = false,
                user = authResult.user,
                isAuthenticated = true
            )
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = e.localizedMessage ?: "Google sign-in failed"
            )
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
