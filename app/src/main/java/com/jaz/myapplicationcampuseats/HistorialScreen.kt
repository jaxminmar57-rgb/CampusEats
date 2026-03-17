package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.repository.PedidoRepository

/**
 * Pantalla que muestra el historial de pedidos del usuario
 */
@Composable
fun HistorialScreen(
    onVolver: () -> Unit,
    onChat: (String) -> Unit) {

    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }

    LaunchedEffect(Unit) {

        PedidoRepository.obtenerPedidosUsuario {

            pedidos = it

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {

        Text(
            "Historial de pedidos",
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn {

            items(pedidos) { pedido ->

                PedidoCard(pedido)

            }

        }

    }

}

@Composable
fun PedidoCard(pedido: Pedido) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text("Pedido: ${pedido.id}")

            Text("Estado: ${pedido.estado}")

            Text("Total: $${pedido.total}")

        }

    }

}