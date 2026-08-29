package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.ConfigBDI
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreConfigBDIRepository (mobileMain). */
interface ConfigBDIRepository {
    suspend fun listarAtivos(): List<ConfigBDI>

    /** Se [config].padrao for true, desmarca padrao dos demais — só um perfil padrão por vez. */
    suspend fun salvar(config: ConfigBDI)
    suspend fun atualizar(config: ConfigBDI)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<ConfigBDI>>
}
