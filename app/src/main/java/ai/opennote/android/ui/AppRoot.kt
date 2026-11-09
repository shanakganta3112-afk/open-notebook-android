package ai.opennote.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.opennote.android.data.NotebookDto

private sealed interface Screen {
    data object Connect : Screen
    data object Notebooks : Screen
    data class Detail(val notebook: NotebookDto) : Screen
}

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Connect) }

    when (val current = screen) {
        is Screen.Connect -> ConnectScreen(
            vm = vm,
            onContinue = { screen = Screen.Notebooks },
        )

        is Screen.Notebooks -> NotebooksScreen(
            vm = vm,
            onBack = { screen = Screen.Connect },
            onOpen = { notebook ->
                vm.openNotebook(notebook)
                screen = Screen.Detail(notebook)
            },
        )

        is Screen.Detail -> NotebookDetailScreen(
            vm = vm,
            notebook = current.notebook,
            onBack = { screen = Screen.Notebooks },
        )
    }
}
