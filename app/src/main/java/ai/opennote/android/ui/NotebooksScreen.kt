package ai.opennote.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.opennote.android.data.NotebookDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebooksScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onOpen: (NotebookDto) -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadNotebooks() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notebooks") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = { vm.loadNotebooks() }) { Text("Refresh") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) { Text("New") }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
        ) {
            if (vm.loadingNotebooks) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            vm.notebooksError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp),
                )
            }
            if (!vm.loadingNotebooks && vm.notebooks.isEmpty() && vm.notebooksError == null) {
                Text(
                    text = "No notebooks yet. Create one with the New button.",
                    modifier = Modifier.padding(16.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                items(vm.notebooks, key = { it.id }) { notebook ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpen(notebook) },
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = notebook.name.ifBlank { "Untitled notebook" },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (notebook.description.isNotBlank()) {
                                Text(
                                    text = notebook.description,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                text = "${notebook.sourceCount} sources - ${notebook.noteCount} notes",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New notebook") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.createNotebook(newName) {
                        newName = ""
                        showCreate = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            },
        )
    }
}
