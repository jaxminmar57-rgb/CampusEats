package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.jaz.myapplicationcampuseats.model.NotificacionApp

object FcmRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ─── Token ───────────────────────────────────────────────────────────────

    fun guardarToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid)
            .update("fcmToken", token)
            .addOnFailureListener {
                db.collection("usuarios").document(uid)
                    .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    fun registrarTokenActual() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            guardarToken(token)
        }
    }

    // ─── Envío de notificaciones ─────────────────────────────────────────────
    //
    // IMPORTANTE: El envío de push (FCM) debe hacerse desde un backend seguro
    // (Cloud Functions, tu propio servidor, etc.) usando la API FCM v1.
    //
    // Desde la app solo guardamos la notificación in-app en Firestore y
    // opcionalmente escribimos en una colección "pendingNotifications" para
    // que una Cloud Function la procese y envíe el push real.
    //
    // NUNCA pongas SERVER_KEY ni credenciales de servicio en el código cliente.

    fun enviarNotificacion(
        destinatarioUid: String,
        titulo: String,
        cuerpo: String,
        tipo: String,
        pedidoId: String = "",
        otroNombre: String = ""
    ) {
        // 1. Guardar notificación in-app (siempre funciona)
        NotificacionesRepository.guardarNotificacion(
            NotificacionApp(
                uid        = destinatarioUid,
                titulo     = titulo,
                mensaje    = cuerpo,
                tipo       = tipo,
                pedidoId   = pedidoId,
                otroNombre = otroNombre
            )
        )

        // 2. Escribir en cola para que Cloud Function envíe el push
        val pushData = hashMapOf(
            "destinatarioUid" to destinatarioUid,
            "titulo"          to titulo,
            "cuerpo"          to cuerpo,
            "tipo"            to tipo,
            "pedidoId"        to pedidoId,
            "otroNombre"      to otroNombre,
            "timestamp"       to System.currentTimeMillis(),
            "enviado"         to false
        )
        db.collection("pendingNotifications").add(pushData)
    }

    // ─── Helpers por evento ──────────────────────────────────────────────────

    fun notificarNuevoPedido(vendedorUid: String, clienteNombre: String, pedidoId: String, total: Double) {
        enviarNotificacion(
            destinatarioUid = vendedorUid,
            titulo     = "Nuevo pedido",
            cuerpo     = "$clienteNombre realizó un pedido de \$${String.format("%.0f", total)}",
            tipo       = "pedido",
            pedidoId   = pedidoId,
            otroNombre = clienteNombre
        )
    }

    fun notificarCambioEstado(clienteUid: String, vendedorNombre: String, pedidoId: String, nuevoEstado: String) {
        val texto = when (nuevoEstado) {
            "aceptado"   -> "Tu pedido fue aceptado"
            "en_espera"  -> "Tu pedido está en espera"
            "listo"      -> "¡Tu pedido está listo!"
            "cancelado"  -> "Tu pedido fue cancelado"
            "completado" -> "Pedido completado"
            else         -> "Tu pedido se actualizó"
        }
        enviarNotificacion(
            destinatarioUid = clienteUid,
            titulo     = texto,
            cuerpo     = "De: $vendedorNombre",
            tipo       = "pedido",
            pedidoId   = pedidoId,
            otroNombre = vendedorNombre
        )
    }

    fun notificarMensajeChat(destinatarioUid: String, remitenteNombre: String, pedidoId: String, mensaje: String, rolRemitente: String = "") {
        val rolTexto = when (rolRemitente) {
            "vendedor" -> "vendedor"
            "cliente" -> "cliente"
            else -> ""
        }
        val titulo = if (rolTexto.isNotEmpty()) "Chat con $rolTexto $remitenteNombre" else "Mensaje de $remitenteNombre"
        enviarNotificacion(
            destinatarioUid = destinatarioUid,
            titulo     = titulo,
            cuerpo     = mensaje,
            tipo       = "chat",
            pedidoId   = pedidoId,
            otroNombre = remitenteNombre
        )
    }
}
