@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private fun fmtNum(x: Float): String = if (x == x.toInt().toFloat()) "${x.toInt()}" else String.format("%.1f", x)

private fun fmtDif(d: Float): String = (if (d > 0) "+" else "") + fmtNum(d)

private fun nombrePestana(t: JuegoPanelTipo): String = when (t) {
    JuegoPanelTipo.INVENTARIO -> "MOCHILA"
    JuegoPanelTipo.EQUIPO -> "TRAJE"
    JuegoPanelTipo.BANCO -> "BANCO"
}

private fun resumenStats(item: JuegoItem, max: Int = 4): String =
    juegoLineasStats(item).take(max).joinToString(" · ") { "${it.etiqueta} ${it.texto}" }

/** Diferencias entre el nivel actual y el siguiente de un objeto, en texto. */
private fun diferenciasMejora(item: JuegoItem): String {
    val ahora = juegoLineasStats(item)
    val luego = juegoLineasStats(item.copy(nivel = item.nivel + 1))
    val cambios = luego.mapNotNull { n ->
        val a = ahora.firstOrNull { it.etiqueta == n.etiqueta }
        if (a == null) "${n.etiqueta} ${n.texto}" else if (a.texto != n.texto) "${n.etiqueta} ${a.texto} → ${n.texto}" else null
    }
    return cambios.joinToString(" · ")
}

@Composable
internal fun JuegoPanelMochila(
    estado: JuegoEstadoUi,
    pestanaInicial: JuegoPanelTipo,
    onCerrar: () -> Unit,
    onEquipar: (Int) -> Unit,
    onDesequipar: (JuegoRanura) -> Unit,
    onUsar: (Int) -> Unit,
    onDescartar: (Int) -> Unit,
    onMejorar: (Int) -> Unit,
    onFabricar: (String) -> Unit
) {
    var pestana by remember { mutableStateOf(pestanaInicial) }
    var sel by remember { mutableStateOf<Int?>(null) }
    var filtro by remember { mutableStateOf<JuegoCategoria?>(null) }
    var grupo by remember { mutableStateOf(JuegoGrupoReceta.ARMAS) }
    LaunchedEffect(pestanaInicial) { pestana = pestanaInicial }

    val traje = remember(estado.equipo) { juegoTraje(estado.equipo) }
    val capacidad = juegoCapacidad(traje)
    val bancoOk = juegoJuntoABanco(estado)
    val itemSel: JuegoItem? = sel?.let { id ->
        estado.inventario.firstOrNull { it.uid == id } ?: estado.equipo.values.firstOrNull { it.uid == id }
    }
    val irBanco = {
        pestana = JuegoPanelTipo.BANCO
        grupo = JuegoGrupoReceta.MEJORAS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2070B18))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SISTEMA DEL TRAJE", color = JuegoNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Mochila ${estado.inventario.size}/$capacidad  ·  Piso ${estado.piso}",
                        color = JuegoTexto,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                JuegoBoton("CERRAR ✕", onCerrar, color = JuegoRojo)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                JuegoPanelTipo.entries.forEach { t ->
                    JuegoBoton(
                        texto = nombrePestana(t),
                        onClick = {
                            pestana = t
                            sel = null
                        },
                        modifier = Modifier.weight(1f),
                        color = if (t == pestana) JuegoNeon else Color(0xFF39406A)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (pestana) {
                    JuegoPanelTipo.INVENTARIO -> JuegoTabInventario(
                        estado, filtro, { filtro = it }, sel, { sel = it }, itemSel,
                        onEquipar, onDesequipar, onUsar, onDescartar, irBanco
                    )
                    JuegoPanelTipo.EQUIPO -> JuegoTabEquipo(
                        estado, traje, sel, { sel = it }, itemSel,
                        onEquipar, onDesequipar, onUsar, onDescartar, irBanco
                    )
                    JuegoPanelTipo.BANCO -> JuegoTabBanco(estado, bancoOk, grupo, { grupo = it }, onFabricar, onMejorar)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                estado.mensaje.ifEmpty { "Toca un objeto para ver sus estadísticas" },
                color = JuegoTextoSuave,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ───────────────────────── Pestaña: mochila ─────────────────────────

@Composable
private fun JuegoTabInventario(
    estado: JuegoEstadoUi,
    filtro: JuegoCategoria?,
    onFiltro: (JuegoCategoria?) -> Unit,
    sel: Int?,
    onSel: (Int) -> Unit,
    itemSel: JuegoItem?,
    onEquipar: (Int) -> Unit,
    onDesequipar: (JuegoRanura) -> Unit,
    onUsar: (Int) -> Unit,
    onDescartar: (Int) -> Unit,
    onIrBanco: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            JuegoChip("Todo ${estado.inventario.size}", filtro == null) { onFiltro(null) }
            JuegoCategoria.entries.forEach { c ->
                val n = estado.inventario.count { it.tipo.cat == c }
                JuegoChip("${c.titulo} $n", filtro == c) { onFiltro(c) }
            }
        }
        Spacer(Modifier.height(8.dp))
        val lista = estado.inventario.filter { filtro == null || it.tipo.cat == filtro }
        if (lista.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay objetos en esta categoría", color = JuegoTextoSuave, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(62.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(lista, key = { it.uid }) { item ->
                    JuegoIconoItem(
                        tipo = item.tipo,
                        rareza = item.rareza,
                        tam = 62.dp,
                        modifier = Modifier.clickable { onSel(item.uid) },
                        cantidad = item.cantidad,
                        seleccionado = sel == item.uid
                    )
                }
            }
        }
        if (itemSel != null) {
            Spacer(Modifier.height(8.dp))
            JuegoDetalle(itemSel, estado, onEquipar, onDesequipar, onUsar, onDescartar, onIrBanco)
        }
    }
}

// ───────────────────────── Detalle y comparación ─────────────────────────

@Composable
private fun JuegoDetalle(
    item: JuegoItem,
    estado: JuegoEstadoUi,
    onEquipar: (Int) -> Unit,
    onDesequipar: (JuegoRanura) -> Unit,
    onUsar: (Int) -> Unit,
    onDescartar: (Int) -> Unit,
    onIrBanco: () -> Unit
) {
    val ranuraEquipada = estado.equipo.entries.firstOrNull { it.value.uid == item.uid }?.key
    val enEquipo = ranuraEquipada != null
    val destino = juegoRanuraDestino(estado, item)
    val comparar: JuegoItem? = if (!enEquipo && destino != null) estado.equipo[destino] else null
    val lineas = juegoLineasStats(item)
    val lineasComp = comparar?.let { juegoLineasStats(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .juegoPanel(item.rareza.color)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JuegoIconoItem(item.tipo, item.rareza, 52.dp, cantidad = item.cantidad)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(item.nombreCompleto(), color = item.rareza.color, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "${item.rareza.nombre} · ${item.tipo.cat.titulo}" + if (enEquipo) " · EQUIPADO" else "",
                    color = JuegoTextoSuave,
                    fontSize = 11.sp
                )
            }
        }
        Text(item.tipo.desc, color = JuegoTexto, fontSize = 12.sp)
        val perk = juegoPerk(item.tipo)
        if (item.rareza == JuegoRareza.LEGENDARIO && perk != null) {
            Text("★ $perk", color = JuegoDorado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (comparar != null) {
            Text("Comparado con: ${comparar.nombreCompleto()}", color = JuegoNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        lineas.forEach { l ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(l.etiqueta, color = JuegoTextoSuave, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(l.texto, color = JuegoTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (lineasComp != null) {
                    val previo = lineasComp.firstOrNull { it.etiqueta == l.etiqueta }
                    val d = l.valor - (previo?.valor ?: 0f)
                    if (d != 0f) {
                        val mejor = (d > 0f) == l.mayorEsMejor
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(${fmtDif(d)})",
                            color = if (mejor) JuegoVerde else JuegoRojo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        if (item.tipo.cat == JuegoCategoria.ARMAS) {
            Text(
                "En el cargador: ${item.municion}   ·   Munición de reserva: ${juegoReserva(estado.inventario, item.tipo.base.familia)}",
                color = JuegoTextoSuave,
                fontSize = 11.sp
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (ranuraEquipada != null) {
                JuegoBoton("DESEQUIPAR", { onDesequipar(ranuraEquipada) }, Modifier.weight(1f), color = JuegoAmarillo)
            } else {
                if (item.tipo.ranura != null) {
                    val turno = if (item.tipo.cat == JuegoCategoria.ARMAS) " (1 turno)" else ""
                    JuegoBoton("EQUIPAR$turno", { onEquipar(item.uid) }, Modifier.weight(1f), color = JuegoVerde)
                }
                if (item.tipo.cat == JuegoCategoria.CONSUMIBLES) {
                    JuegoBoton("USAR (1 turno)", { onUsar(item.uid) }, Modifier.weight(1f), color = JuegoVerde)
                }
            }
            if (item.tipo.mejorable) {
                JuegoBoton("MEJORAR", onIrBanco, Modifier.weight(1f), color = JuegoDorado)
            }
            if (!enEquipo) {
                JuegoBoton("DESCARTAR", { onDescartar(item.uid) }, Modifier.weight(1f), color = JuegoRojo)
            }
        }
    }
}

// ───────────────────────── Pestaña: traje ─────────────────────────

@Composable
private fun JuegoTabEquipo(
    estado: JuegoEstadoUi,
    traje: JuegoTraje,
    sel: Int?,
    onSel: (Int) -> Unit,
    itemSel: JuegoItem?,
    onEquipar: (Int) -> Unit,
    onDesequipar: (JuegoRanura) -> Unit,
    onUsar: (Int) -> Unit,
    onDescartar: (Int) -> Unit,
    onIrBanco: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (itemSel != null) {
            JuegoDetalle(itemSel, estado, onEquipar, onDesequipar, onUsar, onDescartar, onIrBanco)
        }
        JuegoRanura.entries.forEach { r ->
            JuegoFilaRanura(r, estado.equipo[r], estado, sel != null && sel == estado.equipo[r]?.uid) { item -> onSel(item.uid) }
        }
        JuegoEstadisticasTraje(estado, traje)
    }
}

@Composable
private fun JuegoFilaRanura(
    ranura: JuegoRanura,
    item: JuegoItem?,
    estado: JuegoEstadoUi,
    seleccionada: Boolean,
    onClick: (JuegoItem) -> Unit
) {
    var escala by remember { mutableStateOf(1f) }
    LaunchedEffect(item?.uid, item?.nivel) {
        if (item != null) {
            escala = 1.06f
            delay(180.milliseconds)
            escala = 1f
        }
    }
    val escalaAnim by animateFloatAsState(escala, tween(180), label = "equipar")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escalaAnim
                scaleY = escalaAnim
            }
            .juegoPanel(if (seleccionada) Color.White else (item?.rareza?.color ?: Color(0xFF39406A)))
            .clickable(enabled = item != null) { if (item != null) onClick(item) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item != null) JuegoIconoItem(item.tipo, item.rareza, 46.dp) else JuegoRanuraVacia(46.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(ranura.titulo.uppercase(), color = JuegoTextoSuave, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            if (item != null) {
                Text(item.nombreCompleto(), color = item.rareza.color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(resumenStats(item, 3), color = JuegoTextoSuave, fontSize = 10.sp, maxLines = 1)
            } else {
                Text("— vacío —", color = Color(0xFF5C6490), fontSize = 13.sp)
            }
        }
        if (item != null && ranura == JuegoRanura.ARMA) {
            val s = juegoStatsItem(item)
            Text(
                "${item.municion}/${s.cargador}\n⛁ ${juegoReserva(estado.inventario, s.familia)}",
                color = JuegoTexto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun JuegoFilaStat(etiqueta: String, valor: Int, tope: Int, texto: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, color = JuegoTextoSuave, fontSize = 12.sp, modifier = Modifier.width(108.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1B2447))
        ) {
            val f = if (tope <= 0) 0f else (valor.toFloat() / tope).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(f)
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color)))
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(texto, color = JuegoTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(78.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun JuegoEstadisticasTraje(estado: JuegoEstadoUi, traje: JuegoTraje) {
    val arma = estado.equipo[JuegoRanura.ARMA]
    Column(
        modifier = Modifier.fillMaxWidth().juegoPanel().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("ESTADÍSTICAS DEL TRAJE", color = JuegoNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        JuegoFilaStat("Vida máxima", estado.vidaMax, estado.vidaMax, "${estado.vidaMax}", JuegoRojo)
        JuegoFilaStat("Integridad", traje.integridad, 12, "+${traje.integridad}", JuegoRojo)
        JuegoFilaStat("Defensa", traje.defensa, TOPE_DEFENSA, "${traje.defensa}/$TOPE_DEFENSA", JuegoAmarillo)
        JuegoFilaStat("Res. energía", traje.resEnergia, TOPE_ENERGIA, "${traje.resEnergia}/$TOPE_ENERGIA", Color(0xFFE040FB))
        JuegoFilaStat("Oxígeno máx.", estado.oxigenoMax, estado.oxigenoMax, "${estado.oxigenoMax}", JuegoAzul)
        JuegoFilaStat("Eficiencia O₂", traje.eficiencia, TOPE_EFICIENCIA, "${traje.eficiencia}%", JuegoVerde)
        JuegoFilaStat("Prot. tóxica", traje.toxica, TOPE_TOXICA, "${traje.toxica}%", JuegoVerde)
        JuegoFilaStat(
            "Escudo", traje.escudoMax, TOPE_ESCUDO,
            if (traje.escudoMax > 0) "${traje.escudoMax} · ${traje.regen}t" else "—", JuegoAzul
        )
        JuegoFilaStat("Precisión", traje.precision, TOPE_PRECISION, "+${traje.precision}%", JuegoDorado)
        JuegoFilaStat("Crítico", traje.critico, TOPE_CRITICO, "+${traje.critico}%", JuegoDorado)
        JuegoFilaStat("Capacidad", juegoCapacidad(traje), 40, "${juegoCapacidad(traje)}", JuegoNeon)
        if (arma != null) {
            val s = juegoStatsItem(arma)
            val critTotal = (s.critico + traje.critico + estado.bonoCritico).coerceAtMost(60)
            val precTotal = (s.precision + traje.precision).coerceAtMost(98)
            Text(
                "Arma activa: ${arma.nombreCompleto()} · precisión ${precTotal}% · crítico ${critTotal}% · daño +${estado.bonoDanio * 8}%",
                color = JuegoTextoSuave,
                fontSize = 11.sp
            )
        }
    }
}

// ───────────────────────── Pestaña: banco ─────────────────────────

@Composable
private fun JuegoCoste(tipo: JuegoTipoItem, necesario: Int, tengo: Int) {
    val ok = tengo >= necesario
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, if (ok) JuegoVerde.copy(alpha = 0.6f) else JuegoRojo.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JuegoSpriteVista(tipo.icono, Modifier.size(16.dp), tipo.tinte)
        Spacer(Modifier.width(4.dp))
        Text(
            "${tipo.nombre.substringBefore(' ')} $tengo/$necesario",
            color = if (ok) JuegoVerde else JuegoRojo,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun JuegoTarjetaTecnica(
    tipo: JuegoTipoItem,
    rareza: JuegoRareza,
    titulo: String,
    subtitulo: String,
    detalle: String,
    costo: Map<JuegoTipoItem, Int>?,
    inventario: List<JuegoItem>,
    textoBoton: String,
    activo: Boolean,
    cantidad: Int,
    onBoton: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().juegoPanel(rareza.color).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JuegoIconoItem(tipo, rareza, 48.dp, cantidad = cantidad)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = rareza.color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text(subtitulo, color = JuegoTextoSuave, fontSize = 11.sp)
            }
        }
        if (detalle.isNotEmpty()) Text(detalle, color = JuegoTexto, fontSize = 11.sp)
        if (costo != null) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                costo.forEach { (t, n) -> JuegoCoste(t, n, juegoContar(inventario, t)) }
            }
        }
        JuegoBoton(textoBoton, onBoton, Modifier.fillMaxWidth(), enabled = activo, color = JuegoDorado)
    }
}

@Composable
private fun JuegoTabBanco(
    estado: JuegoEstadoUi,
    bancoOk: Boolean,
    grupo: JuegoGrupoReceta,
    onGrupo: (JuegoGrupoReceta) -> Unit,
    onFabricar: (String) -> Unit,
    onMejorar: (Int) -> Unit
) {
    if (!bancoOk) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.juegoPanel(JuegoAmarillo).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JuegoSpriteVista(JuegoSprite.CANDADO, Modifier.size(44.dp))
                Text("BANCO DE TRABAJO NO DISPONIBLE", color = JuegoAmarillo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Acércate al banco (la mesa con pantalla azul de la esclusa de aterrizaje de cada piso) y tócalo para fabricar y mejorar tu equipo.",
                    color = JuegoTexto,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            JuegoGrupoReceta.entries.forEach { g -> JuegoChip(g.titulo, g == grupo) { onGrupo(g) } }
        }
        Spacer(Modifier.height(6.dp))
        val materiales = JuegoTipoItem.entries.filter { it.cat == JuegoCategoria.MATERIALES && juegoContar(estado.inventario, it) > 0 }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (materiales.isEmpty()) {
                Text("Sin materiales: derrota enemigos y recoge su botín", color = JuegoTextoSuave, fontSize = 11.sp)
            }
            materiales.forEach { m ->
                val n = juegoContar(estado.inventario, m)
                JuegoCoste(m, n, n)
            }
        }
        Spacer(Modifier.height(6.dp))
        if (grupo == JuegoGrupoReceta.MEJORAS) {
            val mejorables = (estado.equipo.values + estado.inventario).filter { it.tipo.mejorable }
            if (mejorables.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tienes objetos mejorables", color = JuegoTextoSuave, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mejorables, key = { it.uid }) { item ->
                        val costo = juegoCostoMejora(item)
                        val puede = costo != null && juegoPuedePagar(estado.inventario, costo)
                        val equipado = estado.equipo.values.any { it.uid == item.uid }
                        JuegoTarjetaTecnica(
                            tipo = item.tipo,
                            rareza = item.rareza,
                            titulo = item.nombreCompleto(),
                            subtitulo = (if (equipado) "EQUIPADO" else "EN LA MOCHILA") +
                                if (costo != null) " · MK-${juegoRomano(item.nivel)} → MK-${juegoRomano(item.nivel + 1)}" else " · NIVEL MÁXIMO",
                            detalle = if (costo != null) diferenciasMejora(item) else "Este objeto ya no se puede mejorar más.",
                            costo = costo,
                            inventario = estado.inventario,
                            textoBoton = if (costo != null) "MEJORAR" else "NIVEL MÁXIMO",
                            activo = puede,
                            cantidad = 1,
                            onBoton = { onMejorar(item.uid) }
                        )
                    }
                }
            }
        } else {
            val recetas = JuegoRecetas.filter { it.grupo == grupo }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recetas, key = { it.id }) { r ->
                    val rareza = if (r.resultado.pila > 1) r.resultado.rarezaBase else r.rareza
                    val muestra = juegoCrearItem(r.resultado, rareza, 1, 1, 0)
                    JuegoTarjetaTecnica(
                        tipo = r.resultado,
                        rareza = rareza,
                        titulo = r.resultado.nombre + if (r.cantidad > 1) " ×${r.cantidad}" else "",
                        subtitulo = "${rareza.nombre} · ${r.resultado.cat.titulo}",
                        detalle = if (r.resultado.cat == JuegoCategoria.ARMAS || r.resultado.cat == JuegoCategoria.TECNOLOGIA) {
                            resumenStats(muestra, 5)
                        } else {
                            r.resultado.desc
                        },
                        costo = r.costo,
                        inventario = estado.inventario,
                        textoBoton = "FABRICAR",
                        activo = juegoPuedePagar(estado.inventario, r.costo),
                        cantidad = r.cantidad,
                        onBoton = { onFabricar(r.id) }
                    )
                }
            }
        }
    }
}
