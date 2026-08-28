package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.TipoConta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ContaRepository {
    suspend fun listarAtivas(): List<Conta>
    suspend fun buscarPorId(id: String): Conta?
    suspend fun salvar(conta: Conta)

    /** saldoInicial/dataSaldoInicial não são editáveis aqui — usar um MovimentoConta tipo AJUSTE pra corrigir saldo depois de criada. */
    suspend fun atualizar(conta: Conta)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Conta>>
}

class SqlDelightContaRepository(
    private val db: ObraMasterDatabase,
) : ContaRepository {
    private val queries = db.contaQueries

    override suspend fun listarAtivas(): List<Conta> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorId(id: String): Conta? = withContext(Dispatchers.Default) {
        queries.selectPorId(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(conta: Conta) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = conta.id,
                nome = conta.nome,
                tipo = conta.tipo.name,
                banco = conta.banco,
                agencia = conta.agencia,
                numeroConta = conta.numeroConta,
                saldoInicial = conta.saldoInicial,
                dataSaldoInicial = conta.dataSaldoInicial,
                ativo = conta.ativo,
                cor = conta.cor,
            )
        }
    }

    override suspend fun atualizar(conta: Conta) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = conta.nome,
                tipo = conta.tipo.name,
                banco = conta.banco,
                agencia = conta.agencia,
                numeroConta = conta.numeroConta,
                cor = conta.cor,
                id = conta.id,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivas(): Flow<List<Conta>> =
        queries.selectAtivas().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Conta.toDomain() = Conta(
    id = id,
    nome = nome,
    tipo = TipoConta.valueOf(tipo),
    banco = banco,
    agencia = agencia,
    numeroConta = numeroConta,
    saldoInicial = saldoInicial,
    dataSaldoInicial = dataSaldoInicial,
    ativo = ativo,
    cor = cor,
)
