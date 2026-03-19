package com.jaz.myapplicationcampuseats.model

data class ItemCarrito(
    val id: String = "",
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val imagenUrl: String = "",
    val vendedorId: String = "",
    val nombreVendedor: String = ""
)
