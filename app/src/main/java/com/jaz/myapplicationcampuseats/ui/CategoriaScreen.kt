package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
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
    onVolver: () -> Unit,
    onVerTienda: ((vendedorId: String) -> Unit)? = null
) {
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var snackMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val catCtx = androidx.compose.ui.platform.LocalContext.current

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
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Text(
                    text = categoria,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn)
                }
                productos.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍽️", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No hay platillos en esta categoría", color = Color.White, fontSize = 15.sp)
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(productos) { producto ->
                        ProductoCard(
                            producto = producto,
                            onAgregarAlCarrito = {
                                val item = ItemCarrito(
                                    productoId    = producto.id,
                                    nombre        = producto.nombre,
                                    precio        = producto.precio,
                                    imagenUrl     = producto.imagenUrl,
                                    vendedorId    = producto.vendedorId,
                                    nombreVendedor = producto.nombreVendedor
                                )
                                CarritoRepository.agregarProducto(
                                    userId = usuarioId,
                                    item = item,
                                    onSuccess = {
                                        snackMessage = "\"${producto.nombre}\" agregado al carrito"
                                        com.jaz.myapplicationcampuseats.service.SoundManager.playAgregarCarrito(catCtx)
                                    },
                                    onBloqueado = { msg ->
                                        snackMessage = "🚫 $msg"
                                        com.jaz.myapplicationcampuseats.service.SoundManager.playError(catCtx)
                                    }
                                )
                            },
                            onVerTienda = onVerTienda?.let { cb -> { cb(producto.vendedorId) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onAgregarAlCarrito: () -> Unit,
    onVerTienda: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // Imagen
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
                        Text(
                            producto.nombre,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Vendedor clickeable → ir a tienda
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (onVerTienda != null)
                                Modifier.clickable { onVerTienda() }
                            else Modifier
                        ) {
                            Text(
                                "Por ${producto.nombreVendedor}",
                                color = GreenLight,
                                fontSize = 12.sp
                            )
                            if (onVerTienda != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Store,
                                    null,
                                    tint = GreenLight,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
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
                    Icon(Icons.Default.Star, "Calificación", tint = GoldStar, modifier = Modifier.size(14.dp))
                    val catRating = if (producto.rating > 0) String.format("%.1f", producto.rating) else "-"
                    Text(
                        " $catRating (${producto.numResenas} reseñas)",
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Botón ver tienda
                    if (onVerTienda != null) {
                        OutlinedButton(
                            onClick = onVerTienda,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(Icons.Default.Store, "Tienda", tint = GreenBtn, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tienda", color = GreenBtn, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = onAgregarAlCarrito,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                    ) {
                        Text("Agregar al carrito", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
