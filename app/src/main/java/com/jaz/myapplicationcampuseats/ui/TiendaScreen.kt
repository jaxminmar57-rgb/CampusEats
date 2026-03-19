package com.jaz.myapplicationcampuseats.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.model.Tienda
import com.jaz.myapplicationcampuseats.repository.CarritoRepository
import com.jaz.myapplicationcampuseats.repository.TiendaRepository

@Composable
fun TiendaScreen(
    vendedorId: String,
    userId: String,
    onVolver: () -> Unit,
    onVerPerfil: (uid: String) -> Unit,
    onCarrito: () -> Unit
) {
    var tienda by remember { mutableStateOf<Tienda?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var snackMsg by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        if (snackMsg.isNotEmpty()) {
            snackState.showSnackbar(snackMsg)
            snackMsg = ""
        }
    }

    LaunchedEffect(vendedorId) {
        TiendaRepository.obtenerTiendaDeVendedor(vendedorId) {
            tienda = it
            cargando = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
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
                    tienda?.nombreVendedor ?: "Tienda",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCarrito) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                }
            }

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn)
                }
                return@Scaffold
            }

            val t = tienda
            if (t == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró la tienda", color = Color.Gray)
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Banner de la tienda
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar del vendedor
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface2)
                                        .clickable { onVerPerfil(t.vendedorId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (t.fotoPerfil.isNotEmpty()) {
                                        AsyncImage(
                                            model = t.fotoPerfil,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Store, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        t.nombreVendedor,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Rating
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("⭐", fontSize = 13.sp)
                                            Text(
                                                " ${String.format("%.1f", t.ratingPromedio)}",
                                                color = GoldStar,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                " (${t.numResenas})",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text("·", color = Color.Gray)
                                        Text(
                                            "${t.numProductos} platillo${if (t.numProductos != 1) "s" else ""}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                // Botón ver perfil
                                IconButton(onClick = { onVerPerfil(t.vendedorId) }) {
                                    Icon(Icons.Default.Person, null, tint = GreenBtn, modifier = Modifier.size(22.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Métricas rápidas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MetricaTienda(
                                    icono = "📦",
                                    valor = when (t.preferenciaEntrega) {
                                        "cliente_recoge" -> "Recoger"
                                        "vendedor_lleva" -> "A domicilio"
                                        else -> "Flexible"
                                    },
                                    label = "Entrega"
                                )
                                if (t.ubicacionDescripcion.isNotEmpty()) {
                                    MetricaTienda(
                                        icono = "📍",
                                        valor = t.ubicacionDescripcion,
                                        label = "Ubicación"
                                    )
                                }
                                MetricaTienda(
                                    icono = "🍽️",
                                    valor = "${t.numProductos}",
                                    label = "Platillos"
                                )
                            }
                        }
                    }
                }

                // Título sección productos
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Menú",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("más populares primero", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // Productos de la tienda
                items(t.productos) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen
                            if (producto.imagenUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = producto.imagenUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(DarkSurface2, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text("🍽️", fontSize = 28.sp) }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    producto.nombre,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (producto.descripcion.isNotEmpty()) {
                                    Text(
                                        producto.descripcion,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                }
                                if (producto.ingredientes.isNotEmpty()) {
                                    Text(
                                        "🥘 ${producto.ingredientes}",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⭐", fontSize = 11.sp)
                                        Text(
                                            " ${String.format("%.1f", producto.rating)}",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        "\$${String.format("%.0f", producto.precio)}",
                                        color = GreenBtn,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Botón agregar al carrito
                            FloatingActionButton(
                                onClick = {
                                    if (userId.isEmpty()) return@FloatingActionButton
                                    val item = ItemCarrito(
                                        productoId = producto.id,
                                        nombre = producto.nombre,
                                        precio = producto.precio,
                                        imagenUrl = producto.imagenUrl,
                                        vendedorId = producto.vendedorId,
                                        nombreVendedor = producto.nombreVendedor
                                    )
                                    CarritoRepository.agregarProducto(userId, item)
                                    snackMsg = "\"${producto.nombre}\" agregado al carrito"
                                },
                                containerColor = GreenBtn,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricaTienda(icono: String, valor: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icono, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(valor, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}
