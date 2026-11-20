@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun JuegoEstrellas() {
    val trans = rememberInfiniteTransition(label = "estrellas")
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(900, 1300, 1100, 1500, 1000).forEachIndexed { i, ms ->
            val alfa by trans.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(ms), RepeatMode.Reverse),
                label = "estrella$i"
            )
            Box(
                modifier = Modifier
                    .size(if (i % 2 == 0) 6.dp else 9.dp)
                    .graphicsLayer { this.alpha = alfa }
                    .clip(CircleShape)
                    .background(JuegoDorado)
            )
        }
    }
}

@Composable
internal fun JuegoSelectorDificultad(actual: JuegoDificultad, onCambiar: (JuegoDificultad) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            JuegoDificultad.entries.forEach { d ->
                val seleccionada = d == actual
                val fondo by animateColorAsState(
                    targetValue = if (seleccionada) JuegoAzul else Color(0xFF2A3355),
                    animationSpec = tween(250),
                    label = "dificultad${d.name}"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(fondo)
                        .clickable { onCambiar(d) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        d.nombre,
                        color = if (seleccionada) JuegoFondo else JuegoTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Vida ${actual.vida} · Oxígeno ${actual.oxigeno} · Puntos ${actual.puntosPct}%\nEnemigos ${actual.vidaEnemigoPct}% vida · Botín ${actual.botinPct}%",
            color = JuegoTextoSuave,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun JuegoTarjetaMejora(mejora: JuegoMejora, indice: Int, onElegir: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((150 * indice).milliseconds)
        visible = true
    }
    val alfa by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "alfaMejora$indice"
    )
    val desplazamiento by animateDpAsState(
        targetValue = if (visible) 0.dp else 24.dp,
        animationSpec = tween(400),
        label = "yMejora$indice"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = desplazamiento)
            .graphicsLayer { this.alpha = alfa }
            .clip(RoundedCornerShape(16.dp))
            .background(JuegoColorTablero)
            .clickable(enabled = visible) { onElegir() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JuegoSpriteVista(mejora.sprite(), Modifier.size(44.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text("${indice + 1}. ${mejora.titulo}", color = JuegoTexto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(mejora.detalle, color = JuegoTextoSuave, fontSize = 14.sp)
        }
    }
}

@Composable
internal fun JuegoPantallaMejora(estado: JuegoEstadoUi, onElegir: (JuegoMejora) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Piso ${estado.piso} completado", color = JuegoVerde, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(estado.mensaje, color = JuegoDorado, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Text("Elige una mejora", color = JuegoTexto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            estado.opciones.forEachIndexed { i, m ->
                JuegoTarjetaMejora(m, i) { onElegir(m) }
            }
        }
    }
}

@Composable
internal fun JuegoCambioPiso(estadoActual: JuegoEstadoUi) {
    val datos = remember { estadoActual }
    val trans = rememberInfiniteTransition(label = "cambioPiso")
    val flote by trans.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "floteCambio"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JuegoSpriteVista(JuegoSprite.COHETE, Modifier.size(84.dp).offset(y = flote.dp))
        Spacer(Modifier.height(12.dp))
        Text("Piso ${datos.piso + 1}", color = JuegoTexto, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(juegoNombrePiso(datos.piso + 1), color = JuegoAzul, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Descendiendo a la siguiente cubierta...", color = JuegoTextoSuave, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            datos.mensaje,
            color = JuegoVerde,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text("Puntos: ${datos.puntos}", color = JuegoDorado, fontSize = 16.sp)
    }
}

@Composable
internal fun JuegoFin(
    gano: Boolean,
    estado: JuegoEstadoUi,
    mejor: Int,
    esRecord: Boolean,
    onReiniciar: () -> Unit
) {
    val datos = remember { estado }
    var listo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { listo = true }
    val puntosAnim by animateIntAsState(
        targetValue = if (listo) datos.puntos else 0,
        animationSpec = tween(1200),
        label = "puntosFinal"
    )
    val alfaRecord by animateFloatAsState(
        targetValue = if (listo && esRecord) 1f else 0f,
        animationSpec = tween(600, delayMillis = 900),
        label = "alfaRecord"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gano) JuegoSpriteVista(JuegoSprite.COHETE, Modifier.size(88.dp))
        else JuegoSpriteVista(JuegoSprite.ASTRONAUTA, Modifier.size(88.dp).graphicsLayer { this.alpha = 0.5f })
        Spacer(Modifier.height(12.dp))
        Text(
            if (gano) "¡Escapaste!" else "Game Over",
            color = if (gano) JuegoVerde else JuegoRojo,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (gano) "Superaste las $JUEGO_PISOS cubiertas, derrotaste al Centinela y llegaste a la cápsula de escape."
            else "${datos.mensaje} en el piso ${datos.piso}.",
            color = JuegoTextoSuave,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(JuegoColorTablero)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                JuegoSpriteVista(JuegoSprite.CRISTAL, Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Puntos: $puntosAnim", color = JuegoDorado, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("Dificultad: ${datos.dificultad.nombre}", color = JuegoTexto, fontSize = 15.sp)
            Text("Piso alcanzado: ${datos.piso} de $JUEGO_PISOS", color = JuegoTexto, fontSize = 15.sp)
            Text("Enemigos derrotados: ${datos.derrotados}", color = JuegoTexto, fontSize = 15.sp)
            Text("Mejor combo: x${datos.comboMax}", color = JuegoTexto, fontSize = 15.sp)
            Text("Turnos jugados: ${datos.turno}", color = JuegoTexto, fontSize = 15.sp)
            Text("Mejor puntaje (${datos.dificultad.nombre}): $mejor", color = JuegoTexto, fontSize = 15.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "¡Nuevo récord!",
            color = JuegoDorado,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer { this.alpha = alfaRecord }
        )
        Spacer(Modifier.height(16.dp))
        JuegoBoton(if (gano) "JUGAR OTRA VEZ" else "VOLVER AL MENÚ", onReiniciar, color = JuegoDorado, grande = true)
    }
}

// ───────────────────────── Menú y pausa ─────────────────────────

@Composable
internal fun JuegoMenu(
    mejor: Int,
    dificultad: JuegoDificultad,
    onCambiar: (JuegoDificultad) -> Unit,
    onJugar: () -> Unit
) {
    val trans = rememberInfiniteTransition(label = "menu")
    val flote by trans.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "floteCohete"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JuegoEstrellas()
        Spacer(Modifier.height(12.dp))
        JuegoSpriteVista(JuegoSprite.COHETE, Modifier.size(96.dp).offset(y = flote.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            "ESTACIÓN ESPACIAL Z",
            color = JuegoTexto,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "La estación está fallando. Explora sus $JUEGO_PISOS cubiertas, derrota a sus defensas, fabrica equipo con su botín y llega a la cápsula de escape antes de quedarte sin oxígeno.",
            color = JuegoTextoSuave,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            JuegoSpriteVista(JuegoSprite.ARMA_PISTOLA, Modifier.size(32.dp))
            JuegoSpriteVista(JuegoSprite.MUNICION, Modifier.size(32.dp))
            JuegoSpriteVista(JuegoSprite.PECHERA, Modifier.size(32.dp))
            JuegoSpriteVista(JuegoSprite.TARJETA, Modifier.size(32.dp))
            JuegoSpriteVista(JuegoSprite.CIRCUITO, Modifier.size(32.dp))
            JuegoSpriteVista(JuegoSprite.COFRE, Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().juegoPanel().padding(14.dp)
        ) {
            Text(
                "• Toca una casilla para moverte; tocar una lejana viaja hasta allí por la ruta más corta (incluye diagonales)\n" +
                        "• Toca un enemigo visible (o usa DISPARO) para disparar: hay alcance, precisión y línea de tiro\n" +
                        "• Recarga con RECARGA; la munición de reserva está en tu mochila\n" +
                        "• Los enemigos sueltan botín: acércate para recogerlo\n" +
                        "• Toca el banco de trabajo (esclusa de cada piso) para fabricar y mejorar\n" +
                        "• MOCHILA: equipa armas y piezas del traje, compara y usa consumibles\n" +
                        "• Teclado: flechas/WASD, Q E Z C diagonales, G dispara, R recarga, F pulso, I mochila, P pausa\n" +
                        "• Cada paso gasta oxígeno; el fluido verde gasta más\n" +
                        "• Cuidado con las trampas, las torretas y el Centinela",
                color = JuegoTexto,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        JuegoSelectorDificultad(dificultad, onCambiar)
        if (mejor > 0) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Mejor puntaje (${dificultad.nombre}): $mejor",
                color = JuegoDorado,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(24.dp))
        JuegoBoton("DESPEGAR", onJugar, color = JuegoNeon, grande = true)
    }
}

@Composable
internal fun JuegoPausa(estado: JuegoEstadoUi, onReanudar: () -> Unit, onRendirse: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60B1020))
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PAUSA", color = JuegoTexto, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Piso ${estado.piso} · ${juegoNombrePiso(estado.piso)}",
            color = JuegoAzul,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Puntos ${estado.puntos} · Turno ${estado.turno} · Derrotados ${estado.derrotados} · Mejor combo x${estado.comboMax}",
            color = JuegoTextoSuave,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        JuegoBoton("REANUDAR", onReanudar, color = JuegoVerde, grande = true)
        Spacer(Modifier.height(12.dp))
        JuegoBoton("RENDIRSE", onRendirse, color = JuegoRojo)
    }
}
