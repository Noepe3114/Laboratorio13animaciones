@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pantalla raíz del juego "Estación Espacial Z".
 * Conserva el estado de la partida (inventario y equipo incluidos) entre pisos.
 */
@Composable
fun EjercicioFinal() {
    val contexto = LocalContext.current
    val prefs = remember { contexto.getSharedPreferences("estacion_z", Context.MODE_PRIVATE) }
    var dificultad by remember { mutableStateOf(JuegoDificultad.NORMAL) }
    var estado by remember { mutableStateOf(juegoNuevo(JuegoDificultad.NORMAL)) }
    var mejores by remember {
        mutableStateOf(JuegoDificultad.entries.associateWith { prefs.getInt("mejor_${it.name}", 0) })
    }
    var nuevoRecord by remember { mutableStateOf(false) }
    var pausa by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf<JuegoPanelTipo?>(null) }
    var ruta by remember { mutableStateOf<List<JuegoPos>>(emptyList()) }
    var ultimaTecla by remember { mutableLongStateOf(0L) }
    val enfoque = remember { FocusRequester() }

    LaunchedEffect(estado.fase) {
        runCatching { enfoque.requestFocus() }
        pausa = false
        panel = null
        when (estado.fase) {
            JuegoFase.CAMBIO_PISO -> {
                delay(2200.milliseconds)
                estado = juegoSiguientePiso(estado)
            }
            JuegoFase.VICTORIA, JuegoFase.GAME_OVER -> {
                val previo = mejores[estado.dificultad] ?: 0
                nuevoRecord = estado.puntos > previo
                if (nuevoRecord) {
                    mejores = mejores + (estado.dificultad to estado.puntos)
                    prefs.edit().putInt("mejor_${estado.dificultad.name}", estado.puntos).apply()
                }
            }
            else -> {}
        }
    }

    // Tocar el banco de trabajo abre la estación de fabricación.
    LaunchedEffect(estado.abrirBanco) {
        if (estado.abrirBanco) {
            ruta = emptyList()
            estado = estado.copy(abrirBanco = false)
            panel = JuegoPanelTipo.BANCO
        }
    }

    LaunchedEffect(ruta) {
        if (ruta.isNotEmpty()) {
            val pasos = ruta
            for (paso in pasos) {
                delay(110.milliseconds)
                val antes = estado
                if (antes.fase != JuegoFase.JUGANDO) break
                val idsAntes = juegoEnemigosVisibles(antes)
                val despues = juegoAccion(antes, JuegoComando.MOVER, paso)
                estado = despues
                val nuevos = juegoEnemigosVisibles(despues)
                val cerca = despues.enemigos.any {
                    it.vivo && it.despierto && juegoCheb(it.pos, despues.jugador) <= 2
                }
                if (despues.jugador != paso || despues.vida < antes.vida || despues.oxigeno < antes.oxigeno - 1 ||
                    !idsAntes.containsAll(nuevos) || cerca || despues.fase != JuegoFase.JUGANDO
                ) break
            }
            ruta = emptyList()
        }
    }

    fun ejecutar(cmd: JuegoComando, destino: JuegoPos) {
        ruta = emptyList()
        estado = juegoAccion(estado, cmd, destino)
    }

    fun dispararCercano() {
        val objetivo = juegoObjetivoCercano(estado)
        if (objetivo != null) ejecutar(JuegoComando.DISPARAR, objetivo)
        else estado = estado.copy(mensaje = "No hay enemigos a la vista")
    }

    fun usarRapido(tipos: List<JuegoTipoItem>) {
        ruta = emptyList()
        var uid = -1
        for (t in tipos) {
            if (uid < 0) uid = juegoUidDe(estado, t)
        }
        estado = if (uid >= 0) juegoAccion(estado, JuegoComando.USAR, uid = uid)
        else estado.copy(mensaje = "No tienes ese consumible en la mochila")
    }

    val botiquines = listOf(JuegoTipoItem.CON_BOTIQUIN, JuegoTipoItem.CON_BOTIQUIN_AV)
    val capsulas = listOf(JuegoTipoItem.CON_OXIGENO)

    fun tocarCelda(pos: JuegoPos) {
        val e = estado
        if (e.fase != JuegoFase.JUGANDO || pausa || panel != null) return
        val hayEnemigo = e.dentro(pos) && e.enemigos.any { it.vivo && it.pos == pos && e.idx(pos) in e.visibles }
        if (hayEnemigo) {
            ejecutar(JuegoComando.DISPARAR, pos)
            return
        }
        if (pos == e.jugador) {
            ejecutar(JuegoComando.ESPERAR, e.jugador)
            return
        }
        if (e.dentro(pos) && e.muebles[e.idx(pos)] == JuegoMueble.BANCO) {
            if (juegoCheb(e.jugador, pos) == 1) {
                ejecutar(JuegoComando.MOVER, pos)
            } else {
                val camino = juegoCaminoAdyacente(e, pos)
                if (camino.isEmpty()) estado = e.copy(mensaje = "No hay camino hasta el banco") else ruta = camino
            }
            return
        }
        if (juegoCheb(e.jugador, pos) == 1) {
            ejecutar(JuegoComando.MOVER, pos)
            return
        }
        val camino = juegoCamino(e, pos)
        if (camino.isEmpty()) estado = e.copy(mensaje = "No hay camino hasta ahí") else ruta = camino
    }

    fun manejarTecla(k: Key): Boolean {
        val e = estado
        if (e.fase == JuegoFase.MENU) {
            val todas = JuegoDificultad.entries
            if (k == Key.Enter || k == Key.NumPadEnter || k == Key.Spacebar) {
                estado = juegoGenerarPiso(1, null, dificultad)
                return true
            }
            if (k == Key.DirectionRight) {
                dificultad = todas[(dificultad.ordinal + 1) % todas.size]
                return true
            }
            if (k == Key.DirectionLeft) {
                dificultad = todas[(dificultad.ordinal + todas.size - 1) % todas.size]
                return true
            }
            return false
        }
        if (e.fase == JuegoFase.MEJORA) {
            val i = when (k) {
                Key.One, Key.NumPad1 -> 0
                Key.Two, Key.NumPad2 -> 1
                Key.Three, Key.NumPad3 -> 2
                else -> -1
            }
            if (i in e.opciones.indices) {
                estado = juegoAplicarMejora(e, e.opciones[i])
                return true
            }
            return false
        }
        if (e.fase == JuegoFase.VICTORIA || e.fase == JuegoFase.GAME_OVER) {
            if (k == Key.Enter || k == Key.NumPadEnter || k == Key.Spacebar) {
                estado = juegoNuevo(dificultad)
                return true
            }
            return false
        }
        if (e.fase == JuegoFase.JUGANDO) {
            if (panel != null) {
                if (k == Key.Escape || k == Key.I || k == Key.Tab) panel = null
                return true
            }
            if (k == Key.I || k == Key.Tab) {
                ruta = emptyList()
                panel = JuegoPanelTipo.INVENTARIO
                return true
            }
            if (k == Key.P || k == Key.Escape) {
                ruta = emptyList()
                pausa = !pausa
                return true
            }
            if (pausa) return true
            val mov: Pair<Int, Int>? = when (k) {
                Key.DirectionUp, Key.W, Key.NumPad8 -> Pair(-1, 0)
                Key.DirectionDown, Key.S, Key.NumPad2 -> Pair(1, 0)
                Key.DirectionLeft, Key.A, Key.NumPad4 -> Pair(0, -1)
                Key.DirectionRight, Key.D, Key.NumPad6 -> Pair(0, 1)
                Key.Q, Key.NumPad7, Key.MoveHome -> Pair(-1, -1)
                Key.E, Key.NumPad9, Key.PageUp -> Pair(-1, 1)
                Key.Z, Key.NumPad1, Key.MoveEnd -> Pair(1, -1)
                Key.C, Key.NumPad3, Key.PageDown -> Pair(1, 1)
                else -> null
            }
            if (mov != null) {
                ejecutar(JuegoComando.MOVER, JuegoPos(e.jugador.fila + mov.first, e.jugador.col + mov.second))
                return true
            }
            if (k == Key.Spacebar || k == Key.Period || k == Key.NumPad5 || k == Key.X) {
                ejecutar(JuegoComando.ESPERAR, e.jugador)
                return true
            }
            if (k == Key.G || k == Key.T) {
                dispararCercano()
                return true
            }
            if (k == Key.R) {
                ejecutar(JuegoComando.RECARGAR, e.jugador)
                return true
            }
            if (k == Key.H) {
                usarRapido(botiquines)
                return true
            }
            if (k == Key.F || k == Key.Enter || k == Key.NumPad0) {
                ejecutar(JuegoComando.PULSO, e.jugador)
                return true
            }
        }
        return false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JuegoFondo)
            .navigationBarsPadding()
            .focusRequester(enfoque)
            .focusable()
            .onPreviewKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyDown) {
                    val ahora = System.currentTimeMillis()
                    if (ahora - ultimaTecla < 60L) {
                        true
                    } else {
                        ultimaTecla = ahora
                        manejarTecla(ev.key)
                    }
                } else false
            }
    ) {
        AnimatedContent(
            targetState = estado.fase,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                (fadeIn(tween(500, delayMillis = 200)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(500, delayMillis = 200))) togetherWith
                        fadeOut(tween(250))
            },
            label = "fasesJuego"
        ) { fase ->
            when (fase) {
                JuegoFase.MENU -> JuegoMenu(
                    mejor = mejores[dificultad] ?: 0,
                    dificultad = dificultad,
                    onCambiar = { dificultad = it },
                    onJugar = { estado = juegoGenerarPiso(1, null, dificultad) }
                )
                JuegoFase.JUGANDO -> JuegoPantallaJugando(
                    estado = estado,
                    pausa = pausa,
                    onPausa = {
                        ruta = emptyList()
                        pausa = true
                    },
                    onReanudar = { pausa = false },
                    onRendirse = {
                        pausa = false
                        estado = estado.copy(fase = JuegoFase.GAME_OVER, mensaje = "Abandonaste la misión")
                    },
                    onMochila = {
                        ruta = emptyList()
                        panel = JuegoPanelTipo.INVENTARIO
                    },
                    onTocar = { tocarCelda(it) },
                    onPulso = { ejecutar(JuegoComando.PULSO, estado.jugador) },
                    onEsperar = { ejecutar(JuegoComando.ESPERAR, estado.jugador) },
                    onDisparar = { dispararCercano() },
                    onRecargar = { ejecutar(JuegoComando.RECARGAR, estado.jugador) },
                    onBotiquin = { usarRapido(botiquines) },
                    onOxigeno = { usarRapido(capsulas) }
                )
                JuegoFase.MEJORA -> JuegoPantallaMejora(estado) { estado = juegoAplicarMejora(estado, it) }
                JuegoFase.CAMBIO_PISO -> JuegoCambioPiso(estado)
                JuegoFase.VICTORIA -> JuegoFin(true, estado, mejores[estado.dificultad] ?: 0, nuevoRecord) {
                    estado = juegoNuevo(dificultad)
                }
                JuegoFase.GAME_OVER -> JuegoFin(false, estado, mejores[estado.dificultad] ?: 0, nuevoRecord) {
                    estado = juegoNuevo(dificultad)
                }
            }
        }

        AnimatedVisibility(
            visible = panel != null && estado.fase == JuegoFase.JUGANDO,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(200)) + slideInVertically(tween(260)) { it / 6 },
            exit = fadeOut(tween(150))
        ) {
            JuegoPanelMochila(
                estado = estado,
                pestanaInicial = panel ?: JuegoPanelTipo.INVENTARIO,
                onCerrar = { panel = null },
                onEquipar = { uid ->
                    val item = estado.inventario.firstOrNull { it.uid == uid }
                    estado = if (item != null && item.tipo.cat == JuegoCategoria.ARMAS) {
                        juegoCambiarArma(estado, uid)
                    } else {
                        juegoEquipar(estado, uid)
                    }
                },
                onDesequipar = { r -> estado = juegoDesequipar(estado, r) },
                onUsar = { uid -> estado = juegoAccion(estado, JuegoComando.USAR, uid = uid) },
                onDescartar = { uid -> estado = juegoDescartar(estado, uid) },
                onMejorar = { uid -> estado = juegoMejorar(estado, uid) },
                onFabricar = { id -> estado = juegoFabricar(estado, id) }
            )
        }
    }
}
