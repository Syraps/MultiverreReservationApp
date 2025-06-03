package com.example.multiverrereservationapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multiverrereservationapp.ui.components.formatReadable
import com.example.multiverrereservationapp.viewmodel.ReservationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val reservationViewModel: ReservationViewModel = viewModel()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var players by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val today = LocalDate.now()
    val twoWeeksLater = today.plusDays(14)

    val timeSlots = listOf(
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
        "20:00", "20:30", "21:00", "21:30"
    )
    var selectedTime by remember { mutableStateOf("") }
    var timeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Réserver une table", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = players,
            onValueChange = { players = it },
            label = { Text("Nombre de personnes") },
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
                label = { Text("Heure souhaitée") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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
            val playerCount = players.toIntOrNull()
            if (playerCount == null || playerCount <= 0) {
                errorMessage = "Veuillez entrer un nombre valide"
                return@Button
            }

            val day = selectedDate?.toString() ?: LocalDate.now().toString()

            reservationViewModel.saveReservation(
                game = "Juste une table",
                players = playerCount,
                time = selectedTime,
                day = day,
                stock = 0,
                onSuccess = {
                    showConfirmation = true
                    reservationViewModel.notifyReservation(
                        context,
                        "Réservation confirmée",
                        "Votre réservation pour $day à $selectedTime a bien été enregistrée !"
                    )
                },
                onError = { errorMessage = it }
            )
        }) {
            Text("Confirmer la réservation")
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
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    val picked = millis?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    if (picked != null && picked in today..twoWeeksLater) {
                        selectedDate = picked
                        showDatePicker = false
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showConfirmation && selectedDate != null) {
        val context = LocalContext.current
        val message = """
            Regarde ! Je viens de réserver \"Catan\" pour le ${selectedDate.toString()} à $selectedTime pour $players joueurs.
            Rendez-vous au Multiverre au 15 rue de l'imaginaire à Fausseville !
        """.trimIndent()

        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
                onConfirm()
            },
            title = { Text("Réservation confirmée !") },
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
                    onConfirm()
                }) {
                    Text("Fermer")
                }
            }
        )
    }
}
