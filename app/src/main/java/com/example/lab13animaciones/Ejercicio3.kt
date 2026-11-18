package com.example.lab13animaciones

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Ejercicio3() {
    var expandido by remember { mutableStateOf(false) }

    val tamano by animateDpAsState(
        targetValue = if (expandido) 120.dp else 60.dp,
        animationSpec = tween(durationMillis = 700),
        label = "tamano"
    )
    val desplazamientoX by animateDpAsState(
        targetValue = if (expandido) 150.dp else 0.dp,
        animationSpec = tween(durationMillis = 700),
        label = "offsetX"
    )
    val desplazamientoY by animateDpAsState(
        targetValue = if (expandido) 80.dp else 0.dp,
        animationSpec = tween(durationMillis = 700),
        label = "offsetY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { expandido = !expandido }) {
            Text("Mover y cambiar tamaño")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cuadro rojo: offset → size → background",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEEEEE))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = desplazamientoX, y = desplazamientoY)
                    .size(tamano)
                    .background(Color(0xFFE53935))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cuadro naranja: size → offset → padding → background",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEEEEE))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(tamano)
                    .offset(x = desplazamientoX, y = desplazamientoY)
                    .padding(10.dp)
                    .background(Color(0xFFFB8C00))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}