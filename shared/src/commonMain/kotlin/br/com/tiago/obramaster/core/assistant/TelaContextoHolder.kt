package br.com.tiago.obramaster.core.assistant

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** SPEC_ASSISTENTE_IA.md §3 — "repositório central que cada tela atualiza ao entrar em foco".
 * Singleton via Koin; App.kt atualiza `modulo`/`telaId` centralizadamente a cada troca de tela
 * (deriva do `TelaRaiz` atual), e telas específicas que têm uma entidade carregada (ex.:
 * ProjetoDetalheScreen) complementam com `atualizarEntidade`. */
class TelaContextoHolder {
    private val _atual = MutableStateFlow<TelaContexto?>(null)
    val atual: StateFlow<TelaContexto?> = _atual.asStateFlow()

    fun definir(contexto: TelaContexto) {
        _atual.value = contexto
    }

    /** Atualiza só a entidade aberta da tela atual, preservando módulo/telaId — usado por telas
     * que carregam a entidade de forma assíncrona, depois que a navegação já aconteceu. */
    fun atualizarEntidade(entidade: EntidadeResumo?) {
        _atual.value = _atual.value?.copy(entidadeAberta = entidade)
    }
}
