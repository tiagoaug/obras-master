# ObraMaster Server (Fase 10.1 + 10.2, completa)

Backend Ktor do ObraMaster — auth (JWT) + CRUD de todos os recursos listados em
`SPEC_OBRA_MASTER_KMP.md` §6.2 (Projetos, Etapas, Financeiro, Pessoas, Equipes, Pagamentos,
Cadastros Básicos, Compras, Orçamentos, Vendas, Metas, Módulos). Fases 10.1 e 10.2 completas.

## Rodar localmente

```
./gradlew :server:run
```

Sobe em `http://localhost:8080`, com banco **H2 embarcado** em `./data/obramaster-server` (arquivo
criado automaticamente na primeira execução, sem precisar instalar Postgres). Não há usuário
inicial — crie um Empresa/Colaborador diretamente no banco H2 (ex.: via console H2 ou um script)
até a Fase 10.2 trazer um endpoint de cadastro.

## Variáveis de ambiente (todas opcionais em dev — têm default de desenvolvimento)

| Variável | Efeito | Default (dev) |
|---|---|---|
| `OBRAMASTER_JWT_SECRET` | Chave HMAC do JWT — **obrigatório trocar em produção** | `dev-secret-troque-em-producao` |
| `OBRAMASTER_JWT_ISSUER` / `OBRAMASTER_JWT_AUDIENCE` | Claims de validação do token | `obramaster-server` / `obramaster-clients` |
| `OBRAMASTER_DB_URL` | JDBC URL — trocar para Postgres em produção (`jdbc:postgresql://...`) | H2 local |
| `OBRAMASTER_DB_USER` / `OBRAMASTER_DB_PASSWORD` | Credenciais do banco | `sa` / vazio |
| `OBRAMASTER_PORT` | Porta HTTP | `8080` |

## Endpoints (Fase 10.1)

```
POST /auth/login              { login, senha, empresaId } → { accessToken, refreshToken, ... }
POST /auth/refresh            (Bearer refreshToken)        → novo par de tokens
GET  /me/permissions          (Bearer accessToken)          → permissões do colaborador logado

GET    /projetos
GET    /projetos/{id}
POST   /projetos
PUT    /projetos/{id}
DELETE /projetos/{id}         (soft-delete)

GET    /projetos/{projetoId}/etapas
GET    /etapas/{id}
POST   /projetos/{projetoId}/etapas
PUT    /etapas/{id}
DELETE /etapas/{id}           (soft-delete)

GET    /contas
GET    /contas/{id}
POST   /contas
PUT    /contas/{id}
DELETE /contas/{id}           (soft-delete)

GET    /categorias-financeiras
GET    /categorias-financeiras/{id}
POST   /categorias-financeiras
PUT    /categorias-financeiras/{id}
DELETE /categorias-financeiras/{id}   (soft-delete; 409 se `padraoDoSistema=true` — só inativa via PUT)

GET    /centros-de-custo
GET    /centros-de-custo/{id}
POST   /centros-de-custo
PUT    /centros-de-custo/{id}
DELETE /centros-de-custo/{id} (soft-delete)

GET    /lancamentos-financeiros[?projetoId=]
GET    /lancamentos-financeiros/{id}
POST   /lancamentos-financeiros       (sem PUT/DELETE — lançamento é imutável, correção é por estorno)

GET    /pessoas
GET    /pessoas/{id}
POST   /pessoas
PUT    /pessoas/{id}
DELETE /pessoas/{id}          (soft-delete)

GET    /equipes
GET    /equipes/{id}
POST   /equipes
PUT    /equipes/{id}          (substitui a lista de membros por completo, não faz merge)
DELETE /equipes/{id}          (soft-delete)

GET    /funcionarios
GET    /funcionarios/{pessoaId}       (chave é pessoaId — Funcionario é extensão 1:1 de Pessoa)
POST   /funcionarios
PUT    /funcionarios/{pessoaId}
DELETE /funcionarios/{pessoaId}       (soft-delete)

GET    /pagamentos[?pessoaId=]
GET    /pagamentos/{id}
POST   /pagamentos
PUT    /pagamentos/{id}
DELETE /pagamentos/{id}       (soft-delete)

GET    /registros-trabalho[?pessoaId=][&pago=true|false]
GET    /registros-trabalho/{id}
POST   /registros-trabalho
PUT    /registros-trabalho/{id}
DELETE /registros-trabalho/{id}       (soft-delete)

GET    /cores
GET    /cores/{id}
POST   /cores
PUT    /cores/{id}
DELETE /cores/{id}            (soft-delete)

GET    /unidades-medida
GET    /unidades-medida/{id}
POST   /unidades-medida
PUT    /unidades-medida/{id}
DELETE /unidades-medida/{id}  (soft-delete)

GET    /materiais
GET    /materiais/{id}
POST   /materiais
PUT    /materiais/{id}
DELETE /materiais/{id}        (soft-delete)

GET    /fornecedores          (módulo COMPRAS)
GET    /fornecedores/{pessoaId}       (chave é pessoaId, extensão 1:1 de Pessoa)
POST   /fornecedores
PUT    /fornecedores/{pessoaId}
DELETE /fornecedores/{pessoaId}       (soft-delete)

GET    /pedidos-compra                (body: { pedido, itens } — agregado, mesmo padrão de Equipe/membros)
GET    /pedidos-compra/{id}
POST   /pedidos-compra                (PUT substitui a lista de itens por completo, não faz merge)
PUT    /pedidos-compra/{id}
DELETE /pedidos-compra/{id}   (soft-delete)

GET    /configs-bdi           (módulo ORCAMENTOS)
GET    /configs-bdi/{id}
POST   /configs-bdi
PUT    /configs-bdi/{id}
DELETE /configs-bdi/{id}      (soft-delete)

GET    /orcamentos                    (body: { orcamento, itens })
GET    /orcamentos/{id}
POST   /orcamentos
PUT    /orcamentos/{id}
DELETE /orcamentos/{id}       (soft-delete)

GET    /vendas                        (body: { venda, parcelas })
GET    /vendas/{id}
POST   /vendas
PUT    /vendas/{id}
DELETE /vendas/{id}           (soft-delete)

GET    /metas
GET    /metas/{id}
POST   /metas
PUT    /metas/{id}
DELETE /metas/{id}            (soft-delete)

GET    /modulos                       (Map<moduleId, enabled> — leitura liberada a qualquer colaborador)
PUT    /modulos/{moduleId}            { enabled } — só o Gestor pode alterar (403 caso contrário)
```

Todas as rotas de recurso exigem `Authorization: Bearer <accessToken>` e são validadas pela mesma
`PermissionEngine` (módulo `:core`) usada no cliente — permissão nunca é decidida só no app.
Multi-tenant: toda consulta é escopada pelo `empresaId` do token, nunca por parâmetro do cliente.

## Testes

```
./gradlew :server:test
```

Usa `testApplication` do Ktor + H2 em memória (um banco isolado por teste) — não precisa do
servidor rodando nem de Postgres.

## Ainda não implementado (Fase 10.3+)

- Endpoint de cadastro de Empresa/Colaborador (hoje só existe via inserção direta no banco).
- `SyncEngine` (`/sync/pull`, `/sync/push`) — Fase 10.3.
- Endpoint do Assistente de IA (`/assistant/ask`) — Fase 10.4/10.5.
- Migração para Postgres em produção (troca de `OBRAMASTER_DB_URL`, já suportada pelo Exposed sem
  mudança de código).
