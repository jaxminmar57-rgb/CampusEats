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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.CarritoRepository
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun CarritoScreen(
    userId: String,
    nombreCliente: String,
    onVolver: () -> Unit,
    onPedidoCreado: (String) -> Unit
) {
    var items        by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }
    var cargando     by remember { mutableStateOf(true) }
    val carritoCtx = androidx.compose.ui.platform.LocalContext.current
    var metodoPago   by remember { mutableStateOf("efectivo") }
    var notasVendedor by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var creandoPedido by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf("") }

    // Mapa vendedorId → preferenciaEntrega del vendedor
    var preferenciasVendedor by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    // Mapa vendedorId → datos del vendedor (para mostrar ubicación)
    var datosVendedor by remember { mutableStateOf<Map<String, Usuario>>(emptyMap()) }
    // Set de vendedores con negocio cerrado
    var vendedoresCerrados by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(userId) {
        CarritoRepository.obtenerCarrito(userId) { lista ->
            items = lista
            cargando = false
            // Cargar preferencias y estado abierto/cerrado de TODOS los vendedores del carrito
            val vendedorIds = lista.map { it.vendedorId }.toSet()
            vendedorIds.forEach { vid ->
                UsuarioRepository.obtenerUsuario(vid, onSuccess = { vendedor ->
                    preferenciasVendedor = preferenciasVendedor + (vid to vendedor.preferenciaEntrega)
                    datosVendedor = datosVendedor + (vid to vendedor)
                    if (!vendedor.negocioAbierto) {
                        vendedoresCerrados = vendedoresCerrados + vid
                    }
                })
            }
        }
    }

    // Agrupar items por vendedor
    val gruposPorVendedor = remember(items) {
        items.groupBy { it.vendedorId }
    }

    val total = items.sumOf { it.precio * it.cantidad }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
            }
            Text("Mi carrito", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (items.isNotEmpty()) {
                TextButton(onClick = {
                    CarritoRepository.vaciarCarrito(userId); items = emptyList()
                    com.jaz.myapplicationcampuseats.service.SoundManager.playEliminar(carritoCtx)
                }) {
                    Text("Vaciar", color = RedCancel, fontSize = 13.sp)
                }
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenBtn)
            }
            return@Column
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tu carrito está vacío", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Explora los platillos disponibles", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)) {
                        Text("Explorar")
                    }
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Por cada grupo de vendedor
            gruposPorVendedor.forEach { (vendedorId, itemsVendedor) ->
                val prefEntrega = preferenciasVendedor[vendedorId] ?: "cliente_recoge"
                val infoVendedor = datosVendedor[vendedorId]
                val nombreVend = itemsVendedor.first().nombreVendedor
                val estaCerrado = vendedorId in vendedoresCerrados

                // Cabecera del vendedor con su preferencia de entrega
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (estaCerrado) Color(0xFF3A1A1A) else DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "🏪 $nombreVend",
                                        color = if (estaCerrado) Color.Gray else Color.White,
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                                    )
                                    if (estaCerrado) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(6.dp), color = RedCancel.copy(alpha = 0.3f)) {
                                            Text("🔴 CERRADO", color = RedCancel, fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                // Chip de entrega
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (prefEntrega) {
                                        "vendedor_lleva" -> TealListo.copy(alpha = 0.2f)
                                        "cliente_recoge" -> OrangeWarn.copy(alpha = 0.2f)
                                        else             -> GreenBtn.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        textoEntregaCorto(prefEntrega),
                                        color = when (prefEntrega) {
                                            "vendedor_lleva" -> TealListo
                                            "cliente_recoge" -> OrangeWarn
                                            else             -> GreenBtn
                                        },
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(textoEntregaLargo(prefEntrega), color = Color.Gray, fontSize = 12.sp)
                            // Mostrar ubicación del vendedor si recoge
                            if (prefEntrega == "cliente_recoge" &&
                                infoVendedor?.ubicacionDescripcion?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, "Ubicación", tint = OrangeWarn,
                                        modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(infoVendedor.ubicacionDescripcion,
                                        color = OrangeWarn, fontSize = 12.sp)
                                }
                            }
                            // Botón quitar items de vendedor cerrado
                            if (estaCerrado) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        CarritoRepository.eliminarItemsDeVendedor(userId, vendedorId) {
                                            items = items.filter { it.vendedorId != vendedorId }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🗑️ Quitar productos de este vendedor", color = RedCancel, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Items de este vendedor
                items(itemsVendedor) { item ->
                    ItemCarritoCard(
                        item = item,
                        onEliminar = {
                            CarritoRepository.eliminarItem(userId, item.id)
                            items = items.filter { it.id != item.id }
                        },
                        onCantidadCambio = { nuevaCantidad ->
                            CarritoRepository.actualizarCantidad(userId, item.id, nuevaCantidad)
                            items = items.map {
                                if (it.id == item.id) it.copy(cantidad = nuevaCantidad) else it
                            }.filter { it.cantidad > 0 }
                        }
                    )
                }

                // Notas para ESTE vendedor
                item {
                    val notaActual = notasVendedor[vendedorId] ?: ""
                    OutlinedTextField(
                        value = notaActual,
                        onValueChange = { notasVendedor = notasVendedor + (vendedorId to it) },
                        placeholder = { Text("Nota para $nombreVend (opcional)...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = camposColores(), maxLines = 2,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Separador
            item { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }

            // Método de pago
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Método de pago", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetodoPagoChip("💵 Efectivo", metodoPago == "efectivo") { metodoPago = "efectivo" }
                    MetodoPagoChip("💳 Tarjeta",  metodoPago == "tarjeta")  { metodoPago = "tarjeta" }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (metodoPago == "tarjeta") "El pago se libera cuando ambos confirmen la entrega"
                    else "Paga en efectivo al momento de recibir",
                    color = Color.Gray, fontSize = 12.sp
                )
            }

            if (error.isNotEmpty()) {
                item { Text(error, color = RedCancel, fontSize = 13.sp) }
            }
        }

        // Resumen y botón confirmar
        val hayVendedoresCerrados = vendedoresCerrados.any { vid -> items.any { it.vendedorId == vid } }
        val itemsValidos = items.filter { it.vendedorId !in vendedoresCerrados }
        val totalValido = itemsValidos.sumOf { it.precio * it.cantidad }
        val gruposValidos = itemsValidos.groupBy { it.vendedorId }

        Column(
            modifier = Modifier.fillMaxWidth().background(DarkCard).padding(16.dp)
        ) {
            if (hayVendedoresCerrados) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Algunos vendedores cerraron. Quita esos productos para continuar.",
                        color = OrangeWarn, fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", color = Color.Gray, fontSize = 15.sp)
                Text("\$${String.format("%.2f", totalValido)}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            if (gruposValidos.size > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Se creará un pedido por cada vendedor (${gruposValidos.size})",
                    color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    creandoPedido = true; error = ""
                    com.jaz.myapplicationcampuseats.service.SoundManager.playConfirmarPedido(carritoCtx)
                    val grupos = gruposValidos.entries.toList()
                    var confirmados = 0
                    var ultimoPedidoId = ""
                    grupos.forEach { (vendedorId, itemsGrupo) ->
                        val prefEntrega = preferenciasVendedor[vendedorId] ?: "cliente_recoge"
                        val notasVend = notasVendedor[vendedorId] ?: ""
                        PedidoRepository.crearPedido(
                            clienteId = userId, nombreCliente = nombreCliente,
                            items = itemsGrupo, metodoPago = metodoPago,
                            notas = notasVend, preferenciaEntrega = prefEntrega,
                            onSuccess = { pedidoId ->
                                ultimoPedidoId = pedidoId
                                confirmados++
                                if (confirmados == grupos.size) {
                                    CarritoRepository.vaciarCarrito(userId)
                                    creandoPedido = false
                                    onPedidoCreado(ultimoPedidoId)
                                }
                            },
                            onError = { msg -> creandoPedido = false; error = msg }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !creandoPedido && itemsValidos.isNotEmpty() && !hayVendedoresCerrados
            ) {
                if (creandoPedido) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else Text(
                    if (hayVendedoresCerrados) "Quita productos cerrados primero"
                    else "Confirmar pedido",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ItemCarritoCard(item: ItemCarrito, onEliminar: () -> Unit, onCantidadCambio: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imagenUrl.isNotEmpty()) {
                AsyncImage(model = item.imagenUrl, contentDescription = item.nombre,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(70.dp).background(DarkSurface2, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 28.sp) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(item.nombreVendedor, color = Color.Gray, fontSize = 12.sp)
                Text("\$${String.format("%.2f", item.precio * item.cantidad)}", color = GreenBtn,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onCantidadCambio(item.cantidad - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (item.cantidad == 1) Icons.Default.Delete else Icons.Default.Remove,
                            null, tint = if (item.cantidad == 1) RedCancel else Color.White,
                            modifier = Modifier.size(18.dp))
                    }
                    Text("${item.cantidad}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onCantidadCambio(item.cantidad + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, "Agregar", tint = GreenBtn, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MetodoPagoChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp),
        color = if (seleccionado) GreenBtn else DarkSurface, modifier = Modifier.height(38.dp)) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(texto, color = Color.White, fontSize = 14.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
