package com.jaz.myapplicationcampuseats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ImageRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun AjustesScreen(
    usuario: Usuario?,
    onVolver: () -> Unit,
    onUsuarioActualizado: (Usuario) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var preferenciaEntrega by remember { mutableStateOf(usuario?.preferenciaEntrega ?: "cliente_recoge") }
    var ubicacionDescripcion by remember { mutableStateOf(usuario?.ubicacionDescripcion ?: "") }
    var guardando by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    val fotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val uid = usuario?.uid ?: return@let
            ImageRepository.subirFotoPerfil(
                uri = it,
                userId = uid,
                onSuccess = { url ->
                    UsuarioRepository.actualizarPerfil(uid, mapOf("fotoPerfil" to url)) {
                        val actualizado = usuario?.copy(fotoPerfil = url)
                        actualizado?.let { u -> onUsuarioActualizado(u) }
                    }
                },
                onError = {}
            )
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            containerColor = DarkSurface,
            title = { Text("Cerrar sesión", color = Color.White) },
            text = { Text("¿Seguro que quieres cerrar sesión?", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel)
                ) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
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
            Text("Ajustes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Foto de perfil
        SectionCard {
            Text("Foto de perfil", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(DarkSurface2),
                    contentAlignment = Alignment.Center
                ) {
                    if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                        AsyncImage(
                            model = usuario.fotoPerfil,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { fotoLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cambiar foto", color = GreenBtn) }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preferencia de entrega
        SectionCard {
            Text("🚗 Preferencia de entrega", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Cuando alguien haga un pedido de tus platillos, ¿cómo prefieren coordinar la entrega?",
                color = Color.Gray, fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("cliente_recoge", "El cliente me recoge", "El cliente va a tu ubicación a recoger su pedido"),
                Triple("vendedor_lleva", "Yo llevo el pedido", "Tú llevas el pedido a la ubicación del cliente"),
                Triple("ambos", "Ambas opciones", "Coordinan entre ambos según convenga")
            ).forEach { (valor, titulo, descripcion) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = preferenciaEntrega == valor,
                        onClick = { preferenciaEntrega = valor },
                        colors = RadioButtonDefaults.colors(selectedColor = GreenBtn)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(titulo, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(descripcion, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción de ubicación
            Text("Descripción de tu ubicación (opcional)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ej: Cafetería principal, planta baja", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = ubicacionDescripcion,
                onValueChange = { ubicacionDescripcion = it },
                placeholder = { Text("Describe dónde encuentras...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Guardar cambios
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (guardado) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambios guardados", color = GreenBtn, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        val uid = usuario?.uid ?: return@Button
                        guardando = true
                        val campos = mapOf(
                            "preferenciaEntrega" to preferenciaEntrega,
                            "ubicacionDescripcion" to ubicacionDescripcion
                        )
                        UsuarioRepository.actualizarPerfil(uid, campos,
                            onSuccess = {
                                guardando = false
                                guardado = true
                                val actualizado = usuario?.copy(
                                    preferenciaEntrega = preferenciaEntrega,
                                    ubicacionDescripcion = ubicacionDescripcion
                                )
                                actualizado?.let { onUsuarioActualizado(it) }
                            },
                            onError = { guardando = false }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                    enabled = !guardando
                ) {
                    if (guardando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Guardar cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cerrar sesión
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { mostrarDialogoCerrarSesion = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel)
                ) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
