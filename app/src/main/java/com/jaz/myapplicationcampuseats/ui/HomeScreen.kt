package com.jaz.myapplicationcampuseats.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import com.jaz.myapplicationcampuseats.viewmodel.HomeViewModel

val todasCategorias = listOf(
    Pair("Hamburguesas", R.drawable.hamburguesa),
    Pair("Pizza",        R.drawable.pizza),
    Pair("Pastas",       R.drawable.pastas),
    Pair("Bebidas",      R.drawable.bebidas),
    Pair("Ensaladas",    R.drawable.ensaladas),
    Pair("Burritos",     R.drawable.burritos),
    Pair("Sandwich",     R.drawable.sandwich),
    Pair("Tacos",        R.drawable.otros),
    Pair("Tortas",       R.drawable.otros),
    Pair("Quesadillas",  R.drawable.otros),
    Pair("Hot Dogs",     R.drawable.otros),
    Pair("Sushi",        R.drawable.sushi),
    Pair("Alitas",       R.drawable.otros),
    Pair("Postres",      R.drawable.otros),
    Pair("Snacks",       R.drawable.otros),
    Pair("Desayunos",    R.drawable.otros),
    Pair("Comida Corrida", R.drawable.otros),
    Pair("Mariscos",     R.drawable.otros),
    Pair("Antojitos",    R.drawable.otros),
    Pair("Saludable",    R.drawable.ensaladas),
    Pair("Café",         R.drawable.bebidas),
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
    val vendedoresAbiertos  = vm.vendedoresAbiertos
    val negocioAbierto      = vm.negocioAbierto

    // ── Datos derivados ────────────────────────────────────────────────────────
    // Solo mostrar productos de vendedores con negocio abierto
    val productosVisibles = remember(productos, vendedoresAbiertos) {
        productos.filter { it.vendedorId in vendedoresAbiertos }
    }
    val categoriasConProductos = remember(productosVisibles) {
        val cats = productosVisibles.map { it.categoria }.toSet()
        todasCategorias.filter { (nombre, _) -> nombre in cats }
    }
    // Populares: mejor rating + más vendidos (score compuesto)
    val populares = remember(productosVisibles) {
        productosVisibles
            .sortedByDescending { it.rating * (it.numResenas + 1) + (it.ventasTotales * 0.5) }
            .take(8)
    }
    val productosFiltrados = if (busqueda.isEmpty()) productosVisibles
    else productosVisibles.filter {
        coincideFuzzy(it.nombre, busqueda) ||
        coincideFuzzy(it.categoria, busqueda) ||
        coincideFuzzy(it.nombreVendedor, busqueda) ||
        coincideFuzzy(it.descripcion, busqueda)
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

        Column(modifier = Modifier.fillMaxSize()
            .statusBarsPadding()) { // <- respeta la barra de estado del teléfono

            // ═══ HEADER FIJO (no se mueve al hacer scroll) ═══════════════════
            Column(modifier = Modifier.fillMaxWidth().background(DarkBg)) {

                // Barra superior
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, "Menú", tint = Color.White,
                        modifier = Modifier.size(26.dp).clickable { menuAbierto = true })
                    Text("Hola, ${usuario?.nombre?.split(" ")?.firstOrNull() ?: ""}", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pedidosActivos.isNotEmpty()) {
                            Box {
                                IconButton(onClick = onHistorial, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                BadgeNumero(pedidosActivos.size, OrangeWarn,
                                    Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                            }
                        }
                        Box {
                            IconButton(onClick = onChats, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.ChatBubble, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            if (mensajesNuevosTotal > 0) {
                                BadgeNumero(mensajesNuevosTotal, RedCancel,
                                    Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                            }
                        }
                        Box {
                            IconButton(onClick = onNotificaciones, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            if (notifNoLeidas > 0) {
                                BadgeNumero(notifNoLeidas, RedCancel,
                                    Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                            }
                        }
                        Box {
                            IconButton(onClick = onCarrito, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            if (carritoCount > 0) {
                                BadgeNumero(carritoCount, GreenBtn,
                                    Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                            }
                        }
                    }
                }

                // Búsqueda (más compacta)
                OutlinedTextField(value = busqueda, onValueChange = { busqueda = it },
                    placeholder = { Text("¿Qué se te antoja hoy?", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = GreenBtn,
                        unfocusedTextColor = Color(0xFF1A1A1A), focusedTextColor = Color(0xFF1A1A1A)),
                    singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp))

                // Barra de ubicación del vendedor (solo si tiene preferencia de recogida)
                if (usuario?.preferenciaEntrega != "vendedor_lleva") {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp)).background(DarkCard)
                        .clickable {
                            nuevaUbicacion = usuario?.ubicacionDescripcion ?: ""
                            mostrarDialogoUbicacion = true
                        }.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = OrangeWarn, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val ubicacion = usuario?.ubicacionDescripcion
                        Text(
                            if (ubicacion.isNullOrBlank()) "Toca para agregar tu ubicación"
                            else "📍 $ubicacion",
                            color = if (ubicacion.isNullOrBlank()) Color.Gray else Color.White,
                            fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // ═══ CONTENIDO SCROLLEABLE ═══════════════════════════════════════
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

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
        }

        // FAB — color refleja si el negocio está abierto o cerrado
        val fabColor = if (negocioAbierto) GreenBtn else RedCancel
        FloatingActionButton(
            onClick = onPublicar,
            containerColor = fabColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp).zIndex(2f).size(56.dp)
        ) {
            Icon(Icons.Default.Add, "Publicar platillo", modifier = Modifier.size(26.dp))
        }

        // Menú lateral
        if (menuAbierto) {
            // Back handler para cerrar menú con botón atrás
            androidx.activity.compose.BackHandler { menuAbierto = false }

            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                // Overlay oscuro
                Box(modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { menuAbierto = false })

                // Panel del menú
                Column(modifier = Modifier.fillMaxHeight().width(280.dp)
                    .align(Alignment.CenterStart)
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3320), Color(0xFF0D1F17))))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
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
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle abrir/cerrar negocio
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (negocioAbierto) GreenBtn.copy(alpha = 0.15f) else RedCancel.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (negocioAbierto) "🟢" else "🔴", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (negocioAbierto) "Negocio abierto" else "Negocio cerrado",
                            color = if (negocioAbierto) GreenBtn else RedCancel,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = negocioAbierto,
                        onCheckedChange = { vm.toggleNegocioAbierto(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenBtn,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = RedCancel.copy(alpha = 0.5f)),
                        modifier = Modifier.height(24.dp)
                    )
                }
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

                Spacer(modifier = Modifier.height(40.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                MenuOpcion(Icons.Default.ExitToApp, "Cerrar sesión", tint = RedCancel) {
                    menuAbierto = false; onCerrarSesion()
                }
                Spacer(modifier = Modifier.height(16.dp))
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

    // Resumen de productos
    val resumen = remember(pedido.items) {
        val nombres = pedido.items.take(1).mapNotNull { it["nombre"] as? String }
        val extra = if (pedido.items.size > 1) " +${pedido.items.size - 1}" else ""
        nombres.joinToString() + extra
    }

    Card(modifier = Modifier.width(210.dp).clickable { onClick() }, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = info.color.copy(alpha = 0.10f)),
        border = if (esListo)
            androidx.compose.foundation.BorderStroke(2.dp, info.color.copy(alpha = borderAlpha))
        else
            androidx.compose.foundation.BorderStroke(1.dp, info.color.copy(alpha = 0.3f))
    ) {
        Column {
            // Status bar — full width
            Row(modifier = Modifier.fillMaxWidth().background(info.color)
                .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(info.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(info.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(tiempoTranscurrido(pedido.fecha), color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
            }
            // Info
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (esDeVendedor) "👤 ${pedido.nombreCliente}" else "🏪 ${pedido.nombreVendedor}",
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    if (resumen.isNotEmpty()) {
                        Text(resumen, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
                    }
                    Text("${textoEntregaCorto(pedido.preferenciaEntrega)} · \$${String.format("%.0f", pedido.total)}",
                        color = Color.Gray, fontSize = 11.sp)
                }
                IconButton(onClick = onChat, modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(info.color.copy(alpha = 0.25f))) {
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
                // Rating badge overlay
                Surface(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.75f)) {
                    val r = if (producto.rating > 0) String.format("%.1f", producto.rating) else "-"
                    Text("⭐ $r", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(producto.nombre, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(producto.nombreVendedor, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                    if (producto.ubicacionVendedor.isNotEmpty()) {
                        Text(" · ", color = Color.Gray, fontSize = 10.sp)
                        Text("📍${producto.ubicacionVendedor}", color = OrangeWarn.copy(alpha = 0.8f), fontSize = 9.sp,
                            maxLines = 1, modifier = Modifier.weight(1f, fill = false).basicMarquee())
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("\$${String.format("%.0f", producto.precio)}", color = GreenBtn, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    if (producto.preferenciaEntregaVendedor.isNotEmpty()) {
                        Text(textoEntregaCorto(producto.preferenciaEntregaVendedor), color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoMiniCard(producto: Producto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.padding(vertical = 6.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column {
            Box {
                if (producto.imagenUrl.isNotEmpty()) {
                    AsyncImage(model = producto.imagenUrl, contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(DarkSurface2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 36.sp) }
                }
                // Rating badge overlay
                Surface(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.75f)) {
                    val r = if (producto.rating > 0) String.format("%.1f", producto.rating) else "-"
                    Text("⭐ $r", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(producto.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(producto.nombreVendedor, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    if (producto.ubicacionVendedor.isNotEmpty()) {
                        Text(" · ", color = Color.Gray, fontSize = 11.sp)
                        Text("📍${producto.ubicacionVendedor}", color = OrangeWarn.copy(alpha = 0.8f), fontSize = 10.sp,
                            maxLines = 1, modifier = Modifier.basicMarquee())
                    }
                }
                if (producto.mostrarCantidad && producto.cantidadDisponible >= 0) {
                    Text(
                        if (producto.cantidadDisponible == 0) "Agotado" else "${producto.cantidadDisponible} disp.",
                        color = if (producto.cantidadDisponible == 0) RedCancel else OrangeWarn,
                        fontSize = 10.sp
                    )
                }
                // Precio + tipo de entrega en la misma fila
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("\$${String.format("%.0f", producto.precio)}", color = GreenBtn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (producto.preferenciaEntregaVendedor.isNotEmpty()) {
                        Text(textoEntregaCorto(producto.preferenciaEntregaVendedor), color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
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
