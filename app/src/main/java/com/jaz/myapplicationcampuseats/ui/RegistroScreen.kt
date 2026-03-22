package com.jaz.myapplicationcampuseats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.ImageRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onEntrar: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var edad   by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var sexo   by remember { mutableStateOf("") }
    var preferenciaEntrega by remember { mutableStateOf("") }
    var menuSexoAbierto    by remember { mutableStateOf(false) }
    var menuEntregaAbierto by remember { mutableStateOf(false) }
    var password  by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }

    var fotoUri      by remember { mutableStateOf<Uri?>(null) }
    var subiendoFoto by remember { mutableStateOf(false) }
    var cargando     by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf("") }

    val opcionesSexo = listOf("Masculino", "Femenino", "Prefiero no decir")
    val opcionesEntrega = listOf(
        "cliente_recoge" to "🚶 Los clientes recogen su pedido conmigo",
        "vendedor_lleva" to "🛵 Yo llevo el pedido al cliente",
        "ambos"          to "🤝 Ambas opciones (flexible)"
    )

    val fotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> fotoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
            }
            Text("Crear cuenta", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Foto de perfil
        Text("Foto de perfil *", color = Color.White, fontSize = 14.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(DarkSurface)
                .border(2.dp, if (fotoUri != null) GreenBtn else Color.Gray, CircleShape)
                .clickable(onClickLabel = "Seleccionar foto") { fotoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (fotoUri != null) {
                AsyncImage(model = fotoUri, contentDescription = "Imagen",
                    modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, "Añadir foto", tint = Color.Gray, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Agregar", color = Color.Gray, fontSize = 11.sp)
                }
            }
            if (subiendoFoto) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenBtn, modifier = Modifier.size(28.dp))
                }
            }
        }

        if (fotoUri != null) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = { fotoLauncher.launch("image/*") }) {
                Text("Cambiar foto", color = GreenBtn, fontSize = 13.sp)
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Toca el círculo para seleccionar una foto", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nombre
        OutlinedTextField(value = nombre, onValueChange = { nombre = it; error = "" },
            label = { Text("Nombre completo *", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Person, "Perfil", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        // Correo
        OutlinedTextField(value = correo, onValueChange = { correo = it; error = "" },
            label = { Text("Correo electrónico *", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Email, "Correo", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        // Edad
        OutlinedTextField(value = edad,
            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) { edad = it; error = "" } },
            label = { Text("Edad *", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, "Fecha", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        // Sexo
        Box {
            OutlinedTextField(value = sexo, onValueChange = {},
                label = { Text("Sexo *", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.People, "Personas", tint = Color.Gray) },
                trailingIcon = { IconButton(onClick = { menuSexoAbierto = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Desplegar", tint = Color.Gray) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = camposColores(), readOnly = true, singleLine = true,
                placeholder = { Text("Selecciona una opción", color = Color.Gray) })
            DropdownMenu(expanded = menuSexoAbierto, onDismissRequest = { menuSexoAbierto = false },
                modifier = Modifier.background(DarkSurface)) {
                opcionesSexo.forEach { opcion ->
                    DropdownMenuItem(text = { Text(opcion, color = Color.White) },
                        onClick = { sexo = opcion; menuSexoAbierto = false; error = "" })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Preferencia de entrega (OBLIGATORIA) ──
        Text("Preferencia de entrega *", color = Color.White, fontSize = 14.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        Text("¿Cómo manejarás los pedidos cuando seas vendedor?",
            color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))

        Box {
            val etiquetaEntrega = opcionesEntrega.firstOrNull { it.first == preferenciaEntrega }?.second ?: ""
            OutlinedTextField(value = etiquetaEntrega, onValueChange = {},
                label = { Text("Tipo de entrega *", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.LocalShipping, "Entrega", tint = Color.Gray) },
                trailingIcon = { IconButton(onClick = { menuEntregaAbierto = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Desplegar", tint = Color.Gray) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = camposColores(), readOnly = true, singleLine = true,
                placeholder = { Text("Selecciona una opción", color = Color.Gray) })
            DropdownMenu(expanded = menuEntregaAbierto, onDismissRequest = { menuEntregaAbierto = false },
                modifier = Modifier.background(DarkSurface)) {
                opcionesEntrega.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = Color.White) },
                        onClick = { preferenciaEntrega = key; menuEntregaAbierto = false; error = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Teléfono (opcional)
        OutlinedTextField(value = telefono,
            onValueChange = { if (it.length <= 15 && it.all { c -> c.isDigit() || c == '+' }) telefono = it },
            label = { Text("Teléfono (opcional)", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Phone, "Teléfono", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseña
        OutlinedTextField(value = password, onValueChange = { password = it; error = "" },
            label = { Text("Contraseña *", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, "Contraseña", tint = Color.Gray) },
            trailingIcon = { IconButton(onClick = { verPassword = !verPassword }) {
                Icon(if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (verPassword) "Ocultar contraseña" else "Mostrar contraseña", tint = Color.Gray) } },
            visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = confirmar, onValueChange = { confirmar = it; error = "" },
            label = { Text("Confirmar contraseña *", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, "Contraseña", tint = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = camposColores(), singleLine = true)

        Spacer(modifier = Modifier.height(8.dp))

        if (error.isNotEmpty()) {
            Text(error, color = RedCancel, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                when {
                    fotoUri == null ->
                        error = "La foto de perfil es obligatoria"
                    nombre.isBlank() || correo.isBlank() || edad.isBlank() || password.isBlank() ->
                        error = "Completa todos los campos obligatorios (*)"
                    sexo.isBlank() ->
                        error = "Selecciona tu sexo"
                    preferenciaEntrega.isBlank() ->
                        error = "Selecciona tu preferencia de entrega"
                    password.length < 6 ->
                        error = "La contraseña debe tener al menos 6 caracteres"
                    password != confirmar ->
                        error = "Las contraseñas no coinciden"
                    edad.toIntOrNull() == null || edad.toInt() < 14 ->
                        error = "Debes tener al menos 14 años"
                    else -> {
                        cargando = true; subiendoFoto = true; error = ""

                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(correo.trim(), password)
                            .addOnSuccessListener { authResult ->
                                val uid = authResult.user?.uid ?: run {
                                    cargando = false; subiendoFoto = false
                                    error = "Error al crear cuenta"; return@addOnSuccessListener
                                }
                                ImageRepository.subirFotoPerfil(
                                    uri = fotoUri!!, userId = uid,
                                    onSuccess = { fotoUrl ->
                                        subiendoFoto = false
                                        val usuario = Usuario(
                                            uid = uid, nombre = nombre.trim(),
                                            correo = correo.trim(), edad = edad,
                                            fotoPerfil = fotoUrl, telefono = telefono.trim(),
                                            sexo = sexo, preferenciaEntrega = preferenciaEntrega
                                        )
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("usuarios").document(uid).set(usuario)
                                            .addOnSuccessListener { cargando = false; onEntrar(usuario) }
                                            .addOnFailureListener { e ->
                                                cargando = false; error = "Error al guardar perfil: ${e.message}" }
                                    },
                                    onError = { e ->
                                        subiendoFoto = false; cargando = false
                                        error = "Error al subir foto: ${e.message}" }
                                )
                            }
                            .addOnFailureListener { e ->
                                cargando = false; subiendoFoto = false
                                error = e.message ?: "Error al crear cuenta" }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
            enabled = !cargando && !subiendoFoto
        ) {
            when {
                subiendoFoto -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp)); Text("Subiendo foto...")
                }
                cargando -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp)); Text("Creando cuenta...")
                }
                else -> Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = Color.Gray, fontSize = 14.sp)
            TextButton(onClick = onVolver) {
                Text("Iniciar sesión", color = GreenBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
