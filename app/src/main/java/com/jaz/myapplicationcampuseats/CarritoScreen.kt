package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ItemCarrito(val nombre: String, val precio: Int, val emoji: String, var cantidad: Int = 1)

@Composable
fun CarritoScreen(
    items: List<ItemCarrito>,
    onVolver: () -> Unit,
    onPedir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
            }
            Text(
                text = "Carrito",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (items.isEmpty()) {
            // Carrito vacío
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🥤", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "El carrito está vacío",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Abre la página de una tienda y luego agrega a tu carrito los productos que deseas pedir.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(text = "Volver a Inicio", color = Color.White)
                }
            }
        } else {
            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.emoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Cantidad: ${item.cantidad}", color = Color.Gray, fontSize = 14.sp)
                        }
                        Text(
                            text = "\$${item.precio * item.cantidad}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                    HorizontalDivider(color = Color.LightGray)
                }
            }

            // Total y botón pedir
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "\$${items.sumOf { it.precio * it.cantidad }}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GreenBtn
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPedir,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Pedir ahora", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}