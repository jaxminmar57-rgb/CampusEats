package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.repository.PedidoRepository

@Composable
fun HistorialScreen(
    userId: String,
    onVolver: () -> Unit,
    onAbrirPedido: (String) -> Unit
) {
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Escucha en tiempo real para que el cliente vea cambios de estado
    DisposableEffect(userId) {
        val listener = PedidoRepository.escucharPedidosCliente(userId) { lista ->
            pedidos = lista
            cargando = false
        }
        onDispose { listener.remove() }
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Mis pedidos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        when {
            cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn)
                }
            }
            pedidos.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍽️", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aún no tienes pedidos", color = Color.White, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Explora los platillos disponibles", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onVolver,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                        ) { Text("Explorar") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidos) { pedido ->
                        PedidoClienteCard(pedido = pedido, onClick = { onAbrirPedido(pedido.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoClienteCard(pedido: Pedido, onClick: () -> Unit) {
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
            Text(estadoEmoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Vendedor: ${pedido.nombreVendedor}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "\$${String.format("%.2f", pedido.total)}",
                        color = GreenBtn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = estadoColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            pedido.estado.replaceFirstChar { it.uppercase() }.replace("_", " "),
                            color = estadoColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}
