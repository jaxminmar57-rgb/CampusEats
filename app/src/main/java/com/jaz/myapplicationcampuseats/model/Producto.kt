package com.jaz.myapplicationcampuseats.model

/**
 * Modelo que representa un producto almacenado en Firebase.
 */
data class Producto(

    // ID del documento en Firestore
    val id: String = "",

    // Nombre del producto
    val nombre: String = "",

    // Descripción
    val descripcion: String = "",

    // Precio
    val precio: Double = 0.0,

    // URL de imagen en Firebase Storage
    val imagenUrl: String = "",

    // Categoría del producto
    val categoria: String = "",

    // ID del vendedor
    val vendedorId: String = "",

    // Calificación promedio
    val rating: Double = 0.0
)