package com.jaz.myapplicationcampuseats.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

val categorias = listOf(
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
    onMisPublicaciones: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var menuAbierto by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Listener en tiempo real: se actualiza automáticamente cuando
    // cualquier vendedor publique o modifique un producto
    DisposableEffect(Unit) {
        val listener = ProductoRepository.escucharProductosDisponibles { lista ->
            productos = lista
            cargando = false
        }
        onDispose { listener.remove() }
    }

    val productosFiltrados = if (busqueda.isEmpty()) productos
    else productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
        it.categoria.contains(busqueda, ignoreCase = true) ||
        it.nombreVendedor.contains(busqueda, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // --- Barra superior ---
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
                    IconButton(onClick = onNotificaciones) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = onCarrito) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // --- Barra de búsqueda ---
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

            if (busqueda.isEmpty()) {
                // --- Carrusel banner ---
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf("🌯 Burritos del día", "🍕 Pizza especial", "🍔 Combo hamburguesa")) { texto ->
                        Box(
                            modifier = Modifier
                                .size(width = 220.dp, height = 110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = texto, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Categorías ---
                Text(
                    text = "Categorías",
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
                    items(categorias) { (nombre, imagen) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onCategoria(nombre) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface),
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
                            Text(text = nombre, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Título sección productos ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (busqueda.isEmpty()) "Disponibles ahora"
                           else "Resultados para \"$busqueda\"",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                            text = if (busqueda.isEmpty()) "No hay platillos disponibles"
                                   else "Sin resultados para \"$busqueda\"",
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        if (busqueda.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("¡Sé el primero en publicar algo! 🍽️", color = Color.Gray, fontSize = 14.sp)
                        }
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
                                        onClick = { onCategoria(producto.categoria) }
                                    )
                                }
                                if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // --- FAB publicar ---
        FloatingActionButton(
            onClick = onPublicar,
            containerColor = GreenBtn,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(62.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Publicar", modifier = Modifier.size(30.dp))
        }

        // --- Menú lateral ---
        if (menuAbierto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { menuAbierto = false }
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(270.dp)
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF1A3320))
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Avatar + nombre
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                            AsyncImage(model = usuario.fotoPerfil, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(usuario?.nombre ?: "", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(usuario?.correo ?: "", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("CLIENTE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MenuOpcion(Icons.Default.Receipt, "Mis pedidos") { menuAbierto = false; onHistorial() }
                MenuOpcion(Icons.Default.ShoppingCart, "Carrito") { menuAbierto = false; onCarrito() }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("VENDEDOR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MenuOpcion(Icons.Default.Add, "Publicar platillo") { menuAbierto = false; onPublicar() }
                MenuOpcion(Icons.Default.List, "Pedidos recibidos") { menuAbierto = false; onPedidosVendedor() }
                MenuOpcion(Icons.Default.RestaurantMenu, "Mis publicaciones") { menuAbierto = false; onMisPublicaciones() }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                MenuOpcion(Icons.Default.Person, "Mi cuenta") { menuAbierto = false; onCuenta() }
                MenuOpcion(Icons.Default.Settings, "Ajustes") { menuAbierto = false; onAjustes() }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                MenuOpcion(Icons.Default.ExitToApp, "Cerrar sesión", tint = RedCancel) {
                    menuAbierto = false
                    onCerrarSesion()
                }
            }
        }
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
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp)
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = texto, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = texto, color = tint, fontSize = 15.sp)
    }
}
