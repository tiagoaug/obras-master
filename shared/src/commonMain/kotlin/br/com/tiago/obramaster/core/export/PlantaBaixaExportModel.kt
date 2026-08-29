package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PontoXY

/** SPEC_PLANTA_BAIXA.md §6 (Fase 9.5) — traduz a geometria da planta (em px do editor, ver
 * PlantaBaixaEngine) pra coordenadas já prontas pro formato de saída (px do JPG ou pt do PDF):
 * escala-pra-caber dentro da margem, com opção de inverter Y (PDF é bottom-up, o editor/Canvas é
 * top-down). Um único cálculo de transformação compartilhado pelos dois renderers (JPG e PDF) —
 * evita duplicar a mesma matemática de enquadramento duas vezes. */
object PlantaBaixaExportModel {

    data class FormaComodo(val pontos: List<PontoXY>, val corHex: String, val rotulo: String, val centro: PontoXY)
    data class SegmentoParede(val inicio: PontoXY, val fim: PontoXY, val espessuraSaida: Double)
    data class PlantaBaixaDesenho(val comodos: List<FormaComodo>, val paredes: List<SegmentoParede>)

    fun montar(
        comodos: List<Comodo>,
        paredes: List<Parede>,
        escalaPxPorMetro: Double,
        larguraSaida: Double,
        alturaSaida: Double,
        margem: Double,
        inverterY: Boolean,
    ): PlantaBaixaDesenho {
        val todosPontos = comodos.flatMap { it.pontos } + paredes.flatMap { listOf(it.pontoInicio, it.pontoFim) }
        if (todosPontos.isEmpty()) return PlantaBaixaDesenho(emptyList(), emptyList())

        val minX = todosPontos.minOf { it.x }
        val maxX = todosPontos.maxOf { it.x }
        val minY = todosPontos.minOf { it.y }
        val maxY = todosPontos.maxOf { it.y }
        val larguraDesenho = (maxX - minX).coerceAtLeast(1.0)
        val alturaDesenho = (maxY - minY).coerceAtLeast(1.0)

        val larguraDisponivel = (larguraSaida - 2 * margem).coerceAtLeast(1.0)
        val alturaDisponivel = (alturaSaida - 2 * margem).coerceAtLeast(1.0)
        val escala = minOf(larguraDisponivel / larguraDesenho, alturaDisponivel / alturaDesenho)

        fun transformar(p: PontoXY): PontoXY {
            val x = margem + (p.x - minX) * escala
            val yBase = margem + (p.y - minY) * escala
            val y = if (inverterY) alturaSaida - yBase else yBase
            return PontoXY(x, y)
        }

        val formasComodos = comodos.map { comodo ->
            val pontosTransformados = comodo.pontos.map(::transformar)
            val centro = PontoXY(
                pontosTransformados.sumOf { it.x } / pontosTransformados.size,
                pontosTransformados.sumOf { it.y } / pontosTransformados.size,
            )
            FormaComodo(pontosTransformados, comodo.corPreenchimento, comodo.nome, centro)
        }

        val segmentosParedes = paredes.map { parede ->
            val espessuraSaida = if (escalaPxPorMetro > 0) {
                (parede.espessuraCm / 100.0) * escalaPxPorMetro * escala
            } else {
                2.0 * escala
            }
            SegmentoParede(transformar(parede.pontoInicio), transformar(parede.pontoFim), espessuraSaida.coerceAtLeast(1.0))
        }

        return PlantaBaixaDesenho(formasComodos, segmentosParedes)
    }
}
