# Checklist de Testes — ObraMaster

> Colocar em: `docs/CHECKLIST_TESTES.md`
> Roteiro prático de validação manual, fase por fase. Use depois que o Antigravity entregar cada fase, **antes** de pedir a próxima. Marque os itens conforme testar.

---

## Como usar este checklist

1. Termine uma fase no Antigravity (veja `GUIA_ANTIGRAVITY.md`).
2. Rode o app (Android é o ciclo mais rápido para o dia a dia).
3. Percorra os itens da fase correspondente aqui.
4. Se algo falhar, volte pro Antigravity **na mesma sessão da fase**, corrija, teste de novo.
5. Só avance para a próxima fase com a fase atual 100% marcada.

---

## Fase 0-1 — Setup, Login, Módulos, Permissões

- [ ] App abre sem erro em Android
- [ ] App abre sem erro em iOS (mesmo que só "Hello Obra")
- [ ] Primeiro uso cria o Gestor corretamente (nome, login, senha)
- [ ] Login com senha errada é rejeitado com mensagem clara
- [ ] Login com senha correta entra na Home
- [ ] Gestor consegue cadastrar um segundo colaborador
- [ ] Colaborador sem permissão em um módulo **não vê** esse módulo no menu
- [ ] Colaborador com permissão de leitura vê a tela mas **não vê** botões de editar/excluir
- [ ] Gestor desativa um módulo em Configurações → módulo some do menu de **todos**, inclusive do próprio Gestor
- [ ] Reativar o módulo faz ele voltar a aparecer

---

## Fase 1.5 — Onboarding

- [ ] Onboarding aparece automaticamente no primeiro uso, antes da Home
- [ ] É possível concluir preenchendo só o mínimo obrigatório (empresa, Gestor, 1 módulo, 1 conta), pulando o resto
- [ ] Fechar o app no meio do onboarding e reabrir retoma na etapa exata em que parou
- [ ] Botão "Configurar com IA" leva ao modo conversa
- [ ] Modo IA sem internet mostra aviso e oferece o formulário tradicional, sem travar
- [ ] Trocar do modo IA para o formulário no meio do processo preserva os dados já preenchidos
- [ ] Tela de Resumo mostra tudo que foi preenchido, com opção de editar cada bloco
- [ ] Nada aparece no banco de dados (nenhum colaborador, conta, projeto) antes de confirmar na tela de Resumo
- [ ] Ao confirmar, cai direto na Home já com os dados reais funcionando (não uma Home vazia)

---

## Fase 2-3 — Pessoas, Cadastros Básicos, Projetos e Etapas

- [ ] Cadastro de Pessoa funciona (criar, editar, excluir, buscar)
- [ ] Importar contato da agenda traz nome/telefone corretamente
- [ ] Uma Pessoa pode ter mais de uma etiqueta (cliente + fornecedor, por exemplo)
- [ ] Cadastros Básicos (Cores, Materiais, Unidades) têm CRUD completo
- [ ] Criar um Projeto pede área construída, área do terreno e orçamento total
- [ ] Cadastrar Etapas dentro do projeto funciona, com reordenação
- [ ] Custo por m² aparece calculado automaticamente (pela área construída e pela do terreno)
- [ ] `CalculatorTextField` (ícone de calculadora) aparece em todo campo de valor testado até aqui

---

## Fase 3.5-3.6 — Planta Baixa

- [ ] Ferramenta "Cômodo (retângulo)" desenha um cômodo e calcula área/perímetro sozinho
- [ ] Ferramenta "Cômodo (polígono livre)" fecha corretamente ao tocar no ponto inicial
- [ ] Um cômodo em formato L calcula a área certa (confira com uma calculadora externa)
- [ ] Inserir porta/janela numa parede funciona e é visualmente reconhecível
- [ ] Ferramenta "Medir" mostra distância real entre dois pontos
- [ ] Importar foto do projeto abre a tela de calibração
- [ ] Marcar uma linha de calibração e informar a medida real ajusta a escala corretamente
- [ ] Sem calibrar, o app **não permite** marcar a planta como área oficial do projeto
- [ ] Área total da planta pode ser usada como área construída do Projeto (opção explícita, não automática)
- [ ] Planta exporta em JPG e PDF com qualidade legível

## Fase 3.65-3.68 — Importação de DXF, PDF e SVG

- [ ] Importar um DXF com unidade métrica detecta a escala automaticamente, sem pedir calibração
- [ ] Importar um DXF sem unidade detectável cai no fluxo de calibração manual, sem travar
- [ ] Tela de resultado mostra corretamente quantas paredes/cômodos foram lidos
- [ ] Lista de camadas do DXF permite escolher quais importar
- [ ] Importar um SVG com formas básicas gera cômodos/paredes corretamente
- [ ] Importar um PDF vetorial (testar em Android e Web) extrai geometria sem precisar de foto
- [ ] Importar um PDF escaneado (ou testar em iOS) cai automaticamente no fluxo de foto + calibração
- [ ] Geometria importada aparece como pré-visualização — não substitui a planta sem confirmação explícita
- [ ] Botão "Editar antes" abre a geometria importada no editor normal para ajustes

---

## Fase 4-5 — Financeiro Completo, Contas, Equipes

- [ ] Lançar uma despesa vinculada a um projeto/etapa abate o saldo corretamente
- [ ] Filtro de período (mês, ano, personalizado) funciona no painel financeiro
- [ ] Cadastrar uma Conta (caixa, corrente) funciona
- [ ] Pagar/receber algo exige escolher a conta e debita/credita o saldo dela
- [ ] Transferência entre duas contas **não aparece** como receita nem despesa no relatório de lucro
- [ ] Saldo de cada conta bate exatamente com `saldo inicial + movimentos`
- [ ] Lançamento marcado como "Não Contábil" pode ser filtrado para fora de um relatório oficial
- [ ] Centro de Custo de um projeto novo é criado automaticamente junto com o projeto
- [ ] Cadastrar categoria financeira com subcategoria (hierarquia) funciona
- [ ] Cadastrar um funcionário e registrar diárias/empreitadas funciona
- [ ] Gerar pagamento de um período soma corretamente e já cria a despesa no Financeiro
- [ ] Pagamento debita a conta escolhida

---

## Fase 6 — Compras, Orçamentos (com BDI), Vendas

- [ ] Criar pedido de compra e marcar como "Comprado" gera a despesa automaticamente (sem duplicar)
- [ ] Montar um orçamento somando itens de material e mão de obra mostra o custo direto corretamente
- [ ] Aplicar um perfil de BDI calcula o preço de venda — confira manualmente com a fórmula da spec (`[(1+AC+S+R+DF)×(1+L)]/(1−I) − 1`)
- [ ] Alterar a configuração de BDI padrão **não muda** um orçamento já enviado anteriormente
- [ ] Orçamento aprovado tem opção "Converter em Projeto"
- [ ] Fechar uma venda parcelada gera as parcelas corretamente
- [ ] Receber uma parcela credita a conta escolhida

---

## Fase 7 — Planejamento e Execução

- [ ] Cronograma mostra data prevista x realizada por etapa
- [ ] Etapa atrasada (data prevista já passou, não concluída) aparece destacada
- [ ] Diário de obra permite adicionar foto + texto, vinculado a uma etapa e data
- [ ] Checklist de tarefas por etapa funciona (criar, marcar concluída)

---

## Fase 8 — Calculadoras

- [ ] Calculadora científica faz as operações básicas + potência/raiz/log corretamente
- [ ] Calculadora trigonométrica alterna entre graus e radianos corretamente
- [ ] Calculadora de área irregular (Shoelace) dá o resultado certo para um polígono de teste conhecido
- [ ] Calculadora de traço de concreto retorna cimento/areia/brita/água coerentes com o volume informado
- [ ] Resultado de qualquer calculadora tem botão "Usar valor" que devolve ao campo de origem

---

## Fase 8.5-8.8 — Área do Executor

- [ ] Catálogo de normas ABNT abre e lista os itens do seed
- [ ] Busca encontra norma por número (ex.: "6118") e por palavra do título
- [ ] Tela de detalhe da norma **não mostra texto integral**, só resumo próprio + link para o Catálogo ABNT
- [ ] "Anexar meu PDF" funciona e o arquivo aparece na Biblioteca de Manuais
- [ ] Busca full-text encontra um termo que está **dentro** de um PDF anexado (não só no nome do arquivo)
- [ ] Abrir um PDF anexado usa o visualizador nativo da plataforma
- [ ] Excluir um documento remove o arquivo e ele some da busca
- [ ] Calculadora de Ferragem mostra o vínculo com a NBR 6118

---

## Fase 9 — Metas e Exportação

- [ ] Meta financeira calcula o progresso automaticamente com base nos lançamentos reais
- [ ] Exportar uma lista qualquer gera PDF corretamente
- [ ] Exportar a mesma lista gera XLSX abrindo sem erro no Excel/Sheets
- [ ] Exportar em JPG (via Canvas) é visualmente equivalente ao PDF
- [ ] Compartilhar (ícone de exportar) abre o menu nativo de compartilhamento da plataforma

---

## Fase 10 — Backend e Sincronização

- [ ] Backend sobe localmente sem erro (`./gradlew :server:run`)
- [ ] Login via API retorna token corretamente
- [ ] Lançar um dado no mobile offline e depois conectar sincroniza sem duplicar
- [ ] Editar o mesmo registro em dois dispositivos gera detecção de conflito (não sobrescreve silenciosamente um lançamento financeiro)
- [ ] Chave de API da IA **não aparece** em nenhum lugar do código do app cliente (só no backend, via variável de ambiente)

---

## Fase 11 — Configurações, Acessibilidade, Assistente de IA

- [ ] Mudar tema (claro/escuro/alto contraste) aplica no app inteiro imediatamente
- [ ] Aumentar o tamanho da fonte aplica em todas as telas, não só na atual
- [ ] Botão flutuante do Assistente abre em qualquer tela testada
- [ ] Pergunta feita na tela de um projeto específico traz exemplo com os dados **reais** daquele projeto
- [ ] Toda resposta do Assistente aponta pelo menos uma seção do manual
- [ ] Sem internet, o Assistente ainda responde com base na busca local do manual (não trava, não dá erro genérico)
- [ ] Link "Ver no Manual" abre o manual dentro do app, já na seção certa
- [ ] Nenhuma chamada do Assistente inclui dado de outro projeto/colaborador fora do que está na tela atual

---

## Checklist Transversal (testar periodicamente, não é uma fase só)

- [ ] Nenhum valor monetário aparece arredondado errado (conferir centavos em pelo menos 3 lançamentos)
- [ ] IDs de novos registros são UUID (não sequência 1, 2, 3...)
- [ ] App funciona 100% offline no mobile (modo avião) para tudo exceto Assistente de IA e Sync
- [ ] Layout se adapta corretamente ao girar o tablet / redimensionar a janela web (COMPACT/MEDIUM/EXPANDED)
- [ ] Nenhuma tela trava ou fecha sozinha ao alternar rapidamente entre módulos
- [ ] Textos em português sem erro de acentuação/encoding
