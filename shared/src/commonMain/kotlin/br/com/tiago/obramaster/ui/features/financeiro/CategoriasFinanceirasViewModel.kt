package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CategoriasFinanceirasViewModel(
    private val repository: CategoriaFinanceiraRepository,
) : ViewModel() {

    /** Raízes primeiro, filhas logo depois do pai — visão em árvore sem precisar de um componente novo. */
    val categorias: StateFlow<List<CategoriaFinanceira>> = repository.observarAtivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun ordenadasEmArvore(lista: List<CategoriaFinanceira>): List<CategoriaFinanceira> {
        val porPai = lista.groupBy { it.categoriaPaiId }
        val resultado = mutableListOf<CategoriaFinanceira>()
        fun adicionar(paiId: String?) {
            porPai[paiId].orEmpty().sortedBy { it.nome }.forEach { categoria ->
                resultado += categoria
                adicionar(categoria.id)
            }
        }
        adicionar(null)
        return resultado
    }

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: CategoriaFinanceira?,
        nome: String,
        tipo: TipoLancamento,
        naturezaPadrao: NaturezaLancamento,
        categoriaPaiId: String?,
        cor: String,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(
                    existente.copy(nome = nome, tipo = tipo, naturezaPadrao = naturezaPadrao, categoriaPaiId = categoriaPaiId, cor = cor),
                )
            } else {
                repository.salvar(
                    CategoriaFinanceira(
                        id = Uuid.random().toString(),
                        nome = nome,
                        tipo = tipo,
                        naturezaPadrao = naturezaPadrao,
                        categoriaPaiId = categoriaPaiId,
                        cor = cor,
                    ),
                )
            }
        }
    }

    /** Categoria padrão do sistema não é excluível (SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5) — o botão nem aparece, ver podeExcluir. */
    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
