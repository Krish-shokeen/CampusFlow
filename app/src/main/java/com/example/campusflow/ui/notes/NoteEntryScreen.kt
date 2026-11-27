package com.example.campusflow.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Note
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
    noteId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf(LocalDateTime.now()) }
    
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNote(noteId)?.let { note ->
                title = note.title
                content = note.content
                createdAt = note.createdAt
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(onClick = {
                        val note = Note(
                            id = noteId ?: 0,
                            subjectId = null,
                            title = title,
                            content = content,
                            createdAt = createdAt,
                            updatedAt = LocalDateTime.now()
                        )
                        coroutineScope.launch {
                            if (noteId == null) viewModel.addNote(note) else viewModel.updateNote(note)
                            navigateBack()
                        }
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}