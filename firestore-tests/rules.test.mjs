// Testes de unidade das Firestore Security Rules (../firestore.rules) contra o emulador.
// Cobre a garantia central documentada nas rules: isolamento multi-tenant (ninguém lê/escreve
// dado de uma empresa que não é a sua) — não replica a PermissionEngine fina do cliente, que é
// deliberadamente responsabilidade só do app (ver comentário no topo de firestore.rules).
//
// Rodar com o emulador já de pé:
//   firebase emulators:exec --only firestore,auth "npm --prefix firestore-tests test"
// ou, com o emulador rodando em outro terminal:
//   npm --prefix firestore-tests test

import { test, before, after } from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} from "@firebase/rules-unit-testing";
import { doc, getDoc, setDoc, updateDoc, deleteDoc } from "firebase/firestore";

let testEnv;

const EMPRESA_A = "empresa-a";
const EMPRESA_B = "empresa-b";

const GESTOR_A = { uid: "gestor-a", email: "gestor-a@example.com" };
const COLAB_A = { uid: "colab-a", email: "colab-a@example.com" };
const GESTOR_B = { uid: "gestor-b", email: "gestor-b@example.com" };
const OUTSIDER = { uid: "outsider", email: "outsider@example.com" };

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "obras-master-rules-test",
    firestore: {
      rules: fs.readFileSync("../firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085,
    },
  });

  // Semeia o estado inicial direto (bypassando as rules) pra cada teste partir de um cenário
  // já com dois tenants distintos e seus colaboradores.
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, "colaboradores", GESTOR_A.uid), {
      empresaId: EMPRESA_A,
      nome: "Gestor A",
      email: GESTOR_A.email,
      ativo: true,
      ehGestor: true,
    });
    await setDoc(doc(db, "colaboradores", COLAB_A.uid), {
      empresaId: EMPRESA_A,
      nome: "Colaborador A",
      email: COLAB_A.email,
      ativo: true,
      ehGestor: false,
    });
    await setDoc(doc(db, "colaboradores", GESTOR_B.uid), {
      empresaId: EMPRESA_B,
      nome: "Gestor B",
      email: GESTOR_B.email,
      ativo: true,
      ehGestor: true,
    });
    await setDoc(doc(db, "empresas", EMPRESA_A), { nome: "Empresa A" });
    await setDoc(doc(db, "empresas", EMPRESA_B), { nome: "Empresa B" });
    await setDoc(doc(db, "empresas", EMPRESA_A, "contas", "conta-1"), {
      nome: "Caixa A",
      tipo: "CAIXA",
    });
    await setDoc(doc(db, "convites", "convite-1"), {
      empresaId: EMPRESA_A,
      email: "convidado@example.com",
      nome: "Convidado",
      ehGestor: false,
    });
  });
});

after(async () => {
  await testEnv.cleanup();
});

function as(user) {
  return testEnv.authenticatedContext(user.uid, { email: user.email }).firestore();
}

function anon() {
  return testEnv.unauthenticatedContext().firestore();
}

// --- Isolamento entre empresas -------------------------------------------------------------

test("colaborador da empresa A não lê o documento de empresa da empresa B", async () => {
  await assertFails(getDoc(doc(as(COLAB_A), "empresas", EMPRESA_B)));
});

test("colaborador da empresa A lê o documento da própria empresa", async () => {
  await assertSucceeds(getDoc(doc(as(COLAB_A), "empresas", EMPRESA_A)));
});

test("colaborador da empresa A não lê recurso de negócio (contas) da empresa B", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), "empresas", EMPRESA_B, "contas", "conta-b"), {
      nome: "Caixa B",
    });
  });
  await assertFails(getDoc(doc(as(COLAB_A), "empresas", EMPRESA_B, "contas", "conta-b")));
});

test("colaborador da empresa A lê e escreve recurso de negócio da própria empresa", async () => {
  await assertSucceeds(getDoc(doc(as(COLAB_A), "empresas", EMPRESA_A, "contas", "conta-1")));
  await assertSucceeds(
    setDoc(doc(as(COLAB_A), "empresas", EMPRESA_A, "contas", "conta-2"), { nome: "Nova conta" }),
  );
});

test("colaborador (não-Gestor) da empresa A não escreve o documento da empresa (só o Gestor pode)", async () => {
  await assertFails(updateDoc(doc(as(COLAB_A), "empresas", EMPRESA_A), { nome: "Hackeado" }));
});

test("Gestor da empresa A escreve o documento da própria empresa", async () => {
  await assertSucceeds(updateDoc(doc(as(GESTOR_A), "empresas", EMPRESA_A), { nome: "Empresa A Ltda" }));
});

test("usuário não autenticado não lê nada", async () => {
  await assertFails(getDoc(doc(anon(), "empresas", EMPRESA_A)));
  await assertFails(getDoc(doc(anon(), "colaboradores", GESTOR_A.uid)));
});

// --- colaboradores/{uid} --------------------------------------------------------------------

test("um uid recém-autenticado consegue criar o próprio doc de colaborador (auto-cadastro)", async () => {
  await assertSucceeds(
    setDoc(doc(as({ uid: "novo-uid", email: "novo@example.com" }), "colaboradores", "novo-uid"), {
      empresaId: EMPRESA_A,
      nome: "Novo",
      email: "novo@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("um uid não consegue criar o doc de colaborador de outro uid", async () => {
  await assertFails(
    setDoc(doc(as(COLAB_A), "colaboradores", "outro-uid"), {
      empresaId: EMPRESA_A,
      nome: "Forjado",
      email: "x@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("colaborador da empresa A não lê colaborador da empresa B", async () => {
  await assertFails(getDoc(doc(as(COLAB_A), "colaboradores", GESTOR_B.uid)));
});

test("colaborador da empresa A lê colaborador da própria empresa (o Gestor)", async () => {
  await assertSucceeds(getDoc(doc(as(COLAB_A), "colaboradores", GESTOR_A.uid)));
});

test("Gestor da empresa A desativa (update) o colaborador da própria empresa", async () => {
  await assertSucceeds(updateDoc(doc(as(GESTOR_A), "colaboradores", COLAB_A.uid), { ativo: false }));
});

test("Gestor da empresa B não consegue alterar colaborador da empresa A", async () => {
  await assertFails(updateDoc(doc(as(GESTOR_B), "colaboradores", COLAB_A.uid), { ativo: false }));
});

test("delete em colaboradores é sempre negado (soft-delete only)", async () => {
  await assertFails(deleteDoc(doc(as(GESTOR_A), "colaboradores", COLAB_A.uid)));
});

// --- convites/{id} ---------------------------------------------------------------------------

test("colaborador comum (não-Gestor) não cria convite", async () => {
  await assertFails(
    setDoc(doc(as(COLAB_A), "convites", "convite-negado"), {
      empresaId: EMPRESA_A,
      email: "x@example.com",
      nome: "X",
      ehGestor: false,
    }),
  );
});

test("Gestor cria convite pra própria empresa", async () => {
  await assertSucceeds(
    setDoc(doc(as(GESTOR_A), "convites", "convite-novo"), {
      empresaId: EMPRESA_A,
      email: "novoconvidado@example.com",
      nome: "Novo Convidado",
      ehGestor: false,
    }),
  );
});

test("pessoa com o e-mail do convite consegue ler, mesmo sem pertencer a nenhuma empresa ainda", async () => {
  await assertSucceeds(
    getDoc(doc(as({ uid: "quem-sabe", email: "convidado@example.com" }), "convites", "convite-1")),
  );
});

test("outsider sem relação com o convite não consegue ler", async () => {
  await assertFails(getDoc(doc(as(OUTSIDER), "convites", "convite-1")));
});

test("Gestor de outra empresa não consegue ler convite que não é dele", async () => {
  await assertFails(getDoc(doc(as(GESTOR_B), "convites", "convite-1")));
});

// --- Default deny -----------------------------------------------------------------------------

test("coleção de nível raiz não declarada nas rules é negada por padrão", async () => {
  await assertFails(getDoc(doc(as(GESTOR_A), "algumaColecaoNaoDeclarada", "doc-1")));
});
