package br.com.tiago.obramaster.core.calc

import kotlin.math.PI
import kotlin.math.sqrt

/** SPEC_OBRA_MASTER.md §4.12.5 — volumes de sólidos usados em obra. Todas as funções retornam
 * null para entrada inválida (medidas não positivas) em vez de lançar exceção. */
object VolumeEngine {

    fun paralelepipedo(comprimento: Double, largura: Double, altura: Double): Double? =
        if (comprimento > 0 && largura > 0 && altura > 0) comprimento * largura * altura else null

    fun cilindro(raio: Double, altura: Double): Double? =
        if (raio > 0 && altura > 0) PI * raio * raio * altura else null

    fun esfera(raio: Double): Double? =
        if (raio > 0) (4.0 / 3.0) * PI * raio * raio * raio else null

    fun cone(raio: Double, altura: Double): Double? =
        if (raio > 0 && altura > 0) (PI * raio * raio * altura) / 3.0 else null

    /** Prisma reto de base poligonal regular: área da base (via GeometriaEngine) × altura. */
    fun prisma(areaBase: Double, altura: Double): Double? =
        if (areaBase > 0 && altura > 0) areaBase * altura else null

    /** Tronco de pirâmide (ou cone) de bases quadradas/circulares equivalentes — fórmula geral
     * a partir das áreas das duas bases e da altura entre elas. */
    fun troncoDePiramide(areaBaseMaior: Double, areaBaseMenor: Double, altura: Double): Double? {
        if (areaBaseMaior <= 0 || areaBaseMenor <= 0 || altura <= 0) return null
        return (altura / 3.0) * (areaBaseMaior + areaBaseMenor + sqrt(areaBaseMaior * areaBaseMenor))
    }
}
