package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class ModuloDoc(val moduleId: String = "", val enabled: Boolean = true)

class FirestoreModuleConfigRepository(empresaContexto: EmpresaContexto) : ModuleConfigRepository {
    private val colecao = FirestoreCollection(empresaContexto, "modulos", ModuloDoc.serializer())

    override suspend fun listarTodos(): Map<String, Boolean> = colecao.listarTodos().associate { it.moduleId to it.enabled }

    override suspend fun definir(moduleId: String, enabled: Boolean) {
        colecao.salvar(moduleId, ModuloDoc(moduleId, enabled))
    }

    override fun observarTodos(): Flow<Map<String, Boolean>> =
        colecao.observarTodos().map { lista -> lista.associate { it.moduleId to it.enabled } }
}
