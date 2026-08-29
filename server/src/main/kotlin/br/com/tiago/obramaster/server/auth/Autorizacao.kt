package br.com.tiago.obramaster.server.auth

import br.com.tiago.obramaster.core.auth.PermissionEngine
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.server.repository.ColaboradorRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

/** SPEC_OBRA_MASTER_KMP.md §6.2 — "a PermissionEngine roda também no servidor; permissão nunca é
 * validada só no cliente". Reaproveita a mesma engine pura de :core, só busca o Colaborador e as
 * Permissoes no banco em vez de vir do estado local do app. */
suspend fun ApplicationCall.autorizarOuResponder(module: AppModule, exigirEscrita: Boolean): RequestPrincipal? {
    val principal = requestPrincipal()
    val colaborador = ColaboradorRepository.buscarPorId(principal.colaboradorId)
    if (colaborador == null || !colaborador.ativo) {
        respond(HttpStatusCode.Unauthorized, mapOf("erro" to "Colaborador inválido ou inativo"))
        return null
    }
    val permissoes = ColaboradorRepository.permissoesDoColaborador(principal.colaboradorId)
    val autorizado = if (exigirEscrita) {
        PermissionEngine.canEdit(colaborador, permissoes, module)
    } else {
        PermissionEngine.canView(colaborador, permissoes, module)
    }
    if (!autorizado) {
        respond(HttpStatusCode.Forbidden, mapOf("erro" to "Sem permissão para este módulo"))
        return null
    }
    return principal
}
