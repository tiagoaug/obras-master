package br.com.tiago.obramaster.ui.features.assistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.assistant.ManualSearchEngine
import br.com.tiago.obramaster.core.assistant.ManualSection
import br.com.tiago.obramaster.core.assistant.TelaContexto
import br.com.tiago.obramaster.core.assistant.TelaContextoHolder
import br.com.tiago.obramaster.data.repository.ManualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistenteUiState(
    val pergunta: String = "",
    val contexto: TelaContexto? = null,
    val resultados: List<ManualSection> = emptyList(),
    val buscou: Boolean = false,
)

/** SPEC_ASSISTENTE_IA.md — Fase A/B: busca local no manual, sempre disponível offline (§7.4).
 * A geração de prosa/exemplo dinâmico via IA (Fase C, endpoint `/assistant/ask`) depende de um
 * backend que ainda não existe pós-pivô Firebase — ver nota em CHECKLIST_TESTES.md Fase 11. */
class AssistenteViewModel(
    private val manualRepository: ManualRepository,
    private val telaContextoHolder: TelaContextoHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistenteUiState())
    val uiState: StateFlow<AssistenteUiState> = _uiState.asStateFlow()

    private var secoes: List<ManualSection> = emptyList()

    init {
        viewModelScope.launch { secoes = manualRepository.listarSecoes() }
        viewModelScope.launch {
            telaContextoHolder.atual.collect { contexto -> _uiState.update { it.copy(contexto = contexto) } }
        }
    }

    fun atualizarPergunta(texto: String) {
        _uiState.update { it.copy(pergunta = texto) }
    }

    fun buscar() {
        val estado = _uiState.value
        val resultados = ManualSearchEngine.buscar(estado.pergunta, secoes, estado.contexto?.modulo)
        _uiState.update { it.copy(resultados = resultados, buscou = true) }
    }

    fun limpar() {
        _uiState.update { it.copy(pergunta = "", resultados = emptyList(), buscou = false) }
    }
}
