package com.jaz.myapplicationcampuseats

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

@Composable
fun PublicarScreen(onVolver: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugarEntrega by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Hamburguesas") }
    var publicado by remember { mutableStateOf(false) }
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    if (publicado) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "✅", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "¡Publicado con éxito!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Tu comida ya está visible para los compradores", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(text = "Volver al inicio", color = Color.White, fontSize = 16.sp)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(text = "Publicar comida", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            Text(text = "Nombre del platillo:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Ej: Burrito de pollo", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = Color(0xFF2D2D44),
                    focusedContainerColor = Color(0xFF2D2D44)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Precio ($):", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                placeholder = { Text("Ej: 45", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = Color(0xFF2D2D44),
                    focusedContainerColor = Color(0xFF2D2D44)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Categoría:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                OutlinedTextField(
                    value = categoriaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona una categoría", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { menuCategoriaAbierto = true }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Gray,
                                modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = GreenBtn,
                        unfocusedContainerColor = Color(0xFF2D2D44),
                        focusedContainerColor = Color(0xFF2D2D44)
                    )
                )
                DropdownMenu(
                    expanded = menuCategoriaAbierto,
                    onDismissRequest = { menuCategoriaAbierto = false },
                    modifier = Modifier.background(Color(0xFF2D2D44))
                ) {
                    categorias.forEach { (cat, _) ->
                        DropdownMenuItem(
                            text = { Text(text = cat, color = Color.White) },
                            onClick = {
                                categoriaSeleccionada = cat
                                menuCategoriaAbierto = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Lugar de entrega:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = lugarEntrega,
                onValueChange = { lugarEntrega = it },
                placeholder = { Text("Ej: Cafetería principal, Edificio A", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = Color(0xFF2D2D44),
                    focusedContainerColor = Color(0xFF2D2D44)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Descripción y especificaciones:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Ej: Incluye arroz y frijoles, sin picante", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = GreenBtn,
                    unfocusedContainerColor = Color(0xFF2D2D44),
                    focusedContainerColor = Color(0xFF2D2D44)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nombre.isNotEmpty() && precio.isNotEmpty() && lugarEntrega.isNotEmpty()) {
                        publicado = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Publicar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}