package com.jaz.myapplicationcampuseats

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.ImageRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

@Composable
fun PublicarScreen(onVolver: () -> Unit) {

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugarEntrega by remember { mutableStateOf("") }

    var categoriaSeleccionada by remember { mutableStateOf("Hamburguesas") }

    var publicado by remember { mutableStateOf(false) }

    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    if (publicado) {

        Box(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            contentAlignment = Alignment.Center
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text("✅", fontSize = 60.sp)

                Spacer(Modifier.height(16.dp))

                Text(
                    "¡Publicado con éxito!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Tu comida ya está visible para los compradores",
                    color = Color.Gray
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                ) {
                    Text("Volver al inicio", color = Color.White)
                }

            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, "volver", tint = Color.White)
            }

            Text(
                "Publicar comida",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(Modifier.padding(16.dp)) {

            Text("Nombre del platillo:", color = Color.White)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Precio:", color = Color.White)

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Descripción:", color = Color.White)

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("image/*") }
            ) {
                Text("Seleccionar imagen")
            }

            imagenUri?.let {

                Spacer(Modifier.height(12.dp))

                AsyncImage(
                    model = it,
                    contentDescription = "Imagen",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(

                onClick = {

                    val userId =
                        FirebaseAuth.getInstance().currentUser?.uid ?: return@Button

                    val productoId =
                        FirebaseFirestore.getInstance()
                            .collection("productos")
                            .document()
                            .id

                    imagenUri?.let { uri ->

                        ImageRepository.subirImagenProducto(
                            uri,
                            productoId,

                            onSuccess = { imageUrl ->

                                val producto = Producto(

                                    id = productoId,

                                    nombre = nombre,

                                    descripcion = descripcion,

                                    precio = precio.toDoubleOrNull() ?: 0.0,

                                    imagenUrl = imageUrl,

                                    categoria = categoriaSeleccionada,

                                    vendedorId = userId
                                )

                                ProductoRepository.publicarProducto(
                                    producto,
                                    onSuccess = {

                                        publicado = true

                                    },
                                    onError = {

                                    }
                                )
                            },

                            onError = {

                            }
                        )

                    }

                },

                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    "Publicar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}