# SPEC — ObraMaster (App de Gestão de Obras)

> **Documento de especificação para geração de código (Antigravity)**
> Colocar em: `docs/SPEC_OBRA_MASTER.md`
> Plataforma: **Android nativo — Kotlin + Jetpack Compose**
> Arquitetura: **MVVM + Clean Architecture + Engines isoladas (padrão Strategy/Engine)**

---

## 1. Visão Geral

Aplicativo mobile para **gestão completa de execução de obras**, do projeto ao acabamento, com:

- Controle financeiro completo (lucro, despesas, filtros por data)
- Acompanhamento por **etapas da construção** (cadastráveis)
- Gerenciamento de **equipes de mão de obra com pagamentos**
- **Controle de acesso por colaborador** (login com senha, permissões geridas pelo Gestor)
- Sistema **100% modular** — cada módulo com flag de **ligar/desligar**
- Orçamento por projeto com **abatimento progressivo** e divisão por etapas
- Metas por projeto e por setor
- Custo por **m² (área construída ou área do terreno)**
- Exportação universal: **XLS, PDF e JPG** em todos os setores
- Calculadoras integradas (científica, trigonométrica, engenharia, volumes/áreas/perímetros)
- **Mini-calculadora embutida em todo campo de valor numérico**
- Acessibilidade completa (temas, fontes, tamanhos de letra)

---

## 2. Stack Técnica

| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navegação | Navigation Compose (single-activity) |
| Estado | ViewModel + StateFlow |
| Persistência local | Room (SQLite) — offline-first |
| Injeção | Hilt |
| Exportação PDF | `android.graphics.pdf.PdfDocument` |
| Exportação XLS | Apache POI (ou `fastexcel` para reduzir peso do APK) |
| Exportação JPG | Renderização de Composable → Bitmap → JPG |
| Contatos | `ContactsContract` (import direto da agenda, com permissão `READ_CONTACTS`) |
| Segurança | Hash de senha com BCrypt + `EncryptedSharedPreferences` para sessão |
| Datas | `java.time` (LocalDate/LocalDateTime) |

**Regra geral:** nenhuma dependência de backend na versão base. Tudo local (Room). Estruturar repositórios com interfaces para permitir sync futuro (Firebase/API) sem refatorar.

---

## 3. Arquitetura Modular (núcleo do sistema)

### 3.1 ModuleRegistry (flag central de liga/desliga)

```kotlin
enum class AppModule(val id: String, val labelPtBr: String) {
    PLANEJAMENTO("planejamento", "Planejamento"),
    EXECUCAO("execucao", "Execução"),
    COMPRAS("compras", "Compras"),
    VENDAS("vendas", "Vendas"),
    ORCAMENTOS("orcamentos", "Orçamentos (Material + Mão de Obra)"),
    FINANCEIRO("financeiro", "Financeiro"),
    EQUIPES("equipes", "Equipes e Pagamentos"),
    PROJETOS("projetos", "Projetos e Etapas"),
    PESSOAS("pessoas", "Cadastro de Pessoas"),
    CALCULADORAS("calculadoras", "Calculadoras"),
    METAS("metas", "Metas"),
    CADASTROS_BASE("cadastros_base", "Cadastros Básicos (Cores, Materiais...)"),
    RELATORIOS("relatorios", "Relatórios e Exportação")
}
```

- Tabela `module_config(moduleId, enabled: Boolean)` no Room.
- `ModuleRegistry` é um singleton (Hilt) que expõe `StateFlow<Map<AppModule, Boolean>>`.
- O menu principal (drawer/grid) **renderiza apenas módulos ativos**.
- Rotas de módulos desativados redirecionam para tela "Módulo desativado — contate o Gestor".
- Somente o **Gestor** liga/desliga módulos (tela de Configurações).

### 3.2 Controle de Acesso (RBAC — Role Based Access Control)

**Entidades:**

```
Colaborador(id, nome, login, senhaHash, ativo, ehGestor: Boolean, fotoUri?)
Permissao(colaboradorId, moduleId, nivel: NENHUM | LEITURA | ESCRITA | TOTAL)
```

**Regras:**
- Primeiro uso do app: wizard cria o **Gestor** (acesso total, irrevogável, não pode ser excluído nem rebaixado pelo próprio fluxo comum).
- Login: tela com usuário + senha (BCrypt). Sessão persistida em `EncryptedSharedPreferences` com opção "manter conectado".
- Gestor gerencia colaboradores: CRUD completo + matriz de permissões (grid módulo × nível).
- `PermissionEngine` (função pura, testável):

```kotlin
object PermissionEngine {
    fun canView(user: Colaborador, perms: List<Permissao>, module: AppModule): Boolean
    fun canEdit(user: Colaborador, perms: List<Permissao>, module: AppModule): Boolean
}
```

- Toda tela consulta `PermissionEngine` no ViewModel; botões de criar/editar/excluir são ocultados sem permissão de ESCRITA.
- Log de auditoria simples: `AuditLog(id, colaboradorId, acao, entidade, timestamp)` — visível só para o Gestor.

---

## 4. Módulos Funcionais

### 4.1 Projetos e Etapas (módulo central)

```
Projeto(id, nome, cliente(pessoaId?), endereco, areaConstruidaM2, areaTerrenoM2,
        orcamentoTotal, dataInicio, dataPrevisaoFim, status: PLANEJAMENTO|EM_EXECUCAO|PAUSADO|CONCLUIDO,
        fotoCapaUri?)

Etapa(id, projetoId, nome, ordem, orcamentoEtapa, dataInicio?, dataFim?,
      progressoPercent: Int, status)
```

- Etapas **cadastráveis livremente** (ex.: Fundação, Alvenaria, Cobertura, Elétrica, Hidráulica, Acabamento) + opção de criar a partir de **template padrão** editável nas Configurações.
- CRUD completo de projetos e etapas, com reordenação drag-and-drop das etapas.
- Tela de detalhe do projeto: cards com progresso geral, orçamento vs gasto, custo/m².

**BudgetEngine (função pura):**

```kotlin
object BudgetEngine {
    fun saldoProjeto(orcamentoTotal: BigDecimal, gastos: List<Gasto>): BigDecimal
    fun saldoEtapa(etapa: Etapa, gastos: List<Gasto>): BigDecimal
    fun custoPorM2Construcao(gastoTotal: BigDecimal, areaConstruidaM2: BigDecimal): BigDecimal
    fun custoPorM2Terreno(gastoTotal: BigDecimal, areaTerrenoM2: BigDecimal): BigDecimal
    fun percentualConsumido(orcamento: BigDecimal, gasto: BigDecimal): Double
}
```

- Todo gasto lançado (compras, mão de obra, despesas avulsas) referencia `projetoId` e opcionalmente `etapaId` → abatimento automático do orçamento do projeto **e** da etapa.
- Alertas visuais: barra de progresso verde/amarela/vermelha (80% / 100% do orçamento).
- Tela "Gastos por Etapa": lista com valor gasto, orçado, saldo e % por etapa.

### 4.2 Financeiro (completo)

```
LancamentoFinanceiro(id, tipo: RECEITA|DESPESA, categoriaId, projetoId?, etapaId?,
                     descricao, valor, data, formaPagamento, pago: Boolean,
                     pessoaId?, anexoUri?)
CategoriaFinanceira(id, nome, tipo, cor)
```

- Dashboard: receitas, despesas, **lucro (receitas − despesas)**, com **filtros por período** (hoje, semana, mês, ano, intervalo personalizado) e por projeto.
- Gráficos: pizza por categoria, barras por mês, linha de evolução do lucro.
- Contas a pagar / a receber com status e data de vencimento.
- `FinanceEngine` isolada como função pura (mesmo padrão do seu app de eventos): recebe listas de lançamentos + filtros, retorna totais e agrupamentos.
- Exportação de qualquer visão filtrada em XLS/PDF/JPG.

### 4.3 Equipes e Pagamentos

```
Funcionario(pessoaId, funcao, tipoContratacao: DIARIA|EMPREITADA|MENSAL, valorBase)
Equipe(id, nome, liderPessoaId?)
EquipeMembro(equipeId, pessoaId)
RegistroTrabalho(id, pessoaId, projetoId, etapaId?, data, tipo: DIARIA|EMPREITADA_PARCELA|HORA_EXTRA, valor, observacao)
Pagamento(id, pessoaId, projetoId?, periodo, valorTotal, dataPagamento, status: PENDENTE|PAGO, comprovanteUri?)
```

- Fluxo: registrar diárias/empreitadas → app acumula → gerar pagamento do período → marcar como pago → gera automaticamente `LancamentoFinanceiro` (DESPESA, categoria "Mão de Obra") vinculado ao projeto/etapa.
- Relatório por funcionário e por equipe: dias trabalhados, total a pagar, total pago.
- Recibo de pagamento exportável em PDF/JPG.

### 4.4 Compras

```
Fornecedor(pessoaId, cnpjCpf?, observacoes)
PedidoCompra(id, projetoId, etapaId?, fornecedorId?, data, status: COTACAO|APROVADO|COMPRADO|ENTREGUE, valorTotal)
ItemCompra(id, pedidoId, materialId, quantidade, unidade, valorUnitario, valorTotal)
```

- Lista de compras por projeto/etapa, com status.
- Ao marcar como COMPRADO → gera `LancamentoFinanceiro` (DESPESA, categoria "Materiais") automaticamente.
- Comparativo simples de cotações (mesmo item, fornecedores diferentes).

### 4.5 Orçamentos (Matéria-Prima + Mão de Obra)

```
Orcamento(id, projetoId?, clientePessoaId?, titulo, data, validadeDias,
          status: RASCUNHO|ENVIADO|APROVADO|RECUSADO, descontoPercent?, observacoes)
ItemOrcamento(id, orcamentoId, tipo: MATERIAL|MAO_DE_OBRA, descricao, materialId?,
              quantidade, unidade, valorUnitario, valorTotal)
```

- Composição livre de itens de material e mão de obra, subtotais separados por tipo, desconto, total geral.
- Orçamento aprovado → botão "Converter em Projeto" (cria projeto com orçamentoTotal = valor aprovado).
- Exportação PDF com layout profissional (logo, dados da empresa das Configurações).

### 4.6 Vendas

```
Venda(id, projetoId?, clientePessoaId, descricao, valorTotal, data,
      formaPagamento, parcelas?, status: NEGOCIACAO|FECHADA|CANCELADA)
ParcelaVenda(id, vendaId, numero, valor, vencimento, pago: Boolean)
```

- Venda fechada → gera `LancamentoFinanceiro` (RECEITA) por parcela.
- Funil simples: Negociação → Fechada.

### 4.7 Planejamento

- Cronograma por projeto: lista de etapas com datas previstas vs reais.
- Visão Gantt simplificada (barras horizontais por etapa, Compose Canvas).
- Checklist de tarefas por etapa: `Tarefa(id, etapaId, descricao, responsavelPessoaId?, prazo?, concluida)`.
- Indicador de atraso (data prevista < hoje e não concluída → vermelho).

### 4.8 Execução

- Diário de obra: `DiarioObra(id, projetoId, etapaId?, data, texto, clima?, fotosUris[])`.
- Registro fotográfico com data/etapa (fotos comprimidas, salvas no storage interno do app).
- Atualização de progresso (%) por etapa com slider.
- Exportação do diário em PDF com fotos.

### 4.9 Metas

```
Meta(id, escopo: GERAL|PROJETO|SETOR, referenciaId?, titulo,
     tipo: FINANCEIRA|PRAZO|PROGRESSO, valorAlvo, valorAtual(calculado), prazo?, concluida)
```

- Metas financeiras (ex.: lucro X no mês), de prazo (concluir etapa até data) e de progresso.
- `MetaEngine`: calcula `valorAtual` a partir dos dados dos outros módulos (função pura, sem side-effects).
- Dashboard de metas com progresso visual.

### 4.10 Pessoas (cadastro unificado)

```
Pessoa(id, nome, tipoTags: [CLIENTE, FUNCIONARIO, FORNECEDOR], telefone, email?,
       endereco?, documento?, fotoUri?, observacoes)
```

- **Importação direta da agenda do celular** (`ContactsContract`): tela de seleção múltipla → importa nome/telefone/email/foto.
- Uma pessoa pode ter múltiplas tags (ex.: cliente e fornecedor).
- CRUD completo; excluir pessoa vinculada a registros → soft-delete (flag `ativo`).

### 4.11 Cadastros Básicos

- **Cores**: `Cor(id, nome, hex, codigoFabricante?)` com preview visual.
- **Materiais**: `Material(id, nome, unidadePadrao, precoReferencia?, categoria, cor?)`.
- **Unidades de medida**: cadastráveis (m, m², m³, kg, sc, un, lata, etc.).
- **Categorias financeiras**, **Funções de mão de obra**, **Formas de pagamento**, **Template de etapas**.
- Todos com LCRUD completo (listar, criar, ler, atualizar, deletar) + busca.

### 4.12 Calculadoras

Módulo `CALCULADORAS` com hub de acesso:

1. **Científica**: operações básicas, potência, raiz, log, ln, exp, fatorial, %, memória (M+, M−, MR, MC), histórico.
2. **Trigonométrica completa**: sin/cos/tan e inversas, graus/radianos, resolução de triângulos (dado 2 lados + ângulo, etc.), teorema de Pitágoras, lei dos senos/cossenos — com **desenho esquemático do triângulo** (Canvas).
3. **Áreas**: quadrado, retângulo, triângulo, trapézio, círculo, polígono regular, área irregular por coordenadas (Shoelace).
4. **Perímetros**: mesmas figuras.
5. **Volumes**: paralelepípedo, cilindro, esfera, cone, prisma, tronco de pirâmide.
6. **Engenharia (construção civil)**:
   - Concreto: volume + traço (cimento/areia/brita/água) por m³
   - Argamassa de assentamento e reboco (por m²)
   - Tijolos/blocos por m² de parede (com tipos cadastráveis)
   - Piso/revestimento: m² + perda % → caixas necessárias
   - Tinta: m² por demão → litros/latas
   - Telhado: área inclinada a partir de área plana + inclinação %
   - Escada: cálculo de degraus (fórmula de Blondel 63 ≤ 2h + p ≤ 65)
   - Ferragem: kg de aço estimado por m³ de concreto (taxas configuráveis)

- Cada resultado tem botão **"Usar valor"** → devolve o número ao campo de origem (ver 5.2) e botão **exportar/compartilhar** (PDF/JPG do cálculo).
- Todas as calculadoras implementadas como **objetos puros** em `core/calc/` (100% testáveis por unit test), com UI separada.

---

## 5. Componentes Transversais

### 5.1 ExportEngine (XLS / PDF / JPG em todos os setores)

```kotlin
interface ExportStrategy { fun export(doc: ExportableDocument, out: File) }
class XlsExportStrategy : ExportStrategy
class PdfExportStrategy : ExportStrategy
class JpgExportStrategy : ExportStrategy   // renderiza Composable off-screen → Bitmap
```

- Toda tela de listagem/relatório monta um `ExportableDocument(titulo, colunas, linhas, resumo?, logo?)`.
- Botão de exportar padrão (ícone compartilhar) no TopAppBar de todas as listas → bottom sheet com as 3 opções → gera arquivo em `cacheDir` → `FileProvider` + Intent de compartilhamento.

### 5.2 Campo de valor com calculadora embutida (componente global)

- Composable `CalculatorTextField`: substitui todo campo numérico/monetário do app.
- Ícone de calculadora no trailing → abre **bottom sheet** com calculadora básica (+ − × ÷ % parênteses).
- Botão "OK" insere o resultado no campo.
- Suporta formatação monetária BR (R$ 1.234,56) e decimal simples.
- **Obrigatório usar este componente em 100% dos campos de valor do app.**

### 5.3 Padrão de telas (LCRUD)

Todo cadastro segue o mesmo template:
- **Lista**: busca no topo, filtros, FAB "+", swipe/menu para editar/excluir, botão exportar.
- **Formulário**: validação inline, `CalculatorTextField` nos valores, botão salvar fixo no rodapé.
- **Detalhe**: visualização + ações.
- Exclusão sempre com diálogo de confirmação; entidades referenciadas usam soft-delete.

---

## 6. Configurações e Acessibilidade

### 6.1 Configurações (só Gestor, exceto acessibilidade)

- Dados da empresa (nome, logo, CNPJ, endereço, telefone) — usados nos PDFs.
- Liga/desliga de módulos (ModuleRegistry).
- Gestão de colaboradores e permissões.
- Templates de etapas, categorias, unidades.
- Backup/restauração local: exportar/importar banco (arquivo `.db` ou JSON zipado) via SAF.
- Log de auditoria.

### 6.2 Acessibilidade (todo usuário)

```
PrefsAcessibilidade(tema: CLARO|ESCURO|SISTEMA|ALTO_CONTRASTE,
                    fonte: PADRAO|SERIFADA|LEGIVEL(OpenDyslexic-like),
                    escalaFonte: 0.85f..1.4f,
                    espacamentoAumentado: Boolean)
```

- Aplicado via `CompositionLocal` no tema raiz — afeta o app inteiro imediatamente.
- Tipografia base **legível e compacta** (bodyMedium ~14sp, titleMedium ~16sp) — letras claras sem exagero de tamanho, conforme requisito.
- Suporte a TalkBack: `contentDescription` em todos os ícones/ações.

---

## 7. Navegação

- **Login** → **Home** (grid de cards dos módulos ativos + resumo: saldo do mês, projetos em execução, alertas de orçamento).
- Bottom bar fixa: Home | Projetos | Financeiro | Mais.
- "Mais" abre grid com os demais módulos ativos (respeitando permissões).
- Navigation Compose com rotas tipadas; deep-link interno entre módulos (ex.: do gasto → projeto → etapa).

---

## 8. Estrutura de Pacotes

```
br.com.tiago.obramaster/
├── core/
│   ├── auth/            (PermissionEngine, sessão)
│   ├── modules/         (ModuleRegistry)
│   ├── calc/            (todas as engines de cálculo puras)
│   ├── export/          (ExportEngine + strategies)
│   ├── ui/              (tema, CalculatorTextField, componentes LCRUD)
│   └── util/            (formatação BR, datas)
├── data/
│   ├── db/              (Room: entities, DAOs, database, migrations)
│   └── repository/      (interfaces + impl local)
├── domain/              (modelos, BudgetEngine, FinanceEngine, MetaEngine)
└── features/
    ├── login/  ├── home/  ├── projetos/  ├── financeiro/
    ├── equipes/ ├── compras/ ├── orcamentos/ ├── vendas/
    ├── planejamento/ ├── execucao/ ├── metas/ ├── pessoas/
    ├── cadastros/ ├── calculadoras/ └── configuracoes/
```

---

## 9. Regras de Negócio Críticas (resumo para o gerador)

1. **Nada renderiza sem checar** `ModuleRegistry` (módulo ativo?) **e** `PermissionEngine` (usuário pode?).
2. Todo gasto de Compras e Pagamento de mão de obra **gera lançamento financeiro automático** vinculado a projeto/etapa.
3. Orçamento do projeto = teto; saldo = orçamento − soma dos gastos vinculados; idem por etapa.
4. Custo/m² sempre disponível nas duas bases: área construída e área do terreno.
5. Valores monetários em `BigDecimal`/`Long` (centavos) — **nunca Float/Double** para dinheiro.
6. Todas as engines (`BudgetEngine`, `FinanceEngine`, `MetaEngine`, calculadoras) são **funções puras sem dependência de Android** — cobertas por unit tests.
7. Exportação XLS/PDF/JPG disponível em toda listagem e relatório.
8. `CalculatorTextField` obrigatório em todos os campos de valor.
9. Offline-first: zero dependência de rede na versão base.

---

## 10. Fases de Implementação (para o Antigravity gerar em ordem)

| Fase | Entrega |
|---|---|
| **1** | Projeto base, tema + acessibilidade, Room, Login/Gestor, ModuleRegistry, PermissionEngine, Home |
| **2** | Pessoas (com import da agenda), Cadastros Básicos, componente LCRUD, CalculatorTextField |
| **3** | Projetos + Etapas + BudgetEngine + custo/m² |
| **4** | Financeiro completo (FinanceEngine, dashboard, filtros, gráficos) |
| **5** | Equipes + Pagamentos (integração com Financeiro) |
| **6** | Compras + Orçamentos + Vendas (integrações com Financeiro/Projetos) |
| **7** | Planejamento + Execução (diário de obra, Gantt) |
| **8** | Calculadoras (científica, trig, áreas, volumes, engenharia) |
| **9** | Metas + ExportEngine (XLS/PDF/JPG) em todos os setores |
| **10** | Configurações finais, backup, auditoria, polimento |

---

## 11. Critérios de Aceite

- [ ] Gestor criado no primeiro uso; login funcional com senha hasheada
- [ ] Colaborador sem permissão não vê botões de edição nem acessa módulo negado
- [ ] Módulo desligado some do menu e bloqueia rota
- [ ] Gasto lançado abate orçamento do projeto e da etapa em tempo real
- [ ] Custo/m² calculado pelas duas áreas
- [ ] Filtros de data funcionam em todo o Financeiro
- [ ] Exportar qualquer lista em XLS, PDF e JPG e compartilhar
- [ ] Import de contato da agenda cria Pessoa completa
- [ ] Calculadora embutida presente em todos os campos de valor
- [ ] Tema/fonte/escala mudam o app inteiro instantaneamente
- [ ] Todas as engines com unit tests passando
