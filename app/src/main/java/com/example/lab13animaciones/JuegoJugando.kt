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

// ───────────────────────── Pantalla de juego ─────────────────────────

@Composable
internal fun JuegoPantallaJugando(
    estado: JuegoEstadoUi,
    pausa: Boolean,
    onPausa: () -> Unit,
    onReanudar: () -> Unit,
    onRendirse: () -> Unit,
    onMochila: () -> Unit,
    onTocar: (JuegoPos) -> Unit,
    onPulso: () -> Unit,
    onEsperar: () -> Unit,
    onDisparar: () -> Unit,
    onRecargar: () -> Unit,
    onBotiquin: () -> Unit,
    onOxigeno: () -> Unit
) {
    val jefe = estado.enemigos.firstOrNull { it.tipo == JuegoTipoEnemigo.CENTINELA }
    val etiqueta = buildString {
        append(juegoNombreSala(estado).uppercase())
        if (estado.combo >= 2) append(" · COMBO x${estado.combo}")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            JuegoHud(estado, onBotiquin, onOxigeno)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    etiqueta,
                    color = JuegoTextoSuave,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                JuegoBoton("MOCHILA", onMochila, color = JuegoNeon)
                JuegoBoton("PAUSA", onPausa, color = Color(0xFF7986CB))
            }

            if (jefe != null) JuegoBarraJefe(jefe)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                JuegoMapa(estado, onTocar, Modifier.fillMaxSize())
                JuegoAvisos(estado.avisos, Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
            }

            JuegoPanelArma(estado)
            JuegoPanelInferior(estado, onPulso, onEsperar, onDisparar, onRecargar)
        }
        JuegoAlarma(estado.oxigeno <= JUEGO_OXIGENO_BAJO || estado.vida <= JUEGO_VIDA_BAJA)
        AnimatedVisibility(
            visible = pausa,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            JuegoPausa(estado, onReanudar, onRendirse)
        }
    }
}

@Composable
private fun JuegoBotonRapido(icono: JuegoSprite, cantidad: Int, onClick: () -> Unit) {
    val alfa by animateFloatAsState(if (cantidad > 0) 1f else 0.35f, tween(250), label = "rapido")
    Row(
        modifier = Modifier
            .graphicsLayer { this.alpha = alfa }
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JuegoSpriteVista(icono, Modifier.size(20.dp))
        Spacer(Modifier.width(3.dp))
        Text("×$cantidad", color = JuegoTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun JuegoHud(estado: JuegoEstadoUi, onBotiquin: () -> Unit, onOxigeno: () -> Unit) {
    val traje = remember(estado.equipo) { juegoTraje(estado.equipo) }
    val proporcion = estado.oxigeno / estado.oxigenoMax.toFloat()
    val colorO2 by animateColorAsState(
        targetValue = when {
            proporcion > 0.6f -> JuegoVerde
            proporcion > 0.3f -> JuegoAmarillo
            else -> JuegoRojo
        },
        animationSpec = tween(500),
        label = "colorOxigeno"
    )
    val nBotiquines = juegoContar(estado.inventario, JuegoTipoItem.CON_BOTIQUIN) +
            juegoContar(estado.inventario, JuegoTipoItem.CON_BOTIQUIN_AV)
    val nOxigeno = juegoContar(estado.inventario, JuegoTipoItem.CON_OXIGENO)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            JuegoContador(estado.piso, "/$JUEGO_PISOS", "Piso")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                JuegoIndicador(estado.tieneTarjeta) { JuegoSpriteVista(JuegoSprite.TARJETA, Modifier.size(24.dp)) }
                JuegoIndicador(estado.escudo) { JuegoSpriteVista(JuegoSprite.ESCUDO, Modifier.size(22.dp)) }
                JuegoBotonRapido(JuegoSprite.BOTIQUIN, nBotiquines, onBotiquin)
                JuegoBotonRapido(JuegoSprite.TANQUE, nOxigeno, onOxigeno)
            }
            JuegoContador(estado.puntos, "") { JuegoSpriteVista(JuegoSprite.CRISTAL, Modifier.size(18.dp)) }
        }
        JuegoBarraEstado(
            JuegoSprite.CORAZON,
            estado.vida / estado.vidaMax.toFloat(),
            JuegoRojo,
            "${estado.vida}/${estado.vidaMax}"
        )
        if (traje.escudoMax > 0) {
            JuegoBarraEstado(
                JuegoSprite.ESCUDO,
                estado.escudoPts / traje.escudoMax.toFloat(),
                JuegoAzul,
                "Escudo ${estado.escudoPts}/${traje.escudoMax}",
                alto = 10.dp
            )
        }
        JuegoBarraEstado(
            JuegoSprite.TANQUE,
            proporcion,
            colorO2,
            "O₂ ${estado.oxigeno}/${estado.oxigenoMax}"
        )
    }
}

@Composable
internal fun JuegoBarraJefe(jefe: JuegoEnemigo) {
    val fase = juegoFaseJefe(jefe)
    val fraccion = (jefe.vida / jefe.vidaMax.toFloat()).coerceIn(0f, 1f)
    val frente by animateFloatAsState(targetValue = fraccion, animationSpec = tween(300), label = "vidaJefe")
    val estela by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(900, delayMillis = 350),
        label = "estelaJefe"
    )
    val trans = rememberInfiniteTransition(label = "latidoJefe")
    val brillo by trans.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "brilloJefe"
    )
    val colorTexto = when {
        !jefe.vivo -> JuegoVerde
        fase == 3 -> JuegoRojo
        fase == 2 -> JuegoAmarillo
        else -> JuegoTexto
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .juegoPanel(Color(0xFFB71C1C))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JuegoSpriteVista(JuegoSprite.CENTINELA, Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (!jefe.vivo) "CENTINELA DERROTADO" else "CENTINELA · FASE $fase/3",
                color = colorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text("${jefe.vida}/${jefe.vidaMax}", color = JuegoTexto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF2B1B45))
        ) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(estela).background(JuegoAmarillo.copy(alpha = 0.9f)))
            Box(Modifier.fillMaxHeight().fillMaxWidth(frente).background(JuegoRojo.copy(alpha = brillo)))
        }
    }
}

/** Arma equipada, cargador, reserva y estado de recarga. */
@Composable
internal fun JuegoPanelArma(estado: JuegoEstadoUi) {
    val arma = estado.equipo[JuegoRanura.ARMA]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .juegoPanel(arma?.rareza?.color ?: Color(0xFF39406A))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (arma == null) {
            Text(
                "Sin arma equipada: abre la mochila para equipar una",
                color = JuegoTextoSuave,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            return@Row
        }
        val s = juegoStatsItem(arma)
        val reserva = juegoReserva(estado.inventario, s.familia)
        val recargando = estado.recarga > 0
        JuegoIconoItem(arma.tipo, arma.rareza, 38.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(arma.nombreCompleto(), color = arma.rareza.color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(s.cargador.coerceAtMost(30)) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                when {
                                    recargando -> JuegoAzul.copy(alpha = 0.5f)
                                    i < arma.municion -> JuegoDorado
                                    else -> Color(0xFF2A3355)
                                }
                            )
                    )
                }
            }
            Text(
                if (recargando) "RECARGANDO… ${estado.recarga} turno(s)"
                else "${s.familia?.danio?.nombre ?: "Cinético"} · alcance ${s.alcance} · precisión ${s.precision}%",
                color = if (recargando) JuegoAzul else JuegoTextoSuave,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${arma.municion}/${s.cargador}",
                color = if (arma.municion == 0) JuegoRojo else JuegoTexto,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "⛁ $reserva",
                color = if (reserva == 0) JuegoRojo else JuegoAzul,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun JuegoBotonAccion(
    texto: String,
    icono: JuegoSprite?,
    color: Color,
    activo: Boolean,
    progreso: Float,
    onClick: () -> Unit
) {
    val alfa by animateFloatAsState(if (activo) 1f else 0.45f, tween(250), label = "alfaAccion")
    Column(
        modifier = Modifier
            .width(54.dp)
            .graphicsLayer { this.alpha = alfa }
            .clip(JuegoCorte)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0.15f))))
            .border(1.dp, color, JuegoCorte)
            .clickable { onClick() }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icono != null) {
            JuegoSpriteVista(icono, Modifier.size(22.dp))
        } else {
            Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) { Text("⏳", fontSize = 15.sp) }
        }
        Text(texto, color = JuegoTexto, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        if (progreso >= 0f) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x55000000))
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(progreso.coerceIn(0f, 1f)).background(JuegoDorado)
                )
            }
        }
    }
}

@Composable
internal fun JuegoPanelInferior(
    estado: JuegoEstadoUi,
    onPulso: () -> Unit,
    onEsperar: () -> Unit,
    onDisparar: () -> Unit,
    onRecargar: () -> Unit
) {
    val texto = estado.mensaje.ifEmpty { "Toca un enemigo para disparar o una casilla para moverte" }
    val arma = estado.equipo[JuegoRanura.ARMA]
    val s = if (arma != null) juegoStatsItem(arma) else null
    val hayObjetivo = juegoObjetivoCercano(estado) != null
    val puedeDisparar = arma != null && arma.municion > 0 && estado.recarga == 0 && hayObjetivo
    val puedeRecargar = arma != null && s != null && arma.municion < s.cargador &&
            juegoReserva(estado.inventario, s.familia) > 0 && estado.recarga == 0
    val pulsoListo = estado.pulsoEspera == 0
    val cargaPulso = if (pulsoListo) 1f else 1f - estado.pulsoEspera / estado.pulsoRecarga.toFloat()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JuegoMinimapa(estado, Modifier.size(84.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = texto,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "mensajeJuego"
            ) { t ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t, color = JuegoTexto, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 4)
                }
            }
            Text(
                "Turno ${estado.turno} · Enemigos: ${estado.enemigos.count { it.vivo }}",
                color = JuegoTextoSuave,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                JuegoBotonAccion(
                    texto = if (pulsoListo) "PULSO" else "${estado.pulsoEspera}",
                    icono = JuegoSprite.PULSO,
                    color = JuegoAzul,
                    activo = pulsoListo,
                    progreso = cargaPulso,
                    onClick = onPulso
                )
                JuegoBotonAccion(
                    texto = "DISPARO",
                    icono = arma?.tipo?.icono ?: JuegoSprite.ARMA_PISTOLA,
                    color = JuegoRojo,
                    activo = puedeDisparar,
                    progreso = -1f,
                    onClick = onDisparar
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                JuegoBotonAccion(
                    texto = if (estado.recarga > 0) "…${estado.recarga}" else "RECARGA",
                    icono = JuegoSprite.MUNICION,
                    color = JuegoDorado,
                    activo = puedeRecargar,
                    progreso = -1f,
                    onClick = onRecargar
                )
                JuegoBotonAccion(
                    texto = "ESPERAR",
                    icono = null,
                    color = Color(0xFF7986CB),
                    activo = true,
                    progreso = -1f,
                    onClick = onEsperar
                )
            }
        }
    }
}

// ───────────────────────── Efectos de combate ─────────────────────────

@Composable
internal fun JuegoDisparoVista(d: JuegoDisparo, celdaPx: Float, camX: State<Float>, camY: State<Float>) {
    var activa by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(d.retraso.milliseconds)
        activa = true
    }
    val prog by animateFloatAsState(
        targetValue = if (activa) 1f else 0f,
        animationSpec = tween(190),
        label = "disparo"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (!activa || prog >= 1f) return@Canvas
        val a = Offset((d.desde.col + 0.5f - camX.value) * celdaPx, (d.desde.fila + 0.5f - camY.value) * celdaPx)
        val b = Offset((d.hasta.col + 0.5f - camX.value) * celdaPx, (d.hasta.fila + 0.5f - camY.value) * celdaPx)
        val dir = b - a
        val largo = dir.getDistance()
        if (largo == 0f) return@Canvas
        val perp = Offset(-dir.y / largo, dir.x / largo)
        val desvio = if (d.acierta) 0f else celdaPx * 0.45f * (if (d.id % 2 == 0) 1f else -1f)
        val fin = b + perp * desvio
        val cabeza = a + (fin - a) * prog
        val cola = a + (fin - a) * maxOf(0f, prog - 0.4f)
        val alfa = 1f - prog * 0.6f
        val col = d.danio.color
        when (d.danio) {
            JuegoTipoDanio.CINETICO -> {
                drawLine(color = col.copy(alpha = alfa), start = cola, end = cabeza, strokeWidth = celdaPx * 0.07f)
                drawCircle(color = Color.White.copy(alpha = alfa), radius = celdaPx * 0.06f, center = cabeza)
            }
            JuegoTipoDanio.PLASMA -> {
                drawLine(color = col.copy(alpha = 0.35f * alfa), start = cola, end = cabeza, strokeWidth = celdaPx * 0.24f)
                drawLine(color = Color.White.copy(alpha = alfa), start = cola, end = cabeza, strokeWidth = celdaPx * 0.07f)
                drawCircle(color = col.copy(alpha = alfa), radius = celdaPx * 0.13f, center = cabeza)
            }
            JuegoTipoDanio.ELECTRICO -> {
                val pasos = 6
                var previo = cola
                for (i in 1..pasos) {
                    val t = i / pasos.toFloat()
                    val base = cola + (cabeza - cola) * t
                    val salto = if (i == pasos) 0f else sin(d.id * 3.1f + i * 1.7f) * celdaPx * 0.14f
                    val p = base + perp * salto
                    drawLine(color = col.copy(alpha = alfa), start = previo, end = p, strokeWidth = celdaPx * 0.06f)
                    previo = p
                }
                drawCircle(color = Color.White.copy(alpha = alfa), radius = celdaPx * 0.07f, center = cabeza)
            }
        }
    }
}

@Composable
internal fun JuegoEfectoVista(ef: JuegoEfecto, celdaPx: Float, camX: State<Float>, camY: State<Float>) {
    var activo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { activo = true }
    val duracion = when (ef.tipo) {
        JuegoTipoEfecto.EXPLOSION -> 520
        JuegoTipoEfecto.IMPACTO -> 260
        JuegoTipoEfecto.DESTELLO -> 800
        JuegoTipoEfecto.ADVERTENCIA -> 900
    }
    val p by animateFloatAsState(
        targetValue = if (activo) 1f else 0f,
        animationSpec = tween(duracion),
        label = "efecto"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (!activo || p >= 1f) return@Canvas
        val c = Offset((ef.pos.col + 0.5f - camX.value) * celdaPx, (ef.pos.fila + 0.5f - camY.value) * celdaPx)
        val a = (1f - p).coerceIn(0f, 1f)
        when (ef.tipo) {
            JuegoTipoEfecto.EXPLOSION -> {
                drawCircle(color = ef.color.copy(alpha = 0.55f * a), radius = celdaPx * (0.25f + 0.55f * ef.radio * p), center = c)
                drawCircle(color = Color.White.copy(alpha = 0.8f * a), radius = celdaPx * 0.28f * (1f - p), center = c)
                drawCircle(
                    color = ef.color.copy(alpha = a),
                    radius = celdaPx * (0.3f + 0.7f * ef.radio * p),
                    center = c,
                    style = Stroke(width = celdaPx * 0.06f)
                )
                for (i in 0 until 8) {
                    val ang = i * PI / 4
                    val v = Offset(cos(ang).toFloat(), sin(ang).toFloat())
                    drawLine(
                        color = Color(0xFFFFE082).copy(alpha = a),
                        start = c + v * (celdaPx * (0.25f + 0.45f * p)),
                        end = c + v * (celdaPx * (0.40f + 0.85f * p * ef.radio)),
                        strokeWidth = celdaPx * 0.05f
                    )
                }
            }
            JuegoTipoEfecto.IMPACTO -> {
                for (i in 0 until 6) {
                    val ang = i * PI / 3 + 0.4
                    val v = Offset(cos(ang).toFloat(), sin(ang).toFloat())
                    drawLine(
                        color = ef.color.copy(alpha = a),
                        start = c + v * (celdaPx * 0.12f * p),
                        end = c + v * (celdaPx * (0.18f + 0.30f * p)),
                        strokeWidth = celdaPx * 0.05f
                    )
                }
                drawCircle(color = Color.White.copy(alpha = 0.7f * a), radius = celdaPx * 0.14f * (1f - p), center = c)
            }
            JuegoTipoEfecto.DESTELLO -> {
                drawCircle(
                    color = ef.color.copy(alpha = a),
                    radius = celdaPx * (0.2f + 0.7f * ef.radio * p),
                    center = c,
                    style = Stroke(width = celdaPx * 0.07f)
                )
                val largo = celdaPx * (0.25f + 0.55f * ef.radio) * (1f - p * 0.5f)
                drawLine(color = Color.White.copy(alpha = a), start = c - Offset(largo, 0f), end = c + Offset(largo, 0f), strokeWidth = celdaPx * 0.05f)
                drawLine(color = Color.White.copy(alpha = a), start = c - Offset(0f, largo), end = c + Offset(0f, largo), strokeWidth = celdaPx * 0.05f)
                drawCircle(color = ef.color.copy(alpha = 0.35f * a), radius = celdaPx * 0.3f, center = c)
            }
            JuegoTipoEfecto.ADVERTENCIA -> {
                val pulso = 0.5f + 0.5f * sin(p * 18f)
                drawCircle(
                    color = ef.color.copy(alpha = 0.18f * pulso),
                    radius = celdaPx * (ef.radio + 0.5f),
                    center = c
                )
                drawCircle(
                    color = ef.color.copy(alpha = 0.8f * a),
                    radius = celdaPx * (ef.radio + 0.5f),
                    center = c,
                    style = Stroke(width = celdaPx * 0.07f)
                )
            }
        }
    }
}


// ───────────────────────── Piezas del mapa ─────────────────────────

@Composable
internal fun JuegoAlarma(activa: Boolean) {
    val trans = rememberInfiniteTransition(label = "alarma")
    val pulso by trans.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulsoAlarma"
    )
    AnimatedVisibility(
        visible = activa,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(JuegoRojo.copy(alpha = pulso)))
    }
}

@Composable
internal fun JuegoIndicador(activo: Boolean, contenido: @Composable () -> Unit) {
    val alfa by animateFloatAsState(
        targetValue = if (activo) 1f else 0.25f,
        animationSpec = tween(300),
        label = "indicador"
    )
    Box(modifier = Modifier.graphicsLayer { this.alpha = alfa }) {
        contenido()
    }
}

@Composable
internal fun JuegoContador(
    valor: Int,
    sufijo: String,
    prefijo: String = "",
    icono: @Composable () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icono()
        if (prefijo.isNotEmpty()) {
            Text("$prefijo ", color = JuegoTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        } else {
            Spacer(Modifier.width(4.dp))
        }
        AnimatedContent(
            targetState = valor,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { -it } + fadeIn(tween(250))) togetherWith
                            (slideOutVertically { it } + fadeOut(tween(250)))
                } else {
                    (slideInVertically { it } + fadeIn(tween(250))) togetherWith
                            (slideOutVertically { -it } + fadeOut(tween(250)))
                }
            },
            label = "contador"
        ) { v ->
            Text("$v", color = JuegoTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text(sufijo, color = JuegoTextoSuave, fontSize = 13.sp)
    }
}

@Composable
internal fun JuegoMinimapa(estado: JuegoEstadoUi, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0E1A))
    ) {
        val celda = size.width / maxOf(estado.ancho, estado.alto)
        val offX = (size.width - celda * estado.ancho) / 2f
        val offY = (size.height - celda * estado.alto) / 2f
        for (id in estado.explorado) {
            val f = id / estado.ancho
            val c = id % estado.ancho
            val color: Color? = when (estado.mapa[id]) {
                JuegoTile.VACIO -> null
                JuegoTile.PARED, JuegoTile.VENTANA -> Color(0xFF3B4466)
                JuegoTile.PUERTA -> Color(0xFFFFCA28)
                JuegoTile.PUERTA_CERRADA -> Color(0xFFFF5252)
                JuegoTile.ESCOTILLA -> Color(0xFF69F0AE)
                JuegoTile.FLUIDO -> Color(0xFF1E8E75)
                else -> when {
                    estado.muebles[id] == JuegoMueble.BANCO -> Color(0xFF22D3EE)
                    id in estado.muebles -> Color(0xFF4A5486)
                    else -> Color(0xFF6C78B0)
                }
            }
            if (color != null) {
                drawRect(
                    color = color,
                    topLeft = Offset(offX + c * celda, offY + f * celda),
                    size = Size(celda, celda)
                )
            }
        }
        estado.objetos.forEach { o ->
            if (o.activo && o.tipo == JuegoTipoObjeto.BOTIN && estado.idx(o.pos) in estado.explorado) {
                val rz = o.items.maxByOrNull { it.rareza.ordinal }?.rareza ?: JuegoRareza.COMUN
                drawCircle(
                    color = rz.color,
                    radius = celda * 0.8f,
                    center = Offset(offX + (o.pos.col + 0.5f) * celda, offY + (o.pos.fila + 0.5f) * celda)
                )
            }
        }
        estado.enemigos.forEach { en ->
            if (en.vivo && estado.idx(en.pos) in estado.visibles) {
                drawCircle(
                    color = Color(0xFFFF5252),
                    radius = celda * 0.9f,
                    center = Offset(offX + (en.pos.col + 0.5f) * celda, offY + (en.pos.fila + 0.5f) * celda)
                )
            }
        }
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = celda * 1.1f,
            center = Offset(offX + (estado.jugador.col + 0.5f) * celda, offY + (estado.jugador.fila + 0.5f) * celda)
        )
    }
}

@Composable
internal fun JuegoMapa(estado: JuegoEstadoUi, onTocar: (JuegoPos) -> Unit, modifier: Modifier = Modifier) {
    val haptico = LocalHapticFeedback.current
    var destello by remember { mutableStateOf<Set<JuegoPos>>(emptySet()) }
    var sacudida by remember { mutableStateOf(0.dp) }
    val sacudidaAnim by animateDpAsState(sacudida, tween(40), label = "sacudida")
    val tocar by rememberUpdatedState(onTocar)

    LaunchedEffect(estado.golpeContador) {
        if (estado.golpeContador > 0) {
            destello = estado.golpes
            delay(300.milliseconds)
            destello = emptySet()
        }
    }
    LaunchedEffect(estado.golpeContador) {
        if (estado.golpeContador > 0 && estado.golpes.contains(estado.jugador)) {
            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(3) {
                sacudida = 7.dp
                delay(45.milliseconds)
                sacudida = (-7).dp
                delay(45.milliseconds)
            }
            sacudida = 0.dp
        }
    }

    val objetosPorId = remember(estado.objetos, estado.ancho) {
        estado.objetos.filter { it.activo }.associateBy { it.pos.fila * estado.ancho + it.pos.col }
    }
    val trans = rememberInfiniteTransition(label = "mapa")
    val flote by trans.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "floteObjetos"
    )

    BoxWithConstraints(modifier = modifier.offset(x = sacudidaAnim).clipToBounds()) {
        key(estado.piso) {
            val celdaDp = maxWidth / JUEGO_COLUMNAS_VISTA
            val celdaPx = with(LocalDensity.current) { celdaDp.toPx() }
            val filasVista = maxHeight.value / celdaDp.value
            val colsVista = JUEGO_COLUMNAS_VISTA.toFloat()
            val objX = (estado.jugador.col + 0.5f - colsVista / 2f).coerceIn(0f, maxOf(0f, estado.ancho - colsVista))
            val objY = (estado.jugador.fila + 0.5f - filasVista / 2f).coerceIn(0f, maxOf(0f, estado.alto - filasVista))
            val camX = animateFloatAsState(objX, tween(220), label = "camX")
            val camY = animateFloatAsState(objY, tween(220), label = "camY")

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(celdaPx) {
                        detectTapGestures { punto ->
                            val c = floor(punto.x / celdaPx + camX.value).toInt()
                            val f = floor(punto.y / celdaPx + camY.value).toInt()
                            tocar(JuegoPos(f, c))
                        }
                    }
            ) {
                val cx = camX.value
                val cy = camY.value
                val colIni = floor(cx).toInt().coerceAtLeast(0)
                val colFin = ceil(cx + size.width / celdaPx).toInt().coerceAtMost(estado.ancho - 1)
                val filIni = floor(cy).toInt().coerceAtLeast(0)
                val filFin = ceil(cy + size.height / celdaPx).toInt().coerceAtMost(estado.alto - 1)
                for (f in filIni..filFin) {
                    for (c in colIni..colFin) {
                        val id = f * estado.ancho + c
                        if (id !in estado.explorado) continue
                        val visible = id in estado.visibles
                        translate(left = (c - cx) * celdaPx, top = (f - cy) * celdaPx) {
                            juegoDibujarTile(estado.mapa[id], celdaPx, (f + c) % 2 == 0, id)
                            val mueble = estado.muebles[id]
                            if (mueble != null) juegoDibujarMueble(mueble, celdaPx)
                            if (id in estado.trampas) {
                                translate(left = celdaPx * 0.1f, top = celdaPx * 0.1f) {
                                    juegoDibujarSprite(JuegoSprite.TRAMPA, celdaPx * 0.8f)
                                }
                            }
                            val objeto = objetosPorId[id]
                            if (objeto != null) {
                                var spriteObj = objeto.tipo.sprite()
                                var tinteObj = Color.Unspecified
                                if (objeto.tipo == JuegoTipoObjeto.BOTIN) {
                                    val rz = objeto.items.maxByOrNull { it.rareza.ordinal }?.rareza ?: JuegoRareza.COMUN
                                    val centro = Offset(celdaPx / 2f, celdaPx / 2f)
                                    drawCircle(
                                        color = rz.color.copy(alpha = 0.32f + flote * 0.06f),
                                        radius = celdaPx * (0.44f + flote * 0.02f),
                                        center = centro
                                    )
                                    if (rz.ordinal >= JuegoRareza.RARO.ordinal) {
                                        drawCircle(
                                            color = rz.color,
                                            radius = celdaPx * (0.46f + flote * 0.02f),
                                            center = centro,
                                            style = Stroke(width = celdaPx * 0.04f)
                                        )
                                    }
                                    if (rz == JuegoRareza.LEGENDARIO) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.55f),
                                            radius = celdaPx * (0.36f - flote * 0.03f),
                                            center = centro,
                                            style = Stroke(width = celdaPx * 0.025f)
                                        )
                                    }
                                    if (objeto.items.size == 1) {
                                        spriteObj = objeto.items[0].tipo.icono
                                        tinteObj = objeto.items[0].tipo.tinte
                                    }
                                }
                                translate(left = celdaPx * 0.14f, top = celdaPx * 0.14f + flote) {
                                    juegoDibujarSprite(spriteObj, celdaPx * 0.72f, tinteObj)
                                }
                            }
                            if (!visible) {
                                drawRect(color = Color(0xAA060A18), size = Size(celdaPx, celdaPx))
                            }
                        }
                    }
                }
                destello.forEach { p ->
                    drawRect(
                        color = Color(0x66E53935),
                        topLeft = Offset((p.col - cx) * celdaPx, (p.fila - cy) * celdaPx),
                        size = Size(celdaPx, celdaPx)
                    )
                }
            }

            estado.enemigos.forEach { en ->
                key(en.id) {
                    JuegoEnemigoVista(
                        en = en,
                        visible = estado.idx(en.pos) in estado.visibles,
                        celdaDp = celdaDp,
                        celdaPx = celdaPx,
                        camX = camX,
                        camY = camY
                    )
                }
            }

            JuegoJugadorVista(estado.jugador, estado.escudo, celdaDp, celdaPx, camX, camY)

            if (estado.pulsoContador > 0) {
                key(estado.pulsoContador) {
                    JuegoOndaPulso(estado.jugador, celdaPx, camX, camY)
                }
            }

            estado.disparos.forEach { d -> key(d.id) { JuegoDisparoVista(d, celdaPx, camX, camY) } }
            estado.efectos.forEach { ef -> key(ef.id) { JuegoEfectoVista(ef, celdaPx, camX, camY) } }

            estado.popups.forEach { p ->
                key(p.id) { JuegoPopupVista(p, celdaDp, celdaPx, camX, camY) }
            }
        }
    }
}

@Composable
internal fun JuegoEnemigoVista(
    en: JuegoEnemigo,
    visible: Boolean,
    celdaDp: Dp,
    celdaPx: Float,
    camX: State<Float>,
    camY: State<Float>
) {
    val x by animateFloatAsState(en.pos.col.toFloat(), tween(280), label = "enemigoX")
    val y by animateFloatAsState(en.pos.fila.toFloat(), tween(280), label = "enemigoY")
    val fraccionVida = en.vida / en.vidaMax.toFloat()
    val esJefe = en.tipo == JuegoTipoEnemigo.CENTINELA
    val furioso = esJefe && en.vivo && en.vida * 3 <= en.vidaMax * 2
    val tamSprite = if (esJefe) celdaDp * 0.98f else celdaDp * 0.78f
    val trans = rememberInfiniteTransition(label = "auraEnemigo")
    val aura by trans.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "auraJefe"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    ((x - camX.value) * celdaPx).roundToInt(),
                    ((y - camY.value) * celdaPx).roundToInt()
                )
            }
            .size(celdaDp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = en.vivo && visible,
            enter = fadeIn(),
            exit = fadeOut(tween(400)) + scaleOut(targetScale = 0.2f, animationSpec = tween(400))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (furioso) {
                    Box(
                        modifier = Modifier
                            .size(celdaDp * 0.95f)
                            .clip(CircleShape)
                            .background(JuegoRojo.copy(alpha = aura))
                    )
                }
                JuegoSpriteVista(en.tipo.sprite(), Modifier.size(tamSprite))
                val equipoEnemigo = en.equipo
                if (equipoEnemigo != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(equipoEnemigo.rareza.color)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
                if (en.aturdido > 0) {
                    Text("⚡", fontSize = 13.sp, modifier = Modifier.align(Alignment.TopStart))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp)
                        .width(celdaDp * 0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x66000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccionVida.coerceIn(0f, 1f))
                            .background(Color(0xFFEF5350))
                    )
                }
            }
        }
    }
}

@Composable
internal fun JuegoJugadorVista(
    pos: JuegoPos,
    escudo: Boolean,
    celdaDp: Dp,
    celdaPx: Float,
    camX: State<Float>,
    camY: State<Float>
) {
    val x by animateFloatAsState(pos.col.toFloat(), tween(220), label = "jugadorX")
    val y by animateFloatAsState(pos.fila.toFloat(), tween(220), label = "jugadorY")
    val trans = rememberInfiniteTransition(label = "respiracion")
    val escala by trans.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "escalaJugador"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    ((x - camX.value) * celdaPx).roundToInt(),
                    ((y - camY.value) * celdaPx).roundToInt()
                )
            }
            .size(celdaDp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = escudo,
            enter = fadeIn(tween(300)) + scaleIn(animationSpec = tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .size(celdaDp * 0.92f)
                    .clip(CircleShape)
                    .background(JuegoAzul.copy(alpha = 0.35f))
            )
        }
        JuegoSpriteVista(
            JuegoSprite.ASTRONAUTA,
            Modifier
                .size(celdaDp * 0.82f)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
        )
    }
}

@Composable
internal fun JuegoOndaPulso(pos: JuegoPos, celdaPx: Float, camX: State<Float>, camY: State<Float>) {
    val origen = remember { pos }
    var activa by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { activa = true }
    val progreso by animateFloatAsState(
        targetValue = if (activa) 1f else 0f,
        animationSpec = tween(600),
        label = "ondaPulso"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = JuegoAzul.copy(alpha = (1f - progreso).coerceIn(0f, 1f)),
            radius = celdaPx * (JUEGO_RADIO_PULSO + 0.6f) * progreso,
            center = Offset(
                (origen.col + 0.5f - camX.value) * celdaPx,
                (origen.fila + 0.5f - camY.value) * celdaPx
            ),
            style = Stroke(width = celdaPx * 0.12f)
        )
    }
}

@Composable
internal fun JuegoPopupVista(
    popup: JuegoPopup,
    celdaDp: Dp,
    celdaPx: Float,
    camX: State<Float>,
    camY: State<Float>
) {
    var arriba by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { arriba = true }
    val avance by animateFloatAsState(
        targetValue = if (arriba) 1f else 0f,
        animationSpec = tween(900),
        label = "popupY"
    )
    val alfa by animateFloatAsState(
        targetValue = if (arriba) 0f else 1f,
        animationSpec = tween(900),
        label = "popupAlfa"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    ((popup.pos.col - camX.value) * celdaPx).roundToInt(),
                    ((popup.pos.fila - camY.value - 0.8f * avance) * celdaPx).roundToInt()
                )
            }
            .size(celdaDp)
            .graphicsLayer { this.alpha = alfa },
        contentAlignment = Alignment.Center
    ) {
        Text(
            popup.texto,
            color = popup.color,
            fontSize = (celdaDp.value * 0.3f).sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}
