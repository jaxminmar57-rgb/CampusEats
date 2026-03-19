package com.jaz.myapplicationcampuseats.ui

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores principales de CampusEats
val DarkBg       = Color(0xFF0D1F17)
val DarkCard     = Color(0xFF1A2E21)
val DarkSurface  = Color(0xFF2D2D44)
val DarkSurface2 = Color(0xFF3D3D54)
val GreenBtn     = Color(0xFF4CAF50)
val GreenDark    = Color(0xFF1B3A2D)
val GreenLight   = Color(0xFF81C784)
val OrangeWarn   = Color(0xFFFF9800)
val RedCancel    = Color(0xFFE53935)
val GoldStar     = Color(0xFFFFC107)

// Colores de campos de texto compartidos en toda la app
@Composable
fun camposColores() = OutlinedTextFieldDefaults.colors(
    unfocusedTextColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedBorderColor = GreenBtn,
    unfocusedContainerColor = DarkSurface,
    focusedContainerColor = DarkSurface,
    cursorColor = GreenBtn
)
