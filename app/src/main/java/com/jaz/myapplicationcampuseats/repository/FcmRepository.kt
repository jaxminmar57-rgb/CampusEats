package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.jaz.myapplicationcampuseats.model.NotificacionApp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

    // ─── Envío ───────────────────────────────────────────────────────────────

    /**
     * Reemplaza TU_SERVER_KEY_AQUI con tu clave de servidor de Firebase:
     * Firebase Console → Configuración del proyecto → Cloud Messaging → Clave del servidor
     */
    private const val SERVER_KEY = "BMBPQ9_amy98QRxjQHnXhkKkzY2BSzY4GoP19dJdC-9uYSQr7Xh3ej_K-h4YBfyGT253eY4PyPfOTTPejaNymN8"
    private const val FCM_URL    = "https://fcm.googleapis.com/fcm/send"

    fun enviarNotificacion(
        destinatarioUid: String,
        titulo: String,
        cuerpo: String,
        tipo: String,
        pedidoId: String = "",
        otroNombre: String = ""
    ) {
        // 1. Guardar la notificación en Firestore (in-app)
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

        // 2. Enviar push FCM
        db.collection("usuarios").document(destinatarioUid).get()
            .addOnSuccessListener { doc ->
                val token = doc.getString("fcmToken") ?: return@addOnSuccessListener
                Thread { enviarFcm(token, titulo, cuerpo, tipo, pedidoId, otroNombre) }.start()
            }
    }

    private fun enviarFcm(
        token: String, titulo: String, cuerpo: String,
        tipo: String, pedidoId: String, otroNombre: String
    ) {
        if (SERVER_KEY == "TU_SERVER_KEY_AQUI") return

        try {
            val url  = URL(FCM_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "key=$SERVER_KEY")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("to", token)
                put("notification", JSONObject().apply {
                    put("title", titulo)
                    put("body", cuerpo)
                    put("sound", "default")
                })
                put("data", JSONObject().apply {
                    put("tipo", tipo)
                    put("pedidoId", pedidoId)
                    put("otroNombre", otroNombre)
                    put("titulo", titulo)
                    put("cuerpo", cuerpo)
                })
                put("priority", "high")
            }

            conn.outputStream.write(payload.toString().toByteArray())
            conn.responseCode
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ─── Helpers por evento ──────────────────────────────────────────────────

    fun notificarNuevoPedido(vendedorUid: String, clienteNombre: String, pedidoId: String, total: Double) {
        enviarNotificacion(
            destinatarioUid = vendedorUid,
            titulo     = "🛒 Nuevo pedido",
            cuerpo     = "$clienteNombre realizó un pedido de \$${String.format("%.0f", total)}",
            tipo       = "pedido",
            pedidoId   = pedidoId,
            otroNombre = clienteNombre
        )
    }

    fun notificarCambioEstado(clienteUid: String, vendedorNombre: String, pedidoId: String, nuevoEstado: String) {
        val (emoji, texto) = when (nuevoEstado) {
            "aceptado"   -> "✅" to "Tu pedido fue aceptado"
            "en_espera"  -> "🕐" to "Tu pedido está en espera"
            "listo"      -> "🎉" to "¡Tu pedido está listo!"
            "cancelado"  -> "❌" to "Tu pedido fue cancelado"
            "completado" -> "🏆" to "Pedido completado"
            else         -> "📦" to "Tu pedido se actualizó"
        }
        enviarNotificacion(
            destinatarioUid = clienteUid,
            titulo     = "$emoji $texto",
            cuerpo     = "De: $vendedorNombre",
            tipo       = "pedido",
            pedidoId   = pedidoId,
            otroNombre = vendedorNombre
        )
    }

    fun notificarMensajeChat(destinatarioUid: String, remitenteNombre: String, pedidoId: String, mensaje: String) {
        enviarNotificacion(
            destinatarioUid = destinatarioUid,
            titulo     = "💬 Mensaje de $remitenteNombre",
            cuerpo     = mensaje,
            tipo       = "chat",
            pedidoId   = pedidoId,
            otroNombre = remitenteNombre
        )
    }
}
