package com.jaz.myapplicationcampuseats.ui

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
    var tienda     by remember { mutableStateOf<Tienda?>(null) }
    var cargando   by remember { mutableStateOf(true) }
    var snackMsg   by remember { mutableStateOf("") }
    var carritoCount by remember { mutableStateOf(0) }
    val snackState = remember { SnackbarHostState() }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // Diálogo de bloqueo (compra propia)
    var msgBloqueado by remember { mutableStateOf("") }

    LaunchedEffect(snackMsg) {
        if (snackMsg.isNotEmpty()) { snackState.showSnackbar(snackMsg); snackMsg = "" }
    }

    LaunchedEffect(vendedorId) {
        TiendaRepository.obtenerTiendaDeVendedor(vendedorId) { tienda = it; cargando = false }
    }

    // Carrito en tiempo real — igual que HomeScreen
    DisposableEffect(userId) {
        if (userId.isEmpty()) return@DisposableEffect onDispose {}
        val ref = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("usuarios").document(userId).collection("carrito")
        val listener = ref.addSnapshotListener { snap, _ ->
            carritoCount = snap?.documents?.sumOf { (it.getLong("cantidad") ?: 1L).toInt() } ?: 0
        }
        onDispose { listener.remove() }
    }

    // Mostrar mensaje de bloqueo
    LaunchedEffect(msgBloqueado) {
        if (msgBloqueado.isNotEmpty()) {
            snackState.showSnackbar(msgBloqueado)
            msgBloqueado = ""
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackState) }, containerColor = DarkBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(DarkBg).statusBarsPadding()) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Text(tienda?.nombreVendedor ?: "Tienda", color = Color.White,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                // Carrito con badge en tiempo real
                Box {
                    IconButton(onClick = onCarrito) {
                        Icon(Icons.Default.ShoppingCart, "Carrito", tint = Color.White)
                    }
                    if (carritoCount > 0) {
                        BadgeNumero(
                            numero = carritoCount, color = GreenBtn,
                            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 4.dp)
                        )
                    }
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

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {

                // Banner de la tienda
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(64.dp).clip(CircleShape)
                                    .background(DarkSurface2).clickable(onClickLabel = "Ver perfil") { onVerPerfil(t.vendedorId) },
                                    contentAlignment = Alignment.Center) {
                                    if (t.fotoPerfil.isNotEmpty()) {
                                        AsyncImage(model = t.fotoPerfil, contentDescription = "Imagen",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop)
                                    } else {
                                        Icon(Icons.Default.Store, "Tienda", tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.nombreVendedor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("⭐", fontSize = 13.sp)
                                            val storeRating = if (t.ratingPromedio > 0) String.format("%.1f", t.ratingPromedio) else "-"
                                            Text(" $storeRating", color = GoldStar, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(" (${t.numResenas})", color = Color.Gray, fontSize = 12.sp)
                                        }
                                        Text("·", color = Color.Gray)
                                        Text("${t.numProductos} platillo${if (t.numProductos != 1) "s" else ""}", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { onVerPerfil(t.vendedorId) }) {
                                    Icon(Icons.Default.Person, "Perfil", tint = GreenBtn, modifier = Modifier.size(22.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MetricaTienda(icono = "📦",
                                    valor = when (t.preferenciaEntrega) {
                                        "cliente_recoge" -> "Recoger"
                                        "vendedor_lleva" -> "A domicilio"
                                        else -> "Flexible" },
                                    label = "Entrega")
                                if (t.ubicacionDescripcion.isNotEmpty()) {
                                    MetricaTienda(icono = "📍", valor = t.ubicacionDescripcion, label = "Ubicación")
                                }
                                MetricaTienda(icono = "🍽️", valor = "${t.numProductos}", label = "Platillos")
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Menú", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("más populares primero", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                items(t.productos) { producto ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (producto.imagenUrl.isNotEmpty()) {
                                AsyncImage(model = producto.imagenUrl, contentDescription = "Imagen",
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.size(72.dp).background(DarkSurface2, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 28.sp) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(producto.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                if (producto.descripcion.isNotEmpty()) Text(producto.descripcion, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                                if (producto.ingredientes.isNotEmpty()) Text("🥘 ${producto.ingredientes}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⭐", fontSize = 11.sp)
                                        val rTxt = if (producto.rating > 0) String.format("%.1f", producto.rating) else "-"
                                        Text(" $rTxt", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Text("\$${String.format("%.0f", producto.precio)}", color = GreenBtn, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            val esPropioProducto = producto.vendedorId == userId
                            FloatingActionButton(
                                onClick = {
                                    if (esPropioProducto) {
                                        msgBloqueado = "🚫 No puedes comprar tus propios productos"
                                        com.jaz.myapplicationcampuseats.service.SoundManager.playError(ctx)
                                        return@FloatingActionButton
                                    }
                                    if (userId.isEmpty()) return@FloatingActionButton
                                    val nuevoItem = ItemCarrito(
                                        productoId = producto.id, nombre = producto.nombre,
                                        precio = producto.precio, imagenUrl = producto.imagenUrl,
                                        vendedorId = producto.vendedorId, nombreVendedor = producto.nombreVendedor)
                                    CarritoRepository.agregarProducto(
                                        userId = userId,
                                        item = nuevoItem,
                                        onSuccess = {
                                            snackMsg = "\"${producto.nombre}\" agregado al carrito"
                                            com.jaz.myapplicationcampuseats.service.SoundManager.playAgregarCarrito(ctx)
                                        },
                                        onBloqueado = { msg ->
                                            msgBloqueado = msg
                                            com.jaz.myapplicationcampuseats.service.SoundManager.playError(ctx)
                                        }
                                    )
                                },
                                containerColor = if (esPropioProducto) Color.Gray else GreenBtn,
                                contentColor = Color.White,
                                shape = CircleShape, modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    if (esPropioProducto) Icons.Default.Block else Icons.Default.Add,
                                    if (esPropioProducto) "No disponible" else "Agregar",
                                    modifier = Modifier.size(20.dp))
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
