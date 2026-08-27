# ADENDO — Módulo Financeiro Avançado (Centro de Custo, Contas, Contábil/Não Contábil)

> Complementa `SPEC_OBRA_MASTER.md` e `SPEC_OBRA_MASTER_KMP.md`.
> Colocar em: `docs/SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md`

---

## 1. Exportação JPG — padrão Canvas (ajuste à Fase 9)

Substitui a estratégia de renderização de JPG descrita na spec KMP (seção 5.2):

- Todo relatório exportável é desenhado por um **Composable de relatório** usando `Canvas`/`DrawScope`, comum às 3 plataformas.
- `ReportCanvasRenderer` (em `commonMain`) recebe o `ExportableDocument` e desenha: cabeçalho com logo/empresa, tabela de colunas/linhas, linha de resumo, rodapé.
- Captura do Canvas em bitmap via `ImageComposeScene` (Compose Multiplatform) → `ImageBitmap` → encode JPEG (Skia, disponível nas 3 plataformas Compose).
- **Vantagem:** o mesmo `ReportCanvasRenderer` também pode virar o preview de tela cheia antes de exportar/compartilhar — sem lógica duplicada.
- A etapa de *salvar/compartilhar o arquivo* continua no contrato `expect/actual FileExporter` (isso é o único pedaço realmente específico de plataforma).

```kotlin
object ReportCanvasRenderer {
    fun draw(scope: DrawScope, doc: ExportableDocument, size: Size)
}
```

---

## 2. Cadastros: Contábil x Não Contábil

Todo cadastro financeiro (Categoria, Lançamento, Conta) ganha um flag:

```kotlin
enum class NaturezaLancamento { CONTABIL, NAO_CONTABIL }
```

- **Contábil**: movimentações formais, com nota fiscal/recibo, que compõem o resultado oficial da empresa (para contador, DRE, impostos).
- **Não Contábil**: movimentações informais/gerenciais (ex.: adiantamento entre sócios, caixinha de obra, vale a funcionário sem nota) — entram no controle gerencial mas **não** no fechamento contábil.
- Todo relatório financeiro tem **filtro por natureza**: "Somente Contábil", "Somente Não Contábil", "Ambos".
- O **DRE gerencial** (ver seção 6) considera só lançamentos `CONTABIL` por padrão, com opção de incluir os dois.
- Isso resolve uma dor real do segmento: obra sempre tem uma parte de caixa "por fora" que precisa aparecer no controle, mas não pode contaminar a contabilidade oficial repassada ao contador.

---

## 3. Centro de Custo

```
CentroDeCusto(id, nome, tipo: PROJETO | ADMINISTRATIVO | COMERCIAL | OUTRO,
              projetoId?, ativo)
```

- Todo `LancamentoFinanceiro` passa a ter `centroDeCustoId` (além de `projetoId`/`etapaId`, que continuam existindo — projeto/etapa é o centro de custo operacional da obra; Centro de Custo cobre também despesas administrativas que **não** pertencem a nenhuma obra, ex.: aluguel do escritório, salário do administrativo, combustível da frota).
- Ao criar um Projeto, o sistema **gera automaticamente** um Centro de Custo vinculado (1:1), mantendo a spec original intacta — quem já usa por projeto/etapa não perde nada.
- Relatório de **Resultado por Centro de Custo**: receita − despesa por centro, no período filtrado, exportável.
- Rateio: lançamento pode ser dividido percentualmente entre múltiplos centros de custo (ex.: conta de luz do escritório rateada entre 3 obras em andamento) — tabela `RateioLancamento(lancamentoId, centroDeCustoId, percentual)`.

---

## 4. Contas (Bancárias / Caixa) e Movimentações

```
Conta(id, nome, tipo: CAIXA | CONTA_CORRENTE | POUPANCA | CARTAO_CREDITO | INVESTIMENTO,
      banco?, agencia?, numeroConta?, saldoInicial, dataSaldoInicial, ativo, cor?)

MovimentoConta(id, contaId, tipo: PAGAMENTO | RECEBIMENTO | TRANSFERENCIA_SAIDA | TRANSFERENCIA_ENTRADA | AJUSTE,
               valor, data, descricao, lancamentoFinanceiroId?, transferenciaVinculoId?, conciliado: Boolean)
```

### 4.1 Regras

- **Pagamento** e **Recebimento** de contas sempre vinculados a uma `Conta` — todo `LancamentoFinanceiro` (receita/despesa) ao ser marcado como "pago"/"recebido" exige escolher a conta de origem/destino, gerando o `MovimentoConta` correspondente automaticamente.
- **Transferência entre contas**: tela dedicada (conta origem, conta destino, valor, data, motivo) → gera **dois** `MovimentoConta` espelhados (`TRANSFERENCIA_SAIDA` na origem, `TRANSFERENCIA_ENTRADA` no destino), ligados por `transferenciaVinculoId`. **Não gera** `LancamentoFinanceiro` (transferência não é receita nem despesa — é só movimentação patrimonial, para não distorcer o DRE).
- **Saldo de conta** = `saldoInicial` + soma de todos os `MovimentoConta` até a data — função pura `SaldoContaEngine.calcular(conta, movimentos, ateData)`.
- **Extrato por conta**: lista cronológica de movimentos com saldo corrente (igual extrato bancário), exportável.
- **Conciliação bancária** (simples): tela para marcar `conciliado = true` comparando com extrato real do banco; filtro "não conciliados" ajuda a achar divergência.
- Dashboard: **saldo consolidado** (soma de todas as contas ativas) + saldo por conta em cards.

### 4.2 Integração com módulos existentes

- **Compras** → pagamento ao fornecedor debita a Conta escolhida.
- **Pagamentos de mão de obra** → idem.
- **Vendas** (recebimento de parcela) → credita a Conta escolhida.
- **Orçamentos aprovados** não geram movimento — só o lançamento real de venda/recebimento gera.

---

## 5. Categorias — Configuração Completa

Expande `CategoriaFinanceira` da spec original:

```
CategoriaFinanceira(id, nome, tipo: RECEITA | DESPESA, naturezaPadrao: NaturezaLancamento,
                     categoriaPaiId?, cor, icone?, ativo)
```

- **Hierarquia** (categoria e subcategoria) — ex.: "Materiais" → "Cimento e Argamassa", "Elétrica", "Hidráulica". Permite relatório tanto no nível macro quanto detalhado.
- Tela em **Configurações → Categorias**: CRUD completo, com árvore expansível, drag-and-drop para reordenar/reclassificar.
- Categoria "padrão do sistema" (não excluível, só inativável) para as básicas: Materiais, Mão de Obra, Equipamentos, Administrativo, Impostos, Transporte, Alimentação de equipe, Combustível.
- Toda categoria tem `naturezaPadrao` — ao lançar uma despesa nessa categoria, o campo Natureza (Contábil/Não Contábil) já vem pré-preenchido, mas editável no lançamento.

---

## 6. Funcionalidades Comuns do Segmento (recomendação)

Baseado em como ERPs de construção civil/financeiro no Brasil (Bling, Conta Azul, Sienge, Arquimedes) organizam isso, recomendo priorizar estas, em ordem de impacto/esforço:

### Alto impacto, esforço moderado — recomendo entrar já na Fase 4/5

| Funcionalidade | Por quê |
|---|---|
| **Fluxo de caixa projetado** | Mostra não só o realizado, mas contas a pagar/receber futuras (próximos 30/60/90 dias) — evita ficar sem caixa no meio da obra. Deriva dos dados que já existem (`LancamentoFinanceiro` com `pago=false` e `data` futura). |
| **Medição de obra** (measurement-based billing) | Padrão do mercado brasileiro: cliente contratante paga por "medições" periódicas conforme % executado por etapa. `Medicao(id, projetoId, numero, dataReferencia, percentualAcumulado, valorMedido, aprovada)`. Gera automaticamente a parcela de venda/recebimento correspondente. |
| **Retenções fiscais em serviços** | INSS (11%), ISS, IRRF sobre nota de mão de obra/empreitada. Campo `retencoes: List<Retencao>` no lançamento, com cálculo automático do valor líquido a pagar. Comum em contratos formais de empreitada. |
| **Conciliação bancária** | Já descrita acima — baixo esforço, alto valor percebido pelo Gestor. |
| **Curva ABC de materiais/fornecedores** | Ranking de maior gasto — deriva quase de graça do módulo Compras já especificado, só precisa de uma consulta agregada. |

### Médio impacto — bom para depois do MVP

| Funcionalidade | Por quê |
|---|---|
| **DRE gerencial simplificado** | Receita − custos diretos (material+MO por obra) − despesas administrativas rateadas = resultado. Só lançamentos `CONTABIL`. |
| **Contratos e aditivos** | Registro do contrato com cliente (valor, escopo, prazo) + aditivos de valor/prazo — muito comum em obra ter aditivo por mudança de escopo. |
| **Controle de estoque de materiais** | Se ele compra material antecipado para múltiplas obras (não só sob demanda), vale ter saldo de estoque por material, com baixa ao alocar para uma obra/etapa. |
| **Apontamento de horas por colaborador** | Além de diária/empreitada — controle mais fino de produtividade por etapa. |
| **BDI (Bonificação e Despesas Indiretas)** | Percentual aplicado sobre o custo direto para formar o preço de venda no Orçamento — cálculo padrão de mercado na construção civil. |

### Baixo prioridade agora (nice-to-have futuro)

- Portal do cliente (cliente acompanha % de progresso da própria obra, medições e pagamentos, só leitura) — muito natural quando a Web já existir.
- Assinatura digital de contratos e orçamentos.
- Integração com emissão de NF-e/NFS-e (webservice da prefeitura/SEFAZ) — complexidade alta, deixar para quando o financeiro já estiver maduro.
- Curva S (previsto x realizado) no Planejamento — visual bonito, mas exige histórico consolidado primeiro.

---

## 7. Impacto nas Entidades Existentes

```
LancamentoFinanceiro(
  ..., // campos já existentes
  centroDeCustoId,
  contaId?,               // conta usada no pagamento/recebimento
  natureza: NaturezaLancamento,
  retencoes: List<Retencao> = emptyList()
)

Retencao(tipo: INSS | ISS | IRRF | OUTRO, percentual, valorCalculado)
```

---

## 8. Ajuste nas Fases de Implementação

| Fase | Ajuste |
|---|---|
| **4** (Financeiro) | Incluir: Contas, MovimentoConta, Transferências, Centro de Custo, Natureza Contábil/Não Contábil, Categorias hierárquicas, filtros por natureza/centro |
| **5** (Equipes/Pagamentos) | Pagamento de mão de obra passa a debitar Conta escolhida + suporte a Retenções (INSS) quando aplicável |
| **6** (Compras/Orçamentos/Vendas) | Pagamento a fornecedor debita Conta; Orçamento ganha campo de BDI opcional |
| **7** (Planejamento/Execução) | Incluir módulo de Medição de Obra vinculado às Etapas |
| **9** (Exportação) | `ReportCanvasRenderer` como estratégia única de JPG (e como preview) |
| **Nova — 9.5** | Fluxo de Caixa Projetado + Conciliação Bancária + Curva ABC (relatórios derivados, baixo custo de implementação) |

---

## 9. Critérios de Aceite Adicionais

- [ ] Transferência entre contas nunca aparece como receita/despesa no DRE
- [ ] Saldo de cada conta bate com `saldoInicial + soma dos movimentos`
- [ ] Lançamento Não Contábil pode ser filtrado para fora de qualquer relatório oficial
- [ ] Centro de Custo de um Projeto é criado automaticamente ao criar o Projeto
- [ ] Rateio de lançamento entre centros de custo soma sempre 100%
- [ ] Medição aprovada gera automaticamente a cobrança/recebimento vinculado
- [ ] Retenção calculada corretamente reduz o valor líquido do pagamento de mão de obra
- [ ] JPG exportado via Canvas é visualmente idêntico ao PDF do mesmo relatório
