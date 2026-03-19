package com.jaz.myapplicationcampuseats.model

/**
 * Estados posibles de un pedido:
 *  pendiente      -> recién creado por el cliente, esperando respuesta del vendedor
 *  en_espera      -> vendedor puso en espera (ej: está preparando)
 *  aceptado       -> vendedor aceptó, coordinando entrega
 *  listo          -> vendedor marcó el pedido como listo para recoger/entregar
 *  completado     -> ambos confirmaron que se completó (libera el pago)
 *  cancelado      -> vendedor o cliente canceló
 */
data class Pedido(
    val id: String = "",
    val clienteId: String = "",
    val vendedorId: String = "",
    val nombreCliente: String = "",
    val nombreVendedor: String = "",

    // Lista de items (guardados como mapa para Firestore)
    val items: List<Map<String, Any>> = emptyList(),

    val estado: String = "pendiente",
    val total: Double = 0.0,

    // Método de pago elegido por el cliente
    // "efectivo" o "tarjeta"
    val metodoPago: String = "efectivo",

    // Preferencia de entrega que tenía el vendedor al momento del pedido
    val preferenciaEntrega: String = "cliente_recoge",

    // Confirmaciones de pago liberado
    val clienteConfirmoEntrega: Boolean = false,
    val vendedorConfirmoEntrega: Boolean = false,

    // Notas adicionales del cliente
    val notas: String = "",

    val fecha: Long = System.currentTimeMillis()
)
