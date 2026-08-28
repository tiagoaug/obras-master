package br.com.tiago.obramaster.ui.features.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.FornecedorRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.domain.Fornecedor
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FornecedorComPessoa(val pessoa: Pessoa, val fornecedor: Fornecedor)

data class FornecedoresUiState(
    val fornecedores: List<FornecedorComPessoa> = emptyList(),
    val pessoasDisponiveis: List<Pessoa> = emptyList(), // ativas e ainda sem registro de Fornecedor
)

class FornecedoresViewModel(
    private val fornecedorRepository: FornecedorRepository,
    private val pessoaRepository: PessoaRepository,
) : ViewModel() {

    val uiState: StateFlow<FornecedoresUiState> = combine(
        fornecedorRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
    ) { fornecedores, pessoas ->
        val pessoasPorId = pessoas.associateBy { it.id }
        val comPessoa = fornecedores.mapNotNull { fornecedor -> pessoasPorId[fornecedor.pessoaId]?.let { FornecedorComPessoa(it, fornecedor) } }
        val idsComFornecedor = fornecedores.map { it.pessoaId }.toSet()
        FornecedoresUiState(fornecedores = comPessoa, pessoasDisponiveis = pessoas.filter { it.id !in idsComFornecedor })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FornecedoresUiState())

    fun salvar(pessoaId: String, cnpjCpf: String?, observacoes: String?, existente: Fornecedor?) {
        viewModelScope.launch {
            if (existente != null) {
                fornecedorRepository.atualizar(existente.copy(cnpjCpf = cnpjCpf, observacoes = observacoes))
            } else {
                val pessoa = uiState.value.pessoasDisponiveis.firstOrNull { it.id == pessoaId }
                if (pessoa != null && TagPessoa.FORNECEDOR !in pessoa.tags) {
                    pessoaRepository.atualizar(pessoa.copy(tags = pessoa.tags + TagPessoa.FORNECEDOR))
                }
                fornecedorRepository.salvar(Fornecedor(pessoaId = pessoaId, cnpjCpf = cnpjCpf, observacoes = observacoes))
            }
        }
    }

    fun excluir(pessoaId: String) {
        viewModelScope.launch { fornecedorRepository.desativar(pessoaId) }
    }
}
