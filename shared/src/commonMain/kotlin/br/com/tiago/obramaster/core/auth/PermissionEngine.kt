package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Permissao

object PermissionEngine {

    fun nivelDe(user: Colaborador, perms: List<Permissao>, module: AppModule): NivelPermissao {
        if (user.ehGestor) return NivelPermissao.TOTAL
        return perms.firstOrNull { it.colaboradorId == user.id && it.moduleId == module.id }?.nivel
            ?: NivelPermissao.NENHUM
    }

    fun canView(user: Colaborador, perms: List<Permissao>, module: AppModule): Boolean =
        nivelDe(user, perms, module) != NivelPermissao.NENHUM

    fun canEdit(user: Colaborador, perms: List<Permissao>, module: AppModule): Boolean =
        nivelDe(user, perms, module).let { it == NivelPermissao.ESCRITA || it == NivelPermissao.TOTAL }
}
