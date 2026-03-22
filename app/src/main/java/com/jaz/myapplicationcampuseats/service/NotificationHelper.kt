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

    const val EXTRA_PEDIDO_ID   = "pedido_id"
    const val EXTRA_OTRO_NOMBRE = "otro_nombre"
    const val EXTRA_DESTINO     = "destino"

    private const val GROUP_PEDIDOS = "group_pedidos"
    private const val GROUP_CHAT    = "group_chat"
    private const val SUMMARY_PEDIDOS_ID = 90000
    private const val SUMMARY_CHAT_ID    = 90001

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_PEDIDOS, "Pedidos", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Actualizaciones de tus pedidos"
            setSound(sonido, atributos)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 100, 250)
            enableLights(true)
            lightColor = 0xFF4CAF50.toInt() // Verde
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_CHAT, "Mensajes", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Mensajes de chat"
            setSound(sonido, atributos)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 150, 80, 150)
            enableLights(true)
            lightColor = 0xFF2196F3.toInt() // Azul
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones generales"
        })
    }

    /**
     * Emoji y color según el estado del pedido.
     */
    private fun emojiEstado(estado: String): String = when (estado) {
        "aceptado"   -> "✅"
        "en_espera"  -> "🕐"
        "listo"      -> "🎉"
        "completado" -> "🏆"
        "cancelado"  -> "❌"
        else         -> "📦"
    }

    private fun colorEstado(estado: String): Int = when (estado) {
        "aceptado"   -> 0xFF42A5F5.toInt()
        "listo"      -> 0xFF26A69A.toInt()
        "completado" -> 0xFF4CAF50.toInt()
        "cancelado"  -> 0xFFE53935.toInt()
        else         -> 0xFFFF9800.toInt()
    }

    /**
     * Notificación de pedido mejorada con agrupación, emojis y colores por estado.
     */
    fun mostrarNotificacionPedido(
        context: Context,
        pedidoId: String,
        titulo: String,
        mensaje: String,
        otroNombre: String = "",
        estado: String = "",
        notifId: Int = pedidoId.hashCode()
    ) {
        val intentPedido = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DESTINO, "pedido")
            putExtra(EXTRA_PEDIDO_ID, pedidoId)
        }
        val piPedido = PendingIntent.getActivity(
            context, notifId, intentPedido,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

        val emoji = emojiEstado(estado)
        val color = colorEstado(estado)
        val tituloFinal = "$emoji $titulo"

        val notif = NotificationCompat.Builder(context, CHANNEL_PEDIDOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(tituloFinal)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(mensaje)
                .setBigContentTitle(tituloFinal))
            .setColor(color)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .setAutoCancel(true)
            .setContentIntent(piPedido)
            .setGroup(GROUP_PEDIDOS)
            .addAction(android.R.drawable.ic_menu_view, "Ver pedido", piPedido)
            .apply {
                if (otroNombre.isNotEmpty()) {
                    addAction(android.R.drawable.ic_menu_send, "Abrir chat", piChat)
                }
            }
            .build()

        // Summary para agrupar múltiples notificaciones de pedidos
        val summary = NotificationCompat.Builder(context, CHANNEL_PEDIDOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Campus Eats")
            .setContentText("Tienes actualizaciones de pedidos")
            .setColor(0xFF4CAF50.toInt())
            .setGroup(GROUP_PEDIDOS)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        try {
            val nm = NotificationManagerCompat.from(context)
            nm.notify(notifId, notif)
            nm.notify(SUMMARY_PEDIDOS_ID, summary)
        } catch (_: SecurityException) {}
    }

    /**
     * Notificación de chat mejorada con estilo de mensajería.
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

        // Estilo de mensajería
        val person = androidx.core.app.Person.Builder()
            .setName(remitente)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle("Chat con $remitente")
            .addMessage(mensaje, System.currentTimeMillis(), person)

        val notif = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💬 $remitente")
            .setContentText(mensaje)
            .setStyle(messagingStyle)
            .setColor(0xFF2196F3.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVibrate(longArrayOf(0, 150, 80, 150))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_CHAT)
            .addAction(android.R.drawable.ic_menu_send, "Responder", pi)
            .build()

        // Summary para agrupar chats
        val summary = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Campus Eats")
            .setContentText("Tienes mensajes nuevos")
            .setColor(0xFF2196F3.toInt())
            .setGroup(GROUP_CHAT)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        try {
            val nm = NotificationManagerCompat.from(context)
            nm.notify(notifId, notif)
            nm.notify(SUMMARY_CHAT_ID, summary)
        } catch (_: SecurityException) {}
    }
}
