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
    var items by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var metodoPago by remember { mutableStateOf("efectivo") }
    var notas by remember { mutableStateOf("") }
    var creandoPedido by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var preferenciaEntrega by remember { mutableStateOf("cliente_recoge") }

    LaunchedEffect(userId) {
        CarritoRepository.obtenerCarrito(userId) { lista ->
            items = lista
            cargando = false
            // Cargar preferencia de entrega del vendedor del primer item
            val vendedorId = lista.firstOrNull()?.vendedorId ?: return@obtenerCarrito
            UsuarioRepository.obtenerUsuario(vendedorId, onSuccess = { vendedor ->
                preferenciaEntrega = vendedor.preferenciaEntrega
            })
        }
    }

    val total = items.sumOf { it.precio * it.cantidad }

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
            Text("Mi carrito", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (items.isNotEmpty()) {
                TextButton(onClick = {
                    CarritoRepository.vaciarCarrito(userId)
                    items = emptyList()
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
                    Button(
                        onClick = onVolver,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                    ) { Text("Explorar") }
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
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

            // Info de entrega del vendedor
            item {
                val textoEntrega = when (preferenciaEntrega) {
                    "cliente_recoge" -> "🚶 Tú debes recoger el pedido con el vendedor"
                    "vendedor_lleva" -> "🛵 El vendedor puede acercarse a tu ubicación"
                    else             -> "🤝 Coordinan dónde/cómo se hace la entrega por el chat"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = GreenBtn, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(textoEntrega, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Método de pago
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Método de pago", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetodoPagoChip(
                        texto = "💵 Efectivo",
                        seleccionado = metodoPago == "efectivo",
                        onClick = { metodoPago = "efectivo" }
                    )
                    MetodoPagoChip(
                        texto = "💳 Tarjeta",
                        seleccionado = metodoPago == "tarjeta",
                        onClick = { metodoPago = "tarjeta" }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (metodoPago == "tarjeta")
                        "El pago se libera cuando ambos confirmen la entrega"
                    else
                        "Paga en efectivo al momento de recibir",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Notas
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Notas para el vendedor (opcional)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    placeholder = { Text("Ej: sin cebolla, punto de cocción...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = camposColores(),
                    maxLines = 3
                )
            }

            if (error.isNotEmpty()) {
                item {
                    Text(error, color = RedCancel, fontSize = 13.sp)
                }
            }
        }

        // Resumen y botón confirmar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", color = Color.Gray, fontSize = 15.sp)
                Text(
                    "\$${String.format("%.2f", total)}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    creandoPedido = true
                    error = ""
                    PedidoRepository.crearPedido(
                        clienteId = userId,
                        nombreCliente = nombreCliente,
                        items = items,
                        metodoPago = metodoPago,
                        notas = notas,
                        preferenciaEntrega = preferenciaEntrega,
                        onSuccess = { pedidoId ->
                            CarritoRepository.vaciarCarrito(userId)
                            creandoPedido = false
                            onPedidoCreado(pedidoId)
                        },
                        onError = { msg ->
                            creandoPedido = false
                            error = msg
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !creandoPedido && items.isNotEmpty()
            ) {
                if (creandoPedido) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Confirmar pedido", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ItemCarritoCard(
    item: ItemCarrito,
    onEliminar: () -> Unit,
    onCantidadCambio: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imagenUrl,
                    contentDescription = item.nombre,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(70.dp).background(DarkSurface2, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("🍽️", fontSize = 28.sp) }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(item.nombreVendedor, color = Color.Gray, fontSize = 12.sp)
                Text(
                    "\$${String.format("%.2f", item.precio * item.cantidad)}",
                    color = GreenBtn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onCantidadCambio(item.cantidad - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (item.cantidad == 1) Icons.Default.Delete else Icons.Default.Remove,
                            null,
                            tint = if (item.cantidad == 1) RedCancel else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "${item.cantidad}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onCantidadCambio(item.cantidad + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = GreenBtn, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MetodoPagoChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (seleccionado) GreenBtn else DarkSurface,
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                texto,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
