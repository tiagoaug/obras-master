package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface RegistroTrabalhoRepository {
    suspend fun listarDaPessoa(pessoaId: String): List<RegistroTrabalho>
    suspend fun listarNaoPagosDaPessoa(pessoaId: String): List<RegistroTrabalho>
    suspend fun listarTodos(): List<RegistroTrabalho>
    suspend fun salvar(registro: RegistroTrabalho)
    suspend fun marcarPagos(ids: List<String>, pagamentoId: String)
    suspend fun desativar(id: String)
    fun observarTodos(): Flow<List<RegistroTrabalho>>
}

class SqlDelightRegistroTrabalhoRepository(
    private val db: ObraMasterDatabase,
) : RegistroTrabalhoRepository {
    private val queries = db.registroTrabalhoQueries

    override suspend fun listarDaPessoa(pessoaId: String): List<RegistroTrabalho> = withContext(Dispatchers.Default) {
        queries.selectAtivosDaPessoa(pessoaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun listarNaoPagosDaPessoa(pessoaId: String): List<RegistroTrabalho> = withContext(Dispatchers.Default) {
        queries.selectNaoPagosDaPessoa(pessoaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun listarTodos(): List<RegistroTrabalho> = withContext(Dispatchers.Default) {
        queries.selectTodosAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(registro: RegistroTrabalho) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = registro.id,
                pessoaId = registro.pessoaId,
                projetoId = registro.projetoId,
                etapaId = registro.etapaId,
                data_ = registro.data,
                tipo = registro.tipo.name,
                valor = registro.valor,
                observacao = registro.observacao,
                pago = registro.pago,
                pagamentoId = registro.pagamentoId,
                ativo = registro.ativo,
            )
        }
    }

    override suspend fun marcarPagos(ids: List<String>, pagamentoId: String) {
        withContext(Dispatchers.Default) {
            db.transaction { ids.forEach { id -> queries.marcarPagos(pagamentoId, id) } }
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarTodos(): Flow<List<RegistroTrabalho>> =
        queries.selectTodosAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.RegistroTrabalho.toDomain() = RegistroTrabalho(
    id = id,
    pessoaId = pessoaId,
    projetoId = projetoId,
    etapaId = etapaId,
    data = data_,
    tipo = TipoRegistroTrabalho.valueOf(tipo),
    valor = valor,
    observacao = observacao,
    pago = pago,
    pagamentoId = pagamentoId,
    ativo = ativo,
)
