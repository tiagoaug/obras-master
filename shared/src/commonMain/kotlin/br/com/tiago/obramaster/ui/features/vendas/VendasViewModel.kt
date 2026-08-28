package br.com.tiago.obramaster.ui.features.vendas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.VendaRepository
import br.com.tiago.obramaster.domain.CATEGORIA_PADRAO_RECEITA_VENDAS
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import br.com.tiago.obramaster.domain.Venda
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class VendasUiState(
    val vendas: List<Venda> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val clientes: List<Pessoa> = emptyList(),
    val contas: List<Conta> = emptyList(),
)

private data class DadosReferenciaVendas(
    val projetos: List<Projeto>,
    val clientes: List<Pessoa>,
    val contas: List<Conta>,
)

/**
 * SPEC_OBRA_MASTER.md §4.6 — funil Negociação → Fechada. Fechar gera 1 LancamentoFinanceiro
 * (RECEITA) por parcela, pago=false; o recebimento efetivo é feito parcela a parcela via
 * receberParcela (exige Conta, mesmo padrão de "marcar como pago" já usado em Lançamentos).
 *
 * Decisão técnica sinalizada: nem categoria RECEITA nem Centro de Custo estão nos campos literais
 * de Venda na spec, mas LancamentoFinanceiro exige ambos (NOT NULL). Categoria vem do perfil
 * padrão "Venda de Obra" (ver CATEGORIA_PADRAO_RECEITA_VENDAS). Centro de Custo vem do projeto da
 * venda (1:1, mesmo padrão de Compras/Orçamentos) ou, se a venda não tem projeto, do primeiro
 * Centro tipo COMERCIAL cadastrado. Se nenhum dos dois pré-requisitos existir, fechar() não gera
 * os lançamentos (mesmo comportamento permissivo já usado em PedidosCompraViewModel.atualizarStatus).
 */
class VendasViewModel(
    private val vendaRepository: VendaRepository,
    private val projetoRepository: ProjetoRepository,
    private val pessoaRepository: PessoaRepository,
    private val contaRepository: ContaRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val lancamentoRepository: LancamentoFinanceiroRepository,
    private val movimentoContaRepository: MovimentoContaRepository,
) : ViewModel() {

    private val dadosReferencia = combine(
        projetoRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
        contaRepository.observarAtivas(),
    ) { projetos, pessoas, contas ->
        DadosReferenciaVendas(projetos, pessoas.filter { TagPessoa.CLIENTE in it.tags }, contas)
    }

    val uiState: StateFlow<VendasUiState> = combine(
        vendaRepository.observarTodos(),
        dadosReferencia,
    ) { vendas, referencia ->
        VendasUiState(vendas = vendas, projetos = referencia.projetos, clientes = referencia.clientes, contas = referencia.contas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VendasUiState())

    suspend fun parcelasDaVenda(vendaId: String): List<ParcelaVenda> = vendaRepository.parcelasDaVenda(vendaId)

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        clientePessoaId: String,
        projetoId: String?,
        descricao: String,
        data: Long,
        formaPagamento: String,
        parcelas: List<ParcelaVenda>,
    ) {
        viewModelScope.launch {
            val vendaId = Uuid.random().toString()
            val valorTotal = parcelas.sumOf { it.valor }
            vendaRepository.salvar(
                Venda(
                    id = vendaId,
                    projetoId = projetoId,
                    clientePessoaId = clientePessoaId,
                    descricao = descricao,
                    valorTotal = valorTotal,
                    data = data,
                    formaPagamento = formaPagamento,
                ),
                parcelas.mapIndexed { indice, parcela -> parcela.copy(vendaId = vendaId, numero = indice + 1) },
            )
        }
    }

    fun cancelar(venda: Venda) {
        viewModelScope.launch { vendaRepository.atualizarStatus(venda.id, StatusVenda.CANCELADA) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun fechar(venda: Venda) {
        viewModelScope.launch {
            if (venda.status == StatusVenda.FECHADA) return@launch

            val categoria = categoriaRepository.listarAtivas().firstOrNull { it.nome == CATEGORIA_PADRAO_RECEITA_VENDAS }
            val centro = venda.projetoId?.let { centroRepository.buscarPorProjetoId(it) }
                ?: centroRepository.listarAtivos().firstOrNull { it.tipo == TipoCentroDeCusto.COMERCIAL }

            if (categoria != null && centro != null) {
                val nomeCliente = uiState.value.clientes.firstOrNull { it.id == venda.clientePessoaId }?.nome ?: ""
                val parcelas = vendaRepository.parcelasDaVenda(venda.id)
                parcelas.forEach { parcela ->
                    val lancamentoId = Uuid.random().toString()
                    lancamentoRepository.salvar(
                        LancamentoFinanceiro(
                            id = lancamentoId,
                            tipo = TipoLancamento.RECEITA,
                            categoriaId = categoria.id,
                            centroDeCustoId = centro.id,
                            natureza = NaturezaLancamento.CONTABIL,
                            projetoId = venda.projetoId,
                            descricao = "Venda - $nomeCliente - parcela ${parcela.numero}/${parcelas.size}",
                            valor = parcela.valor,
                            data = parcela.vencimento,
                            formaPagamento = venda.formaPagamento,
                            pago = false,
                        ),
                    )
                    vendaRepository.atualizarParcela(parcela.copy(lancamentoFinanceiroId = lancamentoId))
                }
            }

            vendaRepository.atualizarStatus(venda.id, StatusVenda.FECHADA)
        }
    }

    /** Recebimento efetivo de uma parcela: exige Conta, marca o lançamento vinculado como pago e
     * gera o MovimentoConta correspondente (mesmo padrão de LancamentosViewModel.salvar). */
    @OptIn(ExperimentalUuidApi::class)
    fun receberParcela(venda: Venda, parcela: ParcelaVenda, contaId: String) {
        viewModelScope.launch {
            val lancamentoId = parcela.lancamentoFinanceiroId ?: return@launch
            lancamentoRepository.marcarPago(lancamentoId, true)
            movimentoContaRepository.salvar(
                MovimentoConta(
                    id = Uuid.random().toString(),
                    contaId = contaId,
                    tipo = TipoMovimentoConta.RECEBIMENTO,
                    valor = parcela.valor,
                    data = parcela.vencimento,
                    descricao = "Recebimento - ${venda.descricao} - parcela ${parcela.numero}",
                    lancamentoFinanceiroId = lancamentoId,
                ),
            )
            vendaRepository.atualizarParcela(parcela.copy(pago = true))
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { vendaRepository.desativar(id) }
    }
}
