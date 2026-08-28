package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.FiltroFinanceiro
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.core.financeiro.MesAno
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Projeto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FinanceiroDashboardUiState(
    val filtro: FiltroFinanceiro = FiltroFinanceiro(),
    val totalReceitas: Long = 0L,
    val totalDespesas: Long = 0L,
    val lucro: Long = 0L,
    val porCategoria: Map<CategoriaFinanceira, Long> = emptyMap(),
    val porMes: Map<MesAno, Pair<Long, Long>> = emptyMap(),
    val evolucaoLucro: List<Pair<MesAno, Long>> = emptyList(),
    val resultadoPorCentro: Map<CentroDeCusto, Long> = emptyMap(),
    val projetos: List<Projeto> = emptyList(),
)

class FinanceiroDashboardViewModel(
    private val lancamentoRepository: LancamentoFinanceiroRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val projetoRepository: ProjetoRepository,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroFinanceiro())

    val uiState: StateFlow<FinanceiroDashboardUiState> = combine(
        lancamentoRepository.observarAtivos(),
        categoriaRepository.observarAtivas(),
        centroRepository.observarAtivos(),
        projetoRepository.observarAtivos(),
        _filtro,
    ) { lancamentos, categorias, centros, projetos, filtro ->
        val filtrados = FinanceEngine.aplicarFiltro(lancamentos, filtro)
        val categoriasPorId = categorias.associateBy { it.id }
        val centrosPorId = centros.associateBy { it.id }
        FinanceiroDashboardUiState(
            filtro = filtro,
            totalReceitas = FinanceEngine.totalReceitas(filtrados),
            totalDespesas = FinanceEngine.totalDespesas(filtrados),
            lucro = FinanceEngine.lucro(filtrados),
            porCategoria = FinanceEngine.agruparPorCategoria(filtrados)
                .mapNotNull { (id, valor) -> categoriasPorId[id]?.let { it to valor } }.toMap(),
            porMes = FinanceEngine.agruparPorMes(filtrados),
            evolucaoLucro = FinanceEngine.evolucaoLucroPorMes(filtrados),
            resultadoPorCentro = FinanceEngine.resultadoPorCentroDeCusto(filtrados)
                .mapNotNull { (id, valor) -> centrosPorId[id]?.let { it to valor } }.toMap(),
            projetos = projetos,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceiroDashboardUiState())

    fun atualizarFiltro(filtro: FiltroFinanceiro) {
        _filtro.value = filtro
    }
}
