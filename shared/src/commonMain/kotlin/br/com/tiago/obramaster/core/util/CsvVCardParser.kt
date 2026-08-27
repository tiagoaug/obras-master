package br.com.tiago.obramaster.core.util

import br.com.tiago.obramaster.domain.ContatoImportado

/** SPEC_OBRA_MASTER_KMP.md §4.1 — caminho de importação de contatos na Web (sem agenda nativa). */
object CsvVCardParser {

    fun parsear(conteudo: String): List<ContatoImportado> {
        val texto = conteudo.trim()
        return if (texto.contains("BEGIN:VCARD", ignoreCase = true)) parsearVCard(texto) else parsearCsv(texto)
    }

    private fun parsearVCard(conteudo: String): List<ContatoImportado> {
        val contatos = mutableListOf<ContatoImportado>()
        var nome: String? = null
        var telefone: String? = null
        var email: String? = null

        conteudo.lineSequence().forEach { linhaBruta ->
            val linha = linhaBruta.trim()
            when {
                linha.equals("BEGIN:VCARD", ignoreCase = true) -> {
                    nome = null
                    telefone = null
                    email = null
                }
                linha.equals("END:VCARD", ignoreCase = true) -> {
                    nome?.let { contatos.add(ContatoImportado(nome = it, telefone = telefone, email = email)) }
                }
                linha.startsWith("FN:", ignoreCase = true) -> nome = linha.substringAfter(":").trim()
                linha.startsWith("TEL", ignoreCase = true) && telefone == null ->
                    telefone = linha.substringAfter(":").trim().takeIf { it.isNotBlank() }
                linha.startsWith("EMAIL", ignoreCase = true) && email == null ->
                    email = linha.substringAfter(":").trim().takeIf { it.isNotBlank() }
            }
        }
        return contatos
    }

    private fun parsearCsv(conteudo: String): List<ContatoImportado> {
        val linhas = conteudo.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (linhas.isEmpty()) return emptyList()

        val cabecalho = dividirLinhaCsv(linhas.first()).map { it.trim().lowercase() }
        val indiceNome = cabecalho.indexOfFirst { it.contains("nome") }
        val indiceTelefone = cabecalho.indexOfFirst { it.contains("telefone") || it.contains("fone") || it.contains("phone") }
        val indiceEmail = cabecalho.indexOfFirst { it.contains("email") || it.contains("e-mail") }
        if (indiceNome == -1) return emptyList()

        return linhas.drop(1).mapNotNull { linha ->
            val colunas = dividirLinhaCsv(linha)
            val nome = colunas.getOrNull(indiceNome)?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            ContatoImportado(
                nome = nome,
                telefone = indiceTelefone.takeIf { it >= 0 }?.let { colunas.getOrNull(it)?.trim()?.takeIf { t -> t.isNotBlank() } },
                email = indiceEmail.takeIf { it >= 0 }?.let { colunas.getOrNull(it)?.trim()?.takeIf { e -> e.isNotBlank() } },
            )
        }
    }

    private fun dividirLinhaCsv(linha: String): List<String> {
        val campos = mutableListOf<String>()
        val atual = StringBuilder()
        var dentroDeAspas = false
        for (c in linha) {
            when {
                c == '"' -> dentroDeAspas = !dentroDeAspas
                c == ',' && !dentroDeAspas -> {
                    campos.add(atual.toString())
                    atual.clear()
                }
                else -> atual.append(c)
            }
        }
        campos.add(atual.toString())
        return campos
    }
}
