package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.Resena
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ResenaRepository

@Composable
fun CuentaScreen(
    usuario: Usuario?,
    onVolver: () -> Unit,
    onUsuarioActualizado: (Usuario) -> Unit
) {
    var resenasComprador by remember { mutableStateOf<List<Resena>>(emptyList()) }
    var resenasVendedor by remember { mutableStateOf<List<Resena>>(emptyList()) }

    LaunchedEffect(usuario?.uid) {
        val uid = usuario?.uid ?: return@LaunchedEffect
        ResenaRepository.obtenerResenasDeUsuario(uid, "comprador") { resenasComprador = it }
        ResenaRepository.obtenerResenasDeUsuario(uid, "vendedor") { resenasVendedor = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Mi Cuenta", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Avatar + nombre
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                    AsyncImage(
                        model = usuario.fotoPerfil,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(50.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(usuario?.nombre ?: "", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(usuario?.correo ?: "", color = Color.Gray, fontSize = 14.sp)
        }

        // Información básica
        SectionCard {
            Text("Información", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            InfoFila("Usuario", usuario?.nombre ?: "")
            InfoFila("Correo", usuario?.correo ?: "")
            InfoFila("Edad", "${usuario?.edad ?: ""} años")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Como Comprador
        SectionCard {
            Text("🛒 Como Comprador", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            InfoFila("Pedidos realizados", "${usuario?.pedidosComoComprador ?: 0}")
            Spacer(modifier = Modifier.height(8.dp))
            RatingDisplay(rating = usuario?.ratingComprador ?: 0.0, numResenas = usuario?.numResenasComprador ?: 0)
            if (resenasComprador.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Reseñas recientes:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                resenasComprador.take(3).forEach { ResenaItemCard(it) }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Como Vendedor
        SectionCard {
            Text("🍽️ Como Vendedor", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            InfoFila("Pedidos atendidos", "${usuario?.pedidosComoVendedor ?: 0}")
            Spacer(modifier = Modifier.height(8.dp))
            RatingDisplay(rating = usuario?.ratingVendedor ?: 0.0, numResenas = usuario?.numResenasVendedor ?: 0)
            if (resenasVendedor.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Reseñas recientes:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                resenasVendedor.take(3).forEach { ResenaItemCard(it) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun InfoFila(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(valor, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.07f), modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
fun RatingDisplay(rating: Double, numResenas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, null, tint = GoldStar, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            String.format("%.1f", rating),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            " ($numResenas reseña${if (numResenas != 1) "s" else ""})",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ResenaItemCard(resena: Resena) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface2)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(resena.autorNombre, color = GreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row {
                    repeat(resena.estrellas) { Text("⭐", fontSize = 11.sp) }
                }
            }
            if (resena.comentario.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("\"${resena.comentario}\"", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
