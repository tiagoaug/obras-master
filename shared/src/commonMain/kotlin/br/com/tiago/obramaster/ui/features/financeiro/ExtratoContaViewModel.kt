package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.SaldoContaEngine
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.MovimentoConta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExtratoContaUiState(
    val conta: Conta? = null,
    val linhas: List<Pair<MovimentoConta, Long>> = emptyList(), // movimento + saldo corrente
    val somenteNaoConciliados: Boolean = false,
)

class ExtratoContaViewModel(
    private val contaId: String,
    private val contaRepository: ContaRepository,
    private val movimentoRepository: MovimentoContaRepository,
) : ViewModel() {

    private val _somenteNaoConciliados = MutableStateFlow(false)
    private val _conta = MutableStateFlow<Conta?>(null)

    init {
        viewModelScope.launch { _conta.value = contaRepository.buscarPorId(contaId) }
    }

    val uiState: StateFlow<ExtratoContaUiState> = combine(
        _conta,
        movimentoRepository.observarDaConta(contaId),
        _somenteNaoConciliados,
    ) { conta, movimentos, somenteNaoConciliados ->
        if (conta == null) {
            ExtratoContaUiState()
        } else {
            val linhas = SaldoContaEngine.extratoComSaldoCorrente(conta, movimentos).sortedByDescending { it.first.data }
            ExtratoContaUiState(
                conta = conta,
                linhas = if (somenteNaoConciliados) linhas.filter { !it.first.conciliado } else linhas,
                somenteNaoConciliados = somenteNaoConciliados,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExtratoContaUiState())

    fun alternarFiltroConciliados() {
        _somenteNaoConciliados.value = !_somenteNaoConciliados.value
    }

    fun marcarConciliado(id: String, conciliado: Boolean) {
        viewModelScope.launch { movimentoRepository.marcarConciliado(id, conciliado) }
    }
}
