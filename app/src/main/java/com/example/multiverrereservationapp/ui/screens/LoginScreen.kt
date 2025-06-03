package com.example.multiverrereservationapp.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import android.util.Log
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multiverrereservationapp.viewmodel.AuthViewModel
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext
import com.example.multiverrereservationapp.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val callbackManager = remember { CallbackManager.Factory.create() }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                authViewModel.signInWithGoogle(idToken) { success ->
                    if (success) onLoginSuccess()
                    else errorMessage = "Échec de la connexion avec Google"
                }
            } else {
                errorMessage = "Token Google non disponible"
            }
        } catch (e: Exception) {
            errorMessage = "Erreur Google Sign-In : ${e.message}"
        }
    }

    fun launchGoogleSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenue", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = passwordVisible,
                onCheckedChange = { passwordVisible = it }
            )
            Text("Afficher le mot de passe")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { launchGoogleSignIn(context) }) {
            Text("Connexion avec Google")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            LoginManager.getInstance().logInWithReadPermissions(
                context as Activity,
                listOf("email", "public_profile")
            )

            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        authViewModel.signInWithFacebook(result.accessToken) { success ->
                            if (success) onLoginSuccess()
                            else errorMessage = "Échec de la connexion Facebook"
                        }
                    }

                    override fun onCancel() {
                        errorMessage = "Connexion Facebook annulée"
                    }

                    override fun onError(error: FacebookException) {
                        errorMessage = "Erreur Facebook : ${error.message}"
                    }
                })
        }) {
            Text("Connexion avec Facebook")
        }

        Button(onClick = {
            authViewModel.login(email, password) { success ->
                if (success) onLoginSuccess()
                else errorMessage = "Connexion échouée"
            }
        }) {
            Text("Se connecter")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Email et mot de passe requis"
                return@Button
            }
            authViewModel.register(email, password) { success ->
                if (success) onLoginSuccess()
                else errorMessage = "Inscription échouée"
            }
        }) {
            Text("Créer un compte")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
