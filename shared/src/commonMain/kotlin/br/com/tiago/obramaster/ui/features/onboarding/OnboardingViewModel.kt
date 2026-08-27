package br.com.tiago.obramaster.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.onboarding.ColaboradorDraft
import br.com.tiago.obramaster.core.onboarding.ContaDraft
import br.com.tiago.obramaster.core.onboarding.DadosEmpresaDraft
import br.com.tiago.obramaster.core.onboarding.GestorDraft
import br.com.tiago.obramaster.core.onboarding.OnboardingDraftStore
import br.com.tiago.obramaster.core.onboarding.OnboardingEngine
import br.com.tiago.obramaster.core.onboarding.OnboardingState
import br.com.tiago.obramaster.core.onboarding.OnboardingStep
import br.com.tiago.obramaster.core.onboarding.ProjetoDraft
import br.com.tiago.obramaster.core.onboarding.ValidationResult
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.theme.PrefsAcessibilidade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OnboardingEvento {
    data class Concluido(val colaboradorGestor: Colaborador) : OnboardingEvento
    data class Erro(val mensagem: String) : OnboardingEvento
}

class OnboardingViewModel(
    private val empresaRepository: EmpresaRepository,
    private val colaboradorRepository: ColaboradorRepository,
    private val permissaoRepository: PermissaoRepository,
    private val moduleConfigRepository: ModuleConfigRepository,
    private val contaRepository: ContaRepository,
    private val accessibilityPrefsStore: AccessibilityPrefsStore,
    private val draftStore: OnboardingDraftStore,
) : ViewModel() {

    private val _state = MutableStateFlow(draftStore.carregarRascunho() ?: OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _concluindo = MutableStateFlow(false)
    val concluindo: StateFlow<Boolean> = _concluindo.asStateFlow()

    private val _evento = MutableStateFlow<OnboardingEvento?>(null)
    val evento: StateFlow<OnboardingEvento?> = _evento.asStateFlow()

    private fun atualizar(transformacao: (OnboardingState) -> OnboardingState) {
        _state.value = transformacao(_state.value)
        draftStore.salvarRascunho(_state.value)
    }

    fun validacaoAtual(): ValidationResult = OnboardingEngine.validarEtapa(_state.value.etapaAtual, _state.value)

    fun avancar() = atualizar(OnboardingEngine::avancar)
    fun pular() = atualizar(OnboardingEngine::pular)
    fun voltar() = atualizar(OnboardingEngine::voltar)
    fun irParaEtapa(step: OnboardingStep) = atualizar { it.copy(etapaAtual = step) }

    fun atualizarEmpresa(draft: DadosEmpresaDraft) = atualizar { it.copy(empresa = draft) }
    fun atualizarGestor(draft: GestorDraft) = atualizar { it.copy(gestor = draft) }

    fun alternarModulo(modulo: AppModule, ativo: Boolean) = atualizar {
        it.copy(modulosAtivos = if (ativo) it.modulosAtivos + modulo else it.modulosAtivos - modulo)
    }

    fun adicionarConta(draft: ContaDraft) = atualizar { it.copy(contas = it.contas + draft) }
    fun removerConta(indice: Int) = atualizar { it.copy(contas = it.contas.filterIndexed { i, _ -> i != indice }) }

    fun atualizarUsarCategoriasDefault(usar: Boolean) = atualizar { it.copy(usarCategoriasDefault = usar) }
    fun atualizarUsarBdiPadrao(usar: Boolean) = atualizar { it.copy(usarBdiPadrao = usar) }
    fun atualizarUsarTemplateEtapasPadrao(usar: Boolean) = atualizar { it.copy(usarTemplateEtapasPadrao = usar) }

    fun adicionarColaborador(draft: ColaboradorDraft) = atualizar { it.copy(colaboradores = it.colaboradores + draft) }
    fun removerColaborador(indice: Int) = atualizar {
        it.copy(colaboradores = it.colaboradores.filterIndexed { i, _ -> i != indice })
    }

    fun atualizarPrimeiroProjeto(draft: ProjetoDraft?) = atualizar { it.copy(primeiroProjeto = draft) }
    fun atualizarAcessibilidade(prefs: PrefsAcessibilidade) = atualizar { it.copy(acessibilidade = prefs) }

    fun concluir() {
        val estadoAtual = _state.value
        if (!OnboardingEngine.podeConcluir(estadoAtual)) {
            _evento.value = OnboardingEvento.Erro("Preencha os dados obrigatórios antes de concluir")
            return
        }
        viewModelScope.launch {
            _concluindo.value = true
            try {
                OnboardingEngine.commitar(
                    state = estadoAtual,
                    empresaRepository = empresaRepository,
                    colaboradorRepository = colaboradorRepository,
                    permissaoRepository = permissaoRepository,
                    moduleConfigRepository = moduleConfigRepository,
                    contaRepository = contaRepository,
                    accessibilityPrefsStore = accessibilityPrefsStore,
                    draftStore = draftStore,
                )
                val gestor = colaboradorRepository.buscarPorLogin(estadoAtual.gestor.login)
                _evento.value = if (gestor != null) {
                    OnboardingEvento.Concluido(gestor)
                } else {
                    OnboardingEvento.Erro("Não foi possível confirmar o Gestor criado")
                }
            } catch (e: Exception) {
                _evento.value = OnboardingEvento.Erro(e.message ?: "Erro ao concluir")
            } finally {
                _concluindo.value = false
            }
        }
    }

    fun eventoConsumido() {
        _evento.value = null
    }
}
