package com.jaz.myapplicationcampuseats.model

data class MensajeChat(
    val id: String = "",
    val pedidoId: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
