package com.jaz.myapplicationcampuseats.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val edad: String = "",
    val fotoPerfil: String = "",

    // Ratings
    val ratingComprador: Double = 0.0,
    val numResenasComprador: Int = 0,
    val ratingVendedor: Double = 0.0,
    val numResenasVendedor: Int = 0,

    // Preferencia de entrega del vendedor:
    // "cliente_recoge"  = el cliente va donde el vendedor
    // "vendedor_lleva"  = el vendedor va a una ubicación acordada
    // "ambos"           = cualquiera de las dos
    val preferenciaEntrega: String = "cliente_recoge",

    // Ubicación actual del vendedor (opcional, solo si está activo vendiendo)
    val ubicacionLat: Double = 0.0,
    val ubicacionLng: Double = 0.0,
    val ubicacionDescripcion: String = "",

    val pedidosComoComprador: Int = 0,
    val pedidosComoVendedor: Int = 0,

    val fechaRegistro: Long = System.currentTimeMillis()
)
