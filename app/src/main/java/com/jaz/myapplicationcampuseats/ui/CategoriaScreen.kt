package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.CarritoRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

@Composable
fun CategoriaScreen(
    categoria: String,
    usuarioId: String,
    onVolver: () -> Unit
) {
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var snackMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listener en tiempo real por categoría
    DisposableEffect(categoria) {
        val listener = ProductoRepository.escucharProductosPorCategoria(categoria) { lista ->
            productos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    LaunchedEffect(snackMessage) {
        if (snackMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackMessage)
            snackMessage = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                Text(
                    text = categoria,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
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
                            Text("No hay platillos en esta categoría", color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(productos) { producto ->
                            ProductoCard(
                                producto = producto,
                                onAgregarAlCarrito = {
                                    val item = ItemCarrito(
                                        productoId = producto.id,
                                        nombre = producto.nombre,
                                        precio = producto.precio,
                                        imagenUrl = producto.imagenUrl,
                                        vendedorId = producto.vendedorId,
                                        nombreVendedor = producto.nombreVendedor
                                    )
                                    CarritoRepository.agregarProducto(usuarioId, item)
                                    snackMessage = "\"${producto.nombre}\" agregado al carrito"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onAgregarAlCarrito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            if (producto.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("🍽️", fontSize = 48.sp) }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(producto.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Por ${producto.nombreVendedor}",
                            color = GreenLight,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        "\$${String.format("%.0f", producto.precio)}",
                        color = GreenBtn,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = GoldStar, modifier = Modifier.size(14.dp))
                    Text(
                        " ${String.format("%.1f", producto.rating)} (${producto.numResenas} reseñas)",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (producto.descripcion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(producto.descripcion, color = Color.Gray, fontSize = 13.sp, maxLines = 2)
                }

                if (producto.ingredientes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "🧾 ${producto.ingredientes}",
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAgregarAlCarrito,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                ) {
                    Text("Agregar al carrito", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
