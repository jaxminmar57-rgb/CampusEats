package com.jaz.myapplicationcampuseats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.ImageRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository

val categoriasLista = listOf(
    "Hamburguesas", "Pizza", "Hot Dogs", "Papas Fritas", "Sandwich", "Alitas",
    "Tacos", "Burritos", "Tortas", "Quesadillas", "Tamales", "Gorditas",
    "Chilaquiles", "Enchiladas", "Pozole", "Elotes", "Antojitos",
    "Sushi", "Pastas", "Ramen", "Comida China", "Comida Árabe",
    "Comida Corrida", "Mariscos", "Pollo", "Carne Asada", "Costillas",
    "Desayunos", "Snacks", "Churros", "Esquites", "Fruta",
    "Bebidas", "Café", "Smoothies", "Aguas Frescas", "Licuados", "Cerveza",
    "Postres", "Helados", "Pan Dulce", "Galletas", "Pasteles",
    "Ensaladas", "Saludable", "Vegano", "Bowls",
    "Otros"
)

@Composable
fun PublicarScreen(
    userId: String,
    nombreVendedor: String,
    ubicacionVendedor: String = "",
    preferenciaEntregaVendedor: String = "",
    onVolver: () -> Unit,
    productoExistente: Producto? = null   // null = nuevo, not null = edición
) {
    val modoEdicion = productoExistente != null

    var nombre                by remember { mutableStateOf(productoExistente?.nombre ?: "") }
    var precio                by remember { mutableStateOf(productoExistente?.precio?.takeIf { it > 0 }?.toString() ?: "") }
    var descripcion           by remember { mutableStateOf(productoExistente?.descripcion ?: "") }
    var ingredientes          by remember { mutableStateOf(productoExistente?.ingredientes ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf(productoExistente?.categoria ?: "Hamburguesas") }
    var menuCategoriaAbierto  by remember { mutableStateOf(false) }
    var imagenUri             by remember { mutableStateOf<Uri?>(null) }
    var publicando            by remember { mutableStateOf(false) }
    var error                 by remember { mutableStateOf("") }
    var publicado             by remember { mutableStateOf(false) }
    val pubCtx = androidx.compose.ui.platform.LocalContext.current

    // Stock
    var cantidadStr     by remember {
        mutableStateOf(
            if ((productoExistente?.cantidadDisponible ?: -1) >= 0) productoExistente!!.cantidadDisponible.toString() else ""
        )
    }
    var mostrarCantidad by remember { mutableStateOf(productoExistente?.mostrarCantidad ?: false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imagenUri = uri }

    if (publicado) {
        Box(Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 60.sp)
                Spacer(Modifier.height(16.dp))
                Text(if (modoEdicion) "¡Actualizado con éxito!" else "¡Publicado con éxito!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(if (modoEdicion) "Los cambios ya son visibles" else "Tu platillo ya es visible para los compradores", color = Color.Gray)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)) { Text("Volver") }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).statusBarsPadding().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text(if (modoEdicion) "Editar platillo" else "Publicar platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Imagen
            Text("Foto del platillo", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val imagenMostrar: Any? = imagenUri ?: productoExistente?.imagenUrl?.takeIf { it.isNotEmpty() }
            if (imagenMostrar != null) {
                AsyncImage(model = imagenMostrar, contentDescription = "Imagen",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Text(if (imagenMostrar != null) "Cambiar imagen" else "Seleccionar imagen", color = GreenBtn)
            }

            Spacer(Modifier.height(16.dp))

            // Nombre
            Text("Nombre del platillo *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it; error = "" },
                placeholder = { Text("Ej: Hamburguesa doble con papas", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = camposColores(), singleLine = true)

            Spacer(Modifier.height(14.dp))

            // Categoría con búsqueda
            Text("Categoría *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            var busquedaCategoria by remember { mutableStateOf("") }
            val categoriasFiltradas = remember(busquedaCategoria) {
                if (busquedaCategoria.isBlank()) categoriasLista
                else categoriasLista.filter {
                    coincideFuzzy(it, busquedaCategoria)
                }
            }
            Box {
                OutlinedButton(onClick = { menuCategoriaAbierto = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Text(categoriaSeleccionada, color = Color.White)
                }
                DropdownMenu(
                    expanded = menuCategoriaAbierto,
                    onDismissRequest = { menuCategoriaAbierto = false; busquedaCategoria = "" },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    OutlinedTextField(
                        value = busquedaCategoria,
                        onValueChange = { busquedaCategoria = it },
                        placeholder = { Text("Buscar categoría...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    categoriasFiltradas.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { categoriaSeleccionada = cat; menuCategoriaAbierto = false; busquedaCategoria = "" }
                        )
                    }
                    if (categoriasFiltradas.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Sin resultados", color = Color.Gray) },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Precio
            Text("Precio *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = precio,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) { precio = it; error = "" } },
                placeholder = { Text("Ej: 45.00", color = Color.Gray) }, prefix = { Text("\$", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = camposColores(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Spacer(Modifier.height(14.dp))

            // Descripción
            Text("Descripción *", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it; error = "" },
                placeholder = { Text("Describe tu platillo brevemente...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(10.dp), colors = camposColores(), maxLines = 4)

            Spacer(Modifier.height(14.dp))

            // Ingredientes
            Text("Ingredientes / Contenido", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Ayuda a los clientes con alergias o preferencias", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = ingredientes, onValueChange = { ingredientes = it },
                placeholder = { Text("Ej: pollo, lechuga, tomate, mayo...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(10.dp), colors = camposColores(), maxLines = 4)

            Spacer(Modifier.height(20.dp))

            // ── Stock opcional ──
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📦 Existencias (opcional)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Deja vacío para no tener límite de pedidos.", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cantidadStr,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) cantidadStr = it },
                        label = { Text("Cantidad disponible", color = Color.Gray) },
                        placeholder = { Text("Ej: 10", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = camposColores(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Mostrar cantidad a clientes", color = Color.White, fontSize = 14.sp)
                            Text(if (mostrarCantidad) "Los clientes verán cuántas quedan" else "Solo tú ves la cantidad", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(checked = mostrarCantidad, onCheckedChange = { mostrarCantidad = it },
                            modifier = Modifier.semantics { contentDescription = if (mostrarCantidad) "Cantidad visible, desactivar" else "Cantidad oculta, activar" },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBtn),
                            enabled = cantidadStr.isNotEmpty())
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (error.isNotEmpty()) { Text(error, color = RedCancel, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> error = "El nombre es obligatorio"
                        precio.isBlank() || precio.toDoubleOrNull() == null -> error = "Ingresa un precio válido"
                        descripcion.isBlank() -> error = "Agrega una descripción"
                        else -> {
                            publicando = true
                            val cantidad = cantidadStr.toIntOrNull() ?: -1

                            if (modoEdicion) {
                                // MODO EDICIÓN
                                val campos = mutableMapOf<String, Any>(
                                    "nombre" to nombre.trim(), "descripcion" to descripcion.trim(),
                                    "ingredientes" to ingredientes.trim(), "precio" to precio.toDouble(),
                                    "categoria" to categoriaSeleccionada, "cantidadDisponible" to cantidad,
                                    "mostrarCantidad" to (mostrarCantidad && cantidad >= 0),
                                    "ubicacionVendedor" to ubicacionVendedor,
                                    "preferenciaEntregaVendedor" to preferenciaEntregaVendedor
                                )
                                fun guardarEdicion(url: String) {
                                    if (url.isNotEmpty()) campos["imagenUrl"] = url
                                    ProductoRepository.actualizarProducto(productoExistente!!.id, campos,
                                        onSuccess = { publicando = false; publicado = true; com.jaz.myapplicationcampuseats.service.SoundManager.playExito(pubCtx) },
                                        onError = { publicando = false; error = "Error: ${it.message}" })
                                }
                                if (imagenUri != null) {
                                    ImageRepository.subirImagenProducto(imagenUri!!, productoExistente!!.id,
                                        onSuccess = { guardarEdicion(it) }, onError = { publicando = false; error = "Error subiendo imagen" })
                                } else { guardarEdicion("") }

                            } else {
                                // MODO PUBLICAR
                                val productoId = FirebaseFirestore.getInstance().collection("productos").document().id
                                fun guardar(url: String) {
                                    ProductoRepository.publicarProducto(
                                        Producto(id = productoId, nombre = nombre.trim(), descripcion = descripcion.trim(),
                                            ingredientes = ingredientes.trim(), precio = precio.toDouble(), imagenUrl = url,
                                            categoria = categoriaSeleccionada, vendedorId = userId, nombreVendedor = nombreVendedor,
                                            disponible = true, cantidadDisponible = cantidad, mostrarCantidad = mostrarCantidad && cantidad >= 0,
                                            ubicacionVendedor = ubicacionVendedor, preferenciaEntregaVendedor = preferenciaEntregaVendedor),
                                        onSuccess = { publicando = false; publicado = true; com.jaz.myapplicationcampuseats.service.SoundManager.playExito(pubCtx) },
                                        onError = { publicando = false; error = "Error: ${it.message}" })
                                }
                                if (imagenUri != null) {
                                    ImageRepository.subirImagenProducto(imagenUri!!, productoId,
                                        onSuccess = { guardar(it) }, onError = { publicando = false; error = "Error subiendo imagen" })
                                } else { guardar("") }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn), enabled = !publicando
            ) {
                if (publicando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else Text(if (modoEdicion) "Guardar cambios" else "Publicar platillo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
