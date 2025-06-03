package com.example.multiverrereservationapp.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<FirebaseAuth?>(null).apply {
        value = if (auth.currentUser != null) auth else null
    }
    val authState = _authState.asStateFlow()
    var isAdmin by mutableStateOf(false)
        private set
    var isAdminChecked by mutableStateOf(false)
        private set

    fun checkAdminStatus() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val isAdminFlag = document.getBoolean("isAdmin") ?: false
                isAdmin = isAdminFlag
                isAdminChecked = true
            }
            .addOnFailureListener {
                isAdmin = false
                isAdminChecked = true
            }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _authState.value = if (task.isSuccessful) auth else null
                onResult(task.isSuccessful)
            }
    }

    fun signInWithFacebook(token: AccessToken, onResult: (Boolean) -> Unit) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _authState.value = if (task.isSuccessful) auth else null
                onResult(task.isSuccessful)
            }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    _authState.value = if (it.isSuccessful) auth else null
                    onResult(it.isSuccessful)
                }
        }
    }

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    _authState.value = if (it.isSuccessful) auth else null
                    onResult(it.isSuccessful)
                }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = null
    }

    fun signOut(context: Context) {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            )
            googleSignInClient.signOut()
        }
        auth.signOut()
        _authState.value = null
    }

    fun updateStock(documentId: String, newStock: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("boardGames")
            .document(documentId)
            .update("stock", newStock)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur mise à jour stock") }
    }
}
