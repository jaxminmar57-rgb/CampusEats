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
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.repository.PedidoRepository

@Composable
fun ChatsScreen(
    userId: String,
    onVolver: () -> Unit,
    onAbrirChat: (pedidoId: String, otroNombre: String) -> Unit
) {
    // Cargamos pedidos donde el usuario es cliente O vendedor
    var pedidosComoCliente by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var pedidosComoVendedor by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("activos") } // "activos" o "todos"

    DisposableEffect(userId) {
        val listenerCliente = PedidoRepository.escucharPedidosCliente(userId) { lista ->
            pedidosComoCliente = lista
            cargando = false
        }
        val listenerVendedor = PedidoRepository.escucharPedidosVendedor(userId) { lista ->
            pedidosComoVendedor = lista
        }
        onDispose {
            listenerCliente.remove()
            listenerVendedor.remove()
        }
    }

    // Combinar pedidos de ambos roles, sin duplicados
    val todosPedidos = remember(pedidosComoCliente, pedidosComoVendedor) {
        (pedidosComoCliente + pedidosComoVendedor)
            .distinctBy { it.id }
            .sortedByDescending { it.fecha }
    }

    val pedidosFiltrados = remember(todosPedidos, filtro) {
        if (filtro == "activos") {
            todosPedidos.filter { it.estado !in listOf("completado", "cancelado") }
        } else {
            todosPedidos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Chats",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Badge de chats activos
            val activos = todosPedidos.count { it.estado !in listOf("completado", "cancelado") }
            if (activos > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GreenBtn),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$activos", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("activos" to "Activos", "todos" to "Todos").forEach { (key, label) ->
                FilterChip(
                    selected = filtro == key,
                    onClick = { filtro = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenBtn,
                        selectedLabelColor = Color.White,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn)
                }
            }
            pedidosFiltrados.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (filtro == "activos") "No tienes chats activos"
                            else "No tienes chats aún",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Los chats aparecen cuando creas o recibes un pedido",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pedidosFiltrados) { pedido ->
                        val esVendedor = pedido.vendedorId == userId
                        val otroNombre = if (esVendedor) pedido.nombreCliente else pedido.nombreVendedor
                        val rolPropio = if (esVendedor) "Vendedor" else "Cliente"

                        ChatResumenCard(
                            pedido = pedido,
                            otroNombre = otroNombre,
                            rolPropio = rolPropio,
                            onClick = { onAbrirChat(pedido.id, otroNombre) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatResumenCard(
    pedido: Pedido,
    otroNombre: String,
    rolPropio: String,
    onClick: () -> Unit
) {
    val (estadoColor, estadoEmoji) = when (pedido.estado) {
        "pendiente"  -> Pair(OrangeWarn, "⏳")
        "en_espera"  -> Pair(OrangeWarn, "🕐")
        "aceptado"   -> Pair(GreenBtn, "✅")
        "listo"      -> Pair(GreenBtn, "🎉")
        "completado" -> Pair(GreenLight, "🏆")
        "cancelado"  -> Pair(RedCancel, "❌")
        else         -> Pair(Color.Gray, "❓")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DarkSurface2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    otroNombre.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        otroNombre,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(estadoEmoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rol propio
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GreenBtn.copy(alpha = 0.2f)
                    ) {
                        Text(
                            rolPropio,
                            color = GreenBtn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    // Estado
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = estadoColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            pedido.estado.replaceFirstChar { it.uppercase() }.replace("_", " "),
                            color = estadoColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Pedido · \$${String.format("%.2f", pedido.total)} · ${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
