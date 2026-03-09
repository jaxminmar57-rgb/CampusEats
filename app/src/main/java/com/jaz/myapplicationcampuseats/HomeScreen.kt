package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkBg = Color(0xFF1A1A2E)
val GreenBtn = Color(0xFF4CAF50)

data class Producto(val nombre: String, val precio: Int, val rating: Double)

val categorias = listOf("Hamburguesas", "Pizza", "Pastas", "Bebidas", "Ensaladas", "Burritos", "Sandwich", "Otros")

val populares = listOf(
    Producto("Burritos", 30, 4.9),
    Producto("Sushi", 80, 4.5),
    Producto("Pizza", 50, 4.7),
    Producto("Ensalada", 25, 4.3)
)

@Composable
fun HomeScreen(nombre: String, onCarrito: () -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White, modifier = Modifier.size(28.dp))
            Text(text = "Hola, $nombre...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onCarrito) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("¿Qué se te antoja hoy?", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = GreenBtn
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Carrusel de imágenes
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) {
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2D2D44)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🍔", fontSize = 48.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner "Un deleite de menú"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2D2D44))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Un deleite de menú", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "→", color = GreenBtn, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categorías
        Text(
            text = "Categoría",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categorias) { cat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2D2D44)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍽️", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = cat, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Populares
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Populares", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "...", color = Color.Gray, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(populares) { producto ->
                Card(
                    modifier = Modifier.size(width = 140.dp, height = 160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3D3D54)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🍱", fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "\$${producto.precio}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⭐", fontSize = 12.sp)
                            Text(text = "${producto.rating}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(text = producto.nombre, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}