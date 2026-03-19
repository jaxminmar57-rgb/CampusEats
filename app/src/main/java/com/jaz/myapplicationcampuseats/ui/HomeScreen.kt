package com.jaz.myapplicationcampuseats.ui

import android.content.Context
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.viewmodel.HomeViewModel

val todasCategorias = listOf(
    Pair("Hamburguesas", R.drawable.hamburguesa),
    Pair("Pizza",        R.drawable.pizza),
    Pair("Pastas",       R.drawable.pastas),
    Pair("Bebidas",      R.drawable.bebidas),
    Pair("Ensaladas",    R.drawable.ensaladas),
    Pair("Burritos",     R.drawable.burritos),
    Pair("Sandwich",     R.drawable.sandwich),
    Pair("Otros",        R.drawable.otros)
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
    onVerTienda: (vendedorId: String) -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val uid = usuario?.uid ?: ""

    // Iniciar listeners UNA vez (sobrevive rotación, recomposición, etc.)
    LaunchedEffect(uid) { vm.iniciarListeners(uid) }

    // Estado local de UI (no es lógica de negocio)
    var busqueda     by remember { mutableStateOf("") }
    var menuAbierto  by remember { mutableStateOf(false) }
    var fabExpandido by remember { mutableStateOf(false) }
    var mostrarDialogoUbicacion by remember { mutableStateOf(false) }
    var nuevaUbicacion by remember { mutableStateOf(usuario?.ubicacionDescripcion ?: "") }
    var guardandoUbicacion by remember { mutableStateOf(false) }

    // Estado del ViewModel (reactivo)
    val productos       = vm.productos
    val cargando        = vm.cargando
    val pedidosActivos  = vm.pedidosActivos
    val carritoCount    = vm.carritoCount
    val notifNoLeidas   = vm.notifNoLeidas
    val mensajesNuevosTotal = vm.mensajesNuevosTotal

    // ── Datos derivados ────────────────────────────────────────────────────────
    val categoriasConProductos = remember(productos) {
        val cats = productos.map { it.categoria }.toSet()
        todasCategorias.filter { (nombre, _) -> nombre in cats }
    }
    val populares = remember(productos) {
        productos.sortedByDescending { it.rating * (it.numResenas + 1) }.take(6)
    }
    val productosFiltrados = if (busqueda.isEmpty()) productos
    else productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
        it.categoria.contains(busqueda, ignoreCase = true) ||
        it.nombreVendedor.contains(busqueda, ignoreCase = true)
    }

    // ── Diálogo de ubicación rápida ───────────────────────────────────────────
    if (mostrarDialogoUbicacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoUbicacion = false },
            containerColor = DarkSurface,
            title = { Text("📍 Actualizar mi ubicación", color = Color.White) },
            text = {
                Column {
                    Text("Los clientes con pedidos de recogida verán esta ubicación.",
                        color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nuevaUbicacion,
                        onValueChange = { nuevaUbicacion = it },
                        placeholder = { Text("Ej: Edificio A, planta baja, frente al gym", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = camposColores(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        guardandoUbicacion = true
                        vm.actualizarUbicacion(nuevaUbicacion.trim()) {
                            guardandoUbicacion = false
                            mostrarDialogoUbicacion = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                    enabled = !guardandoUbicacion
                ) {
                    if (guardandoUbicacion) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    else Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoUbicacion = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Barra superior
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Menu, "Menú", tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable { menuAbierto = true })
                Text("Hola, ${usuario?.nombre ?: ""}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Badge pedidos activos
                    if (pedidosActivos.isNotEmpty()) {
                        Box {
                            IconButton(onClick = onHistorial) {
                                Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            BadgeNumero(pedidosActivos.size, OrangeWarn,
                                Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 4.dp))
                        }
                    }

                    // Badge mensajes nuevos — tiempo real con mapa
                    Box {
                        IconButton(onClick = onChats) {
                            Icon(Icons.Default.ChatBubble, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        if (mensajesNuevosTotal > 0) {
                            BadgeNumero(mensajesNuevosTotal, RedCancel,
                                Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 4.dp))
                        }
                    }

                    // Badge notificaciones
                    Box {
                        IconButton(onClick = onNotificaciones) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        if (notifNoLeidas > 0) {
                            BadgeNumero(notifNoLeidas, RedCancel,
                                Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 4.dp))
                        }
                    }

                    // Carrito con badge
                    Box {
                        IconButton(onClick = onCarrito) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        if (carritoCount > 0) {
                            BadgeNumero(carritoCount, GreenBtn,
                                Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 4.dp))
                        }
                    }
                }
            }

            // Búsqueda
            OutlinedTextField(value = busqueda, onValueChange = { busqueda = it },
                placeholder = { Text("¿Qué se te antoja hoy?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent, focusedBorderColor = GreenBtn,
                    unfocusedTextColor = Color(0xFF1A1A1A), focusedTextColor = Color(0xFF1A1A1A)),
                singleLine = true)

            Spacer(modifier = Modifier.height(12.dp))

            // Pedidos activos
            AnimatedVisibility(visible = pedidosActivos.isNotEmpty() && busqueda.isEmpty(),
                enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(0.8f, 1.2f,
                            infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
                        Box(modifier = Modifier.size(10.dp).scale(pulseScale).clip(CircleShape).background(GreenBtn))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pedidos activos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onHistorial) { Text("Ver todos", color = GreenBtn, fontSize = 12.sp) }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pedidosActivos) { pedido ->
                            val esDeVendedor = pedido.vendedorId == uid
                            PedidoActivoCard(pedido = pedido, esDeVendedor = esDeVendedor,
                                onClick = { onAbrirPedido(pedido.id) },
                                onChat = {
                                    val otro = if (esDeVendedor) pedido.nombreCliente else pedido.nombreVendedor
                                    onAbrirChat(pedido.id, otro)
                                })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (busqueda.isEmpty()) {
                if (categoriasConProductos.isNotEmpty()) {
                    Text("Categorías", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categoriasConProductos) { (nombre, imagen) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onCategoria(nombre) }) {
                                Box(modifier = Modifier.size(62.dp).clip(CircleShape).background(DarkSurface),
                                    contentAlignment = Alignment.Center) {
                                    Image(painterResource(id = imagen), nombre,
                                        modifier = Modifier.size(46.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(nombre, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                if (populares.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 18.sp); Spacer(modifier = Modifier.width(6.dp))
                        Text("Populares", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(populares) { ProductoPopularCard(producto = it, onClick = { onVerTienda(it.vendedorId) }) }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Text(if (busqueda.isEmpty()) "Disponibles ahora" else "Resultados para \"$busqueda\"",
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(10.dp))

            when {
                cargando -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenBtn) }
                productosFiltrados.isEmpty() -> Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp); Spacer(modifier = Modifier.height(12.dp))
                    Text(if (busqueda.isEmpty()) "No hay platillos disponibles" else "Sin resultados para \"$busqueda\"",
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                else -> Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    productosFiltrados.chunked(2).forEach { fila ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            fila.forEach { ProductoMiniCard(producto = it, modifier = Modifier.weight(1f), onClick = { onVerTienda(it.vendedorId) }) }
                            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Speed Dial
        if (fabExpandido) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                .clickable { fabExpandido = false }.zIndex(1f))
        }
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp).zIndex(2f),
            horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedVisibility(visible = fabExpandido,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SpeedDialOpcion(Icons.Default.Notifications, "Notificaciones") { fabExpandido = false; onNotificaciones() }
                    SpeedDialOpcion(Icons.Default.Receipt, "Mis pedidos") { fabExpandido = false; onHistorial() }
                    SpeedDialOpcion(Icons.Default.ChatBubble, "Mensajes") { fabExpandido = false; onChats() }
                    SpeedDialOpcion(Icons.Default.ShoppingCart, "Ver carrito") { fabExpandido = false; onCarrito() }
                    SpeedDialOpcion(Icons.Default.AddBox, "Publicar platillo") { fabExpandido = false; onPublicar() }
                }
            }
            FloatingActionButton(onClick = { fabExpandido = !fabExpandido }, containerColor = GreenBtn,
                contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(60.dp)) {
                Icon(if (fabExpandido) Icons.Default.Close else Icons.Default.Add, "Acciones", modifier = Modifier.size(28.dp))
            }
        }

        // Menú lateral
        if (menuAbierto) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
                .clickable { menuAbierto = false }.zIndex(10f))
            Column(modifier = Modifier.fillMaxHeight().width(275.dp).align(Alignment.CenterStart)
                .background(Brush.verticalGradient(listOf(Color(0xFF1A3320), Color(0xFF0D1F17))))
                .padding(horizontal = 20.dp, vertical = 24.dp).zIndex(11f).clickable(enabled = false) {}) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(DarkSurface),
                        contentAlignment = Alignment.Center) {
                        if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                            AsyncImage(model = usuario.fotoPerfil, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
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
                // Actualizar ubicación rápido (solo si su preferencia es cliente_recoge o ambos)
                if (usuario?.preferenciaEntrega != "vendedor_lleva") {
                    MenuOpcion(Icons.Default.LocationOn, "Actualizar mi ubicación", tint = OrangeWarn) {
                        menuAbierto = false
                        nuevaUbicacion = usuario?.ubicacionDescripcion ?: ""
                        mostrarDialogoUbicacion = true
                    }
                }

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

// ── Composables reutilizables ─────────────────────────────────────────────────

@Composable
fun BadgeNumero(numero: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
        .clip(CircleShape).background(color).padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center) {
        Text(if (numero > 99) "99+" else "$numero", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PedidoActivoCard(pedido: Pedido, esDeVendedor: Boolean, onClick: () -> Unit, onChat: () -> Unit) {
    val info = estadoInfo(pedido.estado)
    val esListo = pedido.estado == "listo"
    val infiniteTransition = rememberInfiniteTransition(label = "listoHome")
    val borderAlpha by infiniteTransition.animateFloat(0.3f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "borderAlpha")

    Card(modifier = Modifier.width(220.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = info.color.copy(alpha = 0.12f)),
        border = if (esListo)
            androidx.compose.foundation.BorderStroke(2.dp, info.color.copy(alpha = borderAlpha))
        else
            androidx.compose.foundation.BorderStroke(1.dp, info.color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(info.color).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(info.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(info.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(tiempoTranscurrido(pedido.fecha), color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(4.dp),
                color = if (esDeVendedor) OrangeWarn.copy(alpha = 0.2f) else GreenBtn.copy(alpha = 0.2f)) {
                Text(if (esDeVendedor) "📦 Como vendedor" else "🛒 Como cliente",
                    color = if (esDeVendedor) OrangeWarn else GreenBtn,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(if (esDeVendedor) "👤 ${pedido.nombreCliente}" else "🏪 ${pedido.nombreVendedor}",
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${pedido.items.size} producto${if (pedido.items.size != 1) "s" else ""} · \$${String.format("%.0f", pedido.total)}",
                color = Color.Gray, fontSize = 12.sp)
            Text(textoEntregaCorto(pedido.preferenciaEntrega), color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onChat,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(info.color.copy(alpha = 0.3f))) {
                    Icon(Icons.Default.ChatBubble, null, tint = info.color, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProductoPopularCard(producto: Producto, onClick: () -> Unit) {
    Card(modifier = Modifier.width(160.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column {
            Box {
                if (producto.imagenUrl.isNotEmpty()) {
                    AsyncImage(model = producto.imagenUrl, contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 32.sp) }
                }
                Surface(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.7f)) {
                    Text("⭐ ${String.format("%.1f", producto.rating)}", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
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

@Composable
fun ProductoMiniCard(producto: Producto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.padding(vertical = 6.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column {
            if (producto.imagenUrl.isNotEmpty()) {
                AsyncImage(model = producto.imagenUrl, contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 36.sp) }
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
                if (producto.mostrarCantidad && producto.cantidadDisponible >= 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(if (producto.cantidadDisponible == 0) "Sin stock" else "${producto.cantidadDisponible} disponibles",
                        color = if (producto.cantidadDisponible == 0) RedCancel else OrangeWarn, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SpeedDialOpcion(icono: ImageVector, etiqueta: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.clickable { onClick() }) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3320), shadowElevation = 4.dp) {
            Text(etiqueta, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(onClick = onClick, containerColor = DarkCard, contentColor = GreenBtn, shape = CircleShape, modifier = Modifier.size(44.dp)) {
            Icon(icono, etiqueta, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun MenuOpcion(icono: ImageVector, texto: String, tint: Color = Color.White, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, texto, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(texto, color = tint, fontSize = 15.sp)
    }
}
