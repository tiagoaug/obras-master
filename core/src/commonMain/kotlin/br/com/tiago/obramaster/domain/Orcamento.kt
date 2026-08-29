package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class StatusOrcamento { RASCUNHO, ENVIADO, APROVADO, RECUSADO }

@Serializable
enum class TipoItemOrcamento { MATERIAL, MAO_DE_OBRA }

/** SPEC_OBRA_MASTER.md §4.5 + SPEC_OBRA_MASTER_ADENDO_BDI.md §3 — bdiPercentualCalculado/custoDiretoTotal/precoVendaTotal
 * são congelados (snapshot) na primeira vez que o orçamento sai de RASCUNHO; ver OrcamentosViewModel. */
@Serializable
data class Orcamento(
    val id: String,
    val projetoId: String? = null,
    val clientePessoaId: String? = null,
    val titulo: String,
    val data: Long, // epoch millis UTC
    val validadeDias: Int,
    val status: StatusOrcamento = StatusOrcamento.RASCUNHO,
    val descontoPercent: Double? = null,
    val observacoes: String? = null,
    val configBdiId: String? = null,
    val bdiPercentualCalculado: Double = 0.0,
    val bdiCustomizado: Boolean = false,
    val custoDiretoTotal: Long = 0L, // centavos
    val precoVendaTotal: Long = 0L, // centavos
    val ativo: Boolean = true,
)

@Serializable
data class ItemOrcamento(
    val id: String,
    val orcamentoId: String,
    val tipo: TipoItemOrcamento,
    val descricao: String,
    val materialId: String? = null,
    val quantidade: Double,
    val unidade: String,
    val valorUnitario: Long, // centavos
    val valorTotal: Long, // centavos
)
