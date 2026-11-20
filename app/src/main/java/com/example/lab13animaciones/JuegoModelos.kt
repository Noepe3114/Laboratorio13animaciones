@file:Suppress("SpellCheckingInspection")

package com.example.lab13animaciones

import androidx.compose.ui.graphics.Color

// ───────────────────────── Constantes ─────────────────────────

internal const val JUEGO_PISOS = 5
internal const val JUEGO_OXIGENO_BAJO = 25
internal const val JUEGO_VIDA_BAJA = 3
internal const val JUEGO_DANIO_TRAMPA = 2
internal const val JUEGO_DANIO_PULSO = 2
internal const val JUEGO_COSTE_FLUIDO = 2
internal const val JUEGO_RADIO_VISION = 7
internal const val JUEGO_RADIO_PULSO = 2
internal const val JUEGO_COLUMNAS_VISTA = 9
internal const val JUEGO_LIMITE_DIST = 40
internal const val JUEGO_SEPARACION = 3
internal const val JUEGO_VENTANA_COMBO = 3
internal const val JUEGO_CAPACIDAD_BASE = 18
internal const val JUEGO_TANQUE_INICIAL = 25
internal const val JUEGO_NIVEL_MAX = 3
internal const val JUEGO_RECARGA_PULSO = 8
internal const val JUEGO_MAX_AVISOS = 4

// Topes del traje: evitan que las estadísticas se acumulen sin límite
internal const val TOPE_DEFENSA = 4
internal const val TOPE_ENERGIA = 4
internal const val TOPE_EFICIENCIA = 40
internal const val TOPE_TOXICA = 100
internal const val TOPE_ESCUDO = 14
internal const val TOPE_PRECISION = 15
internal const val TOPE_CRITICO = 25
internal const val TOPE_BOTIN = 30

// ───────────────────────── Colores ─────────────────────────

internal val JuegoFondo = Color(0xFF0B1020)
internal val JuegoColorTablero = Color(0xFF141B33)
internal val JuegoPanel = Color(0xFF0F1830)
internal val JuegoNeon = Color(0xFF22D3EE)
internal val JuegoTexto = Color(0xFFE8EAF6)
internal val JuegoTextoSuave = Color(0xFF9FA8DA)
internal val JuegoVerde = Color(0xFF43A047)
internal val JuegoAmarillo = Color(0xFFFDD835)
internal val JuegoRojo = Color(0xFFE53935)
internal val JuegoAzul = Color(0xFF38BDF8)
internal val JuegoDorado = Color(0xFFFFD54F)

internal val JUEGO_VECINOS8 = listOf(
    JuegoPos(-1, -1), JuegoPos(-1, 0), JuegoPos(-1, 1),
    JuegoPos(0, -1), JuegoPos(0, 1),
    JuegoPos(1, -1), JuegoPos(1, 0), JuegoPos(1, 1)
)

internal val JuegoNombresPiso = listOf(
    "Hangar de carga",
    "Laboratorio",
    "Sala de reactores",
    "Puente de mando",
    "Cápsula de escape"
)

// ───────────────────────── Enums generales ─────────────────────────

enum class JuegoFase { MENU, JUGANDO, MEJORA, CAMBIO_PISO, VICTORIA, GAME_OVER }

enum class JuegoComando { MOVER, PULSO, ESPERAR, DISPARAR, RECARGAR, USAR }

enum class JuegoPanelTipo { INVENTARIO, EQUIPO, BANCO }

enum class JuegoDificultad(
    val nombre: String,
    val vida: Int,
    val oxigeno: Int,
    val extraEnemigos: Int,
    val puntosPct: Int,
    val vidaEnemigoPct: Int,
    val danioEnemigoPct: Int,
    val botinPct: Int,
    val rarezaPct: Int
) {
    FACIL("Fácil", 18, 170, 0, 70, 85, 80, 115, 85),
    NORMAL("Normal", 14, 140, 1, 100, 100, 100, 100, 100),
    DIFICIL("Difícil", 10, 115, 2, 150, 125, 125, 85, 130);

    /** Oxígeno propio del traje sin tanque: el tanque inicial aporta el resto. */
    val oxigenoBase: Int get() = oxigeno - JUEGO_TANQUE_INICIAL
}

enum class JuegoMejora(val titulo: String, val detalle: String) {
    BLINDAJE("Blindaje", "+3 de vida máxima y curación total (máx. 3 veces)"),
    ARMAS("Armas", "+8% de daño con cualquier arma (máx. 3 veces)"),
    TANQUE("Tanque grande", "+20 de oxígeno máximo (máx. 2 veces)"),
    ESCUDO("Escudo reforzado", "Empiezas el siguiente piso con escudo"),
    PULSO("Pulso rápido", "El pulso recarga 2 turnos más rápido"),
    PRECISION("Precisión", "+15% de probabilidad de golpe crítico (máx. 2 veces)")
}

enum class JuegoTipoObjeto {
    TANQUE,
    TARJETA,
    BOTIQUIN,
    ESCUDO,
    CRISTAL,
    MODULO,
    COFRE,
    BOTIN
}

enum class JuegoTile(val camina: Boolean, val bloqueaVista: Boolean, val suelo: Boolean = false) {
    VACIO(false, true),
    PARED(false, true),
    VENTANA(false, true),
    PASILLO(true, false, true),
    SUELO_ENTRADA(true, false, true),
    SUELO_OFICINA(true, false, true),
    SUELO_LAB(true, false, true),
    SUELO_REACTOR(true, false, true),
    SUELO_ENFERMERIA(true, false, true),
    SUELO_ALMACEN(true, false, true),
    SUELO_DORM(true, false, true),
    SUELO_COMEDOR(true, false, true),
    SUELO_MANDO(true, false, true),
    FLUIDO(true, false, true),
    PUERTA(true, true),
    PUERTA_CERRADA(false, true),
    ESCOTILLA(true, false)
}

enum class JuegoMueble { ESCRITORIO, CONSOLA, MESA, CAMA, NUCLEO, CAJAS, TUBO, BANCO }

enum class JuegoSala(val nombre: String, val suelo: JuegoTile) {
    ESCLUSA("Esclusa de aterrizaje", JuegoTile.SUELO_ENTRADA),
    OFICINA("Oficinas", JuegoTile.SUELO_OFICINA),
    LABORATORIO("Laboratorio", JuegoTile.SUELO_LAB),
    REACTOR("Sala del reactor", JuegoTile.SUELO_REACTOR),
    ENFERMERIA("Enfermería", JuegoTile.SUELO_ENFERMERIA),
    ALMACEN("Almacén", JuegoTile.SUELO_ALMACEN),
    DORMITORIO("Dormitorios", JuegoTile.SUELO_DORM),
    COMEDOR("Comedor", JuegoTile.SUELO_COMEDOR),
    MANDO("Sala de mando", JuegoTile.SUELO_MANDO)
}

enum class JuegoSprite {
    ASTRONAUTA, ROBOT, ALIEN, DRON, CENTINELA, TORRETA, CAJA, TRAMPA, TANQUE, TARJETA,
    BOTIQUIN, ESCUDO, CRISTAL, MODULO, PULSO, CORAZON, COHETE, CANDADO, COFRE,
    ARMA_PISTOLA, ARMA_SUBFUSIL, ARMA_ESCOPETA, ARMA_RIFLE, ARMA_PLASMA, ARMA_EM,
    MUNICION, CASCO, PECHERA, BRAZOS, PIERNAS, MOCHILA, GENERADOR,
    CHATARRA, CIRCUITO, NUCLEO, CELDA, ENGRANAJE, PLACA, TEJIDO, MODULO_CHIP, BOLSA,
    ROBOT_COMBATE, ROBOT_PESADO, DRON_ATAQUE, GUARDIA, BLINDADO
}

// ───────────────────────── Rareza, daño y categorías ─────────────────────────

enum class JuegoRareza(val nombre: String, val color: Color, val mult: Float) {
    COMUN("Común", Color(0xFF9E9E9E), 1.0f),
    POCO_COMUN("Poco común", Color(0xFF66BB6A), 1.1f),
    RARO("Raro", Color(0xFF42A5F5), 1.22f),
    EPICO("Épico", Color(0xFFAB47BC), 1.38f),
    LEGENDARIO("Legendario", Color(0xFFFFC107), 1.6f)
}

enum class JuegoTipoDanio(val nombre: String, val color: Color, val energetico: Boolean) {
    CINETICO("Cinético", Color(0xFFFFE082), false),
    PLASMA("Plasma", Color(0xFFE040FB), true),
    ELECTRICO("Electromagnético", Color(0xFF40C4FF), true)
}

enum class JuegoCategoria(val titulo: String) {
    ARMAS("Armas"),
    MUNICION("Munición"),
    MATERIALES("Materiales"),
    CONSUMIBLES("Consumibles"),
    ARMADURAS("Armaduras"),
    TECNOLOGIA("Tecnología")
}

enum class JuegoRanura(val titulo: String) {
    ARMA("Arma"),
    CASCO("Casco"),
    PECHERA("Pechera"),
    BRAZOS("Brazos"),
    PIERNAS("Piernas"),
    MOCHILA("Soporte vital"),
    TANQUE("Tanque de oxígeno"),
    ESCUDO("Generador de escudo"),
    MODULO1("Módulo 1"),
    MODULO2("Módulo 2")
}

enum class JuegoFamilia(val nombre: String, val danio: JuegoTipoDanio) {
    PISTOLA("Pistola", JuegoTipoDanio.CINETICO),
    SUBFUSIL("Subfusil", JuegoTipoDanio.CINETICO),
    ESCOPETA("Escopeta", JuegoTipoDanio.CINETICO),
    RIFLE_PRECISION("Rifle de precisión", JuegoTipoDanio.CINETICO),
    RIFLE_PLASMA("Rifle de plasma", JuegoTipoDanio.PLASMA),
    ELECTROMAGNETICA("Arma electromagnética", JuegoTipoDanio.ELECTRICO)
}

fun JuegoFamilia.municion(): JuegoTipoItem = when (this) {
    JuegoFamilia.PISTOLA, JuegoFamilia.SUBFUSIL -> JuegoTipoItem.MUN_BALAS
    JuegoFamilia.ESCOPETA -> JuegoTipoItem.MUN_CARTUCHOS
    JuegoFamilia.RIFLE_PRECISION -> JuegoTipoItem.MUN_PRECISION
    JuegoFamilia.RIFLE_PLASMA -> JuegoTipoItem.MUN_PLASMA
    JuegoFamilia.ELECTROMAGNETICA -> JuegoTipoItem.MUN_EM
}

/** Estadísticas base de un objeto. Se usa tanto para armas como para equipo y consumibles. */
data class JuegoBase(
    val dano: Float = 0f,
    val alcance: Int = 0,
    val precision: Int = 0,
    val critico: Int = 0,
    val critMult: Int = 150,
    val cargador: Int = 0,
    val recarga: Int = 0,
    val disparos: Int = 1,
    val consumo: Int = 1,
    val caida: Int = 0,
    val penetracion: Int = 0,
    val aturdir: Int = 0,
    val splash: Int = 0,
    val ahorro: Int = 0,
    val familia: JuegoFamilia? = null,
    val defensa: Int = 0,
    val integridad: Int = 0,
    val oxigeno: Int = 0,
    val eficiencia: Int = 0,
    val toxica: Int = 0,
    val escudo: Int = 0,
    val regen: Int = 0,
    val resEnergia: Int = 0,
    val capacidad: Int = 0,
    val menosRecarga: Int = 0,
    val botin: Int = 0,
    val cura: Int = 0
)

/** Totales del traje, ya limitados por los topes. */
data class JuegoTraje(
    val defensa: Int = 0,
    val integridad: Int = 0,
    val oxigeno: Int = 0,
    val eficiencia: Int = 0,
    val toxica: Int = 0,
    val escudoMax: Int = 0,
    val regen: Int = 6,
    val resEnergia: Int = 0,
    val capacidad: Int = 0,
    val precision: Int = 0,
    val critico: Int = 0,
    val menosRecarga: Int = 0,
    val botin: Int = 0
)

// ───────────────────────── Catálogo de objetos ─────────────────────────

enum class JuegoTipoItem(
    val nombre: String,
    val desc: String,
    val cat: JuegoCategoria,
    val icono: JuegoSprite,
    val base: JuegoBase = JuegoBase(),
    val ranura: JuegoRanura? = null,
    val pila: Int = 1,
    val rarezaBase: JuegoRareza = JuegoRareza.COMUN,
    val tinte: Color = Color.Unspecified,
    val mejorable: Boolean = false
) {
    // ── Armas ──
    ARMA_PISTOLA(
        "Pistola de servicio", "Arma reglamentaria de la estación. Fiable y barata de municionar.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_PISTOLA,
        JuegoBase(dano = 2f, alcance = 4, precision = 85, critico = 5, critMult = 150, cargador = 12, recarga = 1, caida = 3, familia = JuegoFamilia.PISTOLA),
        JuegoRanura.ARMA, mejorable = true
    ),
    ARMA_SUBFUSIL(
        "Subfusil táctico", "Dispara ráfagas de 3 balas. Gasta munición muy rápido pero castiga a corta distancia.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_SUBFUSIL,
        JuegoBase(dano = 1.3f, alcance = 4, precision = 72, critico = 4, critMult = 150, cargador = 30, recarga = 2, disparos = 3, consumo = 3, caida = 4, familia = JuegoFamilia.SUBFUSIL),
        JuegoRanura.ARMA, mejorable = true
    ),
    ARMA_ESCOPETA(
        "Escopeta de asalto", "Cuatro perdigones por disparo. Devastadora de cerca, inútil de lejos.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_ESCOPETA,
        JuegoBase(dano = 1.6f, alcance = 3, precision = 80, critico = 5, critMult = 150, cargador = 6, recarga = 2, disparos = 4, consumo = 1, caida = 12, familia = JuegoFamilia.ESCOPETA),
        JuegoRanura.ARMA, mejorable = true
    ),
    ARMA_RIFLE_PRECISION(
        "Rifle de precisión", "Gran alcance y críticos letales. Perfora parte de la armadura.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_RIFLE,
        JuegoBase(dano = 5f, alcance = 8, precision = 92, critico = 20, critMult = 200, cargador = 5, recarga = 2, caida = 0, penetracion = 25, familia = JuegoFamilia.RIFLE_PRECISION),
        JuegoRanura.ARMA, mejorable = true
    ),
    ARMA_RIFLE_PLASMA(
        "Rifle de plasma", "Proyectiles de plasma que ignoran la mitad de la defensa enemiga.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_PLASMA,
        JuegoBase(dano = 4f, alcance = 6, precision = 80, critico = 10, critMult = 175, cargador = 8, recarga = 2, caida = 2, penetracion = 50, familia = JuegoFamilia.RIFLE_PLASMA),
        JuegoRanura.ARMA, mejorable = true
    ),
    ARMA_EM(
        "Cañón electromagnético", "Daño electromagnético: letal contra robots y drones, y puede aturdir.",
        JuegoCategoria.ARMAS, JuegoSprite.ARMA_EM,
        JuegoBase(dano = 3f, alcance = 5, precision = 88, critico = 5, critMult = 150, cargador = 6, recarga = 2, caida = 2, aturdir = 35, familia = JuegoFamilia.ELECTROMAGNETICA),
        JuegoRanura.ARMA, mejorable = true
    ),

    // ── Munición ──
    MUN_BALAS("Balas de 9 mm", "Munición para pistolas y subfusiles.", JuegoCategoria.MUNICION, JuegoSprite.MUNICION, pila = 60, tinte = Color(0xFFFFB74D)),
    MUN_CARTUCHOS("Cartuchos de escopeta", "Munición para escopetas.", JuegoCategoria.MUNICION, JuegoSprite.MUNICION, pila = 30, tinte = Color(0xFFEF5350)),
    MUN_PRECISION("Munición de precisión", "Munición para rifles de precisión.", JuegoCategoria.MUNICION, JuegoSprite.MUNICION, pila = 30, tinte = Color(0xFFB0BEC5)),
    MUN_PLASMA("Cargas de plasma", "Munición para rifles de plasma.", JuegoCategoria.MUNICION, JuegoSprite.MUNICION, pila = 30, tinte = Color(0xFFE040FB)),
    MUN_EM("Cargas electromagnéticas", "Munición para armas electromagnéticas.", JuegoCategoria.MUNICION, JuegoSprite.MUNICION, pila = 30, tinte = Color(0xFF40C4FF)),

    // ── Materiales ──
    MAT_CHATARRA("Chatarra metálica", "Restos de metal. La base de casi toda fabricación.", JuegoCategoria.MATERIALES, JuegoSprite.CHATARRA, pila = 60),
    MAT_CIRCUITOS("Circuitos electrónicos", "Placas y componentes electrónicos recuperables.", JuegoCategoria.MATERIALES, JuegoSprite.CIRCUITO, pila = 40),
    MAT_NUCLEO("Núcleo robótico", "Unidad de procesamiento de un robot. Escasa.", JuegoCategoria.MATERIALES, JuegoSprite.NUCLEO, pila = 20, rarezaBase = JuegoRareza.RARO, tinte = Color(0xFFFF7043)),
    MAT_CELDA("Célula de energía", "Batería compacta para armas y mejoras.", JuegoCategoria.MATERIALES, JuegoSprite.CELDA, pila = 30, rarezaBase = JuegoRareza.POCO_COMUN),
    MAT_COMPONENTE("Componente de arma", "Piezas de precisión para fabricar y mejorar armas.", JuegoCategoria.MATERIALES, JuegoSprite.ENGRANAJE, pila = 20, rarezaBase = JuegoRareza.POCO_COMUN),
    MAT_PLACA("Placa de blindaje", "Plancha de aleación para reforzar armaduras.", JuegoCategoria.MATERIALES, JuegoSprite.PLACA, pila = 20, rarezaBase = JuegoRareza.POCO_COMUN),
    MAT_TEJIDO("Tejido biológico", "Muestra orgánica de un alien. Útil en medicina.", JuegoCategoria.MATERIALES, JuegoSprite.TEJIDO, pila = 40),
    MAT_CRISTAL_ORG("Cristal orgánico", "Cristal que crece en los aliens. Conduce energía.", JuegoCategoria.MATERIALES, JuegoSprite.CRISTAL, pila = 20, rarezaBase = JuegoRareza.POCO_COMUN, tinte = Color(0xFF66BB6A)),
    MAT_NUCLEO_MUTADO("Núcleo mutado", "Corazón mutante cargado de energía inestable.", JuegoCategoria.MATERIALES, JuegoSprite.NUCLEO, pila = 10, rarezaBase = JuegoRareza.RARO, tinte = Color(0xFFAB47BC)),
    MAT_ESPECIAL("Material especial", "Aleación exótica. Extremadamente rara.", JuegoCategoria.MATERIALES, JuegoSprite.CRISTAL, pila = 5, rarezaBase = JuegoRareza.EPICO, tinte = Color(0xFFFFC107)),

    // ── Consumibles ──
    CON_BOTIQUIN("Botiquín", "Restaura 5 de vida. Usarlo cuesta un turno.", JuegoCategoria.CONSUMIBLES, JuegoSprite.BOTIQUIN, JuegoBase(cura = 5), pila = 5),
    CON_BOTIQUIN_AV("Botiquín avanzado", "Restaura 9 de vida. Usarlo cuesta un turno.", JuegoCategoria.CONSUMIBLES, JuegoSprite.BOTIQUIN, JuegoBase(cura = 9), pila = 5, rarezaBase = JuegoRareza.POCO_COMUN, tinte = Color(0xFF66BB6A)),
    CON_OXIGENO("Cápsula de oxígeno", "Recarga 25 de oxígeno. Usarla cuesta un turno.", JuegoCategoria.CONSUMIBLES, JuegoSprite.TANQUE, JuegoBase(oxigeno = 25), pila = 5),
    CON_CELDA_ESCUDO("Celda de escudo", "Recarga el generador de escudo (o da un escudo de un golpe).", JuegoCategoria.CONSUMIBLES, JuegoSprite.ESCUDO, pila = 5, rarezaBase = JuegoRareza.POCO_COMUN),
    CON_CONDENSADOR("Condensador de pulso", "Reinicia al instante la recarga del pulso electromagnético.", JuegoCategoria.CONSUMIBLES, JuegoSprite.PULSO, pila = 5, rarezaBase = JuegoRareza.POCO_COMUN),

    // ── Armaduras ──
    CASCO_SERVICIO(
        "Casco de servicio", "Casco sellado estándar con un filtro básico.",
        JuegoCategoria.ARMADURAS, JuegoSprite.CASCO, JuegoBase(integridad = 2, toxica = 10), JuegoRanura.CASCO, mejorable = true
    ),
    CASCO_FILTRO(
        "Casco con filtro atmosférico", "Filtro avanzado que reduce el oxígeno extra gastado en zonas contaminadas.",
        JuegoCategoria.ARMADURAS, JuegoSprite.CASCO, JuegoBase(integridad = 1, toxica = 35, eficiencia = 3), JuegoRanura.CASCO, mejorable = true
    ),
    PECHERA_SERVICIO(
        "Pechera de servicio", "Protección torácica ligera.",
        JuegoCategoria.ARMADURAS, JuegoSprite.PECHERA, JuegoBase(defensa = 1, integridad = 2), JuegoRanura.PECHERA, mejorable = true
    ),
    PECHERA_REFORZADA(
        "Pechera reforzada", "Placas de aleación que reducen el daño recibido.",
        JuegoCategoria.ARMADURAS, JuegoSprite.PECHERA, JuegoBase(defensa = 2, integridad = 4, resEnergia = 1), JuegoRanura.PECHERA, mejorable = true
    ),
    BRAZOS_SERVICIO(
        "Guanteletes de servicio", "Mejoran el agarre y la puntería.",
        JuegoCategoria.ARMADURAS, JuegoSprite.BRAZOS, JuegoBase(precision = 3, integridad = 1), JuegoRanura.BRAZOS, mejorable = true
    ),
    BRAZOS_TACTICOS(
        "Brazos tácticos", "Estabilizadores y blindaje ligero en los antebrazos.",
        JuegoCategoria.ARMADURAS, JuegoSprite.BRAZOS, JuegoBase(precision = 5, defensa = 1, critico = 2), JuegoRanura.BRAZOS, mejorable = true
    ),
    PIERNAS_SERVICIO(
        "Grebas de servicio", "Actuadores que ahorran un poco de oxígeno al caminar.",
        JuegoCategoria.ARMADURAS, JuegoSprite.PIERNAS, JuegoBase(eficiencia = 4, integridad = 1), JuegoRanura.PIERNAS, mejorable = true
    ),
    PIERNAS_ASALTO(
        "Piernas de asalto", "Servomotores eficientes y rodilleras blindadas.",
        JuegoCategoria.ARMADURAS, JuegoSprite.PIERNAS, JuegoBase(eficiencia = 6, defensa = 1, integridad = 2), JuegoRanura.PIERNAS, mejorable = true
    ),

    // ── Equipamiento tecnológico ──
    MOCHILA_BASICA(
        "Mochila de soporte básica", "Amplía la capacidad de carga y recicla algo de aire.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MOCHILA, JuegoBase(capacidad = 4, oxigeno = 10, eficiencia = 4), JuegoRanura.MOCHILA, mejorable = true
    ),
    MOCHILA_SOPORTE(
        "Sistema de soporte vital", "Reduce el consumo de oxígeno por turno y amplía la mochila.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MOCHILA, JuegoBase(capacidad = 6, oxigeno = 15, eficiencia = 12), JuegoRanura.MOCHILA, mejorable = true
    ),
    TANQUE_OXIGENO(
        "Tanque de oxígeno", "Aumenta la capacidad máxima de oxígeno. Se puede mejorar en el banco.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.TANQUE, JuegoBase(oxigeno = 25), JuegoRanura.TANQUE, mejorable = true
    ),
    TANQUE_PRESURIZADO(
        "Tanque presurizado", "Más compacto y con válvulas eficientes.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.TANQUE, JuegoBase(oxigeno = 34, eficiencia = 4), JuegoRanura.TANQUE, mejorable = true
    ),
    GENERADOR_ESCUDO(
        "Generador de escudo", "Genera puntos de escudo que absorben daño y se regeneran solos.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.GENERADOR, JuegoBase(escudo = 3, regen = 6), JuegoRanura.ESCUDO, mejorable = true
    ),
    GENERADOR_AVANZADO(
        "Generador de escudo mejorado", "Mayor capacidad de escudo y resistencia a ataques energéticos.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.GENERADOR, JuegoBase(escudo = 5, regen = 5, resEnergia = 1), JuegoRanura.ESCUDO, mejorable = true
    ),
    MOD_PUNTERIA(
        "Módulo de puntería", "Estabiliza el arma: +6% de precisión.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MODULO_CHIP, JuegoBase(precision = 6), JuegoRanura.MODULO1, rarezaBase = JuegoRareza.POCO_COMUN
    ),
    MOD_FILTRO(
        "Módulo filtro de toxinas", "Reduce el oxígeno extra gastado en fluidos tóxicos.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MODULO_CHIP, JuegoBase(toxica = 30), JuegoRanura.MODULO1, rarezaBase = JuegoRareza.POCO_COMUN
    ),
    MOD_ESTABILIZADOR(
        "Módulo estabilizador", "Disipa energía: +2 de resistencia a ataques energéticos.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MODULO_CHIP, JuegoBase(resEnergia = 2), JuegoRanura.MODULO1, rarezaBase = JuegoRareza.POCO_COMUN
    ),
    MOD_RECARGA(
        "Módulo de recarga rápida", "Reduce en 1 turno el tiempo de recarga de las armas.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MODULO_CHIP, JuegoBase(menosRecarga = 1), JuegoRanura.MODULO1, rarezaBase = JuegoRareza.POCO_COMUN
    ),
    MOD_ESCANER(
        "Escáner de recuperación", "Aumenta un 15% la probabilidad de obtener materiales de los enemigos.",
        JuegoCategoria.TECNOLOGIA, JuegoSprite.MODULO_CHIP, JuegoBase(botin = 15), JuegoRanura.MODULO1, rarezaBase = JuegoRareza.POCO_COMUN
    )
}

// ───────────────────────── Enemigos ─────────────────────────

enum class JuegoCategoriaEnemigo { COMUN, ESPECIAL, JEFE }

enum class JuegoIA { CUERPO, DISTANCIA, ESTATICO, DRON, JEFE }

data class JuegoDrop(val tipo: JuegoTipoItem, val prob: Int, val min: Int = 1, val max: Int = 1)

enum class JuegoTipoEnemigo(
    val nombre: String,
    val vidaMax: Int,
    val danio: Int,
    val defensa: Int,
    val alcance: Int,
    val precision: Int,
    val resCin: Int,
    val resPla: Int,
    val resEle: Int,
    val puntos: Int,
    val categoria: JuegoCategoriaEnemigo,
    val ia: JuegoIA,
    val danioTipo: JuegoTipoDanio,
    val probEquipo: Int,
    val periodo: Int,
    val errante: Int,
    val botin: List<JuegoDrop>
) {
    ROBOT(
        "Robot", 3, 2, 0, 1, 100, 100, 100, 130, 30, JuegoCategoriaEnemigo.COMUN, JuegoIA.CUERPO,
        JuegoTipoDanio.CINETICO, 0, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 85, 1, 3), JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 45, 1, 2),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 12), JuegoDrop(JuegoTipoItem.MAT_CELDA, 18),
            JuegoDrop(JuegoTipoItem.MAT_COMPONENTE, 6), JuegoDrop(JuegoTipoItem.MAT_PLACA, 8),
            JuegoDrop(JuegoTipoItem.MUN_BALAS, 20, 3, 6)
        )
    ),
    ALIEN(
        "Alien", 5, 3, 0, 1, 100, 100, 120, 80, 50, JuegoCategoriaEnemigo.COMUN, JuegoIA.CUERPO,
        JuegoTipoDanio.CINETICO, 0, 1, 40,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_TEJIDO, 80, 1, 2), JuegoDrop(JuegoTipoItem.MAT_CRISTAL_ORG, 40),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO_MUTADO, 10), JuegoDrop(JuegoTipoItem.MAT_ESPECIAL, 4)
        )
    ),
    DRON(
        "Dron", 4, 2, 0, 4, 75, 100, 100, 150, 40, JuegoCategoriaEnemigo.COMUN, JuegoIA.DRON,
        JuegoTipoDanio.ELECTRICO, 0, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 70, 1, 2), JuegoDrop(JuegoTipoItem.MAT_CELDA, 30),
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 40, 1, 2), JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 6)
        )
    ),
    TORRETA(
        "Torreta", 7, 3, 1, 5, 80, 80, 100, 150, 70, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.ESTATICO,
        JuegoTipoDanio.PLASMA, 0, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_COMPONENTE, 40, 1, 2), JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 60, 2, 4),
            JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 35, 1, 2), JuegoDrop(JuegoTipoItem.MAT_CELDA, 25)
        )
    ),
    ROBOT_COMBATE(
        "Robot de combate", 6, 2, 1, 5, 75, 90, 100, 130, 90, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.DISTANCIA,
        JuegoTipoDanio.CINETICO, 30, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 80, 1, 3), JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 55, 1, 2),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 18), JuegoDrop(JuegoTipoItem.MAT_CELDA, 25),
            JuegoDrop(JuegoTipoItem.MAT_COMPONENTE, 20), JuegoDrop(JuegoTipoItem.MAT_PLACA, 15),
            JuegoDrop(JuegoTipoItem.MUN_BALAS, 35, 4, 8)
        )
    ),
    ROBOT_PESADO(
        "Robot pesado", 14, 4, 1, 1, 100, 70, 100, 140, 120, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.CUERPO,
        JuegoTipoDanio.CINETICO, 28, 2, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 95, 2, 5), JuegoDrop(JuegoTipoItem.MAT_PLACA, 55, 1, 2),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 25), JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 40, 1, 2),
            JuegoDrop(JuegoTipoItem.MAT_CELDA, 20)
        )
    ),
    DRON_ATAQUE(
        "Dron de ataque", 5, 2, 0, 4, 85, 100, 100, 160, 80, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.DRON,
        JuegoTipoDanio.PLASMA, 25, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 75, 1, 3), JuegoDrop(JuegoTipoItem.MAT_CELDA, 40),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 10), JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 40, 1, 2)
        )
    ),
    GUARDIA(
        "Guardia", 6, 2, 0, 4, 80, 100, 100, 100, 70, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.DISTANCIA,
        JuegoTipoDanio.CINETICO, 35, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 50, 1, 2), JuegoDrop(JuegoTipoItem.MUN_BALAS, 55, 4, 8),
            JuegoDrop(JuegoTipoItem.CON_BOTIQUIN, 15), JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 20)
        )
    ),
    BLINDADO(
        "Blindado", 10, 3, 2, 1, 100, 60, 80, 120, 110, JuegoCategoriaEnemigo.ESPECIAL, JuegoIA.CUERPO,
        JuegoTipoDanio.CINETICO, 30, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_CHATARRA, 80, 1, 3), JuegoDrop(JuegoTipoItem.MAT_PLACA, 60, 1, 2),
            JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 35), JuegoDrop(JuegoTipoItem.MAT_COMPONENTE, 12),
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 12)
        )
    ),
    CENTINELA(
        "Centinela", 34, 4, 1, 5, 90, 75, 100, 120, 300, JuegoCategoriaEnemigo.JEFE, JuegoIA.JEFE,
        JuegoTipoDanio.PLASMA, 60, 1, 0,
        listOf(
            JuegoDrop(JuegoTipoItem.MAT_NUCLEO, 100, 2, 3), JuegoDrop(JuegoTipoItem.MAT_CIRCUITOS, 100, 3, 5),
            JuegoDrop(JuegoTipoItem.MAT_CELDA, 100, 2, 3), JuegoDrop(JuegoTipoItem.MAT_COMPONENTE, 100, 2, 3),
            JuegoDrop(JuegoTipoItem.MAT_PLACA, 100, 2, 2), JuegoDrop(JuegoTipoItem.MAT_ESPECIAL, 60)
        )
    )
}

// ───────────────────────── Estado de partida ─────────────────────────

data class JuegoPos(val fila: Int, val col: Int)

data class JuegoRect(val x: Int, val y: Int, val w: Int, val h: Int)

data class JuegoSalaInfo(val rect: JuegoRect, val tipo: JuegoSala)

/** Un objeto del inventario. Las armas guardan su cargador en [municion]. */
data class JuegoItem(
    val uid: Int,
    val tipo: JuegoTipoItem,
    val rareza: JuegoRareza = JuegoRareza.COMUN,
    val nivel: Int = 1,
    val cantidad: Int = 1,
    val municion: Int = 0
)

data class JuegoEnemigo(
    val id: Int,
    val tipo: JuegoTipoEnemigo,
    val pos: JuegoPos,
    val vida: Int,
    val vidaMax: Int,
    val danio: Int,
    val defensa: Int,
    val vivo: Boolean = true,
    val despierto: Boolean = false,
    val aturdido: Int = 0,
    val carga: Int = 0,
    val equipo: JuegoItem? = null
)

data class JuegoObjeto(
    val id: Int,
    val tipo: JuegoTipoObjeto,
    val pos: JuegoPos,
    val activo: Boolean = true,
    val items: List<JuegoItem> = emptyList()
)

data class JuegoPopup(
    val id: Int,
    val pos: JuegoPos,
    val texto: String,
    val color: Color
)

data class JuegoDisparo(
    val id: Int,
    val desde: JuegoPos,
    val hasta: JuegoPos,
    val danio: JuegoTipoDanio,
    val acierta: Boolean,
    val retraso: Int,
    val enemigo: Boolean
)

enum class JuegoTipoEfecto { EXPLOSION, IMPACTO, DESTELLO, ADVERTENCIA }

data class JuegoEfecto(
    val id: Int,
    val pos: JuegoPos,
    val tipo: JuegoTipoEfecto,
    val color: Color,
    val radio: Int = 1
)

data class JuegoAviso(
    val id: Int,
    val texto: String,
    val color: Color,
    val icono: JuegoSprite
)

data class JuegoEstadoUi(
    val fase: JuegoFase = JuegoFase.JUGANDO,
    val piso: Int = 1,
    val ancho: Int,
    val alto: Int,
    val dificultad: JuegoDificultad = JuegoDificultad.NORMAL,
    val mapa: List<JuegoTile>,
    val muebles: Map<Int, JuegoMueble>,
    val salas: List<JuegoSalaInfo>,
    val jugador: JuegoPos,
    val vida: Int,
    val vidaMax: Int,
    val oxigeno: Int,
    val oxigenoMax: Int,
    val tieneTarjeta: Boolean = false,
    val escudo: Boolean = false,
    val escudoPts: Int = 0,
    val escudoRegen: Int = 0,
    val bonoDanio: Int = 0,
    val bonoCritico: Int = 0,
    val bonoVida: Int = 0,
    val bonoOxigeno: Int = 0,
    val pulsoEspera: Int = 0,
    val pulsoRecarga: Int = JUEGO_RECARGA_PULSO,
    val pulsoContador: Int = 0,
    val equipo: Map<JuegoRanura, JuegoItem> = emptyMap(),
    val inventario: List<JuegoItem> = emptyList(),
    val itemId: Int = 1,
    val recarga: Int = 0,
    val oxiAcum: Int = 0,
    val objetos: List<JuegoObjeto>,
    val enemigos: List<JuegoEnemigo>,
    val trampas: Set<Int> = emptySet(),
    val visibles: Set<Int> = emptySet(),
    val explorado: Set<Int> = emptySet(),
    val mensaje: String = "",
    val golpes: Set<JuegoPos> = emptySet(),
    val golpeContador: Int = 0,
    val popups: List<JuegoPopup> = emptyList(),
    val popupId: Int = 0,
    val disparos: List<JuegoDisparo> = emptyList(),
    val disparoId: Int = 0,
    val efectos: List<JuegoEfecto> = emptyList(),
    val efectoId: Int = 0,
    val avisos: List<JuegoAviso> = emptyList(),
    val avisoId: Int = 0,
    val puntos: Int = 0,
    val derrotados: Int = 0,
    val combo: Int = 0,
    val comboMax: Int = 0,
    val ultimoKill: Int = 0,
    val invocados: Int = 0,
    val turno: Int = 0,
    val opciones: List<JuegoMejora> = emptyList(),
    val abrirBanco: Boolean = false
)
