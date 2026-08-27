# Manual do ObraMaster

> Guia de uso para quem vai operar o sistema no dia a dia.
> Colocar em: `docs/MANUAL_DO_PROGRAMA.md`
> As imagens abaixo são **wireframes ilustrativos** (o app real terá o mesmo layout, com o visual final do tema aplicado). Ficam em `docs/mockups/`.
> Este manual é a **fonte de conteúdo do Assistente de IA** (ver `SPEC_ASSISTENTE_IA.md`) — cada seção tem um identificador (`#id`) usado pela IA para apontar "onde está no manual".

---

## Sumário

1. Primeiro Acesso e Login
2. Tela Inicial (Home)
3. Módulos — Ligar e Desligar
4. Projetos e Etapas
5. Financeiro (Contas, Categorias, Centro de Custo)
6. Compras
7. Orçamentos (com BDI)
8. Vendas
9. Equipes e Pagamentos
10. Planejamento e Execução
11. Metas
12. Pessoas
13. Cadastros Básicos
14. Calculadoras
15. Exportação (PDF, XLSX, JPG)
16. Configurações e Permissões
17. Acessibilidade
18. Assistente de IA

---

## 1. Primeiro Acesso e Login `#login`

Ao instalar o app pela primeira vez, um assistente cria o **Gestor** — o usuário com acesso total ao sistema. Esse cadastro pede nome, login e senha, e não pode ser excluído depois.

Nos acessos seguintes, a tela de login pede usuário e senha. Se o Gestor cadastrar outros colaboradores (seção 16), cada um entra com seu próprio login e só vê os módulos que tiver permissão.

**Exemplo prático:** você contrata um administrativo para lançar notas de compra. O Gestor cadastra esse colaborador com acesso de **escrita** só no módulo Compras e **leitura** no Financeiro — ele lança as notas, mas não vê o lucro da empresa.

---

## 2. Tela Inicial (Home) `#home`

![Home](mockups/01_home.svg)

Assim que você loga, a Home mostra:

- **Saldo consolidado** de todas as contas ativas
- **Lucro do mês** (receitas menos despesas)
- **Alertas de orçamento** — projetos que já passaram de 80% do valor orçado aparecem em destaque amarelo
- **Projetos em execução**, com barra de progresso do orçamento de cada um
- **Grade de módulos** ativos, para acesso rápido
- O botão azul flutuante (canto inferior direito) abre o **Assistente de IA** de qualquer tela do app

**Exemplo prático:** ao abrir o app de manhã, você vê de cara que a obra "Residencial Silva" está em 82% do orçamento na etapa de Acabamento — isso já avisa que é hora de revisar os próximos gastos dessa etapa antes de continuar comprando.

---

## 3. Módulos — Ligar e Desligar `#modulos`

O sistema é modular: cada setor (Compras, Vendas, Planejamento, etc.) pode ser **ativado ou desativado** pelo Gestor em Configurações → Módulos (seção 16.2). Módulo desativado some do menu de todos os colaboradores.

**Exemplo prático:** se sua empresa ainda não trabalha com vendas formais (só executa obra para terceiros), pode desligar o módulo Vendas até precisar dele — deixa o app mais limpo para o dia a dia.

---

## 4. Projetos e Etapas `#projetos`

![Projeto Detalhe](mockups/02_projeto_detalhe.svg)

Cada obra é cadastrada como um **Projeto**, com endereço, área construída, área do terreno e um **orçamento total**. Dentro do projeto, você cadastra as **etapas** (Fundação, Estrutura, Alvenaria, Acabamento, etc.), cada uma com seu próprio orçamento.

- Todo gasto lançado abate automaticamente o saldo do **projeto** e da **etapa** escolhida
- A barra de progresso de cada etapa muda de cor: verde (tranquilo), amarelo (acima de 80%), vermelho (estourou)
- O sistema calcula sozinho o **custo por m²**, tanto pela área construída quanto pela área do terreno
- Botão "Diário de Obra" registra fotos e observações datadas por etapa

**Exemplo prático (o mesmo da tela acima):** a etapa Acabamento está orçada em R$ 110.000, já gastou R$ 67.600 — o app mostra o saldo de R$ 42.400 automaticamente, sem você precisar somar nada na mão.

---

## 5. Financeiro `#financeiro`

![Financeiro](mockups/03_financeiro.svg)

O painel financeiro mostra receitas, despesas e lucro, com **filtros por período** (mês, ano, intervalo personalizado) e por projeto.

### 5.1 Contas `#financeiro-contas`
Cadastre suas contas (caixa da obra, conta corrente, cartão) em Configurações → Contas. Todo pagamento ou recebimento é debitado/creditado de uma conta específica, e você sempre sabe o saldo real de cada uma.

### 5.2 Transferência entre contas `#financeiro-transferencia`
Botão "Transferir" move dinheiro entre duas contas suas (ex.: do caixa da obra para a conta corrente) — isso **não** entra como receita nem despesa, é só movimentação de patrimônio.

### 5.3 Centro de Custo `#financeiro-centro-custo`
Além de projeto/etapa, despesas administrativas (aluguel do escritório, combustível da frota) podem ser lançadas num Centro de Custo próprio, sem misturar com o custo de uma obra específica.

### 5.4 Contábil x Não Contábil `#financeiro-natureza`
Cada lançamento é marcado como **Contábil** (formal, com nota, vai para o contador) ou **Não Contábil** (gerencial, ex.: um vale interno). Relatórios têm filtro para mostrar só um tipo, os dois, ou gerar o fechamento oficial (DRE) considerando só os contábeis.

**Exemplo prático:** você paga um adiantamento em dinheiro a um funcionário, sem nota — lança como Não Contábil. Isso aparece no seu controle gerencial, mas não entra no relatório que vai para o contador.

---

## 6. Compras `#compras`

Cadastre pedidos de compra vinculados a um projeto/etapa e um fornecedor. Ao marcar um pedido como "Comprado", o sistema já gera automaticamente a despesa correspondente no Financeiro — você não lança duas vezes.

**Exemplo prático:** você fecha compra de cimento com o fornecedor X. Ao marcar "Comprado", o gasto já aparece na etapa "Estrutura" do projeto, sem retrabalho.

---

## 7. Orçamentos (com BDI) `#orcamentos`

Monte um orçamento somando itens de material e mão de obra. O sistema soma o **custo direto** e aplica o **BDI** (percentual que cobre administração, impostos, risco e lucro) para chegar no **preço de venda** que você vai propor ao cliente.

Você pode ter perfis diferentes de BDI cadastrados (ex.: um para obra particular, outro para obra pública) e escolher qual usar em cada orçamento.

**Exemplo prático:** um orçamento com R$ 100.000 de custo direto e um perfil de BDI de 24,5% mostra automaticamente o preço final de R$ 124.520 para o cliente — sem você calcular na calculadora do celular.

---

## 8. Vendas `#vendas`

Registre a venda fechada com o cliente, parcelada ou à vista. Cada parcela recebida gera o recebimento no Financeiro, creditando a conta escolhida.

Se o projeto usa **medição por etapa** (comum em obra formal), cada medição aprovada já gera a cobrança correspondente.

---

## 9. Equipes e Pagamentos `#equipes`

Cadastre funcionários (diária, empreitada ou mensal) e organize em equipes. Registre o trabalho de cada um por dia/projeto/etapa, e ao final do período, gere o pagamento — isso já cria a despesa de mão de obra no Financeiro e debita a conta escolhida.

**Exemplo prático:** um pedreiro trabalhou 12 diárias na etapa de Acabamento. No fim do mês, você gera o pagamento dele — o app soma tudo, aplica retenção se houver, e já debita do caixa da obra.

---

## 10. Planejamento e Execução `#planejamento-execucao`

Cronograma por etapa com data prevista x realizada, checklist de tarefas, e o Diário de Obra (fotos + observações). Atrasos aparecem destacados em vermelho.

---

## 11. Metas `#metas`

Cadastre metas financeiras (lucro do mês), de prazo (terminar etapa até uma data) ou de progresso. O app calcula sozinho quanto falta, com base nos dados que já existem nos outros módulos.

---

## 12. Pessoas `#pessoas`

Cadastro único de clientes, fornecedores e funcionários — uma pessoa pode ter mais de uma dessas etiquetas. Você pode **importar direto da agenda do celular**, escolhendo os contatos que quer trazer para o sistema.

---

## 13. Cadastros Básicos `#cadastros-basicos`

Cores, materiais, unidades de medida, categorias e templates de etapas — tudo o que os outros módulos usam como listas de apoio. Fica em Configurações, com CRUD completo (criar, ver, editar, excluir).

---

## 14. Calculadoras `#calculadoras`

![Calculadora no campo](mockups/04_calculadora_campo.svg)

Todo campo de valor no app tem um ícone de calculadora ao lado — toque nele para abrir uma calculadora completa sem sair da tela, e o resultado volta automaticamente para o campo.

Também há um hub de calculadoras dedicado: científica, trigonométrica, áreas, perímetros, volumes e as de **engenharia civil** (traço de concreto, argamassa, tijolos por m², tinta, telhado, escada, ferragem).

**Exemplo prático (mostrado na imagem acima):** ao lançar um gasto de material, você não precisa saber o valor de cabeça — toque no ícone da calculadora, faça a conta (ex.: 45 × 72) e o resultado já preenche o campo de valor.

---

## 15. Exportação `#exportacao`

Qualquer lista ou relatório do sistema pode ser exportado em **PDF**, **Excel (XLSX)** ou **imagem (JPG)**, pelo botão de compartilhar no topo da tela. Útil para mandar um relatório pro cliente, pro contador ou guardar um comprovante.

---

## 16. Configurações e Permissões `#configuracoes`

![Configurações](mockups/05_configuracoes_modulos.svg)

### 16.1 Colaboradores e Permissões `#configuracoes-permissoes`
O Gestor cadastra cada colaborador e define, módulo por módulo, se ele pode: não ver, só ver, ou ver e editar.

### 16.2 Módulos `#configuracoes-modulos`
Liga/desliga cada setor do sistema (ver seção 3).

### 16.3 Categorias e BDI `#configuracoes-categorias-bdi`
Cadastro de categorias financeiras (com hierarquia) e perfis de BDI.

**Exemplo prático (mostrado na imagem acima):** o módulo "Vendas" está desligado nesta empresa porque ela só executa obra para terceiros — o Gestor desativou e ele some do menu de todo mundo.

---

## 17. Acessibilidade `#acessibilidade`

Qualquer usuário pode ajustar, no seu próprio perfil: tema (claro, escuro, alto contraste), tipo de fonte e tamanho da letra. A mudança se aplica no app inteiro na hora.

---

## 18. Assistente de IA `#assistente-ia`

![Assistente IA](mockups/06_assistente_ia.svg)

Toque no botão azul flutuante (ícone "?") em qualquer tela para abrir o Assistente. Ele já sabe **em qual tela e projeto você está** e responde à sua pergunta com:

1. A explicação em português simples
2. Um link **"Ver no Manual"** apontando pra seção exata deste documento
3. Um **exemplo prático com os seus dados atuais** (ex.: "com o saldo atual da sua etapa X, se você lançar Y...")
4. Quando aplicável, um botão de atalho para já abrir a tela da ação sugerida

Ver detalhes técnicos de como isso funciona em `SPEC_ASSISTENTE_IA.md`.

**Exemplo prático (mostrado na imagem acima):** você pergunta "como faço para lançar um gasto só nesta etapa?" estando na tela do projeto Residencial Silva — o Assistente responde com o passo a passo, aponta a seção 4 deste manual, e mostra o que aconteceria com o saldo da etapa Acabamento se você lançasse R$ 5.000 agora.
