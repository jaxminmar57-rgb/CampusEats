package com.jaz.myapplicationcampuseats.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ingredientes: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "",
    val categoria: String = "",
    val vendedorId: String = "",
    val nombreVendedor: String = "",
    val rating: Double = 0.0,
    val numResenas: Int = 0,
    val ventasTotales: Int = 0,       // cuántas veces se ha vendido
    val disponible: Boolean = true,
    val fechaPublicacion: Long = System.currentTimeMillis(),
    val cantidadDisponible: Int = -1,
    val mostrarCantidad: Boolean = false,
    val ubicacionVendedor: String = "",
    val preferenciaEntregaVendedor: String = ""
)
