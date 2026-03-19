package com.jaz.myapplicationcampuseats.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

val todasCategorias = listOf(
    Pair("Hamburguesas", R.drawable.hamburguesa),
    Pair("Pizza", R.drawable.pizza),
    Pair("Pastas", R.drawable.pastas),
    Pair("Bebidas", R.drawable.bebidas),
    Pair("Ensaladas", R.drawable.ensaladas),
    Pair("Burritos", R.drawable.burritos),
    Pair("Sandwich", R.drawable.sandwich),
    Pair("Otros", R.drawable.otros)
)

@Composable
fun HomeScreen(
    usuario: Usuario?,
    onCarrito: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCuenta: () -> Unit,
    onHistorial: () -> Unit,
    onCategoria: (String) -> Unit,
    onPublicar: () -> Unit,
    onNotificaciones: () -> Unit,
    onPedidosVendedor: () -> Unit,
    onAjustes: () -> Unit,
    onMisPublicaciones: () -> Unit,
    onChats: () -> Unit,
    onAbrirPedido: (String) -> Unit,
    onAbrirChat: (pedidoId: String, otroNombre: String) -> Unit,
    onVerTienda: (vendedorId: String) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var menuAbierto by remember { mutableStateOf(false) }
    var fabExpandido by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var pedidosActivos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Listener de productos en tiempo real
    DisposableEffect(Unit) {
        val listener = ProductoRepository.escucharProductosDisponibles { lista ->
            productos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    // Listener de pedidos activos del cliente
    DisposableEffect(usuario?.uid) {
        val uid = usuario?.uid ?: return@DisposableEffect onDispose {}
        val listener = PedidoRepository.escucharPedidosCliente(uid) { lista ->
            pedidosActivos = lista.filter { it.estado !in listOf("completado", "cancelado") }
        }
        onDispose { listener.remove() }
    }

    // Categorías con productos
    val categoriasConProductos = remember(productos) {
        val cats = productos.map { it.categoria }.toSet()
        todasCategorias.filter { (nombre, _) -> nombre in cats }
    }

    // Productos populares (top 6 por rating * numResenas)
    val populares = remember(productos) {
        productos.sortedByDescending { it.rating * (it.numResenas + 1) }.take(6)
    }

    val productosFiltrados = if (busqueda.isEmpty()) productos
    else productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
        it.categoria.contains(busqueda, ignoreCase = true) ||
        it.nombreVendedor.contains(busqueda, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        // ── Contenido principal ──
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Barra superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable { menuAbierto = true }
                )
                Text(
                    text = "Hola, ${usuario?.nombre ?: ""}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge de pedidos activos
                    if (pedidosActivos.isNotEmpty()) {
                        Box {
                            IconButton(onClick = onHistorial) {
                                Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(RedCancel),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${pedidosActivos.size}",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(onClick = onNotificaciones) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = onCarrito) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // Barra de búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("¿Qué se te antoja hoy?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenBtn,
                    unfocusedTextColor = Color(0xFF1A1A1A),
                    focusedTextColor = Color(0xFF1A1A1A)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Pedidos activos ──
            AnimatedVisibility(
                visible = pedidosActivos.isNotEmpty() && busqueda.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Punto pulsante
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.8f, targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "pulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(GreenBtn)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mis pedidos activos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onHistorial) {
                            Text("Ver todos", color = GreenBtn, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pedidosActivos) { pedido ->
                            PedidoActivoCard(
                                pedido = pedido,
                                onClick = { onAbrirPedido(pedido.id) },
                                onChat = {
                                    onAbrirChat(pedido.id, pedido.nombreVendedor)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (busqueda.isEmpty()) {
                // ── Categorías dinámicas ──
                if (categoriasConProductos.isNotEmpty()) {
                    Text(
                        "Categorías",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categoriasConProductos) { (nombre, imagen) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onCategoria(nombre) }
                            ) {
                                Box(
                                    modifier = Modifier.size(62.dp).clip(CircleShape).background(DarkSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = imagen),
                                        contentDescription = nombre,
                                        modifier = Modifier.size(46.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(nombre, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Populares ──
                if (populares.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Populares", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(populares) { producto ->
                            ProductoPopularCard(
                                producto = producto,
                                onClick = { onVerTienda(producto.vendedorId) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ── Todos los productos ──
            Text(
                text = if (busqueda.isEmpty()) "Disponibles ahora"
                       else "Resultados para \"$busqueda\"",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            when {
                cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenBtn)
                    }
                }
                productosFiltrados.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (busqueda.isEmpty()) "No hay platillos disponibles"
                            else "Sin resultados para \"$busqueda\"",
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        productosFiltrados.chunked(2).forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                fila.forEach { producto ->
                                    ProductoMiniCard(
                                        producto = producto,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onVerTienda(producto.vendedorId) }
                                    )
                                }
                                if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ── Speed Dial FAB ──
        if (fabExpandido) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { fabExpandido = false }
                    .zIndex(1f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .zIndex(2f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = fabExpandido,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpeedDialOpcion(Icons.Default.Notifications, "Notificaciones") {
                        fabExpandido = false; onNotificaciones()
                    }
                    SpeedDialOpcion(Icons.Default.Receipt, "Mis pedidos") {
                        fabExpandido = false; onHistorial()
                    }
                    SpeedDialOpcion(Icons.Default.ChatBubble, "Mensajes") {
                        fabExpandido = false; onChats()
                    }
                    SpeedDialOpcion(Icons.Default.ShoppingCart, "Ver carrito") {
                        fabExpandido = false; onCarrito()
                    }
                    SpeedDialOpcion(Icons.Default.AddBox, "Publicar platillo") {
                        fabExpandido = false; onPublicar()
                    }
                }
            }

            FloatingActionButton(
                onClick = { fabExpandido = !fabExpandido },
                containerColor = GreenBtn,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    if (fabExpandido) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Acciones",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ── Menú lateral ──
        if (menuAbierto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { menuAbierto = false }
                    .zIndex(10f)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(275.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A3320), Color(0xFF0D1F17))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .zIndex(11f)
                    .clickable(enabled = false) {}
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                            AsyncImage(
                                model = usuario.fotoPerfil,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(usuario?.nombre ?: "", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(usuario?.correo ?: "", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                Text("CLIENTE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                MenuOpcion(Icons.Default.Receipt, "Mis pedidos") { menuAbierto = false; onHistorial() }
                MenuOpcion(Icons.Default.ShoppingCart, "Carrito") { menuAbierto = false; onCarrito() }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("VENDEDOR", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                MenuOpcion(Icons.Default.Add, "Publicar platillo") { menuAbierto = false; onPublicar() }
                MenuOpcion(Icons.Default.List, "Pedidos recibidos") { menuAbierto = false; onPedidosVendedor() }
                MenuOpcion(Icons.Default.RestaurantMenu, "Mis publicaciones") { menuAbierto = false; onMisPublicaciones() }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                MenuOpcion(Icons.Default.ChatBubble, "Chats") { menuAbierto = false; onChats() }
                MenuOpcion(Icons.Default.Notifications, "Notificaciones") { menuAbierto = false; onNotificaciones() }
                MenuOpcion(Icons.Default.Person, "Mi cuenta") { menuAbierto = false; onCuenta() }
                MenuOpcion(Icons.Default.Settings, "Ajustes") { menuAbierto = false; onAjustes() }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                MenuOpcion(Icons.Default.ExitToApp, "Cerrar sesión", tint = RedCancel) {
                    menuAbierto = false; onCerrarSesion()
                }
            }
        }
    }
}

// ── Tarjeta de pedido activo en HomeScreen ──
@Composable
fun PedidoActivoCard(
    pedido: Pedido,
    onClick: () -> Unit,
    onChat: () -> Unit
) {
    val estadoInfo = estadoInfo(pedido.estado)
    val tiempoTexto = tiempoTranscurrido(pedido.fecha)

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = estadoInfo.color.copy(alpha = 0.12f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(estadoInfo.color.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Estado con color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(estadoInfo.color)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(estadoInfo.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        estadoInfo.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(tiempoTexto, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "🏪 ${pedido.nombreVendedor}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""} · \$${String.format("%.0f", pedido.total)}",
                color = Color.Gray,
                fontSize = 12.sp
            )

            // Primera item del pedido
            val primerItem = pedido.items.firstOrNull()
            val nombreItem = primerItem?.get("nombre") as? String ?: ""
            if (nombreItem.isNotEmpty()) {
                Text(nombreItem, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón chat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onChat,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(estadoInfo.color.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.ChatBubble,
                        null,
                        tint = estadoInfo.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Tarjeta de producto popular (horizontal) ──
@Composable
fun ProductoPopularCard(producto: Producto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            Box {
                if (producto.imagenUrl.isNotEmpty()) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("🍽️", fontSize = 32.sp) }
                }
                // Badge rating
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        "⭐ ${String.format("%.1f", producto.rating)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(producto.nombre, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(producto.nombreVendedor, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                Text("\$${String.format("%.0f", producto.precio)}", color = GreenBtn, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Helpers ──

data class EstadoInfo(val color: Color, val emoji: String, val label: String)

fun estadoInfo(estado: String): EstadoInfo = when (estado) {
    "pendiente"  -> EstadoInfo(OrangeWarn, "⏳", "Pendiente")
    "en_espera"  -> EstadoInfo(OrangeWarn, "🕐", "En espera")
    "aceptado"   -> EstadoInfo(GreenBtn,   "✅", "Aceptado")
    "listo"      -> EstadoInfo(GreenBtn,   "🎉", "¡Listo!")
    "completado" -> EstadoInfo(GreenLight, "🏆", "Completado")
    "cancelado"  -> EstadoInfo(RedCancel,  "❌", "Cancelado")
    else         -> EstadoInfo(Color.Gray, "❓", estado)
}

fun tiempoTranscurrido(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutos = diff / 60_000
    return when {
        minutos < 1  -> "ahora"
        minutos < 60 -> "${minutos}min"
        else         -> "${minutos / 60}h ${minutos % 60}min"
    }
}

@Composable
fun ProductoMiniCard(
    producto: Producto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.padding(vertical = 6.dp).clickable { onClick() },
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
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("🍽️", fontSize = 36.sp) }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(producto.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(producto.nombreVendedor, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 11.sp)
                    Text(" ${String.format("%.1f", producto.rating)}", color = Color.Gray, fontSize = 11.sp)
                }
                Text("\$${String.format("%.0f", producto.precio)}", color = GreenBtn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SpeedDialOpcion(icono: ImageVector, etiqueta: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1A3320),
            shadowElevation = 4.dp
        ) {
            Text(
                etiqueta,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onClick,
            containerColor = DarkCard,
            contentColor = GreenBtn,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(icono, contentDescription = etiqueta, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun MenuOpcion(
    icono: ImageVector,
    texto: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = texto, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(texto, color = tint, fontSize = 15.sp)
    }
}
