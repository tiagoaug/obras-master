package br.com.tiago.obramaster.ui.features.equipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.equipes.RelatorioEquipesEngine
import br.com.tiago.obramaster.core.equipes.ResumoTrabalho
import br.com.tiago.obramaster.data.repository.EquipeRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.RegistroTrabalhoRepository
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.domain.Pessoa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ResumoPessoa(val pessoa: Pessoa, val resumo: ResumoTrabalho)
data class ResumoEquipe(val equipe: Equipe, val resumo: ResumoTrabalho)

data class RelatorioEquipesUiState(
    val porPessoa: List<ResumoPessoa> = emptyList(),
    val porEquipe: List<ResumoEquipe> = emptyList(),
)

/** SPEC_OBRA_MASTER.md §4.3 — "Relatório por funcionário e por equipe: dias trabalhados, total a pagar, total pago." */
class RelatorioEquipesViewModel(
    registroRepository: RegistroTrabalhoRepository,
    pessoaRepository: PessoaRepository,
    equipeRepository: EquipeRepository,
) : ViewModel() {

    val uiState: StateFlow<RelatorioEquipesUiState> = combine(
        registroRepository.observarTodos(),
        pessoaRepository.observarAtivas(),
        equipeRepository.observarAtivas(),
    ) { registros, pessoas, equipes ->
        val resumoPorPessoaId = RelatorioEquipesEngine.porPessoa(registros)
        val pessoasPorId = pessoas.associateBy { it.id }
        RelatorioEquipesUiState(
            porPessoa = resumoPorPessoaId.mapNotNull { (pessoaId, resumo) -> pessoasPorId[pessoaId]?.let { ResumoPessoa(it, resumo) } },
            porEquipe = equipes.map { equipe -> ResumoEquipe(equipe, RelatorioEquipesEngine.porEquipe(registros, equipe.membrosIds)) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RelatorioEquipesUiState())
}
