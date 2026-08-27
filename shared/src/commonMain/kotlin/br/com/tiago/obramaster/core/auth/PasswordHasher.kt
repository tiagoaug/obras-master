package br.com.tiago.obramaster.core.auth

import org.kotlincrypto.SecureRandom
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * PBKDF2-HMAC-SHA256 puro (org.kotlincrypto), em vez de BCrypt (JVM-only) —
 * ver decisão registrada na Fase 1: precisa rodar igual em Android/iOS/Web.
 */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val SALT_LENGTH_BYTES = 16
    private const val KEY_LENGTH_BYTES = 32

    data class Hashed(val hashBase64: String, val saltBase64: String)

    @OptIn(ExperimentalEncodingApi::class)
    fun hash(password: String): Hashed {
        val salt = SecureRandom().nextBytesOf(SALT_LENGTH_BYTES)
        val derived = pbkdf2HmacSha256(password.encodeToByteArray(), salt, ITERATIONS, KEY_LENGTH_BYTES)
        return Hashed(Base64.encode(derived), Base64.encode(salt))
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun verify(password: String, saltBase64: String, expectedHashBase64: String): Boolean {
        val salt = Base64.decode(saltBase64)
        val expectedHash = Base64.decode(expectedHashBase64)
        val derived = pbkdf2HmacSha256(password.encodeToByteArray(), salt, ITERATIONS, KEY_LENGTH_BYTES)
        return constantTimeEquals(derived, expectedHash)
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }

    private fun pbkdf2HmacSha256(password: ByteArray, salt: ByteArray, iterations: Int, keyLengthBytes: Int): ByteArray {
        val hmac = HmacSHA256(password)
        val hLen = hmac.macLength()
        val numBlocks = (keyLengthBytes + hLen - 1) / hLen
        val output = ByteArray(numBlocks * hLen)

        for (blockIndex in 1..numBlocks) {
            val blockIndexBytes = byteArrayOf(
                (blockIndex ushr 24).toByte(),
                (blockIndex ushr 16).toByte(),
                (blockIndex ushr 8).toByte(),
                blockIndex.toByte(),
            )
            var u = hmac.doFinal(salt + blockIndexBytes)
            val t = u.copyOf()
            for (iteration in 2..iterations) {
                u = hmac.doFinal(u)
                for (i in t.indices) t[i] = (t[i].toInt() xor u[i].toInt()).toByte()
            }
            t.copyInto(output, (blockIndex - 1) * hLen)
        }

        return output.copyOf(keyLengthBytes)
    }
}
