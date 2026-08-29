package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.CategoriaFinanceira
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreCategoriaFinanceiraRepository (mobileMain). */
interface CategoriaFinanceiraRepository {
    suspend fun listarAtivas(): List<CategoriaFinanceira>
    suspend fun salvar(categoria: CategoriaFinanceira)
    suspend fun atualizar(categoria: CategoriaFinanceira)
    suspend fun desativar(id: String)

    /** Insere as categorias padrão do sistema na primeira vez que o app roda (SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5). */
    suspend fun garantirCategoriasPadrao()
    fun observarAtivas(): Flow<List<CategoriaFinanceira>>
}
