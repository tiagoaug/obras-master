package br.com.tiago.obramaster.core.calc

import kotlin.math.round
import kotlin.math.sqrt

/** SPEC_OBRA_MASTER.md §4.12.6 — calculadoras de engenharia civil usadas no dia a dia da obra.
 * Todas as funções retornam null para entrada inválida em vez de lançar exceção. */
object EngenhariaEngine {

    // Densidades reais de referência (kg/m³) usadas no método de volume absoluto para dosagem
    // de traço — não são densidade aparente (de compra), por isso os resultados de traço saem
    // sempre em massa (kg), nunca em volume de compra.
    private const val DENSIDADE_CIMENTO_KG_M3 = 3100.0
    private const val DENSIDADE_AREIA_KG_M3 = 2650.0
    private const val DENSIDADE_BRITA_KG_M3 = 2650.0
    private const val DENSIDADE_AGUA_KG_M3 = 1000.0

    data class ResultadoTraco(
        val cimentoKg: Double,
        val sacosCimento50kg: Double,
        val areiaKg: Double,
        val britaKg: Double?,
        val aguaLitros: Double,
    )

    /** Método de dosagem por volume absoluto: V = C·(1/dC + a/dA + b/dB + x/dAg), onde a/b são as
     * partes de areia/brita por parte de cimento e x é o fator água/cimento. */
    private fun calcularTraco(volumeM3: Double, partesCimento: Double, partesAreia: Double, partesBrita: Double, fatorAguaCimento: Double): ResultadoTraco? {
        if (volumeM3 <= 0 || partesCimento <= 0 || partesAreia < 0 || partesBrita < 0 || fatorAguaCimento <= 0) return null
        val a = partesAreia / partesCimento
        val b = partesBrita / partesCimento
        val somaVolumesEspecificos = 1.0 / DENSIDADE_CIMENTO_KG_M3 + a / DENSIDADE_AREIA_KG_M3 + b / DENSIDADE_BRITA_KG_M3 + fatorAguaCimento / DENSIDADE_AGUA_KG_M3
        val cimentoKg = volumeM3 / somaVolumesEspecificos
        return ResultadoTraco(
            cimentoKg = cimentoKg,
            sacosCimento50kg = cimentoKg / 50.0,
            areiaKg = a * cimentoKg,
            britaKg = if (partesBrita > 0) b * cimentoKg else null,
            aguaLitros = fatorAguaCimento * cimentoKg,
        )
    }

    /** Traço de concreto (cimento:areia:brita + fator água/cimento) para um volume desejado em m³. */
    fun tracoConcreto(volumeM3: Double, partesCimento: Double, partesAreia: Double, partesBrita: Double, fatorAguaCimento: Double): ResultadoTraco? {
        if (partesBrita <= 0) return null
        return calcularTraco(volumeM3, partesCimento, partesAreia, partesBrita, fatorAguaCimento)
    }

    /** Traço de argamassa (cimento:areia + fator água/cimento, sem brita) para assentamento ou reboco. */
    fun tracoArgamassa(volumeM3: Double, partesCimento: Double, partesAreia: Double, fatorAguaCimento: Double): ResultadoTraco? =
        calcularTraco(volumeM3, partesCimento, partesAreia, 0.0, fatorAguaCimento)

    /** Volume de argamassa necessário pra cobrir uma área com determinada espessura de camada. */
    fun volumeArgamassaPorArea(areaM2: Double, espessuraCm: Double): Double? =
        if (areaM2 > 0 && espessuraCm > 0) areaM2 * (espessuraCm / 100.0) else null

    data class TipoTijolo(val nome: String, val comprimentoCm: Double, val alturaCm: Double)

    /** Presets de referência — dimensões da face exposta na parede (comprimento × altura). O uso
     * de "Personalizado" na tela permite informar as dimensões reais do material da obra. */
    val TIPOS_TIJOLO_PADRAO = listOf(
        TipoTijolo("Tijolo maciço comum", comprimentoCm = 19.0, alturaCm = 5.7),
        TipoTijolo("Tijolo furado 8 furos", comprimentoCm = 19.0, alturaCm = 9.0),
        TipoTijolo("Bloco cerâmico de vedação 9x19x19", comprimentoCm = 19.0, alturaCm = 19.0),
        TipoTijolo("Bloco de concreto 14x19x39", comprimentoCm = 39.0, alturaCm = 19.0),
    )

    /** Quantidade de unidades por m² de parede, considerando a espessura da junta de assentamento. */
    fun tijolosPorM2(comprimentoCm: Double, alturaCm: Double, juntaCm: Double): Double? {
        if (comprimentoCm <= 0 || alturaCm <= 0 || juntaCm < 0) return null
        val areaComJuntaM2 = ((comprimentoCm + juntaCm) / 100.0) * ((alturaCm + juntaCm) / 100.0)
        return if (areaComJuntaM2 > 0) 1.0 / areaComJuntaM2 else null
    }

    fun quantidadeTijolos(areaParedeM2: Double, tijolosPorM2: Double, percentualPerda: Double): Double? {
        if (areaParedeM2 <= 0 || tijolosPorM2 <= 0 || percentualPerda < 0) return null
        return areaParedeM2 * tijolosPorM2 * (1 + percentualPerda / 100.0)
    }

    /** Caixas de piso/revestimento necessárias, considerando o percentual de perda de corte. */
    fun caixasNecessarias(areaM2: Double, percentualPerda: Double, areaPorCaixaM2: Double): Double? {
        if (areaM2 <= 0 || percentualPerda < 0 || areaPorCaixaM2 <= 0) return null
        return (areaM2 * (1 + percentualPerda / 100.0)) / areaPorCaixaM2
    }

    /** Litros de tinta necessários dado o rendimento (m² por litro) e o número de demãos. */
    fun litrosTinta(areaM2: Double, numeroDemaos: Double, rendimentoM2PorLitro: Double): Double? {
        if (areaM2 <= 0 || numeroDemaos <= 0 || rendimentoM2PorLitro <= 0) return null
        return areaM2 * numeroDemaos / rendimentoM2PorLitro
    }

    fun latasNecessarias(litros: Double, volumeLataLitros: Double): Double? =
        if (litros > 0 && volumeLataLitros > 0) litros / volumeLataLitros else null

    /** Área inclinada do telhado a partir da área plana e da inclinação (%). */
    fun areaInclinadaTelhado(areaPlanaM2: Double, inclinacaoPercentual: Double): Double? {
        if (areaPlanaM2 <= 0 || inclinacaoPercentual < 0) return null
        val fator = sqrt(1.0 + (inclinacaoPercentual / 100.0) * (inclinacaoPercentual / 100.0))
        return areaPlanaM2 * fator
    }

    data class ResultadoEscada(
        val numeroDegraus: Int,
        val alturaEspelhoCm: Double,
        val profundidadePisoCm: Double,
        val valorBlondelCm: Double,
        val dentroDaFaixaRecomendada: Boolean,
    )

    /** Fórmula de Blondel: 63 ≤ 2×espelho + piso ≤ 65 (ideal = 64). Dado o piso desejado e a
     * altura total do lance, calcula o número de degraus que mais se aproxima do ideal. */
    fun calcularDegraus(alturaTotalCm: Double, pisoDesejadoCm: Double): ResultadoEscada? {
        if (alturaTotalCm <= 0 || pisoDesejadoCm <= 0) return null
        val alturaEspelhoIdeal = (64.0 - pisoDesejadoCm) / 2.0
        if (alturaEspelhoIdeal <= 0) return null
        val numeroDegraus = round(alturaTotalCm / alturaEspelhoIdeal).toInt().coerceAtLeast(1)
        val alturaEspelhoReal = alturaTotalCm / numeroDegraus
        val valorBlondel = 2 * alturaEspelhoReal + pisoDesejadoCm
        return ResultadoEscada(
            numeroDegraus = numeroDegraus,
            alturaEspelhoCm = alturaEspelhoReal,
            profundidadePisoCm = pisoDesejadoCm,
            valorBlondelCm = valorBlondel,
            dentroDaFaixaRecomendada = valorBlondel in 63.0..65.0,
        )
    }

    /** Estimativa de aço (kg) a partir do volume de concreto e de uma taxa de consumo (kg/m³)
     * informada pelo usuário — taxas típicas variam por elemento estrutural (laje/viga/pilar). */
    fun kgAcoEstimado(volumeConcretoM3: Double, taxaKgPorM3: Double): Double? =
        if (volumeConcretoM3 > 0 && taxaKgPorM3 > 0) volumeConcretoM3 * taxaKgPorM3 else null
}
