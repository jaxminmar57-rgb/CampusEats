package com.jaz.myapplicationcampuseats.model

/**
 * Modelo que representa un pedido realizado por un cliente
 */
data class Pedido(

    val id: String = "",

    val usuarioId: String = "",

    val vendedorId: String = "",

    val estado: String = "pendiente",

    val total: Double = 0.0,

    val fecha: Long = System.currentTimeMillis()
)