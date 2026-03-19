package com.jaz.myapplicationcampuseats.model

data class Tienda(
    val vendedorId: String = "",
    val nombreVendedor: String = "",
    val fotoPerfil: String = "",
    val ubicacionDescripcion: String = "",
    val preferenciaEntrega: String = "cliente_recoge",

    // Calculados en memoria a partir de sus productos
    val ratingPromedio: Double = 0.0,
    val numProductos: Int = 0,
    val numResenas: Int = 0,

    // Tiempo promedio de entrega en minutos (calculado de pedidos completados)
    val tiempoPromedioEntregaMin: Int = 0,

    val productos: List<Producto> = emptyList()
)
