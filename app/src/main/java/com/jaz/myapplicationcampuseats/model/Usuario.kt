package com.jaz.myapplicationcampuseats.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val edad: String = "",
    val fotoPerfil: String = "",

    val telefono: String = "",
    val sexo: String = "",

    // Ratings
    val ratingComprador: Double = 0.0,
    val numResenasComprador: Int = 0,
    val ratingVendedor: Double = 0.0,
    val numResenasVendedor: Int = 0,

    // Preferencia de entrega del vendedor
    val preferenciaEntrega: String = "cliente_recoge",

    // Ubicación del vendedor
    val ubicacionLat: Double = 0.0,
    val ubicacionLng: Double = 0.0,
    val ubicacionDescripcion: String = "",

    val pedidosComoComprador: Int = 0,
    val pedidosComoVendedor: Int = 0,

    // Horario y disponibilidad del vendedor
    val horarioInicio: String = "09:00",
    val horarioFin: String = "18:00",
    val diasTrabajo: List<String> = listOf("Lun","Mar","Mié","Jue","Vie"),
    val negocioAbierto: Boolean = false,  // toggle diario

    val fechaRegistro: Long = System.currentTimeMillis()
)
