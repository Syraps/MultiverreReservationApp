package com.example.multiverrereservationapp.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multiverrereservationapp.model.BoardGame
import com.example.multiverrereservationapp.viewmodel.ReservationViewModel
import com.example.multiverrereservationapp.ui.components.DatePickerWithLimit
import com.example.multiverrereservationapp.ui.components.formatReadable
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameReservationScreen(
    reservationViewModel: ReservationViewModel,
    onReservationSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var boardGames by remember { mutableStateOf<List<BoardGame>>(emptyList()) }
    var selectedGame by remember { mutableStateOf<BoardGame?>(null) }
    var expandedGameMenu by remember { mutableStateOf(false) }

    var playersInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val timeSlots = listOf(
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
        "20:00", "20:30", "21:00", "21:30"
    )
    var selectedTime by remember { mutableStateOf("") }
    var timeMenuExpanded by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Charger les jeux depuis Firestore au lancement
    LaunchedEffect(Unit) {
        reservationViewModel.loadBoardGames(
            onResult = { boardGames = it },
            onError = { errorMessage = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Réserver un jeu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Sélecteur de jeu
        ExposedDropdownMenuBox(
            expanded = expandedGameMenu,
            onExpandedChange = { expandedGameMenu = !expandedGameMenu }
        ) {
            OutlinedTextField(
                value = selectedGame?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Jeu de société") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedGameMenu,
                onDismissRequest = { expandedGameMenu = false }
            ) {
                boardGames.forEach { game ->
                    DropdownMenuItem(
                        text = { Text(game.name) },
                        onClick = {
                            selectedGame = game
                            expandedGameMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre de joueurs
        OutlinedTextField(
            value = playersInput,
            onValueChange = { playersInput = it },
            label = {
                Text("Nombre de joueurs" +
                        if (selectedGame != null)
                            " (${selectedGame!!.minPlayers}-${selectedGame!!.maxPlayers})"
                        else ""
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showDatePicker = true }) {
            Text(selectedDate?.formatReadable() ?: "Choisir une date")
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = timeMenuExpanded,
            onExpandedChange = { timeMenuExpanded = !timeMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Créneau horaire") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = timeMenuExpanded,
                onDismissRequest = { timeMenuExpanded = false }
            ) {
                timeSlots.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            selectedTime = time
                            timeMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val players = playersInput.toIntOrNull()

            if (selectedGame == null) {
                errorMessage = "Veuillez choisir un jeu"
                return@Button
            }
            if (selectedDate == null) {
                errorMessage = "Veuillez choisir une date"
                return@Button
            }
            if (selectedTime.isBlank()) {
                errorMessage = "Veuillez choisir une heure"
                return@Button
            }
            if (players == null || players < selectedGame!!.minPlayers || players > selectedGame!!.maxPlayers) {
                errorMessage = "Nombre de joueurs invalide"
                return@Button
            }

            val game = selectedGame!!
            val dateStr = selectedDate.toString()

            // 🔍 Log avant d'appeler la vérification de disponibilité
            Log.d("FlowTest", "Appel à isBoardGameAvailable() pour $game à $dateStr $selectedTime")

            reservationViewModel.isBoardGameAvailable(
                gameName = game.name,
                day = dateStr,
                time = selectedTime,
                durationMinutes = game.durationMinutes,
                stock = game.stock,
                onResult = { isAvailable ->
                    Log.d("FlowTest", "Disponible ? $isAvailable")
                    if (isAvailable) {
                        reservationViewModel.saveReservation(
                            game = game.name,
                            players = players,
                            time = selectedTime,
                            day = dateStr,
                            stock = game.stock,
                            onSuccess = {
                                showConfirmation = true
                                reservationViewModel.notifyReservation(
                                    context,
                                    "Réservation confirmée",
                                    "Votre réservation pour ${selectedDate.toString()} à $selectedTime a bien été enregistrée !"
                                )
                            },
                            onError = {
                                errorMessage = it
                                Log.e("FlowTest", "Erreur saveReservation: $it")
                            }
                        )
                    } else {
                        errorMessage = "Ce jeu n’est pas disponible à ce créneau"
                    }
                },
                onError = {
                    errorMessage = it
                    Log.e("FlowTest", "Erreur dans isBoardGameAvailable: $it")
                }
            )
        }) {
            Text("Réserver")
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onCancel) {
            Text("Annuler")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }

    if (showDatePicker) {
        DatePickerWithLimit(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showConfirmation && selectedGame != null && selectedDate != null) {
        val message = """
            Regarde ! Je viens de réserver \"${selectedGame!!.name}\" pour le ${selectedDate.toString()} à $selectedTime pour $playersInput joueurs.
            Rendez-vous au Multiverre au 15 rue de l'imaginaire à Fausseville !
        """.trimIndent()

        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
                onReservationSuccess()
            },
            title = { Text("Réservation confirmée") },
            text = { Text("Souhaitez-vous partager votre réservation ?") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(Intent.createChooser(intent, "Partager via..."))
                }) {
                    Text("Partager")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showConfirmation = false
                    onReservationSuccess()
                }) {
                    Text("Fermer")
                }
            }
        )
    }
}
