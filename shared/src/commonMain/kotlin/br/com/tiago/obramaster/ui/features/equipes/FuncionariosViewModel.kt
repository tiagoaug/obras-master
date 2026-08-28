package br.com.tiago.obramaster.ui.features.equipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.FuncionarioRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.domain.TipoContratacao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FuncionarioComPessoa(val pessoa: Pessoa, val funcionario: Funcionario)

data class FuncionariosUiState(
    val funcionarios: List<FuncionarioComPessoa> = emptyList(),
    val pessoasDisponiveis: List<Pessoa> = emptyList(), // ativas e ainda sem registro de Funcionario
)

class FuncionariosViewModel(
    private val funcionarioRepository: FuncionarioRepository,
    private val pessoaRepository: PessoaRepository,
) : ViewModel() {

    val uiState: StateFlow<FuncionariosUiState> = combine(
        funcionarioRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
    ) { funcionarios, pessoas ->
        val pessoasPorId = pessoas.associateBy { it.id }
        val funcionariosComPessoa = funcionarios.mapNotNull { funcionario ->
            pessoasPorId[funcionario.pessoaId]?.let { pessoa -> FuncionarioComPessoa(pessoa, funcionario) }
        }
        val idsComFuncionario = funcionarios.map { it.pessoaId }.toSet()
        FuncionariosUiState(
            funcionarios = funcionariosComPessoa,
            pessoasDisponiveis = pessoas.filter { it.id !in idsComFuncionario },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FuncionariosUiState())

    fun salvar(pessoaId: String, funcao: String, tipoContratacao: TipoContratacao, valorBase: Long, existente: Funcionario?) {
        viewModelScope.launch {
            if (existente != null) {
                funcionarioRepository.atualizar(existente.copy(funcao = funcao, tipoContratacao = tipoContratacao, valorBase = valorBase))
            } else {
                // Garante a tag FUNCIONARIO na Pessoa escolhida, mesmo que ela já exista com outras tags (ex.: também é cliente).
                val pessoa = uiState.value.pessoasDisponiveis.firstOrNull { it.id == pessoaId }
                if (pessoa != null && TagPessoa.FUNCIONARIO !in pessoa.tags) {
                    pessoaRepository.atualizar(pessoa.copy(tags = pessoa.tags + TagPessoa.FUNCIONARIO))
                }
                funcionarioRepository.salvar(Funcionario(pessoaId = pessoaId, funcao = funcao, tipoContratacao = tipoContratacao, valorBase = valorBase))
            }
        }
    }

    fun excluir(pessoaId: String) {
        viewModelScope.launch { funcionarioRepository.desativar(pessoaId) }
    }
}
