package com.jaz.myapplicationcampuseats

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

val DarkBg = Color(0xFF0D1F17)
val GreenBtn = Color(0xFF4CAF50)

data class Producto(val nombre: String, val precio: Int, val rating: Double, val emoji: String)

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

val populares = listOf(
    Producto("Burritos", 30, 4.9, "🌯"),
    Producto("Sushi", 80, 4.5, "🍱"),
    Producto("Pizza", 50, 4.7, "🍕"),
    Producto("Ensalada", 25, 4.3, "🥗"),
    Producto("Hamburguesa", 45, 4.8, "🍔"),
    Producto("Pasta", 35, 4.6, "🍝")
)

@Composable
fun HomeScreen(
    nombre: String,
    onCarrito: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCuenta: () -> Unit,
    onBilletera: () -> Unit,
    onCategoria: (String) -> Unit,
    onPublicar: () -> Unit,
    onNotificaciones: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var menuAbierto by remember { mutableStateOf(false) }
    val productosFiltrados = if (busqueda.isEmpty()) populares
    else populares.filter { it.nombre.contains(busqueda, ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Barra superior
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable { menuAbierto = true }
                )
                Text(text = "Hola, $nombre...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNotificaciones) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = onCarrito) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // Búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("¿Qué se te antoja hoy?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenBtn
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (busqueda.isEmpty()) {
                // Carrusel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        Box(
                            modifier = Modifier.size(width = 200.dp, height = 120.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2D2D44)),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "🍔", fontSize = 48.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banner
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2D2D44)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Un deleite de menú", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "→", color = GreenBtn, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categorías
                Text(text = "Categoría", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
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
                                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF2D2D44)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = imagen),
                                    contentDescription = nombre,
                                    modifier = Modifier.size(45.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = nombre, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Populares título
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (busqueda.isEmpty()) "Populares" else "Resultados para \"$busqueda\"",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Text(text = "...", color = Color.Gray, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Sin resultados
            if (productosFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Sin resultados para \"$busqueda\"", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Intenta con otro nombre de comida", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    productosFiltrados.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            fila.forEach { producto ->
                                Card(
                                    modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF3D3D54)),
                                            contentAlignment = Alignment.Center
                                        ) { Text(text = producto.emoji, fontSize = 36.sp) }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = producto.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "⭐", fontSize = 11.sp)
                                            Text(text = " ${producto.rating}", color = Color.Gray, fontSize = 11.sp)
                                        }
                                        Text(text = "\$${producto.precio}", color = GreenBtn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Botón + flotante
        FloatingActionButton(
            onClick = onPublicar,
            containerColor = GreenBtn,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Publicar", modifier = Modifier.size(32.dp))
        }

        // Menú lateral
        if (menuAbierto) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { menuAbierto = false }
            )
            Column(
                modifier = Modifier.fillMaxHeight().width(260.dp).align(Alignment.CenterStart).background(Color(0xFF1A3320)).padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(text = "CampusEats", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Hola, $nombre", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                MenuOpcion(icono = Icons.Default.Person, texto = "Cuenta") {
                    menuAbierto = false
                    onCuenta()
                }
                MenuOpcion(icono = Icons.Default.Star, texto = "Historial") {
                    menuAbierto = false
                    onBilletera()
                }
                MenuOpcion(icono = Icons.Default.Notifications, texto = "Notificaciones") {
                    menuAbierto = false
                    onNotificaciones()
                }
                MenuOpcion(icono = Icons.Default.Settings, texto = "Ajustes") { menuAbierto = false }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                MenuOpcion(icono = Icons.Default.ExitToApp, texto = "Cerrar sesión") {
                    menuAbierto = false
                    onCerrarSesion()
                }
            }
        }
    }
}

@Composable
fun MenuOpcion(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = texto, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = texto, color = Color.White, fontSize = 16.sp)
    }
}