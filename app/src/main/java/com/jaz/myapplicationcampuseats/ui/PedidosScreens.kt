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
import androidx.compose.material.icons.filled.ChatBubble
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
fun HistorialScreen(
    userId: String,
    onVolver: () -> Unit,
    onAbrirPedido: (String) -> Unit,
    onAbrirChat: ((pedidoId: String, otroNombre: String) -> Unit)? = null
) {
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("activos") }

    DisposableEffect(userId) {
        val listener = PedidoRepository.escucharPedidosCliente(userId) { lista ->
            pedidos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    val pedidosFiltrados = pedidos.filter { pedido ->
        if (filtro == "activos") pedido.estado !in listOf("completado", "cancelado")
        else pedido.estado in listOf("completado", "cancelado")
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Mis pedidos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("activos" to "Activos", "historial" to "Historial").forEach { (key, label) ->
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
            cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenBtn)
            }
            pedidosFiltrados.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (filtro == "activos") "🍽️" else "📋", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (filtro == "activos") "No tienes pedidos activos" else "Sin pedidos en el historial",
                        color = Color.White, fontSize = 15.sp
                    )
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidosFiltrados) { pedido ->
                    PedidoClienteCard(
                        pedido = pedido,
                        onClick = { onAbrirPedido(pedido.id) },
                        onChat = onAbrirChat?.let { cb -> { cb(pedido.id, pedido.nombreVendedor) } }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoClienteCard(
    pedido: Pedido,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null
) {
    val info = estadoInfo(pedido.estado)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // Barra de estado — ancho completo con color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(info.color)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(info.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            info.label,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        tiempoTranscurrido(pedido.fecha),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }

            // Contenido
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "🏪 ${pedido.nombreVendedor}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""} · ${pedido.metodoPago}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (pedido.notas.isNotEmpty()) {
                        Text("📝 ${pedido.notas}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "\$${String.format("%.2f", pedido.total)}",
                        color = GreenBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        if (onChat != null) {
                            IconButton(onClick = onChat, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ChatBubble, null, tint = GreenBtn, modifier = Modifier.size(18.dp))
                            }
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────

@Composable
fun PedidosVendedorScreen(
    vendedorId: String,
    onVolver: () -> Unit,
    onAbrirPedido: (String) -> Unit,
    onAbrirChat: ((pedidoId: String, otroNombre: String) -> Unit)? = null
) {
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("activos") }

    DisposableEffect(vendedorId) {
        val listener = PedidoRepository.escucharPedidosVendedor(vendedorId) { lista ->
            pedidos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    val pedidosFiltrados = pedidos.filter { pedido ->
        if (filtro == "activos") pedido.estado !in listOf("completado", "cancelado")
        else pedido.estado in listOf("completado", "cancelado")
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Pedidos recibidos",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            val pendientes = pedidos.count { it.estado == "pendiente" }
            if (pendientes > 0) {
                Surface(shape = RoundedCornerShape(12.dp), color = RedCancel) {
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("activos" to "Activos", "historial" to "Historial").forEach { (key, label) ->
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
            cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenBtn)
            }
            pedidosFiltrados.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidosFiltrados) { pedido ->
                    PedidoVendedorCard(
                        pedido = pedido,
                        onClick = { onAbrirPedido(pedido.id) },
                        onChat = onAbrirChat?.let { cb -> { cb(pedido.id, pedido.nombreCliente) } }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoVendedorCard(
    pedido: Pedido,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null
) {
    val info = estadoInfo(pedido.estado)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // Barra de estado ancho completo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(info.color)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(info.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(info.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        tiempoTranscurrido(pedido.fecha),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }

            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "👤 ${pedido.nombreCliente}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""} · ${pedido.metodoPago}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (pedido.notas.isNotEmpty()) {
                        Text("📝 ${pedido.notas}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "\$${String.format("%.2f", pedido.total)}",
                        color = GreenBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        if (onChat != null) {
                            IconButton(onClick = onChat, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ChatBubble, null, tint = GreenBtn, modifier = Modifier.size(18.dp))
                            }
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
