package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Material
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreMaterialRepository(empresaContexto: EmpresaContexto) : MaterialRepository {
    private val colecao = FirestoreCollection(empresaContexto, "materiais", Material.serializer())

    override suspend fun listarAtivos(): List<Material> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(material: Material) = colecao.salvar(material.id, material)
    override suspend fun atualizar(material: Material) = colecao.salvar(material.id, material)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivos(): Flow<List<Material>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
