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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import com.jaz.myapplicationcampuseats.model.ItemCarrito

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

    var carrito by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }

    val db = FirebaseFirestore.getInstance()

    // SOLO SE EJECUTA UNA VEZ
    LaunchedEffect(Unit) {

        db.collection("test")
            .add(hashMapOf("mensaje" to "firebase conectado"))

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

        "publicar" -> PublicarScreen(
            onVolver = { pantalla = "home" }
        )

        "chat" -> ChatScreen(
            nombreVendedor = nombreVendedorChat,
            nombreComprador = nombreUsuario,
            onVolver = { pantalla = "historial" }
        )

        "notificaciones" -> NotificacionesScreen(
            onVolver = { pantalla = "home" }
        )
    }
}

@Composable
fun SplashScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenDark),

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

            Text(
                text = "CampusEats",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}