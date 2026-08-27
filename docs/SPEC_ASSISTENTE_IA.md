# SPEC — Assistente de IA Integrado (ObraMaster)

> Complementa todas as specs anteriores.
> Colocar em: `docs/SPEC_ASSISTENTE_IA.md`
> Objetivo: um "gancho" onde a IA entende o programa inteiro (via manual + estado atual da tela) e responde perguntas do usuário, sempre citando **onde está no manual** e trazendo **um exemplo com os dados reais da situação**.

---

## 1. Visão Geral do Fluxo

```
Usuário pergunta em linguagem natural
        │
        ▼
1. Captura de CONTEXTO da tela atual (módulo, projeto, etapa, entidade aberta)
        │
        ▼
2. BUSCA no Manual indexado (local, funciona offline) → traz os trechos mais relevantes
        │
        ▼
3. (se online) Envia pergunta + contexto + trechos do manual para o modelo de IA (via backend)
        │
        ▼
4. Resposta estruturada: explicação + seção do manual + exemplo com dados reais + atalho de ação
```

- **Offline:** o app ainda funciona — mostra os trechos do manual encontrados por busca local, sem a prosa gerada pela IA.
- **Online:** a resposta fica natural, contextualizada e com o exemplo numérico calculado.

---

## 2. Indexação do Manual (fonte de conhecimento)

O `MANUAL_DO_PROGRAMA.md` é a única fonte de verdade sobre "como o app funciona". Ele é **estruturado com âncoras** (`#id`) por seção, exatamente como já entregue — isso não é acidental, é o contrato de indexação.

### 2.1 Pipeline de indexação (build-time, roda uma vez por versão do manual)

```
manual.md → parser de seções (## e ###, cada uma com #id)
          → ManualSection(id, moduloRelacionado, titulo, conteudo, exemplosPraticos[], imagemRef?)
          → gera manual_index.json, empacotado no app (assets)
          → na primeira execução, popula a tabela local ManualSection (SQLDelight)
```

```kotlin
data class ManualSection(
    val id: String,               // ex.: "financeiro-transferencia"
    val modulo: AppModule?,       // vínculo com o módulo correspondente, se houver
    val titulo: String,
    val conteudo: String,         // texto da seção, já sem markdown
    val exemploPratico: String?,  // parágrafo "Exemplo prático" extraído
    val palavrasChave: List<String> // extraídas automaticamente (tokenização simples)
)
```

- Reindexar é automático a cada atualização do app (o manual evolui junto com as features — **regra de processo**: nenhuma feature nova entra sem atualizar a seção correspondente do manual).

### 2.2 Busca local (funciona 100% offline)

```kotlin
object ManualSearchEngine {
    // Busca por palavras-chave com pontuação simples (BM25-lite), sem dependência de rede
    fun buscar(query: String, secoes: List<ManualSection>, top: Int = 3): List<ManualSection>
}
```

- Função pura em `commonMain`, sem dependência de embeddings/rede — garante que o Assistente **sempre** devolve algo, mesmo sem internet.
- Critério de pontuação: correspondência de palavras-chave + peso extra se a seção pertence ao `AppModule` da tela atual (contexto ganha prioridade).

---

## 3. Captura de Contexto da Tela

Todo `ViewModel` de tela expõe um objeto padronizado, atualizado sempre que a tela muda de estado:

```kotlin
data class TelaContexto(
    val modulo: AppModule,
    val telaId: String,                  // ex.: "projeto_detalhe"
    val entidadeAberta: EntidadeResumo?,  // ex.: projeto atual, com campos-chave já resumidos
    val filtrosAtivos: Map<String, String> = emptyMap()
)

data class EntidadeResumo(
    val tipo: String,          // "Projeto", "Etapa", "LancamentoFinanceiro"...
    val id: String,
    val camposChave: Map<String, String>  // ex.: {"orcamento": "320000.00", "gasto": "262400.00", "etapaAtual": "Acabamento"}
)
```

- `AssistenteViewModel` observa o `TelaContexto` atual (via um `CompositionLocal`/repositório central que cada tela atualiza ao entrar em foco).
- **Importante de privacidade:** `EntidadeResumo` carrega só os campos necessários para exemplificar (valores agregados), nunca a lista completa de dados sensíveis (ex.: não manda todos os lançamentos financeiros, só o saldo relevante).

---

## 4. Backend — Endpoint do Assistente

```
POST /assistant/ask
Body: {
  pergunta: string,
  contexto: TelaContexto,
  trechosManualLocais: [ManualSection]   // os já encontrados pela busca local, enviados como apoio
}

Response: {
  resposta: string,               // explicação em português, curta e direta
  secoesManual: [{ id, titulo }], // referências oficiais usadas
  exemploPratico: string,         // gerado com os dados reais do contexto
  acaoSugerida: { label: string, rota: string }? // atalho, se aplicável
}
```

### 4.1 Prompt de sistema (mantido no servidor, nunca no cliente)

O backend monta o prompt de sistema com:
1. Um resumo fixo do que é o ObraMaster e como seus módulos se relacionam (gerado a partir do próprio manual, não escrito à mão duas vezes)
2. Instrução explícita: **toda resposta deve citar a seção do manual usada** e **gerar um exemplo numérico com os dados do contexto recebido**, nunca inventar dado que não veio no `contexto`
3. Os `trechosManualLocais` relevantes daquela pergunta (evita mandar o manual inteiro toda vez — mais barato e mais preciso)

```kotlin
// server/assistant/AssistantService.kt
class AssistantService(private val aiClient: AnthropicClient) {
    suspend fun responder(pergunta: String, contexto: TelaContexto, trechos: List<ManualSection>): AssistantResponse {
        val prompt = PromptBuilder.montar(pergunta, contexto, trechos)
        val respostaIA = aiClient.perguntar(prompt) // chamada ao modelo, formato de saída estruturado (JSON)
        return AssistantResponse.parse(respostaIA)
    }
}
```

- Usa a própria API da Anthropic (mesmo padrão do bloco `anthropic_api_in_artifacts`, mas rodando no backend Ktor, não no cliente — chave de API nunca fica no app).
- Saída pedida ao modelo em **JSON estruturado** (resposta, secoesManual, exemploPratico, acaoSugerida) para o app renderizar de forma consistente, sem parsing frágil de texto livre.

---

## 5. Cliente — UI do Assistente

![Assistente IA](mockups/06_assistente_ia.svg)

- Botão flutuante (ícone "?") presente em **todas as telas**, definido uma vez no `Scaffold` raiz — não precisa ser adicionado tela por tela.
- Ao abrir, já mostra um chip de contexto ("📍 Contexto: Projeto X • Etapa Y") para o usuário confirmar que a pergunta vai considerar a tela certa.
- Resposta renderizada em 3 blocos visuais fixos (para ficar sempre previsível):
  1. **Explicação** (texto)
  2. **📖 Ver no Manual: Seção N** — toque abre o `MANUAL_DO_PROGRAMA.md` renderizado no app, já na âncora certa
  3. **💡 Exemplo com seus dados atuais** — card verde, com o cálculo já feito
  4. (opcional) botão **"→ Abrir [ação] agora"** — navega direto pra tela relevante

```kotlin
@Composable
fun AssistenteFab(navController: NavController) { /* renderiza em todo Scaffold raiz */ }

@Composable
fun AssistenteSheet(contexto: TelaContexto, viewModel: AssistenteViewModel) { /* UI descrita acima */ }
```

### 5.1 Visor do Manual no app

- O `MANUAL_DO_PROGRAMA.md` também é renderizado **dentro do app** (não só como doc de desenvolvimento) — uma tela "Ajuda" no menu "Mais", com busca e navegação por seção.
- Isso faz o manual servir **duas audiências com um conteúdo só**: documentação para o time de desenvolvimento (Antigravity) e ajuda real para o usuário final, sem duplicar texto.

---

## 6. Modelo de Dados (resumo)

```kotlin
// commonMain
data class ManualSection(...)             // seção 2.1
data class TelaContexto(...)              // seção 3
data class EntidadeResumo(...)            // seção 3
data class AssistantResponse(
    val resposta: String,
    val secoesManual: List<ManualSectionRef>,
    val exemploPratico: String,
    val acaoSugerida: AcaoSugerida?
)
data class ManualSectionRef(val id: String, val titulo: String)
data class AcaoSugerida(val label: String, val rota: String)
```

---

## 7. Regras Críticas

1. **O manual é a única fonte de verdade** sobre "como o app funciona" — a IA nunca responde algo que não esteja fundamentado numa seção do manual (evita alucinação sobre funcionalidades que não existem).
2. **Toda resposta cita a seção usada.** Se a IA não achar seção relevante, ela diz isso explicitamente em vez de inventar.
3. **Exemplo prático usa só dados já visíveis na tela atual** (via `EntidadeResumo`) — nunca busca dados extras do banco que o usuário não estava vendo.
4. **Funciona offline** com degradação graciosa: sem rede, mostra as seções do manual encontradas localmente, sem a prosa da IA.
5. **Privacidade:** só os campos-chave agregados do contexto atual vão para o backend/IA — nunca a base de dados inteira, nunca dados de outros colaboradores/projetos que não estão na tela.
6. **Chave de API da IA fica só no backend**, nunca embutida no app cliente.
7. **Todo módulo novo exige atualizar a seção correspondente do manual** antes de ser considerado "pronto" — é isso que mantém a IA confiável no longo prazo.

---

## 8. Fases de Implementação

| Fase | Entrega |
|---|---|
| **A** | Estruturar o manual com âncoras (já entregue) + parser/indexador + `ManualSearchEngine` local |
| **B** | `TelaContexto` propagado por todas as telas + botão flutuante + sheet de resposta (só com busca local, sem IA ainda) |
| **C** | Endpoint `/assistant/ask` no backend Ktor + integração com API da Anthropic + geração de exemplo numérico |
| **D** | Visor do manual dentro do app (tela "Ajuda") + navegação por âncora a partir da resposta da IA |
| **E** | Botão "Abrir ação agora" com navegação direta + telemetria de perguntas sem resposta satisfatória (para saber o que falta no manual) |

> Sugiro encaixar a Fase A/B junto da Fase 11 (Configurações/polimento) da spec KMP, e a Fase C junto da Fase 10 (Backend/Sync), já que ambas dependem do servidor existir.

---

## 9. Critérios de Aceite

- [ ] Pergunta feita na tela de um projeto específico recebe exemplo com os dados reais daquele projeto
- [ ] Toda resposta da IA aponta pelo menos uma seção do manual (`secoesManual` nunca vazio quando há seção relevante)
- [ ] Sem internet, o Assistente ainda responde com trechos do manual (sem travar, sem erro)
- [ ] Nenhuma chamada ao backend do Assistente inclui dados de outro projeto/colaborador fora do contexto atual
- [ ] Botão "Abrir ação agora" navega corretamente para a tela sugerida
- [ ] Atualizar uma seção do manual reflete na resposta da IA na build seguinte, sem alteração de código
