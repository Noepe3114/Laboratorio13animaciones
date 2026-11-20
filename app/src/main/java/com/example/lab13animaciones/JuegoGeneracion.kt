@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

internal fun juegoNombrePiso(piso: Int): String = JuegoNombresPiso.getOrElse(piso - 1) { "Cubierta $piso" }

internal fun juegoCheb(a: JuegoPos, b: JuegoPos): Int = maxOf(abs(a.fila - b.fila), abs(a.col - b.col))

internal fun JuegoEstadoUi.idx(p: JuegoPos): Int = p.fila * ancho + p.col

internal fun JuegoEstadoUi.dentro(p: JuegoPos): Boolean = p.fila in 0 until alto && p.col in 0 until ancho

internal fun JuegoEstadoUi.tile(p: JuegoPos): JuegoTile = if (dentro(p)) mapa[idx(p)] else JuegoTile.VACIO

internal fun JuegoEstadoUi.libre(p: JuegoPos): Boolean =
    dentro(p) && mapa[idx(p)].camina && idx(p) !in muebles

internal fun juegoContacto(e: JuegoEstadoUi, a: JuegoPos, b: JuegoPos): Boolean {
    if (juegoCheb(a, b) != 1) return false
    if (a.fila == b.fila || a.col == b.col) return true
    return e.libre(JuegoPos(a.fila, b.col)) && e.libre(JuegoPos(b.fila, a.col))
}

internal fun juegoNombreSala(e: JuegoEstadoUi): String {
    val s = e.salas.firstOrNull {
        e.jugador.col in it.rect.x until it.rect.x + it.rect.w &&
                e.jugador.fila in it.rect.y until it.rect.y + it.rect.h
    } ?: return "Pasillo"
    return if (s.tipo == JuegoSala.MANDO && e.piso >= JUEGO_PISOS) "Cápsula de escape" else s.tipo.nombre
}

internal fun juegoEnemigosVisibles(e: JuegoEstadoUi): Set<Int> =
    e.enemigos.filter { it.vivo && e.idx(it.pos) in e.visibles }.map { it.id }.toSet()

internal fun juegoLineaLibre(mapa: List<JuegoTile>, ancho: Int, a: JuegoPos, b: JuegoPos): Boolean {
    var x = a.col
    var y = a.fila
    val dx = abs(b.col - a.col)
    val dy = abs(b.fila - a.fila)
    val sx = if (a.col < b.col) 1 else -1
    val sy = if (a.fila < b.fila) 1 else -1
    var err = dx - dy
    repeat(400) {
        if (x == b.col && y == b.fila) return true
        if (!(x == a.col && y == a.fila) && mapa[y * ancho + x].bloqueaVista) return false
        val e2 = 2 * err
        if (e2 > -dy) {
            err -= dy
            x += sx
        }
        if (e2 < dx) {
            err += dx
            y += sy
        }
    }
    return true
}

internal fun juegoCalcularVista(mapa: List<JuegoTile>, ancho: Int, alto: Int, origen: JuegoPos): Set<Int> {
    val res = mutableSetOf<Int>()
    val r = JUEGO_RADIO_VISION
    for (f in origen.fila - r..origen.fila + r) {
        for (c in origen.col - r..origen.col + r) {
            if (f < 0 || c < 0 || f >= alto || c >= ancho) continue
            val dx = c - origen.col
            val dy = f - origen.fila
            if (dx * dx + dy * dy > r * r + r) continue
            if (juegoLineaLibre(mapa, ancho, origen, JuegoPos(f, c))) res.add(f * ancho + c)
        }
    }
    return res
}

internal fun juegoMapaDistancias(e: JuegoEstadoUi, origen: JuegoPos): IntArray {
    val dist = IntArray(e.ancho * e.alto) { -1 }
    val cola = ArrayDeque<JuegoPos>()
    dist[e.idx(origen)] = 0
    cola.add(origen)
    while (cola.isNotEmpty()) {
        val a = cola.removeFirst()
        val da = dist[e.idx(a)]
        if (da >= JUEGO_LIMITE_DIST) continue
        for (d in JUEGO_VECINOS8) {
            val b = JuegoPos(a.fila + d.fila, a.col + d.col)
            if (!e.libre(b) || dist[e.idx(b)] != -1) continue
            if (!juegoContacto(e, a, b)) continue
            dist[e.idx(b)] = da + 1
            cola.add(b)
        }
    }
    return dist
}


internal fun juegoAlcanzables(e: JuegoEstadoUi, abrir: Boolean): Set<Int> {
    val vistos = mutableSetOf<Int>()
    val cola = ArrayDeque<JuegoPos>()
    vistos.add(e.idx(e.jugador))
    cola.add(e.jugador)
    while (cola.isNotEmpty()) {
        val a = cola.removeFirst()
        for (d in JUEGO_VECINOS8) {
            val b = JuegoPos(a.fila + d.fila, a.col + d.col)
            if (!e.dentro(b)) continue
            val t = e.tile(b)
            val pasa = (t.camina || (abrir && t == JuegoTile.PUERTA_CERRADA)) && e.idx(b) !in e.muebles
            if (!pasa || e.idx(b) in vistos) continue
            if (!juegoContacto(e, a, b)) continue
            vistos.add(e.idx(b))
            cola.add(b)
        }
    }
    return vistos
}

internal fun juegoSeparados(a: JuegoRect, b: JuegoRect): Boolean =
    a.x + a.w + JUEGO_SEPARACION <= b.x || b.x + b.w + JUEGO_SEPARACION <= a.x ||
            a.y + a.h + JUEGO_SEPARACION <= b.y || b.y + b.h + JUEGO_SEPARACION <= a.y

internal fun juegoPuertaHacia(a: JuegoRect, b: JuegoRect): Pair<JuegoPos, JuegoPos> {
    val dx = (b.x + b.w / 2) - (a.x + a.w / 2)
    val dy = (b.y + b.h / 2) - (a.y + a.h / 2)
    return if (abs(dx) >= abs(dy)) {
        val f = Random.nextInt(a.y, a.y + a.h)
        if (dx > 0) Pair(JuegoPos(f, a.x + a.w), JuegoPos(f, a.x + a.w + 1))
        else Pair(JuegoPos(f, a.x - 1), JuegoPos(f, a.x - 2))
    } else {
        val c = Random.nextInt(a.x, a.x + a.w)
        if (dy > 0) Pair(JuegoPos(a.y + a.h, c), JuegoPos(a.y + a.h + 1, c))
        else Pair(JuegoPos(a.y - 1, c), JuegoPos(a.y - 2, c))
    }
}

internal fun juegoCavarPasillo(
    zona: BooleanArray,
    ancho: Int,
    alto: Int,
    desde: JuegoPos,
    hasta: JuegoPos
): List<JuegoPos>? {
    val padre = IntArray(ancho * alto) { -2 }
    val cola = ArrayDeque<Int>()
    val ini = desde.fila * ancho + desde.col
    val fin = hasta.fila * ancho + hasta.col
    padre[ini] = -1
    cola.add(ini)
    val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
    while (cola.isNotEmpty()) {
        val a = cola.removeFirst()
        if (a == fin) break
        val f = a / ancho
        val c = a % ancho
        for (d in dirs.shuffled()) {
            val ff = f + d.first
            val cc = c + d.second
            if (ff < 1 || cc < 1 || ff > alto - 2 || cc > ancho - 2) continue
            val b = ff * ancho + cc
            if (padre[b] != -2 || zona[b]) continue
            padre[b] = a
            cola.add(b)
        }
    }
    if (padre[fin] == -2) return null
    val camino = mutableListOf<JuegoPos>()
    var actual = fin
    while (actual != -1) {
        camino.add(JuegoPos(actual / ancho, actual % ancho))
        actual = padre[actual]
    }
    return camino
}


internal fun juegoConstruirPisoInterno(piso: Int, previo: JuegoEstadoUi?, dificultad: JuegoDificultad): JuegoEstadoUi? {
    val dif = previo?.dificultad ?: dificultad
    val ancho = 26 + piso * 2
    val alto = 30 + piso * 2
    val objetivo = 6 + piso

    val rects = mutableListOf<JuegoRect>()
    var intentos = 0
    while (rects.size < objetivo && intentos < 900) {
        intentos++
        val w = Random.nextInt(4, 9)
        val h = Random.nextInt(4, 8)
        val x = Random.nextInt(3, ancho - w - 2)
        val y = Random.nextInt(3, alto - h - 2)
        val r = JuegoRect(x, y, w, h)
        if (rects.all { juegoSeparados(it, r) }) rects.add(r)
    }
    if (rects.size < 6) return null

    val n = rects.size
    val centros = rects.map { JuegoPos(it.y + it.h / 2, it.x + it.w / 2) }
    fun distCentros(i: Int, j: Int): Int =
        abs(centros[i].fila - centros[j].fila) + abs(centros[i].col - centros[j].col)

    val conectadas = mutableSetOf(0)
    val aristas = mutableListOf<Pair<Int, Int>>()
    while (conectadas.size < n) {
        var mejorA = -1
        var mejorB = -1
        var mejorD = Int.MAX_VALUE
        for (a in conectadas) {
            for (b in 0 until n) {
                if (b in conectadas) continue
                val d = distCentros(a, b)
                if (d < mejorD) {
                    mejorD = d
                    mejorA = a
                    mejorB = b
                }
            }
        }
        aristas.add(Pair(mejorA, mejorB))
        conectadas.add(mejorB)
    }

    val grado = IntArray(n)
    aristas.forEach {
        grado[it.first]++
        grado[it.second]++
    }
    val hojas = (1 until n).filter { grado[it] == 1 }
    val salida = hojas.maxByOrNull { distCentros(0, it) } ?: (n - 1)

    var extras = 0
    for (a in 0 until n) {
        for (b in a + 1 until n) {
            if (extras >= 3 || a == salida || b == salida) continue
            val existe = aristas.any { (it.first == a && it.second == b) || (it.first == b && it.second == a) }
            if (!existe && distCentros(a, b) < 16 && Random.nextInt(100) < 30) {
                aristas.add(Pair(a, b))
                extras++
            }
        }
    }

    val tiposLibres = JuegoSala.entries.filter { it != JuegoSala.ESCLUSA && it != JuegoSala.MANDO }.shuffled()
    val tipos = Array(n) { JuegoSala.OFICINA }
    var k = 0
    for (i in 0 until n) {
        tipos[i] = when (i) {
            0 -> JuegoSala.ESCLUSA
            salida -> JuegoSala.MANDO
            else -> {
                val t = tiposLibres[k % tiposLibres.size]
                k++
                t
            }
        }
    }

    val zona = BooleanArray(ancho * alto)
    for (r in rects) {
        for (f in r.y - 1..r.y + r.h) {
            for (c in r.x - 1..r.x + r.w) zona[f * ancho + c] = true
        }
    }

    val tiles = Array(ancho * alto) { JuegoTile.VACIO }
    for (i in 0 until n) {
        val r = rects[i]
        for (f in r.y until r.y + r.h) {
            for (c in r.x until r.x + r.w) tiles[f * ancho + c] = tipos[i].suelo
        }
    }

    val puertas = mutableListOf<JuegoPos>()
    var puertaSalida = JuegoPos(-1, -1)
    for ((a, b) in aristas) {
        val (da, pa) = juegoPuertaHacia(rects[a], rects[b])
        val (db, pb) = juegoPuertaHacia(rects[b], rects[a])
        val camino = juegoCavarPasillo(zona, ancho, alto, pa, pb) ?: return null
        for (p in camino) {
            if (tiles[p.fila * ancho + p.col] == JuegoTile.VACIO) tiles[p.fila * ancho + p.col] = JuegoTile.PASILLO
        }
        puertas.add(da)
        puertas.add(db)
        if (a == salida) puertaSalida = da
        if (b == salida) puertaSalida = db
    }
    for (p in puertas) tiles[p.fila * ancho + p.col] = JuegoTile.PUERTA
    if (puertaSalida.fila < 0) return null
    tiles[puertaSalida.fila * ancho + puertaSalida.col] = JuegoTile.PUERTA_CERRADA

    val paraPared = mutableListOf<Int>()
    for (f in 0 until alto) {
        for (c in 0 until ancho) {
            if (tiles[f * ancho + c] != JuegoTile.VACIO) continue
            var cerca = false
            for (df in -1..1) {
                for (dc in -1..1) {
                    val ff = f + df
                    val cc = c + dc
                    if (ff in 0 until alto && cc in 0 until ancho) {
                        val t = tiles[ff * ancho + cc]
                        if (t != JuegoTile.VACIO && t != JuegoTile.PARED) cerca = true
                    }
                }
            }
            if (cerca) paraPared.add(f * ancho + c)
        }
    }
    paraPared.forEach { tiles[it] = JuegoTile.PARED }

    for (r in rects) {
        for (c in r.x until r.x + r.w) {
            val id = (r.y - 1) * ancho + c
            if (tiles[id] == JuegoTile.PARED && c % 2 == 0 && Random.nextInt(100) < 55) tiles[id] = JuegoTile.VENTANA
        }
    }

    val muebles = mutableMapOf<Int, JuegoMueble>()
    fun poner(f: Int, c: Int, m: JuegoMueble) {
        muebles[f * ancho + c] = m
    }
    for (i in 0 until n) {
        val r = rects[i]
        val cx0 = r.x + 1
        val cy0 = r.y + 1
        val cw = r.w - 2
        val ch = r.h - 2
        when (tipos[i]) {
            JuegoSala.OFICINA -> {
                for (f in cy0 until cy0 + ch) {
                    for (c in cx0 until cx0 + cw) {
                        if ((f - cy0) % 2 == 0 && (c - cx0) % 2 == 0) poner(f, c, JuegoMueble.ESCRITORIO)
                    }
                }
            }
            JuegoSala.LABORATORIO -> {
                val fm = cy0 + ch / 2
                for (c in cx0 until cx0 + cw) {
                    if ((c - cx0) % 3 != 2) poner(fm, c, JuegoMueble.MESA)
                }
                poner(cy0, cx0, JuegoMueble.TUBO)
                poner(cy0, cx0 + cw - 1, JuegoMueble.TUBO)
            }
            JuegoSala.REACTOR -> {
                val nf = cy0 + (ch - 2) / 2
                val nc = cx0 + (cw - 2) / 2
                for (df in 0..1) {
                    for (dc in 0..1) poner(nf + df, nc + dc, JuegoMueble.NUCLEO)
                }
                repeat(5) {
                    val f = Random.nextInt(r.y, r.y + r.h)
                    val c = Random.nextInt(r.x, r.x + r.w)
                    if ((f * ancho + c) !in muebles) tiles[f * ancho + c] = JuegoTile.FLUIDO
                }
            }
            JuegoSala.ENFERMERIA -> {
                for (c in cx0 until cx0 + cw) {
                    if ((c - cx0) % 2 == 0) {
                        poner(cy0, c, JuegoMueble.CAMA)
                        if (ch >= 3) poner(cy0 + ch - 1, c, JuegoMueble.CAMA)
                    }
                }
            }
            JuegoSala.ALMACEN -> {
                for (f in cy0 until cy0 + ch) {
                    for (c in cx0 until cx0 + cw) {
                        if (Random.nextInt(100) < 35) poner(f, c, JuegoMueble.CAJAS)
                    }
                }
            }
            JuegoSala.DORMITORIO -> {
                for (f in cy0 until cy0 + ch) {
                    for (c in cx0 until cx0 + cw) {
                        if ((f - cy0) % 2 == 0 && (c - cx0) % 2 == 0) poner(f, c, JuegoMueble.CAMA)
                    }
                }
            }
            JuegoSala.COMEDOR -> {
                for (f in cy0 until cy0 + ch) {
                    for (c in cx0 until cx0 + cw) {
                        if ((f - cy0) % 2 == 0 && (c - cx0) % 3 != 2) poner(f, c, JuegoMueble.MESA)
                    }
                }
            }
            JuegoSala.MANDO -> {
                for (c in cx0 until cx0 + cw) {
                    if ((c - cx0) % 2 == 0) poner(cy0, c, JuegoMueble.CONSOLA)
                }
            }
            JuegoSala.ESCLUSA -> poner(cy0, cx0, JuegoMueble.BANCO)
            else -> {}
        }
    }

    val rs = rects[salida]
    val escotilla = JuegoPos(rs.y + rs.h / 2, rs.x + rs.w / 2)
    muebles.remove(escotilla.fila * ancho + escotilla.col)
    tiles[escotilla.fila * ancho + escotilla.col] = JuegoTile.ESCOTILLA

    val usadas = mutableSetOf<Int>()
    fun celdasLibres(i: Int): List<JuegoPos> {
        val r = rects[i]
        val lista = mutableListOf<JuegoPos>()
        for (f in r.y until r.y + r.h) {
            for (c in r.x until r.x + r.w) {
                val id = f * ancho + c
                if (tiles[id].suelo && id !in muebles && id !in usadas) lista.add(JuegoPos(f, c))
            }
        }
        return lista
    }
    fun tomar(i: Int): JuegoPos {
        val p = celdasLibres(i).random()
        usadas.add(p.fila * ancho + p.col)
        return p
    }

    val inicio = tomar(0)

    val objetos = mutableListOf<JuegoObjeto>()
    fun agregar(tipo: JuegoTipoObjeto, sala: Int) {
        objetos.add(JuegoObjeto(objetos.size, tipo, tomar(sala)))
    }
    val salasLoot = (1 until n).filter { it != salida }
    val salasObjetos = (0 until n).filter { it != salida }
    agregar(JuegoTipoObjeto.TARJETA, salasLoot.random())
    repeat(3 + piso / 2) { agregar(JuegoTipoObjeto.TANQUE, salasObjetos.random()) }
    repeat(1 + piso / 3) { agregar(JuegoTipoObjeto.BOTIQUIN, salasObjetos.random()) }
    repeat(3 + piso / 2) { agregar(JuegoTipoObjeto.CRISTAL, salasObjetos.random()) }
    if (piso >= 2) agregar(JuegoTipoObjeto.ESCUDO, salasLoot.random())
    if (piso % 2 == 0) agregar(JuegoTipoObjeto.MODULO, salasLoot.random())
    agregar(JuegoTipoObjeto.COFRE, salasLoot.random())

    var idItem = previo?.itemId ?: 10
    fun bolsa(item: JuegoItem, sala: Int) {
        objetos.add(JuegoObjeto(objetos.size, JuegoTipoObjeto.BOTIN, tomar(sala), true, listOf(item)))
    }
    repeat(2 + piso / 2) { bolsa(juegoMunicionAleatoria(piso, idItem++), salasObjetos.random()) }
    repeat(1 + piso / 2) { bolsa(juegoEquipoAleatorio(piso, dif, idItem++), salasLoot.random()) }
    repeat(2) {
        val mat = if (Random.nextBoolean()) JuegoTipoItem.MAT_CHATARRA else JuegoTipoItem.MAT_CIRCUITOS
        bolsa(juegoCrearMaterial(mat, Random.nextInt(2, 5), idItem++), salasObjetos.random())
    }

    val tiposEnemigo = mutableListOf<JuegoTipoEnemigo>()
    val robots = if (piso >= JUEGO_PISOS) piso - 2 + dif.extraEnemigos else piso + 1 + dif.extraEnemigos
    repeat(robots) { tiposEnemigo.add(JuegoTipoEnemigo.ROBOT) }
    repeat((piso + 1) / 2) { tiposEnemigo.add(JuegoTipoEnemigo.ALIEN) }
    if (piso >= 2) repeat(piso / 2) { tiposEnemigo.add(JuegoTipoEnemigo.DRON) }
    if (piso >= 3) repeat((piso - 1) / 2) { tiposEnemigo.add(JuegoTipoEnemigo.TORRETA) }
    for (i in tiposEnemigo.indices) tiposEnemigo[i] = juegoVariante(tiposEnemigo[i], piso)

    val enemigos = mutableListOf<JuegoEnemigo>()
    for (tipo in tiposEnemigo) {
        var colocado = false
        var t = 0
        while (!colocado && t < 30) {
            t++
            val sala = salasLoot.random()
            val candidatas = celdasLibres(sala).filter { juegoCheb(it, inicio) >= 6 }
            if (candidatas.isNotEmpty()) {
                val p = candidatas.random()
                usadas.add(p.fila * ancho + p.col)
                enemigos.add(juegoCrearEnemigo(enemigos.size, tipo, p, piso, dif) { idItem++ })
                colocado = true
            }
        }
        if (!colocado) return null
    }
    if (piso >= JUEGO_PISOS) {
        val p = celdasLibres(salida).random()
        usadas.add(p.fila * ancho + p.col)
        enemigos.add(juegoCrearEnemigo(enemigos.size, JuegoTipoEnemigo.CENTINELA, p, piso, dif) { idItem++ })
    }

    val cercaPuertas = mutableSetOf<Int>()
    for (p in puertas) {
        for (df in -2..2) {
            for (dc in -2..2) {
                val ff = p.fila + df
                val cc = p.col + dc
                if (ff in 0 until alto && cc in 0 until ancho) cercaPuertas.add(ff * ancho + cc)
            }
        }
    }
    val candidatasTrampa = mutableListOf<Int>()
    for (f in 0 until alto) {
        for (c in 0 until ancho) {
            val id = f * ancho + c
            val t = tiles[id]
            if (t.suelo && t != JuegoTile.PASILLO && t != JuegoTile.FLUIDO && id !in muebles && id !in usadas &&
                id !in cercaPuertas && juegoCheb(JuegoPos(f, c), inicio) >= 4
            ) candidatasTrampa.add(id)
        }
    }
    val trampas = candidatasTrampa.shuffled().take(piso + 3).toSet()

    val inicial = if (previo == null) juegoInicial() else null
    val equipo = previo?.equipo ?: inicial!!.first
    val inventario = previo?.inventario ?: inicial!!.second
    val bonoVida = previo?.bonoVida ?: 0
    val bonoOx = previo?.bonoOxigeno ?: 0
    val traje = juegoTraje(equipo)
    val vidaMax = juegoVidaMax(dif, bonoVida, traje)
    val oxMax = juegoOxigenoMax(dif, bonoOx, traje)
    val salas = rects.mapIndexed { i, r -> JuegoSalaInfo(r, tipos[i]) }
    val intro = if (piso >= JUEGO_PISOS) "Piso $piso: consigue la tarjeta y derrota al Centinela"
    else "Piso $piso: consigue la tarjeta y abre la sala de mando"

    val base = JuegoEstadoUi(
        piso = piso,
        ancho = ancho,
        alto = alto,
        dificultad = dif,
        mapa = tiles.toList(),
        muebles = muebles.toMap(),
        salas = salas,
        jugador = inicio,
        vida = if (previo == null) vidaMax else minOf(vidaMax, previo.vida + 4),
        vidaMax = vidaMax,
        oxigeno = oxMax,
        oxigenoMax = oxMax,
        escudo = previo?.escudo ?: false,
        escudoPts = traje.escudoMax,
        bonoDanio = previo?.bonoDanio ?: 0,
        bonoCritico = previo?.bonoCritico ?: 0,
        bonoVida = bonoVida,
        bonoOxigeno = bonoOx,
        pulsoRecarga = previo?.pulsoRecarga ?: JUEGO_RECARGA_PULSO,
        equipo = equipo,
        inventario = inventario,
        itemId = idItem,
        recarga = 0,
        objetos = objetos,
        enemigos = enemigos,
        trampas = trampas,
        mensaje = intro,
        puntos = previo?.puntos ?: 0,
        derrotados = previo?.derrotados ?: 0,
        comboMax = previo?.comboMax ?: 0,
        turno = previo?.turno ?: 0
    )

    val r1 = juegoAlcanzables(base, false)
    val r2 = juegoAlcanzables(base, true)
    if (objetos.any { base.idx(it.pos) !in r1 }) return null
    if (enemigos.any { it.tipo != JuegoTipoEnemigo.CENTINELA && base.idx(it.pos) !in r1 }) return null
    if (enemigos.any { base.idx(it.pos) !in r2 }) return null
    if (base.idx(escotilla) !in r2) return null
    val idBanco = muebles.entries.firstOrNull { it.value == JuegoMueble.BANCO }?.key
    if (idBanco != null) {
        val fb = idBanco / ancho
        val cb = idBanco % ancho
        val accesible = JUEGO_VECINOS8.any { d ->
            val p = JuegoPos(fb + d.fila, cb + d.col)
            base.dentro(p) && base.idx(p) in r1
        }
        if (!accesible) return null
    }

    val vis = juegoCalcularVista(base.mapa, ancho, alto, inicio)
    return base.copy(visibles = vis, explorado = vis)
}


internal fun juegoConstruirPiso(piso: Int, previo: JuegoEstadoUi?, dificultad: JuegoDificultad): JuegoEstadoUi? =
    runCatching { juegoConstruirPisoInterno(piso, previo, dificultad) }.getOrNull()

internal fun juegoGenerarPiso(piso: Int, previo: JuegoEstadoUi?, dificultad: JuegoDificultad): JuegoEstadoUi {
    repeat(400) {
        val e = juegoConstruirPiso(piso, previo, dificultad)
        if (e != null) return e
    }
    throw IllegalStateException("No se pudo generar el piso")
}

internal fun juegoNuevo(dificultad: JuegoDificultad): JuegoEstadoUi =
    juegoGenerarPiso(1, null, dificultad).copy(fase = JuegoFase.MENU)

internal fun juegoSiguientePiso(e: JuegoEstadoUi): JuegoEstadoUi = juegoGenerarPiso(e.piso + 1, e, e.dificultad)


/** A partir del piso 2 algunos robots se convierten en variantes más peligrosas. */
internal fun juegoVariante(t: JuegoTipoEnemigo, piso: Int): JuegoTipoEnemigo {
    if (piso < 2) return t
    val x = Random.nextInt(100)
    return when (t) {
        JuegoTipoEnemigo.ROBOT -> when {
            piso >= 4 && x < 15 -> JuegoTipoEnemigo.BLINDADO
            piso >= 3 && x < 30 -> JuegoTipoEnemigo.ROBOT_PESADO
            x < 30 + 5 * piso -> JuegoTipoEnemigo.ROBOT_COMBATE
            x < 45 + 5 * piso -> JuegoTipoEnemigo.GUARDIA
            else -> JuegoTipoEnemigo.ROBOT
        }
        JuegoTipoEnemigo.DRON -> if (piso >= 3 && x < 40) JuegoTipoEnemigo.DRON_ATAQUE else JuegoTipoEnemigo.DRON
        else -> t
    }
}

/** Crea un enemigo con estadísticas escaladas por piso y dificultad, y con su equipo. */
internal fun juegoCrearEnemigo(
    id: Int,
    tipo: JuegoTipoEnemigo,
    pos: JuegoPos,
    piso: Int,
    dif: JuegoDificultad,
    nuevoUid: () -> Int
): JuegoEnemigo {
    val vida = maxOf(1, (tipo.vidaMax * (1f + 0.15f * (piso - 1)) * dif.vidaEnemigoPct / 100f).roundToInt())
    val danio = maxOf(1, (tipo.danio * (1f + 0.08f * (piso - 1)) * dif.danioEnemigoPct / 100f).roundToInt())
    val equipo: JuegoItem? = when (tipo) {
        JuegoTipoEnemigo.ROBOT_COMBATE -> {
            val arma = if (Random.nextBoolean()) JuegoTipoItem.ARMA_SUBFUSIL else JuegoTipoItem.ARMA_RIFLE_PRECISION
            juegoCrearItem(arma, juegoRarezaAleatoria(piso, dif), 1, 1, nuevoUid())
        }
        JuegoTipoEnemigo.GUARDIA ->
            juegoCrearItem(JuegoTipoItem.ARMA_PISTOLA, juegoRarezaAleatoria(piso, dif), 1, 1, nuevoUid())
        JuegoTipoEnemigo.ROBOT_PESADO, JuegoTipoEnemigo.BLINDADO ->
            juegoEquipoAleatorio(piso, dif, nuevoUid(), 100, JuegoRareza.COMUN, JuegoCategoria.ARMADURAS)
        JuegoTipoEnemigo.DRON_ATAQUE -> {
            val modulos = listOf(
                JuegoTipoItem.MOD_PUNTERIA, JuegoTipoItem.MOD_FILTRO, JuegoTipoItem.MOD_ESTABILIZADOR,
                JuegoTipoItem.MOD_RECARGA, JuegoTipoItem.MOD_ESCANER
            )
            juegoCrearItem(modulos.random(), juegoRarezaAleatoria(piso, dif), 1, 1, nuevoUid())
        }
        JuegoTipoEnemigo.CENTINELA -> {
            val legendario = Random.nextInt(100) < 8
            juegoEquipoAleatorio(piso, dif, nuevoUid(), 300, if (legendario) JuegoRareza.LEGENDARIO else JuegoRareza.EPICO)
        }
        else -> null
    }
    val defExtra = if (equipo != null && equipo.tipo.cat == JuegoCategoria.ARMADURAS) juegoStatsItem(equipo).defensa else 0
    return JuegoEnemigo(id, tipo, pos, vida, vida, danio, tipo.defensa + defExtra, equipo = equipo)
}
