package com.jaz.myapplicationcampuseats.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.ImageRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

val categoriasLista = listOf(
    "Hamburguesas", "Pizza", "Pastas", "Bebidas",
    "Ensaladas", "Burritos", "Sandwich", "Otros"
)

@Composable
fun PublicarScreen(
    userId: String,
    nombreVendedor: String,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Hamburguesas") }
    var menuCategoriaAbierto by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var publicando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var publicado by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
                Text("¡Publicado con éxito!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Tu platillo ya es visible para los compradores", color = Color.Gray)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                ) { Text("Volver al inicio") }
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Publicar platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Imagen
            Text("Foto del platillo", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (imagenUri != null) {
                AsyncImage(
                    model = imagenUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (imagenUri == null) "Seleccionar imagen" else "Cambiar imagen", color = GreenBtn)
            }

            Spacer(Modifier.height(16.dp))

            // Nombre
            Text("Nombre del platillo *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; error = "" },
                placeholder = { Text("Ej: Hamburguesa doble con papas", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // Categoría
            Text("Categoría *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Box {
                OutlinedButton(
                    onClick = { menuCategoriaAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(categoriaSeleccionada, color = Color.White)
                }
                DropdownMenu(
                    expanded = menuCategoriaAbierto,
                    onDismissRequest = { menuCategoriaAbierto = false }
                ) {
                    categoriasLista.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { categoriaSeleccionada = cat; menuCategoriaAbierto = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Precio
            Text("Precio *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = precio,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) { precio = it; error = "" } },
                placeholder = { Text("Ej: 45.00", color = Color.Gray) },
                prefix = { Text("\$", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // Descripción
            Text("Descripción *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it; error = "" },
                placeholder = { Text("Describe tu platillo brevemente...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                maxLines = 4
            )

            Spacer(Modifier.height(14.dp))

            // Ingredientes
            Text("Ingredientes / Contenido", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Ayuda a los clientes con alergias o preferencias", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = ingredientes,
                onValueChange = { ingredientes = it },
                placeholder = { Text("Ej: pollo, lechuga, tomate, mayo, pan integral...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(10.dp),
                colors = camposColores(),
                maxLines = 4
            )

            Spacer(Modifier.height(24.dp))

            if (error.isNotEmpty()) {
                Text(error, color = RedCancel, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> error = "El nombre es obligatorio"
                        precio.isBlank() || precio.toDoubleOrNull() == null -> error = "Ingresa un precio válido"
                        descripcion.isBlank() -> error = "Agrega una descripción"
                        else -> {
                            publicando = true
                            val productoId = FirebaseFirestore.getInstance().collection("productos").document().id

                            fun guardar(imageUrl: String) {
                                val producto = Producto(
                                    id = productoId,
                                    nombre = nombre.trim(),
                                    descripcion = descripcion.trim(),
                                    ingredientes = ingredientes.trim(),
                                    precio = precio.toDouble(),
                                    imagenUrl = imageUrl,
                                    categoria = categoriaSeleccionada,
                                    vendedorId = userId,
                                    nombreVendedor = nombreVendedor,
                                    disponible = true
                                )
                                ProductoRepository.publicarProducto(
                                    producto,
                                    onSuccess = { publicando = false; publicado = true },
                                    onError = { publicando = false; error = "Error al publicar: ${it.message}" }
                                )
                            }

                            if (imagenUri != null) {
                                ImageRepository.subirImagenProducto(
                                    uri = imagenUri!!,
                                    productoId = productoId,
                                    onSuccess = { url -> guardar(url) },
                                    onError = { publicando = false; error = "Error subiendo imagen" }
                                )
                            } else {
                                guardar("")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                enabled = !publicando
            ) {
                if (publicando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Publicar platillo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
