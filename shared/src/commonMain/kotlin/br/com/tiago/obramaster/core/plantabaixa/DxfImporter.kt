package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.hypot
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class UnidadeDxf { MILIMETROS, CENTIMETROS, METROS, POLEGADAS, DESCONHECIDA }

/**
 * SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §2 — parser de DXF ASCII puro em Kotlin, sem
 * dependência de plataforma. `paredes`/`comodos` saem com `plantaId` vazio — quem chama associa
 * à planta de destino antes de persistir (o parser não conhece esse contexto).
 */
object DxfImporter {

    data class ResultadoImportacaoDxf(
        val paredes: List<Parede>,
        val comodos: List<Comodo>,
        val unidadeDetectada: UnidadeDxf,
        val escalaAutomaticaPxPorMetro: Double?,
        val camadasEncontradas: List<String>,
        val elementosIgnorados: Int,
    )

    /** Convenção do editor: 1 metro real = 100px quando a escala é calibrada automaticamente. */
    private const val ESCALA_PX_POR_METRO_PADRAO = 100.0

    private data class Tag(val codigo: Int, val valor: String)

    private data class EntidadeDxf(
        val tipo: String,
        val layer: String?,
        val pontos: List<PontoXY>,
        val fechado: Boolean,
        val texto: String?,
    )

    @OptIn(ExperimentalUuidApi::class)
    fun importar(conteudoDxf: String, camadasSelecionadas: Set<String>? = null): ResultadoImportacaoDxf {
        val tags = tokenizar(conteudoDxf)
        val unidade = detectarUnidade(tags)
        val escalaAuto = if (unidade != UnidadeDxf.DESCONHECIDA) ESCALA_PX_POR_METRO_PADRAO else null
        val fatorParaPx = if (escalaAuto != null) fatorConversaoParaMetros(unidade) * escalaAuto else 1.0

        val entidades = extrairEntidades(tags)
        val camadasEncontradas = entidades.mapNotNull { it.layer }.distinct().sorted()
        val textos = entidades.filter { it.tipo == "TEXT" || it.tipo == "MTEXT" }

        val paredes = mutableListOf<Parede>()
        data class ComodoBruto(val pontosBrutos: List<PontoXY>)
        val comodosBrutos = mutableListOf<ComodoBruto>()
        var ignorados = 0

        for (entidade in entidades) {
            if (entidade.tipo == "TEXT" || entidade.tipo == "MTEXT") continue
            if (camadasSelecionadas != null && entidade.layer != null && entidade.layer !in camadasSelecionadas) {
                ignorados++
                continue
            }
            when (entidade.tipo) {
                "LINE" -> {
                    if (entidade.pontos.size >= 2) {
                        paredes += Parede(
                            id = Uuid.random().toString(),
                            plantaId = "",
                            pontoInicio = converter(entidade.pontos[0], fatorParaPx),
                            pontoFim = converter(entidade.pontos[1], fatorParaPx),
                        )
                    } else {
                        ignorados++
                    }
                }

                "LWPOLYLINE", "POLYLINE" -> {
                    when {
                        entidade.fechado && entidade.pontos.size >= 3 -> comodosBrutos += ComodoBruto(entidade.pontos)
                        entidade.pontos.size >= 2 -> {
                            for (indice in 0 until entidade.pontos.size - 1) {
                                paredes += Parede(
                                    id = Uuid.random().toString(),
                                    plantaId = "",
                                    pontoInicio = converter(entidade.pontos[indice], fatorParaPx),
                                    pontoFim = converter(entidade.pontos[indice + 1], fatorParaPx),
                                )
                            }
                        }
                        else -> ignorados++
                    }
                }

                "CIRCLE" -> ignorados++
                else -> ignorados++
            }
        }

        val comodos = comodosBrutos.map { bruto ->
            val centroideBruto = centroide(bruto.pontosBrutos)
            val nome = textoMaisProximo(centroideBruto, bruto.pontosBrutos, textos)
            val pontosPx = bruto.pontosBrutos.map { converter(it, fatorParaPx) }
            val escalaParaCalculo = escalaAuto ?: 1.0
            Comodo(
                id = Uuid.random().toString(),
                plantaId = "",
                nome = nome ?: "Cômodo importado",
                pontos = pontosPx,
                corPreenchimento = "#90CAF9",
                areaM2 = if (escalaAuto != null) PlantaBaixaEngine.calcularAreaM2(pontosPx, escalaParaCalculo) else 0.0,
                perimetroM = if (escalaAuto != null) PlantaBaixaEngine.calcularPerimetroM(pontosPx, escalaParaCalculo) else 0.0,
            )
        }

        return ResultadoImportacaoDxf(
            paredes = paredes,
            comodos = comodos,
            unidadeDetectada = unidade,
            escalaAutomaticaPxPorMetro = escalaAuto,
            camadasEncontradas = camadasEncontradas,
            elementosIgnorados = ignorados,
        )
    }

    private fun converter(ponto: PontoXY, fator: Double) = PontoXY(ponto.x * fator, ponto.y * fator)

    private fun centroide(pontos: List<PontoXY>) =
        PontoXY(pontos.sumOf { it.x } / pontos.size, pontos.sumOf { it.y } / pontos.size)

    /** Texto mais próximo do centróide, mas só aceito se estiver dentro do "raio" do próprio cômodo (heurística sem unidade fixa). */
    private fun textoMaisProximo(centroide: PontoXY, pontosComodo: List<PontoXY>, textos: List<EntidadeDxf>): String? {
        if (textos.isEmpty()) return null
        val diagonal = diagonalBoundingBox(pontosComodo)
        val maisProximo = textos
            .mapNotNull { entidade -> entidade.pontos.firstOrNull()?.let { ponto -> Triple(ponto, entidade.texto, hypot(ponto.x - centroide.x, ponto.y - centroide.y)) } }
            .filter { (_, texto, _) -> !texto.isNullOrBlank() }
            .minByOrNull { (_, _, distancia) -> distancia }
            ?: return null
        return if (maisProximo.third <= diagonal) maisProximo.second else null
    }

    private fun diagonalBoundingBox(pontos: List<PontoXY>): Double {
        val minX = pontos.minOf { it.x }
        val maxX = pontos.maxOf { it.x }
        val minY = pontos.minOf { it.y }
        val maxY = pontos.maxOf { it.y }
        return hypot(maxX - minX, maxY - minY)
    }

    private fun fatorConversaoParaMetros(unidade: UnidadeDxf): Double = when (unidade) {
        UnidadeDxf.MILIMETROS -> 0.001
        UnidadeDxf.CENTIMETROS -> 0.01
        UnidadeDxf.METROS -> 1.0
        UnidadeDxf.POLEGADAS -> 0.0254
        UnidadeDxf.DESCONHECIDA -> 1.0
    }

    private fun detectarUnidade(tags: List<Tag>): UnidadeDxf {
        for (indice in tags.indices) {
            if (tags[indice].codigo == 9 && tags[indice].valor == "\$INSUNITS") {
                val proxima = tags.getOrNull(indice + 1) ?: continue
                if (proxima.codigo == 70) {
                    return when (proxima.valor.trim().toIntOrNull()) {
                        1 -> UnidadeDxf.POLEGADAS
                        4 -> UnidadeDxf.MILIMETROS
                        5 -> UnidadeDxf.CENTIMETROS
                        6 -> UnidadeDxf.METROS
                        else -> UnidadeDxf.DESCONHECIDA
                    }
                }
            }
        }
        return UnidadeDxf.DESCONHECIDA
    }

    private fun tokenizar(conteudo: String): List<Tag> {
        val linhas = conteudo.replace("\r\n", "\n").split("\n")
        val tags = mutableListOf<Tag>()
        var indice = 0
        while (indice + 1 < linhas.size) {
            val codigo = linhas[indice].trim().toIntOrNull()
            val valor = linhas[indice + 1].trim()
            if (codigo != null) tags += Tag(codigo, valor)
            indice += 2
        }
        return tags
    }

    private fun extrairEntidades(tags: List<Tag>): List<EntidadeDxf> {
        val entidades = mutableListOf<EntidadeDxf>()
        var dentroEntities = false
        var indice = 0
        while (indice < tags.size) {
            val tag = tags[indice]

            if (tag.codigo == 0 && tag.valor == "SECTION" && tags.getOrNull(indice + 1)?.codigo == 2) {
                dentroEntities = tags[indice + 1].valor == "ENTITIES"
                indice += 2
                continue
            }
            if (tag.codigo == 0 && tag.valor == "ENDSEC") {
                dentroEntities = false
                indice++
                continue
            }
            if (!dentroEntities || tag.codigo != 0) {
                indice++
                continue
            }

            val tipo = tag.valor
            indice++
            when (tipo) {
                "LINE" -> {
                    var layer: String? = null
                    var x1: Double? = null
                    var y1: Double? = null
                    var x2: Double? = null
                    var y2: Double? = null
                    while (indice < tags.size && tags[indice].codigo != 0) {
                        when (tags[indice].codigo) {
                            8 -> layer = tags[indice].valor
                            10 -> x1 = tags[indice].valor.toDoubleOrNull()
                            20 -> y1 = tags[indice].valor.toDoubleOrNull()
                            11 -> x2 = tags[indice].valor.toDoubleOrNull()
                            21 -> y2 = tags[indice].valor.toDoubleOrNull()
                        }
                        indice++
                    }
                    val pontos = if (x1 != null && y1 != null && x2 != null && y2 != null) {
                        listOf(PontoXY(x1, y1), PontoXY(x2, y2))
                    } else {
                        emptyList()
                    }
                    entidades += EntidadeDxf("LINE", layer, pontos, false, null)
                }

                "LWPOLYLINE" -> {
                    var layer: String? = null
                    var flag = 0
                    val pontos = mutableListOf<PontoXY>()
                    var xAtual: Double? = null
                    while (indice < tags.size && tags[indice].codigo != 0) {
                        when (tags[indice].codigo) {
                            8 -> layer = tags[indice].valor
                            70 -> flag = tags[indice].valor.toIntOrNull() ?: 0
                            10 -> xAtual = tags[indice].valor.toDoubleOrNull()
                            20 -> {
                                val y = tags[indice].valor.toDoubleOrNull()
                                if (xAtual != null && y != null) pontos += PontoXY(xAtual, y)
                            }
                        }
                        indice++
                    }
                    entidades += EntidadeDxf("LWPOLYLINE", layer, pontos, (flag and 1) == 1, null)
                }

                "POLYLINE" -> {
                    var layer: String? = null
                    var flag = 0
                    while (indice < tags.size && tags[indice].codigo != 0) {
                        when (tags[indice].codigo) {
                            8 -> layer = tags[indice].valor
                            70 -> flag = tags[indice].valor.toIntOrNull() ?: 0
                        }
                        indice++
                    }
                    val pontos = mutableListOf<PontoXY>()
                    while (indice < tags.size && !(tags[indice].codigo == 0 && tags[indice].valor == "SEQEND")) {
                        if (tags[indice].codigo == 0 && tags[indice].valor == "VERTEX") {
                            indice++
                            var vx: Double? = null
                            var vy: Double? = null
                            while (indice < tags.size && tags[indice].codigo != 0) {
                                when (tags[indice].codigo) {
                                    10 -> vx = tags[indice].valor.toDoubleOrNull()
                                    20 -> vy = tags[indice].valor.toDoubleOrNull()
                                }
                                indice++
                            }
                            if (vx != null && vy != null) pontos += PontoXY(vx, vy)
                        } else {
                            indice++
                        }
                    }
                    if (indice < tags.size && tags[indice].codigo == 0 && tags[indice].valor == "SEQEND") indice++
                    entidades += EntidadeDxf("POLYLINE", layer, pontos, (flag and 1) == 1, null)
                }

                "TEXT", "MTEXT" -> {
                    var layer: String? = null
                    var x: Double? = null
                    var y: Double? = null
                    val textoBuilder = StringBuilder()
                    while (indice < tags.size && tags[indice].codigo != 0) {
                        when (tags[indice].codigo) {
                            8 -> layer = tags[indice].valor
                            10 -> x = tags[indice].valor.toDoubleOrNull()
                            20 -> y = tags[indice].valor.toDoubleOrNull()
                            1, 3 -> textoBuilder.append(tags[indice].valor)
                        }
                        indice++
                    }
                    val pontos = if (x != null && y != null) listOf(PontoXY(x, y)) else emptyList()
                    entidades += EntidadeDxf(tipo, layer, pontos, false, textoBuilder.toString().ifBlank { null })
                }

                else -> {
                    var layer: String? = null
                    while (indice < tags.size && tags[indice].codigo != 0) {
                        if (tags[indice].codigo == 8) layer = tags[indice].valor
                        indice++
                    }
                    entidades += EntidadeDxf(tipo, layer, emptyList(), false, null)
                }
            }
        }
        return entidades
    }
}
