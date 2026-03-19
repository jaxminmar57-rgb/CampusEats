package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
    onVolver: () -> Unit
) {
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(vendedorId) {
        ProductoRepository.obtenerProductosDelVendedor(vendedorId) { lista ->
            productos = lista
            cargando = false
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = DarkSurface,
            title = { Text("Eliminar platillo", color = Color.White) },
            text = { Text("¿Eliminar \"${productoAEliminar!!.nombre}\"? Esta acción no se puede deshacer.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        val id = productoAEliminar!!.id
                        ProductoRepository.eliminarProducto(id) {
                            productos = productos.filter { it.id != id }
                        }
                        productoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Mis publicaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        when {
            cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn)
                }
            }
            productos.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍽️", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aún no tienes platillos publicados", color = Color.White, fontSize = 15.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productos) { producto ->
                        MiProductoCard(
                            producto = producto,
                            onToggleDisponible = { nuevoEstado ->
                                ProductoRepository.toggleDisponibilidad(producto.id, nuevoEstado) {
                                    productos = productos.map {
                                        if (it.id == producto.id) it.copy(disponible = nuevoEstado) else it
                                    }
                                }
                            },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiProductoCard(
    producto: Producto,
    onToggleDisponible: (Boolean) -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (producto.disponible) DarkSurface else DarkSurface.copy(alpha = 0.5f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (producto.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = null,
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
                Text(
                    producto.nombre,
                    color = if (producto.disponible) Color.White else Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(producto.categoria, color = Color.Gray, fontSize = 12.sp)
                Text(
                    "\$${String.format("%.2f", producto.precio)}",
                    color = GreenBtn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (producto.disponible) "Disponible" else "No disponible",
                        color = if (producto.disponible) GreenBtn else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = producto.disponible,
                    onCheckedChange = { onToggleDisponible(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBtn)
                )
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedCancel, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
