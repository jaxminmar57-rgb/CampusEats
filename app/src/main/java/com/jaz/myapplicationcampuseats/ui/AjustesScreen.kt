package com.jaz.myapplicationcampuseats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
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
    val uid = usuario?.uid ?: ""

    // Perfil
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var sexo by remember { mutableStateOf(usuario?.sexo ?: "") }
    var menuSexoAbierto by remember { mutableStateOf(false) }

    // Vendedor
    var preferenciaEntrega by remember { mutableStateOf(usuario?.preferenciaEntrega ?: "cliente_recoge") }
    var horarioInicio by remember { mutableStateOf(usuario?.horarioInicio ?: "09:00") }
    var horarioFin by remember { mutableStateOf(usuario?.horarioFin ?: "18:00") }
    val todosLosDias = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")
    var diasTrabajo by remember { mutableStateOf(usuario?.diasTrabajo ?: listOf("Lun","Mar","Mié","Jue","Vie")) }
    var ubicacionDescripcion by remember { mutableStateOf(usuario?.ubicacionDescripcion ?: "") }

    var guardando by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }
    var subiendoFoto by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    val opcionesSexo = listOf("Masculino", "Femenino", "Prefiero no decir")

    val fotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (uid.isEmpty()) return@let
            subiendoFoto = true
            ImageRepository.subirFotoPerfil(
                uri = it,
                userId = uid,
                onSuccess = { url ->
                    subiendoFoto = false
                    UsuarioRepository.actualizarPerfil(uid, mapOf("fotoPerfil" to url)) {
                        val actualizado = usuario?.copy(fotoPerfil = url)
                        actualizado?.let { u -> onUsuarioActualizado(u) }
                    }
                },
                onError = { subiendoFoto = false }
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

        // ── Foto de perfil ──
        AjustesSectionCard {
            Text("Foto de perfil", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(DarkSurface2),
                    contentAlignment = Alignment.Center
                ) {
                    if (usuario?.fotoPerfil?.isNotEmpty() == true) {
                        AsyncImage(
                            model = usuario.fotoPerfil,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    if (subiendoFoto) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenBtn, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { fotoLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp),
                    enabled = !subiendoFoto
                ) {
                    Text(if (subiendoFoto) "Subiendo..." else "Cambiar foto", color = GreenBtn)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Información personal ──
        AjustesSectionCard {
            Text("👤 Información personal", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Correo y edad no se pueden cambiar", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))

            // Info no editable
            InfoFilaAjuste("Correo", usuario?.correo ?: "—")
            InfoFilaAjuste("Edad", if ((usuario?.edad ?: "").isNotEmpty()) "${usuario?.edad} años" else "—")

            Spacer(modifier = Modifier.height(14.dp))

            // Teléfono (editable)
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    if (it.length <= 15 && it.all { c -> c.isDigit() || c == '+' }) {
                        telefono = it
                        guardado = false
                    }
                },
                label = { Text("Teléfono (opcional)", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sexo (editable, dropdown)
            Box {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    label = { Text("Sexo", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.People, null, tint = Color.Gray) },
                    trailingIcon = {
                        IconButton(onClick = { menuSexoAbierto = true }) {
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = camposColores(),
                    readOnly = true,
                    singleLine = true,
                    placeholder = { Text("Seleccionar...", color = Color.Gray) }
                )
                // Área clickeable encima del campo para abrir el menú
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { menuSexoAbierto = true }
                )
                DropdownMenu(
                    expanded = menuSexoAbierto,
                    onDismissRequest = { menuSexoAbierto = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    opcionesSexo.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = Color.White) },
                            onClick = { sexo = opcion; menuSexoAbierto = false; guardado = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Preferencia de entrega (vendedor) ──
        AjustesSectionCard {
            Text("🚗 Preferencia de entrega", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "¿Cómo coordinas la entrega cuando recibes un pedido?",
                color = Color.Gray, fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("cliente_recoge", "El cliente me recoge", "El cliente va a tu ubicación"),
                Triple("vendedor_lleva", "Yo llevo el pedido", "Tú vas donde el cliente"),
                Triple("ambos", "Ambas opciones", "Se coordina en el chat")
            ).forEach { (valor, titulo, descripcion) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = preferenciaEntrega == valor,
                        onClick = { preferenciaEntrega = valor; guardado = false },
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

            OutlinedTextField(
                value = ubicacionDescripcion,
                onValueChange = { ubicacionDescripcion = it; guardado = false },
                label = { Text("Descripción de tu ubicación (opcional)", color = Color.Gray) },
                placeholder = { Text("Ej: Cafetería principal, planta baja", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Horario de trabajo ──
        AjustesSectionCard {
            Text("🕐 Horario de trabajo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Los clientes verán cuándo sueles vender", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Desde", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = horarioInicio, onValueChange = { horarioInicio = it; guardado = false },
                        placeholder = { Text("09:00", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = camposColores(), singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hasta", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = horarioFin, onValueChange = { horarioFin = it; guardado = false },
                        placeholder = { Text("18:00", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = camposColores(), singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Días que trabajas", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                todosLosDias.forEach { dia ->
                    val seleccionado = dia in diasTrabajo
                    FilterChip(
                        selected = seleccionado,
                        onClick = {
                            diasTrabajo = if (seleccionado) diasTrabajo - dia else diasTrabajo + dia
                            guardado = false
                        },
                        label = { Text(dia, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenBtn,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Guardar cambios ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (guardado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenBtn, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambios guardados", color = GreenBtn, fontSize = 14.sp)
                    }
                }
                Button(
                    onClick = {
                        if (uid.isEmpty()) return@Button
                        guardando = true
                        val campos = mapOf(
                            "telefono" to telefono.trim(),
                            "sexo" to sexo,
                            "preferenciaEntrega" to preferenciaEntrega,
                            "ubicacionDescripcion" to ubicacionDescripcion.trim(),
                            "horarioInicio" to horarioInicio.trim(),
                            "horarioFin" to horarioFin.trim(),
                            "diasTrabajo" to diasTrabajo
                        )
                        UsuarioRepository.actualizarPerfil(uid, campos,
                            onSuccess = {
                                guardando = false
                                guardado = true
                                val actualizado = usuario?.copy(
                                    telefono = telefono.trim(),
                                    sexo = sexo,
                                    preferenciaEntrega = preferenciaEntrega,
                                    ubicacionDescripcion = ubicacionDescripcion.trim(),
                                    horarioInicio = horarioInicio.trim(),
                                    horarioFin = horarioFin.trim(),
                                    diasTrabajo = diasTrabajo
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
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Cerrar sesión ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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

                Spacer(modifier = Modifier.height(12.dp))

                // ── Eliminar cuenta ──
                var mostrarDialogoEliminar by remember { mutableStateOf(false) }
                var eliminando by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { mostrarDialogoEliminar = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel)
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar mi cuenta", fontWeight = FontWeight.Bold)
                }

                if (mostrarDialogoEliminar) {
                    AlertDialog(
                        onDismissRequest = { if (!eliminando) mostrarDialogoEliminar = false },
                        containerColor = DarkSurface,
                        title = { Text("⚠️ Eliminar cuenta", color = RedCancel) },
                        text = {
                            Text("Esta acción es permanente. Se eliminarán todos tus datos, pedidos, publicaciones y reseñas. ¿Estás seguro?",
                                color = Color.Gray, fontSize = 14.sp)
                        },
                        confirmButton = {
                            Button(onClick = {
                                eliminando = true
                                val uid = usuario?.uid ?: return@Button
                                // Eliminar datos de Firestore
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("usuarios").document(uid).delete()
                                // Eliminar cuenta de Auth
                                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
                                    ?.addOnSuccessListener {
                                        eliminando = false
                                        mostrarDialogoEliminar = false
                                        onCerrarSesion()
                                    }
                                    ?.addOnFailureListener {
                                        eliminando = false
                                        mostrarDialogoEliminar = false
                                        // Si falla (sesión vieja), cerrar sesión de todos modos
                                        onCerrarSesion()
                                    }
                            }, colors = ButtonDefaults.buttonColors(containerColor = RedCancel),
                                enabled = !eliminando) {
                                if (eliminando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                else Text("Eliminar permanentemente")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoEliminar = false }, enabled = !eliminando) {
                                Text("Cancelar", color = Color.Gray)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AjustesSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun InfoFilaAjuste(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(valor, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
}
