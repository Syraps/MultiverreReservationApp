package com.example.multiverrereservationapp.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.multiverrereservationapp.model.BoardGame
import com.example.multiverrereservationapp.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime

class ReservationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun saveReservation(
        game: String,
        players: Int,
        time: String,
        day: String,
        stock: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        Log.d("FirestoreTest", "Saving reservation for userId = $userId")

        if (userId == null) {
            onError("Utilisateur non connecté.")
            return
        }

        val reservation = Reservation(
            userId = userId,
            game = game,
            players = players,
            time = time,
            day = day
        )

        db.collection("reservations")
            .add(reservation)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Une erreur est survenue.")
            }
    }

    fun isBoardGameAvailable(
        gameName: String,
        day: String,
        time: String,
        durationMinutes: Int,
        stock: Int,
        onResult: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("reservations")
            .whereEqualTo("game", gameName)
            .whereEqualTo("day", day)
            .get()
            .addOnSuccessListener { result ->
                val requestedStart = LocalTime.parse(time)
                val requestedEnd = requestedStart.plusMinutes(durationMinutes.toLong())

                var overlapping = 0

                for (doc in result) {
                    val resTime = doc.getString("time") ?: continue
                    val resDuration = doc.getLong("durationMinutes")?.toInt() ?: continue

                    val resStart = LocalTime.parse(resTime)
                    val resEnd = resStart.plusMinutes(resDuration.toLong())

                    if (requestedStart < resEnd && resStart < requestedEnd) {
                        overlapping++
                    }
                }

                onResult(overlapping < stock)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erreur inconnue")
            }
    }

    fun notifyReservation(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val notification = NotificationCompat.Builder(context, "reservation_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    fun loadBoardGames(onResult: (List<BoardGame>) -> Unit, onError: (String) -> Unit) {
        db.collection("boardGames")
            .get()
            .addOnSuccessListener { result ->
                val games = result.mapNotNull { doc ->
                    try {
                        BoardGame(
                            name = doc.getString("name") ?: return@mapNotNull null,
                            minPlayers = doc.getLong("minPlayers")?.toInt() ?: return@mapNotNull null,
                            maxPlayers = doc.getLong("maxPlayers")?.toInt() ?: return@mapNotNull null,
                            durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: return@mapNotNull null,
                            stock = doc.getLong("stock")?.toInt() ?: return@mapNotNull null
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(games)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erreur lors du chargement des jeux")
            }
    }
}