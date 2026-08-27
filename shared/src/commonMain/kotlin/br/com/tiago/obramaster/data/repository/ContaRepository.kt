package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.TipoConta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ContaRepository {
    suspend fun listarAtivas(): List<Conta>
    suspend fun salvar(conta: Conta)
}

class SqlDelightContaRepository(
    private val db: ObraMasterDatabase,
) : ContaRepository {
    private val queries = db.contaQueries

    override suspend fun listarAtivas(): List<Conta> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
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
