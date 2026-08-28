package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.hypot
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §3 — parser de SVG (XML) puro em Kotlin, sem
 * dependência de plataforma. As coordenadas do SVG (dentro do viewBox) já são usadas
 * diretamente como "px" do editor — não precisam de conversão como no DxfImporter, porque o
 * viewBox já define esse espaço de coordenadas. `paredes`/`comodos` saem com `plantaId` vazio —
 * quem chama associa à planta de destino antes de persistir.
 */
object SvgImporter {

    data class ResultadoImportacaoSvg(
        val paredes: List<Parede>,
        val comodos: List<Comodo>,
        val escalaDetectadaAutomaticamente: Boolean,
        val escalaAutomaticaPxPorMetro: Double?,
    )

    private val REGEX_NUMERO = Regex("-?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?")
    private val REGEX_TAG = Regex("<(rect|line|polygon|polyline|path)\\b([^>]*)>", RegexOption.IGNORE_CASE)
    private val REGEX_COMANDO_PATH = Regex("([MmLlHhVvZzCcQqAa])([^MmLlHhVvZzCcQqAa]*)")

    @OptIn(ExperimentalUuidApi::class)
    fun importar(conteudoSvg: String): ResultadoImportacaoSvg {
        val (escalaDetectada, escalaAuto) = detectarEscala(conteudoSvg)

        val paredes = mutableListOf<Parede>()
        val comodos = mutableListOf<Comodo>()

        for (match in REGEX_TAG.findAll(conteudoSvg)) {
            val tag = match.groupValues[1].lowercase()
            val atributos = match.groupValues[2]

            when (tag) {
                "rect" -> {
                    val x = atributoNumero(atributos, "x") ?: 0.0
                    val y = atributoNumero(atributos, "y") ?: 0.0
                    val largura = atributoNumero(atributos, "width") ?: continue
                    val altura = atributoNumero(atributos, "height") ?: continue
                    if (largura <= 0.0 || altura <= 0.0) continue
                    val pontos = listOf(
                        PontoXY(x, y), PontoXY(x + largura, y), PontoXY(x + largura, y + altura), PontoXY(x, y + altura),
                    )
                    comodos += construirComodo(pontos, escalaAuto)
                }

                "line" -> {
                    val x1 = atributoNumero(atributos, "x1") ?: continue
                    val y1 = atributoNumero(atributos, "y1") ?: continue
                    val x2 = atributoNumero(atributos, "x2") ?: continue
                    val y2 = atributoNumero(atributos, "y2") ?: continue
                    paredes += construirParede(PontoXY(x1, y1), PontoXY(x2, y2))
                }

                "polygon" -> {
                    val pontos = extrairPontosDeAtributo(atributos, "points")
                    if (pontos.size >= 3) comodos += construirComodo(pontos, escalaAuto)
                }

                "polyline" -> {
                    val pontos = extrairPontosDeAtributo(atributos, "points")
                    adicionarFormaAberturaOuFechada(pontos, fechadoExplicito = false, paredes, comodos, escalaAuto)
                }

                "path" -> {
                    val d = extrairAtributo(atributos, "d") ?: continue
                    val fechado = d.contains('Z') || d.contains('z')
                    var pontos = parsePathParaPontos(d)
                    if (fechado && pontos.size >= 2 && quaseIguais(pontos.first(), pontos.last())) {
                        pontos = pontos.dropLast(1)
                    }
                    adicionarFormaAberturaOuFechada(pontos, fechadoExplicito = fechado, paredes, comodos, escalaAuto)
                }
            }
        }

        return ResultadoImportacaoSvg(paredes, comodos, escalaDetectada, escalaAuto)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun adicionarFormaAberturaOuFechada(
        pontos: List<PontoXY>,
        fechadoExplicito: Boolean,
        paredes: MutableList<Parede>,
        comodos: MutableList<Comodo>,
        escalaAuto: Double?,
    ) {
        val fechado = fechadoExplicito || (pontos.size >= 3 && quaseIguais(pontos.first(), pontos.last()))
        val pontosSemDuplicataDeFechamento = if (fechado && pontos.size >= 2 && quaseIguais(pontos.first(), pontos.last())) {
            pontos.dropLast(1)
        } else {
            pontos
        }
        when {
            fechado && pontosSemDuplicataDeFechamento.size >= 3 -> comodos += construirComodo(pontosSemDuplicataDeFechamento, escalaAuto)
            pontos.size >= 2 -> for (indice in 0 until pontos.size - 1) paredes += construirParede(pontos[indice], pontos[indice + 1])
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun construirComodo(pontos: List<PontoXY>, escalaAuto: Double?) = Comodo(
        id = Uuid.random().toString(),
        plantaId = "",
        nome = "Cômodo importado",
        pontos = pontos,
        corPreenchimento = "#90CAF9",
        areaM2 = if (escalaAuto != null) PlantaBaixaEngine.calcularAreaM2(pontos, escalaAuto) else 0.0,
        perimetroM = if (escalaAuto != null) PlantaBaixaEngine.calcularPerimetroM(pontos, escalaAuto) else 0.0,
    )

    @OptIn(ExperimentalUuidApi::class)
    private fun construirParede(a: PontoXY, b: PontoXY) = Parede(
        id = Uuid.random().toString(),
        plantaId = "",
        pontoInicio = a,
        pontoFim = b,
    )

    private fun quaseIguais(a: PontoXY, b: PontoXY, tolerancia: Double = 0.001): Boolean = hypot(b.x - a.x, b.y - a.y) <= tolerancia

    /** viewBox + width/height com unidade real (mm/cm/in) → escala automática; caso raro (spec §3.1). */
    private fun detectarEscala(conteudoSvg: String): Pair<Boolean, Double?> {
        val svgMatch = Regex("<svg\\b([^>]*)>", RegexOption.IGNORE_CASE).find(conteudoSvg) ?: return false to null
        val atributosSvg = svgMatch.groupValues[1]

        val viewBox = extrairAtributo(atributosSvg, "viewBox") ?: return false to null
        val numerosViewBox = extrairNumeros(viewBox)
        if (numerosViewBox.size < 4) return false to null
        val larguraViewBoxUnidades = numerosViewBox[2]

        val largura = extrairAtributo(atributosSvg, "width") ?: return false to null
        val (valorLargura, unidade) = separarNumeroEUnidade(largura) ?: return false to null
        val fatorParaMetros = when (unidade) {
            "mm" -> 0.001
            "cm" -> 0.01
            "m" -> 1.0
            "in" -> 0.0254
            else -> return false to null
        }
        val larguraRealMetros = valorLargura * fatorParaMetros
        if (larguraRealMetros <= 0.0) return false to null

        return true to (larguraViewBoxUnidades / larguraRealMetros)
    }

    private fun separarNumeroEUnidade(texto: String): Pair<Double, String>? {
        val match = Regex("(-?[0-9.]+)\\s*([a-zA-Z%]*)").find(texto.trim()) ?: return null
        val numero = match.groupValues[1].toDoubleOrNull() ?: return null
        return numero to match.groupValues[2].lowercase()
    }

    private fun extrairAtributo(atributos: String, nome: String): String? =
        Regex("$nome\\s*=\\s*\"([^\"]*)\"", RegexOption.IGNORE_CASE).find(atributos)?.groupValues?.get(1)

    private fun atributoNumero(atributos: String, nome: String): Double? =
        extrairAtributo(atributos, nome)?.toDoubleOrNull()

    private fun extrairNumeros(texto: String): List<Double> = REGEX_NUMERO.findAll(texto).map { it.value.toDouble() }.toList()

    private fun extrairPontosDeAtributo(atributos: String, nome: String): List<PontoXY> {
        val numeros = extrairNumeros(extrairAtributo(atributos, nome) ?: return emptyList())
        val pontos = mutableListOf<PontoXY>()
        var indice = 0
        while (indice + 2 <= numeros.size) {
            pontos += PontoXY(numeros[indice], numeros[indice + 1])
            indice += 2
        }
        return pontos
    }

    /** Comandos M/L/H/V/Z absolutos e relativos; C/Q/A aproximados por reta até o ponto final (spec §3.1). */
    private fun parsePathParaPontos(d: String): List<PontoXY> {
        val pontos = mutableListOf<PontoXY>()
        var atualX = 0.0
        var atualY = 0.0
        var inicioSubpathX = 0.0
        var inicioSubpathY = 0.0

        for (match in REGEX_COMANDO_PATH.findAll(d)) {
            val comando = match.groupValues[1]
            val numeros = extrairNumeros(match.groupValues[2])
            val relativo = comando[0].isLowerCase()

            when (comando.uppercase()) {
                "M" -> {
                    var indice = 0
                    var ehPrimeiro = true
                    while (indice + 2 <= numeros.size) {
                        atualX = if (relativo) atualX + numeros[indice] else numeros[indice]
                        atualY = if (relativo) atualY + numeros[indice + 1] else numeros[indice + 1]
                        pontos += PontoXY(atualX, atualY)
                        if (ehPrimeiro) {
                            inicioSubpathX = atualX
                            inicioSubpathY = atualY
                            ehPrimeiro = false
                        }
                        indice += 2
                    }
                }

                "L" -> {
                    var indice = 0
                    while (indice + 2 <= numeros.size) {
                        atualX = if (relativo) atualX + numeros[indice] else numeros[indice]
                        atualY = if (relativo) atualY + numeros[indice + 1] else numeros[indice + 1]
                        pontos += PontoXY(atualX, atualY)
                        indice += 2
                    }
                }

                "H" -> for (x in numeros) {
                    atualX = if (relativo) atualX + x else x
                    pontos += PontoXY(atualX, atualY)
                }

                "V" -> for (y in numeros) {
                    atualY = if (relativo) atualY + y else y
                    pontos += PontoXY(atualX, atualY)
                }

                "Z" -> {
                    atualX = inicioSubpathX
                    atualY = inicioSubpathY
                    pontos += PontoXY(atualX, atualY)
                }

                "C" -> {
                    var indice = 0
                    while (indice + 6 <= numeros.size) {
                        atualX = if (relativo) atualX + numeros[indice + 4] else numeros[indice + 4]
                        atualY = if (relativo) atualY + numeros[indice + 5] else numeros[indice + 5]
                        pontos += PontoXY(atualX, atualY)
                        indice += 6
                    }
                }

                "Q" -> {
                    var indice = 0
                    while (indice + 4 <= numeros.size) {
                        atualX = if (relativo) atualX + numeros[indice + 2] else numeros[indice + 2]
                        atualY = if (relativo) atualY + numeros[indice + 3] else numeros[indice + 3]
                        pontos += PontoXY(atualX, atualY)
                        indice += 4
                    }
                }

                "A" -> {
                    var indice = 0
                    while (indice + 7 <= numeros.size) {
                        atualX = if (relativo) atualX + numeros[indice + 5] else numeros[indice + 5]
                        atualY = if (relativo) atualY + numeros[indice + 6] else numeros[indice + 6]
                        pontos += PontoXY(atualX, atualY)
                        indice += 7
                    }
                }
            }
        }
        return pontos
    }
}
