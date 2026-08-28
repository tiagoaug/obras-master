package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.FiltroFinanceiro
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RateioLancamentoRepository
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val filtro: FiltroFinanceiro = FiltroFinanceiro(),
)

class LancamentosViewModel(
    private val repository: LancamentoFinanceiroRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val projetoRepository: ProjetoRepository,
    private val rateioRepository: RateioLancamentoRepository,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroFinanceiro())

    val uiState: StateFlow<LancamentosUiState> = combine(
        repository.observarAtivos(),
        categoriaRepository.observarAtivas(),
        centroRepository.observarAtivos(),
        projetoRepository.observarAtivos(),
        _filtro,
    ) { lancamentos, categorias, centros, projetos, filtro ->
        LancamentosUiState(
            lancamentos = lancamentos,
            lancamentosFiltrados = FinanceEngine.aplicarFiltro(lancamentos, filtro),
            categorias = categorias,
            centros = centros,
            projetos = projetos,
            filtro = filtro,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LancamentosUiState())

    fun atualizarFiltro(filtro: FiltroFinanceiro) {
        _filtro.value = filtro
    }

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
            )
            if (existente != null) repository.atualizar(lancamento) else repository.salvar(lancamento)

            if (rateios.isNotEmpty()) {
                rateioRepository.substituir(
                    id,
                    rateios.map { (centroId, percentual) ->
                        RateioLancamento(id = Uuid.random().toString(), lancamentoId = id, centroDeCustoId = centroId, percentual = percentual)
                    },
                )
            } else {
                rateioRepository.substituir(id, emptyList())
            }
        }
    }

    fun marcarPago(id: String, pago: Boolean) {
        viewModelScope.launch { repository.marcarPago(id, pago) }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }

    suspend fun rateiosDoLancamento(lancamentoId: String): List<RateioLancamento> = rateioRepository.listarDoLancamento(lancamentoId)
}
