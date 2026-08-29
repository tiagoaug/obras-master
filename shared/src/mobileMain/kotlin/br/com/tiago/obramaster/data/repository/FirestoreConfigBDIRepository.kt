package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.ConfigBDI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreConfigBDIRepository(empresaContexto: EmpresaContexto) : ConfigBDIRepository {
    private val colecao = FirestoreCollection(empresaContexto, "configsBdi", ConfigBDI.serializer())

    private suspend fun limparPadrao(exceto: String) {
        colecao.listarTodos().filter { it.padrao && it.id != exceto }.forEach { colecao.atualizarCampos(it.id, "padrao" to false) }
    }

    override suspend fun listarAtivos(): List<ConfigBDI> = colecao.listarTodos().filter { it.ativo }

    override suspend fun salvar(config: ConfigBDI) {
        colecao.salvar(config.id, config)
        if (config.padrao) limparPadrao(config.id)
    }

    override suspend fun atualizar(config: ConfigBDI) {
        colecao.salvar(config.id, config)
        if (config.padrao) limparPadrao(config.id)
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivos(): Flow<List<ConfigBDI>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
