package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.FiltroFinanceiro
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RateioLancamentoRepository
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class LancamentosUiState(
    val lancamentos: List<LancamentoFinanceiro> = emptyList(),
    val lancamentosFiltrados: List<LancamentoFinanceiro> = emptyList(),
    val categorias: List<CategoriaFinanceira> = emptyList(),
    val centros: List<CentroDeCusto> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val contas: List<Conta> = emptyList(),
    val filtro: FiltroFinanceiro = FiltroFinanceiro(),
)

private data class DadosReferencia(
    val categorias: List<CategoriaFinanceira>,
    val centros: List<CentroDeCusto>,
    val projetos: List<Projeto>,
    val contas: List<Conta>,
)

class LancamentosViewModel(
    private val repository: LancamentoFinanceiroRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val projetoRepository: ProjetoRepository,
    private val rateioRepository: RateioLancamentoRepository,
    private val contaRepository: ContaRepository,
    private val movimentoContaRepository: MovimentoContaRepository,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroFinanceiro())

    private val dadosReferencia = combine(
        categoriaRepository.observarAtivas(),
        centroRepository.observarAtivos(),
        projetoRepository.observarAtivos(),
        contaRepository.observarAtivas(),
    ) { categorias, centros, projetos, contas -> DadosReferencia(categorias, centros, projetos, contas) }

    val uiState: StateFlow<LancamentosUiState> = combine(
        repository.observarAtivos(),
        dadosReferencia,
        _filtro,
    ) { lancamentos, referencia, filtro ->
        LancamentosUiState(
            lancamentos = lancamentos,
            lancamentosFiltrados = FinanceEngine.aplicarFiltro(lancamentos, filtro),
            categorias = referencia.categorias,
            centros = referencia.centros,
            projetos = referencia.projetos,
            contas = referencia.contas,
            filtro = filtro,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LancamentosUiState())

    fun atualizarFiltro(filtro: FiltroFinanceiro) {
        _filtro.value = filtro
    }

    /**
     * SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §4.1 — marcar como pago/recebido exige uma Conta;
     * isso gera (ou substitui, se já existia) o MovimentoConta correspondente automaticamente.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: LancamentoFinanceiro?,
        tipo: TipoLancamento,
        categoriaId: String,
        centroDeCustoId: String,
        natureza: NaturezaLancamento,
        projetoId: String?,
        etapaId: String?,
        descricao: String,
        valor: Long,
        data: Long,
        formaPagamento: String,
        pago: Boolean,
        contaId: String?,
        rateios: List<Pair<String, Double>>, // centroDeCustoId to percentual
    ) {
        viewModelScope.launch {
            val id = existente?.id ?: Uuid.random().toString()
            val lancamento = LancamentoFinanceiro(
                id = id,
                tipo = tipo,
                categoriaId = categoriaId,
                centroDeCustoId = centroDeCustoId,
                natureza = natureza,
                projetoId = projetoId,
                etapaId = etapaId,
                descricao = descricao,
                valor = valor,
                data = data,
                formaPagamento = formaPagamento,
                pago = pago,
                contaId = if (pago) contaId else null,
            )
            if (existente != null) repository.atualizar(lancamento) else repository.salvar(lancamento)

            rateioRepository.substituir(
                id,
                rateios.map { (centroId, percentual) ->
                    RateioLancamento(id = Uuid.random().toString(), lancamentoId = id, centroDeCustoId = centroId, percentual = percentual)
                },
            )

            movimentoContaRepository.excluirPorLancamentoId(id)
            if (pago && contaId != null) {
                movimentoContaRepository.salvar(
                    MovimentoConta(
                        id = Uuid.random().toString(),
                        contaId = contaId,
                        tipo = if (tipo == TipoLancamento.RECEITA) TipoMovimentoConta.RECEBIMENTO else TipoMovimentoConta.PAGAMENTO,
                        valor = valor,
                        data = data,
                        descricao = descricao,
                        lancamentoFinanceiroId = id,
                    ),
                )
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch {
            repository.desativar(id)
            movimentoContaRepository.excluirPorLancamentoId(id)
        }
    }

    suspend fun rateiosDoLancamento(lancamentoId: String): List<RateioLancamento> = rateioRepository.listarDoLancamento(lancamentoId)
}
