package com.jaz.myapplicationcampuseats.ui

import androidx.compose.ui.graphics.Color

// ─── Modelo de info de estado ─────────────────────────────────────────────────

data class EstadoInfo(val color: Color, val emoji: String, val label: String)

fun estadoInfo(estado: String): EstadoInfo = when (estado) {
    "pendiente"  -> EstadoInfo(OrangeWarn,   "⏳", "Pendiente")
    "en_espera"  -> EstadoInfo(OrangeWarn,   "🕐", "En espera")
    "aceptado"   -> EstadoInfo(BlueAceptado, "✅", "Aceptado")   // azul — distinto del verde de listo
    "listo"      -> EstadoInfo(TealListo,    "🎉", "¡Listo!")    // teal — listo para entregar/recoger
    "completado" -> EstadoInfo(GreenLight,   "🏆", "Completado")
    "cancelado"  -> EstadoInfo(RedCancel,    "❌", "Cancelado")
    else         -> EstadoInfo(Color.Gray,   "❓", estado)
}

// ─── Tiempo transcurrido ──────────────────────────────────────────────────────

fun tiempoTranscurrido(timestamp: Long): String {
    val diff    = System.currentTimeMillis() - timestamp
    val minutos = diff / 60_000
    return when {
        minutos < 1  -> "ahora"
        minutos < 60 -> "${minutos}min"
        else         -> "${minutos / 60}h ${minutos % 60}min"
    }
}

// ─── Descripción de estado (genérica) ────────────────────────────────────────

fun descripcionEstado(estado: String) = when (estado) {
    "pendiente"  -> "Esperando respuesta del vendedor"
    "en_espera"  -> "El vendedor está preparando tu pedido"
    "aceptado"   -> "Pedido aceptado — coordina la entrega"
    "listo"      -> "¡Tu pedido está listo!"
    "completado" -> "Pedido completado con éxito"
    "cancelado"  -> "Este pedido fue cancelado"
    else -> ""
}

/**
 * Descripción contextual del estado "listo" según si el vendedor lleva o el
 * cliente recoge, y si el que consulta es el cliente.
 */
fun descripcionEstadoContextual(
    estado: String,
    preferenciaEntrega: String,
    esCliente: Boolean
): String {
    if (estado != "listo") return descripcionEstado(estado)
    return when {
        !esCliente -> "Pedido listo — confirma cuando lo entregues"
        preferenciaEntrega == "vendedor_lleva" ->
            "🛵 ¡Listo! En breve el vendedor te lo lleva"
        preferenciaEntrega == "cliente_recoge" ->
            "🚶 ¡Listo! Pasa a recoger tu pedido con el vendedor"
        else ->
            "🤝 ¡Listo! Coordina con el vendedor cómo recibirlo"
    }
}

// ─── Texto corto de entrega para tarjetas ────────────────────────────────────

fun textoEntregaCorto(preferenciaEntrega: String): String = when (preferenciaEntrega) {
    "cliente_recoge" -> "🚶 Recoger"
    "vendedor_lleva" -> "🛵 A domicilio"
    else             -> "🤝 Flexible"
}

fun textoEntregaLargo(preferenciaEntrega: String): String = when (preferenciaEntrega) {
    "cliente_recoge" -> "🚶 Tú recoges el pedido con el vendedor"
    "vendedor_lleva" -> "🛵 El vendedor te lo lleva a tu ubicación"
    else             -> "🤝 Coordinan la entrega por el chat"
}

// ─── Formato de fecha ─────────────────────────────────────────────────────────

fun formatearFecha(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

fun formatearTiempoRelativo(timestamp: Long): String {
    val diff    = System.currentTimeMillis() - timestamp
    val minutos = diff / 60_000
    return when {
        minutos < 1    -> "Ahora mismo"
        minutos < 60   -> "Hace ${minutos}min"
        minutos < 1440 -> "Hace ${minutos / 60}h"
        else           -> java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(timestamp))
    }
}
