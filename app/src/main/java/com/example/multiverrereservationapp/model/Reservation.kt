package com.example.multiverrereservationapp.model


data class Reservation(
    val userId: String = "",
    val game: String = "",
    val players: Int = 0,
    val time: String = "",
    val day: String = ""
)