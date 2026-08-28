package br.com.tiago.obramaster.core.orcamentos

import br.com.tiago.obramaster.domain.ConfigBDI

/** SPEC_OBRA_MASTER_ADENDO_BDI.md §2.1/§4 — fórmula gross-up: tributos incidem sobre o preço de venda, não o custo,
 * por isso entram dividindo em vez de somando ao BDI. */
object BdiEngine {

    data class ResultadoBdi(
        val bdiPercentual: Double,
        val precoVenda: Long, // centavos
    )

    fun calcularBdi(config: ConfigBDI): Double {
        val base = 1 + config.administracaoCentral + config.seguroGarantia + config.riscos + config.despesasFinanceiras
        val comLucro = base * (1 + config.lucro)
        val comTributos = comLucro / (1 - config.tributos)
        return comTributos - 1
    }

    fun aplicarBdi(custoDireto: Long, config: ConfigBDI): ResultadoBdi {
        val bdi = calcularBdi(config)
        val preco = (custoDireto * (1 + bdi)).toLong()
        return ResultadoBdi(bdi, preco)
    }

    /** Combinação livre de um percentual de BDI já definido (calculado ou customizado) com o custo direto. */
    fun precoVendaComBdiPercentual(custoDireto: Long, bdiPercentual: Double): Long =
        (custoDireto * (1 + bdiPercentual)).toLong()
}
