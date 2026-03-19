package com.jaz.myapplicationcampuseats.model

data class Resena(
    val id: String = "",
    val pedidoId: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val destinatarioId: String = "",   // a quién se califica
    val rolDestinatario: String = "",  // "vendedor" o "comprador"
    val productoId: String = "",
    val estrellas: Int = 5,
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis()
)
