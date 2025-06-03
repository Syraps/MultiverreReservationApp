package com.example.multiverrereservationapp.model

data class BoardGame(
    val name: String = "",
    val minPlayers: Int = 0,
    val maxPlayers: Int = 0,
    val durationMinutes: Int = 0,
    val stock: Int = 0,
    val documentId: String? = null
)

