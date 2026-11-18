package com.example.lab13animaciones

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val JUEGO_TAM = 6
private const val JUEGO_PISOS = 3
private const val JUEGO_VIDA_MAX = 12
private const val JUEGO_OXIGENO_MAX = 30
private const val JUEGO_OXIGENO_BAJO = 8
private const val JUEGO_RECARGA = 10
private const val JUEGO_CURA = 4
private const val JUEGO_DANIO_JUGADOR = 2
private const val JUEGO_RADIO_VISION = 2

private val JuegoFondo = Color(0xFF0B1020)
private val JuegoColorTablero = Color(0xFF141B33)
private val JuegoTexto = Color(0xFFE8EAF6)
private val JuegoTextoSuave = Color(0xFF9FA8DA)
private val JuegoVerde = Color(0xFF43A047)
private val JuegoAmarillo = Color(0xFFFDD835)
private val JuegoRojo = Color(0xFFE53935)
private val JuegoAzul = Color(0xFF38BDF8)
private val JuegoDorado = Color(0xFFFFD54F)

private val JuegoDirecciones = listOf(JuegoPos(-1, 0), JuegoPos(1, 0), JuegoPos(0, -1), JuegoPos(0, 1))

enum class JuegoFase { MENU, JUGANDO, CAMBIO_PISO, VICTORIA, GAME_OVER }

enum class JuegoTipoEnemigo(
    val emoji: String,
    val nombre: String,
    val vidaMax: Int,
    val danio: Int,
    val puntos: Int
) {
    ROBOT("🤖", "Robot", 2, 1, 30),
    ALIEN("👽", "Alien", 4, 2, 50),
    CENTINELA("👾", "Centinela", 8, 3, 150)
}

enum class JuegoTipoObjeto(val emoji: String) {
    TANQUE("🫧"),
    TARJETA("🔑"),
    BOTIQUIN("💊"),
    ESCUDO("🛡️"),
    CRISTAL("💎")
}

data class JuegoPos(val fila: Int, val col: Int)

data class JuegoEnemigo(
    val id: Int,
    val tipo: JuegoTipoEnemigo,
    val pos: JuegoPos,
    val vida: Int,
    val vivo: Boolean = true
)

data class JuegoObjeto(
    val id: Int,
    val tipo: JuegoTipoObjeto,
    val pos: JuegoPos,
    val activo: Boolean = true
)

data class JuegoPopup(
    val id: Int,
    val pos: JuegoPos,
    val texto: String,
    val color: Color
)

data class JuegoEstadoUi(
    val fase: JuegoFase = JuegoFase.JUGANDO,
    val piso: Int = 1,
    val jugador: JuegoPos,
    val vida: Int,
    val oxigeno: Int,
    val tieneTarjeta: Boolean = false,
    val escudo: Boolean = false,
    val puerta: JuegoPos,
    val objetos: List<JuegoObjeto>,
    val enemigos: List<JuegoEnemigo>,
    val paredes: Set<JuegoPos> = emptySet(),
    val explorado: Set<JuegoPos> = emptySet(),
    val mensaje: String = "",
    val golpes: Set<JuegoPos> = emptySet(),
    val golpeContador: Int = 0,
    val popups: List<JuegoPopup> = emptyList(),
    val popupId: Int = 0,
    val puntos: Int = 0,
    val derrotados: Int = 0,
    val turno: Int = 0
)

private fun juegoDistancia(a: JuegoPos, b: JuegoPos) = abs(a.fila - b.fila) + abs(a.col - b.col)

private fun juegoDentro(p: JuegoPos) = p.fila in 0 until JUEGO_TAM && p.col in 0 until JUEGO_TAM

private fun juegoRevelar(explorado: Set<JuegoPos>, centro: JuegoPos): Set<JuegoPos> {
    val nuevo = explorado.toMutableSet()
    for (f in 0 until JUEGO_TAM) {
        for (c in 0 until JUEGO_TAM) {
            val p = JuegoPos(f, c)
            if (juegoDistancia(p, centro) <= JUEGO_RADIO_VISION) nuevo.add(p)
        }
    }
    return nuevo
}

private fun juegoEsJugable(e: JuegoEstadoUi): Boolean {
    val visitados = mutableSetOf(e.jugador)
    val cola = ArrayDeque<JuegoPos>()
    cola.add(e.jugador)
    while (cola.isNotEmpty()) {
        val actual = cola.removeFirst()
        if (actual == e.puerta) continue
        for (d in JuegoDirecciones) {
            val sig = JuegoPos(actual.fila + d.fila, actual.col + d.col)
            if (juegoDentro(sig) && sig !in e.paredes && sig !in visitados) {
                visitados.add(sig)
                cola.add(sig)
            }
        }
    }
    return e.puerta in visitados && e.objetos.all { it.pos in visitados }
}

private fun juegoConstruirPiso(piso: Int, previo: JuegoEstadoUi?, cantidadParedes: Int): JuegoEstadoUi {
    val libres = mutableListOf<JuegoPos>()
    for (f in 0 until JUEGO_TAM) for (c in 0 until JUEGO_TAM) libres.add(JuegoPos(f, c))

    fun tomar(filtro: (JuegoPos) -> Boolean = { true }): JuegoPos {
        val elegida = libres.filter(filtro).random()
        libres.remove(elegida)
        return elegida
    }

    val jugador = tomar()
    val puerta = tomar { juegoDistancia(it, jugador) >= 4 }

    val objetos = mutableListOf<JuegoObjeto>()
    objetos.add(JuegoObjeto(0, JuegoTipoObjeto.TARJETA, tomar()))
    objetos.add(JuegoObjeto(1, JuegoTipoObjeto.TANQUE, tomar()))
    objetos.add(JuegoObjeto(2, JuegoTipoObjeto.TANQUE, tomar()))
    objetos.add(JuegoObjeto(3, JuegoTipoObjeto.BOTIQUIN, tomar()))
    objetos.add(JuegoObjeto(4, JuegoTipoObjeto.CRISTAL, tomar()))
    if (piso >= 2) objetos.add(JuegoObjeto(5, JuegoTipoObjeto.ESCUDO, tomar()))
    if (piso >= 3) objetos.add(JuegoObjeto(6, JuegoTipoObjeto.CRISTAL, tomar()))

    val tipos = mutableListOf<JuegoTipoEnemigo>()
    repeat(piso) { tipos.add(JuegoTipoEnemigo.ROBOT) }
    repeat((piso + 1) / 2) { tipos.add(JuegoTipoEnemigo.ALIEN) }
    if (piso >= JUEGO_PISOS) tipos.add(JuegoTipoEnemigo.CENTINELA)
    val enemigos = tipos.mapIndexed { i, tipo ->
        JuegoEnemigo(i, tipo, tomar { juegoDistancia(it, jugador) >= 3 }, tipo.vidaMax)
    }

    val paredes = libres
        .filter { juegoDistancia(it, jugador) >= 2 }
        .shuffled()
        .take(cantidadParedes)
        .toSet()

    val intro = if (piso >= JUEGO_PISOS) "Piso $piso: ¡cuidado con el Centinela 👾!"
    else "Piso $piso: busca la tarjeta 🔑 y llega a la puerta 🚪"

    return JuegoEstadoUi(
        piso = piso,
        jugador = jugador,
        vida = if (previo == null) JUEGO_VIDA_MAX else minOf(JUEGO_VIDA_MAX, previo.vida + 3),
        oxigeno = JUEGO_OXIGENO_MAX,
        escudo = previo?.escudo ?: false,
        puerta = puerta,
        objetos = objetos,
        enemigos = enemigos,
        paredes = paredes,
        explorado = juegoRevelar(emptySet(), jugador),
        mensaje = intro,
        puntos = previo?.puntos ?: 0,
        derrotados = previo?.derrotados ?: 0,
        turno = previo?.turno ?: 0
    )
}

private fun juegoGenerarPiso(piso: Int, previo: JuegoEstadoUi?): JuegoEstadoUi {
    var estado = juegoConstruirPiso(piso, previo, piso + 4)
    var intentos = 0
    while (!juegoEsJugable(estado) && intentos < 40) {
        estado = juegoConstruirPiso(piso, previo, piso + 4)
        intentos++
    }
    if (!juegoEsJugable(estado)) estado = juegoConstruirPiso(piso, previo, 0)
    return estado
}

private fun juegoNuevo(): JuegoEstadoUi = juegoGenerarPiso(1, null).copy(fase = JuegoFase.MENU)

private fun juegoSiguientePiso(e: JuegoEstadoUi): JuegoEstadoUi = juegoGenerarPiso(e.piso + 1, e)

private fun juegoPasoAleatorio(origen: JuegoPos, ocupadas: List<JuegoPos>): JuegoPos {
    for (d in JuegoDirecciones.shuffled()) {
        val p = JuegoPos(origen.fila + d.fila, origen.col + d.col)
        if (juegoDentro(p) && p !in ocupadas) return p
    }
    return origen
}

private fun juegoPasoHacia(origen: JuegoPos, meta: JuegoPos, ocupadas: List<JuegoPos>): JuegoPos {
    val actual = juegoDistancia(origen, meta)
    val mejor = JuegoDirecciones
        .map { JuegoPos(origen.fila + it.fila, origen.col + it.col) }
        .filter { juegoDentro(it) && it !in ocupadas }
        .shuffled()
        .minByOrNull { juegoDistancia(it, meta) }
    return if (mejor != null && juegoDistancia(mejor, meta) < actual) mejor
    else juegoPasoAleatorio(origen, ocupadas)
}

private fun juegoTocar(e: JuegoEstadoUi, destino: JuegoPos): JuegoEstadoUi {
    if (e.fase != JuegoFase.JUGANDO) return e
    if (!juegoDentro(destino) || juegoDistancia(e.jugador, destino) != 1) return e
    if (destino in e.paredes) return e.copy(mensaje = "Una caja 📦 bloquea el paso")

    var vida = e.vida
    var oxigeno = e.oxigeno - 1
    var tarjeta = e.tieneTarjeta
    var escudo = e.escudo
    var jugador = e.jugador
    var objetos = e.objetos
    var puntos = e.puntos
    var derrotados = e.derrotados
    var mensaje = ""
    val golpes = mutableSetOf<JuegoPos>()
    val lista = e.enemigos.toMutableList()
    val popups = mutableListOf<JuegoPopup>()
    var idPopup = e.popupId

    fun popup(pos: JuegoPos, texto: String, color: Color) {
        popups.add(JuegoPopup(idPopup, pos, texto, color))
        idPopup += 1
    }

    val indiceObjetivo = lista.indexOfFirst { it.vivo && it.pos == destino }
    if (indiceObjetivo >= 0) {
        val objetivo = lista[indiceObjetivo]
        val critico = Random.nextInt(100) < 25
        val danio = if (critico) JUEGO_DANIO_JUGADOR * 2 else JUEGO_DANIO_JUGADOR
        val vidaRestante = (objetivo.vida - danio).coerceAtLeast(0)
        lista[indiceObjetivo] = objetivo.copy(vida = vidaRestante, vivo = vidaRestante > 0)
        golpes.add(destino)
        if (vidaRestante > 0) {
            popup(destino, "-$danio", JuegoAmarillo)
            mensaje = if (critico) "¡Golpe crítico al ${objetivo.tipo.nombre}!"
            else "Golpeaste al ${objetivo.tipo.nombre}"
        } else {
            derrotados += 1
            puntos += objetivo.tipo.puntos
            popup(destino, "+${objetivo.tipo.puntos}", JuegoVerde)
            mensaje = "¡${objetivo.tipo.nombre} destruido! +${objetivo.tipo.puntos} puntos"
        }
    } else if (destino == e.puerta) {
        if (!tarjeta) {
            popup(destino, "🔒", JuegoTexto)
            return e.copy(
                mensaje = "La puerta está bloqueada. Necesitas la tarjeta 🔑",
                popups = popups.toList(),
                popupId = idPopup
            )
        }
        val victoria = e.piso >= JUEGO_PISOS
        val bono = 100 + oxigeno.coerceAtLeast(0) * 2 + if (victoria) 200 else 0
        return e.copy(
            jugador = destino,
            oxigeno = oxigeno,
            fase = if (victoria) JuegoFase.VICTORIA else JuegoFase.CAMBIO_PISO,
            puntos = e.puntos + bono,
            turno = e.turno + 1,
            mensaje = "Piso completado: +$bono puntos",
            golpes = emptySet()
        )
    } else {
        jugador = destino
        objetos = objetos.map { o ->
            if (o.activo && o.pos == destino) {
                when (o.tipo) {
                    JuegoTipoObjeto.TANQUE -> {
                        val antes = oxigeno
                        oxigeno = minOf(JUEGO_OXIGENO_MAX, oxigeno + JUEGO_RECARGA)
                        popup(destino, "+${oxigeno - antes}🫧", JuegoAzul)
                        mensaje = "Tanque recogido: +${oxigeno - antes} de oxígeno"
                    }
                    JuegoTipoObjeto.TARJETA -> {
                        tarjeta = true
                        popup(destino, "🔑", JuegoDorado)
                        mensaje = "¡Tarjeta 🔑 conseguida! La puerta ya se puede abrir"
                    }
                    JuegoTipoObjeto.BOTIQUIN -> {
                        val antes = vida
                        vida = minOf(JUEGO_VIDA_MAX, vida + JUEGO_CURA)
                        popup(destino, "+${vida - antes}❤️", JuegoVerde)
                        mensaje = "Botiquín usado: +${vida - antes} de vida"
                    }
                    JuegoTipoObjeto.ESCUDO -> {
                        escudo = true
                        popup(destino, "🛡️", JuegoAzul)
                        mensaje = "Escudo activado: bloqueará el próximo golpe"
                    }
                    JuegoTipoObjeto.CRISTAL -> {
                        puntos += 50
                        popup(destino, "+50", JuegoDorado)
                        mensaje = "Cristal recogido: +50 puntos"
                    }
                }
                o.copy(activo = false)
            } else o
        }
    }

    val turno = e.turno + 1
    var danioRecibido = 0
    for (i in lista.indices) {
        val en = lista[i]
        if (!en.vivo) continue
        if (en.tipo == JuegoTipoEnemigo.CENTINELA && turno % 2 != 0) continue
        if (juegoDistancia(en.pos, jugador) == 1) {
            danioRecibido += en.tipo.danio
            continue
        }
        val ocupadas = lista.filter { it.vivo && it.id != en.id }.map { it.pos } + jugador + e.paredes
        val persigue = when (en.tipo) {
            JuegoTipoEnemigo.ROBOT -> true
            JuegoTipoEnemigo.CENTINELA -> true
            JuegoTipoEnemigo.ALIEN -> juegoDistancia(en.pos, jugador) <= 3
        }
        val nueva = if (persigue) juegoPasoHacia(en.pos, jugador, ocupadas)
        else juegoPasoAleatorio(en.pos, ocupadas)
        lista[i] = en.copy(pos = nueva)
    }

    if (danioRecibido > 0) {
        if (escudo) {
            escudo = false
            popup(jugador, "🛡️", JuegoAzul)
            mensaje = "¡El escudo 🛡️ absorbió el golpe!"
        } else {
            vida -= danioRecibido
            golpes.add(jugador)
            popup(jugador, "-$danioRecibido", JuegoRojo)
            mensaje = "¡Te atacaron! -$danioRecibido de vida"
        }
    }

    vida = vida.coerceAtLeast(0)
    oxigeno = oxigeno.coerceAtLeast(0)

    var fase = JuegoFase.JUGANDO
    if (vida <= 0) {
        fase = JuegoFase.GAME_OVER
        mensaje = "Los enemigos acabaron contigo"
    } else if (oxigeno <= 0) {
        fase = JuegoFase.GAME_OVER
        mensaje = "Te quedaste sin oxígeno"
    }

    return e.copy(
        fase = fase,
        jugador = jugador,
        vida = vida,
        oxigeno = oxigeno,
        tieneTarjeta = tarjeta,
        escudo = escudo,
        objetos = objetos,
        enemigos = lista.toList(),
        explorado = juegoRevelar(e.explorado, jugador),
        mensaje = mensaje,
        golpes = golpes.toSet(),
        golpeContador = e.golpeContador + if (golpes.isNotEmpty()) 1 else 0,
        popups = popups.toList(),
        popupId = idPopup,
        puntos = puntos,
        derrotados = derrotados,
        turno = turno
    )
}

@Composable
fun EjercicioFinal() {
    var estado by remember { mutableStateOf(juegoNuevo()) }
    var mejor by remember { mutableIntStateOf(0) }

    LaunchedEffect(estado.fase) {
        when (estado.fase) {
            JuegoFase.CAMBIO_PISO -> {
                delay(2000)
                estado = juegoSiguientePiso(estado)
            }
            JuegoFase.VICTORIA -> mejor = maxOf(mejor, estado.puntos)
            JuegoFase.GAME_OVER -> mejor = maxOf(mejor, estado.puntos)
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JuegoFondo)
            .navigationBarsPadding()
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
                JuegoFase.MENU -> JuegoMenu(mejor) { estado = juegoGenerarPiso(1, null) }
                JuegoFase.JUGANDO -> JuegoPantallaJugando(estado) { estado = juegoTocar(estado, it) }
                JuegoFase.CAMBIO_PISO -> JuegoCambioPiso(estado.piso + 1, estado.mensaje, estado.puntos)
                JuegoFase.VICTORIA -> JuegoFin(true, estado, mejor) { estado = juegoNuevo() }
                JuegoFase.GAME_OVER -> JuegoFin(false, estado, mejor) { estado = juegoNuevo() }
            }
        }
    }
}

@Composable
private fun JuegoEstrellas() {
    val trans = rememberInfiniteTransition(label = "estrellas")
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(900, 1300, 1100, 1500, 1000).forEachIndexed { i, ms ->
            val alfa by trans.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(ms), RepeatMode.Reverse),
                label = "estrella$i"
            )
            Text(
                if (i % 2 == 0) "✨" else "⭐",
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer { this.alpha = alfa }
            )
        }
    }
}

@Composable
private fun JuegoMenu(mejor: Int, onJugar: () -> Unit) {
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
        Text("🚀", fontSize = 72.sp, modifier = Modifier.offset(y = flote.dp))
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
            "La nave está fallando. Llega a la cápsula de escape antes de quedarte sin oxígeno.",
            color = JuegoTextoSuave,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "• Toca una casilla vecina para moverte\n" +
                    "• Camina hacia un enemigo para atacarlo\n" +
                    "• Cada paso gasta 1 de oxígeno 🫧\n" +
                    "• Las cajas 📦 bloquean el paso\n" +
                    "• 💊 cura, 🛡️ bloquea un golpe, 💎 da puntos\n" +
                    "• Recoge la tarjeta 🔑 para abrir la puerta 🚪\n" +
                    "• Solo ves 2 casillas a tu alrededor",
            color = JuegoTexto,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        if (mejor > 0) {
            Spacer(Modifier.height(16.dp))
            Text("🏆 Mejor puntaje: $mejor", color = JuegoDorado, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onJugar) { Text("Despegar", fontSize = 18.sp) }
    }
}

@Composable
private fun JuegoCambioPiso(piso: Int, mensaje: String, puntos: Int) {
    val destino = remember { piso }
    val texto = remember { mensaje }
    val total = remember { puntos }
    val trans = rememberInfiniteTransition(label = "cambioPiso")
    val flote by trans.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "floteSatelite"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛰️", fontSize = 72.sp, modifier = Modifier.offset(y = flote.dp))
        Spacer(Modifier.height(12.dp))
        Text("Piso $destino", color = JuegoTexto, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Descendiendo a la siguiente cubierta...", color = JuegoTextoSuave, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        Text(texto, color = JuegoVerde, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("⭐ Puntos: $total", color = JuegoDorado, fontSize = 16.sp)
    }
}

@Composable
private fun JuegoFin(gano: Boolean, estado: JuegoEstadoUi, mejor: Int, onReiniciar: () -> Unit) {
    val datos = remember { estado }
    var listo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { listo = true }
    val puntosAnim by animateIntAsState(
        targetValue = if (listo) datos.puntos else 0,
        animationSpec = tween(1200),
        label = "puntosFinal"
    )
    val esRecord = datos.puntos > 0 && datos.puntos >= mejor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (gano) "🚀" else "💀", fontSize = 72.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            if (gano) "¡Escapaste!" else "Game Over",
            color = if (gano) JuegoVerde else JuegoRojo,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (gano) "Superaste los $JUEGO_PISOS pisos y llegaste a la cápsula de escape."
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
            Text("⭐ Puntos: $puntosAnim", color = JuegoDorado, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("👾 Enemigos derrotados: ${datos.derrotados}", color = JuegoTexto, fontSize = 15.sp)
            Text("👣 Turnos jugados: ${datos.turno}", color = JuegoTexto, fontSize = 15.sp)
            Text("🏆 Mejor puntaje: $mejor", color = JuegoTexto, fontSize = 15.sp)
        }
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(
            visible = listo && esRecord,
            enter = fadeIn(tween(600, delayMillis = 800)) + scaleIn(tween(600, delayMillis = 800))
        ) {
            Text("🎉 ¡Nuevo récord!", color = JuegoDorado, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReiniciar) {
            Text(if (gano) "Jugar otra vez" else "Reintentar desde el piso 1")
        }
    }
}

@Composable
private fun JuegoPantallaJugando(estado: JuegoEstadoUi, onTocar: (JuegoPos) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            JuegoHud(estado)

            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val lado = minOf(maxWidth, maxHeight)
                JuegoTablero(estado, lado, onTocar)
            }

            JuegoMensaje(estado)
        }
        JuegoAlarma(estado.oxigeno <= JUEGO_OXIGENO_BAJO)
    }
}

@Composable
private fun JuegoAlarma(activa: Boolean) {
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
private fun JuegoHud(estado: JuegoEstadoUi) {
    val colorBarra by animateColorAsState(
        targetValue = when {
            estado.oxigeno > 18 -> JuegoVerde
            estado.oxigeno > 9 -> JuegoAmarillo
            else -> JuegoRojo
        },
        animationSpec = tween(500),
        label = "colorOxigeno"
    )
    val fraccion by animateFloatAsState(
        targetValue = estado.oxigeno / JUEGO_OXIGENO_MAX.toFloat(),
        animationSpec = tween(400),
        label = "fraccionOxigeno"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            JuegoContador("🛰️ Piso", estado.piso, "/$JUEGO_PISOS")
            JuegoContador("❤️", estado.vida, "/$JUEGO_VIDA_MAX")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedContent(
                    targetState = estado.tieneTarjeta,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "tarjetaHud"
                ) { tiene ->
                    Text(
                        if (tiene) "🔑✔" else "🔑✖",
                        color = if (tiene) JuegoVerde else JuegoTextoSuave,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                AnimatedContent(
                    targetState = estado.escudo,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "escudoHud"
                ) { tiene ->
                    Text(
                        if (tiene) "🛡️✔" else "🛡️✖",
                        color = if (tiene) JuegoAzul else JuegoTextoSuave,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🫧", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF2A3355))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                        .background(colorBarra)
                )
            }
            Spacer(Modifier.width(8.dp))
            JuegoContador("", estado.oxigeno, "/$JUEGO_OXIGENO_MAX")
            Spacer(Modifier.width(12.dp))
            JuegoContador("⭐", estado.puntos, "")
        }
    }
}

@Composable
private fun JuegoContador(prefijo: String, valor: Int, sufijo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (prefijo.isNotEmpty()) {
            Text("$prefijo ", color = JuegoTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
private fun JuegoMensaje(estado: JuegoEstadoUi) {
    val texto = estado.mensaje.ifEmpty { "Toca una casilla vecina para moverte o atacar" }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = texto,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "mensajeJuego"
        ) { t ->
            Box(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(t, color = JuegoTexto, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
        Text(
            "Turno ${estado.turno} · 🤖 persigue · 👽 acecha · 👾 jefe · 💊 cura · 🛡️ escudo · 💎 puntos",
            color = JuegoTextoSuave,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun JuegoTablero(estado: JuegoEstadoUi, lado: Dp, onTocar: (JuegoPos) -> Unit) {
    val celda = lado / JUEGO_TAM
    val tamEmoji = (celda.value * 0.5f).sp

    var destello by remember { mutableStateOf<Set<JuegoPos>>(emptySet()) }
    var sacudida by remember { mutableStateOf(0.dp) }
    val sacudidaAnim by animateDpAsState(sacudida, tween(40), label = "sacudida")

    LaunchedEffect(estado.golpeContador) {
        if (estado.golpeContador > 0) {
            destello = estado.golpes
            delay(300)
            destello = emptySet()
        }
    }
    LaunchedEffect(estado.golpeContador) {
        if (estado.golpeContador > 0 && estado.golpes.contains(estado.jugador)) {
            repeat(3) {
                sacudida = 7.dp
                delay(45)
                sacudida = (-7).dp
                delay(45)
            }
            sacudida = 0.dp
        }
    }

    Box(
        modifier = Modifier
            .offset(x = sacudidaAnim)
            .size(lado)
            .clip(RoundedCornerShape(14.dp))
            .background(JuegoColorTablero)
    ) {
        Column {
            for (f in 0 until JUEGO_TAM) {
                Row {
                    for (c in 0 until JUEGO_TAM) {
                        val pos = JuegoPos(f, c)
                        JuegoCasilla(pos, celda, destello.contains(pos), pos in estado.paredes, tamEmoji)
                    }
                }
            }
        }

        JuegoPuerta(estado.puerta, estado.tieneTarjeta, celda, tamEmoji)

        estado.objetos.forEach { obj ->
            key(obj.id) { JuegoObjetoVista(obj, celda, tamEmoji) }
        }

        estado.enemigos.forEach { en ->
            key(en.id) { JuegoEnemigoVista(en, celda, tamEmoji) }
        }

        JuegoJugadorVista(estado.jugador, estado.escudo, celda, tamEmoji)

        estado.popups.forEach { p ->
            key(p.id) { JuegoPopupVista(p, celda) }
        }

        Column {
            for (f in 0 until JUEGO_TAM) {
                Row {
                    for (c in 0 until JUEGO_TAM) {
                        val pos = JuegoPos(f, c)
                        JuegoCeldaNiebla(
                            tam = celda,
                            oculta = juegoDistancia(pos, estado.jugador) > JUEGO_RADIO_VISION,
                            explorada = pos in estado.explorado,
                            onClick = { onTocar(pos) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JuegoCasilla(pos: JuegoPos, tam: Dp, destello: Boolean, pared: Boolean, tamEmoji: TextUnit) {
    val base = when {
        pared -> Color(0xFF3B2F4A)
        (pos.fila + pos.col) % 2 == 0 -> Color(0xFF1B2440)
        else -> Color(0xFF222C4D)
    }
    val color by animateColorAsState(
        targetValue = if (destello) JuegoRojo else base,
        animationSpec = tween(250),
        label = "casilla"
    )
    Box(
        modifier = Modifier
            .size(tam)
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (pared) Text("📦", fontSize = tamEmoji)
    }
}

@Composable
private fun JuegoCeldaNiebla(tam: Dp, oculta: Boolean, explorada: Boolean, onClick: () -> Unit) {
    val colorNiebla by animateColorAsState(
        targetValue = if (explorada) Color(0xD90A0E1A) else Color(0xFF0A0E1A),
        animationSpec = tween(300),
        label = "colorNiebla"
    )
    Box(modifier = Modifier.size(tam).clickable { onClick() }) {
        AnimatedVisibility(
            visible = oculta,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorNiebla)
            )
        }
    }
}

@Composable
private fun JuegoPuerta(puerta: JuegoPos, abierta: Boolean, celda: Dp, tamEmoji: TextUnit) {
    val trans = rememberInfiniteTransition(label = "puerta")
    val brillo by trans.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "brilloPuerta"
    )
    Box(
        modifier = Modifier
            .offset(x = celda * puerta.col, y = celda * puerta.fila)
            .size(celda),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = abierta,
            enter = fadeIn(tween(500)) + scaleIn(animationSpec = tween(500)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(JuegoVerde.copy(alpha = brillo))
            )
        }
        Text("🚪", fontSize = tamEmoji)
        AnimatedVisibility(
            visible = !abierta,
            modifier = Modifier.align(Alignment.BottomEnd),
            exit = fadeOut(tween(400)) + scaleOut(animationSpec = tween(400))
        ) {
            Text("🔒", fontSize = (celda.value * 0.3f).sp)
        }
    }
}

@Composable
private fun JuegoObjetoVista(obj: JuegoObjeto, celda: Dp, tamEmoji: TextUnit) {
    val trans = rememberInfiniteTransition(label = "objeto")
    val flote by trans.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "floteObjeto"
    )
    Box(
        modifier = Modifier
            .offset(x = celda * obj.pos.col, y = celda * obj.pos.fila)
            .size(celda),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = obj.activo,
            enter = fadeIn(),
            exit = fadeOut(tween(300)) + scaleOut(targetScale = 1.8f, animationSpec = tween(300))
        ) {
            Text(obj.tipo.emoji, fontSize = tamEmoji, modifier = Modifier.offset(y = flote.dp))
        }
    }
}

@Composable
private fun JuegoEnemigoVista(en: JuegoEnemigo, celda: Dp, tamEmoji: TextUnit) {
    val x by animateDpAsState(celda * en.pos.col, tween(300), label = "enemigoX")
    val y by animateDpAsState(celda * en.pos.fila, tween(300), label = "enemigoY")
    val fraccionVida = en.vida / en.tipo.vidaMax.toFloat()
    val tamano = if (en.tipo == JuegoTipoEnemigo.CENTINELA) tamEmoji * 1.15f else tamEmoji

    Box(
        modifier = Modifier.offset(x = x, y = y).size(celda),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = en.vivo,
            enter = fadeIn(),
            exit = fadeOut(tween(400)) + scaleOut(targetScale = 0.2f, animationSpec = tween(400))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(en.tipo.emoji, fontSize = tamano)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp)
                        .width(celda * 0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x66000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccionVida)
                            .background(Color(0xFFEF5350))
                    )
                }
            }
        }
    }
}

@Composable
private fun JuegoJugadorVista(pos: JuegoPos, escudo: Boolean, celda: Dp, tamEmoji: TextUnit) {
    val x by animateDpAsState(celda * pos.col, tween(250), label = "jugadorX")
    val y by animateDpAsState(celda * pos.fila, tween(250), label = "jugadorY")
    val trans = rememberInfiniteTransition(label = "respiracion")
    val escala by trans.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "escalaJugador"
    )

    Box(
        modifier = Modifier.offset(x = x, y = y).size(celda),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = escudo,
            enter = fadeIn(tween(300)) + scaleIn(animationSpec = tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .size(celda * 0.9f)
                    .clip(CircleShape)
                    .background(JuegoAzul.copy(alpha = 0.35f))
            )
        }
        Text(
            "🧑‍🚀",
            fontSize = tamEmoji,
            modifier = Modifier.graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
        )
    }
}

@Composable
private fun JuegoPopupVista(popup: JuegoPopup, celda: Dp) {
    var arriba by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { arriba = true }
    val dy by animateDpAsState(
        targetValue = if (arriba) -celda * 0.8f else 0.dp,
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
            .offset(x = celda * popup.pos.col, y = celda * popup.pos.fila + dy)
            .size(celda)
            .graphicsLayer { this.alpha = alfa },
        contentAlignment = Alignment.Center
    ) {
        Text(
            popup.texto,
            color = popup.color,
            fontSize = (celda.value * 0.32f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}