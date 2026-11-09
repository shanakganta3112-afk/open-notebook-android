package ai.opennote.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ai.opennote.android.ui.AppRoot
import ai.opennote.android.ui.OpenNotebookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenNotebookTheme {
                AppRoot()
            }
        }
    }
}
