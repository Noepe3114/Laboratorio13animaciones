@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.ui.graphics.Color

internal fun juegoAvisar(e: JuegoEstadoUi, texto: String, color: Color, icono: JuegoSprite): JuegoEstadoUi {
    val lista = (e.avisos + JuegoAviso(e.avisoId, texto, color, icono)).takeLast(JUEGO_MAX_AVISOS)
    return e.copy(avisos = lista, avisoId = e.avisoId + 1)
}

/**
 * Recalcula los máximos de vida, oxígeno y escudo tras cambiar el equipo.
 * Si un máximo sube, la barra sube con él; si baja, se recorta.
 */
internal fun juegoSincronizar(e: JuegoEstadoUi, antes: JuegoTraje): JuegoEstadoUi {
    val traje = juegoTraje(e.equipo)
    val vMax = juegoVidaMax(e.dificultad, e.bonoVida, traje)
    val oMax = juegoOxigenoMax(e.dificultad, e.bonoOxigeno, traje)
    val eMax = traje.escudoMax
    return e.copy(
        vidaMax = vMax,
        vida = (e.vida + maxOf(0, vMax - e.vidaMax)).coerceIn(1, vMax),
        oxigenoMax = oMax,
        oxigeno = (e.oxigeno + maxOf(0, oMax - e.oxigenoMax)).coerceIn(0, oMax),
        escudoPts = (e.escudoPts + maxOf(0, eMax - antes.escudoMax)).coerceIn(0, eMax)
    )
}

/** Devuelve la ranura donde iría el objeto (los módulos usan la primera libre). */
internal fun juegoRanuraDestino(e: JuegoEstadoUi, item: JuegoItem): JuegoRanura? {
    val r = item.tipo.ranura ?: return null
    if (r == JuegoRanura.MODULO1) {
        return when {
            e.equipo[JuegoRanura.MODULO1] == null -> JuegoRanura.MODULO1
            e.equipo[JuegoRanura.MODULO2] == null -> JuegoRanura.MODULO2
            else -> JuegoRanura.MODULO1
        }
    }
    return r
}

internal fun juegoEquipar(e: JuegoEstadoUi, uid: Int): JuegoEstadoUi {
    val item = e.inventario.firstOrNull { it.uid == uid } ?: return e
    val r = juegoRanuraDestino(e, item) ?: return e.copy(mensaje = "Este objeto no se puede equipar")
    val antes = juegoTraje(e.equipo)
    val previo = e.equipo[r]
    var inv = e.inventario.filter { it.uid != uid }
    if (previo != null) inv = inv + previo
    val equipoNuevo = e.equipo + (r to item)
    if (inv.size > juegoCapacidad(juegoTraje(equipoNuevo))) {
        return e.copy(mensaje = "Libera espacio en la mochila antes de cambiar esta pieza")
    }
    var n = e.copy(equipo = equipoNuevo, inventario = inv, mensaje = "Equipado: ${item.nombreCompleto()}")
    if (r == JuegoRanura.ARMA) n = n.copy(recarga = 0)
    n = juegoSincronizar(n, antes)
    return juegoAvisar(n, "Equipado: ${item.nombreCompleto()}", item.rareza.color, item.tipo.icono)
}

internal fun juegoDesequipar(e: JuegoEstadoUi, ranura: JuegoRanura): JuegoEstadoUi {
    val item = e.equipo[ranura] ?: return e
    val antes = juegoTraje(e.equipo)
    val equipoNuevo = e.equipo - ranura
    val inv = e.inventario + item
    if (inv.size > juegoCapacidad(juegoTraje(equipoNuevo))) {
        return e.copy(mensaje = "Mochila llena: no hay hueco para guardarlo")
    }
    var n = e.copy(equipo = equipoNuevo, inventario = inv, mensaje = "Desequipado: ${item.nombreCompleto()}")
    if (ranura == JuegoRanura.ARMA) n = n.copy(recarga = 0)
    return juegoSincronizar(n, antes)
}

internal fun juegoDescartar(e: JuegoEstadoUi, uid: Int): JuegoEstadoUi {
    val item = e.inventario.firstOrNull { it.uid == uid } ?: return e
    return e.copy(
        inventario = e.inventario.filter { it.uid != uid },
        mensaje = "Descartado: ${item.nombreCompleto()}"
    )
}

internal fun juegoMejorar(e: JuegoEstadoUi, uid: Int): JuegoEstadoUi {
    val enInv = e.inventario.firstOrNull { it.uid == uid }
    val entradaEq = e.equipo.entries.firstOrNull { it.value.uid == uid }
    val item = enInv ?: entradaEq?.value ?: return e
    val costo = juegoCostoMejora(item) ?: return e.copy(mensaje = "Ya está al nivel máximo")
    if (!juegoPuedePagar(e.inventario, costo)) return e.copy(mensaje = "Faltan materiales para la mejora")
    var inv = e.inventario
    for ((tipo, n) in costo) inv = juegoQuitar(inv, tipo, n)
    val mejor = item.copy(nivel = item.nivel + 1)
    val antes = juegoTraje(e.equipo)
    val base = if (enInv != null) {
        e.copy(inventario = inv.map { if (it.uid == uid) mejor else it })
    } else {
        e.copy(inventario = inv, equipo = e.equipo + (entradaEq!!.key to mejor))
    }
    val n = juegoSincronizar(base.copy(mensaje = "Mejorado: ${mejor.nombreCompleto()}"), antes)
    return juegoAvisar(n, "Mejorado: ${mejor.nombreCompleto()}", mejor.rareza.color, mejor.tipo.icono)
}

internal fun juegoFabricar(e: JuegoEstadoUi, id: String): JuegoEstadoUi {
    val r = JuegoRecetas.firstOrNull { it.id == id } ?: return e
    if (!juegoPuedePagar(e.inventario, r.costo)) return e.copy(mensaje = "Faltan materiales")
    var inv = e.inventario
    for ((tipo, n) in r.costo) inv = juegoQuitar(inv, tipo, n)
    var sig = e.itemId + 1
    val rareza = if (r.resultado.pila > 1) r.resultado.rarezaBase else r.rareza
    val item = juegoCrearItem(r.resultado, rareza, 1, r.cantidad, e.itemId)
    val (inv2, resto) = juegoAgregar(inv, item, juegoCapacidad(juegoTraje(e.equipo))) { sig++ }
    if (resto > 0) return e.copy(mensaje = "Mochila llena: libera espacio para fabricar")
    val n = e.copy(
        inventario = inv2,
        itemId = sig,
        mensaje = "Fabricado: ${item.nombreCompleto()}" + if (r.cantidad > 1) " ×${r.cantidad}" else ""
    )
    return juegoAvisar(n, "Fabricado: ${item.nombreCompleto()}", rareza.color, item.tipo.icono)
}

/** Primer objeto del tipo pedido en la mochila, o -1. Sirve para los botones de uso rápido. */
internal fun juegoUidDe(e: JuegoEstadoUi, tipo: JuegoTipoItem): Int =
    e.inventario.firstOrNull { it.tipo == tipo }?.uid ?: -1

internal fun juegoUidBotiquin(e: JuegoEstadoUi): Int {
    val av = juegoUidDe(e, JuegoTipoItem.CON_BOTIQUIN)
    return if (av >= 0) av else juegoUidDe(e, JuegoTipoItem.CON_BOTIQUIN_AV)
}

internal fun juegoInicial(): Pair<Map<JuegoRanura, JuegoItem>, List<JuegoItem>> {
    val pistola = juegoCrearItem(JuegoTipoItem.ARMA_PISTOLA, JuegoRareza.COMUN, 1, 1, 1)
    val tanque = JuegoItem(2, JuegoTipoItem.TANQUE_OXIGENO, JuegoRareza.COMUN, 1, 1)
    val equipo = mapOf(JuegoRanura.ARMA to pistola, JuegoRanura.TANQUE to tanque)
    val inv = listOf(
        JuegoItem(3, JuegoTipoItem.MUN_BALAS, JuegoRareza.COMUN, 1, 36),
        JuegoItem(4, JuegoTipoItem.CON_BOTIQUIN, JuegoRareza.COMUN, 1, 1)
    )
    return Pair(equipo, inv)
}

/** True si el jugador está pegado a un banco de trabajo. */
internal fun juegoJuntoABanco(e: JuegoEstadoUi): Boolean =
    e.muebles.entries.any { (id, m) ->
        m == JuegoMueble.BANCO && juegoCheb(JuegoPos(id / e.ancho, id % e.ancho), e.jugador) <= 1
    }
