package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val productosPorCategoria = mapOf(
    "Hamburguesas" to listOf(
        Producto("Hamburguesa Clásica", 45, 4.8, "🍔"),
        Producto("Hamburguesa BBQ", 55, 4.7, "🍔"),
        Producto("Hamburguesa Doble", 65, 4.9, "🍔")
    ),
    "Pizza" to listOf(
        Producto("Pizza Pepperoni", 50, 4.7, "🍕"),
        Producto("Pizza Hawaiana", 48, 4.5, "🍕"),
        Producto("Pizza 4 Quesos", 55, 4.8, "🍕")
    ),
    "Pastas" to listOf(
        Producto("Pasta Alfredo", 35, 4.6, "🍝"),
        Producto("Pasta Bolognesa", 38, 4.7, "🍝"),
        Producto("Pasta Carbonara", 40, 4.8, "🍝")
    ),
    "Bebidas" to listOf(
        Producto("Refresco", 15, 4.2, "🥤"),
        Producto("Agua Natural", 10, 4.0, "💧"),
        Producto("Jugo Natural", 25, 4.5, "🧃")
    ),
    "Ensaladas" to listOf(
        Producto("Ensalada César", 25, 4.3, "🥗"),
        Producto("Ensalada Mixta", 22, 4.2, "🥗"),
        Producto("Ensalada Griega", 28, 4.4, "🥗")
    ),
    "Burritos" to listOf(
        Producto("Burrito Pollo", 30, 4.9, "🌯"),
        Producto("Burrito Carne", 35, 4.8, "🌯"),
        Producto("Burrito Veggie", 28, 4.6, "🌯")
    ),
    "Sandwich" to listOf(
        Producto("Sandwich Club", 30, 4.5, "🥪"),
        Producto("Sandwich Pollo", 28, 4.4, "🥪"),
        Producto("Sandwich Vegetal", 25, 4.3, "🥪")
    ),
    "Otros" to listOf(
        Producto("Sushi", 80, 4.5, "🍱"),
        Producto("Tacos", 25, 4.7, "🌮"),
        Producto("Hot Dog", 20, 4.3, "🌭")
    )
)

@Composable
fun CategoriaScreen(categoria: String, onVolver: () -> Unit) {
    val productos = productosPorCategoria[categoria] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(text = categoria, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (productos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🍽️", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No hay productos disponibles", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Vuelve más tarde", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { producto ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3D3D54)),
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
            }
        }
    }
}