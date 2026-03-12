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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Pedido(
    val id: String,
    val producto: String,
    val precio: Int,
    val estado: String,
    val fecha: String,
    val tipo: String,
    val vendedor: String
)

@Composable
fun HistorialScreen(onVolver: () -> Unit, onChat: (String) -> Unit) {
    var tabSeleccionado by remember { mutableStateOf(0) }

    val pedidosCompras = listOf(
        Pedido("001", "Burrito Pollo", 30, "Entregado", "10/03/2026", "compra", "Carlos V."),
        Pedido("002", "Pizza Pepperoni", 50, "En camino", "11/03/2026", "compra", "María G."),
        Pedido("003", "Hamburguesa Clásica", 45, "Cancelado", "08/03/2026", "compra", "Luis R.")
    )

    val pedidosVentas = listOf(
        Pedido("004", "Sushi Mix", 80, "Entregado", "09/03/2026", "venta", "Ana P."),
        Pedido("005", "Pasta Alfredo", 35, "Pendiente", "11/03/2026", "venta", "Juan M.")
    )

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
            Text(text = "Historial de Pedidos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Tabs comprador / vendedor
        TabRow(
            selectedTabIndex = tabSeleccionado,
            containerColor = Color(0xFF16213E),
            contentColor = GreenBtn
        ) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = { Text("🛒 Compras", color = if (tabSeleccionado == 0) GreenBtn else Color.Gray) }
            )
            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = { Text("🍽️ Ventas", color = if (tabSeleccionado == 1) GreenBtn else Color.Gray) }
            )
        }

        val pedidosMostrar = if (tabSeleccionado == 0) pedidosCompras else pedidosVentas

        if (pedidosMostrar.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = if (tabSeleccionado == 0) "🛒" else "🍽️", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (tabSeleccionado == 0) "No has hecho compras aún" else "No has hecho ventas aún",
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidosMostrar) { pedido ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = pedido.producto, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (tabSeleccionado == 0) "Vendedor: ${pedido.vendedor}" else "Comprador: ${pedido.vendedor}",
                                        color = Color.Gray, fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Fecha: ${pedido.fecha}", color = Color.Gray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "\$${pedido.precio}", color = GreenBtn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (pedido.estado) {
                                                "Entregado" -> Color(0xFF2E7D32)
                                                "En camino" -> Color(0xFF1565C0)
                                                "Pendiente" -> Color(0xFFF57F17)
                                                "Cancelado" -> Color(0xFFC62828)
                                                else -> Color.Gray
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = pedido.estado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (pedido.estado != "Cancelado" && pedido.estado != "Entregado") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onChat(pedido.vendedor) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "💬 Contactar", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}