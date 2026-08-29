package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Permissao
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionEngineTest {

    private fun colaborador(ehGestor: Boolean = false) = Colaborador(
        id = "c1",
        nome = "Fulano",
        email = "fulano@obramaster.app",
        ativo = true,
        ehGestor = ehGestor,
    )

    @Test
    fun gestor_temAcessoTotalMesmoSemPermissaoExplicita() {
        val gestor = colaborador(ehGestor = true)
        assertTrue(PermissionEngine.canView(gestor, emptyList(), AppModule.FINANCEIRO))
        assertTrue(PermissionEngine.canEdit(gestor, emptyList(), AppModule.FINANCEIRO))
    }

    @Test
    fun colaboradorSemPermissao_naoVeNemEdita() {
        val user = colaborador()
        assertFalse(PermissionEngine.canView(user, emptyList(), AppModule.FINANCEIRO))
        assertFalse(PermissionEngine.canEdit(user, emptyList(), AppModule.FINANCEIRO))
    }

    @Test
    fun colaboradorComLeitura_veMasNaoEdita() {
        val user = colaborador()
        val perms = listOf(Permissao(user.id, AppModule.FINANCEIRO.id, NivelPermissao.LEITURA))
        assertTrue(PermissionEngine.canView(user, perms, AppModule.FINANCEIRO))
        assertFalse(PermissionEngine.canEdit(user, perms, AppModule.FINANCEIRO))
    }

    @Test
    fun colaboradorComEscrita_veEEdita() {
        val user = colaborador()
        val perms = listOf(Permissao(user.id, AppModule.FINANCEIRO.id, NivelPermissao.ESCRITA))
        assertTrue(PermissionEngine.canView(user, perms, AppModule.FINANCEIRO))
        assertTrue(PermissionEngine.canEdit(user, perms, AppModule.FINANCEIRO))
    }

    @Test
    fun permissaoDeOutroModulo_naoVaza() {
        val user = colaborador()
        val perms = listOf(Permissao(user.id, AppModule.FINANCEIRO.id, NivelPermissao.TOTAL))
        assertFalse(PermissionEngine.canView(user, perms, AppModule.PROJETOS))
    }
}
