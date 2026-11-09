package ai.opennote.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.opennote.android.data.NotebookDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookDetailScreen(
    vm: AppViewModel,
    notebook: NotebookDto,
    onBack: () -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    var showAddNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    val tabs = listOf("Sources", "Notes", "Search")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(notebook.name.ifBlank { "Notebook" }) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = { showAddNote = true }) { Text("Add note") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(title) },
                    )
                }
            }

            vm.detailError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp),
                )
            }

            when (tab) {
                0 -> {
                    if (vm.loadingDetail) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                    if (vm.sources.isEmpty() && !vm.loadingDetail) {
                        Text("No sources in this notebook yet.", modifier = Modifier.padding(16.dp))
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    ) {
                        items(vm.sources, key = { it.id }) { source ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = source.title ?: "Untitled source",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    val status = source.status ?: if (source.embedded) "ready" else "processing"
                                    Text(
                                        text = "$status - ${source.insightsCount} insights",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (vm.notes.isEmpty() && !vm.loadingDetail) {
                        Text("No notes in this notebook yet.", modifier = Modifier.padding(16.dp))
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    ) {
                        items(vm.notes, key = { it.id }) { note ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = note.title ?: "Note",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = note.content ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Search this notebook") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { vm.search(query) },
                            enabled = !vm.searching,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (vm.searching) "Searching..." else "Search")
                        }
                        vm.searchError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(vm.searchHits) { hit ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = hit.title,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        if (hit.kind.isNotBlank()) {
                                            Text(
                                                text = hit.kind,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                        Text(
                                            text = hit.snippet.take(400),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNote) {
        AlertDialog(
            onDismissRequest = { showAddNote = false },
            title = { Text("Add note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addNote(noteText) {
                        noteText = ""
                        showAddNote = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddNote = false }) { Text("Cancel") }
            },
        )
    }
}
