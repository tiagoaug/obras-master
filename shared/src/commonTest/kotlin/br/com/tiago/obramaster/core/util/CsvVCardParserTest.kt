package br.com.tiago.obramaster.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvVCardParserTest {

    @Test
    fun parseiaCsvComCabecalho() {
        val csv = """
            nome,telefone,email
            João Silva,11999999999,joao@email.com
            Maria Souza,11988888888,
        """.trimIndent()

        val contatos = CsvVCardParser.parsear(csv)

        assertEquals(2, contatos.size)
        assertEquals("João Silva", contatos[0].nome)
        assertEquals("11999999999", contatos[0].telefone)
        assertEquals("joao@email.com", contatos[0].email)
        assertEquals("Maria Souza", contatos[1].nome)
        assertEquals(null, contatos[1].email)
    }

    @Test
    fun parseiaCsvComCamposEntreAspas() {
        val csv = """
            nome,telefone
            "Silva, João",11999999999
        """.trimIndent()

        val contatos = CsvVCardParser.parsear(csv)

        assertEquals(1, contatos.size)
        assertEquals("Silva, João", contatos[0].nome)
    }

    @Test
    fun parseiaVCardComMultiplosContatos() {
        val vcard = """
            BEGIN:VCARD
            FN:João Silva
            TEL:11999999999
            EMAIL:joao@email.com
            END:VCARD
            BEGIN:VCARD
            FN:Maria Souza
            END:VCARD
        """.trimIndent()

        val contatos = CsvVCardParser.parsear(vcard)

        assertEquals(2, contatos.size)
        assertEquals("João Silva", contatos[0].nome)
        assertEquals("11999999999", contatos[0].telefone)
        assertEquals("Maria Souza", contatos[1].nome)
        assertEquals(null, contatos[1].telefone)
    }

    @Test
    fun csvSemColunaNomeRetornaVazio() {
        val csv = "telefone\n11999999999"
        assertEquals(emptyList(), CsvVCardParser.parsear(csv))
    }
}
