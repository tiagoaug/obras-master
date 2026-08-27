# ObraMaster — Índice da Documentação

> Colocar em: `docs/README.md`
> Ponto de entrada para o Antigravity e para qualquer pessoa nova no projeto.

## Como ler esta documentação

Leia nesta ordem na primeira vez. Depois, use como referência conforme for gerando cada módulo.

| # | Arquivo | O que define |
|---|---|---|
| 1 | `SPEC_OBRA_MASTER.md` | Visão geral, módulos, entidades, regras de negócio, fases de implementação (base do sistema) |
| 2 | `SPEC_OBRA_MASTER_KMP.md` | Arquitetura multiplataforma (Android, iOS, Web): estrutura de projeto, persistência, expect/actual, backend, sincronização |
| 3 | `SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md` | Módulo financeiro avançado: Contas, Centro de Custo, Contábil x Não Contábil, Categorias, exportação JPG via Canvas, funcionalidades do segmento |
| 4 | `SPEC_OBRA_MASTER_ADENDO_BDI.md` | Cálculo de BDI para o módulo de Orçamentos (fórmula, engine, integração na tela) |
| 5 | `SPEC_ASSISTENTE_IA.md` | Assistente de IA integrado: indexação do manual, contexto de tela, backend, UI |
| 6 | `SPEC_ONBOARDING.md` | Primeiro acesso: wizard tradicional + onboarding guiado por IA, mesmo estado compartilhado |
| 7 | `SPEC_PLANTA_BAIXA.md` | Editor de planta baixa (desenho de cômodos/paredes) + importação por foto com calibração de escala e OCR assistido |
| 7b | `SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md` | Importação de DXF, PDF e SVG diretamente na área de Projeto, com leitura de geometria e escala automática quando possível |
| 8 | `SPEC_AREA_EXECUTOR.md` | Catálogo de normas ABNT (índice) + biblioteca pessoal de manuais em PDF, com busca full-text offline |
| 9 | `MANUAL_DO_PROGRAMA.md` | Manual do usuário final — também é a base de conhecimento consultada pelo Assistente de IA |
| — | `mockups/*.svg` | Wireframes ilustrativos das telas principais, referenciados pelo manual |
| — | `manual_index.json` | Exemplo do formato de saída do parser do manual — fixture para implementar o indexador do Assistente de IA |
| — | `GUIA_ANTIGRAVITY.md` | Passo a passo prático de como conduzir a geração do projeto no Antigravity, fase por fase |
| — | `INSTRUCAO_MESTRA.md` | **Colar no Antigravity antes de começar** — contrato de como as specs devem ser seguidas à risca, fase por fase |
| — | `CHECKLIST_TESTES.md` | Roteiro de testes manuais, uma seção por fase — valide antes de avançar para a próxima |

## Regra de manutenção

Sempre que uma funcionalidade nova for implementada:

1. A entidade/regra entra na spec correspondente (1-4)
2. A tela correspondente ganha (ou atualiza) sua seção no `MANUAL_DO_PROGRAMA.md`, com âncora `#id`
3. Isso mantém o Assistente de IA (item 5) confiável — ele só sabe o que está escrito no manual

## Como começar de verdade

1. Coloque toda a pasta `docs/` (arquivos e `mockups/`) no seu repositório.
2. Abra o Antigravity e cole o conteúdo de `INSTRUCAO_MESTRA.md` inteiro, como primeira mensagem.
3. Aguarde a confirmação de entendimento, então peça a Fase 0.
4. Depois de cada fase entregue, valide contra `CHECKLIST_TESTES.md` antes de pedir a próxima.

## Ordem sugerida de execução no Antigravity

```
Fase 0-1   → Setup KMP + Auth + ModuleRegistry + PermissionEngine        (docs 1, 2)
Fase 1.5   → Onboarding (wizard + modo IA)                               (docs 6, 2)
Fase 2-3   → Pessoas, Cadastros, Projetos/Etapas                         (docs 1, 2)
Fase 3.5-6 → Planta Baixa: editor de desenho + importação por foto/OCR   (doc 7)
Fase 3.65-3.68 → Planta Baixa: importação de DXF, SVG e PDF             (doc 7b)
Fase 4-5   → Financeiro completo + Contas/Centro de Custo + Equipes      (docs 1, 2, 3)
Fase 6     → Compras, Orçamentos (com BDI), Vendas                       (docs 1, 3, 4)
Fase 7     → Planejamento, Execução                                     (docs 1)
Fase 8     → Calculadoras                                                (docs 1)
Fase 8.5-8 → Área do Executor: catálogo ABNT + biblioteca de manuais PDF  (doc 8)
Fase 9     → Exportação PDF/XLSX/JPG (Canvas)                            (docs 2, 3)
Fase 10    → Backend Ktor + Sync + Endpoint do Assistente                (docs 2, 5)
Fase 11    → Configurações, Acessibilidade, Manual dentro do app, IA UI  (docs 5, 6)
```

## Glossário rápido

- **Engine**: função pura, sem dependência de Android/iOS/Web, testável em `commonTest`, roda igual nas 3 plataformas
- **expect/actual**: mecanismo do Kotlin Multiplatform para código específico de cada plataforma (câmera, contatos, arquivos)
- **BDI**: Bonificação e Despesas Indiretas — percentual aplicado sobre o custo direto para formar o preço de venda no Orçamento
- **Centro de Custo**: agrupador de despesas/receitas, gerado automaticamente por Projeto, ou administrativo/comercial
- **Natureza (Contábil / Não Contábil)**: se o lançamento entra no fechamento oficial repassado ao contador ou só no controle gerencial interno
