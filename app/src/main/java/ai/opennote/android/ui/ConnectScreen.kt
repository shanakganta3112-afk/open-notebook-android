package ai.opennote.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(vm: AppViewModel, onContinue: () -> Unit) {
    var url by remember { mutableStateOf(vm.settings.baseUrl) }
    var password by remember { mutableStateOf(vm.settings.password) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Open Notebook") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Connect to your Open Notebook server",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Open Notebook is a server: run it on your computer or a VPS (docker compose up -d), " +
                    "then point this app at it. Port 5055 is the REST API.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Server URL") },
                singleLine = true,
                placeholder = { Text("http://192.168.1.10:5055") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Emulator tip: 10.0.2.2 is your computer's localhost.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (OPEN_NOTEBOOK_PASSWORD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    vm.apply(url, password)
                    vm.testConnection()
                },
                enabled = !vm.connecting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (vm.connecting) "Testing..." else "Save and test connection")
            }
            OutlinedButton(
                onClick = {
                    vm.apply(url, password)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue to notebooks")
            }
            vm.connectionMessage?.let { message ->
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
