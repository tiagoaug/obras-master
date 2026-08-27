package br.com.tiago.obramaster.ui.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    onEntrar: (login: String, senha: String, manterConectado: Boolean) -> Unit,
) {
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var manterConectado by remember { mutableStateOf(true) }

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
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login") },
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = manterConectado, onCheckedChange = { manterConectado = it })
                Text("Manter conectado")
            }

            Button(
                onClick = { onEntrar(login, senha, manterConectado) },
                enabled = login.isNotBlank() && senha.isNotBlank() && !autenticando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (autenticando) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text("Entrar")
                }
            }
        }
    }
}
