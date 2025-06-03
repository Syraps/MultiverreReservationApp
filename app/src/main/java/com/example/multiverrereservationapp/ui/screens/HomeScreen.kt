package com.example.multiverrereservationapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multiverrereservationapp.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit,
    onNavigateToReservation: () -> Unit,
    onNavigateToGameReservation: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit

) {
    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenue dans Multiverre !", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateToProfile) {
            Text("Mon Profil")
        }

        Button(onClick = onNavigateToReservation) {
            Text("Réserver une table")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToGameReservation) {
            Text("Réserver un jeu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Button(onClick = {
            authViewModel.signOut(context)
            onLogout()
        }) {
            Text("Se déconnecter")
        }
        if (authViewModel.isAdminChecked && authViewModel.isAdmin) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToAdminPanel) {
                Text("Panel Admin")
            }
        }

    }
}

