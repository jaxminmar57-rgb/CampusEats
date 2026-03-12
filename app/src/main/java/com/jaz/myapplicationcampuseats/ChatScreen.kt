package com.jaz.myapplicationcampuseats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape

data class Mensaje(
    val texto: String,
    val esPropio: Boolean,
    val hora: String
)

@Composable
fun ChatScreen(
    nombreVendedor: String,
    nombreComprador: String,
    onVolver: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val mensajes = remember {
        mutableStateListOf(
            Mensaje("Hola! Vi tu publicación de comida", true, "10:00"),
            Mensaje("Hola! Sí, aún está disponible 😊", false, "10:01"),
            Mensaje("¿A qué hora puedes entregar?", true, "10:02"),
            Mensaje("En unos 20 minutos en la cafetería principal", false, "10:03")
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16213E))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column {
                Text(
                    text = nombreVendedor,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "En línea", color = GreenBtn, fontSize = 12.sp)
            }
        }

        // Mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mensajes) { mensaje ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mensaje.esPropio) Arrangement.End else Arrangement.Start
                ) {
                    Column(horizontalAlignment = if (mensaje.esPropio) Alignment.End else Alignment.Start) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (mensaje.esPropio) GreenBtn else Color(0xFF2D2D44),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (mensaje.esPropio) 16.dp else 4.dp,
                                        bottomEnd = if (mensaje.esPropio) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .widthIn(max = 260.dp)
                        ) {
                            Text(text = mensaje.texto, color = Color.White, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = mensaje.hora, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Campo de texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16213E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = Color(0xFF2D2D44),
                    focusedContainerColor = Color(0xFF2D2D44)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (texto.isNotEmpty()) {
                        val hora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date())
                        mensajes.add(Mensaje(texto, true, hora))
                        texto = ""
                        scope.launch {
                            listState.animateScrollToItem(mensajes.size - 1)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(GreenBtn, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}