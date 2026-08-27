# SPEC — ObraMaster (Multiplataforma: Android · iOS · Web)

> **Documento de especificação para geração de código (Antigravity)**
> Colocar em: `docs/SPEC_OBRA_MASTER_KMP.md`
> Stack: **Kotlin Multiplatform (KMP) + Compose Multiplatform**
> Substitui/estende a spec `SPEC_OBRA_MASTER.md` (versão Android-only)

---

## 0. O que muda em relação à spec original

Todas as **regras de negócio, entidades, módulos, permissões, orçamentos, calculadoras e critérios de aceite** da spec original **permanecem idênticos**. Este documento redefine apenas:

1. A **estrutura de projeto** (KMP em vez de projeto Android único)
2. A camada de **persistência** (multiplataforma)
3. As **APIs específicas de plataforma** (exportação, contatos, câmera, storage, biometria)
4. A **arquitetura de sincronização** para viabilizar a interface Web
5. O **build e distribuição** por plataforma

> **Princípio-guia:** tudo que é lógica pura (Engines, modelos, validação, cálculos, formatação) vive em `commonMain` e é escrito **uma única vez**. Só toca em código de plataforma o que o sistema operacional obriga.

---

## 1. Estratégia de Plataformas

| Plataforma | UI | Distribuição |
|---|---|---|
| **Android** | Compose Multiplatform | APK / Google Play |
| **iOS** | Compose Multiplatform (via UIViewController) | App Store / TestFlight |
| **Web** | Compose Multiplatform for Web (Wasm) — **ou** front React consumindo a mesma API | Navegador (PWA instalável) |
| **Desktop** (opcional, quase grátis) | Compose Desktop (JVM) | .exe / .dmg — útil para o escritório |

### 1.1 Decisão sobre a Web — duas opções

**Opção A — Compose Multiplatform Web (Wasm) — RECOMENDADA para o MVP**
- Mesmo código de UI das telas mobile, adaptado para layout responsivo.
- Ganho: zero retrabalho de UI. Custo: bundle inicial maior (~3-5 MB), SEO nulo (irrelevante aqui, é sistema interno).

**Opção B — Front web separado (React/Compose HTML) consumindo a API**
- Melhor experiência "desktop-first" (tabelas densas, atalhos de teclado, multi-janela).
- Custo: reimplementar a camada de UI. As Engines continuam reaproveitadas via Kotlin/JS.

> A spec assume a **Opção A**. A arquitetura (repositórios atrás de interfaces + API REST) permite trocar para a Opção B sem tocar em domínio.

### 1.2 Adaptação de layout (responsividade)

Componente `WindowSizeClass` compartilhado:

```kotlin
enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }  // <600dp, 600-840dp, >840dp
```

| Tamanho | Navegação | Listas |
|---|---|---|
| COMPACT (celular) | Bottom bar + FAB | Cards empilhados |
| MEDIUM (tablet) | Navigation Rail | Lista + detalhe lado a lado |
| EXPANDED (web/desktop) | Drawer permanente | Tabela densa + painel de detalhe |

Todas as telas devem usar o mesmo ViewModel; só o Composable de layout muda por `ScreenSize`.

---

## 2. Estrutura do Projeto

```
ObraMaster/
├── shared/                          ← módulo KMP (coração do sistema)
│   ├── commonMain/kotlin/
│   │   ├── domain/                  (modelos, BudgetEngine, FinanceEngine, MetaEngine)
│   │   ├── core/
│   │   │   ├── auth/                (PermissionEngine, hash de senha)
│   │   │   ├── modules/             (ModuleRegistry)
│   │   │   ├── calc/                (TODAS as calculadoras — puras)
│   │   │   ├── export/              (ExportEngine + modelo ExportableDocument)
│   │   │   ├── sync/                (SyncEngine, resolução de conflitos)
│   │   │   └── util/                (formatação BR, datas, moeda)
│   │   ├── data/
│   │   │   ├── db/                  (SQLDelight: .sq, queries, mappers)
│   │   │   ├── remote/              (Ktor client, DTOs)
│   │   │   └── repository/          (interfaces + impl offline-first)
│   │   └── ui/                      (Compose Multiplatform — TODAS as telas)
│   │       ├── theme/               (tema + acessibilidade)
│   │       ├── components/          (CalculatorTextField, LcrudScaffold, etc.)
│   │       └── features/            (login, home, projetos, financeiro, ...)
│   ├── androidMain/                 (actual: contatos, PDF, XLS, câmera, storage)
│   ├── iosMain/                     (actual: idem, via APIs nativas)
│   ├── wasmJsMain/                  (actual: download de arquivo, IndexedDB)
│   └── jvmMain/                     (actual: desktop, opcional)
│
├── androidApp/                      (Activity + manifest + ícones)
├── iosApp/                          (projeto Xcode + SwiftUI wrapper)
├── webApp/                          (index.html + service worker PWA)
├── desktopApp/                      (opcional)
└── server/                          ← Ktor backend (necessário para a Web)
    ├── routes/                      (REST: /auth, /projetos, /financeiro, /sync)
    ├── db/                          (Exposed + PostgreSQL)
    └── shared -> reutiliza domain/ e engines do módulo shared
```

> **Detalhe importante:** o backend Ktor é JVM e importa o mesmo módulo `shared` — ou seja, `BudgetEngine` e `FinanceEngine` rodam **idênticos** no servidor e nos clientes. Zero divergência de cálculo.

---

## 3. Persistência Multiplataforma

### 3.1 Banco local — SQLDelight (substitui Room)

```
shared/commonMain/sqldelight/br/com/tiago/obramaster/db/
├── Colaborador.sq
├── Projeto.sq
├── Etapa.sq
├── LancamentoFinanceiro.sq
├── ... (uma .sq por entidade)
```

Drivers por plataforma (`expect/actual`):

| Plataforma | Driver |
|---|---|
| Android | `AndroidSqliteDriver` |
| iOS | `NativeSqliteDriver` |
| Web (Wasm) | `WebWorkerDriver` (sql.js + OPFS/IndexedDB) |
| Desktop | `JdbcSqliteDriver` |

```kotlin
// commonMain
expect class DatabaseDriverFactory { fun create(): SqlDriver }
```

- Todas as queries escritas **uma vez** em `.sq` → SQLDelight gera código type-safe para todas as plataformas.
- Migrações versionadas em `migrations/*.sqm`.

### 3.2 Campos monetários

- Armazenar como **`Long` em centavos** (não existe `BigDecimal` em `commonMain` do KMP sem dependência extra).
- Alternativa: usar a lib `ionspin/kotlin-multiplatform-bignum` se precisar de precisão arbitrária em orçamentos grandes.
- Formatação BR (`R$ 1.234,56`) implementada em `core/util/MoneyFormatter.kt` — **função pura, sem `NumberFormat`** (que não existe em common). Formatação manual garante resultado idêntico nas 3 plataformas.

### 3.3 Datas

- `kotlinx-datetime` (`LocalDate`, `LocalDateTime`, `Instant`, `TimeZone`).
- Armazenar sempre em **UTC epoch millis** no banco; converter para timezone local só na apresentação.

### 3.4 Preferências

- `multiplatform-settings` (encapsula SharedPreferences / NSUserDefaults / localStorage).
- Dados sensíveis (token de sessão): `EncryptedSharedPreferences` (Android) / Keychain (iOS) / cookie httpOnly (Web) — via `expect/actual SecureStorage`.

---

## 4. Camada de Plataforma (expect/actual)

Contratos obrigatórios em `commonMain`:

```kotlin
// Exportação de arquivos
expect class FileExporter {
    suspend fun savePdf(bytes: ByteArray, fileName: String): ExportResult
    suspend fun saveXlsx(bytes: ByteArray, fileName: String): ExportResult
    suspend fun saveJpg(bytes: ByteArray, fileName: String): ExportResult
    suspend fun share(file: ExportResult)
}

// Importação de contatos da agenda
expect class ContactsProvider {
    suspend fun isAvailable(): Boolean
    suspend fun pickContacts(): List<ContatoImportado>
}

// Câmera / galeria (diário de obra)
expect class ImagePicker {
    suspend fun takePhoto(): ImageRef?
    suspend fun pickFromGallery(multiple: Boolean): List<ImageRef>
}

// Armazenamento de imagens
expect class ImageStore {
    suspend fun save(image: ImageRef, compressQuality: Int = 80): String // retorna path/key
    suspend fun load(key: String): ByteArray?
    suspend fun delete(key: String)
}

// Biometria (login rápido) — opcional
expect class BiometricAuth {
    suspend fun isAvailable(): Boolean
    suspend fun authenticate(reason: String): Boolean
}

expect class DatabaseDriverFactory { fun create(): SqlDriver }
expect object SecureStorage { fun put(k: String, v: String); fun get(k: String): String? }
```

### 4.1 Implementações por plataforma

| Contrato | Android | iOS | Web (Wasm) |
|---|---|---|---|
| `FileExporter` | `FileProvider` + `Intent.ACTION_SEND` | `UIActivityViewController` | Blob + `<a download>` |
| `ContactsProvider` | `ContactsContract` | `CNContactPickerViewController` | **Indisponível** → import via CSV/vCard |
| `ImagePicker` | `PhotoPicker` / CameraX | `PHPickerViewController` / `UIImagePickerController` | `<input type="file" capture>` |
| `ImageStore` | `filesDir` | `NSFileManager` documents | OPFS / IndexedDB |
| `BiometricAuth` | `BiometricPrompt` | `LAContext` (Face/Touch ID) | WebAuthn (opcional) |

> **Regra de degradação:** todo contrato tem `isAvailable()`. Recurso indisponível na plataforma → o botão correspondente **some da UI**, nunca quebra. Na Web, "Importar da agenda" vira "Importar contatos (CSV/vCard)".

---

## 5. Geração de PDF / XLS / JPG multiplataforma

Este é o ponto mais delicado da portabilidade. Estratégia em duas camadas:

### 5.1 Camada comum (compartilhada)

`ExportEngine` em `commonMain` produz um **modelo abstrato de documento**, sem nenhuma API gráfica:

```kotlin
data class ExportableDocument(
    val titulo: String,
    val subtitulo: String? = null,
    val empresa: DadosEmpresa,
    val colunas: List<Coluna>,
    val linhas: List<List<CellValue>>,
    val resumo: List<Pair<String, String>> = emptyList(),
    val rodape: String? = null
)
```

### 5.2 Renderização

| Formato | Estratégia |
|---|---|
| **XLSX** | Escrever o formato **OOXML na mão** em `commonMain` (é um ZIP com XMLs). Usar `kotlinx-io` + lib de zip multiplataforma. Evita Apache POI (JVM-only). Suporte a estilos básicos, números, datas e fórmulas de soma é suficiente. |
| **PDF** | Gerador próprio em `commonMain` escrevendo PDF 1.4 puro (texto, linhas, tabelas, imagens JPEG embutidas, fontes base14 + fonte embutida para acentuação). É trabalhoso, porém é **a única forma de ter saída idêntica nas 3 plataformas**. |
| **JPG** | Renderizar um Composable off-screen com `ImageComposeScene` (Compose Multiplatform) → `Bitmap` → encode JPEG via **Skia** (`org.jetbrains.skia.Image.encodeToData`) — disponível em todas as plataformas Compose MP. |

> **Alternativa pragmática para acelerar o MVP:** gerar PDF no **backend Ktor** (JVM, com iText/OpenPDF) e o cliente apenas baixa. Mobile offline mantém o gerador próprio simplificado. Decidir na Fase 9.

---

## 6. Backend e Sincronização (habilita a Web)

### 6.1 Modelo híbrido

- **Mobile:** offline-first. Escreve sempre local; sincroniza quando há rede.
- **Web:** online-first, fala direto com a API (com cache leve em IndexedDB para funcionar em queda rápida de conexão).
- **Modo standalone:** se o cliente não quiser servidor, o app roda 100% local (flag `SYNC_ENABLED = false`) — a Web simplesmente não é oferecida.

### 6.2 Backend Ktor

```
POST /auth/login            → JWT (access + refresh)
GET  /me/permissions
GET  /sync/pull?since=<ts>  → alterações desde timestamp
POST /sync/push             → lote de mudanças locais
CRUD /projetos /etapas /financeiro /compras /orcamentos /vendas
     /pessoas /equipes /pagamentos /metas /cadastros /modulos
GET  /export/{recurso}?formato=pdf|xlsx  (opcional, ver 5.2)
```

- **Banco:** PostgreSQL + Exposed.
- **Auth:** JWT + BCrypt. A `PermissionEngine` roda **também no servidor** — permissão nunca é validada só no cliente.
- **Multi-tenant:** coluna `empresaId` em todas as tabelas (permite atender várias construtoras no futuro).

### 6.3 SyncEngine (em `commonMain`, função pura + coordenador)

```kotlin
// Toda entidade sincronizável carrega:
interface Syncable {
    val id: String            // UUID gerado no cliente (não autoincrement!)
    val updatedAt: Long       // epoch millis
    val deletedAt: Long?      // soft-delete
    val syncStatus: SyncStatus // LOCAL_ONLY | PENDING | SYNCED | CONFLICT
}
```

**Regras de conflito:**
1. Padrão: **Last-Write-Wins** por `updatedAt`.
2. Exceção — **valores financeiros** (lançamentos, pagamentos, gastos): nunca sobrescrever. Registros financeiros são **imutáveis após criação**; correção se faz por estorno/novo lançamento. Elimina a classe mais perigosa de conflito.
3. Conflito real detectado → marca `CONFLICT` e exibe tela de resolução manual para o Gestor.

> **Consequência obrigatória:** todos os IDs passam de `Long autoincrement` para **`String` UUID gerado no cliente**. Isso precisa valer desde a Fase 1 — mudar depois é caro.

---

## 7. Injeção de Dependência e Navegação

| Assunto | Escolha |
|---|---|
| DI | **Koin** (Hilt é Android-only) |
| Navegação | **Compose Navigation Multiplatform** (ou Voyager/Decompose) |
| ViewModel | `androidx.lifecycle.ViewModel` multiplataforma (KMP-ready) ou ViewModel próprio com `CoroutineScope` |
| Rede | Ktor Client (engine: OkHttp / Darwin / JS) |
| Serialização | `kotlinx-serialization` |
| Concorrência | Coroutines + `StateFlow` |
| Imagens | Coil 3 (multiplataforma) |
| Gráficos do Financeiro | Compose Canvas próprio (evita libs Android-only) |

---

## 8. Especificidades de iOS

- Módulo `shared` exporta um **Framework** consumido pelo Xcode.
- `iosApp` é um wrapper SwiftUI mínimo: `ComposeUIViewController { App() }`.
- Permissões no `Info.plist`: `NSContactsUsageDescription`, `NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`, `NSFaceIDUsageDescription`.
- Gestos: respeitar swipe-back nativo; evitar padrões exclusivos de Android (ex.: botão de voltar físico).
- Compilar em **Mac** (obrigatório para build/assinatura). Se você não tem Mac, usar **Codemagic** ou GitHub Actions com runner macOS.
- Ícone de exportar: usar `SF Symbols`-like coerente; sheet de compartilhamento nativa.

---

## 9. Especificidades da Web

- **PWA instalável**: `manifest.json` + service worker com cache de assets.
- Rotas refletidas na URL (`/projetos/{id}/etapas`) para permitir F5, favoritos e voltar do navegador.
- Atalhos de teclado no layout EXPANDED: `Ctrl+N` novo, `Ctrl+F` buscar, `Esc` fechar.
- Tabelas densas com ordenação por coluna e paginação (a UI mobile de cards não escala para centenas de linhas na tela grande).
- Download de exportações via Blob, sem sheet de compartilhamento.
- Sem acesso a contatos → substituir por **importação de CSV/vCard** (parser em `commonMain`, reaproveitado nas 3 plataformas como opção alternativa).

---

## 10. Ajustes na Arquitetura Modular

`ModuleRegistry` ganha um novo eixo: **disponibilidade por plataforma**.

```kotlin
data class ModuleAvailability(
    val module: AppModule,
    val enabled: Boolean,               // definido pelo Gestor
    val platforms: Set<Platform> = Platform.ALL  // onde faz sentido existir
)

enum class Platform { ANDROID, IOS, WEB, DESKTOP }

expect val currentPlatform: Platform
```

Regra de renderização de qualquer tela/ação:

```
visível = módulo.enabled
        && currentPlatform in módulo.platforms
        && PermissionEngine.canView(user, module)
        && (recurso.isAvailable() se depender de API de plataforma)
```

Exemplos práticos:
- **Diário de obra com câmera**: Android/iOS completo; Web apenas upload de arquivo.
- **Import da agenda**: Android/iOS; Web via CSV.
- **Relatórios densos / fechamento financeiro**: melhor experiência em Web/Desktop, mas disponível em todas.

---

## 11. Fases de Implementação (revisadas para KMP)

| Fase | Entrega | Plataformas |
|---|---|---|
| **0** | Setup KMP: Gradle multiplataforma, Koin, SQLDelight, kotlinx-datetime, tema base, `expect/actual` esqueleto, "Hello Obra" rodando nos 3 alvos | And + iOS + Web |
| **1** | Tema + acessibilidade, banco local, Login/Gestor, `ModuleRegistry`, `PermissionEngine`, Home responsiva (3 `ScreenSize`) | 3 alvos |
| **2** | Pessoas (contatos nativos + CSV), Cadastros Básicos, `LcrudScaffold`, `CalculatorTextField` | 3 alvos |
| **3** | Projetos + Etapas + `BudgetEngine` + custo/m² | 3 alvos |
| **4** | Financeiro completo + gráficos em Canvas | 3 alvos |
| **5** | Equipes + Pagamentos | 3 alvos |
| **6** | Compras + Orçamentos + Vendas | 3 alvos |
| **7** | Planejamento + Execução (diário de obra, Gantt) | 3 alvos |
| **8** | Calculadoras (todas puras em `commonMain` — funcionam de graça nos 3) | 3 alvos |
| **9** | `ExportEngine`: XLSX + PDF + JPG multiplataforma | 3 alvos |
| **10** | **Backend Ktor + SyncEngine** + habilitação real da Web multiusuário | Server + Web |
| **11** | Configurações, backup, auditoria, PWA, polimento, publicação nas lojas | 3 alvos |

> **Recomendação de execução:** validar Android primeiro (ciclo mais rápido), rodar iOS a cada fase (para não acumular dívida de `actual` não implementado) e a Web ao final de cada bloco.

---

## 12. Regras Críticas para o Gerador (KMP)

1. **Nenhum import de `android.*` fora de `androidMain`.** Se aparecer em `commonMain`, é erro de arquitetura → criar `expect/actual`.
2. **IDs são `String` UUID gerados no cliente**, desde a Fase 1.
3. **Dinheiro em `Long` (centavos)** — nunca `Double`.
4. **Datas em `kotlinx-datetime`, armazenadas em UTC millis.**
5. Todas as **Engines** (`Budget`, `Finance`, `Meta`, `Permission`, calculadoras, formatadores) ficam em `commonMain` como **funções puras**, com testes em `commonTest` — rodam nos 3 alvos.
6. Toda tela usa o **mesmo ViewModel** nas 3 plataformas; apenas o layout varia por `ScreenSize`.
7. Todo recurso de plataforma passa por um contrato `expect` com `isAvailable()` e **degradação graciosa** na UI.
8. `CalculatorTextField` obrigatório em 100% dos campos de valor, nas 3 plataformas.
9. Permissões validadas **no cliente e no servidor** — cliente nunca é fonte de verdade de autorização.
10. Registros financeiros são **imutáveis**; correção por estorno.

---

## 13. Critérios de Aceite (adicionais aos da spec original)

- [ ] O mesmo binário lógico roda em Android, iOS e Web a partir de `commonMain`
- [ ] Nenhum código de UI duplicado entre plataformas (exceto wrappers de entrada)
- [ ] `BudgetEngine` e `FinanceEngine` produzem resultados **bit-idênticos** nos 3 alvos (teste em `commonTest`)
- [ ] Layout se adapta corretamente em COMPACT / MEDIUM / EXPANDED
- [ ] Exportação PDF/XLSX/JPG funciona nas 3 plataformas com saída visualmente equivalente
- [ ] App funciona **100% offline** no mobile; sincroniza sem perda ao voltar a rede
- [ ] Conflito de sincronização é detectado e apresentado ao Gestor, nunca resolvido silenciosamente em dado financeiro
- [ ] Recurso indisponível na plataforma some da UI em vez de gerar erro
- [ ] Web instalável como PWA, com URLs navegáveis e botão voltar funcional
- [ ] iOS compila, assina e roda em dispositivo físico via TestFlight
