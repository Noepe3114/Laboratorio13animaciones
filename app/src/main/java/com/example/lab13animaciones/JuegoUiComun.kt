@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal val JuegoCorte = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)

/** Panel de cristal oscuro con borde de neón y esquinas cortadas. */
internal fun Modifier.juegoPanel(color: Color = JuegoNeon, forma: Shape = JuegoCorte): Modifier =
    this
        .clip(forma)
        .background(Brush.verticalGradient(listOf(Color(0xFF17224A), Color(0xFF0D1430))))
        .border(1.dp, color.copy(alpha = 0.55f), forma)

@Composable
internal fun JuegoBoton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = JuegoNeon,
    grande: Boolean = false
) {
    val alfa by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = tween(200),
        label = "alfaBoton"
    )
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alfa }
            .clip(JuegoCorte)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.14f))))
            .border(1.5.dp, color.copy(alpha = 0.9f), JuegoCorte)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = if (grande) 24.dp else 10.dp, vertical = if (grande) 12.dp else 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = JuegoTexto,
            fontSize = if (grande) 17.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
internal fun JuegoChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    val color = if (activo) JuegoNeon else Color(0xFF39406A)
    Box(
        modifier = Modifier
            .clip(JuegoCorte)
            .background(if (activo) JuegoNeon.copy(alpha = 0.25f) else Color(0x22FFFFFF))
            .border(1.dp, color, JuegoCorte)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            texto,
            color = if (activo) JuegoTexto else JuegoTextoSuave,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/** Icono de objeto con el color de su rareza; épicos y legendarios brillan. */
@Composable
internal fun JuegoIconoItem(
    tipo: JuegoTipoItem,
    rareza: JuegoRareza,
    tam: Dp,
    modifier: Modifier = Modifier,
    cantidad: Int = 1,
    seleccionado: Boolean = false
) {
    val trans = rememberInfiniteTransition(label = "brilloItem")
    val pulso by trans.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulsoItem"
    )
    val brilla = rareza.ordinal >= JuegoRareza.EPICO.ordinal
    val fuerza = if (rareza == JuegoRareza.LEGENDARIO) 0.75f else 0.35f
    val colorBorde = if (brilla) lerp(rareza.color, Color.White, pulso * fuerza) else rareza.color
    val forma = CutCornerShape(topStart = tam / 6, bottomEnd = tam / 6)
    Box(
        modifier = modifier
            .size(tam)
            .clip(forma)
            .background(Brush.verticalGradient(listOf(rareza.color.copy(alpha = 0.30f), Color(0xFF0D1430))))
            .border(
                width = if (seleccionado) 2.5.dp else 1.5.dp,
                color = if (seleccionado) Color.White else colorBorde,
                shape = forma
            )
    ) {
        JuegoSpriteVista(tipo.icono, Modifier.align(Alignment.Center).size(tam * 0.7f), tipo.tinte)
        if (cantidad > 1) {
            Text(
                "$cantidad",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 1.dp)
            )
        }
    }
}

/** Marcador vacío para las ranuras de equipo sin objeto. */
@Composable
internal fun JuegoRanuraVacia(tam: Dp) {
    val forma = CutCornerShape(topStart = tam / 6, bottomEnd = tam / 6)
    Box(
        modifier = Modifier
            .size(tam)
            .clip(forma)
            .background(Color(0x15FFFFFF))
            .border(1.dp, Color(0xFF39406A), forma)
    )
}

@Composable
internal fun JuegoBarraEstado(
    icono: JuegoSprite,
    fraccion: Float,
    color: Color,
    texto: String,
    modifier: Modifier = Modifier,
    alto: Dp = 14.dp
) {
    val f by animateFloatAsState(
        targetValue = fraccion.coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "barraEstado"
    )
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        JuegoSpriteVista(icono, Modifier.size(alto + 8.dp))
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(alto)
                .clip(RoundedCornerShape(alto / 2))
                .background(Color(0xFF1B2447))
                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(alto / 2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(f)
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.65f), color)))
            )
            Text(
                texto,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ───────────────────────── Notificaciones ─────────────────────────

@Composable
internal fun JuegoAvisos(avisos: List<JuegoAviso>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        avisos.forEach { a -> key(a.id) { JuegoAvisoVista(a) } }
    }
}

@Composable
private fun JuegoAvisoVista(a: JuegoAviso) {
    val estado = remember { MutableTransitionState(false).apply { targetState = true } }
    LaunchedEffect(Unit) {
        delay(2600.milliseconds)
        estado.targetState = false
    }
    AnimatedVisibility(
        visibleState = estado,
        enter = slideInVertically { -it } + fadeIn(tween(250)),
        exit = fadeOut(tween(450))
    ) {
        Row(
            modifier = Modifier
                .juegoPanel(a.color)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JuegoSpriteVista(a.icono, Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(a.texto, color = a.color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        }
    }
}
