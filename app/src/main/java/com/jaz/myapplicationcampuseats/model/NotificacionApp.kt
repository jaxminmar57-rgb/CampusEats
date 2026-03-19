package com.jaz.myapplicationcampuseats.model

data class NotificacionApp(
    val id: String = "",
    val uid: String = "",           // usuario destinatario
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "",          // "pedido" | "chat" | "general"
    val pedidoId: String = "",
    val otroNombre: String = "",
    val leida: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
