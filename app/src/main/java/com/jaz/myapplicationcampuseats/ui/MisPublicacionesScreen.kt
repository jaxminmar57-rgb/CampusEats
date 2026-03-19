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
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

@Composable
fun MisPublicacionesScreen(
    vendedorId: String,
    onVolver: () -> Unit,
    onEditar: ((Producto) -> Unit)? = null
) {
    var productos         by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando          by remember { mutableStateOf(true) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var ajustandoId       by remember { mutableStateOf<String?>(null) }

    // Listener en tiempo real para ver cambios de stock al instante
    DisposableEffect(vendedorId) {
        val listener = ProductoRepository.escucharProductosDelVendedor(vendedorId) { lista ->
            productos = lista.sortedByDescending { it.fechaPublicacion }
            cargando = false
        }
        onDispose { listener.remove() }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = DarkSurface,
            title = { Text("Eliminar platillo", color = Color.White) },
            text = { Text("¿Eliminar \"${productoAEliminar!!.nombre}\"? No se puede deshacer.", color = Color.Gray) },
            confirmButton = {
                Button(onClick = { ProductoRepository.eliminarProducto(productoAEliminar!!.id); productoAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel)) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Mis publicaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (productos.isNotEmpty()) Text("${productos.size}", color = Color.Gray, fontSize = 14.sp)
        }

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenBtn) }
            productos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aún no tienes platillos publicados", color = Color.White, fontSize = 15.sp)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(productos, key = { it.id }) { producto ->
                    MiProductoCard(
                        producto = producto,
                        ajustando = ajustandoId == producto.id,
                        onToggleDisponible = { ProductoRepository.toggleDisponibilidad(producto.id, it) },
                        onEliminar = { productoAEliminar = producto },
                        onEditar = onEditar?.let { cb -> { cb(producto) } },
                        onAgregarStock = {
                            ajustandoId = producto.id
                            ProductoRepository.ajustarStock(producto.id, +1) { ajustandoId = null }
                        },
                        onQuitarStock = {
                            if (producto.cantidadDisponible > 0) {
                                ajustandoId = producto.id
                                ProductoRepository.ajustarStock(producto.id, -1) { ajustandoId = null }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiProductoCard(
    producto: Producto,
    ajustando: Boolean = false,
    onToggleDisponible: (Boolean) -> Unit,
    onEliminar: () -> Unit,
    onEditar: (() -> Unit)? = null,
    onAgregarStock: () -> Unit = {},
    onQuitarStock: () -> Unit = {}
) {
    val tieneStock = producto.cantidadDisponible >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (producto.disponible) DarkSurface else DarkSurface.copy(alpha = 0.5f))
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                // Imagen
                if (producto.imagenUrl.isNotEmpty()) {
                    AsyncImage(model = producto.imagenUrl, contentDescription = null,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(72.dp).background(DarkSurface2, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 28.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre, color = if (producto.disponible) Color.White else Color.Gray,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(producto.categoria, color = Color.Gray, fontSize = 12.sp)
                    Text("\$${String.format("%.2f", producto.precio)}", color = GreenBtn, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    // Stock info
                    if (tieneStock) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val stockColor = when {
                            producto.cantidadDisponible == 0  -> RedCancel
                            producto.cantidadDisponible <= 3  -> OrangeWarn
                            else                              -> GreenBtn
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📦 ", fontSize = 12.sp)
                            Text(
                                when (producto.cantidadDisponible) {
                                    0    -> "Sin existencias"
                                    else -> "${producto.cantidadDisponible} disponible${if (producto.cantidadDisponible != 1) "s" else ""}"
                                },
                                color = stockColor, fontSize = 12.sp, fontWeight = FontWeight.Bold
                            )
                            if (producto.mostrarCantidad) Text(" · visible para clientes", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                Switch(checked = producto.disponible, onCheckedChange = { onToggleDisponible(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBtn))
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

            // Barra de acciones
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Editar
                if (onEditar != null) {
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = GreenBtn)
                        Spacer(Modifier.width(4.dp))
                        Text("Editar", color = GreenBtn, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Controles de stock
                if (tieneStock) {
                    if (ajustando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GreenBtn, strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onQuitarStock, modifier = Modifier.size(32.dp),
                            enabled = producto.cantidadDisponible > 0) {
                            Icon(Icons.Default.Remove, null,
                                tint = if (producto.cantidadDisponible > 0) OrangeWarn else Color.Gray,
                                modifier = Modifier.size(18.dp))
                        }
                        Text("${producto.cantidadDisponible}", color = Color.White, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 24.dp))
                        IconButton(onClick = onAgregarStock, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, null, tint = GreenBtn, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Eliminar
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedCancel, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
