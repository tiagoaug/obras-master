# Testes das Firestore Security Rules

Testes de unidade para `../firestore.rules`, rodando contra o emulador do Firestore. Cobrem o
isolamento multi-tenant (a garantia central documentada no topo do arquivo de rules) — não a
permissão fina por módulo, que é responsabilidade do cliente (`PermissionEngine`).

## Pré-requisitos

- Node.js (o repo já assume isso disponível, via `npx firebase`).
- **Java 21+** — só para o emulador do Firestore/Auth em si, não para o app (que continua em
  Java 17). Se só tiver 17 instalado, instale um JDK 21 à parte (ex.: `winget install --id
  EclipseAdoptium.Temurin.21.JDK -e`) e aponte `JAVA_HOME`/`PATH` pra ele só na hora de rodar o
  emulador, como no comando abaixo.

## Rodando

```sh
npm install   # uma vez, instala @firebase/rules-unit-testing

# com JDK 21 disponível como padrão:
firebase emulators:exec --only firestore,auth "npm test"

# ou, se só o Java 17 está no PATH por padrão, aponte pro 21 explicitamente:
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot" \
PATH="/c/Program Files/Eclipse Adoptium/jdk-21.x.x.x-hotspot/bin:$PATH" \
firebase emulators:exec --only firestore,auth "npm test"
```

`emulators:exec` sobe o emulador, roda o comando, e derruba o emulador no final — não precisa
deixar nada rodando manualmente.
