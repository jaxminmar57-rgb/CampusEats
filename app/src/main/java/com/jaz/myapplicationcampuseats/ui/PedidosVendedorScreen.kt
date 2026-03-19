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
fun PedidosVendedorScreen(
    vendedorId: String,
    onVolver: () -> Unit,
    onAbrirPedido: (String) -> Unit
) {
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("activos") } // "activos" o "historial"

    DisposableEffect(vendedorId) {
        val listener = PedidoRepository.escucharPedidosVendedor(vendedorId) { lista ->
            pedidos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    val pedidosFiltrados = pedidos.filter { pedido ->
        if (filtro == "activos") {
            pedido.estado !in listOf("completado", "cancelado")
        } else {
            pedido.estado in listOf("completado", "cancelado")
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
            Text("Pedidos recibidos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Badge de pendientes
            val pendientes = pedidos.count { it.estado == "pendiente" }
            if (pendientes > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RedCancel
                ) {
                    Text(
                        "$pendientes nuevo${if (pendientes > 1) "s" else ""}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Tabs filtro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("activos" to "Activos", "historial" to "Historial").forEach { (key, label) ->
                FilterChip(
                    selected = filtro == key,
                    onClick = { filtro = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenBtn,
                        selectedLabelColor = Color.White
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
                        Text(if (filtro == "activos") "📭" else "📋", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (filtro == "activos") "No tienes pedidos activos"
                            else "Sin pedidos en el historial",
                            color = Color.White, fontSize = 15.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidosFiltrados) { pedido ->
                        PedidoVendedorCard(
                            pedido = pedido,
                            onClick = { onAbrirPedido(pedido.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoVendedorCard(pedido: Pedido, onClick: () -> Unit) {
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
                        "De: ${pedido.nombreCliente}",
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
                        "${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""}  •  ${pedido.metodoPago}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                if (pedido.notas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📝 ${pedido.notas}", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}
