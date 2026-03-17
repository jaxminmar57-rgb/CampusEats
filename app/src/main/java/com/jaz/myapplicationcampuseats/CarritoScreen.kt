package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.repository.CarritoRepository
import com.jaz.myapplicationcampuseats.repository.PedidoRepository

@Composable
fun CarritoScreen(
    items: List<ItemCarrito>,
    onVolver: () -> Unit,
    onPedir: () -> Unit
) {

    var items by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }

    LaunchedEffect(Unit) {

        CarritoRepository.obtenerCarrito {

            items = it

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onVolver) {

                Icon(Icons.Default.ArrowBack, "", tint = Color.White)

            }

            Text("Carrito", color = Color.White)

        }

        LazyColumn {

            items(items) { item ->

                ItemCarritoCard(item)

            }

        }
        /**
         * Botón para confirmar compra y crear pedido
         */
        Button(

            onClick = {

                PedidoRepository.crearPedido(items) {

                    items = emptyList()

                }

            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = GreenBtn
            )

        ) {

            Text("Confirmar pedido")

        }

    }

}

@Composable
fun ItemCarritoCard(item: ItemCarrito) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = item.imagenUrl,
                contentDescription = item.nombre,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(item.nombre)

                Text("$${item.precio}")

            }

            IconButton(
                onClick = {

                    CarritoRepository.eliminarItem(item.id)

                }
            ) {

                Text("❌")

            }

        }

    }

}