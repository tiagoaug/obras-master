package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.CentroDeCusto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreCentroDeCustoRepository(empresaContexto: EmpresaContexto) : CentroDeCustoRepository {
    private val colecao = FirestoreCollection(empresaContexto, "centrosDeCusto", CentroDeCusto.serializer())

    override suspend fun listarAtivos(): List<CentroDeCusto> = colecao.listarTodos().filter { it.ativo }
    override suspend fun buscarPorProjetoId(projetoId: String): CentroDeCusto? =
        colecao.consultar { where { "projetoId" equalTo projetoId } }.firstOrNull()

    override suspend fun salvar(centro: CentroDeCusto) = colecao.salvar(centro.id, centro)
    override suspend fun atualizar(centro: CentroDeCusto) =
        colecao.atualizarCampos(centro.id, "nome" to centro.nome, "tipo" to centro.tipo.name)

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivos(): Flow<List<CentroDeCusto>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
