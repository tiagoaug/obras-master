package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.CATEGORIAS_PADRAO_NOMES
import br.com.tiago.obramaster.domain.CATEGORIA_PADRAO_RECEITA_VENDAS
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FirestoreCategoriaFinanceiraRepository(empresaContexto: EmpresaContexto) : CategoriaFinanceiraRepository {
    private val colecao = FirestoreCollection(empresaContexto, "categoriasFinanceiras", CategoriaFinanceira.serializer())

    override suspend fun listarAtivas(): List<CategoriaFinanceira> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(categoria: CategoriaFinanceira) = colecao.salvar(categoria.id, categoria)

    override suspend fun atualizar(categoria: CategoriaFinanceira) {
        colecao.atualizarCampos(
            categoria.id,
            "nome" to categoria.nome,
            "tipo" to categoria.tipo.name,
            "naturezaPadrao" to categoria.naturezaPadrao.name,
            "categoriaPaiId" to categoria.categoriaPaiId,
            "cor" to categoria.cor,
            "icone" to categoria.icone,
        )
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun garantirCategoriasPadrao() {
        val jaExistem = colecao.listarTodos().any { it.padraoDoSistema }
        if (jaExistem) return
        CATEGORIAS_PADRAO_NOMES.forEach { nome ->
            salvar(
                CategoriaFinanceira(
                    id = Uuid.random().toString(), nome = nome, tipo = TipoLancamento.DESPESA,
                    naturezaPadrao = NaturezaLancamento.CONTABIL, cor = "#90A4AE", padraoDoSistema = true, ativo = true,
                ),
            )
        }
        salvar(
            CategoriaFinanceira(
                id = Uuid.random().toString(), nome = CATEGORIA_PADRAO_RECEITA_VENDAS, tipo = TipoLancamento.RECEITA,
                naturezaPadrao = NaturezaLancamento.CONTABIL, cor = "#66BB6A", padraoDoSistema = true, ativo = true,
            ),
        )
    }

    override fun observarAtivas(): Flow<List<CategoriaFinanceira>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
