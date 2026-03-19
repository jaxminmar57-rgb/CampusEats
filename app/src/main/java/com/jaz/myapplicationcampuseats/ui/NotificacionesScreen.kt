package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Notificacion(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val hora: String,
    val tipo: String,
    var leida: Boolean = false
)

@Composable
fun NotificacionesScreen(onVolver: () -> Unit) {
    val notificaciones = remember {
        mutableStateListOf(
            Notificacion("1", "💬 Nuevo mensaje", "El vendedor te envió un mensaje sobre tu pedido", "10:05", "chat"),
            Notificacion("2", "✅ Pedido aceptado", "Tu pedido fue aceptado — coordina la entrega", "09:45", "aceptado"),
            Notificacion("3", "🎉 Pedido listo", "Tu pedido está listo para recoger", "08:30", "listo"),
            Notificacion("4", "🛒 Nuevo pedido recibido", "Tienes un nuevo pedido de un cliente", "08:00", "nuevo"),
            Notificacion("5", "❌ Pedido cancelado", "Un pedido fue cancelado", "07:30", "cancelado")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text("Notificaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { notificaciones.replaceAll { it.copy(leida = true) } }) {
                Text("Marcar todas", color = GreenBtn, fontSize = 13.sp)
            }
        }

        if (notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes notificaciones", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notificaciones) { notificacion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (!notificacion.leida) Color(0xFF1E1E3A) else DarkBg)
                            .clickable {
                                val index = notificaciones.indexOf(notificacion)
                                if (index >= 0) notificaciones[index] = notificacion.copy(leida = true)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (!notificacion.leida) GreenBtn else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                notificacion.titulo,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notificacion.mensaje, color = Color.Gray, fontSize = 13.sp)
                        }
                        Text(notificacion.hora, color = Color.Gray, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}
