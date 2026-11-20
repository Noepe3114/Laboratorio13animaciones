@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.ui.graphics.Color
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

// ───────────────────────── Búsqueda de caminos ─────────────────────────

/**
 * A* con ocho direcciones. Las diagonales cuestan lo mismo que las rectas (como un alfil que cruza
 * el tablero), se penalizan los cambios de dirección para que los trayectos salgan limpios y
 * nunca se corta una esquina bloqueada. Evita muebles, paredes, trampas y enemigos visibles.
 */
internal fun juegoCamino(e: JuegoEstadoUi, meta: JuegoPos): List<JuegoPos> {
    if (!e.dentro(meta) || e.idx(meta) !in e.explorado || !e.libre(meta)) return emptyList()
    val enemigosVisibles = e.enemigos.filter { it.vivo && e.idx(it.pos) in e.visibles }.map { e.idx(it.pos) }.toSet()
    if (e.idx(meta) in enemigosVisibles) return emptyList()
    val total = e.ancho * e.alto
    val g = IntArray(total) { Int.MAX_VALUE }
    val padre = IntArray(total) { -2 }
    val dirPadre = IntArray(total) { -1 }
    val cerrado = BooleanArray(total)
    val ini = e.idx(e.jugador)
    val fin = e.idx(meta)

    fun h(i: Int): Int = 100 * maxOf(abs(i / e.ancho - fin / e.ancho), abs(i % e.ancho - fin % e.ancho))

    val cola = PriorityQueue<Pair<Int, Int>>(compareBy<Pair<Int, Int>>({ it.first }, { it.second }))
    g[ini] = 0
    padre[ini] = -1
    cola.add(Pair(h(ini), ini))
    while (cola.isNotEmpty()) {
        val par = cola.poll() ?: break
        val a = par.second
        if (cerrado[a]) continue
        cerrado[a] = true
        if (a == fin) break
        val pa = JuegoPos(a / e.ancho, a % e.ancho)
        for ((di, d) in JUEGO_VECINOS8.withIndex()) {
            val b = JuegoPos(pa.fila + d.fila, pa.col + d.col)
            if (!e.libre(b)) continue
            val ib = e.idx(b)
            if (cerrado[ib] || ib !in e.explorado) continue
            if (ib in e.trampas && ib != fin) continue
            if (ib in enemigosVisibles) continue
            if (!juegoContacto(e, pa, b)) continue
            var costo = 100
            if (e.tile(b) == JuegoTile.FLUIDO) costo += 250
            if (dirPadre[a] != -1 && dirPadre[a] != di) costo += 2
            val ng = g[a] + costo
            if (ng < g[ib]) {
                g[ib] = ng
                padre[ib] = a
                dirPadre[ib] = di
                cola.add(Pair(ng + h(ib), ib))
            }
        }
    }
    if (padre[fin] == -2) return emptyList()
    val ruta = mutableListOf<JuegoPos>()
    var actual = fin
    while (actual != ini) {
        ruta.add(JuegoPos(actual / e.ancho, actual % e.ancho))
        actual = padre[actual]
    }
    ruta.reverse()
    return ruta
}

/** Camino hasta una casilla vecina de un mueble (para ir hacia el banco de trabajo). */
internal fun juegoCaminoAdyacente(e: JuegoEstadoUi, mueble: JuegoPos): List<JuegoPos> {
    var mejor: List<JuegoPos> = emptyList()
    for (d in JUEGO_VECINOS8) {
        val b = JuegoPos(mueble.fila + d.fila, mueble.col + d.col)
        if (b == e.jugador) return emptyList()
        val c = juegoCamino(e, b)
        if (c.isNotEmpty() && (mejor.isEmpty() || c.size < mejor.size)) mejor = c
    }
    return mejor
}

/** Enemigo visible más cercano: prefiere los que están a tiro del arma equipada. */
internal fun juegoObjetivoCercano(e: JuegoEstadoUi): JuegoPos? {
    val arma = e.equipo[JuegoRanura.ARMA]
    val alcance = if (arma != null) juegoStatsItem(arma).alcance else 1
    val candidatos = e.enemigos
        .filter { it.vivo && e.idx(it.pos) in e.visibles }
        .sortedBy { juegoCheb(it.pos, e.jugador) }
    val aTiro = candidatos.firstOrNull {
        juegoCheb(it.pos, e.jugador) <= alcance && juegoLineaLibre(e.mapa, e.ancho, e.jugador, it.pos)
    }
    return (aTiro ?: candidatos.firstOrNull())?.pos
}

// ───────────────────────── Jefe y ataques enemigos ─────────────────────────

internal fun juegoFaseJefe(en: JuegoEnemigo): Int = when {
    en.vida * 3 <= en.vidaMax -> 3
    en.vida * 3 <= en.vidaMax * 2 -> 2
    else -> 1
}

internal data class JuegoAtaque(val alcance: Int, val precision: Int, val danio: Int, val tipo: JuegoTipoDanio)

/** El arma o módulo que lleva el enemigo cambia su forma de atacar. */
internal fun juegoAtaque(en: JuegoEnemigo): JuegoAtaque {
    var alcance = en.tipo.alcance
    var precision = en.tipo.precision
    var danio = en.danio
    var tipo = en.tipo.danioTipo
    val eq = en.equipo
    if (eq != null) {
        when (eq.tipo.cat) {
            JuegoCategoria.ARMAS -> {
                val s = juegoStatsItem(eq)
                alcance = maxOf(alcance, minOf(s.alcance, 6))
                precision = (s.precision - 8).coerceIn(40, 90)
                danio += (maxOf(0f, s.dano - 2f) * 0.6f).roundToInt()
                tipo = s.familia?.danio ?: tipo
            }
            JuegoCategoria.TECNOLOGIA -> precision += 8
            else -> {}
        }
    }
    return JuegoAtaque(alcance, precision.coerceAtMost(95), danio, tipo)
}

// ───────────────────────── Mejoras de piso ─────────────────────────

internal fun JuegoMejora.disponible(e: JuegoEstadoUi): Boolean = when (this) {
    JuegoMejora.BLINDAJE -> e.bonoVida < 9
    JuegoMejora.ARMAS -> e.bonoDanio < 3
    JuegoMejora.TANQUE -> e.bonoOxigeno < 40
    JuegoMejora.ESCUDO -> true
    JuegoMejora.PULSO -> e.pulsoRecarga > 4
    JuegoMejora.PRECISION -> e.bonoCritico < 30
}

internal fun juegoAplicarMejora(e: JuegoEstadoUi, m: JuegoMejora): JuegoEstadoUi {
    val base = e.copy(fase = JuegoFase.CAMBIO_PISO, mensaje = "${e.mensaje}\nMejora: ${m.titulo}")
    return when (m) {
        JuegoMejora.BLINDAJE -> base.copy(bonoVida = e.bonoVida + 3, vidaMax = e.vidaMax + 3, vida = e.vidaMax + 3)
        JuegoMejora.ARMAS -> base.copy(bonoDanio = minOf(3, e.bonoDanio + 1))
        JuegoMejora.TANQUE -> base.copy(bonoOxigeno = minOf(40, e.bonoOxigeno + 20))
        JuegoMejora.ESCUDO -> base.copy(escudo = true)
        JuegoMejora.PULSO -> base.copy(pulsoRecarga = maxOf(4, e.pulsoRecarga - 2))
        JuegoMejora.PRECISION -> base.copy(bonoCritico = minOf(30, e.bonoCritico + 15))
    }
}

/** Cambiar de arma cuesta un turno. */
internal fun juegoCambiarArma(e: JuegoEstadoUi, uid: Int): JuegoEstadoUi {
    val nombre = e.inventario.firstOrNull { it.uid == uid }?.nombreCompleto() ?: return e
    val equipada = juegoEquipar(e, uid)
    if (equipada.equipo == e.equipo) return equipada
    val r = juegoAccion(equipada, JuegoComando.ESPERAR)
    return if (r.vida < equipada.vida) r else r.copy(mensaje = "Cambias a $nombre")
}

// ───────────────────────── Contexto de un turno ─────────────────────────

internal class JuegoCtx(val e: JuegoEstadoUi) {
    var mapa = e.mapa
    var vida = e.vida
    var oxigeno = e.oxigeno
    var tarjeta = e.tieneTarjeta
    var escudo = e.escudo
    var escudoPts = e.escudoPts
    var escudoRegen = e.escudoRegen
    var jugador = e.jugador
    var objetos = e.objetos
    var trampas = e.trampas
    var puntos = e.puntos
    var derrotados = e.derrotados
    var combo = e.combo
    var comboMax = e.comboMax
    var ultimoKill = e.ultimoKill
    var bonoDanio = e.bonoDanio
    var pulsoEspera = e.pulsoEspera
    var pulsoContador = e.pulsoContador
    var inventario = e.inventario
    var equipo = e.equipo
    var itemId = e.itemId
    var recarga = e.recarga
    var oxiAcum = e.oxiAcum
    var invocados = e.invocados
    var mensaje = ""
    var ultimoGanado = 0
    val golpes = mutableSetOf<JuegoPos>()
    val lista = e.enemigos.toMutableList()
    val popups = mutableListOf<JuegoPopup>()
    val disparos = mutableListOf<JuegoDisparo>()
    val efectos = mutableListOf<JuegoEfecto>()
    val avisos = e.avisos.toMutableList()
    var idPopup = e.popupId
    var idDisparo = e.disparoId
    var idEfecto = e.efectoId
    var idAviso = e.avisoId
    val traje = juegoTraje(e.equipo)
    val turno = e.turno + 1
    var perdidaVida = 0
    var perdidaEscudo = 0
    var bloqueos = 0
    var atacado = false
    private val pendientes = mutableListOf<JuegoObjeto>()

    // ── utilidades ──

    fun popup(pos: JuegoPos, texto: String, color: Color) {
        popups.add(JuegoPopup(idPopup, pos, texto, color))
        idPopup += 1
    }

    fun efecto(pos: JuegoPos, tipo: JuegoTipoEfecto, color: Color, radio: Int = 1) {
        efectos.add(JuegoEfecto(idEfecto, pos, tipo, color, radio))
        idEfecto += 1
    }

    fun aviso(texto: String, color: Color, icono: JuegoSprite) {
        avisos.add(JuegoAviso(idAviso, texto, color, icono))
        idAviso += 1
        while (avisos.size > JUEGO_MAX_AVISOS) avisos.removeAt(0)
    }

    fun uid(): Int {
        val v = itemId
        itemId += 1
        return v
    }

    fun ganar(base: Int): Int = base * e.dificultad.puntosPct / 100

    fun consumirOxigeno() {
        oxiAcum += traje.eficiencia
        if (oxiAcum >= 100) oxiAcum -= 100 else oxigeno -= 1
    }

    fun agregarItem(item: JuegoItem): Int {
        val (nuevo, resto) = juegoAgregar(inventario, item, juegoCapacidad(traje)) { uid() }
        inventario = nuevo
        return resto
    }

    fun sinTurno(): JuegoEstadoUi = e.copy(
        mensaje = mensaje,
        popups = popups.toList(),
        popupId = idPopup,
        avisos = avisos.toList(),
        avisoId = idAviso
    )

    // ── daño ──

    fun danioAEnemigo(en: JuegoEnemigo, base: Float, tipo: JuegoTipoDanio, pen: Int): Int {
        val pct = when (tipo) {
            JuegoTipoDanio.CINETICO -> en.tipo.resCin
            JuegoTipoDanio.PLASMA -> en.tipo.resPla
            JuegoTipoDanio.ELECTRICO -> en.tipo.resEle
        }
        val defensa = en.defensa * (100 - pen.coerceIn(0, 100)) / 100f
        return maxOf(1, juegoRedondeoProb(base * pct / 100f - defensa))
    }

    fun recibirDanio(danio: Int, tipo: JuegoTipoDanio, enemigo: Boolean = true) {
        if (enemigo) atacado = true
        if (escudo) {
            escudo = false
            bloqueos += 1
            return
        }
        var d = if (tipo.energetico) danio - traje.resEnergia - traje.defensa / 2 else danio - traje.defensa
        d = maxOf(1, d)
        val absorbido = minOf(escudoPts, d)
        escudoPts -= absorbido
        d -= absorbido
        perdidaEscudo += absorbido
        if (d > 0) {
            vida -= d
            perdidaVida += d
        }
    }

    fun matar(en: JuegoEnemigo, pos: JuegoPos) {
        derrotados += 1
        combo = if (combo > 0 && turno - ultimoKill <= JUEGO_VENTANA_COMBO) combo + 1 else 1
        ultimoKill = turno
        comboMax = maxOf(comboMax, combo)
        val ganado = ganar(en.tipo.puntos) + if (combo >= 2) ganar(10 * combo) else 0
        puntos += ganado
        ultimoGanado = ganado
        popup(pos, "+$ganado", JuegoVerde)
        efecto(pos, JuegoTipoEfecto.EXPLOSION, Color(0xFFFF7043))
        soltarBotin(en, pos)
    }

    private fun soltarBotin(en: JuegoEnemigo, pos: JuegoPos) {
        val pct = e.dificultad.botinPct + traje.botin
        val items = mutableListOf<JuegoItem>()
        for (d in en.tipo.botin) {
            val prob = if (d.prob >= 100) 100 else minOf(95, d.prob * pct / 100)
            if (Random.nextInt(100) < prob) {
                items.add(juegoCrearMaterial(d.tipo, Random.nextInt(d.min, d.max + 1), uid()))
            }
        }
        val eq = en.equipo
        if (eq != null && Random.nextInt(100) < en.tipo.probEquipo) items.add(eq)
        if (items.isEmpty()) return
        val existente = objetos.indexOfFirst { it.activo && it.tipo == JuegoTipoObjeto.BOTIN && it.pos == pos }
        objetos = if (existente >= 0) {
            objetos.mapIndexed { i, o -> if (i == existente) o.copy(items = o.items + items) else o }
        } else {
            objetos + JuegoObjeto((objetos.maxOfOrNull { it.id } ?: -1) + 1, JuegoTipoObjeto.BOTIN, pos, true, items)
        }
        val mejor = items.maxByOrNull { it.rareza.ordinal } ?: return
        popup(JuegoPos(pos.fila - 1, pos.col), "Botín", mejor.rareza.color)
        if (mejor.rareza.ordinal >= JuegoRareza.RARO.ordinal) {
            efecto(pos, JuegoTipoEfecto.DESTELLO, mejor.rareza.color, 2)
            aviso("¡Botín ${mejor.rareza.nombre.lowercase()}: ${mejor.nombreCompleto()}!", mejor.rareza.color, mejor.tipo.icono)
        }
    }

    /** Aplica daño a un enemigo. Devuelve true si murió. */
    fun aplicarDanio(i: Int, dmg: Int, aturdirPct: Int = 0): Boolean {
        val en = lista[i]
        val faseAntes = if (en.tipo == JuegoTipoEnemigo.CENTINELA) juegoFaseJefe(en) else 0
        val vidaN = (en.vida - dmg).coerceAtLeast(0)
        var nuevo = en.copy(vida = vidaN, vivo = vidaN > 0, despierto = true)
        golpes.add(en.pos)
        if (vidaN <= 0) {
            lista[i] = nuevo
            matar(en, en.pos)
            return true
        }
        if (aturdirPct > 0 && Random.nextInt(100) < aturdirPct) {
            nuevo = nuevo.copy(aturdido = 1)
            popup(en.pos, "⚡", JuegoAzul)
        }
        lista[i] = nuevo
        if (en.tipo == JuegoTipoEnemigo.CENTINELA) {
            val f = juegoFaseJefe(nuevo)
            if (f > faseAntes) {
                mensaje = "¡El Centinela entra en la fase $f!"
                efecto(en.pos, JuegoTipoEfecto.ADVERTENCIA, JuegoRojo, 2)
            }
        }
        return false
    }

    // ── acciones del jugador ──

    private fun melee(idx: Int): Boolean {
        val en = lista[idx]
        val dmg = danioAEnemigo(en, 1f + 0.08f * bonoDanio, JuegoTipoDanio.CINETICO, 0)
        val murio = aplicarDanio(idx, dmg)
        efecto(en.pos, JuegoTipoEfecto.IMPACTO, JuegoAmarillo)
        if (murio) {
            mensaje = "¡${en.tipo.nombre} destruido! +$ultimoGanado puntos"
        } else {
            popup(en.pos, "-$dmg", JuegoAmarillo)
            mensaje = "Golpe con la culata: -$dmg"
        }
        return true
    }

    fun disparar(obj: JuegoPos): Boolean {
        val idx = lista.indexOfFirst { it.vivo && it.pos == obj }
        if (idx < 0) {
            mensaje = "No hay objetivo ahí"
            return false
        }
        if (e.idx(obj) !in e.visibles) {
            mensaje = "No ves a ese enemigo"
            return false
        }
        val arma = equipo[JuegoRanura.ARMA]
        val d = juegoCheb(jugador, obj)
        if (arma == null) {
            if (d <= 1) return melee(idx)
            mensaje = "No llevas ningún arma equipada"
            return false
        }
        if (recarga > 0) {
            if (d <= 1) return melee(idx)
            mensaje = "Recargando el arma..."
            return false
        }
        if (arma.municion < 1) {
            if (d <= 1) return melee(idx)
            mensaje = "Cargador vacío: recarga con el botón o la tecla R"
            return false
        }
        val s = juegoStatsItem(arma)
        if (d > s.alcance) {
            mensaje = "Fuera de alcance (${s.alcance} casillas)"
            return false
        }
        if (!juegoLineaLibre(mapa, e.ancho, jugador, obj)) {
            mensaje = "No hay línea de tiro"
            return false
        }

        val gasto = minOf(s.consumo, arma.municion)
        val tiros = if (gasto >= s.consumo) s.disparos else gasto
        val ahorra = s.ahorro > 0 && Random.nextInt(100) < s.ahorro
        equipo = equipo + (JuegoRanura.ARMA to arma.copy(municion = arma.municion - if (ahorra) 0 else gasto))

        val libre = if (s.familia == JuegoFamilia.ESCOPETA) 1 else 2
        val prec = (s.precision + traje.precision - s.caida * maxOf(0, d - libre)).coerceIn(5, 98)
        val critProb = (s.critico + traje.critico + e.bonoCritico).coerceAtMost(60)
        val tipoD = s.familia?.danio ?: JuegoTipoDanio.CINETICO
        val danoBase = s.dano * (1f + 0.08f * bonoDanio)
        val nombre = lista[idx].tipo.nombre
        var aciertos = 0
        var total = 0
        var hubo = false
        var murio = false

        for (t in 0 until tiros) {
            val acierta = Random.nextInt(100) < prec
            disparos.add(JuegoDisparo(idDisparo, jugador, obj, tipoD, acierta, t * 70, false))
            idDisparo += 1
            if (!acierta || murio) continue
            aciertos += 1
            val esCritico = Random.nextInt(100) < critProb
            if (esCritico) hubo = true
            val bruto = danoBase * if (esCritico) s.critMult / 100f else 1f
            val dmg = danioAEnemigo(lista[idx], bruto, tipoD, s.penetracion)
            total += dmg
            efecto(obj, JuegoTipoEfecto.IMPACTO, tipoD.color)
            if (aplicarDanio(idx, dmg, s.aturdir)) murio = true
        }

        if (aciertos > 0 && s.splash > 0) {
            for (j in lista.indices) {
                val otro = lista[j]
                if (j != idx && otro.vivo && juegoCheb(otro.pos, obj) <= 1) {
                    val dmg = danioAEnemigo(otro, danoBase * s.splash / 100f, tipoD, s.penetracion)
                    efecto(otro.pos, JuegoTipoEfecto.IMPACTO, tipoD.color)
                    if (!aplicarDanio(j, dmg)) popup(otro.pos, "-$dmg", tipoD.color)
                }
            }
        }

        if (aciertos > 0) {
            popup(obj, if (hubo) "¡-$total!" else "-$total", if (hubo) JuegoDorado else JuegoAmarillo)
        } else {
            popup(obj, "Falla", JuegoTextoSuave)
        }
        mensaje = when {
            murio -> "¡$nombre destruido! +$ultimoGanado puntos" + if (combo >= 2) " (Combo x$combo)" else ""
            aciertos == 0 -> "Fallaste el disparo"
            hubo -> "¡Golpe crítico! -$total al $nombre"
            else -> "Impacto: $aciertos/$tiros, -$total al $nombre"
        }
        for (i in lista.indices) {
            val x = lista[i]
            if (x.vivo && !x.despierto && juegoCheb(x.pos, jugador) <= 5 && Random.nextInt(100) < 50) {
                lista[i] = x.copy(despierto = true)
            }
        }
        return true
    }

    fun iniciarRecarga(): Boolean {
        val arma = equipo[JuegoRanura.ARMA]
        if (arma == null) {
            mensaje = "No llevas ningún arma"
            return false
        }
        val s = juegoStatsItem(arma)
        if (recarga > 0) {
            mensaje = "Ya estás recargando"
            return false
        }
        if (arma.municion >= s.cargador) {
            mensaje = "El cargador está lleno"
            return false
        }
        if (juegoReserva(inventario, s.familia) <= 0) {
            mensaje = "No te queda munición de reserva"
            return false
        }
        val turnos = maxOf(1, s.recarga - traje.menosRecarga)
        if (turnos <= 1) {
            completarRecarga()
        } else {
            recarga = turnos - 1
            mensaje = "Recargando..."
        }
        return true
    }

    fun completarRecarga() {
        val arma = equipo[JuegoRanura.ARMA] ?: return
        val s = juegoStatsItem(arma)
        val familia = s.familia ?: return
        val falta = s.cargador - arma.municion
        val toma = minOf(falta, juegoReserva(inventario, familia))
        if (toma <= 0) return
        inventario = juegoQuitar(inventario, familia.municion(), toma)
        equipo = equipo + (JuegoRanura.ARMA to arma.copy(municion = arma.municion + toma))
        mensaje = "Arma recargada: ${arma.municion + toma}/${s.cargador}"
        popup(jugador, "Recargada", JuegoAzul)
    }

    fun usar(uid: Int): Boolean {
        val obj = inventario.firstOrNull { it.uid == uid && it.tipo.cat == JuegoCategoria.CONSUMIBLES }
        if (obj == null) {
            mensaje = "No tienes ese objeto"
            return false
        }
        val s = obj.tipo.base
        when (obj.tipo) {
            JuegoTipoItem.CON_BOTIQUIN, JuegoTipoItem.CON_BOTIQUIN_AV -> {
                if (vida >= e.vidaMax) {
                    mensaje = "Ya tienes la vida al máximo"
                    return false
                }
                val antes = vida
                vida = minOf(e.vidaMax, vida + s.cura)
                popup(jugador, "+${vida - antes}", JuegoVerde)
                mensaje = "${obj.tipo.nombre} usado: +${vida - antes} de vida"
            }
            JuegoTipoItem.CON_OXIGENO -> {
                if (oxigeno >= e.oxigenoMax) {
                    mensaje = "El tanque ya está lleno"
                    return false
                }
                val antes = oxigeno
                oxigeno = minOf(e.oxigenoMax, oxigeno + s.oxigeno)
                popup(jugador, "+${oxigeno - antes} O2", JuegoAzul)
                mensaje = "Cápsula usada: +${oxigeno - antes} de oxígeno"
            }
            JuegoTipoItem.CON_CELDA_ESCUDO -> {
                if (traje.escudoMax > 0) {
                    if (escudoPts >= traje.escudoMax) {
                        mensaje = "El escudo ya está al máximo"
                        return false
                    }
                    escudoPts = traje.escudoMax
                } else {
                    if (escudo) {
                        mensaje = "Ya tienes un escudo activo"
                        return false
                    }
                    escudo = true
                }
                popup(jugador, "Escudo", JuegoAzul)
                mensaje = "Celda de escudo usada: escudo recargado"
            }
            JuegoTipoItem.CON_CONDENSADOR -> {
                if (pulsoEspera <= 0) {
                    mensaje = "El pulso ya está listo"
                    return false
                }
                pulsoEspera = 0
                popup(jugador, "Pulso listo", JuegoAzul)
                mensaje = "Condensador usado: el pulso está listo"
            }
            else -> {
                mensaje = "No se puede usar"
                return false
            }
        }
        inventario = juegoQuitar(inventario, obj.tipo, 1)
        return true
    }

    fun pulso() {
        pulsoContador += 1
        pulsoEspera = e.pulsoRecarga
        var alcanzados = 0
        for (i in lista.indices) {
            val en = lista[i]
            if (en.vivo && juegoCheb(en.pos, jugador) <= JUEGO_RADIO_PULSO && e.idx(en.pos) in e.visibles) {
                val dmg = danioAEnemigo(en, JUEGO_DANIO_PULSO.toFloat(), JuegoTipoDanio.ELECTRICO, 0)
                alcanzados += 1
                if (!aplicarDanio(i, dmg, 100)) popup(en.pos, "-$dmg", JuegoAmarillo)
            }
        }
        mensaje = if (alcanzados > 0) "¡Pulso! Alcanzó y aturdió a $alcanzados enemigo(s)" else "El pulso no alcanzó a nadie"
    }

    // ── movimiento y recogida ──

    private fun darConsumible(tipo: JuegoTipoItem): Boolean {
        val resto = agregarItem(JuegoItem(uid(), tipo, tipo.rarezaBase, 1, 1))
        if (resto > 0) {
            mensaje = "Mochila llena: no puedes recoger ${tipo.nombre}"
            return false
        }
        popup(jugador, "+1 ${tipo.nombre}", tipo.rarezaBase.color)
        aviso("+1 ${tipo.nombre}", tipo.rarezaBase.color, tipo.icono)
        mensaje = "${tipo.nombre} guardado en la mochila"
        return true
    }

    private fun notificar(item: JuegoItem, n: Int) {
        aviso("+$n ${item.nombreCompleto()}", item.rareza.color, item.tipo.icono)
        mensaje = "Recogido: ${item.nombreCompleto()}" + if (n > 1) " ×$n" else ""
        if (item.rareza.ordinal >= JuegoRareza.RARO.ordinal) {
            efecto(jugador, JuegoTipoEfecto.DESTELLO, item.rareza.color, 2)
        }
    }

    private fun recogerBolsa(o: JuegoObjeto, alPisar: Boolean): JuegoObjeto {
        val restantes = mutableListOf<JuegoItem>()
        var tomo = false
        for (item in o.items) {
            val resto = agregarItem(item)
            val n = item.cantidad - resto
            if (n > 0) {
                tomo = true
                notificar(item, n)
            }
            if (resto > 0) restantes.add(item.copy(uid = uid(), cantidad = resto))
        }
        if (restantes.isEmpty()) return o.copy(activo = false, items = emptyList())
        if (!tomo && alPisar) mensaje = "Mochila llena: libera espacio para recoger el botín"
        return o.copy(items = restantes)
    }

    private fun recogerUnico(o: JuegoObjeto): JuegoObjeto {
        val destino = o.pos
        when (o.tipo) {
            JuegoTipoObjeto.TANQUE -> if (!darConsumible(JuegoTipoItem.CON_OXIGENO)) return o
            JuegoTipoObjeto.BOTIQUIN -> if (!darConsumible(JuegoTipoItem.CON_BOTIQUIN)) return o
            JuegoTipoObjeto.TARJETA -> {
                tarjeta = true
                popup(destino, "Tarjeta", JuegoDorado)
                mensaje = "¡Tarjeta de acceso conseguida!"
            }
            JuegoTipoObjeto.ESCUDO -> {
                escudo = true
                popup(destino, "Escudo", JuegoAzul)
                mensaje = "Escudo activado: bloqueará el próximo golpe"
            }
            JuegoTipoObjeto.CRISTAL -> {
                val ganado = ganar(50)
                puntos += ganado
                popup(destino, "+$ganado", JuegoDorado)
                mensaje = "Cristal recogido: +$ganado puntos"
            }
            JuegoTipoObjeto.MODULO -> {
                if (bonoDanio < 3) {
                    bonoDanio += 1
                    popup(destino, "+8% ATQ", JuegoDorado)
                    mensaje = "Módulo de armas: +8% de daño permanente"
                } else {
                    val ganado = ganar(100)
                    puntos += ganado
                    popup(destino, "+$ganado", JuegoDorado)
                    mensaje = "Módulo de armas al límite: +$ganado puntos"
                }
            }
            JuegoTipoObjeto.COFRE -> abrirCofre(destino)
            JuegoTipoObjeto.BOTIN -> {}
        }
        return o.copy(activo = false)
    }

    private fun abrirCofre(destino: JuegoPos) {
        var texto = when (Random.nextInt(5)) {
            0 -> {
                escudo = true
                popup(destino, "Escudo", JuegoAzul)
                "un escudo"
            }
            1 -> {
                if (bonoDanio < 3) {
                    bonoDanio += 1
                    popup(destino, "+8% ATQ", JuegoDorado)
                    "+8% de daño permanente"
                } else {
                    val g = ganar(150)
                    puntos += g
                    popup(destino, "+$g", JuegoDorado)
                    "+$g puntos"
                }
            }
            2 -> {
                val antes = vida
                vida = e.vidaMax
                popup(destino, "+${vida - antes}", JuegoVerde)
                "curación total"
            }
            3 -> {
                val antes = oxigeno
                oxigeno = e.oxigenoMax
                popup(destino, "+${oxigeno - antes} O2", JuegoAzul)
                "tanque de oxígeno lleno"
            }
            else -> {
                val g = ganar(200)
                puntos += g
                popup(destino, "+$g", JuegoDorado)
                "+$g puntos"
            }
        }
        val item = juegoEquipoAleatorio(e.piso, e.dificultad, uid(), 160, JuegoRareza.POCO_COMUN)
        val resto = agregarItem(item)
        if (resto > 0) {
            pendientes.add(JuegoObjeto(-1, JuegoTipoObjeto.BOTIN, destino, true, listOf(item)))
            texto += " (y ${item.nombreCompleto()}: no cabe, queda en el suelo)"
        } else {
            aviso("Cofre: ${item.nombreCompleto()}", item.rareza.color, item.tipo.icono)
            efecto(destino, JuegoTipoEfecto.DESTELLO, item.rareza.color, 2)
            texto += " y ${item.nombreCompleto()}"
        }
        mensaje = "Cofre: $texto"
    }

    private fun recoger(destino: JuegoPos) {
        objetos = objetos.map { o ->
            when {
                !o.activo -> o
                o.tipo == JuegoTipoObjeto.BOTIN ->
                    if (juegoCheb(o.pos, destino) <= 1) recogerBolsa(o, o.pos == destino) else o
                o.pos == destino -> recogerUnico(o)
                else -> o
            }
        }
        if (pendientes.isNotEmpty()) {
            var sig = (objetos.maxOfOrNull { it.id } ?: -1) + 1
            val nuevos = pendientes.map { it.copy(id = sig++) }
            pendientes.clear()
            objetos = objetos + nuevos
        }
    }

    /** Devuelve un estado final si el movimiento termina la acción (puerta, escotilla, banco). */
    fun mover(destino: JuegoPos): JuegoEstadoUi? {
        val idDest = e.idx(destino)
        val tileDest = e.tile(destino)
        val indiceObjetivo = lista.indexOfFirst { it.vivo && it.pos == destino }
        if (indiceObjetivo >= 0) {
            return if (disparar(destino)) null else sinTurno()
        }
        if (tileDest == JuegoTile.PUERTA_CERRADA) {
            if (!tarjeta) {
                popup(destino, "Cerrada", JuegoRojo)
                mensaje = "La puerta está bloqueada. Necesitas la tarjeta"
                return sinTurno()
            }
            tarjeta = false
            val nuevo = mapa.toMutableList()
            nuevo[idDest] = JuegoTile.PUERTA
            mapa = nuevo.toList()
            jugador = destino
            popup(destino, "Abierta", JuegoVerde)
            mensaje = "Tarjeta usada: la puerta se desbloqueó"
            return null
        }
        if (tileDest == JuegoTile.ESCOTILLA) {
            val jefeVivo = lista.any { it.tipo == JuegoTipoEnemigo.CENTINELA && it.vivo }
            if (jefeVivo) {
                popup(destino, "Bloqueada", JuegoRojo)
                mensaje = "El Centinela protege la salida. ¡Derrótalo primero!"
                return sinTurno()
            }
            val victoria = e.piso >= JUEGO_PISOS
            val bono = ganar(100 + oxigeno.coerceAtLeast(0) * 2 + if (victoria) 400 else 0)
            return e.copy(
                jugador = destino,
                oxigeno = oxigeno,
                oxiAcum = oxiAcum,
                fase = if (victoria) JuegoFase.VICTORIA else JuegoFase.MEJORA,
                puntos = e.puntos + bono,
                turno = e.turno + 1,
                mensaje = "Piso completado: +$bono puntos",
                golpes = emptySet(),
                opciones = if (victoria) emptyList() else JuegoMejora.entries.filter { it.disponible(e) }.shuffled().take(3)
            )
        }
        if (!e.libre(destino)) {
            if (idDest in e.muebles) {
                if (e.muebles[idDest] == JuegoMueble.BANCO) {
                    return e.copy(mensaje = "Banco de trabajo: fabrica y mejora tu equipo", abrirBanco = true)
                }
                mensaje = "Hay un mueble en el camino"
                return sinTurno()
            }
            return e
        }
        jugador = destino
        recoger(destino)
        if (e.tile(destino) == JuegoTile.FLUIDO) {
            val coste = (JUEGO_COSTE_FLUIDO * (100 - traje.toxica) + 99) / 100
            if (coste > 0) {
                oxigeno -= coste
                popup(destino, "-$coste O2", JuegoAzul)
                mensaje = "Fluido tóxico: -$coste de oxígeno extra"
            } else {
                mensaje = "Tu traje es inmune al fluido tóxico"
            }
        }
        if (idDest in trampas) {
            trampas = trampas - idDest
            val antes = perdidaVida
            recibirDanio(JUEGO_DANIO_TRAMPA, JuegoTipoDanio.CINETICO, false)
            mensaje = if (perdidaVida > antes) {
                "¡Pisaste una trampa! -${perdidaVida - antes} de vida"
            } else {
                "El escudo absorbió una trampa"
            }
        }
        return null
    }

    // ── enemigos ──

    private fun avanzar(
        en: JuegoEnemigo,
        tmp: JuegoEstadoUi,
        dist: IntArray,
        ocupadas: MutableSet<Int>,
        errantePct: Int
    ): JuegoEnemigo {
        val idEn = tmp.idx(en.pos)
        val actual = dist[idEn]
        var nueva: JuegoPos? = null
        val vecinos = JUEGO_VECINOS8.shuffled()
        if ((errantePct > 0 && Random.nextInt(100) < errantePct) || actual == -1) {
            for (d in vecinos) {
                val b = JuegoPos(en.pos.fila + d.fila, en.pos.col + d.col)
                if (nueva == null && tmp.libre(b) && tmp.idx(b) !in ocupadas && b != jugador &&
                    juegoContacto(tmp, en.pos, b)
                ) nueva = b
            }
        } else {
            var mejor = actual
            for (d in vecinos) {
                val b = JuegoPos(en.pos.fila + d.fila, en.pos.col + d.col)
                if (!tmp.libre(b) || tmp.idx(b) in ocupadas || b == jugador) continue
                if (!juegoContacto(tmp, en.pos, b)) continue
                val db = dist[tmp.idx(b)]
                if (db != -1 && db < mejor) {
                    mejor = db
                    nueva = b
                }
            }
        }
        if (nueva == null) return en
        ocupadas.remove(idEn)
        ocupadas.add(tmp.idx(nueva))
        return en.copy(pos = nueva)
    }

    private fun retroceder(en: JuegoEnemigo, tmp: JuegoEstadoUi, ocupadas: MutableSet<Int>): JuegoEnemigo {
        val idEn = tmp.idx(en.pos)
        var mejor = juegoCheb(en.pos, jugador)
        var nueva: JuegoPos? = null
        for (d in JUEGO_VECINOS8.shuffled()) {
            val b = JuegoPos(en.pos.fila + d.fila, en.pos.col + d.col)
            if (!tmp.libre(b) || tmp.idx(b) in ocupadas || b == jugador) continue
            if (!juegoContacto(tmp, en.pos, b)) continue
            val db = juegoCheb(b, jugador)
            if (db > mejor) {
                mejor = db
                nueva = b
            }
        }
        if (nueva == null) return en
        ocupadas.remove(idEn)
        ocupadas.add(tmp.idx(nueva))
        return en.copy(pos = nueva)
    }

    private fun golpeCuerpo(en: JuegoEnemigo, tipo: JuegoTipoDanio = en.tipo.danioTipo, danio: Int = en.danio) {
        recibirDanio(danio, tipo)
        efecto(jugador, JuegoTipoEfecto.IMPACTO, JuegoRojo)
    }

    private fun disparoEnemigo(en: JuegoEnemigo, desde: JuegoPos) {
        val a = juegoAtaque(en)
        val acierta = Random.nextInt(100) < a.precision
        disparos.add(JuegoDisparo(idDisparo, desde, jugador, a.tipo, acierta, disparos.size * 90, true))
        idDisparo += 1
        if (acierta) recibirDanio(a.danio, a.tipo) else popup(jugador, "Falla", JuegoTextoSuave)
    }

    private fun invocar(boss: JuegoEnemigo, tmp: JuegoEstadoUi, ocupadas: MutableSet<Int>) {
        for (d in JUEGO_VECINOS8.shuffled()) {
            val p = JuegoPos(boss.pos.fila + d.fila, boss.pos.col + d.col)
            if (tmp.libre(p) && tmp.idx(p) !in ocupadas && p != jugador) {
                val nuevoId = (lista.maxOfOrNull { it.id } ?: 0) + 1
                lista.add(juegoCrearEnemigo(nuevoId, JuegoTipoEnemigo.ROBOT, p, e.piso, e.dificultad) { uid() }.copy(despierto = true))
                ocupadas.add(tmp.idx(p))
                invocados += 1
                popup(boss.pos, "¡Refuerzos!", JuegoRojo)
                efecto(p, JuegoTipoEfecto.DESTELLO, JuegoRojo)
                mensaje = "¡El Centinela invoca un robot!"
                return
            }
        }
    }

    private fun turnoJefe(
        i: Int,
        en: JuegoEnemigo,
        tmp: JuegoEstadoUi,
        dist: IntArray,
        ocupadas: MutableSet<Int>,
        linea: Boolean,
        d: Int
    ) {
        val fase = juegoFaseJefe(en)
        if (fase == 1 && turno % 2 != 0) return
        val c = en.carga + 1
        val base = en.copy(carga = c)
        lista[i] = base
        if (fase >= 2 && c % 5 == 4) {
            popup(en.pos, "¡Carga!", JuegoAmarillo)
            efecto(en.pos, JuegoTipoEfecto.ADVERTENCIA, JuegoRojo, 2)
            mensaje = "¡El Centinela concentra energía! Aléjate"
            return
        }
        if (fase >= 2 && c % 5 == 0) {
            efecto(en.pos, JuegoTipoEfecto.EXPLOSION, JuegoRojo, 2)
            if (d <= 2) recibirDanio(base.danio + 1, JuegoTipoDanio.ELECTRICO) else popup(en.pos, "Onda", JuegoAzul)
            mensaje = "¡Onda de choque del Centinela!"
            return
        }
        if (fase == 3 && c % 7 == 3 && invocados < 3) {
            invocar(base, tmp, ocupadas)
            return
        }
        if (juegoContacto(tmp, en.pos, jugador)) {
            golpeCuerpo(base, JuegoTipoDanio.CINETICO, base.danio + if (fase == 3) 1 else 0)
            return
        }
        if (linea && d <= 5 && c % 3 == 0) {
            val acierta = Random.nextInt(100) < en.tipo.precision
            disparos.add(JuegoDisparo(idDisparo, en.pos, jugador, JuegoTipoDanio.PLASMA, acierta, disparos.size * 90, true))
            idDisparo += 1
            if (acierta) recibirDanio(maxOf(2, base.danio - 1), JuegoTipoDanio.PLASMA) else popup(jugador, "Falla", JuegoTextoSuave)
            return
        }
        lista[i] = avanzar(base, tmp, dist, ocupadas, 0)
    }

    fun faseEnemigos(visiblesNuevos: Set<Int>) {
        val tmp = e.copy(mapa = mapa, jugador = jugador, enemigos = lista.toList())
        val dist = juegoMapaDistancias(tmp, jugador)
        val ocupadas = mutableSetOf<Int>()
        lista.forEach { if (it.vivo) ocupadas.add(tmp.idx(it.pos)) }
        val indices = lista.indices.toList()
        for (i in indices) {
            val en = lista[i]
            if (!en.vivo) continue
            val idEn = tmp.idx(en.pos)
            val ve = idEn in visiblesNuevos
            if (!en.despierto) {
                if (ve && Random.nextInt(100) < 70) lista[i] = en.copy(despierto = true)
                continue
            }
            if (en.aturdido > 0) {
                lista[i] = en.copy(aturdido = en.aturdido - 1)
                continue
            }
            if (en.tipo.periodo > 1 && turno % en.tipo.periodo != 0) continue
            val d = juegoCheb(en.pos, jugador)
            val linea = ve && juegoLineaLibre(mapa, e.ancho, en.pos, jugador)
            when (en.tipo.ia) {
                JuegoIA.CUERPO -> {
                    if (juegoContacto(tmp, en.pos, jugador)) golpeCuerpo(en)
                    else lista[i] = avanzar(en, tmp, dist, ocupadas, en.tipo.errante)
                }
                JuegoIA.DISTANCIA -> {
                    val a = juegoAtaque(en)
                    if (linea && d <= a.alcance) {
                        if (d <= 2 && Random.nextInt(100) < 55) {
                            val r = retroceder(en, tmp, ocupadas)
                            if (r.pos != en.pos) lista[i] = r else disparoEnemigo(en, en.pos)
                        } else {
                            disparoEnemigo(en, en.pos)
                        }
                    } else {
                        lista[i] = avanzar(en, tmp, dist, ocupadas, 0)
                    }
                }
                JuegoIA.ESTATICO -> {
                    val a = juegoAtaque(en)
                    if (linea && d <= a.alcance && turno % 2 == 0) disparoEnemigo(en, en.pos)
                }
                JuegoIA.DRON -> {
                    var actual = en
                    var actuo = false
                    val pasos = if (en.tipo == JuegoTipoEnemigo.DRON_ATAQUE) 2 else 1
                    repeat(pasos) {
                        if (!actuo) {
                            val dd = juegoCheb(actual.pos, jugador)
                            val a = juegoAtaque(actual)
                            val lin = ve && juegoLineaLibre(mapa, e.ancho, actual.pos, jugador)
                            if (juegoContacto(tmp, actual.pos, jugador)) {
                                golpeCuerpo(actual)
                                actuo = true
                            } else if (lin && dd in 2..a.alcance) {
                                disparoEnemigo(actual, actual.pos)
                                actuo = true
                            } else {
                                actual = avanzar(actual, tmp, dist, ocupadas, 0)
                            }
                        }
                    }
                    lista[i] = actual
                }
                JuegoIA.JEFE -> turnoJefe(i, en, tmp, dist, ocupadas, linea, d)
            }
        }
    }

    // ── cierre del turno ──

    fun cerrarTurno(cmd: JuegoComando): JuegoEstadoUi {
        val visiblesNuevos = juegoCalcularVista(mapa, e.ancho, e.alto, jugador)
        if (e.recarga > 0) {
            recarga = (recarga - 1).coerceAtLeast(0)
            if (recarga == 0) completarRecarga()
        }
        faseEnemigos(visiblesNuevos)

        if (perdidaEscudo > 0) popup(jugador, "-$perdidaEscudo", JuegoAzul)
        if (bloqueos > 0) popup(jugador, "Escudo", JuegoAzul)
        if (perdidaVida > 0) {
            golpes.add(jugador)
            popup(jugador, "-$perdidaVida", JuegoRojo)
        }
        if (atacado) {
            mensaje = when {
                perdidaVida > 0 -> "¡Te atacaron! -$perdidaVida de vida"
                perdidaEscudo > 0 || bloqueos > 0 -> "¡El escudo absorbió el golpe!"
                else -> mensaje
            }
        }

        if (traje.escudoMax > 0 && perdidaVida + perdidaEscudo + bloqueos == 0) {
            if (escudoPts < traje.escudoMax) {
                escudoRegen += 1
                if (escudoRegen >= traje.regen) {
                    escudoPts += 1
                    escudoRegen = 0
                }
            }
        } else {
            escudoRegen = 0
        }

        if (cmd != JuegoComando.PULSO) pulsoEspera = (pulsoEspera - 1).coerceAtLeast(0)
        if (turno - ultimoKill > JUEGO_VENTANA_COMBO) combo = 0

        vida = vida.coerceAtLeast(0)
        oxigeno = oxigeno.coerceAtLeast(0)
        var fase = JuegoFase.JUGANDO
        if (vida <= 0) {
            fase = JuegoFase.GAME_OVER
            mensaje = "Tu traje no resistió el daño"
        } else if (oxigeno <= 0) {
            fase = JuegoFase.GAME_OVER
            mensaje = "Te quedaste sin oxígeno"
        }

        return e.copy(
            fase = fase,
            mapa = mapa,
            jugador = jugador,
            vida = vida,
            oxigeno = oxigeno,
            tieneTarjeta = tarjeta,
            escudo = escudo,
            escudoPts = escudoPts,
            escudoRegen = escudoRegen,
            bonoDanio = bonoDanio,
            pulsoEspera = pulsoEspera,
            pulsoContador = pulsoContador,
            inventario = inventario,
            equipo = equipo,
            itemId = itemId,
            recarga = recarga,
            oxiAcum = oxiAcum,
            objetos = objetos,
            enemigos = lista.toList(),
            trampas = trampas,
            visibles = visiblesNuevos,
            explorado = e.explorado + visiblesNuevos,
            mensaje = mensaje,
            golpes = golpes.toSet(),
            golpeContador = e.golpeContador + if (golpes.isNotEmpty()) 1 else 0,
            popups = popups.toList(),
            popupId = idPopup,
            disparos = disparos.toList(),
            disparoId = idDisparo,
            efectos = efectos.toList(),
            efectoId = idEfecto,
            avisos = avisos.toList(),
            avisoId = idAviso,
            puntos = puntos,
            derrotados = derrotados,
            combo = combo,
            comboMax = comboMax,
            ultimoKill = ultimoKill,
            invocados = invocados,
            turno = turno,
            abrirBanco = false
        )
    }
}

// ───────────────────────── Acción de turno ─────────────────────────

internal fun juegoAccion(
    e: JuegoEstadoUi,
    cmd: JuegoComando,
    destino: JuegoPos = e.jugador,
    uid: Int = -1
): JuegoEstadoUi {
    if (e.fase != JuegoFase.JUGANDO) return e
    if (cmd == JuegoComando.PULSO && e.pulsoEspera > 0) return e
    if (cmd == JuegoComando.MOVER) {
        if (!e.dentro(destino) || juegoCheb(e.jugador, destino) != 1) return e
        val hayEnemigo = e.enemigos.any { it.vivo && it.pos == destino }
        if (!hayEnemigo && !juegoContacto(e, e.jugador, destino)) {
            return e.copy(mensaje = "No puedes cortar la esquina en diagonal")
        }
    }

    val c = JuegoCtx(e)
    c.consumirOxigeno()
    var consumioTurno = true
    when (cmd) {
        JuegoComando.ESPERAR -> c.mensaje = "Esperas y escuchas..."
        JuegoComando.PULSO -> c.pulso()
        JuegoComando.RECARGAR -> consumioTurno = c.iniciarRecarga()
        JuegoComando.USAR -> consumioTurno = c.usar(uid)
        JuegoComando.DISPARAR -> consumioTurno = c.disparar(destino)
        JuegoComando.MOVER -> {
            val r = c.mover(destino)
            if (r != null) return r
        }
    }
    if (!consumioTurno) return c.sinTurno()
    return c.cerrarTurno(cmd)
}
