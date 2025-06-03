package com.example.multiverrereservationapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.multiverrereservationapp.model.BoardGame
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val game: String = "",
    val day: String = "",
    val time: String = "",
    val players: Int = 0
)

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun addBoardGame(game: BoardGame, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("boardGames")
            .add(game)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur lors de l'ajout") }
    }

    fun deleteBoardGame(documentId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("boardGames")
            .document(documentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur lors de la suppression") }
    }

    fun fetchBoardGames(onSuccess: (List<BoardGame>) -> Unit, onError: (String) -> Unit) {
        db.collection("boardGames")
            .get()
            .addOnSuccessListener { result ->
                val games = result.mapNotNull { doc ->
                    doc.toObject<BoardGame>().copy(documentId = doc.id)
                }
                onSuccess(games)
            }
            .addOnFailureListener { onError(it.message ?: "Erreur de chargement") }
    }

    // Lister toutes les réservations
    fun fetchAllReservations(onSuccess: (List<Reservation>) -> Unit, onError: (String) -> Unit) {
        db.collection("reservations")
            .get()
            .addOnSuccessListener { result ->
                val reservations = result.mapNotNull { doc ->
                    val game = doc.getString("game") ?: return@mapNotNull null
                    val day = doc.getString("day") ?: return@mapNotNull null
                    val time = doc.getString("time") ?: return@mapNotNull null
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val players = doc.getLong("players")?.toInt() ?: return@mapNotNull null
                    Reservation(
                        id = doc.id,
                        game = game,
                        day = day,
                        time = time,
                        userId = userId,
                        players = players
                    )
                }
                onSuccess(reservations)
            }
            .addOnFailureListener { onError(it.message ?: "Erreur lors du chargement des réservations") }
    }

    // Supprimer une réservation
    fun deleteReservation(reservationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("reservations").document(reservationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur lors de la suppression") }
    }
    fun updateStock(
        documentId: String,
        newStock: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("boardGames")
            .document(documentId)
            .update("stock", newStock)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur lors de la mise à jour du stock") }
    }

}
