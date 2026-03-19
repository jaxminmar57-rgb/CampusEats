package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.R
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onEntrar: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                text = "Crear cuenta",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; error = "" },
            label = { Text("Nombre de usuario", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = "" },
            label = { Text("Correo electrónico", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = edad,
            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) { edad = it; error = "" } },
            label = { Text("Edad", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" },
            label = { Text("Contraseña", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null, tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = camposColores(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it; error = "" },
            label = { Text("Confirmar contraseña", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                when {
                    nombre.isBlank() || correo.isBlank() || edad.isBlank() || password.isBlank() ->
                        error = "Completa todos los campos"
                    password.length < 6 ->
                        error = "La contraseña debe tener al menos 6 caracteres"
                    password != confirmar ->
                        error = "Las contraseñas no coinciden"
                    edad.toIntOrNull() == null || edad.toInt() < 14 ->
                        error = "Debes tener al menos 14 años"
                    else -> {
                        cargando = true
                        UsuarioRepository.registrar(
                            correo = correo.trim(),
                            password = password,
                            nombre = nombre.trim(),
                            edad = edad,
                            onSuccess = { usuario ->
                                cargando = false
                                onEntrar(usuario)
                            },
                            onError = { msg ->
                                cargando = false
                                error = msg
                            }
                        )
                    }
                }
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
                Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
