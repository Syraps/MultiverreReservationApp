package com.example.multiverrereservationapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.multiverrereservationapp.model.BoardGame
import com.example.multiverrereservationapp.viewmodel.ReservationViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameReservationForm(
    availableGames: List<BoardGame>,
    reservationViewModel: ReservationViewModel,
    onSubmit: (selectedGame: BoardGame, players: Int, time: String, day: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGame by remember { mutableStateOf<BoardGame?>(null) }
    var expandedGameMenu by remember { mutableStateOf(false) }

    var selectedTime by remember { mutableStateOf("") }
    var expandedTimeMenu by remember { mutableStateOf(false) }

    var playersInput by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var availabilityMessage by remember { mutableStateOf<String?>(null) }

    val timeSlots = listOf(
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
        "20:00", "20:30", "21:00", "21:30"
    )

    Column(modifier = modifier.padding(16.dp)) {
        Text("Réserver un jeu", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Sélecteur de date
        Button(onClick = { showDatePicker = true }) {
            Text("Jour : ${selectedDate.formatReadable()}")
        }

        if (showDatePicker) {
            DatePickerWithLimit(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onDismiss = { showDatePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu déroulant des jeux
        ExposedDropdownMenuBox(
            expanded = expandedGameMenu,
            onExpandedChange = { expandedGameMenu = !expandedGameMenu }
        ) {
            OutlinedTextField(
                value = selectedGame?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Jeu") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedGameMenu,
                onDismissRequest = { expandedGameMenu = false }
            ) {
                availableGames.forEach { game ->
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

        // Menu déroulant de l'heure
        ExposedDropdownMenuBox(
            expanded = expandedTimeMenu,
            onExpandedChange = { expandedTimeMenu = !expandedTimeMenu }
        ) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Créneau horaire") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedTimeMenu,
                onDismissRequest = { expandedTimeMenu = false }
            ) {
                timeSlots.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            selectedTime = time
                            expandedTimeMenu = false
                        }
                    )
                }
            }
        }

        // Afficher la disponibilité du jeu sélectionné
        LaunchedEffect(selectedGame, selectedTime, selectedDate) {
            if (selectedGame != null && selectedTime.isNotBlank()) {
                reservationViewModel.isBoardGameAvailable(
                    gameName = selectedGame!!.name,
                    day = selectedDate.toString(),
                    time = selectedTime,
                    durationMinutes = selectedGame!!.durationMinutes,
                    stock = selectedGame!!.stock,
                    onResult = { available ->
                        availabilityMessage = if (available) "Jeu disponible" else "Plus de copies disponibles"
                    },
                    onError = { availabilityMessage = "Erreur : $it" }
                )
            }
        }

        availabilityMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val players = playersInput.toIntOrNull()

            when {
                selectedGame == null -> errorMessage = "Veuillez choisir un jeu"
                selectedTime.isBlank() -> errorMessage = "Veuillez choisir une heure"
                players == null -> errorMessage = "Nombre de joueurs invalide"
                players < selectedGame!!.minPlayers || players > selectedGame!!.maxPlayers ->
                    errorMessage = "Ce jeu est prévu pour ${selectedGame!!.minPlayers} à ${selectedGame!!.maxPlayers} joueurs"
                else -> {
                    errorMessage = null
                    onSubmit(selectedGame!!, players, selectedTime, selectedDate.toString())
                }
            }
        }) {
            Text("Réserver")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
