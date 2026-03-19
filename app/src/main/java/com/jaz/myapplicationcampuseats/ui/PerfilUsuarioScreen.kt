package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.Resena
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ResenaRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun PerfilUsuarioScreen(
    uid: String,
    onVolver: () -> Unit,
    onVerTienda: ((vendedorId: String) -> Unit)? = null
) {
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var resenasVendedor by remember { mutableStateOf<List<Resena>>(emptyList()) }
    var resenasComprador by remember { mutableStateOf<List<Resena>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var ordenResenas by remember { mutableStateOf("recientes") } // recientes, calificacion, relevancia
    var tabSeleccionado by remember { mutableStateOf(0) } // 0=vendedor, 1=comprador
    var menuOrdenAbierto by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        UsuarioRepository.obtenerUsuario(uid, onSuccess = {
            usuario = it
            cargando = false
        })
        ResenaRepository.obtenerResenasDeUsuario(uid, "vendedor") { resenasVendedor = it }
        ResenaRepository.obtenerResenasDeUsuario(uid, "comprador") { resenasComprador = it }
    }

    val resenasActuales = if (tabSeleccionado == 0) resenasVendedor else resenasComprador

    val resenasSorted = remember(resenasActuales, ordenResenas) {
        when (ordenResenas) {
            "calificacion" -> resenasActuales.sortedByDescending { it.estrellas }
            "relevancia" -> resenasActuales.sortedByDescending { it.comentario.length }
            else -> resenasActuales.sortedByDescending { it.fecha } // recientes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Perfil",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (onVerTienda != null) {
                IconButton(onClick = { onVerTienda(uid) }) {
                    Icon(Icons.Default.Store, null, tint = GreenBtn)
                }
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenBtn)
            }
            return
        }

        val u = usuario ?: return

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {

            // Tarjeta de perfil
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(DarkSurface2),
                            contentAlignment = Alignment.Center
                        ) {
                            if (u.fotoPerfil.isNotEmpty()) {
                                AsyncImage(
                                    model = u.fotoPerfil,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(u.nombre, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (u.ubicacionDescripcion.isNotEmpty()) {
                            Text("📍 ${u.ubicacionDescripcion}", color = Color.Gray, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatPerfil(
                                valor = String.format("%.1f", u.ratingVendedor),
                                label = "Rating vendedor",
                                icono = "⭐"
                            )
                            StatPerfil(
                                valor = "${u.pedidosComoVendedor}",
                                label = "Pedidos atendidos",
                                icono = "🍽️"
                            )
                            StatPerfil(
                                valor = String.format("%.1f", u.ratingComprador),
                                label = "Rating cliente",
                                icono = "👤"
                            )
                        }
                    }
                }
            }

            // Tabs Vendedor / Comprador
            item {
                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = DarkSurface,
                    contentColor = GreenBtn,
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = tabSeleccionado == 0,
                        onClick = { tabSeleccionado = 0 },
                        text = { Text("Como vendedor (${resenasVendedor.size})") }
                    )
                    Tab(
                        selected = tabSeleccionado == 1,
                        onClick = { tabSeleccionado = 1 },
                        text = { Text("Como cliente (${resenasComprador.size})") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Ordenar por
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${resenasSorted.size} reseña${if (resenasSorted.size != 1) "s" else ""}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Box {
                        TextButton(onClick = { menuOrdenAbierto = true }) {
                            Text(
                                "Ordenar: ${when (ordenResenas) {
                                    "calificacion" -> "Calificación"
                                    "relevancia" -> "Relevancia"
                                    else -> "Recientes"
                                }}",
                                color = GreenBtn,
                                fontSize = 13.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = GreenBtn)
                        }
                        DropdownMenu(
                            expanded = menuOrdenAbierto,
                            onDismissRequest = { menuOrdenAbierto = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            listOf(
                                "recientes" to "Más recientes",
                                "calificacion" to "Calificación",
                                "relevancia" to "Relevancia"
                            ).forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color.White) },
                                    onClick = { ordenResenas = key; menuOrdenAbierto = false },
                                    leadingIcon = {
                                        if (ordenResenas == key) {
                                            Icon(Icons.Default.Check, null, tint = GreenBtn, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Reseñas
            if (resenasSorted.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sin reseñas aún", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(resenasSorted) { resena ->
                    ResenaCard(resena = resena)
                }
            }
        }
    }
}

@Composable
fun StatPerfil(valor: String, label: String, icono: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icono, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(valor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun ResenaCard(resena: Resena) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    resena.autorNombre,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                // Estrellas
                Row {
                    repeat(5) { i ->
                        Text(
                            if (i < resena.estrellas) "⭐" else "☆",
                            fontSize = 13.sp
                        )
                    }
                }
            }
            if (resena.comentario.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(resena.comentario, color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatearFecha(resena.fecha),
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

