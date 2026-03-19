package com.jaz.myapplicationcampuseats.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val edad: String = "",
    val fotoPerfil: String = "",

    // Campos nuevos de perfil
    val telefono: String = "",          // opcional
    val sexo: String = "",              // "Masculino", "Femenino", "Prefiero no decir"

    // Ratings
    val ratingComprador: Double = 0.0,
    val numResenasComprador: Int = 0,
    val ratingVendedor: Double = 0.0,
    val numResenasVendedor: Int = 0,

    // Preferencia de entrega del vendedor
    val preferenciaEntrega: String = "cliente_recoge",

    // Ubicación del vendedor (opcional)
    val ubicacionLat: Double = 0.0,
    val ubicacionLng: Double = 0.0,
    val ubicacionDescripcion: String = "",

    val pedidosComoComprador: Int = 0,
    val pedidosComoVendedor: Int = 0,

    val fechaRegistro: Long = System.currentTimeMillis()
)
