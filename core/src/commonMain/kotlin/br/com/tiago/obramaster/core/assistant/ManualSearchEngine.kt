package br.com.tiago.obramaster.core.assistant

import br.com.tiago.obramaster.core.modules.AppModule

/** SPEC_ASSISTENTE_IA.md §2.2 — busca por palavras-chave com pontuação simples (BM25-lite), sem
 * dependência de rede/embeddings. Função pura: garante que o Assistente sempre devolve algo,
 * mesmo sem internet (regra crítica §7.4 da spec). */
object ManualSearchEngine {

    /** @param moduloContexto se a tela atual pertence a um módulo, seções desse módulo ganham
     * peso extra — "o contexto ganha prioridade" (§2.2). */
    fun buscar(query: String, secoes: List<ManualSection>, moduloContexto: AppModule? = null, top: Int = 3): List<ManualSection> {
        val termos = tokenizar(query)
        if (termos.isEmpty()) return emptyList()

        return secoes
            .map { it to pontuar(it, termos, moduloContexto) }
            .filter { (_, pontos) -> pontos > 0 }
            .sortedByDescending { (_, pontos) -> pontos }
            .take(top)
            .map { (secao, _) -> secao }
    }

    private fun pontuar(secao: ManualSection, termos: List<String>, moduloContexto: AppModule?): Int {
        val tituloNorm = normalizar(secao.titulo)
        val conteudoNorm = normalizar(secao.conteudo)
        val palavrasChaveNorm = secao.palavrasChave.map { normalizar(it) }

        var pontos = 0
        for (termo in termos) {
            if (palavrasChaveNorm.any { it.contains(termo) }) pontos += 3
            if (tituloNorm.contains(termo)) pontos += 2
            if (conteudoNorm.contains(termo)) pontos += 1
        }
        if (pontos > 0 && moduloContexto != null && secao.modulo == moduloContexto) pontos += 2
        return pontos
    }

    private fun tokenizar(texto: String): List<String> =
        normalizar(texto).split(Regex("\\s+")).filter { it.length >= 3 }

    private fun normalizar(texto: String): String = texto.lowercase()
        .replace("á", "a").replace("à", "a").replace("â", "a").replace("ã", "a")
        .replace("é", "e").replace("ê", "e")
        .replace("í", "i")
        .replace("ó", "o").replace("ô", "o").replace("õ", "o")
        .replace("ú", "u")
        .replace("ç", "c")
}
