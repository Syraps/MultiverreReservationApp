package com.example.multiverrereservationapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multiverrereservationapp.viewmodel.ReservationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserReservation(
    val game: String,
    val day: String,
    val time: String,
    val players: Int
)

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    reservationViewModel: ReservationViewModel = viewModel()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var reservations by remember { mutableStateOf<List<UserReservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("reservations")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { snapshot ->
                    reservations = snapshot.documents.mapNotNull { doc ->
                        val game = doc.getString("game") ?: return@mapNotNull null
                        val day = doc.getString("day") ?: return@mapNotNull null
                        val time = doc.getString("time") ?: return@mapNotNull null
                        val players = doc.getLong("players")?.toInt() ?: return@mapNotNull null
                        UserReservation(game, day, time, players)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text("Mon profil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (reservations.isEmpty()) {
                Text("Aucune réservation pour le moment.")
            } else {
                reservations.forEach { res ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jeu : ${res.game}")
                            Text("Jour : ${res.day}")
                            Text("Heure : ${res.time}")
                            Text("Joueurs : ${res.players}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onBack) {
            Text("Retour")
        }
    }
}
