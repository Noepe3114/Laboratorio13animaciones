package com.example.lab13animaciones
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
sealed class EstadoPantalla {
    object Cargando : EstadoPantalla()
    object Contenido : EstadoPantalla()
    object Error : EstadoPantalla()
}
@Composable
fun Ejercicio4() {
    var estado by remember { mutableStateOf<EstadoPantalla>(EstadoPantalla.Cargando) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            estado = when (estado) {
                EstadoPantalla.Cargando -> EstadoPantalla.Contenido
                EstadoPantalla.Contenido -> EstadoPantalla.Error
                EstadoPantalla.Error -> EstadoPantalla.Cargando
            }
        }) {
            Text("Siguiente estado")
        }
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedContent(
            targetState = estado,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 200)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 300))
            },
            label = "contenidoAnimado"
        ) { estadoActual ->
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                when (estadoActual) {
                    EstadoPantalla.Cargando -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cargando datos...", fontSize = 18.sp)
                        }
                    }
                    EstadoPantalla.Contenido -> {
                        Text(
                            "¡Contenido cargado con éxito!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    EstadoPantalla.Error -> {
                        Text(
                            "Error: no se pudo conectar",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}