package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.CarritoRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.ItemCarrito


@Composable
fun CategoriaScreen(
    categoria: String,
    onVolver: () -> Unit
) {

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    var cargando by remember { mutableStateOf(true) }

    /**
     * Cargar productos desde Firebase
     */
    LaunchedEffect(Unit) {

        ProductoRepository.obtenerProductosPorCategoria(categoria) { lista ->

            productos = lista
            cargando = false

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onVolver) {

                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Text(
                text = categoria,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

        }

        if (cargando) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()

            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(productos) { producto ->

                    ProductoCard(producto)

                }

            }

        }

    }

}

@Composable
/**
 * Tarjeta que muestra un producto y permite agregarlo al carrito
 */
fun ProductoCard(producto: Producto) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column {

            AsyncImage(
                model = producto.imagenUrl,
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(producto.nombre)

                Spacer(Modifier.height(4.dp))

                Text(producto.descripcion)

                Spacer(Modifier.height(8.dp))

                Text("$${producto.precio}")

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {

                        val item = ItemCarrito(

                            productoId = producto.id,

                            nombre = producto.nombre,

                            precio = producto.precio,

                            imagenUrl = producto.imagenUrl,

                            vendedorId = producto.vendedorId
                        )

                        CarritoRepository.agregarProducto(item)

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenBtn
                    )
                ) {

                    Text("Agregar al carrito")

                }

            }

        }

    }

}