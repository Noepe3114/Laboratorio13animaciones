@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

// ───────────────────────── Utilidades ─────────────────────────

internal fun juegoRomano(n: Int): String = when (n) {
    1 -> "I"
    2 -> "II"
    3 -> "III"
    else -> "$n"
}

internal fun JuegoItem.nombreCompleto(): String =
    if (tipo.mejorable) "${tipo.nombre} MK-${juegoRomano(nivel)}" else tipo.nombre

/** Redondeo probabilístico: 2.3 da 3 el 30% de las veces. Hace que las mejoras pequeñas tengan efecto real. */
internal fun juegoRedondeoProb(x: Float): Int {
    if (x <= 0f) return 0
    val base = floor(x).toInt()
    val resto = x - base
    return if (Random.nextFloat() < resto) base + 1 else base
}

private fun esc(v: Int, k: Float): Int = if (v == 0) 0 else maxOf(1, (v * k).roundToInt())

// ───────────────────────── Estadísticas ─────────────────────────

internal fun juegoPerk(t: JuegoTipoItem): String? = when (t) {
    JuegoTipoItem.ARMA_PISTOLA -> "Cargador cuántico: 25% de no gastar munición"
    JuegoTipoItem.ARMA_SUBFUSIL -> "Ráfaga ampliada: +1 disparo por ráfaga"
    JuegoTipoItem.ARMA_ESCOPETA -> "Dispersión letal: +2 perdigones"
    JuegoTipoItem.ARMA_RIFLE_PRECISION -> "Perforante: ignora toda la defensa"
    JuegoTipoItem.ARMA_RIFLE_PLASMA -> "Plasma inestable: daño en área al impactar"
    JuegoTipoItem.ARMA_EM -> "Sobrecarga: aturde casi siempre"
    else -> when (t.ranura) {
        JuegoRanura.CASCO -> "Sellado total: inmunidad a toxinas"
        JuegoRanura.PECHERA -> "Nanofibra: +2 de defensa"
        JuegoRanura.BRAZOS -> "Estabilizadores: +8% de precisión y +10% de crítico"
        JuegoRanura.PIERNAS -> "Propulsores: -15% de consumo de oxígeno"
        JuegoRanura.MOCHILA -> "Bodega ampliada: +8 de capacidad"
        JuegoRanura.TANQUE -> "Reserva profunda: +40 de oxígeno"
        JuegoRanura.ESCUDO -> "Escudo cuántico: +5 de escudo"
        JuegoRanura.MODULO1, JuegoRanura.MODULO2 -> "Núcleo perfecto: efecto duplicado"
        else -> null
    }
}

private fun juegoAplicarPerk(t: JuegoTipoItem, s: JuegoBase): JuegoBase = when (t) {
    JuegoTipoItem.ARMA_PISTOLA -> s.copy(ahorro = s.ahorro + 25)
    JuegoTipoItem.ARMA_SUBFUSIL -> s.copy(disparos = s.disparos + 1)
    JuegoTipoItem.ARMA_ESCOPETA -> s.copy(disparos = s.disparos + 2)
    JuegoTipoItem.ARMA_RIFLE_PRECISION -> s.copy(penetracion = 100)
    JuegoTipoItem.ARMA_RIFLE_PLASMA -> s.copy(splash = 50)
    JuegoTipoItem.ARMA_EM -> s.copy(aturdir = 90)
    else -> when (t.ranura) {
        JuegoRanura.CASCO -> s.copy(toxica = 100)
        JuegoRanura.PECHERA -> s.copy(defensa = s.defensa + 2)
        JuegoRanura.BRAZOS -> s.copy(precision = s.precision + 8, critico = s.critico + 10)
        JuegoRanura.PIERNAS -> s.copy(eficiencia = s.eficiencia + 15)
        JuegoRanura.MOCHILA -> s.copy(capacidad = s.capacidad + 8)
        JuegoRanura.TANQUE -> s.copy(oxigeno = s.oxigeno + 40)
        JuegoRanura.ESCUDO -> s.copy(escudo = s.escudo + 5)
        JuegoRanura.MODULO1, JuegoRanura.MODULO2 -> s.copy(
            precision = s.precision * 2, toxica = minOf(100, s.toxica * 2), resEnergia = s.resEnergia * 2,
            menosRecarga = s.menosRecarga * 2, botin = s.botin * 2
        )
        else -> s
    }
}

/** Estadísticas reales de un objeto según su rareza y nivel. */
internal fun juegoStatsItem(item: JuegoItem): JuegoBase {
    val t = item.tipo
    val b = t.base
    val r = item.rareza
    val o = r.ordinal
    val n = item.nivel.coerceIn(1, JUEGO_NIVEL_MAX)
    var s = when (t.cat) {
        JuegoCategoria.ARMAS -> b.copy(
            dano = b.dano * r.mult * (1f + 0.15f * (n - 1)),
            precision = (b.precision + 2 * o + 4 * (n - 1)).coerceAtMost(98),
            critico = b.critico + 2 * o + (n - 1),
            cargador = (b.cargador * (1f + 0.25f * (n - 1)) * (1f + 0.05f * o)).roundToInt(),
            alcance = b.alcance + if (o >= 3) 1 else 0
        )
        JuegoCategoria.ARMADURAS, JuegoCategoria.TECNOLOGIA -> {
            val fn = 1f + (if (t.ranura == JuegoRanura.TANQUE) 0.5f else 0.3f) * (n - 1)
            val k = r.mult * fn
            b.copy(
                defensa = esc(b.defensa, k), integridad = esc(b.integridad, k), oxigeno = esc(b.oxigeno, k),
                eficiencia = esc(b.eficiencia, k), toxica = minOf(100, esc(b.toxica, k)), escudo = esc(b.escudo, k),
                resEnergia = esc(b.resEnergia, k), capacidad = esc(b.capacidad, k), precision = esc(b.precision, k),
                critico = esc(b.critico, k), botin = esc(b.botin, k),
                regen = if (b.regen > 0) maxOf(2, b.regen - (n - 1)) else 0
            )
        }
        else -> b
    }
    if (r == JuegoRareza.LEGENDARIO) s = juegoAplicarPerk(t, s)
    return s
}

/** Suma el equipo (sin el arma) y aplica los topes para que nada crezca sin límite. */
internal fun juegoTraje(equipo: Map<JuegoRanura, JuegoItem>): JuegoTraje {
    var defensa = 0; var integridad = 0; var oxigeno = 0; var eficiencia = 0; var toxica = 0
    var escudo = 0; var regen = 6; var resEnergia = 0; var capacidad = 0; var precision = 0
    var critico = 0; var menosRecarga = 0; var botin = 0
    for ((ranura, item) in equipo) {
        if (ranura == JuegoRanura.ARMA) continue
        val s = juegoStatsItem(item)
        defensa += s.defensa; integridad += s.integridad; oxigeno += s.oxigeno
        eficiencia += s.eficiencia; toxica += s.toxica; resEnergia += s.resEnergia
        capacidad += s.capacidad; precision += s.precision; critico += s.critico
        menosRecarga += s.menosRecarga; botin += s.botin
        if (s.escudo > 0) {
            escudo += s.escudo
            regen = s.regen
        }
    }
    return JuegoTraje(
        defensa = defensa.coerceAtMost(TOPE_DEFENSA),
        integridad = integridad,
        oxigeno = oxigeno,
        eficiencia = eficiencia.coerceAtMost(TOPE_EFICIENCIA),
        toxica = toxica.coerceAtMost(TOPE_TOXICA),
        escudoMax = escudo.coerceAtMost(TOPE_ESCUDO),
        regen = regen,
        resEnergia = resEnergia.coerceAtMost(TOPE_ENERGIA),
        capacidad = capacidad,
        precision = precision.coerceAtMost(TOPE_PRECISION),
        critico = critico.coerceAtMost(TOPE_CRITICO),
        menosRecarga = menosRecarga.coerceAtMost(1),
        botin = botin.coerceAtMost(TOPE_BOTIN)
    )
}

internal fun juegoVidaMax(dif: JuegoDificultad, bonoVida: Int, traje: JuegoTraje): Int =
    dif.vida + bonoVida + traje.integridad

internal fun juegoOxigenoMax(dif: JuegoDificultad, bonoOxigeno: Int, traje: JuegoTraje): Int =
    dif.oxigenoBase + bonoOxigeno + traje.oxigeno

internal fun juegoCapacidad(traje: JuegoTraje): Int = JUEGO_CAPACIDAD_BASE + traje.capacidad

// ───────────────────────── Inventario ─────────────────────────

internal fun juegoContar(inv: List<JuegoItem>, tipo: JuegoTipoItem): Int =
    inv.filter { it.tipo == tipo }.sumOf { it.cantidad }

internal fun juegoReserva(inv: List<JuegoItem>, familia: JuegoFamilia?): Int =
    if (familia == null) 0 else juegoContar(inv, familia.municion())

/** Apila y agrega. Devuelve la nueva lista y la cantidad que no cupo. */
internal fun juegoAgregar(
    inv: List<JuegoItem>,
    item: JuegoItem,
    capacidad: Int,
    nuevoUid: () -> Int
): Pair<List<JuegoItem>, Int> {
    val lista = inv.toMutableList()
    var restante = item.cantidad
    if (item.tipo.pila > 1) {
        for (i in lista.indices) {
            val s = lista[i]
            if (restante > 0 && s.tipo == item.tipo && s.rareza == item.rareza && s.cantidad < s.tipo.pila) {
                val cabe = minOf(restante, s.tipo.pila - s.cantidad)
                lista[i] = s.copy(cantidad = s.cantidad + cabe)
                restante -= cabe
            }
        }
    }
    var primera = true
    while (restante > 0 && lista.size < capacidad) {
        val n = minOf(restante, item.tipo.pila)
        lista.add(item.copy(uid = if (primera) item.uid else nuevoUid(), cantidad = n))
        primera = false
        restante -= n
    }
    return Pair(lista.toList(), restante)
}

internal fun juegoQuitar(inv: List<JuegoItem>, tipo: JuegoTipoItem, cantidad: Int): List<JuegoItem> {
    var falta = cantidad
    val lista = inv.toMutableList()
    val indices = lista.indices.filter { lista[it].tipo == tipo }.sortedBy { lista[it].cantidad }
    for (i in indices) {
        if (falta <= 0) break
        val s = lista[i]
        val quita = minOf(falta, s.cantidad)
        lista[i] = s.copy(cantidad = s.cantidad - quita)
        falta -= quita
    }
    return lista.filter { it.cantidad > 0 }
}

// ───────────────────────── Generación aleatoria ─────────────────────────

internal fun juegoRarezaAleatoria(piso: Int, dif: JuegoDificultad, boostPct: Int = 100): JuegoRareza {
    val k = dif.rarezaPct * boostPct / 10000f
    val pesos = floatArrayOf(
        100f,
        40f + 4f * piso,
        if (piso >= 2) (10f + 3f * piso) * k else 0f,
        if (piso >= 3) (2f + 1.5f * piso) * k else 0f,
        if (piso >= 4) 0.8f * k else 0f
    )
    var tirada = Random.nextFloat() * pesos.sum()
    for (i in pesos.indices) {
        tirada -= pesos[i]
        if (tirada <= 0f) return JuegoRareza.entries[i]
    }
    return JuegoRareza.COMUN
}

internal fun juegoCrearItem(tipo: JuegoTipoItem, rareza: JuegoRareza, nivel: Int, cantidad: Int, uid: Int): JuegoItem {
    val base = JuegoItem(uid, tipo, rareza, nivel, cantidad)
    return if (tipo.cat == JuegoCategoria.ARMAS) base.copy(municion = juegoStatsItem(base).cargador) else base
}

internal fun juegoCrearMaterial(tipo: JuegoTipoItem, cantidad: Int, uid: Int): JuegoItem =
    JuegoItem(uid, tipo, tipo.rarezaBase, 1, cantidad)

private fun <T> elegirPonderado(opciones: List<Pair<T, Int>>): T {
    val total = opciones.sumOf { it.second }
    var t = Random.nextInt(total)
    for ((v, p) in opciones) {
        t -= p
        if (t < 0) return v
    }
    return opciones.last().first
}

internal fun juegoTipoArmaAleatorio(piso: Int): JuegoTipoItem = elegirPonderado(
    listOf(
        Pair(JuegoTipoItem.ARMA_PISTOLA, 30),
        Pair(JuegoTipoItem.ARMA_SUBFUSIL, 25),
        Pair(JuegoTipoItem.ARMA_ESCOPETA, 20),
        Pair(JuegoTipoItem.ARMA_RIFLE_PRECISION, if (piso >= 2) 10 else 0),
        Pair(JuegoTipoItem.ARMA_RIFLE_PLASMA, if (piso >= 3) 6 else 0),
        Pair(JuegoTipoItem.ARMA_EM, if (piso >= 3) 5 else 0)
    ).filter { it.second > 0 }
)

private val juegoListaArmaduras = listOf(
    Pair(JuegoTipoItem.CASCO_SERVICIO, 10), Pair(JuegoTipoItem.CASCO_FILTRO, 6),
    Pair(JuegoTipoItem.PECHERA_SERVICIO, 10), Pair(JuegoTipoItem.PECHERA_REFORZADA, 6),
    Pair(JuegoTipoItem.BRAZOS_SERVICIO, 10), Pair(JuegoTipoItem.BRAZOS_TACTICOS, 6),
    Pair(JuegoTipoItem.PIERNAS_SERVICIO, 10), Pair(JuegoTipoItem.PIERNAS_ASALTO, 6)
)

private val juegoListaTecnologia = listOf(
    Pair(JuegoTipoItem.MOCHILA_BASICA, 8), Pair(JuegoTipoItem.MOCHILA_SOPORTE, 4),
    Pair(JuegoTipoItem.TANQUE_OXIGENO, 8), Pair(JuegoTipoItem.TANQUE_PRESURIZADO, 4),
    Pair(JuegoTipoItem.GENERADOR_ESCUDO, 8), Pair(JuegoTipoItem.GENERADOR_AVANZADO, 4),
    Pair(JuegoTipoItem.MOD_PUNTERIA, 5), Pair(JuegoTipoItem.MOD_FILTRO, 5),
    Pair(JuegoTipoItem.MOD_ESTABILIZADOR, 5), Pair(JuegoTipoItem.MOD_RECARGA, 4),
    Pair(JuegoTipoItem.MOD_ESCANER, 4)
)

/** Genera un arma, armadura o pieza tecnológica con rareza según el piso y la dificultad. */
internal fun juegoEquipoAleatorio(
    piso: Int,
    dif: JuegoDificultad,
    uid: Int,
    boostPct: Int = 100,
    minRareza: JuegoRareza = JuegoRareza.COMUN,
    solo: JuegoCategoria? = null
): JuegoItem {
    val cat = solo ?: elegirPonderado(
        listOf(Pair(JuegoCategoria.ARMAS, 30), Pair(JuegoCategoria.ARMADURAS, 35), Pair(JuegoCategoria.TECNOLOGIA, 35))
    )
    val tipo = when (cat) {
        JuegoCategoria.ARMAS -> juegoTipoArmaAleatorio(piso)
        JuegoCategoria.ARMADURAS -> elegirPonderado(juegoListaArmaduras)
        else -> elegirPonderado(juegoListaTecnologia)
    }
    var rareza = juegoRarezaAleatoria(piso, dif, boostPct)
    if (rareza.ordinal < minRareza.ordinal) rareza = minRareza
    return juegoCrearItem(tipo, rareza, 1, 1, uid)
}

/** Munición aleatoria para suelo y cofres. */
internal fun juegoMunicionAleatoria(piso: Int, uid: Int): JuegoItem {
    val tipo = elegirPonderado(
        listOf(
            Pair(JuegoTipoItem.MUN_BALAS, 55),
            Pair(JuegoTipoItem.MUN_CARTUCHOS, 15),
            Pair(JuegoTipoItem.MUN_PRECISION, if (piso >= 2) 12 else 0),
            Pair(JuegoTipoItem.MUN_PLASMA, if (piso >= 3) 9 else 0),
            Pair(JuegoTipoItem.MUN_EM, if (piso >= 3) 9 else 0)
        ).filter { it.second > 0 }
    )
    val n = when (tipo) {
        JuegoTipoItem.MUN_BALAS -> Random.nextInt(10, 19)
        JuegoTipoItem.MUN_CARTUCHOS -> Random.nextInt(4, 9)
        JuegoTipoItem.MUN_PRECISION -> Random.nextInt(3, 7)
        else -> Random.nextInt(4, 9)
    }
    return juegoCrearMaterial(tipo, n, uid)
}

// ───────────────────────── Recetas y mejoras ─────────────────────────

enum class JuegoGrupoReceta(val titulo: String) {
    ARMAS("Armas"),
    MUNICION("Munición"),
    CONSUMIBLES("Consumibles"),
    MODULOS("Módulos"),
    MEJORAS("Mejoras")
}

data class JuegoReceta(
    val id: String,
    val grupo: JuegoGrupoReceta,
    val resultado: JuegoTipoItem,
    val cantidad: Int,
    val rareza: JuegoRareza,
    val costo: Map<JuegoTipoItem, Int>
)

private fun receta(
    grupo: JuegoGrupoReceta,
    res: JuegoTipoItem,
    cantidad: Int,
    rareza: JuegoRareza,
    vararg costo: Pair<JuegoTipoItem, Int>
) = JuegoReceta(res.name, grupo, res, cantidad, rareza, costo.toMap())

private val CH = JuegoTipoItem.MAT_CHATARRA
private val CI = JuegoTipoItem.MAT_CIRCUITOS
private val CE = JuegoTipoItem.MAT_CELDA
private val CO = JuegoTipoItem.MAT_COMPONENTE
private val PL = JuegoTipoItem.MAT_PLACA
private val NU = JuegoTipoItem.MAT_NUCLEO
private val TE = JuegoTipoItem.MAT_TEJIDO
private val CR = JuegoTipoItem.MAT_CRISTAL_ORG
private val NM = JuegoTipoItem.MAT_NUCLEO_MUTADO

internal val JuegoRecetas: List<JuegoReceta> = listOf(
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_PISTOLA, 1, JuegoRareza.COMUN, CH to 10, CI to 3, CO to 1),
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_SUBFUSIL, 1, JuegoRareza.COMUN, CH to 14, CI to 6, CO to 2),
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_ESCOPETA, 1, JuegoRareza.COMUN, CH to 16, PL to 2, CO to 2),
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_RIFLE_PRECISION, 1, JuegoRareza.POCO_COMUN, CH to 14, CI to 8, CE to 2, CO to 3),
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_RIFLE_PLASMA, 1, JuegoRareza.POCO_COMUN, CI to 10, CE to 6, CO to 3, NM to 2),
    receta(JuegoGrupoReceta.ARMAS, JuegoTipoItem.ARMA_EM, 1, JuegoRareza.POCO_COMUN, CI to 12, CE to 8, NU to 2, CO to 3),

    receta(JuegoGrupoReceta.MUNICION, JuegoTipoItem.MUN_BALAS, 12, JuegoRareza.COMUN, CH to 3, CI to 1),
    receta(JuegoGrupoReceta.MUNICION, JuegoTipoItem.MUN_CARTUCHOS, 6, JuegoRareza.COMUN, CH to 5),
    receta(JuegoGrupoReceta.MUNICION, JuegoTipoItem.MUN_PRECISION, 5, JuegoRareza.COMUN, CH to 4, CI to 2),
    receta(JuegoGrupoReceta.MUNICION, JuegoTipoItem.MUN_PLASMA, 6, JuegoRareza.COMUN, CE to 2, CR to 1),
    receta(JuegoGrupoReceta.MUNICION, JuegoTipoItem.MUN_EM, 6, JuegoRareza.COMUN, CE to 2, CI to 2),

    receta(JuegoGrupoReceta.CONSUMIBLES, JuegoTipoItem.CON_BOTIQUIN, 1, JuegoRareza.COMUN, TE to 2, CH to 1),
    receta(JuegoGrupoReceta.CONSUMIBLES, JuegoTipoItem.CON_BOTIQUIN_AV, 1, JuegoRareza.POCO_COMUN, TE to 3, CR to 1),
    receta(JuegoGrupoReceta.CONSUMIBLES, JuegoTipoItem.CON_OXIGENO, 1, JuegoRareza.COMUN, CH to 3, CE to 1),
    receta(JuegoGrupoReceta.CONSUMIBLES, JuegoTipoItem.CON_CELDA_ESCUDO, 1, JuegoRareza.POCO_COMUN, CE to 2, PL to 1),
    receta(JuegoGrupoReceta.CONSUMIBLES, JuegoTipoItem.CON_CONDENSADOR, 1, JuegoRareza.POCO_COMUN, CI to 3, CE to 1),

    receta(JuegoGrupoReceta.MODULOS, JuegoTipoItem.MOD_PUNTERIA, 1, JuegoRareza.POCO_COMUN, CI to 6, CE to 2, CH to 4),
    receta(JuegoGrupoReceta.MODULOS, JuegoTipoItem.MOD_FILTRO, 1, JuegoRareza.POCO_COMUN, CI to 4, TE to 3, CH to 4),
    receta(JuegoGrupoReceta.MODULOS, JuegoTipoItem.MOD_ESTABILIZADOR, 1, JuegoRareza.POCO_COMUN, CI to 5, PL to 2, CE to 2),
    receta(JuegoGrupoReceta.MODULOS, JuegoTipoItem.MOD_RECARGA, 1, JuegoRareza.POCO_COMUN, CI to 6, CO to 2, CE to 2),
    receta(JuegoGrupoReceta.MODULOS, JuegoTipoItem.MOD_ESCANER, 1, JuegoRareza.POCO_COMUN, CI to 8, NU to 1, CE to 3)
)

/** Coste de subir un objeto al siguiente nivel. Crece con el nivel y la rareza; null si no se puede mejorar. */
internal fun juegoCostoMejora(item: JuegoItem): Map<JuegoTipoItem, Int>? {
    val t = item.tipo
    if (!t.mejorable || item.nivel >= JUEGO_NIVEL_MAX) return null
    val n = item.nivel
    val m = 1f + 0.2f * item.rareza.ordinal
    fun c(v: Float): Int = ceil(v * m).toInt()
    val costo = linkedMapOf<JuegoTipoItem, Int>()
    when {
        t.cat == JuegoCategoria.ARMAS -> {
            val f = when (t.base.familia) {
                JuegoFamilia.RIFLE_PRECISION -> 1.5f
                JuegoFamilia.RIFLE_PLASMA, JuegoFamilia.ELECTROMAGNETICA -> 2f
                else -> 1f
            }
            costo[CH] = c(8f * n * f)
            costo[CI] = c(4f * n * f)
            costo[CE] = c(2f * n * f)
            if (n >= 2) costo[CO] = c(n.toFloat())
        }
        t.ranura == JuegoRanura.TANQUE -> {
            costo[CH] = c(10f * n); costo[CI] = c(5f * n); costo[CE] = c(3f * n)
        }
        t.ranura == JuegoRanura.ESCUDO -> {
            costo[CI] = c(6f * n); costo[CE] = c(4f * n); costo[CH] = c(4f * n)
        }
        t.ranura == JuegoRanura.MOCHILA -> {
            costo[CH] = c(8f * n); costo[CI] = c(3f * n); costo[PL] = c(n.toFloat())
        }
        else -> {
            costo[CH] = c(6f * n); costo[PL] = c(2f * n); costo[CI] = c(2f * n)
        }
    }
    return costo
}

internal fun juegoPuedePagar(inv: List<JuegoItem>, costo: Map<JuegoTipoItem, Int>): Boolean =
    costo.all { (tipo, n) -> juegoContar(inv, tipo) >= n }

// ───────────────────────── Líneas de estadísticas (UI y comparación) ─────────────────────────

data class JuegoLineaStat(
    val etiqueta: String,
    val valor: Float,
    val texto: String,
    val mayorEsMejor: Boolean = true
)

private fun fmt(x: Float): String = if (x == x.toInt().toFloat()) "${x.toInt()}" else String.format("%.1f", x)

internal fun juegoLineasStats(item: JuegoItem): List<JuegoLineaStat> {
    val s = juegoStatsItem(item)
    val l = mutableListOf<JuegoLineaStat>()
    fun add(e: String, v: Int, sufijo: String = "", mayor: Boolean = true) {
        if (v != 0) l.add(JuegoLineaStat(e, v.toFloat(), "$v$sufijo", mayor))
    }
    when (item.tipo.cat) {
        JuegoCategoria.ARMAS -> {
            val porDisparo = if (s.disparos > 1) " ×${s.disparos}" else ""
            l.add(JuegoLineaStat("Daño", s.dano * s.disparos, fmt(s.dano) + porDisparo))
            l.add(JuegoLineaStat("Alcance", s.alcance.toFloat(), "${s.alcance} casillas"))
            l.add(JuegoLineaStat("Precisión", s.precision.toFloat(), "${s.precision}%"))
            l.add(JuegoLineaStat("Crítico", s.critico.toFloat(), "${s.critico}% (x${fmt(s.critMult / 100f)})"))
            l.add(JuegoLineaStat("Cargador", s.cargador.toFloat(), "${s.cargador}"))
            l.add(JuegoLineaStat("Recarga", s.recarga.toFloat(), "${s.recarga} turno(s)", false))
            l.add(JuegoLineaStat("Consumo", s.consumo.toFloat(), "${s.consumo} por disparo", false))
            add("Penetración", s.penetracion, "%")
            add("Aturdimiento", s.aturdir, "%")
            add("Área", s.splash, "%")
        }
        JuegoCategoria.CONSUMIBLES -> {
            add("Cura", s.cura, " de vida")
            add("Oxígeno", s.oxigeno)
        }
        JuegoCategoria.ARMADURAS, JuegoCategoria.TECNOLOGIA -> {
            add("Defensa", s.defensa)
            add("Integridad", s.integridad, " de vida")
            add("Oxígeno", s.oxigeno)
            add("Eficiencia O₂", s.eficiencia, "%")
            add("Prot. tóxica", s.toxica, "%")
            add("Escudo", s.escudo)
            if (s.escudo > 0) add("Regen. escudo", s.regen, " turnos", false)
            add("Res. energía", s.resEnergia)
            add("Capacidad", s.capacidad, " huecos")
            add("Precisión", s.precision, "%")
            add("Crítico", s.critico, "%")
            add("Recarga", -s.menosRecarga, " turno")
            add("Botín", s.botin, "%")
        }
        else -> {}
    }
    return l
}
