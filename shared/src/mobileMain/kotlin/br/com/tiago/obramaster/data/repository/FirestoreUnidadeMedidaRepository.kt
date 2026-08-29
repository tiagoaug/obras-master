package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreUnidadeMedidaRepository(empresaContexto: EmpresaContexto) : UnidadeMedidaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "unidadesMedida", UnidadeMedida.serializer())

    override suspend fun listarAtivas(): List<UnidadeMedida> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(unidade: UnidadeMedida) = colecao.salvar(unidade.id, unidade)
    override suspend fun atualizar(unidade: UnidadeMedida) = colecao.salvar(unidade.id, unidade)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<UnidadeMedida>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
