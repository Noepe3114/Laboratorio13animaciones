package com.example.lab13animaciones
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun Ejercicio1() {
    var visible by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "Ocultar cuadro" else "Mostrar cuadro")
        }

        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 800)),
            exit = fadeOut(animationSpec = tween(durationMillis = 800))
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color(0xFF6200EE))
            )
        }

        Text("Texto debajo del cuadro", modifier = Modifier.padding(top = 16.dp))
    }
}