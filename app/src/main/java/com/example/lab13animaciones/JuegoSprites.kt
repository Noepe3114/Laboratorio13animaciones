@file:Suppress("UnnecessaryVariable", "SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun DrawScope.spAstronauta(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.16f, h * 0.40f), size = Size(w * 0.2f, h * 0.36f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = Color(0xFFCFD8DC), topLeft = Offset(w * 0.30f, h * 0.74f), size = Size(w * 0.17f, h * 0.2f), cornerRadius = CornerRadius(w * 0.05f))
    drawRoundRect(color = Color(0xFFCFD8DC), topLeft = Offset(w * 0.53f, h * 0.74f), size = Size(w * 0.17f, h * 0.2f), cornerRadius = CornerRadius(w * 0.05f))
    drawRoundRect(color = Color(0xFFECEFF1), topLeft = Offset(w * 0.28f, h * 0.42f), size = Size(w * 0.44f, h * 0.38f), cornerRadius = CornerRadius(w * 0.12f))
    drawRoundRect(color = Color(0xFFFF7043), topLeft = Offset(w * 0.42f, h * 0.55f), size = Size(w * 0.16f, h * 0.1f), cornerRadius = CornerRadius(w * 0.03f))
    drawCircle(color = Color(0xFFECEFF1), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.3f))
    drawRoundRect(color = Color(0xFF1A237E), topLeft = Offset(w * 0.32f, h * 0.22f), size = Size(w * 0.36f, h * 0.17f), cornerRadius = CornerRadius(w * 0.09f))
    drawCircle(color = Color(0x99FFFFFF), radius = w * 0.04f, center = Offset(w * 0.4f, h * 0.27f))
}

private fun DrawScope.spRobot(w: Float) {
    val h = w
    drawLine(color = Color(0xFFB0BEC5), start = Offset(w * 0.5f, h * 0.07f), end = Offset(w * 0.5f, h * 0.2f), strokeWidth = w * 0.04f)
    drawCircle(color = Color(0xFFFF5252), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.07f))
    drawRoundRect(color = Color(0xFF90A4AE), topLeft = Offset(w * 0.2f, h * 0.2f), size = Size(w * 0.6f, h * 0.34f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.27f, h * 0.28f), size = Size(w * 0.46f, h * 0.18f), cornerRadius = CornerRadius(w * 0.05f))
    drawCircle(color = Color(0xFFFF5252), radius = w * 0.05f, center = Offset(w * 0.4f, h * 0.37f))
    drawCircle(color = Color(0xFFFF5252), radius = w * 0.05f, center = Offset(w * 0.6f, h * 0.37f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.1f, h * 0.58f), size = Size(w * 0.12f, h * 0.24f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.78f, h * 0.58f), size = Size(w * 0.12f, h * 0.24f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF607D8B), topLeft = Offset(w * 0.26f, h * 0.56f), size = Size(w * 0.48f, h * 0.3f), cornerRadius = CornerRadius(w * 0.06f))
    drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.7f))
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.32f, h * 0.86f), size = Size(w * 0.13f, h * 0.1f), cornerRadius = CornerRadius(w * 0.03f))
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.55f, h * 0.86f), size = Size(w * 0.13f, h * 0.1f), cornerRadius = CornerRadius(w * 0.03f))
}

private fun DrawScope.spAlien(w: Float) {
    val h = w
    drawLine(color = Color(0xFF2E7D32), start = Offset(w * 0.32f, h * 0.22f), end = Offset(w * 0.22f, h * 0.06f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF2E7D32), start = Offset(w * 0.68f, h * 0.22f), end = Offset(w * 0.78f, h * 0.06f), strokeWidth = w * 0.04f)
    drawCircle(color = Color(0xFFCDDC39), radius = w * 0.06f, center = Offset(w * 0.22f, h * 0.06f))
    drawCircle(color = Color(0xFFCDDC39), radius = w * 0.06f, center = Offset(w * 0.78f, h * 0.06f))
    drawOval(color = Color(0xFF4CAF50), topLeft = Offset(w * 0.3f, h * 0.68f), size = Size(w * 0.4f, h * 0.28f))
    drawOval(color = Color(0xFF66BB6A), topLeft = Offset(w * 0.12f, h * 0.16f), size = Size(w * 0.76f, h * 0.62f))
    drawOval(color = Color(0xFF1B1B1B), topLeft = Offset(w * 0.24f, h * 0.36f), size = Size(w * 0.22f, h * 0.28f))
    drawOval(color = Color(0xFF1B1B1B), topLeft = Offset(w * 0.54f, h * 0.36f), size = Size(w * 0.22f, h * 0.28f))
    drawCircle(color = Color(0xFFFFFFFF), radius = w * 0.04f, center = Offset(w * 0.32f, h * 0.43f))
    drawCircle(color = Color(0xFFFFFFFF), radius = w * 0.04f, center = Offset(w * 0.62f, h * 0.43f))
    drawLine(color = Color(0xFF1B5E20), start = Offset(w * 0.42f, h * 0.7f), end = Offset(w * 0.58f, h * 0.7f), strokeWidth = w * 0.03f)
}

private fun DrawScope.spDron(w: Float) {
    val h = w
    drawLine(color = Color(0xFFB0BEC5), start = Offset(w * 0.1f, h * 0.2f), end = Offset(w * 0.42f, h * 0.2f), strokeWidth = w * 0.05f)
    drawLine(color = Color(0xFFB0BEC5), start = Offset(w * 0.58f, h * 0.2f), end = Offset(w * 0.9f, h * 0.2f), strokeWidth = w * 0.05f)
    drawLine(color = Color(0xFF78909C), start = Offset(w * 0.3f, h * 0.2f), end = Offset(w * 0.3f, h * 0.36f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF78909C), start = Offset(w * 0.7f, h * 0.2f), end = Offset(w * 0.7f, h * 0.36f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF78909C), start = Offset(w * 0.32f, h * 0.74f), end = Offset(w * 0.24f, h * 0.92f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF78909C), start = Offset(w * 0.68f, h * 0.74f), end = Offset(w * 0.76f, h * 0.92f), strokeWidth = w * 0.04f)
    drawOval(color = Color(0xFF455A64), topLeft = Offset(w * 0.16f, h * 0.3f), size = Size(w * 0.68f, h * 0.46f))
    drawOval(color = Color(0xFF212121), topLeft = Offset(w * 0.3f, h * 0.42f), size = Size(w * 0.4f, h * 0.2f))
    drawCircle(color = Color(0xFFFF1744), radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.52f))
}

private fun DrawScope.spTorreta(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.12f, h * 0.64f), size = Size(w * 0.76f, h * 0.28f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = Color(0xFF546E7A), topLeft = Offset(w * 0.28f, h * 0.52f), size = Size(w * 0.44f, h * 0.2f), cornerRadius = CornerRadius(w * 0.05f))
    drawRect(color = Color(0xFF263238), topLeft = Offset(w * 0.44f, h * 0.04f), size = Size(w * 0.12f, h * 0.34f))
    drawCircle(color = Color(0xFF607D8B), radius = w * 0.24f, center = Offset(w * 0.5f, h * 0.42f))
    drawCircle(color = Color(0xFFFF1744), radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.42f))
    drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.04f, center = Offset(w * 0.3f, h * 0.78f))
    drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.04f, center = Offset(w * 0.7f, h * 0.78f))
}

private fun DrawScope.spCentinela(w: Float) {
    val h = w
    val pinchos = Path().apply {
        moveTo(w * 0.2f, h * 0.32f)
        lineTo(w * 0.28f, h * 0.04f)
        lineTo(w * 0.4f, h * 0.3f)
        lineTo(w * 0.5f, h * 0.0f)
        lineTo(w * 0.6f, h * 0.3f)
        lineTo(w * 0.72f, h * 0.04f)
        lineTo(w * 0.8f, h * 0.32f)
        close()
    }
    drawPath(path = pinchos, color = Color(0xFFB71C1C))
    drawRoundRect(color = Color(0xFF311B92), topLeft = Offset(w * 0.24f, h * 0.84f), size = Size(w * 0.18f, h * 0.14f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF311B92), topLeft = Offset(w * 0.58f, h * 0.84f), size = Size(w * 0.18f, h * 0.14f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF4A148C), topLeft = Offset(w * 0.1f, h * 0.26f), size = Size(w * 0.8f, h * 0.62f), cornerRadius = CornerRadius(w * 0.16f))
    drawCircle(color = Color(0xFF7B1FA2), radius = w * 0.13f, center = Offset(w * 0.12f, h * 0.42f))
    drawCircle(color = Color(0xFF7B1FA2), radius = w * 0.13f, center = Offset(w * 0.88f, h * 0.42f))
    drawCircle(color = Color(0xFFEDE7F6), radius = w * 0.21f, center = Offset(w * 0.5f, h * 0.52f))
    drawCircle(color = Color(0xFFD50000), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.52f))
    drawCircle(color = Color(0xFF000000), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.52f))
    drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.22f, h * 0.78f), end = Offset(w * 0.78f, h * 0.78f), strokeWidth = w * 0.03f)
}

private fun DrawScope.spCaja(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF8D6E63), topLeft = Offset(w * 0.08f, h * 0.08f), size = Size(w * 0.84f, h * 0.84f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF5D4037), topLeft = Offset(w * 0.08f, h * 0.08f), size = Size(w * 0.84f, h * 0.84f), cornerRadius = CornerRadius(w * 0.08f), style = Stroke(width = w * 0.06f))
    drawLine(color = Color(0xFF5D4037), start = Offset(w * 0.12f, h * 0.12f), end = Offset(w * 0.88f, h * 0.88f), strokeWidth = w * 0.05f)
    drawLine(color = Color(0xFF5D4037), start = Offset(w * 0.88f, h * 0.12f), end = Offset(w * 0.12f, h * 0.88f), strokeWidth = w * 0.05f)
}

private fun DrawScope.spTrampa(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.1f, h * 0.1f), size = Size(w * 0.8f, h * 0.8f), cornerRadius = CornerRadius(w * 0.1f))
    for (i in 0 until 3) {
        val x = w * (0.22f + i * 0.22f)
        val pico = Path().apply {
            moveTo(x, h * 0.74f)
            lineTo(x + w * 0.1f, h * 0.3f)
            lineTo(x + w * 0.2f, h * 0.74f)
            close()
        }
        drawPath(path = pico, color = Color(0xFFCFD8DC))
        drawCircle(color = Color(0xFFFF5252), radius = w * 0.025f, center = Offset(x + w * 0.1f, h * 0.32f))
    }
}

private fun DrawScope.spTanque(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.4f, h * 0.08f), size = Size(w * 0.2f, h * 0.16f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF29B6F6), topLeft = Offset(w * 0.28f, h * 0.2f), size = Size(w * 0.44f, h * 0.7f), cornerRadius = CornerRadius(w * 0.14f))
    drawRect(color = Color(0xFF0277BD), topLeft = Offset(w * 0.28f, h * 0.5f), size = Size(w * 0.44f, h * 0.1f))
    drawRoundRect(color = Color(0x88FFFFFF), topLeft = Offset(w * 0.35f, h * 0.28f), size = Size(w * 0.07f, h * 0.45f), cornerRadius = CornerRadius(w * 0.03f))
}

private fun DrawScope.spTarjeta(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFFFFC107), topLeft = Offset(w * 0.1f, h * 0.26f), size = Size(w * 0.8f, h * 0.5f), cornerRadius = CornerRadius(w * 0.08f))
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(w * 0.1f, h * 0.36f), size = Size(w * 0.8f, h * 0.1f))
    drawRoundRect(color = Color(0xFFFFF59D), topLeft = Offset(w * 0.18f, h * 0.54f), size = Size(w * 0.2f, h * 0.14f), cornerRadius = CornerRadius(w * 0.03f))
    drawLine(color = Color(0xFFFF8F00), start = Offset(w * 0.48f, h * 0.6f), end = Offset(w * 0.8f, h * 0.6f), strokeWidth = w * 0.04f)
}

private fun DrawScope.spBotiquin(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.36f, h * 0.14f), size = Size(w * 0.28f, h * 0.16f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFFF5F5F5), topLeft = Offset(w * 0.12f, h * 0.24f), size = Size(w * 0.76f, h * 0.58f), cornerRadius = CornerRadius(w * 0.1f))
    drawRect(color = Color(0xFFE53935), topLeft = Offset(w * 0.44f, h * 0.34f), size = Size(w * 0.12f, h * 0.38f))
    drawRect(color = Color(0xFFE53935), topLeft = Offset(w * 0.31f, h * 0.47f), size = Size(w * 0.38f, h * 0.12f))
}

private fun DrawScope.spEscudo(w: Float) {
    val h = w
    val forma = Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.86f, h * 0.22f)
        lineTo(w * 0.86f, h * 0.5f)
        quadraticTo(w * 0.86f, h * 0.78f, w * 0.5f, h * 0.94f)
        quadraticTo(w * 0.14f, h * 0.78f, w * 0.14f, h * 0.5f)
        lineTo(w * 0.14f, h * 0.22f)
        close()
    }
    drawPath(path = forma, color = Color(0xFF38BDF8))
    drawPath(path = forma, color = Color(0xFFE1F5FE), style = Stroke(width = w * 0.06f))
    drawLine(color = Color(0xFFE1F5FE), start = Offset(w * 0.5f, h * 0.22f), end = Offset(w * 0.5f, h * 0.78f), strokeWidth = w * 0.05f)
}

private fun DrawScope.spCristal(w: Float) {
    val h = w
    val forma = Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.82f, h * 0.38f)
        lineTo(w * 0.5f, h * 0.92f)
        lineTo(w * 0.18f, h * 0.38f)
        close()
    }
    drawPath(path = forma, color = Color(0xFF4DD0E1))
    val brillo = Color(0xAAFFFFFF)
    drawLine(color = brillo, start = Offset(w * 0.18f, h * 0.38f), end = Offset(w * 0.82f, h * 0.38f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.38f, h * 0.38f), end = Offset(w * 0.5f, h * 0.08f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.62f, h * 0.38f), end = Offset(w * 0.5f, h * 0.08f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.38f, h * 0.38f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.62f, h * 0.38f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = w * 0.03f)
}

private fun DrawScope.spModulo(w: Float) {
    val h = w
    val centro = Offset(w * 0.5f, h * 0.5f)
    for (i in 0 until 8) {
        val a = i * PI / 4
        val dx = cos(a).toFloat()
        val dy = sin(a).toFloat()
        drawLine(
            color = Color(0xFFFFB300),
            start = Offset(centro.x + dx * w * 0.28f, centro.y + dy * h * 0.28f),
            end = Offset(centro.x + dx * w * 0.44f, centro.y + dy * h * 0.44f),
            strokeWidth = w * 0.14f
        )
    }
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.32f, center = centro)
    drawCircle(color = Color(0xFF37474F), radius = w * 0.13f, center = centro)
}

private fun DrawScope.spPulso(w: Float) {
    val h = w
    val centro = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = Color(0xFFFFFFFF), radius = w * 0.1f, center = centro)
    drawCircle(color = Color(0xFFFFFFFF), radius = w * 0.25f, center = centro, style = Stroke(width = w * 0.07f))
    drawCircle(color = Color(0xAAFFFFFF), radius = w * 0.4f, center = centro, style = Stroke(width = w * 0.05f))
}

private fun DrawScope.spCorazon(w: Float) {
    val h = w
    drawCircle(color = Color(0xFFE53935), radius = w * 0.24f, center = Offset(w * 0.34f, h * 0.36f))
    drawCircle(color = Color(0xFFE53935), radius = w * 0.24f, center = Offset(w * 0.66f, h * 0.36f))
    val punta = Path().apply {
        moveTo(w * 0.12f, h * 0.46f)
        lineTo(w * 0.88f, h * 0.46f)
        lineTo(w * 0.5f, h * 0.88f)
        close()
    }
    drawPath(path = punta, color = Color(0xFFE53935))
}

private fun DrawScope.spCohete(w: Float) {
    val h = w
    val aletaIzq = Path().apply {
        moveTo(w * 0.3f, h * 0.52f)
        lineTo(w * 0.12f, h * 0.8f)
        lineTo(w * 0.32f, h * 0.74f)
        close()
    }
    val aletaDer = Path().apply {
        moveTo(w * 0.7f, h * 0.52f)
        lineTo(w * 0.88f, h * 0.8f)
        lineTo(w * 0.68f, h * 0.74f)
        close()
    }
    val llama = Path().apply {
        moveTo(w * 0.4f, h * 0.72f)
        lineTo(w * 0.5f, h * 0.97f)
        lineTo(w * 0.6f, h * 0.72f)
        close()
    }
    val cuerpo = Path().apply {
        moveTo(w * 0.5f, h * 0.04f)
        quadraticTo(w * 0.8f, h * 0.3f, w * 0.7f, h * 0.74f)
        lineTo(w * 0.3f, h * 0.74f)
        quadraticTo(w * 0.2f, h * 0.3f, w * 0.5f, h * 0.04f)
        close()
    }
    drawPath(path = llama, color = Color(0xFFFF9800))
    drawPath(path = aletaIzq, color = Color(0xFFE53935))
    drawPath(path = aletaDer, color = Color(0xFFE53935))
    drawPath(path = cuerpo, color = Color(0xFFECEFF1))
    drawCircle(color = Color(0xFF38BDF8), radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.36f))
    drawCircle(color = Color(0xFF37474F), radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.36f), style = Stroke(width = w * 0.03f))
}

private fun DrawScope.spCandado(w: Float) {
    val h = w
    drawCircle(color = Color(0xFFCFD8DC), radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.38f), style = Stroke(width = w * 0.09f))
    drawRoundRect(color = Color(0xFFFFC107), topLeft = Offset(w * 0.2f, h * 0.42f), size = Size(w * 0.6f, h * 0.5f), cornerRadius = CornerRadius(w * 0.1f))
    drawCircle(color = Color(0xFF5D4037), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.64f))
}

private fun DrawScope.spCofre(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF6D4C41), topLeft = Offset(w * 0.1f, h * 0.3f), size = Size(w * 0.8f, h * 0.56f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF8D6E63), topLeft = Offset(w * 0.1f, h * 0.18f), size = Size(w * 0.8f, h * 0.26f), cornerRadius = CornerRadius(w * 0.1f))
    drawRect(color = Color(0xFFFFC107), topLeft = Offset(w * 0.1f, h * 0.42f), size = Size(w * 0.8f, h * 0.06f))
    drawRoundRect(color = Color(0xFFFFD54F), topLeft = Offset(w * 0.42f, h * 0.4f), size = Size(w * 0.16f, h * 0.18f), cornerRadius = CornerRadius(w * 0.03f))
}

// ───────────────────────── Sprites nuevos ─────────────────────────

private fun DrawScope.spPistola(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF546E7A), topLeft = Offset(w * 0.10f, h * 0.30f), size = Size(w * 0.74f, h * 0.22f), cornerRadius = CornerRadius(w * 0.05f))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.20f, h * 0.50f), size = Size(w * 0.20f, h * 0.34f), cornerRadius = CornerRadius(w * 0.04f))
    drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.80f, h * 0.34f), size = Size(w * 0.14f, h * 0.10f))
    drawRect(color = Color(0xFF263238), topLeft = Offset(w * 0.44f, h * 0.52f), size = Size(w * 0.16f, h * 0.05f))
    drawCircle(color = Color(0xFF22D3EE), radius = w * 0.04f, center = Offset(w * 0.30f, h * 0.41f))
}

private fun DrawScope.spSubfusil(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.06f, h * 0.32f), size = Size(w * 0.80f, h * 0.20f), cornerRadius = CornerRadius(w * 0.04f))
    drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.84f, h * 0.36f), size = Size(w * 0.12f, h * 0.08f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.40f, h * 0.50f), size = Size(w * 0.12f, h * 0.36f), cornerRadius = CornerRadius(w * 0.03f))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.14f, h * 0.50f), size = Size(w * 0.14f, h * 0.24f), cornerRadius = CornerRadius(w * 0.03f))
    drawRect(color = Color(0xFFFFB300), topLeft = Offset(w * 0.56f, h * 0.36f), size = Size(w * 0.14f, h * 0.05f))
}

private fun DrawScope.spEscopeta(w: Float) {
    val h = w
    drawRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.30f, h * 0.36f), size = Size(w * 0.66f, h * 0.08f))
    drawRect(color = Color(0xFF546E7A), topLeft = Offset(w * 0.30f, h * 0.46f), size = Size(w * 0.50f, h * 0.07f))
    drawRoundRect(color = Color(0xFF6D4C41), topLeft = Offset(w * 0.04f, h * 0.38f), size = Size(w * 0.30f, h * 0.30f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = Color(0xFF8D6E63), topLeft = Offset(w * 0.46f, h * 0.54f), size = Size(w * 0.22f, h * 0.10f), cornerRadius = CornerRadius(w * 0.03f))
    drawCircle(color = Color(0xFFEF5350), radius = w * 0.035f, center = Offset(w * 0.40f, h * 0.40f))
}

private fun DrawScope.spRifle(w: Float) {
    val h = w
    drawRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.34f, h * 0.42f), size = Size(w * 0.62f, h * 0.07f))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.18f, h * 0.38f), size = Size(w * 0.34f, h * 0.16f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.04f, h * 0.40f), size = Size(w * 0.18f, h * 0.30f), cornerRadius = CornerRadius(w * 0.05f))
    drawRoundRect(color = Color(0xFF212121), topLeft = Offset(w * 0.40f, h * 0.28f), size = Size(w * 0.26f, h * 0.10f), cornerRadius = CornerRadius(w * 0.04f))
    drawCircle(color = Color(0xFF69F0AE), radius = w * 0.03f, center = Offset(w * 0.63f, h * 0.33f))
}

private fun DrawScope.spArmaPlasma(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF4A148C), topLeft = Offset(w * 0.10f, h * 0.32f), size = Size(w * 0.76f, h * 0.24f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF7B1FA2), topLeft = Offset(w * 0.10f, h * 0.32f), size = Size(w * 0.76f, h * 0.10f), cornerRadius = CornerRadius(w * 0.08f))
    drawCircle(color = Color(0xFFE040FB), radius = w * 0.09f, center = Offset(w * 0.42f, h * 0.44f))
    drawCircle(color = Color(0xFFFFFFFF), radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.44f))
    drawRect(color = Color(0xFFE040FB), topLeft = Offset(w * 0.84f, h * 0.38f), size = Size(w * 0.12f, h * 0.08f))
    drawRoundRect(color = Color(0xFF311B92), topLeft = Offset(w * 0.20f, h * 0.54f), size = Size(w * 0.16f, h * 0.28f), cornerRadius = CornerRadius(w * 0.04f))
}

private fun DrawScope.spArmaEm(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.10f, h * 0.32f), size = Size(w * 0.70f, h * 0.24f), cornerRadius = CornerRadius(w * 0.05f))
    for (i in 0 until 4) {
        drawLine(color = Color(0xFF40C4FF), start = Offset(w * (0.30f + i * 0.11f), h * 0.30f), end = Offset(w * (0.30f + i * 0.11f), h * 0.58f), strokeWidth = w * 0.04f)
    }
    drawRect(color = Color(0xFF80D8FF), topLeft = Offset(w * 0.78f, h * 0.38f), size = Size(w * 0.16f, h * 0.08f))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.18f, h * 0.54f), size = Size(w * 0.16f, h * 0.28f), cornerRadius = CornerRadius(w * 0.04f))
    drawCircle(color = Color(0xFF00E5FF), radius = w * 0.04f, center = Offset(w * 0.18f, h * 0.44f))
}

private fun DrawScope.spMunicion(w: Float, color: Color) {
    val h = w
    for (i in 0 until 3) {
        val x = w * (0.16f + i * 0.26f)
        drawRoundRect(color = color, topLeft = Offset(x, h * 0.18f), size = Size(w * 0.20f, h * 0.50f), cornerRadius = CornerRadius(w * 0.10f))
        drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(x, h * 0.62f), size = Size(w * 0.20f, h * 0.20f))
        drawRect(color = Color(0xFF78909C), topLeft = Offset(x, h * 0.76f), size = Size(w * 0.20f, h * 0.06f))
    }
}

private fun DrawScope.spCasco(w: Float) {
    val h = w
    drawCircle(color = Color(0xFFCFD8DC), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.48f))
    drawRoundRect(color = Color(0xFF0D47A1), topLeft = Offset(w * 0.24f, h * 0.36f), size = Size(w * 0.52f, h * 0.26f), cornerRadius = CornerRadius(w * 0.12f))
    drawRoundRect(color = Color(0x8838BDF8), topLeft = Offset(w * 0.30f, h * 0.40f), size = Size(w * 0.20f, h * 0.08f), cornerRadius = CornerRadius(w * 0.04f))
    drawRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.30f, h * 0.80f), size = Size(w * 0.40f, h * 0.10f))
}

private fun DrawScope.spPechera(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.04f, h * 0.14f), size = Size(w * 0.24f, h * 0.30f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.72f, h * 0.14f), size = Size(w * 0.24f, h * 0.30f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.18f, h * 0.12f), size = Size(w * 0.64f, h * 0.76f), cornerRadius = CornerRadius(w * 0.12f))
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.34f, h * 0.28f), size = Size(w * 0.32f, h * 0.30f), cornerRadius = CornerRadius(w * 0.06f))
    drawCircle(color = Color(0xFF22D3EE), radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.43f))
}

private fun DrawScope.spBrazos(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.10f, h * 0.10f), size = Size(w * 0.24f, h * 0.50f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.66f, h * 0.10f), size = Size(w * 0.24f, h * 0.50f), cornerRadius = CornerRadius(w * 0.08f))
    drawCircle(color = Color(0xFF455A64), radius = w * 0.10f, center = Offset(w * 0.22f, h * 0.62f))
    drawCircle(color = Color(0xFF455A64), radius = w * 0.10f, center = Offset(w * 0.78f, h * 0.62f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.12f, h * 0.68f), size = Size(w * 0.20f, h * 0.22f), cornerRadius = CornerRadius(w * 0.05f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.68f, h * 0.68f), size = Size(w * 0.20f, h * 0.22f), cornerRadius = CornerRadius(w * 0.05f))
}

private fun DrawScope.spPiernas(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.20f, h * 0.06f), size = Size(w * 0.24f, h * 0.60f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.56f, h * 0.06f), size = Size(w * 0.24f, h * 0.60f), cornerRadius = CornerRadius(w * 0.08f))
    drawCircle(color = Color(0xFF455A64), radius = w * 0.10f, center = Offset(w * 0.32f, h * 0.40f))
    drawCircle(color = Color(0xFF455A64), radius = w * 0.10f, center = Offset(w * 0.68f, h * 0.40f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.14f, h * 0.68f), size = Size(w * 0.34f, h * 0.20f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.52f, h * 0.68f), size = Size(w * 0.34f, h * 0.20f), cornerRadius = CornerRadius(w * 0.06f))
}

private fun DrawScope.spMochila(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF5D6D7E), topLeft = Offset(w * 0.16f, h * 0.14f), size = Size(w * 0.68f, h * 0.72f), cornerRadius = CornerRadius(w * 0.12f))
    drawRoundRect(color = Color(0xFF29B6F6), topLeft = Offset(w * 0.26f, h * 0.22f), size = Size(w * 0.18f, h * 0.52f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF29B6F6), topLeft = Offset(w * 0.56f, h * 0.22f), size = Size(w * 0.18f, h * 0.52f), cornerRadius = CornerRadius(w * 0.08f))
    drawRect(color = Color(0xFF34495E), topLeft = Offset(w * 0.16f, h * 0.76f), size = Size(w * 0.68f, h * 0.06f))
    drawCircle(color = Color(0xFF69F0AE), radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.30f))
}

private fun DrawScope.spGenerador(w: Float) {
    val h = w
    val hex = Path().apply {
        moveTo(w * 0.5f, h * 0.06f)
        lineTo(w * 0.88f, h * 0.28f)
        lineTo(w * 0.88f, h * 0.72f)
        lineTo(w * 0.5f, h * 0.94f)
        lineTo(w * 0.12f, h * 0.72f)
        lineTo(w * 0.12f, h * 0.28f)
        close()
    }
    drawPath(path = hex, color = Color(0xFF37474F))
    drawPath(path = hex, color = Color(0xFF38BDF8), style = Stroke(width = w * 0.06f))
    drawCircle(color = Color(0xFF0288D1), radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(color = Color(0xFFE1F5FE), radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.5f))
}

private fun DrawScope.spChatarra(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.10f, h * 0.50f), size = Size(w * 0.46f, h * 0.30f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF90A4AE), topLeft = Offset(w * 0.40f, h * 0.22f), size = Size(w * 0.46f, h * 0.26f), cornerRadius = CornerRadius(w * 0.04f))
    drawCircle(color = Color(0xFF546E7A), radius = w * 0.12f, center = Offset(w * 0.30f, h * 0.34f))
    drawCircle(color = Color(0xFF37474F), radius = w * 0.05f, center = Offset(w * 0.30f, h * 0.34f))
    drawCircle(color = Color(0xFFCFD8DC), radius = w * 0.03f, center = Offset(w * 0.66f, h * 0.35f))
    drawCircle(color = Color(0xFFCFD8DC), radius = w * 0.03f, center = Offset(w * 0.24f, h * 0.65f))
    drawRect(color = Color(0xFFA1887F), topLeft = Offset(w * 0.60f, h * 0.58f), size = Size(w * 0.26f, h * 0.14f))
}

private fun DrawScope.spCircuito(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF2E7D32), topLeft = Offset(w * 0.10f, h * 0.14f), size = Size(w * 0.80f, h * 0.72f), cornerRadius = CornerRadius(w * 0.06f))
    drawRect(color = Color(0xFF212121), topLeft = Offset(w * 0.34f, h * 0.34f), size = Size(w * 0.32f, h * 0.30f))
    drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.16f, h * 0.30f), end = Offset(w * 0.34f, h * 0.30f), strokeWidth = w * 0.03f)
    drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.66f, h * 0.50f), end = Offset(w * 0.84f, h * 0.50f), strokeWidth = w * 0.03f)
    drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.16f, h * 0.70f), end = Offset(w * 0.50f, h * 0.70f), strokeWidth = w * 0.03f)
    drawCircle(color = Color(0xFFFF5252), radius = w * 0.04f, center = Offset(w * 0.78f, h * 0.24f))
}

private fun DrawScope.spNucleo(w: Float, color: Color) {
    val h = w
    val c = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = Color(0xFF37474F), radius = w * 0.42f, center = c)
    drawCircle(color = color, radius = w * 0.32f, center = c)
    drawCircle(color = Color(0x88FFFFFF), radius = w * 0.14f, center = Offset(w * 0.42f, h * 0.42f))
    drawCircle(color = Color(0xFFB0BEC5), radius = w * 0.42f, center = c, style = Stroke(width = w * 0.04f))
}

private fun DrawScope.spCelda(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.22f, h * 0.14f), size = Size(w * 0.56f, h * 0.74f), cornerRadius = CornerRadius(w * 0.08f))
    drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(w * 0.38f, h * 0.06f), size = Size(w * 0.24f, h * 0.10f))
    val rayo = Path().apply {
        moveTo(w * 0.54f, h * 0.22f)
        lineTo(w * 0.36f, h * 0.54f)
        lineTo(w * 0.48f, h * 0.54f)
        lineTo(w * 0.42f, h * 0.80f)
        lineTo(w * 0.66f, h * 0.44f)
        lineTo(w * 0.52f, h * 0.44f)
        close()
    }
    drawPath(path = rayo, color = Color(0xFFFFEB3B))
}

private fun DrawScope.spEngranaje(w: Float) {
    val h = w
    val c = Offset(w * 0.5f, h * 0.5f)
    for (i in 0 until 8) {
        val a = i * PI / 4
        drawLine(
            color = Color(0xFF90A4AE),
            start = Offset(c.x + cos(a).toFloat() * w * 0.26f, c.y + sin(a).toFloat() * h * 0.26f),
            end = Offset(c.x + cos(a).toFloat() * w * 0.42f, c.y + sin(a).toFloat() * h * 0.42f),
            strokeWidth = w * 0.13f
        )
    }
    drawCircle(color = Color(0xFF90A4AE), radius = w * 0.32f, center = c)
    drawCircle(color = Color(0xFF263238), radius = w * 0.13f, center = c)
}

private fun DrawScope.spPlaca(w: Float) {
    val h = w
    val forma = Path().apply {
        moveTo(w * 0.22f, h * 0.10f)
        lineTo(w * 0.78f, h * 0.10f)
        lineTo(w * 0.90f, h * 0.30f)
        lineTo(w * 0.90f, h * 0.70f)
        lineTo(w * 0.78f, h * 0.90f)
        lineTo(w * 0.22f, h * 0.90f)
        lineTo(w * 0.10f, h * 0.70f)
        lineTo(w * 0.10f, h * 0.30f)
        close()
    }
    drawPath(path = forma, color = Color(0xFF607D8B))
    drawPath(path = forma, color = Color(0xFFCFD8DC), style = Stroke(width = w * 0.05f))
    drawCircle(color = Color(0xFFECEFF1), radius = w * 0.04f, center = Offset(w * 0.28f, h * 0.24f))
    drawCircle(color = Color(0xFFECEFF1), radius = w * 0.04f, center = Offset(w * 0.72f, h * 0.24f))
    drawCircle(color = Color(0xFFECEFF1), radius = w * 0.04f, center = Offset(w * 0.28f, h * 0.76f))
    drawCircle(color = Color(0xFFECEFF1), radius = w * 0.04f, center = Offset(w * 0.72f, h * 0.76f))
    drawLine(color = Color(0xFFFFB300), start = Offset(w * 0.30f, h * 0.50f), end = Offset(w * 0.70f, h * 0.50f), strokeWidth = w * 0.06f)
}

private fun DrawScope.spTejido(w: Float) {
    val h = w
    drawOval(color = Color(0xFFD81B60), topLeft = Offset(w * 0.12f, h * 0.30f), size = Size(w * 0.56f, h * 0.46f))
    drawOval(color = Color(0xFFEC407A), topLeft = Offset(w * 0.34f, h * 0.20f), size = Size(w * 0.50f, h * 0.44f))
    drawOval(color = Color(0xFFF48FB1), topLeft = Offset(w * 0.46f, h * 0.28f), size = Size(w * 0.22f, h * 0.16f))
    drawCircle(color = Color(0xFF880E4F), radius = w * 0.05f, center = Offset(w * 0.30f, h * 0.52f))
    drawCircle(color = Color(0xFF880E4F), radius = w * 0.035f, center = Offset(w * 0.52f, h * 0.62f))
}

private fun DrawScope.spCristalTinte(w: Float, base: Color) {
    val h = w
    val forma = Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.82f, h * 0.38f)
        lineTo(w * 0.5f, h * 0.92f)
        lineTo(w * 0.18f, h * 0.38f)
        close()
    }
    drawPath(path = forma, color = base)
    val brillo = Color(0xAAFFFFFF)
    drawLine(color = brillo, start = Offset(w * 0.18f, h * 0.38f), end = Offset(w * 0.82f, h * 0.38f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.38f, h * 0.38f), end = Offset(w * 0.5f, h * 0.08f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.62f, h * 0.38f), end = Offset(w * 0.5f, h * 0.08f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.38f, h * 0.38f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = w * 0.03f)
    drawLine(color = brillo, start = Offset(w * 0.62f, h * 0.38f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = w * 0.03f)
}

private fun DrawScope.spModuloChip(w: Float) {
    val h = w
    for (i in 0 until 4) {
        val t = h * (0.22f + i * 0.16f)
        drawLine(color = Color(0xFFFFC107), start = Offset(w * 0.06f, t), end = Offset(w * 0.22f, t), strokeWidth = w * 0.05f)
        drawLine(color = Color(0xFFFFC107), start = Offset(w * 0.78f, t), end = Offset(w * 0.94f, t), strokeWidth = w * 0.05f)
    }
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.18f, h * 0.14f), size = Size(w * 0.64f, h * 0.72f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFF22D3EE), topLeft = Offset(w * 0.18f, h * 0.14f), size = Size(w * 0.64f, h * 0.72f), cornerRadius = CornerRadius(w * 0.08f), style = Stroke(width = w * 0.04f))
    drawCircle(color = Color(0xFF22D3EE), radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(color = Color(0xFFE0F7FA), radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.5f))
}

private fun DrawScope.spBolsa(w: Float) {
    val h = w
    drawOval(color = Color(0xFF455A64), topLeft = Offset(w * 0.14f, h * 0.30f), size = Size(w * 0.72f, h * 0.60f))
    drawOval(color = Color(0xFF607D8B), topLeft = Offset(w * 0.22f, h * 0.36f), size = Size(w * 0.30f, h * 0.20f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.36f, h * 0.18f), size = Size(w * 0.28f, h * 0.16f), cornerRadius = CornerRadius(w * 0.05f))
    drawLine(color = Color(0xFF22D3EE), start = Offset(w * 0.30f, h * 0.62f), end = Offset(w * 0.70f, h * 0.62f), strokeWidth = w * 0.04f)
    drawCircle(color = Color(0xFFFFD54F), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.74f))
}

private fun DrawScope.spRobotCombate(w: Float) {
    spRobot(w)
    val h = w
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.64f, h * 0.54f), size = Size(w * 0.34f, h * 0.10f), cornerRadius = CornerRadius(w * 0.03f))
    drawRect(color = Color(0xFFFF1744), topLeft = Offset(w * 0.94f, h * 0.56f), size = Size(w * 0.05f, h * 0.06f))
    drawRect(color = Color(0xFFFF5252), topLeft = Offset(w * 0.30f, h * 0.15f), size = Size(w * 0.40f, h * 0.04f))
}

private fun DrawScope.spRobotPesado(w: Float) {
    val h = w
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.30f, h * 0.84f), size = Size(w * 0.16f, h * 0.14f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.54f, h * 0.84f), size = Size(w * 0.16f, h * 0.14f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.04f, h * 0.30f), size = Size(w * 0.92f, h * 0.56f), cornerRadius = CornerRadius(w * 0.12f))
    drawRoundRect(color = Color(0xFF607D8B), topLeft = Offset(w * 0.12f, h * 0.36f), size = Size(w * 0.76f, h * 0.20f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.30f, h * 0.10f), size = Size(w * 0.40f, h * 0.24f), cornerRadius = CornerRadius(w * 0.06f))
    drawRect(color = Color(0xFFFF1744), topLeft = Offset(w * 0.36f, h * 0.18f), size = Size(w * 0.28f, h * 0.06f))
    drawCircle(color = Color(0xFFFFB300), radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.68f))
    drawRect(color = Color(0xFFFFB300), topLeft = Offset(w * 0.08f, h * 0.74f), size = Size(w * 0.84f, h * 0.04f))
}

private fun DrawScope.spDronAtaque(w: Float) {
    spDron(w)
    val h = w
    drawRect(color = Color(0xFFFF1744), topLeft = Offset(w * 0.20f, h * 0.34f), size = Size(w * 0.60f, h * 0.05f))
    drawRect(color = Color(0xFF212121), topLeft = Offset(w * 0.44f, h * 0.76f), size = Size(w * 0.12f, h * 0.20f))
    drawCircle(color = Color(0xFFE040FB), radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.97f))
}

private fun DrawScope.spGuardia(w: Float) {
    spAstronauta(w)
    val h = w
    drawRoundRect(color = Color(0xFFD84315), topLeft = Offset(w * 0.30f, h * 0.20f), size = Size(w * 0.40f, h * 0.20f), cornerRadius = CornerRadius(w * 0.09f))
    drawRect(color = Color(0xFFFF8A65), topLeft = Offset(w * 0.34f, h * 0.27f), size = Size(w * 0.32f, h * 0.05f))
    drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.66f, h * 0.52f), size = Size(w * 0.30f, h * 0.10f), cornerRadius = CornerRadius(w * 0.03f))
    drawRect(color = Color(0xFFD84315), topLeft = Offset(w * 0.40f, h * 0.60f), size = Size(w * 0.20f, h * 0.06f))
}

private fun DrawScope.spBlindado(w: Float) {
    spRobot(w)
    val h = w
    drawRoundRect(color = Color(0xCC546E7A), topLeft = Offset(w * 0.06f, h * 0.18f), size = Size(w * 0.88f, h * 0.40f), cornerRadius = CornerRadius(w * 0.08f))
    drawRoundRect(color = Color(0xFFFFB300), topLeft = Offset(w * 0.06f, h * 0.18f), size = Size(w * 0.88f, h * 0.40f), cornerRadius = CornerRadius(w * 0.08f), style = Stroke(width = w * 0.04f))
    drawRect(color = Color(0xFFFF1744), topLeft = Offset(w * 0.28f, h * 0.30f), size = Size(w * 0.44f, h * 0.07f))
    drawRoundRect(color = Color(0xFF607D8B), topLeft = Offset(w * 0.20f, h * 0.60f), size = Size(w * 0.60f, h * 0.26f), cornerRadius = CornerRadius(w * 0.06f))
    drawRect(color = Color(0xFFFFB300), topLeft = Offset(w * 0.20f, h * 0.70f), size = Size(w * 0.60f, h * 0.03f))
}

// ───────────────────────── Despachador ─────────────────────────

internal fun DrawScope.juegoDibujarSprite(s: JuegoSprite, w: Float, tinte: Color = Color.Unspecified) {
    when (s) {
        JuegoSprite.ASTRONAUTA -> spAstronauta(w)
        JuegoSprite.ROBOT -> spRobot(w)
        JuegoSprite.ALIEN -> spAlien(w)
        JuegoSprite.DRON -> spDron(w)
        JuegoSprite.CENTINELA -> spCentinela(w)
        JuegoSprite.TORRETA -> spTorreta(w)
        JuegoSprite.CAJA -> spCaja(w)
        JuegoSprite.TRAMPA -> spTrampa(w)
        JuegoSprite.TANQUE -> spTanque(w)
        JuegoSprite.TARJETA -> spTarjeta(w)
        JuegoSprite.BOTIQUIN -> spBotiquin(w)
        JuegoSprite.ESCUDO -> spEscudo(w)
        JuegoSprite.CRISTAL -> if (tinte == Color.Unspecified) spCristal(w) else spCristalTinte(w, tinte)
        JuegoSprite.MODULO -> spModulo(w)
        JuegoSprite.PULSO -> spPulso(w)
        JuegoSprite.CORAZON -> spCorazon(w)
        JuegoSprite.COHETE -> spCohete(w)
        JuegoSprite.CANDADO -> spCandado(w)
        JuegoSprite.COFRE -> spCofre(w)
        JuegoSprite.ARMA_PISTOLA -> spPistola(w)
        JuegoSprite.ARMA_SUBFUSIL -> spSubfusil(w)
        JuegoSprite.ARMA_ESCOPETA -> spEscopeta(w)
        JuegoSprite.ARMA_RIFLE -> spRifle(w)
        JuegoSprite.ARMA_PLASMA -> spArmaPlasma(w)
        JuegoSprite.ARMA_EM -> spArmaEm(w)
        JuegoSprite.MUNICION -> spMunicion(w, if (tinte == Color.Unspecified) Color(0xFFFFB74D) else tinte)
        JuegoSprite.CASCO -> spCasco(w)
        JuegoSprite.PECHERA -> spPechera(w)
        JuegoSprite.BRAZOS -> spBrazos(w)
        JuegoSprite.PIERNAS -> spPiernas(w)
        JuegoSprite.MOCHILA -> spMochila(w)
        JuegoSprite.GENERADOR -> spGenerador(w)
        JuegoSprite.CHATARRA -> spChatarra(w)
        JuegoSprite.CIRCUITO -> spCircuito(w)
        JuegoSprite.NUCLEO -> spNucleo(w, if (tinte == Color.Unspecified) Color(0xFFFF7043) else tinte)
        JuegoSprite.CELDA -> spCelda(w)
        JuegoSprite.ENGRANAJE -> spEngranaje(w)
        JuegoSprite.PLACA -> spPlaca(w)
        JuegoSprite.TEJIDO -> spTejido(w)
        JuegoSprite.MODULO_CHIP -> spModuloChip(w)
        JuegoSprite.BOLSA -> spBolsa(w)
        JuegoSprite.ROBOT_COMBATE -> spRobotCombate(w)
        JuegoSprite.ROBOT_PESADO -> spRobotPesado(w)
        JuegoSprite.DRON_ATAQUE -> spDronAtaque(w)
        JuegoSprite.GUARDIA -> spGuardia(w)
        JuegoSprite.BLINDADO -> spBlindado(w)
    }
}

internal fun JuegoTipoObjeto.sprite(): JuegoSprite = when (this) {
    JuegoTipoObjeto.TANQUE -> JuegoSprite.TANQUE
    JuegoTipoObjeto.TARJETA -> JuegoSprite.TARJETA
    JuegoTipoObjeto.BOTIQUIN -> JuegoSprite.BOTIQUIN
    JuegoTipoObjeto.ESCUDO -> JuegoSprite.ESCUDO
    JuegoTipoObjeto.CRISTAL -> JuegoSprite.CRISTAL
    JuegoTipoObjeto.MODULO -> JuegoSprite.MODULO
    JuegoTipoObjeto.COFRE -> JuegoSprite.COFRE
    JuegoTipoObjeto.BOTIN -> JuegoSprite.BOLSA
}

internal fun JuegoTipoEnemigo.sprite(): JuegoSprite = when (this) {
    JuegoTipoEnemigo.ROBOT -> JuegoSprite.ROBOT
    JuegoTipoEnemigo.ALIEN -> JuegoSprite.ALIEN
    JuegoTipoEnemigo.DRON -> JuegoSprite.DRON
    JuegoTipoEnemigo.TORRETA -> JuegoSprite.TORRETA
    JuegoTipoEnemigo.ROBOT_COMBATE -> JuegoSprite.ROBOT_COMBATE
    JuegoTipoEnemigo.ROBOT_PESADO -> JuegoSprite.ROBOT_PESADO
    JuegoTipoEnemigo.DRON_ATAQUE -> JuegoSprite.DRON_ATAQUE
    JuegoTipoEnemigo.GUARDIA -> JuegoSprite.GUARDIA
    JuegoTipoEnemigo.BLINDADO -> JuegoSprite.BLINDADO
    JuegoTipoEnemigo.CENTINELA -> JuegoSprite.CENTINELA
}

internal fun JuegoMejora.sprite(): JuegoSprite = when (this) {
    JuegoMejora.BLINDAJE -> JuegoSprite.CORAZON
    JuegoMejora.ARMAS -> JuegoSprite.MODULO
    JuegoMejora.TANQUE -> JuegoSprite.TANQUE
    JuegoMejora.ESCUDO -> JuegoSprite.ESCUDO
    JuegoMejora.PULSO -> JuegoSprite.PULSO
    JuegoMejora.PRECISION -> JuegoSprite.CRISTAL
}

@Composable
internal fun JuegoSpriteVista(s: JuegoSprite, modifier: Modifier = Modifier, tinte: Color = Color.Unspecified) {
    Canvas(modifier = modifier) { juegoDibujarSprite(s, size.width, tinte) }
}


internal fun juegoColorSuelo(t: JuegoTile, par: Boolean): Color {
    val colores = when (t) {
        JuegoTile.SUELO_ENTRADA -> Pair(Color(0xFF2B4A52), Color(0xFF31555E))
        JuegoTile.SUELO_OFICINA -> Pair(Color(0xFF2D3A63), Color(0xFF34436F))
        JuegoTile.SUELO_LAB -> Pair(Color(0xFF2C5448), Color(0xFF33604F))
        JuegoTile.SUELO_REACTOR -> Pair(Color(0xFF4D3426), Color(0xFF583C2B))
        JuegoTile.SUELO_ENFERMERIA -> Pair(Color(0xFF40505A), Color(0xFF485A66))
        JuegoTile.SUELO_ALMACEN -> Pair(Color(0xFF3E382F), Color(0xFF473F34))
        JuegoTile.SUELO_DORM -> Pair(Color(0xFF40305E), Color(0xFF493868))
        JuegoTile.SUELO_COMEDOR -> Pair(Color(0xFF2F4F5F), Color(0xFF365A6B))
        JuegoTile.SUELO_MANDO -> Pair(Color(0xFF4A2B45), Color(0xFF55314F))
        JuegoTile.FLUIDO -> Pair(Color(0xFF1E6F5C), Color(0xFF237A66))
        else -> Pair(Color(0xFF2A3050), Color(0xFF2F3556))
    }
    return if (par) colores.first else colores.second
}

internal fun DrawScope.juegoDibujarPared(lado: Float) {
    drawRect(color = Color(0xFF3B4466), size = Size(lado, lado))
    drawRect(color = Color(0xFF515C86), topLeft = Offset(0f, 0f), size = Size(lado, lado * 0.12f))
    val linea = Color(0x33000000)
    drawLine(color = linea, start = Offset(0f, lado * 0.5f), end = Offset(lado, lado * 0.5f), strokeWidth = 1.5f)
    drawLine(color = linea, start = Offset(lado * 0.5f, lado * 0.12f), end = Offset(lado * 0.5f, lado * 0.5f), strokeWidth = 1.5f)
    drawLine(color = linea, start = Offset(lado * 0.25f, lado * 0.5f), end = Offset(lado * 0.25f, lado), strokeWidth = 1.5f)
    drawLine(color = linea, start = Offset(lado * 0.75f, lado * 0.5f), end = Offset(lado * 0.75f, lado), strokeWidth = 1.5f)
}

internal fun DrawScope.juegoDibujarVentana(lado: Float, semilla: Int) {
    drawRect(color = Color(0xFF3B4466), size = Size(lado, lado))
    drawRoundRect(color = Color(0xFF0B1230), topLeft = Offset(lado * 0.14f, lado * 0.2f), size = Size(lado * 0.72f, lado * 0.6f), cornerRadius = CornerRadius(lado * 0.08f))
    val s = semilla % 5
    drawCircle(color = Color(0xFFFFFFFF), radius = lado * 0.03f, center = Offset(lado * (0.28f + s * 0.1f), lado * 0.36f))
    drawCircle(color = Color(0xFFB3E5FC), radius = lado * 0.02f, center = Offset(lado * (0.7f - s * 0.06f), lado * 0.55f))
    drawCircle(color = Color(0xFFFFF59D), radius = lado * 0.025f, center = Offset(lado * 0.45f, lado * (0.62f - s * 0.02f)))
    drawRoundRect(color = Color(0xFF8D99C4), topLeft = Offset(lado * 0.14f, lado * 0.2f), size = Size(lado * 0.72f, lado * 0.6f), cornerRadius = CornerRadius(lado * 0.08f), style = Stroke(width = lado * 0.05f))
}

internal fun DrawScope.juegoDibujarPuerta(lado: Float, cerrada: Boolean) {
    drawRect(color = juegoColorSuelo(JuegoTile.PASILLO, true), size = Size(lado, lado))
    drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(lado * 0.08f, lado * 0.04f), size = Size(lado * 0.84f, lado * 0.92f), cornerRadius = CornerRadius(lado * 0.08f))
    drawRoundRect(color = Color(0xFF607D8B), topLeft = Offset(lado * 0.18f, lado * 0.12f), size = Size(lado * 0.64f, lado * 0.76f), cornerRadius = CornerRadius(lado * 0.05f))
    drawLine(color = Color(0xFF37474F), start = Offset(lado * 0.5f, lado * 0.12f), end = Offset(lado * 0.5f, lado * 0.88f), strokeWidth = lado * 0.04f)
    drawCircle(color = if (cerrada) Color(0xFFFF5252) else Color(0xFF69F0AE), radius = lado * 0.06f, center = Offset(lado * 0.5f, lado * 0.2f))
    if (cerrada) {
        translate(left = lado * 0.3f, top = lado * 0.4f) { spCandado(lado * 0.4f) }
    }
}

internal fun DrawScope.juegoDibujarEscotilla(lado: Float) {
    drawRect(color = juegoColorSuelo(JuegoTile.SUELO_MANDO, true), size = Size(lado, lado))
    val c = Offset(lado / 2f, lado / 2f)
    drawCircle(color = Color(0xFF37474F), radius = lado * 0.44f, center = c)
    drawCircle(color = Color(0xFF69F0AE), radius = lado * 0.36f, center = c, style = Stroke(width = lado * 0.05f))
    drawCircle(color = Color(0xFF1B5E20), radius = lado * 0.28f, center = c)
    drawLine(color = Color(0xFF69F0AE), start = Offset(lado * 0.3f, lado * 0.5f), end = Offset(lado * 0.7f, lado * 0.5f), strokeWidth = lado * 0.04f)
    drawLine(color = Color(0xFF69F0AE), start = Offset(lado * 0.5f, lado * 0.3f), end = Offset(lado * 0.5f, lado * 0.7f), strokeWidth = lado * 0.04f)
}

internal fun DrawScope.juegoDibujarTile(t: JuegoTile, lado: Float, par: Boolean, semilla: Int) {
    when (t) {
        JuegoTile.VACIO -> {}
        JuegoTile.PARED -> juegoDibujarPared(lado)
        JuegoTile.VENTANA -> juegoDibujarVentana(lado, semilla)
        JuegoTile.PUERTA -> juegoDibujarPuerta(lado, false)
        JuegoTile.PUERTA_CERRADA -> juegoDibujarPuerta(lado, true)
        JuegoTile.ESCOTILLA -> juegoDibujarEscotilla(lado)
        else -> {
            drawRect(color = juegoColorSuelo(t, par), size = Size(lado, lado))
            drawRect(color = Color(0x22000000), size = Size(lado, lado), style = Stroke(width = 1f))
            if (t == JuegoTile.FLUIDO) {
                drawLine(color = Color(0xFF2FA88A), start = Offset(lado * 0.15f, lado * 0.4f), end = Offset(lado * 0.85f, lado * 0.4f), strokeWidth = lado * 0.05f)
                drawLine(color = Color(0xFF2FA88A), start = Offset(lado * 0.25f, lado * 0.65f), end = Offset(lado * 0.75f, lado * 0.65f), strokeWidth = lado * 0.05f)
            }
        }
    }
}

internal fun DrawScope.juegoDibujarMueble(m: JuegoMueble, lado: Float) {
    val w = lado
    val h = lado
    when (m) {
        JuegoMueble.ESCRITORIO -> {
            drawRoundRect(color = Color(0xFF6D4C41), topLeft = Offset(w * 0.08f, h * 0.42f), size = Size(w * 0.84f, h * 0.42f), cornerRadius = CornerRadius(w * 0.06f))
            drawRect(color = Color(0xFF263238), topLeft = Offset(w * 0.3f, h * 0.12f), size = Size(w * 0.4f, h * 0.3f))
            drawRect(color = Color(0xFF4FC3F7), topLeft = Offset(w * 0.34f, h * 0.16f), size = Size(w * 0.32f, h * 0.22f))
            drawRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.46f, h * 0.42f), size = Size(w * 0.08f, h * 0.06f))
        }
        JuegoMueble.CONSOLA -> {
            drawRoundRect(color = Color(0xFF263238), topLeft = Offset(w * 0.06f, h * 0.2f), size = Size(w * 0.88f, h * 0.66f), cornerRadius = CornerRadius(w * 0.08f))
            drawRect(color = Color(0xFF00E676), topLeft = Offset(w * 0.16f, h * 0.28f), size = Size(w * 0.68f, h * 0.22f))
            drawCircle(color = Color(0xFFFF5252), radius = w * 0.05f, center = Offset(w * 0.25f, h * 0.68f))
            drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.68f))
            drawCircle(color = Color(0xFF40C4FF), radius = w * 0.05f, center = Offset(w * 0.75f, h * 0.68f))
        }
        JuegoMueble.MESA -> {
            drawRoundRect(color = Color(0xFF90A4AE), topLeft = Offset(w * 0.04f, h * 0.3f), size = Size(w * 0.92f, h * 0.5f), cornerRadius = CornerRadius(w * 0.08f))
            drawRoundRect(color = Color(0xFFCFD8DC), topLeft = Offset(w * 0.04f, h * 0.3f), size = Size(w * 0.92f, h * 0.18f), cornerRadius = CornerRadius(w * 0.08f))
            drawCircle(color = Color(0xFF69F0AE), radius = w * 0.09f, center = Offset(w * 0.3f, h * 0.45f))
            drawRect(color = Color(0xFFFF8A65), topLeft = Offset(w * 0.6f, h * 0.32f), size = Size(w * 0.1f, h * 0.2f))
        }
        JuegoMueble.CAMA -> {
            drawRoundRect(color = Color(0xFF90A4AE), topLeft = Offset(w * 0.1f, h * 0.1f), size = Size(w * 0.8f, h * 0.8f), cornerRadius = CornerRadius(w * 0.08f))
            drawRoundRect(color = Color(0xFFFFFFFF), topLeft = Offset(w * 0.18f, h * 0.16f), size = Size(w * 0.64f, h * 0.2f), cornerRadius = CornerRadius(w * 0.06f))
            drawRoundRect(color = Color(0xFF5C6BC0), topLeft = Offset(w * 0.14f, h * 0.42f), size = Size(w * 0.72f, h * 0.42f), cornerRadius = CornerRadius(w * 0.06f))
        }
        JuegoMueble.NUCLEO -> {
            drawCircle(color = Color(0xFF455A64), radius = w * 0.46f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = Color(0xFFFF7043), radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.5f))
        }
        JuegoMueble.CAJAS -> spCaja(w)
        JuegoMueble.BANCO -> {
            drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(w * 0.04f, h * 0.35f), size = Size(w * 0.92f, h * 0.55f), cornerRadius = CornerRadius(w * 0.08f))
            drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(w * 0.04f, h * 0.35f), size = Size(w * 0.92f, h * 0.16f), cornerRadius = CornerRadius(w * 0.08f))
            drawRect(color = Color(0xFF22D3EE), topLeft = Offset(w * 0.14f, h * 0.06f), size = Size(w * 0.42f, h * 0.28f))
            drawRect(color = Color(0xFF0B1230), topLeft = Offset(w * 0.18f, h * 0.1f), size = Size(w * 0.34f, h * 0.2f))
            drawLine(color = Color(0xFF22D3EE), start = Offset(w * 0.22f, h * 0.2f), end = Offset(w * 0.46f, h * 0.2f), strokeWidth = w * 0.03f)
            drawLine(color = Color(0xFFFFB300), start = Offset(w * 0.64f, h * 0.18f), end = Offset(w * 0.86f, h * 0.38f), strokeWidth = w * 0.06f)
            drawCircle(color = Color(0xFFFF7043), radius = w * 0.06f, center = Offset(w * 0.3f, h * 0.7f))
            drawCircle(color = Color(0xFF69F0AE), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.7f))
            drawRect(color = Color(0xFFCFD8DC), topLeft = Offset(w * 0.68f, h * 0.6f), size = Size(w * 0.2f, h * 0.1f))
        }
        JuegoMueble.TUBO -> {
            drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(w * 0.2f, h * 0.06f), size = Size(w * 0.6f, h * 0.88f), cornerRadius = CornerRadius(w * 0.12f))
            drawRoundRect(color = Color(0xCC4DD0E1), topLeft = Offset(w * 0.28f, h * 0.14f), size = Size(w * 0.44f, h * 0.72f), cornerRadius = CornerRadius(w * 0.1f))
            drawCircle(color = Color(0x88FFFFFF), radius = w * 0.05f, center = Offset(w * 0.45f, h * 0.6f))
            drawCircle(color = Color(0x88FFFFFF), radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.4f))
        }
    }
}
