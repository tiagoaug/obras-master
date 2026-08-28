package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.PlantaBaixa
import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.round
import kotlin.math.sin

/** SPEC_PLANTA_BAIXA.md §3 — puro, sem dependência de Compose/Canvas. */
object PlantaBaixaEngine {

    /** Fórmula de Shoelace — mesma matemática do AreaEngine da calculadora de área irregular. */
    fun calcularAreaM2(pontos: List<PontoXY>, escalaPxPorMetro: Double): Double {
        if (pontos.size < 3 || escalaPxPorMetro <= 0.0) return 0.0
        var somaAreaPx2 = 0.0
        for (i in pontos.indices) {
            val atual = pontos[i]
            val proximo = pontos[(i + 1) % pontos.size]
            somaAreaPx2 += atual.x * proximo.y - proximo.x * atual.y
        }
        val areaPx2 = abs(somaAreaPx2) / 2.0
        return areaPx2 / (escalaPxPorMetro * escalaPxPorMetro)
    }

    fun calcularPerimetroM(pontos: List<PontoXY>, escalaPxPorMetro: Double): Double {
        if (pontos.size < 2 || escalaPxPorMetro <= 0.0) return 0.0
        var perimetroPx = 0.0
        for (i in pontos.indices) {
            val atual = pontos[i]
            val proximo = pontos[(i + 1) % pontos.size]
            perimetroPx += hypot(proximo.x - atual.x, proximo.y - atual.y)
        }
        return perimetroPx / escalaPxPorMetro
    }

    fun areaTotalConstruida(plantas: List<PlantaBaixa>, comodos: List<Comodo>): Double {
        val idsDasPlantas = plantas.map { it.id }.toSet()
        return comodos.filter { it.plantaId in idsDasPlantas }.sumOf { it.areaM2 }
    }

    /** Gruda a direção (a partir de [pontoAnterior]) no múltiplo de [grausSnap] mais próximo. */
    fun snapAngulo(pontoAtual: PontoXY, pontoAnterior: PontoXY, grausSnap: Double = 15.0): PontoXY {
        val dx = pontoAtual.x - pontoAnterior.x
        val dy = pontoAtual.y - pontoAnterior.y
        val distancia = hypot(dx, dy)
        if (distancia == 0.0) return pontoAtual

        val anguloAtualGraus = atan2(dy, dx) * 180.0 / PI
        val anguloSnapGraus = round(anguloAtualGraus / grausSnap) * grausSnap
        val anguloSnapRad = anguloSnapGraus * PI / 180.0

        return PontoXY(
            x = pontoAnterior.x + distancia * cos(anguloSnapRad),
            y = pontoAnterior.y + distancia * sin(anguloSnapRad),
        )
    }

    fun snapGrade(ponto: PontoXY, tamanhoGradePx: Double): PontoXY {
        if (tamanhoGradePx <= 0.0) return ponto
        return PontoXY(
            x = round(ponto.x / tamanhoGradePx) * tamanhoGradePx,
            y = round(ponto.y / tamanhoGradePx) * tamanhoGradePx,
        )
    }

    /** Distância real conhecida entre dois pontos na tela → escala px/metro. */
    fun calcularEscala(pontoA: PontoXY, pontoB: PontoXY, distanciaRealM: Double): Double {
        if (distanciaRealM <= 0.0) return 0.0
        val distanciaPx = hypot(pontoB.x - pontoA.x, pontoB.y - pontoA.y)
        return distanciaPx / distanciaRealM
    }

    fun poligonoFechado(pontos: List<PontoXY>, toleranciaPx: Double = 10.0): Boolean {
        if (pontos.size < 3) return false
        val primeiro = pontos.first()
        val ultimo = pontos.last()
        return hypot(ultimo.x - primeiro.x, ultimo.y - primeiro.y) <= toleranciaPx
    }
}
