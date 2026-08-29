package br.com.tiago.obramaster.core.assistant

import br.com.tiago.obramaster.core.modules.AppModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualSearchEngineTest {

    private val secoes = listOf(
        ManualSection(
            id = "financeiro-transferencia",
            modulo = AppModule.FINANCEIRO,
            titulo = "Transferência entre contas",
            conteudo = "Como mover dinheiro de uma conta pra outra sem virar receita nem despesa.",
            exemploPratico = "Transferir R$ 500 do Caixa da Obra pra Conta Corrente.",
            palavrasChave = listOf("transferência", "conta", "mover dinheiro"),
        ),
        ManualSection(
            id = "projetos",
            modulo = AppModule.PROJETOS,
            titulo = "Projetos e Etapas",
            conteudo = "Cadastro de projetos, etapas e acompanhamento de orçamento.",
            exemploPratico = null,
            palavrasChave = listOf("projeto", "etapa", "orçamento"),
        ),
        ManualSection(
            id = "acessibilidade",
            modulo = null,
            titulo = "Acessibilidade",
            conteudo = "Tema claro, escuro, alto contraste e tamanho de fonte ajustável.",
            exemploPratico = null,
            palavrasChave = listOf("tema", "fonte", "contraste"),
        ),
    )

    @Test
    fun `busca vazia nao retorna nada`() {
        assertEquals(emptyList(), ManualSearchEngine.buscar("", secoes))
        assertEquals(emptyList(), ManualSearchEngine.buscar("   ", secoes))
    }

    @Test
    fun `encontra secao por palavra-chave mesmo com acentuacao diferente`() {
        val resultado = ManualSearchEngine.buscar("como fazer transferencia entre contas", secoes)
        assertEquals("financeiro-transferencia", resultado.first().id)
    }

    @Test
    fun `sem termos relevantes nao retorna secoes`() {
        assertEquals(emptyList(), ManualSearchEngine.buscar("xyz abc qwerty", secoes))
    }

    @Test
    fun `contexto do modulo atual da peso extra em caso de empate parcial`() {
        // "projeto" bate em título+palavra-chave de "projetos" (score alto o bastante sozinho),
        // mas o bônus de contexto ainda deve manter "projetos" à frente de qualquer outra seção
        // que também mencione a palavra de leve no conteúdo.
        val resultado = ManualSearchEngine.buscar("projeto", secoes, moduloContexto = AppModule.PROJETOS)
        assertTrue(resultado.isNotEmpty())
        assertEquals("projetos", resultado.first().id)
    }

    @Test
    fun `respeita o limite top`() {
        val resultado = ManualSearchEngine.buscar("conta", secoes, top = 1)
        assertEquals(1, resultado.size)
    }
}
