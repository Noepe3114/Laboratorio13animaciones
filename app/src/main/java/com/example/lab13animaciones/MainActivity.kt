package com.example.lab13animaciones
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaPrincipal()
                }
            }
        }
    }
}
@Composable
fun PantallaPrincipal() {
    var seleccionado by remember { mutableIntStateOf(1) }
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (1..4).forEach { n ->
                Button(
                    onClick = { seleccionado = n },
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) { Text("E$n") }
            }
        }
        when (seleccionado) {
            1 -> Ejercicio1()
            2 -> Ejercicio2()
        }
    }
}