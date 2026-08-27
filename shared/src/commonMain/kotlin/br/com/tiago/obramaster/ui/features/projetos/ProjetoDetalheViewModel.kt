package br.com.tiago.obramaster.ui.features.projetos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.projetos.TEMPLATE_ETAPAS_PADRAO
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusEtapa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ProjetoDetalheUiState(
    val projeto: Projeto? = null,
    val etapas: List<Etapa> = emptyList(),
)

class ProjetoDetalheViewModel(
    private val projetoId: String,
    private val projetoRepository: ProjetoRepository,
    private val etapaRepository: EtapaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjetoDetalheUiState())
    val uiState: StateFlow<ProjetoDetalheUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(projeto = projetoRepository.buscarPorId(projetoId))
        }
        viewModelScope.launch {
            etapaRepository.observarDoProjeto(projetoId).collect { etapas ->
                _uiState.value = _uiState.value.copy(etapas = etapas.sortedBy { it.ordem })
            }
        }
    }

    fun atualizarProjeto(projetoAtualizado: Projeto) {
        viewModelScope.launch {
            projetoRepository.atualizar(projetoAtualizado)
            _uiState.value = _uiState.value.copy(projeto = projetoAtualizado)
        }
    }

    fun excluirProjeto(onExcluido: () -> Unit) {
        viewModelScope.launch {
            projetoRepository.desativar(projetoId)
            onExcluido()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun aplicarTemplatePadrao() {
        viewModelScope.launch {
            val ordemInicial = _uiState.value.etapas.size
            TEMPLATE_ETAPAS_PADRAO.forEachIndexed { indice, nome ->
                etapaRepository.salvar(
                    Etapa(
                        id = Uuid.random().toString(),
                        projetoId = projetoId,
                        nome = nome,
                        ordem = ordemInicial + indice,
                        orcamentoEtapa = 0L,
                    ),
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun salvarEtapa(
        existente: Etapa?,
        nome: String,
        orcamentoEtapa: Long,
        progressoPercent: Int,
        status: StatusEtapa,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                etapaRepository.atualizar(
                    existente.copy(nome = nome, orcamentoEtapa = orcamentoEtapa, progressoPercent = progressoPercent, status = status),
                )
            } else {
                etapaRepository.salvar(
                    Etapa(
                        id = Uuid.random().toString(),
                        projetoId = projetoId,
                        nome = nome,
                        ordem = _uiState.value.etapas.size,
                        orcamentoEtapa = orcamentoEtapa,
                        progressoPercent = progressoPercent,
                        status = status,
                    ),
                )
            }
        }
    }

    fun excluirEtapa(id: String) {
        viewModelScope.launch { etapaRepository.desativar(id) }
    }

    fun moverEtapa(etapa: Etapa, direcao: Int) {
        viewModelScope.launch {
            val lista = _uiState.value.etapas
            val indiceAtual = lista.indexOfFirst { it.id == etapa.id }
            val novoIndice = indiceAtual + direcao
            if (indiceAtual == -1 || novoIndice !in lista.indices) return@launch
            val outra = lista[novoIndice]
            etapaRepository.reordenar(etapa.id, outra.ordem)
            etapaRepository.reordenar(outra.id, etapa.ordem)
        }
    }
}
