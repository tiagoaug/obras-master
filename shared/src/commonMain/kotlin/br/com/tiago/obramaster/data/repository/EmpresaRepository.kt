package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.DadosEmpresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface EmpresaRepository {
    suspend fun buscar(): DadosEmpresa?
    suspend fun salvar(empresa: DadosEmpresa)
}

class SqlDelightEmpresaRepository(
    private val db: ObraMasterDatabase,
) : EmpresaRepository {
    private val queries = db.empresaQueries

    override suspend fun buscar(): DadosEmpresa? = withContext(Dispatchers.Default) {
        queries.selectUnica().executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(empresa: DadosEmpresa) {
        withContext(Dispatchers.Default) {
            queries.upsert(
                id = empresa.id,
                nome = empresa.nome,
                logoUri = empresa.logoUri,
                cnpj = empresa.cnpj,
                telefone = empresa.telefone,
                endereco = empresa.endereco,
                cidade = empresa.cidade,
            )
        }
    }
}

private fun br.com.tiago.obramaster.db.Empresa.toDomain() = DadosEmpresa(
    id = id,
    nome = nome,
    logoUri = logoUri,
    cnpj = cnpj,
    telefone = telefone,
    endereco = endereco,
    cidade = cidade,
)
