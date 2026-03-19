package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.repository.ResenaRepository
import com.jaz.myapplicationcampuseats.model.Resena

@Composable
fun PedidoDetalleScreen(
    pedidoId: String,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onChat: (otroNombre: String) -> Unit
) {
    var pedido by remember { mutableStateOf<Pedido?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var procesando by remember { mutableStateOf(false) }
    var mostrarDialogoResena by remember { mutableStateOf(false) }
    var yaCalificaron by remember { mutableStateOf(false) }

    val uid = usuarioActual?.uid ?: ""

    // Listener en tiempo real
    DisposableEffect(pedidoId) {
        val listener = PedidoRepository.escucharPedido(pedidoId) { p ->
            pedido = p
            cargando = false
        }
        onDispose { listener.remove() }
    }

    // Verificar si ya calificó (cuando el pedido esté completado)
    LaunchedEffect(pedido?.estado) {
        if (pedido?.estado == "completado" && uid.isNotEmpty()) {
            ResenaRepository.yaCalificoPedido(pedidoId, uid) { yaCalificaron = it }
        }
    }

    val esVendedor = pedido?.vendedorId == uid
    val esCliente = pedido?.clienteId == uid

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenBtn)
        }
        return
    }

    val p = pedido ?: run {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            Text("Pedido no encontrado", color = Color.White)
        }
        return
    }

    if (mostrarDialogoResena) {
        DialogoResena(
            pedido = p,
            autorId = uid,
            autorNombre = usuarioActual?.nombre ?: "",
            esVendedor = esVendedor,
            onDismiss = { mostrarDialogoResena = false },
            onEnviado = {
                yaCalificaron = true
                mostrarDialogoResena = false
            }
        )
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
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(p.id.take(8) + "...", color = Color.Gray, fontSize = 12.sp)
            }
            // Botón de chat
            IconButton(onClick = {
                val otroNombre = if (esVendedor) p.nombreCliente else p.nombreVendedor
                onChat(otroNombre)
            }) {
                Icon(Icons.Default.Chat, null, tint = GreenBtn)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Estado del pedido
            item {
                EstadoPedidoCard(estado = p.estado)
            }

            // Información de entrega
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Información", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow("👤 ${if (esVendedor) "Cliente" else "Vendedor"}",
                            if (esVendedor) p.nombreCliente else p.nombreVendedor)
                        InfoRow("📦 Entrega",
                            when (p.preferenciaEntrega) {
                                "cliente_recoge" -> "Tú recoges con el vendedor"
                                "vendedor_lleva" -> "El vendedor va a tu ubicación"
                                else -> "Coordinan entre ambos"
                            }
                        )
                        InfoRow("💳 Pago",
                            if (p.metodoPago == "tarjeta") "Tarjeta — se libera al confirmar" else "Efectivo")
                        if (p.notas.isNotEmpty()) {
                            InfoRow("📝 Notas", p.notas)
                        }
                    }
                }
            }

            // Productos del pedido
            item {
                Text("Productos", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            items(p.items) { itemMap ->
                val nombre = itemMap["nombre"] as? String ?: ""
                val precio = (itemMap["precio"] as? Double) ?: 0.0
                val cantidad = (itemMap["cantidad"] as? Long)?.toInt() ?: 1
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$cantidad× $nombre", color = Color.White, fontSize = 14.sp)
                        Text("\$${String.format("%.2f", precio * cantidad)}", color = GreenBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Total
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", color = Color.Gray, fontSize = 16.sp)
                    Text(
                        "\$${String.format("%.2f", p.total)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Acciones del VENDEDOR
            if (esVendedor && p.estado != "completado" && p.estado != "cancelado") {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Gestionar pedido", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    AccionesVendedor(
                        estado = p.estado,
                        procesando = procesando,
                        vendedorConfirmo = p.vendedorConfirmoEntrega,
                        onAceptar = {
                            procesando = true
                            PedidoRepository.cambiarEstado(pedidoId, "aceptado") { procesando = false }
                        },
                        onEspera = {
                            procesando = true
                            PedidoRepository.cambiarEstado(pedidoId, "en_espera") { procesando = false }
                        },
                        onListo = {
                            procesando = true
                            PedidoRepository.cambiarEstado(pedidoId, "listo") { procesando = false }
                        },
                        onCancelar = {
                            procesando = true
                            PedidoRepository.cambiarEstado(pedidoId, "cancelado") { procesando = false }
                        },
                        onConfirmarEntrega = {
                            procesando = true
                            PedidoRepository.vendedorConfirmaEntrega(pedidoId) { procesando = false }
                        }
                    )
                }
            }

            // Acciones del CLIENTE
            if (esCliente && (p.estado == "listo" || p.estado == "aceptado") && !p.clienteConfirmoEntrega) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            procesando = true
                            PedidoRepository.clienteConfirmaEntrega(pedidoId) { procesando = false }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                        enabled = !procesando
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar que recibí mi pedido", fontWeight = FontWeight.Bold)
                    }
                    if (p.metodoPago == "tarjeta") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Al confirmar, se liberará el pago al vendedor",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Confirmaciones en progreso
            if (p.estado == "listo" || p.estado == "aceptado") {
                item {
                    ConfirmacionesCard(
                        clienteConfirmo = p.clienteConfirmoEntrega,
                        vendedorConfirmo = p.vendedorConfirmoEntrega
                    )
                }
            }

            // Botón calificar (cuando completado y no ha calificado)
            if (p.estado == "completado" && !yaCalificaron) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { mostrarDialogoResena = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(GoldStar)
                        )
                    ) {
                        Text("⭐ Dejar calificación", color = GoldStar, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun EstadoPedidoCard(estado: String) {
    val (color, emoji, label, descripcion) = when (estado) {
        "pendiente"  -> listOf(OrangeWarn, "⏳", "Pendiente", "Esperando respuesta del vendedor")
        "en_espera"  -> listOf(OrangeWarn, "🕐", "En espera", "El vendedor está preparando tu pedido")
        "aceptado"   -> listOf(GreenBtn, "✅", "Aceptado", "El vendedor aceptó — coordina la entrega")
        "listo"      -> listOf(GreenBtn, "🎉", "¡Listo!", "Tu pedido está listo para recoger / entregar")
        "completado" -> listOf(GreenLight, "🏆", "Completado", "Pedido entregado con éxito")
        "cancelado"  -> listOf(RedCancel, "❌", "Cancelado", "Este pedido fue cancelado")
        else         -> listOf(Color.Gray, "❓", estado, "")
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = (color as Color).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji as String, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label as String, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(descripcion as String, color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun AccionesVendedor(
    estado: String,
    procesando: Boolean,
    vendedorConfirmo: Boolean,
    onAceptar: () -> Unit,
    onEspera: () -> Unit,
    onListo: () -> Unit,
    onCancelar: () -> Unit,
    onConfirmarEntrega: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (estado == "pendiente") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                    enabled = !procesando
                ) { Text("✅ Aceptar") }
                Button(
                    onClick = onEspera,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeWarn),
                    enabled = !procesando
                ) { Text("⏸ En espera") }
            }
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !procesando
            ) { Text("❌ Declinar pedido", color = RedCancel) }
        }

        if (estado == "aceptado" || estado == "en_espera") {
            Button(
                onClick = onListo,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !procesando
            ) { Text("🎉 Marcar como listo") }
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !procesando
            ) { Text("❌ Cancelar pedido", color = RedCancel) }
        }

        if (estado == "listo" && !vendedorConfirmo) {
            Button(
                onClick = onConfirmarEntrega,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !procesando
            ) { Text("✔ Confirmar que entregué el pedido") }
        }
    }
}

@Composable
fun ConfirmacionesCard(clienteConfirmo: Boolean, vendedorConfirmo: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Confirmaciones de entrega", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (clienteConfirmo) "✅" else "⬜", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cliente confirmó", color = if (clienteConfirmo) GreenBtn else Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (vendedorConfirmo) "✅" else "⬜", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vendedor confirmó", color = if (vendedorConfirmo) GreenBtn else Color.Gray, fontSize = 14.sp)
            }
            if (!clienteConfirmo || !vendedorConfirmo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("El pedido se completará cuando ambos confirmen", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(valor, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.07f), modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
fun DialogoResena(
    pedido: Pedido,
    autorId: String,
    autorNombre: String,
    esVendedor: Boolean,
    onDismiss: () -> Unit,
    onEnviado: () -> Unit
) {
    var estrellas by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }

    val destinatarioId = if (esVendedor) pedido.clienteId else pedido.vendedorId
    val destinatarioNombre = if (esVendedor) pedido.nombreCliente else pedido.nombreVendedor
    val rolDestinatario = if (esVendedor) "comprador" else "vendedor"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Calificar a $destinatarioNombre", color = Color.White) },
        text = {
            Column {
                Text("¿Cómo fue tu experiencia?", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                // Estrellas
                Row {
                    (1..5).forEach { i ->
                        IconButton(onClick = { estrellas = i }, modifier = Modifier.size(36.dp)) {
                            Text(if (i <= estrellas) "⭐" else "☆", fontSize = 22.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    placeholder = { Text("Escribe un comentario...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = camposColores(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    enviando = true
                    val resena = Resena(
                        pedidoId = pedido.id,
                        autorId = autorId,
                        autorNombre = autorNombre,
                        destinatarioId = destinatarioId,
                        rolDestinatario = rolDestinatario,
                        estrellas = estrellas,
                        comentario = comentario
                    )
                    ResenaRepository.enviarResena(resena) {
                        enviando = false
                        onEnviado()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !enviando
            ) { Text("Enviar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}
