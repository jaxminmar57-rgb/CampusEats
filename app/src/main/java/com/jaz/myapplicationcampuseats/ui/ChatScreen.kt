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
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ChatRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    pedidoId: String,
    usuarioActual: Usuario?,
    otroNombre: String,
    onVolver: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var mensajes by remember { mutableStateOf<List<MensajeChat>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uid = usuarioActual?.uid ?: ""

    // Listener en tiempo real
    DisposableEffect(pedidoId) {
        val listener = ChatRepository.escucharMensajes(pedidoId) { lista ->
            mensajes = lista
        }
        onDispose { listener.remove() }
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
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
                .background(DarkCard)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GreenBtn.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    otroNombre.firstOrNull()?.uppercase() ?: "?",
                    color = GreenBtn,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(otroNombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Chat del pedido", color = Color.Gray, fontSize = 12.sp)
            }
        }

        // Mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (mensajes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Inicia la conversación", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                "Usa el chat para coordinar la entrega",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            items(mensajes) { mensaje ->
                val esPropio = mensaje.autorId == uid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (esPropio) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (esPropio) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!esPropio) {
                            Text(
                                mensaje.autorNombre,
                                color = GreenLight,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (esPropio) GreenBtn else DarkSurface,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (esPropio) 16.dp else 4.dp,
                                        bottomEnd = if (esPropio) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(mensaje.texto, color = Color.White, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(mensaje.timestamp)),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Campo de texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard)
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
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = DarkSurface,
                    focusedContainerColor = DarkSurface
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val msg = texto.trim()
                    if (msg.isNotEmpty() && uid.isNotEmpty()) {
                        texto = ""
                        val mensaje = MensajeChat(
                            autorId = uid,
                            autorNombre = usuarioActual?.nombre ?: "",
                            texto = msg
                        )
                        ChatRepository.enviarMensaje(pedidoId, mensaje)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(GreenBtn, CircleShape)
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
