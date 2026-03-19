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
    val disponible: Boolean = true,
    val fechaPublicacion: Long = System.currentTimeMillis(),
    // Stock
    val cantidadDisponible: Int = -1,    // -1 = sin límite
    val mostrarCantidad: Boolean = false  // si true, clientes ven cuántas quedan
)
