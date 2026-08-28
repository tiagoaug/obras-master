package br.com.tiago.obramaster.ui.features.projetos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.TarefaRepository
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.Tarefa
import br.com.tiago.obramaster.domain.TagPessoa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CronogramaUiState(
    val etapas: List<Etapa> = emptyList(),
    val funcionarios: List<Pessoa> = emptyList(),
)

/** SPEC_OBRA_MASTER.md §4.7 — Cronograma, Gantt simplificado e checklist de tarefas por etapa.
 * Responsável de tarefa é escolhido entre Pessoas com tag FUNCIONARIO (mesmo recorte já usado em
 * Equipes) — a spec não restringe, mas é a leitura mais natural pra "quem executa a tarefa". */
class CronogramaViewModel(
    private val projetoId: String,
    private val etapaRepository: EtapaRepository,
    private val tarefaRepository: TarefaRepository,
    private val pessoaRepository: PessoaRepository,
) : ViewModel() {

    val uiState: StateFlow<CronogramaUiState> = combine(
        etapaRepository.observarDoProjeto(projetoId),
        pessoaRepository.observarAtivas(),
    ) { etapas, pessoas ->
        CronogramaUiState(
            etapas = etapas.sortedBy { it.ordem },
            funcionarios = pessoas.filter { TagPessoa.FUNCIONARIO in it.tags },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CronogramaUiState())

    fun tarefasDaEtapa(etapaId: String): Flow<List<Tarefa>> = tarefaRepository.observarDaEtapa(etapaId)

    fun atualizarDatas(etapa: Etapa, dataInicio: Long?, dataFim: Long?, dataInicioReal: Long?, dataFimReal: Long?) {
        viewModelScope.launch {
            etapaRepository.atualizar(
                etapa.copy(dataInicio = dataInicio, dataFim = dataFim, dataInicioReal = dataInicioReal, dataFimReal = dataFimReal),
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun salvarTarefa(existente: Tarefa?, etapaId: String, descricao: String, responsavelPessoaId: String?, prazo: Long?) {
        viewModelScope.launch {
            if (existente != null) {
                tarefaRepository.atualizar(existente.copy(descricao = descricao, responsavelPessoaId = responsavelPessoaId, prazo = prazo))
            } else {
                tarefaRepository.salvar(
                    Tarefa(id = Uuid.random().toString(), etapaId = etapaId, descricao = descricao, responsavelPessoaId = responsavelPessoaId, prazo = prazo),
                )
            }
        }
    }

    fun alternarConcluida(tarefa: Tarefa) {
        viewModelScope.launch { tarefaRepository.atualizar(tarefa.copy(concluida = !tarefa.concluida)) }
    }

    fun excluirTarefa(id: String) {
        viewModelScope.launch { tarefaRepository.excluir(id) }
    }
}
