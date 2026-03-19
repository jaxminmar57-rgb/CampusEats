package com.jaz.myapplicationcampuseats.ui

import androidx.compose.ui.graphics.Color

// ─── Modelo de info de estado ─────────────────────────────────────────────────

data class EstadoInfo(val color: Color, val emoji: String, val label: String)

fun estadoInfo(estado: String): EstadoInfo = when (estado) {
    "pendiente"  -> EstadoInfo(OrangeWarn,   "⏳", "Pendiente")
    "en_espera"  -> EstadoInfo(OrangeWarn,   "🕐", "En espera")
    "aceptado"   -> EstadoInfo(BlueAceptado, "✅", "Aceptado")
    "listo"      -> EstadoInfo(TealListo,    "🎉", "¡Listo!")
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
 * Descripción contextual para el cliente cuando el estado es "listo".
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
            "🛵 ¡Listo! Tu pedido va en camino a tu ubicación"
        preferenciaEntrega == "cliente_recoge" ->
            "🚶 ¡Ya puedes recoger tu pedido con el vendedor!"
        else ->
            "🤝 ¡Listo! Coordina con el vendedor cómo recibirlo"
    }
}

// ─── Texto corto de entrega para tarjetas (chips) ────────────────────────────

fun textoEntregaCorto(preferenciaEntrega: String): String = when (preferenciaEntrega) {
    "cliente_recoge" -> "🚶 Recogida"
    "vendedor_lleva" -> "🛵 Envío"
    else             -> "🤝 Flexible"
}

fun textoEntregaLargo(preferenciaEntrega: String): String = when (preferenciaEntrega) {
    "cliente_recoge" -> "🚶 Tú recoges el pedido con el vendedor"
    "vendedor_lleva" -> "🛵 El vendedor lleva el pedido a tu ubicación"
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

// ─── Búsqueda aproximada (fuzzy) ─────────────────────────────────────────────

/**
 * Verifica si [texto] contiene una coincidencia aproximada de [query].
 * Tolera errores de escritura, acentos faltantes, y substrings parciales.
 */
fun coincideFuzzy(texto: String, query: String): Boolean {
    if (query.isBlank()) return true
    val t = normalizarTexto(texto)
    val q = normalizarTexto(query)
    if (t.contains(q)) return true
    // Cada palabra del query aparece como substring
    val palabras = q.split(" ").filter { it.isNotBlank() }
    if (palabras.all { p -> t.contains(p) }) return true
    // Distancia de edición corta para queries cortos (3+ chars)
    if (q.length >= 3) {
        val palabrasTexto = t.split(" ")
        return palabras.all { pq ->
            palabrasTexto.any { pt -> distanciaLevenshtein(pq, pt) <= maxOf(1, pq.length / 4) }
        }
    }
    return false
}

private fun normalizarTexto(s: String): String {
    return s.lowercase()
        .replace("á", "a").replace("é", "e").replace("í", "i")
        .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
        .replace("ü", "u").trim()
}

private fun distanciaLevenshtein(a: String, b: String): Int {
    val m = a.length; val n = b.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = minOf(
            dp[i-1][j] + 1, dp[i][j-1] + 1,
            dp[i-1][j-1] + if (a[i-1] == b[j-1]) 0 else 1
        )
    }
    return dp[m][n]
}
