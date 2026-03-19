package com.jaz.myapplicationcampuseats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaz.myapplicationcampuseats.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2500)
        onFinish()
    }

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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Comida de campus, sin complicaciones",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}
