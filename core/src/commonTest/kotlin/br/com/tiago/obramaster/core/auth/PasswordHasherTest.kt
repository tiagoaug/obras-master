package br.com.tiago.obramaster.core.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun verify_aceitaSenhaCorreta() {
        val hashed = PasswordHasher.hash("senhaSegura123")
        assertTrue(PasswordHasher.verify("senhaSegura123", hashed.saltBase64, hashed.hashBase64))
    }

    @Test
    fun verify_rejeitaSenhaErrada() {
        val hashed = PasswordHasher.hash("senhaSegura123")
        assertFalse(PasswordHasher.verify("senhaErrada", hashed.saltBase64, hashed.hashBase64))
    }

    @Test
    fun hash_geraSaltDiferenteACadaChamada() {
        val a = PasswordHasher.hash("mesmaSenha")
        val b = PasswordHasher.hash("mesmaSenha")
        assertNotEquals(a.saltBase64, b.saltBase64)
        assertNotEquals(a.hashBase64, b.hashBase64)
    }
}
