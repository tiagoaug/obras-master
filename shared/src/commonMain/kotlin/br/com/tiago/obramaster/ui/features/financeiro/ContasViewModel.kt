package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.SaldoContaEngine
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ContasUiState(
    val contas: List<Conta> = emptyList(),
    val saldos: Map<String, Long> = emptyMap(),
    val saldoConsolidado: Long = 0L,
)

class ContasViewModel(
    private val contaRepository: ContaRepository,
    private val movimentoRepository: MovimentoContaRepository,
) : ViewModel() {

    val uiState: StateFlow<ContasUiState> = combine(
        contaRepository.observarAtivas(),
        movimentoRepository.observarTodos(),
    ) { contas, movimentos ->
        val saldos = contas.associate { conta -> conta.id to SaldoContaEngine.calcular(conta, movimentos) }
        ContasUiState(contas = contas, saldos = saldos, saldoConsolidado = saldos.values.sum())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContasUiState())

    @OptIn(ExperimentalUuidApi::class)
    fun salvarNovaConta(nome: String, tipo: TipoConta, banco: String?, agencia: String?, numeroConta: String?, saldoInicial: Long, dataSaldoInicial: Long, cor: String?) {
        viewModelScope.launch {
            contaRepository.salvar(
                Conta(
                    id = Uuid.random().toString(),
                    nome = nome,
                    tipo = tipo,
                    banco = banco,
                    agencia = agencia,
                    numeroConta = numeroConta,
                    saldoInicial = saldoInicial,
                    dataSaldoInicial = dataSaldoInicial,
                    cor = cor,
                ),
            )
        }
    }

    fun atualizarConta(conta: Conta) {
        viewModelScope.launch { contaRepository.atualizar(conta) }
    }

    fun excluir(id: String) {
        viewModelScope.launch { contaRepository.desativar(id) }
    }

    /** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §4.1 — dois MovimentoConta espelhados, nunca gera LancamentoFinanceiro. */
    @OptIn(ExperimentalUuidApi::class)
    fun transferir(contaOrigemId: String, contaDestinoId: String, valor: Long, data: Long, motivo: String) {
        if (contaOrigemId == contaDestinoId || valor <= 0) return
        viewModelScope.launch {
            val vinculoId = Uuid.random().toString()
            movimentoRepository.salvar(
                MovimentoConta(
                    id = Uuid.random().toString(),
                    contaId = contaOrigemId,
                    tipo = TipoMovimentoConta.TRANSFERENCIA_SAIDA,
                    valor = valor,
                    data = data,
                    descricao = motivo,
                    transferenciaVinculoId = vinculoId,
                ),
            )
            movimentoRepository.salvar(
                MovimentoConta(
                    id = Uuid.random().toString(),
                    contaId = contaDestinoId,
                    tipo = TipoMovimentoConta.TRANSFERENCIA_ENTRADA,
                    valor = valor,
                    data = data,
                    descricao = motivo,
                    transferenciaVinculoId = vinculoId,
                ),
            )
        }
    }
}
