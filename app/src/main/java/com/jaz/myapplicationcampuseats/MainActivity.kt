package com.jaz.myapplicationcampuseats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val GreenDark = Color(0xFF1B3A2D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("campuseats", android.content.Context.MODE_PRIVATE)

    var pantalla by remember {
        val usuarioGuardado = prefs.getString("usuario", "")
        mutableStateOf(if (usuarioGuardado.isNullOrEmpty()) "splash" else "home")
    }
    var nombreUsuario by remember { mutableStateOf(prefs.getString("usuario", "") ?: "") }
    var correoUsuario by remember { mutableStateOf(prefs.getString("correo", "") ?: "") }
    var edadUsuario by remember { mutableStateOf(prefs.getString("edad", "") ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var nombreVendedorChat by remember { mutableStateOf("Vendedor") }
    var carrito by remember { mutableStateOf(listOf<ItemCarrito>()) }

    LaunchedEffect(Unit) {
        if (pantalla == "splash") {
            delay(3000)
            pantalla = "login"
        }
    }

    when (pantalla) {
        "splash" -> SplashScreen()
        "login" -> LoginScreen(
            onRegistrarme = { pantalla = "registro" },
            onEntrar = { pantalla = "home" }
        )
        "registro" -> RegistroScreen(
            onVolver = { pantalla = "login" },
            onEntrar = { nombre, correo, edad ->
                nombreUsuario = nombre
                correoUsuario = correo
                edadUsuario = edad
                prefs.edit()
                    .putString("usuario", nombre)
                    .putString("correo", correo)
                    .putString("edad", edad)
                    .apply()
                pantalla = "home"
            }
        )
        "home" -> HomeScreen(
            nombre = nombreUsuario,
            onCarrito = { pantalla = "carrito" },
            onCerrarSesion = {
                prefs.edit().clear().apply()
                nombreUsuario = ""
                correoUsuario = ""
                edadUsuario = ""
                pantalla = "login"
            },
            onCuenta = { pantalla = "cuenta" },
            onBilletera = { pantalla = "historial" },
            onCategoria = { cat ->
                categoriaSeleccionada = cat
                pantalla = "categoria"
            },
            onPublicar = { pantalla = "publicar" },
            onNotificaciones = { pantalla = "notificaciones" }
        )
        "carrito" -> CarritoScreen(
            items = carrito,
            onVolver = { pantalla = "home" },
            onPedir = { }
        )
        "cuenta" -> CuentaScreen(
            usuario = nombreUsuario,
            correo = correoUsuario,
            edad = edadUsuario,
            onVolver = { pantalla = "home" }
        )
        "historial" -> HistorialScreen(
            onVolver = { pantalla = "home" },
            onChat = { vendedor ->
                nombreVendedorChat = vendedor
                pantalla = "chat"
            }
        )
        "categoria" -> CategoriaScreen(
            categoria = categoriaSeleccionada,
            onVolver = { pantalla = "home" }
        )
        "publicar" -> PublicarScreen(onVolver = { pantalla = "home" })
        "chat" -> ChatScreen(
            nombreVendedor = nombreVendedorChat,
            nombreComprador = nombreUsuario,
            onVolver = { pantalla = "historial" }
        )
        "notificaciones" -> NotificacionesScreen(onVolver = { pantalla = "home" })
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(GreenDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo CampusEats",
                modifier = Modifier.size(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "CampusEats", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoginScreen(onRegistrarme: () -> Unit, onEntrar: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "CampusEats", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Inicio de sesión", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Correo:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = correo, onValueChange = { correo = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contraseña:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = contrasena, onValueChange = { contrasena = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onEntrar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Entrar", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRegistrarme) {
                Text(text = "Registrarme", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RegistroScreen(onVolver: () -> Unit, onEntrar: (String, String, String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Registro", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Usuario:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = usuario, onValueChange = { usuario = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Correo:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = correo, onValueChange = { correo = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contraseña:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = contrasena, onValueChange = { contrasena = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Edad:", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            TextField(
                value = edad, onValueChange = { edad = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            )
            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMsg, color = Color.Red, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    when {
                        usuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || edad.isEmpty() -> {
                            errorMsg = "Por favor llena todos los campos"
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                            errorMsg = "Ingresa un correo válido"
                        }
                        contrasena.length < 6 -> {
                            errorMsg = "La contraseña debe tener al menos 6 caracteres"
                        }
                        else -> {
                            errorMsg = ""
                            onEntrar(usuario, correo, edad)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Registrarme", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onVolver) {
                Text(text = "¿Ya tienes cuenta? Inicia sesión", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}