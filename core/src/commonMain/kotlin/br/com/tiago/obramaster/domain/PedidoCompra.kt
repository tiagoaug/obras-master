package br.com.tiago.obramaster.domain

enum class StatusPedidoCompra { COTACAO, APROVADO, COMPRADO, ENTREGUE }

/**
 * SPEC_OBRA_MASTER.md §4.4. `lancamentoFinanceiroId` não está no snippet original — sem ele não
 * dá pra rastrear qual despesa esse pedido gerou ao virar COMPRADO. Mesmo padrão de completude
 * técnica já usado em RegistroTrabalho.pagamentoId e Pagamento.lancamentoFinanceiroId.
 */
data class PedidoCompra(
    val id: String,
    val projetoId: String,
    val etapaId: String? = null,
    val fornecedorId: String? = null,
    val data: Long, // epoch millis UTC
    val status: StatusPedidoCompra = StatusPedidoCompra.COTACAO,
    val valorTotal: Long, // centavos, soma dos ItemCompra
    val lancamentoFinanceiroId: String? = null,
    val ativo: Boolean = true,
)

data class ItemCompra(
    val id: String,
    val pedidoId: String,
    val materialId: String,
    val quantidade: Double,
    val unidade: String,
    val valorUnitario: Long, // centavos
    val valorTotal: Long, // centavos
)
