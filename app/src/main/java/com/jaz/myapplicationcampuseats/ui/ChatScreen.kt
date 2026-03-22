package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.model.MensajeChat
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ChatRepository
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Singleton para saber en qué chat está el usuario (suprime notificaciones) */
object ChatActivo {
    var pedidoId: String? = null
}

@Composable
fun ChatScreen(
    pedidoId: String,
    usuarioActual: Usuario?,
    otroNombre: String,
    onVolver: () -> Unit,
    onVerPedido: (() -> Unit)? = null,
    onVerPerfil: ((uid: String) -> Unit)? = null
) {
    var mensajes by remember { mutableStateOf<List<MensajeChat>>(emptyList()) }
    var texto    by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var pedido   by remember { mutableStateOf<Pedido?>(null) }
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    val uid    = usuarioActual?.uid    ?: ""
    val nombre = usuarioActual?.nombre ?: ""

    // Registrar/limpiar chat activo (suprime notificaciones de este chat)
    DisposableEffect(pedidoId) {
        ChatActivo.pedidoId = pedidoId
        onDispose { ChatActivo.pedidoId = null }
    }

    // Pedido en tiempo real
    DisposableEffect(pedidoId) {
        val listener = PedidoRepository.escucharPedido(pedidoId) { p -> pedido = p }
        onDispose { listener.remove() }
    }

    // Mensajes en tiempo real
    DisposableEffect(pedidoId) {
        val listener = ChatRepository.escucharMensajes(pedidoId) { lista ->
            mensajes = lista
            scope.launch { if (lista.isNotEmpty()) listState.animateScrollToItem(lista.size - 1) }
        }
        onDispose { listener.remove() }
    }

    // Scroll al fondo cuando el teclado se abre (layoutInfo cambia)
    LaunchedEffect(listState.layoutInfo.viewportEndOffset, mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    val destinatarioUid = remember(pedido, uid) {
        val p = pedido ?: return@remember ""
        if (p.clienteId == uid) p.vendedorId else p.clienteId
    }

    // Resumen breve del pedido para el header
    val resumenPedido = remember(pedido) {
        val p = pedido ?: return@remember ""
        val nombres = p.items.take(2).mapNotNull { it["nombre"] as? String }
        val extra   = if (p.items.size > 2) " +${p.items.size - 2} más" else ""
        nombres.joinToString(", ") + extra
    }

    // imePadding() hace que el contenido suba SOLO lo que ocupa el teclado,
    // sin desplazar el header fuera de pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .imePadding()
    ) {
        // ── Header FIJO (nunca se mueve) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16213E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(otroNombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    pedido?.let { p ->
                        val info = estadoInfo(p.estado)
                        Text("${info.emoji} ${info.label}", color = info.color, fontSize = 12.sp)
                    }
                }
                // Botón para ver perfil de la otra persona
                if (onVerPerfil != null && destinatarioUid.isNotEmpty()) {
                    IconButton(onClick = { onVerPerfil(destinatarioUid) }) {
                        Icon(Icons.Default.Person, "Ver perfil", tint = GreenBtn)
                    }
                }
                // Botón para ir al detalle del pedido
                if (onVerPedido != null) {
                    IconButton(onClick = onVerPedido) {
                        Icon(Icons.Default.Receipt, "Ver pedido", tint = GreenBtn)
                    }
                }
            }

            // Resumen compacto del pedido
            if (resumenPedido.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🛒 $resumenPedido", color = Color.White, fontSize = 12.sp, maxLines = 1)
                            pedido?.let { p ->
                                Text(
                                    "\$${String.format("%.2f", p.total)} · ${p.metodoPago}",
                                    color = Color.Gray, fontSize = 11.sp
                                )
                            }
                        }
                        if (onVerPedido != null) {
                            TextButton(
                                onClick = onVerPedido,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Ver pedido", color = GreenBtn, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Mensajes (ocupa el espacio restante) ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mensajes) { mensaje ->
                val esPropio = mensaje.autorId == uid
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(mensaje.timestamp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (esPropio) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (esPropio) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!esPropio) {
                            Text(mensaje.autorNombre, color = Color.Gray, fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (esPropio) GreenBtn else DarkSurface,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp, topEnd = 16.dp,
                                        bottomStart = if (esPropio) 16.dp else 4.dp,
                                        bottomEnd = if (esPropio) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(mensaje.texto, color = Color.White, fontSize = 15.sp)
                        }
                        Text(hora, color = Color.Gray, fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
                    }
                }
            }
        }

        // ── Input FIJO (sube con el teclado gracias a imePadding) ──
        val chatBloqueado = pedido?.estado in listOf("completado", "cancelado")

        if (chatBloqueado) {
            // Mensaje de chat cerrado
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF16213E))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔒 ", fontSize = 14.sp)
                Text(
                    if (pedido?.estado == "completado") "Pedido completado — chat cerrado"
                    else "Pedido cancelado — chat cerrado",
                    color = Color.Gray, fontSize = 13.sp
                )
            }
        } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16213E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White, focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray, focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = DarkSurface, focusedContainerColor = DarkSurface
                ),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val msg = texto.trim()
                    if (msg.isEmpty() || uid.isEmpty()) return@IconButton
                    enviando = true
                    ChatRepository.enviarMensaje(
                        pedidoId = pedidoId,
                        mensaje = MensajeChat(
                            pedidoId = pedidoId, autorId = uid, autorNombre = nombre,
                            texto = msg, timestamp = System.currentTimeMillis()
                        ),
                        destinatarioUid = destinatarioUid,
                        onSuccess = { texto = ""; enviando = false },
                        onError   = { enviando = false }
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (texto.isBlank()) DarkSurface else GreenBtn, CircleShape),
                enabled = texto.isNotBlank() && !enviando
            ) {
                Icon(Icons.Default.Send, "Enviar", tint = Color.White)
            }
        }
        } // else chatBloqueado
    }
}
