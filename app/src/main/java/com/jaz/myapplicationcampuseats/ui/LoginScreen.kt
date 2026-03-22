package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun LoginScreen(
    onRegistrarme: () -> Unit,
    onEntrar: (Usuario) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val loginCtx = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "CampusEats",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Inicia sesión para continuar",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Correo
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = "" },
            label = { Text("Correo electrónico", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Email, "Correo", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" },
            label = { Text("Contraseña", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, "Contraseña", tint = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null, tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (verPassword) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = RedCancel,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    error = "Completa todos los campos"
                    return@Button
                }
                cargando = true
                UsuarioRepository.login(
                    correo = correo.trim(),
                    password = password,
                    onSuccess = { usuario ->
                        cargando = false
                        com.jaz.myapplicationcampuseats.service.SoundManager.playExito(loginCtx)
                        onEntrar(usuario)
                    },
                    onError = { msg ->
                        cargando = false
                        error = msg
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Olvidé mi contraseña
        var mostrarResetDialog by remember { mutableStateOf(false) }
        var resetMsg by remember { mutableStateOf("") }

        TextButton(onClick = { mostrarResetDialog = true }) {
            Text("¿Olvidaste tu contraseña?", color = Color.Gray, fontSize = 13.sp)
        }

        if (mostrarResetDialog) {
            var resetCorreo by remember { mutableStateOf(correo) }
            AlertDialog(
                onDismissRequest = { mostrarResetDialog = false },
                containerColor = DarkSurface,
                title = { Text("Recuperar contraseña", color = Color.White) },
                text = {
                    Column {
                        Text("Te enviaremos un correo para restablecer tu contraseña.",
                            color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetCorreo, onValueChange = { resetCorreo = it },
                            label = { Text("Correo electrónico", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = camposColores(), singleLine = true
                        )
                        if (resetMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetMsg, color = if (resetMsg.startsWith("✅")) GreenBtn else RedCancel, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (resetCorreo.isBlank()) { resetMsg = "Ingresa tu correo"; return@Button }
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .sendPasswordResetEmail(resetCorreo.trim())
                            .addOnSuccessListener { resetMsg = "✅ Correo enviado — revisa tu bandeja" }
                            .addOnFailureListener { resetMsg = it.message ?: "Error" }
                    }, colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)) { Text("Enviar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarResetDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = Color.Gray, fontSize = 14.sp)
            TextButton(onClick = onRegistrarme) {
                Text("Regístrate", color = GreenBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


