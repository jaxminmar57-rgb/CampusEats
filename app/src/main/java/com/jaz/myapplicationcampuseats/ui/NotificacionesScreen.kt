package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.model.NotificacionApp
import com.jaz.myapplicationcampuseats.repository.NotificacionesRepository

@Composable
fun NotificacionesScreen(
    userId: String,
    onVolver: () -> Unit,
    onAbrirPedido: ((String) -> Unit)? = null,
    onAbrirChat: ((pedidoId: String, otroNombre: String) -> Unit)? = null
) {
    var notificaciones by remember { mutableStateOf<List<NotificacionApp>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    DisposableEffect(userId) {
        if (userId.isEmpty()) { cargando = false; return@DisposableEffect onDispose {} }
        val listener = NotificacionesRepository.escucharNotificaciones(userId) { lista ->
            // Filtrar notificaciones de chat — esas van en la sección Chats
            notificaciones = lista.filter { it.tipo != "chat" }
            cargando = false
        }
        onDispose { listener.remove() }
    }

    val noLeidas = notificaciones.count { !it.leida }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Text("Notificaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (noLeidas > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeNumero(numero = noLeidas, color = RedCancel)
                }
            }
            if (notificaciones.isNotEmpty()) {
                TextButton(onClick = { NotificacionesRepository.marcarTodasLeidas(userId) }) {
                    Text("Marcar todas", color = GreenBtn, fontSize = 13.sp)
                }
            }
        }

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenBtn)
            }
            notificaciones.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes notificaciones", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Los cambios de estado de tus pedidos aparecerán aquí", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Los mensajes de chat están en la sección Chats", color = Color.Gray, fontSize = 12.sp)
                }
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                items(notificaciones, key = { it.id }) { notif ->
                    NotificacionCard(
                        notif = notif,
                        onClick = {
                            if (!notif.leida) NotificacionesRepository.marcarLeida(userId, notif.id)
                            if (notif.pedidoId.isNotEmpty()) onAbrirPedido?.invoke(notif.pedidoId)
                        },
                        onEliminar = { NotificacionesRepository.eliminarNotificacion(userId, notif.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificacionCard(notif: NotificacionApp, onClick: () -> Unit, onEliminar: () -> Unit) {
    val (accentColor, icono) = when {
        notif.titulo.contains("🛒") || notif.titulo.contains("nuevo pedido", ignoreCase = true) -> Pair(OrangeWarn, "🛒")
        notif.titulo.contains("✅") || notif.titulo.contains("aceptado", ignoreCase = true)     -> Pair(GreenBtn, "✅")
        notif.titulo.contains("🎉") || notif.titulo.contains("listo", ignoreCase = true)        -> Pair(GreenBtn, "🎉")
        notif.titulo.contains("❌") || notif.titulo.contains("cancelado", ignoreCase = true)    -> Pair(RedCancel, "❌")
        notif.titulo.contains("🏆") || notif.titulo.contains("completado", ignoreCase = true)   -> Pair(GreenLight, "🏆")
        notif.titulo.contains("🕐") || notif.titulo.contains("espera", ignoreCase = true)       -> Pair(OrangeWarn, "🕐")
        else -> Pair(Color.Gray, "🔔")
    }

    val bgColor = if (!notif.leida) accentColor.copy(alpha = 0.08f) else DarkBg

    Row(
        modifier = Modifier.fillMaxWidth().background(bgColor).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ícono con punto no-leído
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(icono, fontSize = 18.sp)
            }
            if (!notif.leida) {
                Box(modifier = Modifier.size(10.dp).align(Alignment.TopEnd).clip(CircleShape).background(accentColor))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(notif.titulo, color = Color.White, fontSize = 14.sp,
                fontWeight = if (!notif.leida) FontWeight.Bold else FontWeight.Normal)
            Spacer(modifier = Modifier.height(3.dp))
            Text(notif.mensaje, color = Color.Gray, fontSize = 13.sp, maxLines = 2)

            // Contexto adicional: nombre de la otra parte
            if (notif.otroNombre.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("👤 ${notif.otroNombre}", color = Color.Gray.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(formatearTiempoRelativo(notif.timestamp), color = Color.Gray.copy(alpha = 0.6f), fontSize = 11.sp)
                if (notif.pedidoId.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(4.dp), color = accentColor.copy(alpha = 0.15f)) {
                        Text("Ver pedido →", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }

        // Barra de color + eliminar
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp))
                .background(if (!notif.leida) accentColor else Color.Transparent))
            IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, "Cerrar", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
}
