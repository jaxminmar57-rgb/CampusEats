package com.jaz.myapplicationcampuseats.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jaz.myapplicationcampuseats.MainActivity
import com.jaz.myapplicationcampuseats.R

object NotificationHelper {

    const val CHANNEL_PEDIDOS = "campuseats_pedidos"
    const val CHANNEL_CHAT    = "campuseats_chat"
    const val CHANNEL_GENERAL = "campuseats_general"

    // Extras que se pasan en el Intent para que la app navegue al destino correcto
    const val EXTRA_PEDIDO_ID   = "pedido_id"
    const val EXTRA_OTRO_NOMBRE = "otro_nombre"
    const val EXTRA_DESTINO     = "destino"    // "pedido" | "chat" | "historial"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_PEDIDOS,
            "Pedidos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Actualizaciones de tus pedidos"
            setSound(sonido, atributos)
            enableVibration(true)
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_CHAT,
            "Mensajes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Mensajes de chat"
            setSound(sonido, atributos)
            enableVibration(true)
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones generales"
        })
    }

    /**
     * Muestra notificación de cambio de estado de pedido.
     * Incluye acción "Ver pedido" y "Abrir chat".
     */
    fun mostrarNotificacionPedido(
        context: Context,
        pedidoId: String,
        titulo: String,
        mensaje: String,
        otroNombre: String = "",
        notifId: Int = pedidoId.hashCode()
    ) {
        // Intent principal: abre el detalle del pedido
        val intentPedido = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DESTINO, "pedido")
            putExtra(EXTRA_PEDIDO_ID, pedidoId)
        }
        val piPedido = PendingIntent.getActivity(
            context, notifId, intentPedido,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent acción: abrir chat de ese pedido
        val intentChat = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DESTINO, "chat")
            putExtra(EXTRA_PEDIDO_ID, pedidoId)
            putExtra(EXTRA_OTRO_NOMBRE, otroNombre)
        }
        val piChat = PendingIntent.getActivity(
            context, notifId + 1000, intentChat,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notif = NotificationCompat.Builder(context, CHANNEL_PEDIDOS)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sonido)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .setAutoCancel(true)
            .setContentIntent(piPedido)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Ver pedido",
                piPedido
            )
            .apply {
                if (otroNombre.isNotEmpty()) {
                    addAction(
                        android.R.drawable.ic_menu_send,
                        "Abrir chat",
                        piChat
                    )
                }
            }
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notif)
        } catch (e: SecurityException) {
            // Permiso no concedido en Android 13+
        }
    }

    /**
     * Muestra notificación de mensaje de chat.
     */
    fun mostrarNotificacionChat(
        context: Context,
        pedidoId: String,
        remitente: String,
        mensaje: String,
        notifId: Int = ("chat_$pedidoId").hashCode()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DESTINO, "chat")
            putExtra(EXTRA_PEDIDO_ID, pedidoId)
            putExtra(EXTRA_OTRO_NOMBRE, remitente)
        }
        val pi = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notif = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Mensaje de $remitente")
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sonido)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_menu_send, "Responder", pi)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notif)
        } catch (e: SecurityException) {
            // permiso no concedido
        }
    }
}
