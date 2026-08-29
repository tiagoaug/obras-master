package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class StatusVenda { NEGOCIACAO, FECHADA, CANCELADA }

/** SPEC_OBRA_MASTER.md §4.6 — funil simples Negociação → Fechada (+ Cancelada). */
@Serializable
data class Venda(
    val id: String,
    val projetoId: String? = null,
    val clientePessoaId: String,
    val descricao: String,
    val valorTotal: Long, // centavos, soma das parcelas
    val data: Long, // epoch millis UTC
    val formaPagamento: String,
    val status: StatusVenda = StatusVenda.NEGOCIACAO,
    val ativo: Boolean = true,
)

/** lancamentoFinanceiroId: adição técnica sinalizada, mesmo padrão de PedidoCompra/Pagamento — liga a
 * parcela ao LancamentoFinanceiro (RECEITA) gerado quando a venda é fechada. */
@Serializable
data class ParcelaVenda(
    val id: String,
    val vendaId: String,
    val numero: Int,
    val valor: Long, // centavos
    val vencimento: Long, // epoch millis UTC
    val pago: Boolean = false,
    val lancamentoFinanceiroId: String? = null,
)
