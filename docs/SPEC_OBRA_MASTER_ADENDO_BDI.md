# ADENDO 2 — BDI (Bonificação e Despesas Indiretas) no Módulo de Orçamentos

> Complementa `SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md`.
> Colocar em: `docs/SPEC_OBRA_MASTER_ADENDO_BDI.md`

---

## 1. O que é e por que importa

O **custo direto** de uma obra (material + mão de obra + equipamentos, apurado item a item no Orçamento) **não é o preço de venda**. O BDI é o percentual aplicado sobre o custo direto para chegar ao **preço de venda final**, cobrindo tudo que não está nos itens, mas que existe de verdade: administração central, tributos, risco, lucro.

```
Preço de Venda = Custo Direto × (1 + BDI)
```

Sem isso formalizado, é muito comum orçar "no olho" e descobrir no fim da obra que o lucro real foi menor (ou negativo) que o esperado, porque impostos e despesas indiretas comeram a margem.

---

## 2. Composição padrão do BDI (mercado brasileiro)

O BDI é decomposto em **taxas parciais**, cada uma configurável, para dar transparência (e permitir ajustar cada uma conforme o tipo de obra/cliente):

| Componente | Sigla | Descrição | Faixa típica de mercado |
|---|---|---|---|
| Administração Central | AC | Estrutura da empresa (escritório, contador, pró-labore) rateada sobre as obras | 3% – 8% |
| Seguros e Garantias | S | Seguro de obra, garantia contratual | 0,5% – 1,5% |
| Riscos | R | Imprevistos, reserva de contingência | 0,5% – 2,5% |
| Despesas Financeiras | DF | Custo do capital de giro/financiamento da obra | 0,5% – 1,5% |
| Lucro | L | Margem de lucro pretendida | 6% – 15% |
| Tributos (sobre o faturamento) | I | ISS + PIS + COFINS (+ CPRB se aplicável) | 4% – 6,5% (varia por regime tributário) |

### 2.1 Fórmula (a mais usada no mercado — a mesma lógica do TCU para obras públicas, adaptada)

```
BDI = [ ( (1 + AC + S + R + DF) × (1 + L) ) / (1 − I) ] − 1
```

- **Por que os tributos entram dividindo e não somando:** tributos como ISS/PIS/COFINS incidem sobre o **preço de venda** (faturamento), não sobre o custo. Se você simplesmente somar o percentual de imposto ao BDI, o cálculo fica errado e o lucro real sai menor do que o planejado. Dividir por `(1 − I)` "gross-up" corrige isso corretamente — é a forma tecnicamente certa e a que o TCU exige em obras públicas.
- Todas as taxas (AC, S, R, DF, L, I) entram como **decimal** (ex.: 5% = 0,05).

### 2.2 Exemplo numérico

```
AC = 4%, S = 0,8%, R = 1%, DF = 1%, L = 10%, I = 5,65% (Simples Nacional, faixa comum)

BDI = [ (1 + 0,04 + 0,008 + 0,01 + 0,01) × (1 + 0,10) / (1 − 0,0565) ] − 1
    = [ (1,068 × 1,10) / 0,9435 ] − 1
    = [ 1,1748 / 0,9435 ] − 1
    = 1,2452 − 1
    = 0,2452 → BDI = 24,52%
```

Ou seja: sobre um custo direto de R$ 100.000, o preço de venda seria **R$ 124.520**.

---

## 3. Modelo de Dados

```kotlin
data class ConfigBDI(
    val id: String,
    val nome: String,               // ex.: "Padrão", "Cliente Público", "Reforma Residencial"
    val administracaoCentral: Double,  // decimal, ex.: 0.04
    val seguroGarantia: Double,
    val riscos: Double,
    val despesasFinanceiras: Double,
    val lucro: Double,
    val tributos: Double,           // I — soma de ISS+PIS+COFINS(+CPRB) conforme regime
    val padrao: Boolean = false     // usada por padrão em novo orçamento
)
```

- Cadastro em **Configurações → BDI**: CRUD completo, permite criar múltiplos perfis (ex.: obra pública exige BDI diferenciado por lei/edital; reforma residencial pequena pode ter lucro maior por menor volume).
- `Orcamento` (entidade já existente) ganha:

```kotlin
data class Orcamento(
    // ...campos já existentes...
    val configBdiId: String?,          // perfil de BDI usado
    val bdiPercentualCalculado: Double, // resultado, congelado no momento do envio
    val custoDiretoTotal: Long,        // soma dos ItemOrcamento (centavos)
    val precoVendaTotal: Long          // custoDiretoTotal × (1 + bdi), centavos
)
```

> **Importante:** o BDI calculado é **congelado** (snapshot) no orçamento no momento em que ele é enviado/aprovado. Se depois você mudar a configuração padrão de BDI, orçamentos já enviados não devem mudar retroativamente — só novos orçamentos usam a config atualizada.

---

## 4. Engine (função pura, `commonMain`)

```kotlin
object BdiEngine {

    data class ResultadoBdi(
        val bdiPercentual: Double,
        val precoVenda: Long   // centavos
    )

    fun calcularBdi(config: ConfigBDI): Double {
        val base = (1 + config.administracaoCentral + config.seguroGarantia +
                    config.riscos + config.despesasFinanceiras)
        val comLucro = base * (1 + config.lucro)
        val comTributos = comLucro / (1 - config.tributos)
        return comTributos - 1
    }

    fun aplicarBdi(custoDireto: Long, config: ConfigBDI): ResultadoBdi {
        val bdi = calcularBdi(config)
        val preco = (custoDireto * (1 + bdi)).toLong()
        return ResultadoBdi(bdi, preco)
    }
}
```

- Testes obrigatórios em `commonTest`: validar o exemplo numérico da seção 2.2 e casos-limite (I = 0, L = 0, todas as taxas = 0 → BDI = 0).
- **Validação de sanidade na UI:** se `tributos >= 1.0` (100%) ou resultar em divisão por zero/negativo, bloquear salvar e avisar o Gestor — combinação de taxas inválida.

---

## 5. Integração na Tela de Orçamento

- Ao montar os itens (materiais + mão de obra), o app já mostra o **subtotal de custo direto** em tempo real (já existente na spec original).
- Novo card: **"Aplicar BDI"** — seletor do perfil de `ConfigBDI` (com botão "ver detalhamento" mostrando cada taxa) → mostra automaticamente:
  - BDI calculado (%)
  - Preço de venda total
  - Markup implícito (preço venda / custo direto), útil pra comparação rápida
- Opção de **BDI manual pontual**: sobrescrever o percentual final só naquele orçamento específico (ex.: negociação com cliente), mantendo registrado que foi um valor customizado (`bdiCustomizado: Boolean`) para não perder rastreabilidade.
- No PDF do orçamento exportado: por padrão mostra só o **preço final por item** (comum no mercado não detalhar BDI ao cliente) — com opção "Exibir detalhamento de BDI" para propostas onde o cliente exige transparência (comum em obra pública/licitação).

---

## 6. Critérios de Aceite

- [ ] BDI calculado bate exatamente com a fórmula gross-up da seção 2.1 (validado por teste unitário com o exemplo da seção 2.2)
- [ ] Alterar a `ConfigBDI` padrão não altera orçamentos já enviados/aprovados anteriormente
- [ ] Combinação de taxas que resulte em divisão inválida (tributos ≥ 100%) é bloqueada na UI com mensagem clara
- [ ] É possível ter múltiplos perfis de BDI e escolher por orçamento
- [ ] PDF do orçamento permite alternar entre "preço final" e "detalhamento com BDI"
