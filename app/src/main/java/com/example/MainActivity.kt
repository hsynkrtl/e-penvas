package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saber.data.AppDatabase
import com.example.saber.data.NotesRepository
import com.example.saber.ui.DashboardScreen
import com.example.saber.ui.DashboardViewModel
import com.example.saber.ui.EditorScreen
import com.example.saber.ui.EditorViewModel
import com.example.ui.theme.SaberNotesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = NotesRepository(database.notebookDao(), database.pageDao())

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                        DashboardViewModel(repository) as T
                    }
                    modelClass.isAssignableFrom(EditorViewModel::class.java) -> {
                        EditorViewModel(repository) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        setContent {
            SaberNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SaberApp(factory = factory)
                }
            }
        }
    }
}

@Composable
fun SaberApp(factory: ViewModelProvider.Factory) {
    var selectedNotebookId by remember { mutableStateOf<Long?>(null) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val editorViewModel: EditorViewModel = viewModel(factory = factory)

    val currentNotebookId = selectedNotebookId
    if (currentNotebookId == null) {
        DashboardScreen(
            viewModel = dashboardViewModel,
            onOpenNotebook = { id ->
                selectedNotebookId = id
            }
        )
    } else {
        EditorScreen(
            notebookId = currentNotebookId,
            viewModel = editorViewModel,
            onNavigateBack = {
                selectedNotebookId = null
            }
        )
    }
}
