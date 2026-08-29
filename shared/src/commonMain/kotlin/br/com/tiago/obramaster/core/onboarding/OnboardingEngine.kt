package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.DadosEmpresa
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ValidationResult {
    data object Valido : ValidationResult
    data class Invalido(val motivo: String) : ValidationResult
}

/** SPEC_ONBOARDING.md §5 — função pura, mesma usada pelo wizard e (quando existir) pelo modo IA. */
object OnboardingEngine {

    fun validarEtapa(step: OnboardingStep, state: OnboardingState): ValidationResult = when (step) {
        OnboardingStep.EMPRESA ->
            if (state.empresa.nome.isNotBlank()) ValidationResult.Valido
            else ValidationResult.Invalido("Informe o nome da empresa")

        OnboardingStep.GESTOR ->
            if (state.gestor.nome.isNotBlank() && state.gestor.email.isNotBlank() && state.gestor.senha.isNotBlank()) {
                ValidationResult.Valido
            } else {
                ValidationResult.Invalido("Preencha nome, e-mail e senha do Gestor")
            }

        OnboardingStep.MODULOS ->
            if (state.modulosAtivos.isNotEmpty()) ValidationResult.Valido
            else ValidationResult.Invalido("Ative ao menos um módulo")

        OnboardingStep.CONTAS_FINANCEIRAS ->
            if (state.contas.isNotEmpty()) ValidationResult.Valido
            else ValidationResult.Invalido("Cadastre ao menos uma conta")

        else -> ValidationResult.Valido // etapas opcionais nunca bloqueiam
    }

    fun avancar(state: OnboardingState): OnboardingState {
        if (validarEtapa(state.etapaAtual, state) is ValidationResult.Invalido) return state
        val proxima = state.etapaAtual.proxima() ?: return state
        return state.copy(etapaAtual = proxima, etapasConcluidas = state.etapasConcluidas + state.etapaAtual)
    }

    /** Só etapas não-obrigatórias podem ser puladas sem preencher nada. */
    fun pular(state: OnboardingState): OnboardingState {
        if (state.etapaAtual.obrigatoria) return state
        val proxima = state.etapaAtual.proxima() ?: return state
        return state.copy(etapaAtual = proxima)
    }

    fun voltar(state: OnboardingState): OnboardingState {
        val anterior = state.etapaAtual.anterior() ?: return state
        return state.copy(etapaAtual = anterior)
    }

    fun podeConcluir(state: OnboardingState): Boolean =
        OnboardingStep.entries.filter { it.obrigatoria }
            .all { validarEtapa(it, state) is ValidationResult.Valido }

    /**
     * Grava tudo. Não é uma transação SQL única (os repositórios já abstraem SQLDelight vs.
     * Firestore vs. em-memória na Web) — `OnboardingConcluidoStore.marcarConcluido()` só é chamado
     * quando TUDO deu certo, então uma interrupção no meio faz o app voltar a mostrar o onboarding.
     *
     * O Gestor é criado **primeiro**: os repositórios Firestore de negócio (empresa, contas,
     * colaboradores, módulos) escrevem em `empresas/{empresaId}/...`, e isso só é possível depois
     * que o `EmpresaContexto` sabe qual é a empresa — o que só acontece dentro de `cadastrarGestor`,
     * junto com a conta do Firebase Auth (ver SessionManager/FirebaseSessionManager). Sem isso, as
     * escritas seguintes falhariam.
     *
     * Colaboradores pré-adicionados no wizard já ganham conta de verdade na hora (ver
     * SessionManager.criarColaborador) — só é possível chamar isso aqui porque o Gestor (e a
     * empresa) já existem nesse ponto.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun commitar(
        state: OnboardingState,
        empresaRepository: EmpresaRepository,
        permissaoRepository: PermissaoRepository,
        sessionManager: SessionManager,
        moduleConfigRepository: ModuleConfigRepository,
        contaRepository: ContaRepository,
        accessibilityPrefsStore: AccessibilityPrefsStore,
        draftStore: OnboardingDraftStore,
        onboardingConcluidoStore: OnboardingConcluidoStore,
    ) {
        check(podeConcluir(state)) { "Onboarding incompleto: faltam etapas obrigatórias" }

        val empresaId = Uuid.random().toString()

        val resultado = sessionManager.cadastrarGestor(state.gestor.nome, state.gestor.email, state.gestor.senha, empresaId)
        check(resultado is SessionManager.LoginResult.Sucesso) {
            "Falha ao criar conta do Gestor: ${(resultado as? SessionManager.LoginResult.Erro)?.mensagem ?: resultado}"
        }

        empresaRepository.salvar(
            DadosEmpresa(
                id = empresaId,
                nome = state.empresa.nome,
                logoUri = state.empresa.logoUri,
                cnpj = state.empresa.cnpj,
                telefone = state.empresa.telefone,
                endereco = state.empresa.endereco,
                cidade = state.empresa.cidade,
            ),
        )

        AppModule.entries.forEach { modulo ->
            moduleConfigRepository.definir(modulo.id, modulo in state.modulosAtivos)
        }

        val agora = Clock.System.now().toEpochMilliseconds()
        state.contas.forEach { contaDraft ->
            contaRepository.salvar(
                Conta(
                    id = Uuid.random().toString(),
                    nome = contaDraft.nome,
                    tipo = contaDraft.tipo,
                    saldoInicial = contaDraft.saldoInicialCentavos,
                    dataSaldoInicial = agora,
                    ativo = true,
                ),
            )
        }

        state.colaboradores.forEach { colaboradorDraft ->
            val resultadoColaborador = sessionManager.criarColaborador(colaboradorDraft.nome, colaboradorDraft.email, colaboradorDraft.senha)
            if (resultadoColaborador is SessionManager.LoginResult.Sucesso) {
                colaboradorDraft.permissoes.forEach { (moduleId, nivel) ->
                    permissaoRepository.definir(resultadoColaborador.colaborador.id, moduleId, nivel)
                }
            }
        }

        accessibilityPrefsStore.atualizar(state.acessibilidade)

        draftStore.salvarPreferenciasFuturas(
            PreferenciasPosOnboarding(
                usarCategoriasDefault = state.usarCategoriasDefault,
                usarBdiPadrao = state.usarBdiPadrao,
                usarTemplateEtapasPadrao = state.usarTemplateEtapasPadrao,
                primeiroProjeto = state.primeiroProjeto,
            ),
        )

        onboardingConcluidoStore.marcarConcluido()
        draftStore.limparRascunho()
    }
}
