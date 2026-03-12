package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CuentaScreen(
    usuario: String,
    correo: String,
    edad: String,
    onVolver: () -> Unit
) {
    val pedidosComprador = 12
    val pedidosVendedor = 8
    val calificacionComprador = 4.8
    val calificacionVendedor = 4.5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
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
            Text(text = "Mi Cuenta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Avatar y nombre
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF2D2D44), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = usuario, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = correo, color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info básica
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Información", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                InfoFila(label = "Usuario", valor = usuario)
                InfoFila(label = "Correo", valor = correo)
                InfoFila(label = "Edad", valor = "$edad años")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rol Comprador
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🛒 Como Comprador", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                InfoFila(label = "Pedidos realizados", valor = "$pedidosComprador pedidos")
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Calificación: ", color = Color.Gray, fontSize = 14.sp)
                    repeat(5) { index ->
                        Text(
                            text = if (index < calificacionComprador.toInt()) "⭐" else "☆",
                            fontSize = 16.sp
                        )
                    }
                    Text(text = " $calificacionComprador", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Reseñas recientes:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ResenaItem(texto = "\"Muy buen cliente, pago rápido\" ⭐⭐⭐⭐⭐")
                ResenaItem(texto = "\"Pedido sin problemas\" ⭐⭐⭐⭐")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rol Vendedor
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🍽️ Como Vendedor", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                InfoFila(label = "Pedidos atendidos", valor = "$pedidosVendedor pedidos")
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Calificación: ", color = Color.Gray, fontSize = 14.sp)
                    repeat(5) { index ->
                        Text(
                            text = if (index < calificacionVendedor.toInt()) "⭐" else "☆",
                            fontSize = 16.sp
                        )
                    }
                    Text(text = " $calificacionVendedor", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Reseñas recientes:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ResenaItem(texto = "\"Excelente comida, muy rica\" ⭐⭐⭐⭐⭐")
                ResenaItem(texto = "\"Llegó rápido y caliente\" ⭐⭐⭐⭐⭐")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InfoFila(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = valor, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun ResenaItem(texto: String) {
    Text(
        text = texto,
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF3D3D54), RoundedCornerShape(8.dp))
            .padding(8.dp)
    )
}