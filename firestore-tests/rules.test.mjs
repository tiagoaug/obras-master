// Testes de unidade das Firestore Security Rules (../firestore.rules) contra o emulador.
// Cobre a garantia central documentada nas rules: isolamento multi-tenant (ninguém lê/escreve
// dado de uma empresa que não é a sua), incluindo o caso de um Gestor que administra mais de uma
// empresa (`empresaIds: List<String>`) e o caso de um Gestor criando a conta de um colaborador
// direto (sem fluxo de convite por e-mail).
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
const EMPRESA_C = "empresa-c";

const GESTOR_A = { uid: "gestor-a", email: "gestor-a@example.com" };
const COLAB_A = { uid: "colab-a", email: "colab-a@example.com" };
const GESTOR_B = { uid: "gestor-b", email: "gestor-b@example.com" };
// Gestor multi-empresa: administra A e C (mas não B) — cobre o cenário de "Minhas Empresas".
const GESTOR_MULTI = { uid: "gestor-multi", email: "gestor-multi@example.com" };

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
  // já com três tenants distintos e seus colaboradores.
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, "colaboradores", GESTOR_A.uid), {
      empresaIds: [EMPRESA_A],
      nome: "Gestor A",
      email: GESTOR_A.email,
      ativo: true,
      ehGestor: true,
    });
    await setDoc(doc(db, "colaboradores", COLAB_A.uid), {
      empresaIds: [EMPRESA_A],
      nome: "Colaborador A",
      email: COLAB_A.email,
      ativo: true,
      ehGestor: false,
    });
    await setDoc(doc(db, "colaboradores", GESTOR_B.uid), {
      empresaIds: [EMPRESA_B],
      nome: "Gestor B",
      email: GESTOR_B.email,
      ativo: true,
      ehGestor: true,
    });
    await setDoc(doc(db, "colaboradores", GESTOR_MULTI.uid), {
      empresaIds: [EMPRESA_A, EMPRESA_C],
      nome: "Gestor Multi",
      email: GESTOR_MULTI.email,
      ativo: true,
      ehGestor: true,
    });
    await setDoc(doc(db, "empresas", EMPRESA_A), { nome: "Empresa A" });
    await setDoc(doc(db, "empresas", EMPRESA_B), { nome: "Empresa B" });
    await setDoc(doc(db, "empresas", EMPRESA_C), { nome: "Empresa C" });
    await setDoc(doc(db, "empresas", EMPRESA_A, "contas", "conta-1"), {
      nome: "Caixa A",
      tipo: "CAIXA",
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

// --- Gestor multi-empresa ---------------------------------------------------------------------

test("Gestor multi-empresa lê e escreve nas duas empresas que administra", async () => {
  await assertSucceeds(getDoc(doc(as(GESTOR_MULTI), "empresas", EMPRESA_A)));
  await assertSucceeds(getDoc(doc(as(GESTOR_MULTI), "empresas", EMPRESA_C)));
  await assertSucceeds(updateDoc(doc(as(GESTOR_MULTI), "empresas", EMPRESA_C), { nome: "Empresa C Ltda" }));
});

test("Gestor multi-empresa não lê a empresa B, que não administra", async () => {
  await assertFails(getDoc(doc(as(GESTOR_MULTI), "empresas", EMPRESA_B)));
});

test("colaborador da empresa A lê o doc de um Gestor multi-empresa (empresas em comum)", async () => {
  await assertSucceeds(getDoc(doc(as(COLAB_A), "colaboradores", GESTOR_MULTI.uid)));
});

// --- colaboradores/{uid}: autocadastro ---------------------------------------------------------

test("um uid recém-autenticado consegue criar o próprio doc de colaborador (auto-cadastro)", async () => {
  await assertSucceeds(
    setDoc(doc(as({ uid: "novo-uid", email: "novo@example.com" }), "colaboradores", "novo-uid"), {
      empresaIds: [EMPRESA_A],
      nome: "Novo",
      email: "novo@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

// --- colaboradores/{uid}: Gestor cria colaborador direto (sem convite) -------------------------

test("Gestor cria a conta de um colaborador novo direto na própria empresa", async () => {
  await assertSucceeds(
    setDoc(doc(as(GESTOR_A), "colaboradores", "colab-novo"), {
      empresaIds: [EMPRESA_A],
      nome: "Colaborador Novo",
      email: "colaborador.novo@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("Gestor multi-empresa cria colaborador em qualquer uma das empresas que administra", async () => {
  await assertSucceeds(
    setDoc(doc(as(GESTOR_MULTI), "colaboradores", "colab-empresa-c"), {
      empresaIds: [EMPRESA_C],
      nome: "Colaborador da Empresa C",
      email: "colab.c@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("Gestor não cria colaborador numa empresa que não administra", async () => {
  await assertFails(
    setDoc(doc(as(GESTOR_A), "colaboradores", "colab-forjado"), {
      empresaIds: [EMPRESA_B],
      nome: "Forjado",
      email: "forjado@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("Gestor não cria colaborador já declarando mais de uma empresa", async () => {
  await assertFails(
    setDoc(doc(as(GESTOR_MULTI), "colaboradores", "colab-duas-empresas"), {
      empresaIds: [EMPRESA_A, EMPRESA_C],
      nome: "Suspeito",
      email: "suspeito@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("colaborador comum (não-Gestor) não cria a conta de outro colaborador", async () => {
  await assertFails(
    setDoc(doc(as(COLAB_A), "colaboradores", "colab-via-nao-gestor"), {
      empresaIds: [EMPRESA_A],
      nome: "Forjado",
      email: "x@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

test("um uid não consegue criar o doc de colaborador de outro uid sem ser Gestor", async () => {
  await assertFails(
    setDoc(doc(as(COLAB_A), "colaboradores", "outro-uid"), {
      empresaIds: [EMPRESA_A],
      nome: "Forjado",
      email: "x@example.com",
      ativo: true,
      ehGestor: false,
    }),
  );
});

// --- colaboradores/{uid}: leitura/atualização ---------------------------------------------------

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

// --- Default deny -----------------------------------------------------------------------------

test("coleção de nível raiz não declarada nas rules é negada por padrão", async () => {
  await assertFails(getDoc(doc(as(GESTOR_A), "algumaColecaoNaoDeclarada", "doc-1")));
});

test("coleção convites (removida) não é mais acessível", async () => {
  await assertFails(getDoc(doc(as(GESTOR_A), "convites", "qualquer-id")));
  await assertFails(
    setDoc(doc(as(GESTOR_A), "convites", "qualquer-id"), { empresaIds: [EMPRESA_A] }),
  );
});
