package br.com.tiago.obramaster.ui.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginForm(
    erro: String?,
    autenticando: Boolean,
    onEntrar: (email: String, senha: String) -> Unit,
    onEntrarComGoogle: () -> Unit,
    onCriarContaComConvite: (nome: String, email: String, senha: String) -> Unit,
) {
    var modoCriarConta by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ObraMaster", style = MaterialTheme.typography.titleLarge)

        Column(
            modifier = Modifier.widthIn(max = 420.dp).padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (modoCriarConta) {
                Text(
                    "Recebeu um convite de alguém da equipe? Preencha com o mesmo e-mail do convite.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = erro != null,
                modifier = Modifier.fillMaxWidth(),
            )
            if (erro != null) {
                Text(erro, color = MaterialTheme.colorScheme.error)
            }

            if (modoCriarConta) {
                Button(
                    onClick = { onCriarContaComConvite(nome, email, senha) },
                    enabled = nome.isNotBlank() && email.isNotBlank() && senha.isNotBlank() && !autenticando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (autenticando) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Aceitar convite e entrar")
                }
            } else {
                Button(
                    onClick = { onEntrar(email, senha) },
                    enabled = email.isNotBlank() && senha.isNotBlank() && !autenticando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (autenticando) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Entrar")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            OutlinedButton(
                onClick = onEntrarComGoogle,
                enabled = !autenticando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Entrar com Google")
            }

            TextButton(onClick = { modoCriarConta = !modoCriarConta }, modifier = Modifier.fillMaxWidth()) {
                Text(if (modoCriarConta) "Já tenho conta" else "Recebi um convite, criar minha conta")
            }
        }
    }
}
