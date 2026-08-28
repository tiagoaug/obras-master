package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.CATEGORIAS_PADRAO_NOMES
import br.com.tiago.obramaster.domain.CATEGORIA_PADRAO_RECEITA_VENDAS
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface CategoriaFinanceiraRepository {
    suspend fun listarAtivas(): List<CategoriaFinanceira>
    suspend fun salvar(categoria: CategoriaFinanceira)
    suspend fun atualizar(categoria: CategoriaFinanceira)
    suspend fun desativar(id: String)

    /** Insere as categorias padrão do sistema na primeira vez que o app roda (SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5). */
    suspend fun garantirCategoriasPadrao()
    fun observarAtivas(): Flow<List<CategoriaFinanceira>>
}

class SqlDelightCategoriaFinanceiraRepository(
    private val db: ObraMasterDatabase,
) : CategoriaFinanceiraRepository {
    private val queries = db.categoriaFinanceiraQueries

    override suspend fun listarAtivas(): List<CategoriaFinanceira> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(categoria: CategoriaFinanceira) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = categoria.id,
                nome = categoria.nome,
                tipo = categoria.tipo.name,
                naturezaPadrao = categoria.naturezaPadrao.name,
                categoriaPaiId = categoria.categoriaPaiId,
                cor = categoria.cor,
                icone = categoria.icone,
                padraoDoSistema = categoria.padraoDoSistema,
                ativo = categoria.ativo,
            )
        }
    }

    override suspend fun atualizar(categoria: CategoriaFinanceira) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = categoria.nome,
                tipo = categoria.tipo.name,
                naturezaPadrao = categoria.naturezaPadrao.name,
                categoriaPaiId = categoria.categoriaPaiId,
                cor = categoria.cor,
                icone = categoria.icone,
                id = categoria.id,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun garantirCategoriasPadrao() {
        withContext(Dispatchers.Default) {
            val jaExistem = queries.existeAlgumaPadrao().executeAsOne() > 0
            if (jaExistem) return@withContext
            CATEGORIAS_PADRAO_NOMES.forEach { nome ->
                queries.insert(
                    id = Uuid.random().toString(),
                    nome = nome,
                    tipo = TipoLancamento.DESPESA.name,
                    naturezaPadrao = NaturezaLancamento.CONTABIL.name,
                    categoriaPaiId = null,
                    cor = "#90A4AE",
                    icone = null,
                    padraoDoSistema = true,
                    ativo = true,
                )
            }
            queries.insert(
                id = Uuid.random().toString(),
                nome = CATEGORIA_PADRAO_RECEITA_VENDAS,
                tipo = TipoLancamento.RECEITA.name,
                naturezaPadrao = NaturezaLancamento.CONTABIL.name,
                categoriaPaiId = null,
                cor = "#66BB6A",
                icone = null,
                padraoDoSistema = true,
                ativo = true,
            )
        }
    }

    override fun observarAtivas(): Flow<List<CategoriaFinanceira>> =
        queries.selectAtivas().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.CategoriaFinanceira.toDomain() = CategoriaFinanceira(
    id = id,
    nome = nome,
    tipo = TipoLancamento.valueOf(tipo),
    naturezaPadrao = NaturezaLancamento.valueOf(naturezaPadrao),
    categoriaPaiId = categoriaPaiId,
    cor = cor,
    icone = icone,
    padraoDoSistema = padraoDoSistema,
    ativo = ativo,
)
