# Guia — Como Executar Este Projeto no Antigravity

> Colocar em: `docs/GUIA_ANTIGRAVITY.md`
> Passo a passo prático para você (Tiago) conduzir a geração do ObraMaster com o Antigravity, usando os documentos já produzidos.

---

## 1. Organize a pasta `docs/` antes de começar

Estrutura final que o Antigravity vai enxergar:

```
docs/
├── README.md                                  ← índice, sempre a primeira coisa a referenciar
├── SPEC_OBRA_MASTER.md
├── SPEC_OBRA_MASTER_KMP.md
├── SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md
├── SPEC_OBRA_MASTER_ADENDO_BDI.md
├── SPEC_ASSISTENTE_IA.md
├── SPEC_ONBOARDING.md
├── MANUAL_DO_PROGRAMA.md
├── manual_index.json
└── mockups/
    ├── 01_home.svg
    ├── 02_projeto_detalhe.svg
    ├── 03_financeiro.svg
    ├── 04_calculadora_campo.svg
    ├── 05_configuracoes_modulos.svg
    ├── 06_assistente_ia.svg
    ├── 07_onboarding_wizard.svg
    └── 08_onboarding_ia.svg
```

Baixe todos os arquivos que já gerei aqui e coloque exatamente nesses caminhos no seu repositório, **antes** de abrir o Antigravity. Ele lê o conteúdo de `docs/` como contexto do projeto.

---

## 2. Regra de ouro: uma fase por vez, nunca o projeto inteiro de uma vez

O maior erro nesse tipo de projeto é jogar tudo numa mensagem só. O Antigravity (como qualquer agente de código) produz resultado muito melhor quando você fatia o trabalho na ordem das **Fases** já definidas no `README.md`. Cada fase vira uma sessão de trabalho, você revisa o código gerado, testa, só então avança.

Ordem recomendada (já está no README, repetida aqui com o "prompt de abertura" de cada fase):

| Fase | O que pedir | Docs a referenciar |
|---|---|---|
| **0** | Setup do projeto KMP (Gradle multiplataforma, Koin, SQLDelight, kotlinx-datetime, tema base, esqueleto `expect/actual`, "Hello Obra" rodando em Android e iOS) | `SPEC_OBRA_MASTER_KMP.md` seções 1-2, 7-8 |
| **1** | Autenticação: Gestor, `ModuleRegistry`, `PermissionEngine`, Home responsiva | `SPEC_OBRA_MASTER.md` seções 3.1, 3.2; `SPEC_OBRA_MASTER_KMP.md` seção 10 |
| **1.5** | Onboarding: wizard + modo IA, `OnboardingEngine`, tela de Resumo | `SPEC_ONBOARDING.md` inteiro |
| **2** | Pessoas (com import de contatos/CSV), Cadastros Básicos, `LcrudScaffold`, `CalculatorTextField` | `SPEC_OBRA_MASTER.md` seções 4.10, 4.11, 5.2, 5.3 |
| **3** | Projetos, Etapas, `BudgetEngine`, custo/m² | `SPEC_OBRA_MASTER.md` seção 4.1 |
| **3.5** | Planta Baixa: editor de desenho (retângulo/polígono, cômodos, paredes) | `SPEC_PLANTA_BAIXA.md` seções 2-4 |
| **3.6** | Planta Baixa: importação por foto + calibração de escala | `SPEC_PLANTA_BAIXA.md` seção 5.1 |
| **3.65-3.68** | Planta Baixa: importação de DXF, SVG e PDF (com escala automática quando possível) | `SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md` inteiro |
| **4** | Financeiro completo: Contas, Centro de Custo, Contábil/Não Contábil, Categorias, gráficos | `SPEC_OBRA_MASTER.md` seção 4.2; `SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md` seções 2-5 |
| **5** | Equipes e Pagamentos, integração com Financeiro | `SPEC_OBRA_MASTER.md` seção 4.3 |
| **6** | Compras, Orçamentos (com BDI), Vendas | `SPEC_OBRA_MASTER.md` seções 4.4-4.6; `SPEC_OBRA_MASTER_ADENDO_BDI.md` inteiro |
| **7** | Planejamento, Execução (diário de obra, Gantt) | `SPEC_OBRA_MASTER.md` seções 4.7-4.8 |
| **8** | Calculadoras (científica, trig, áreas, volumes, engenharia) | `SPEC_OBRA_MASTER.md` seção 4.12 |
| **8.5-8.8** | Área do Executor: catálogo ABNT + biblioteca de manuais PDF | `SPEC_AREA_EXECUTOR.md` inteiro |
| **9** | Metas + `ExportEngine` (PDF/XLSX/JPG via Canvas) | `SPEC_OBRA_MASTER.md` seção 4.9; `SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md` seção 1 |
| **9.5** | Exportação da Planta Baixa em JPG/PDF | `SPEC_PLANTA_BAIXA.md` seção 6 |
| **10** | Backend Ktor + `SyncEngine` + endpoint do Assistente | `SPEC_OBRA_MASTER_KMP.md` seção 6; `SPEC_ASSISTENTE_IA.md` seção 4 |
| **11** | Configurações finais, Acessibilidade, Manual dentro do app, UI do Assistente de IA | `SPEC_ASSISTENTE_IA.md` seções 2, 5; `MANUAL_DO_PROGRAMA.md` inteiro |
| **11.5** (opcional) | OCR de sugestão de cotas na Planta Baixa | `SPEC_PLANTA_BAIXA.md` seção 5.2 |

---

## 3. Como escrever o prompt de cada fase

Estrutura recomendada para toda mensagem que você mandar ao Antigravity:

```
1. Referencie o(s) documento(s) da fase (ex.: "Leia docs/SPEC_OBRA_MASTER.md seção 4.1")
2. Diga exatamente qual é o escopo desta sessão (só essa fase, nada além)
3. Lembre as regras críticas que atravessam o projeto inteiro (ver seção 4 abaixo)
4. Peça para ele te dar um plano curto ANTES de gerar código, para você aprovar
```

### Exemplo de prompt real (Fase 3 — Projetos e Etapas)

```
Leia docs/README.md e docs/SPEC_OBRA_MASTER.md (seção 4.1) e
docs/SPEC_OBRA_MASTER_KMP.md (seções 2 e 3, sobre estrutura de projeto e SQLDelight).

Escopo desta sessão: implementar o módulo de Projetos e Etapas.
- Entidades Projeto e Etapa (schema SQLDelight em shared/commonMain/sqldelight)
- BudgetEngine como função pura em domain/, com testes em commonTest
- Telas de lista, formulário e detalhe seguindo o padrão LcrudScaffold já definido
- Cálculo de custo/m² (construção e terreno) visível no detalhe do projeto
- Use o wireframe docs/mockups/02_projeto_detalhe.svg como referência de layout

Regras obrigatórias (valem para toda fase, não repita implementação diferente):
- IDs são String UUID gerados no cliente, nunca autoincrement
- Valores monetários em Long (centavos), nunca Double/Float
- Nada de import android.* fora de androidMain
- Toda tela verifica ModuleRegistry e PermissionEngine antes de renderizar

Antes de gerar código, me dê um plano curto das entidades, arquivos e ordem de
implementação, para eu aprovar.
```

- Repare que o prompt **não** manda o Antigravity ler todas as specs de uma vez — só as relevantes daquela fase, mais o README para ele nunca perder o contexto geral.
- Pedir o "plano curto antes de gerar código" é o passo que mais economiza retrabalho — você pega erro de entendimento antes dele escrever 500 linhas na direção errada.

---

## 4. Regras que valem em toda fase (cole isso uma vez no início e reforce quando notar desvio)

```
Regras globais do projeto ObraMaster (sempre válidas):
1. IDs: String UUID gerado no cliente.
2. Dinheiro: Long em centavos.
3. Datas: kotlinx-datetime, armazenadas em UTC millis.
4. Nenhum código específico de plataforma em commonMain — use expect/actual.
5. Toda Engine (Budget, Finance, Meta, Permission, BDI, calculadoras) é função pura
   em commonMain, com testes em commonTest.
6. Toda tela usa o mesmo ViewModel nas 3 plataformas; só o layout muda por ScreenSize
   (COMPACT / MEDIUM / EXPANDED).
7. CalculatorTextField obrigatório em 100% dos campos de valor.
8. Toda tela checa ModuleRegistry (módulo ativo?) e PermissionEngine (usuário pode?)
   antes de renderizar ações de edição.
9. Registros financeiros são imutáveis após criados — correção é por estorno.
```

---

## 5. Depois que o código de uma fase for gerado

Checklist antes de avançar para a próxima fase:

- [ ] Compila em Android (mínimo, para ciclo rápido de teste)
- [ ] Testes de `commonTest` das Engines dessa fase passam
- [ ] Rodou pelo menos uma vez em iOS (evita acumular `actual` faltando)
- [ ] O comportamento bate com os critérios de aceite da seção correspondente na spec
- [ ] Se a fase envolveu telas novas, a seção correspondente do `MANUAL_DO_PROGRAMA.md` foi criada/atualizada (regra da seção 7 do `SPEC_ASSISTENTE_IA.md` — o manual tem que evoluir junto, senão o Assistente de IA fica desatualizado)

Se algo saiu diferente do esperado, corrija **nessa mesma sessão de fase**, não deixe dívida acumulando para depois — cada fase depende das anteriores estarem sólidas.

---

## 6. Sobre a Fase 10 (Backend) e a Fase 11 (Assistente de IA)

Essas duas são as mais sensíveis porque envolvem segredo (chave de API) e infraestrutura fora do app. Recomendo:

1. Peça ao Antigravity para gerar o backend Ktor **sem** a chave de API hardcoded — sempre via variável de ambiente.
2. Rode o backend localmente primeiro (`./gradlew :server:run`), teste os endpoints com um cliente HTTP (Postman/Insomnia) antes de plugar no app.
3. Para o Assistente de IA (`SPEC_ASSISTENTE_IA.md`), gere primeiro a Fase A/B (indexação do manual + busca local, sem IA) e só depois a Fase C (endpoint com chamada real à API) — assim você já tem o Assistente funcionando (modo offline) antes de depender de rede/custo de API.
4. O `manual_index.json` que já te entreguei é o formato de saída esperado do parser — pode usar como exemplo/fixture de teste ao pedir ao Antigravity para implementar o parser real do `MANUAL_DO_PROGRAMA.md`.

---

## 7. Se o Antigravity "perder o fio" em alguma sessão

Sintomas comuns: ele reimplementa algo que já existe, ignora uma regra global, ou mistura padrão de uma fase com outra. Quando isso acontecer:

1. Pare a geração.
2. Cole de novo a seção 4 deste guia (regras globais).
3. Aponte especificamente o arquivo/trecho que já existe e deve ser reaproveitado, não recriado.
4. Peça para ele listar o que vai mudar antes de mudar.

Isso é mais rápido do que deixar ele seguir errado e você corrigir depois manualmente.

---

## 8. Ordem de leitura recomendada para você mesmo (antes de começar a operar o Antigravity)

Se você quiser revisar tudo por cima antes de começar:

1. `SPEC_OBRA_MASTER.md` — entenda o sistema todo primeiro
2. `SPEC_OBRA_MASTER_KMP.md` — entenda por que cada decisão técnica foi tomada
3. Este guia (`GUIA_ANTIGRAVITY.md`) — entenda como conduzir
4. Os demais (adendos, onboarding, assistente, manual) — consulte conforme for chegando em cada fase

Boa sorte com o projeto — é um sistema grande e ambicioso, mas fatiado em fases pequenas e bem definidas, cada sessão no Antigravity fica administrável.
