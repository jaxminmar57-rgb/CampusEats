package com.jaz.myapplicationcampuseats.ui

import android.content.Context
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.viewmodel.ChatsViewModel

@Composable
fun ChatsScreen(
    userId: String,
    onVolver: () -> Unit,
    onAbrirChat: (pedidoId: String, otroNombre: String) -> Unit,
    vm: ChatsViewModel = viewModel()
) {
    LaunchedEffect(userId) { vm.iniciarListeners(userId) }

    var filtro by remember { mutableStateOf("activos") }

    val todosPedidos = vm.todosPedidos
    val cargando = vm.cargando
    val totalNuevos = vm.totalNuevos

    val pedidosFiltrados = remember(todosPedidos, filtro) {
        if (filtro == "activos") todosPedidos.filter { it.estado !in listOf("completado", "cancelado") }
        else todosPedidos
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text("Chats", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (totalNuevos > 0) { BadgeNumero(numero = totalNuevos, color = RedCancel); Spacer(modifier = Modifier.width(8.dp)) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("activos" to "Activos", "todos" to "Todos").forEach { (key, label) ->
                FilterChip(selected = filtro == key, onClick = { filtro = key }, label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenBtn,
                        selectedLabelColor = Color.White, labelColor = Color.Gray))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenBtn) }
            pedidosFiltrados.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                    Text(if (filtro == "activos") "No tienes chats activos" else "No tienes chats aún",
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Los chats aparecen cuando creas o recibes un pedido", color = Color.Gray, fontSize = 13.sp)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pedidosFiltrados, key = { it.id }) { pedido ->
                    val esVendedor = pedido.vendedorId == userId
                    val otroNombre = if (esVendedor) pedido.nombreCliente else pedido.nombreVendedor
                    val rolPropio  = if (esVendedor) "Vendedor" else "Cliente"
                    val nuevos     = vm.mensajesNuevos[pedido.id] ?: 0

                    ChatResumenCard(
                        pedido = pedido, otroNombre = otroNombre, rolPropio = rolPropio,
                        mensajesNuevos = nuevos,
                        onClick = {
                            vm.marcarLeido(pedido.id)
                            onAbrirChat(pedido.id, otroNombre)
                        }
                    )
                }
            }
        }
    }
}

fun reproducirSonidoMensaje(context: Context) {
    com.jaz.myapplicationcampuseats.service.SoundManager.playMensaje(context)
}

@Composable
fun ChatResumenCard(
    pedido: Pedido,
    otroNombre: String,
    rolPropio: String,
    mensajesNuevos: Int = 0,
    onClick: () -> Unit
) {
    val info = estadoInfo(pedido.estado)
    val esVendedor = rolPropio == "Vendedor"

    // Colores por rol
    val roleColor = if (esVendedor) OrangeWarn else BlueAceptado
    val roleEmoji = if (esVendedor) "📦" else "🛒"
    val roleLabel = if (esVendedor) "Vendiendo" else "Comprando"
    val cardBorder = if (mensajesNuevos > 0)
        androidx.compose.foundation.BorderStroke(1.5.dp, roleColor.copy(alpha = 0.5f))
    else null

    val descripcionItems = remember(pedido.items) {
        val nombres = pedido.items.take(2).mapNotNull { it["nombre"] as? String }
        val extra   = if (pedido.items.size > 2) " +${pedido.items.size - 2} más" else ""
        nombres.joinToString(", ") + extra
    }

    // Fetch other user's photo
    val otroUid = if (esVendedor) pedido.clienteId else pedido.vendedorId
    var fotoOtro by remember { mutableStateOf("") }
    LaunchedEffect(otroUid) {
        if (otroUid.isNotEmpty()) {
            com.jaz.myapplicationcampuseats.repository.UsuarioRepository.obtenerUsuario(otroUid,
                onSuccess = { fotoOtro = it.fotoPerfil })
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = cardBorder
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

            // Avatar with photo or initial
            Box(contentAlignment = Alignment.TopEnd) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(if (esVendedor) OrangeWarn.copy(alpha = 0.15f) else BlueAceptado.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    if (fotoOtro.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = fotoOtro, contentDescription = otroNombre,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(otroNombre.firstOrNull()?.uppercase() ?: "?",
                            color = roleColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (mensajesNuevos > 0) {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(RedCancel),
                        contentAlignment = Alignment.Center) {
                        Text(if (mensajesNuevos > 9) "9+" else "$mensajesNuevos",
                            color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Role tag on top
                Text("$roleEmoji $roleLabel", color = roleColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(otroNombre, color = Color.White, fontSize = 15.sp,
                        fontWeight = if (mensajesNuevos > 0) FontWeight.ExtraBold else FontWeight.Bold)
                    Text(info.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = info.color.copy(alpha = 0.15f)) {
                        Text(pedido.estado.replaceFirstChar { it.uppercase() }.replace("_", " "),
                            color = info.color, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = when (pedido.preferenciaEntrega) {
                        "vendedor_lleva" -> TealListo.copy(alpha = 0.15f)
                        "cliente_recoge" -> OrangeWarn.copy(alpha = 0.15f)
                        else             -> GreenBtn.copy(alpha = 0.15f)
                    }) {
                        Text(textoEntregaCorto(pedido.preferenciaEntrega),
                            color = when (pedido.preferenciaEntrega) {
                                "vendedor_lleva" -> TealListo
                                "cliente_recoge" -> OrangeWarn
                                else             -> GreenBtn
                            },
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
                if (descripcionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🛒 $descripcionItems",
                        color = if (mensajesNuevos > 0) Color.White.copy(alpha = 0.9f) else Color.Gray,
                        fontSize = 12.sp, maxLines = 1,
                        fontWeight = if (mensajesNuevos > 0) FontWeight.Medium else FontWeight.Normal)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("\$${String.format("%.2f", pedido.total)} · ${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""}",
                    color = Color.Gray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, "Abrir", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
