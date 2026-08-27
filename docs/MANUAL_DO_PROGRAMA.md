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

![Onboarding wizard](mockups/07_onboarding_wizard.svg)

No primeiro uso, um assistente de configuração inicial (onboarding) pede os dados da sua empresa, cria seu acesso de Gestor e já deixa módulos e ao menos uma conta financeira prontos para uso — em 12 etapas curtas, só quatro delas obrigatórias (Empresa, Gestor, Módulos, Contas Financeiras). Tudo o mais (categorias, perfil de BDI, template de etapas, colaboradores extras, primeiro projeto, acessibilidade) pode ser pulado e preenchido depois em Configurações — o app nunca trava por falta de um dado secundário.

Fechar o app no meio do onboarding e reabrir retoma exatamente na etapa em que parou (a senha digitada é a única coisa que precisa ser redigitada, por segurança — ela não fica salva em rascunho). **Nada é gravado no banco até você confirmar na tela final de Resumo.**

*(O modo "Configurar com ajuda da IA" aparece na tela de boas-vindas, mas ainda mostra aviso de indisponível — depende do backend, que chega na Fase 10. Por enquanto, o formulário tradicional é o único caminho.)*

Nos acessos seguintes (depois que já existe um Gestor), a tela de login pede usuário e senha. Se o Gestor cadastrar outros colaboradores (seção 16), cada um entra com seu próprio login e só vê os módulos que tiver permissão.

**Exemplo prático:** você contrata um administrativo para lançar notas de compra. O Gestor cadastra esse colaborador com acesso de **escrita** só no módulo Compras e **leitura** no Financeiro — ele lança as notas, mas não vê o lucro da empresa.

---

## 2. Tela Inicial (Home) `#home`

![Home](mockups/01_home.svg)

Assim que você loga, a Home mostra a **grade de módulos** ativos e permitidos para o seu usuário, adaptada ao tamanho da tela (barra inferior no celular, menu lateral recolhido em tablet, menu lateral fixo em desktop/web) — e um atalho para Configurações e para sair.

As peças abaixo chegam nas próximas fases, quando os módulos que elas dependem existirem:
- **Saldo consolidado** e **lucro do mês** — dependem do módulo Financeiro (seção 5)
- **Alertas de orçamento** e **projetos em execução** — dependem do módulo Projetos (seção 4)
- O botão do **Assistente de IA** — chega na Fase 11 (seção 18)

**Exemplo prático (visão final, quando os módulos acima existirem):** ao abrir o app de manhã, você vai ver de cara que a obra "Residencial Silva" está em 82% do orçamento na etapa de Acabamento — isso já avisa que é hora de revisar os próximos gastos dessa etapa antes de continuar comprando.

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

Cadastro único de clientes, fornecedores e funcionários — uma pessoa pode ter mais de uma dessas etiquetas (marque quantas quiser: nome, telefone, e-mail, endereço, documento e observações são todos opcionais, menos o nome e pelo menos uma etiqueta).

**Importar contatos** (ícone de "+pessoa" no topo da lista): em Android e Web já funciona — Android busca direto da agenda do celular (pede permissão na primeira vez); Web não tem acesso à agenda do navegador, então aceita colar um arquivo **CSV** (colunas `nome,telefone,email`) ou **vCard** (`.vcf`) exportado de outro app. Depois de buscar ou colar, você escolhe quais contatos importar e com qual etiqueta (cliente/funcionário/fornecedor) eles entram. *(No iOS a importação direta da agenda ainda não está pronta — pendente de desenvolvimento num Mac; por enquanto o cadastro manual funciona normalmente lá.)*

**Exemplo prático:** você tem 40 contatos de fornecedores salvos no celular. Em vez de digitar um por um, toca em importar, seleciona os 40, marca a etiqueta "Fornecedor" e confirma — todos entram de uma vez, prontos para vincular em Compras.

---

## 13. Cadastros Básicos `#cadastros-basicos`

Acessível pela Home (módulo "Cadastros Básicos"). Hoje reúne os cadastros que não dependem de nenhum outro módulo:

- **Cores**: nome, código hexadecimal (com preview visual) e código do fabricante (opcional).
- **Materiais**: nome, unidade padrão (ex.: m², kg, sc), preço de referência (opcional, com a calculadora embutida) e cor (opcional, escolhida entre as já cadastradas).
- **Unidades de Medida**: sigla e nome (ex.: "m²" / "Metro quadrado").

Todos com busca, criar/editar/excluir (exclusão sempre pede confirmação e não apaga de verdade — só marca como inativo, então nada que já foi usado em outro lugar quebra).

*Categorias financeiras, formas de pagamento, funções de mão de obra e templates de etapas também são "cadastros básicos" na visão geral do sistema, mas cada um entra junto do módulo que efetivamente usa ele (Financeiro, Equipes, Projetos) — não faria sentido cadastrá-los soltos antes de existir onde aplicar.*

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
Cadastro de categorias financeiras (com hierarquia) e perfis de BDI. *(Chega nas Fases 2 e 6, junto dos módulos Cadastros Básicos e Orçamentos — ainda não disponível.)*

**Exemplo prático (mostrado na imagem acima):** o módulo "Vendas" está desligado nesta empresa porque ela só executa obra para terceiros — o Gestor desativou e ele some do menu de todo mundo.

---

## 17. Acessibilidade `#acessibilidade`

Qualquer usuário pode ajustar, em Configurações → Acessibilidade: tema (claro, escuro, conforme o sistema, ou alto contraste), tipo de fonte (padrão, serifada ou legível), tamanho da letra (85% a 140%) e espaçamento aumentado entre linhas/letras. A mudança se aplica no app inteiro na hora, e fica salva no aparelho.

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
