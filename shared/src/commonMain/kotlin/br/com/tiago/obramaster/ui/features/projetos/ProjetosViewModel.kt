package br.com.tiago.obramaster.ui.features.projetos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.onboarding.OnboardingDraftStore
import br.com.tiago.obramaster.core.onboarding.ProjetoDraft
import br.com.tiago.obramaster.core.projetos.TEMPLATE_ETAPAS_PADRAO
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusProjeto
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ProjetosUiState(
    val projetoPendenteDoOnboarding: ProjetoDraft? = null,
    val preferenciaTemplatePadrao: Boolean = true,
)

class ProjetosViewModel(
    private val repository: ProjetoRepository,
    private val etapaRepository: EtapaRepository,
    private val draftStore: OnboardingDraftStore,
    private val centroDeCustoRepository: CentroDeCustoRepository,
) : ViewModel() {

    val projetos: StateFlow<List<Projeto>> = repository.observarAtivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val preferencias = draftStore.carregarPreferenciasFuturas()

    private val _uiState = MutableStateFlow(
        ProjetosUiState(
            projetoPendenteDoOnboarding = preferencias?.primeiroProjeto,
            preferenciaTemplatePadrao = preferencias?.usarTemplateEtapasPadrao ?: true,
        ),
    )
    val uiState: StateFlow<ProjetosUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    fun criarDoOnboarding() {
        val draft = _uiState.value.projetoPendenteDoOnboarding ?: return
        viewModelScope.launch {
            criarProjeto(
                nome = draft.nome,
                clienteId = null,
                endereco = draft.endereco.ifBlank { null },
                areaConstruidaM2 = draft.areaConstruidaM2Vezes100?.let { it / 100.0 },
                areaTerrenoM2 = null,
                orcamentoTotal = draft.orcamentoTotalCentavos,
                aplicarTemplateEtapas = _uiState.value.preferenciaTemplatePadrao,
            )
            limparPendenciaOnboarding()
        }
    }

    fun descartarPendenciaOnboarding() {
        limparPendenciaOnboarding()
    }

    private fun limparPendenciaOnboarding() {
        val atuais = preferencias ?: return
        draftStore.salvarPreferenciasFuturas(atuais.copy(primeiroProjeto = null))
        _uiState.value = _uiState.value.copy(projetoPendenteDoOnboarding = null)
    }

    fun criar(
        nome: String,
        clienteId: String?,
        endereco: String?,
        areaConstruidaM2: Double?,
        areaTerrenoM2: Double?,
        orcamentoTotal: Long,
        aplicarTemplateEtapas: Boolean,
    ) {
        viewModelScope.launch {
            criarProjeto(nome, clienteId, endereco, areaConstruidaM2, areaTerrenoM2, orcamentoTotal, aplicarTemplateEtapas)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun criarProjeto(
        nome: String,
        clienteId: String?,
        endereco: String?,
        areaConstruidaM2: Double?,
        areaTerrenoM2: Double?,
        orcamentoTotal: Long,
        aplicarTemplateEtapas: Boolean,
    ) {
        val projetoId = Uuid.random().toString()
        repository.salvar(
            Projeto(
                id = projetoId,
                nome = nome,
                clienteId = clienteId,
                endereco = endereco,
                areaConstruidaM2 = areaConstruidaM2,
                areaTerrenoM2 = areaTerrenoM2,
                orcamentoTotal = orcamentoTotal,
                status = StatusProjeto.PLANEJAMENTO,
            ),
        )
        // SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §3 — todo Projeto ganha um Centro de Custo 1:1 automático.
        centroDeCustoRepository.salvar(
            CentroDeCusto(id = Uuid.random().toString(), nome = nome, tipo = TipoCentroDeCusto.PROJETO, projetoId = projetoId),
        )
        if (aplicarTemplateEtapas) {
            TEMPLATE_ETAPAS_PADRAO.forEachIndexed { indice, nomeEtapa ->
                etapaRepository.salvar(
                    Etapa(
                        id = Uuid.random().toString(),
                        projetoId = projetoId,
                        nome = nomeEtapa,
                        ordem = indice,
                        orcamentoEtapa = 0L,
                    ),
                )
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
