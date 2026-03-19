package com.jaz.myapplicationcampuseats.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jaz.myapplicationcampuseats.repository.FcmRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Se llama cuando llega una notificación con la app en primer plano
     * o cuando el payload es de tipo "data" (no "notification").
     * Para la app en background con payload "notification", Android la muestra automáticamente.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val tipo      = data["tipo"] ?: "general"
        val pedidoId  = data["pedidoId"] ?: ""
        val titulo    = remoteMessage.notification?.title ?: data["titulo"] ?: "CampusEats"
        val cuerpo    = remoteMessage.notification?.body  ?: data["cuerpo"] ?: ""
        val otroNombre = data["otroNombre"] ?: ""

        NotificationHelper.crearCanales(applicationContext)

        when (tipo) {
            "pedido" -> NotificationHelper.mostrarNotificacionPedido(
                context    = applicationContext,
                pedidoId   = pedidoId,
                titulo     = titulo,
                mensaje    = cuerpo,
                otroNombre = otroNombre
            )
            "chat" -> NotificationHelper.mostrarNotificacionChat(
                context   = applicationContext,
                pedidoId  = pedidoId,
                remitente = otroNombre,
                mensaje   = cuerpo
            )
            else -> NotificationHelper.mostrarNotificacionPedido(
                context  = applicationContext,
                pedidoId = pedidoId,
                titulo   = titulo,
                mensaje  = cuerpo
            )
        }
    }

    /**
     * Se llama cuando Firebase asigna un nuevo token al dispositivo.
     * Guardamos el token en Firestore vinculado al usuario actual.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmRepository.guardarToken(token)
    }
}
