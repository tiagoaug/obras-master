# Instrução Mestra — Colar no Antigravity Antes de Começar

> Colocar em: `docs/INSTRUCAO_MESTRA.md`
> Esta é a mensagem para você colar **inteira, uma vez**, na primeira sessão do Antigravity, antes de pedir qualquer fase. Ela define o contrato de como o projeto inteiro deve ser conduzido. Cole novamente (ou lembre) sempre que perceber desvio.

---

```
Você vai me ajudar a construir o ObraMaster, um ERP mobile/web/desktop para gestão
de obras, em Kotlin Multiplatform + Compose Multiplatform. Toda a especificação já
está pronta e completa na pasta docs/. Antes de escrever qualquer código, leia
docs/README.md — ele é o índice de todos os documentos e explica a ordem de leitura.

REGRA MAIS IMPORTANTE DE TODAS:
As especificações em docs/ não são sugestões — são o contrato do projeto. Você deve
segui-las à risca. Isso significa:

1. Não invente entidades, campos, telas ou fluxos que não estejam descritos nos
   documentos. Se algo parecer faltando ou ambíguo, PARE e me pergunte antes de
   decidir por conta própria.
2. Não simplifique uma regra de negócio para "ir mais rápido". Se uma spec diz que
   uma transferência entre contas não gera lançamento financeiro, ou que um
   lançamento financeiro é imutável, ou que a IA nunca aplica calibração sem
   confirmação do usuário — isso é definitivo, não um ponto de partida para
   discussão.
3. Não troque a arquitetura proposta (Engines puras em commonMain, expect/actual
   para código de plataforma, IDs em UUID, dinheiro em Long/centavos) por
   alternativas "mais simples". Essas decisões já foram tomadas e têm razão de
   ser explicada nos próprios documentos.
4. Se você achar que uma parte da spec tem um problema técnico real (não só uma
   preferência sua), explique o problema e proponha uma alternativa — mas não
   troque silenciosamente.

COMO VAMOS TRABALHAR — UMA FASE POR VEZ:
O projeto está dividido em fases, listadas em docs/README.md e detalhadas em
docs/GUIA_ANTIGRAVITY.md. Vamos seguir essa ordem à risca, uma fase por sessão.
Isso significa:

1. Eu vou te dizer qual fase estamos fazendo e apontar os documentos relevantes
   dela (não é pra você ler o projeto inteiro toda vez).
2. Antes de escrever qualquer código, me dê um plano curto: quais entidades, quais
   arquivos, em que ordem. Eu aprovo ou ajusto antes de você continuar.
3. Implemente só o escopo daquela fase. Não adiante código de fases futuras "já
   que está ali mesmo" — isso quebra minha capacidade de revisar e testar em
   pedaços administráveis.
4. Ao terminar, me diga explicitamente o que foi entregue e quais critérios de
   aceite da spec correspondente foram atendidos. Eu vou validar contra
   docs/CHECKLIST_TESTES.md antes de pedir a próxima fase.
5. Se uma fase depender de algo de uma fase anterior que não ficou pronto direito,
   pare e me avise — não construa em cima de uma base que você sabe que está
   incompleta ou errada.

REGRAS TÉCNICAS GLOBAIS (valem em toda fase, sem exceção):
- IDs de todas as entidades são String (UUID gerado no cliente), nunca
  autoincrement — isso é necessário desde o início por causa da sincronização
  futura com o backend.
- Valores monetários são Long em centavos. Nunca Double ou Float para dinheiro.
- Datas usam kotlinx-datetime, armazenadas em UTC millis.
- Nenhum import de android.*, UIKit ou específico de navegador em código
  commonMain. Se uma funcionalidade precisa de API de plataforma, ela vira um
  contrato expect/actual.
- Toda Engine de regra de negócio (Budget, Finance, Meta, Permission, BDI,
  PlantaBaixa, calculadoras) é uma função pura em commonMain, sem dependência de
  Compose ou Android, com testes correspondentes em commonTest.
- Toda tela usa o mesmo ViewModel nas três plataformas (Android, iOS, Web); só o
  layout muda conforme o tamanho de tela (COMPACT / MEDIUM / EXPANDED).
- O campo CalculatorTextField é obrigatório em 100% dos campos de valor numérico
  do app inteiro, sem exceção.
- Toda tela verifica ModuleRegistry (o módulo está ativo?) e PermissionEngine
  (o usuário logado pode ver/editar isso?) antes de renderizar qualquer ação.
- Registros financeiros são imutáveis depois de criados — correções são feitas
  por estorno, nunca por edição direta do lançamento original.
- Nenhuma norma da ABNT tem texto integral reproduzido no app — só metadado
  (número, título, resumo próprio) e link para a fonte oficial.
- Nenhuma chave de API (IA, serviços externos) fica hardcoded ou embutida no
  cliente — sempre via variável de ambiente no backend.

DOCUMENTAÇÃO QUE VOCÊ DEVE MANTER ATUALIZADA JUNTO COM O CÓDIGO:
Toda vez que uma tela ou funcionalidade nova for implementada, a seção
correspondente em docs/MANUAL_DO_PROGRAMA.md precisa ser criada ou atualizada
(com a âncora #id no mesmo padrão das seções existentes). Isso não é opcional —
é o que mantém o Assistente de IA do próprio app confiável, porque ele responde
com base no que está escrito nesse manual. Uma funcionalidade só está "pronta"
quando o código E a documentação do manual estão alinhados.

QUANDO EU DISSER "PRÓXIMA FASE":
Isso significa que eu já testei a fase atual contra o checklist e aprovei. Você
pode seguir para a próxima fase da lista, sempre relendo o(s) documento(s)
indicado(s) para aquela fase antes de começar.

Confirme que entendeu essas regras antes de começarmos. Depois disso, eu vou
indicar a primeira fase (Fase 0 — Setup do projeto KMP).
```

---

## Por que esta instrução existe

Agentes de código tendem a "preencher lacunas" com suposições razoáveis quando a especificação não é clara — o que é ótimo em projetos pequenos e perigoso num projeto deste tamanho, onde uma suposição errada na Fase 3 (ex.: usar `Long` autoincrement em vez de UUID) só aparece como problema real na Fase 10, quando já é caro de corrigir. Esta instrução existe para eliminar essa categoria de erro: qualquer dúvida vira pergunta para você, não decisão silenciosa do agente.

O pedido de "plano curto antes do código" é o segundo maior redutor de retrabalho — é muito mais barato corrigir um plano de 10 linhas do que revisar 500 linhas de código já escritas na direção errada.
