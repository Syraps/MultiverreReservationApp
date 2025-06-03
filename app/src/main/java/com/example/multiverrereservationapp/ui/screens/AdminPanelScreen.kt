package com.example.multiverrereservationapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.multiverrereservationapp.model.BoardGame
import com.example.multiverrereservationapp.viewmodel.AdminViewModel
import com.google.firebase.firestore.FirebaseFirestore

data class ReservationItem(
    val userId: String = "",
    val game: String = "",
    val day: String = "",
    val time: String = "",
    val players: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel = AdminViewModel(),
    onBack: () -> Unit
) {
    var boardGames by remember { mutableStateOf<List<BoardGame>>(emptyList()) }
    var reservations by remember { mutableStateOf<List<ReservationItem>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var minPlayers by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf(0) }

    fun refreshGames() {
        adminViewModel.fetchBoardGames(
            onSuccess = { boardGames = it },
            onError = { errorMessage = it }
        )
    }

    fun refreshReservations() {
        FirebaseFirestore.getInstance().collection("reservations")
            .get()
            .addOnSuccessListener { snapshot ->
                reservations = snapshot.documents.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val game = doc.getString("game") ?: return@mapNotNull null
                    val day = doc.getString("day") ?: return@mapNotNull null
                    val time = doc.getString("time") ?: return@mapNotNull null
                    val players = doc.getLong("players")?.toInt() ?: return@mapNotNull null
                    ReservationItem(userId, game, day, time, players)
                }
            }
            .addOnFailureListener {
                errorMessage = "Erreur chargement réservations : ${it.message}"
            }
    }

    LaunchedEffect(Unit) {
        refreshGames()
        refreshReservations()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Panel Administrateur", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Jeux enregistrés", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Réservations", modifier = Modifier.padding(16.dp))
            }
        }

        when (selectedTab) {
            0 -> {
                boardGames.forEach { game ->
                    var updatedStock by remember { mutableStateOf(game.stock.toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nom : ${game.name}")
                            Text("Joueurs : ${game.minPlayers} - ${game.maxPlayers}")
                            Text("Durée : ${game.durationMinutes} min")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = updatedStock,
                                    onValueChange = { updatedStock = it },
                                    label = { Text("Stock") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    val stockVal = updatedStock.toIntOrNull()
                                    if (stockVal != null && game.documentId != null) {
                                        adminViewModel.updateStock(
                                            documentId = game.documentId,
                                            newStock = stockVal,
                                            onSuccess = { refreshGames() },
                                            onError = { errorMessage = it }
                                        )
                                    }
                                }) {
                                    Text("Mettre à jour")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    game.documentId?.let {
                                        adminViewModel.deleteBoardGame(
                                            documentId = it,
                                            onSuccess = { refreshGames() },
                                            onError = { errorMessage = it }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Supprimer")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Ajouter un nouveau jeu", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                OutlinedTextField(value = minPlayers, onValueChange = { minPlayers = it }, label = { Text("Min joueurs") })
                OutlinedTextField(value = maxPlayers, onValueChange = { maxPlayers = it }, label = { Text("Max joueurs") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durée (min)") })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    val min = minPlayers.toIntOrNull()
                    val max = maxPlayers.toIntOrNull()
                    val dur = duration.toIntOrNull()
                    val stk = stock.toIntOrNull()

                    if (name.isBlank() || min == null || max == null || dur == null || stk == null) {
                        errorMessage = "Tous les champs doivent être valides."
                        return@Button
                    }

                    adminViewModel.addBoardGame(
                        BoardGame(name, min, max, dur, stk),
                        onSuccess = {
                            name = ""
                            minPlayers = ""
                            maxPlayers = ""
                            duration = ""
                            stock = ""
                            refreshGames()
                        },
                        onError = { errorMessage = it }
                    )
                }) {
                    Text("Ajouter le jeu")
                }
            }

            1 -> {
                reservations.forEach { res ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Utilisateur : ${res.userId}")
                            Text("Jeu : ${res.game}")
                            Text("Jour : ${res.day}")
                            Text("Heure : ${res.time}")
                            Text("Joueurs : ${res.players}")
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) {
            Text("Retour")
        }
    }
}
