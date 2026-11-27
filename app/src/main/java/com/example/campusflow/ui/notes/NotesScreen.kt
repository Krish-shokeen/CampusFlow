package com.example.campusflow.ui.notes

<<<<<<< Updated upstream
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
=======
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
>>>>>>> Stashed changes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
<<<<<<< Updated upstream
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Note
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.NotesViewModel

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editingNote = null; showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create note")
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Notes", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search notes") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            if (state.notes.isEmpty()) {
                Text(
                    if (query.isBlank()) "Your saved notes will appear here."
                    else "No notes match your search.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.notes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            onClick = { editingNote = note; showEditor = true }
                        ) {
                            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 4
                                    )
                                    note.subjectId?.let { subjectId ->
                                        state.subjects.find { it.id == subjectId }?.subjectName?.let {
                                            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                IconButton(onClick = { editingNote = note; showEditor = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit note")
                                }
                                IconButton(onClick = { deletingNote = note }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = MaterialTheme.colorScheme.error)
                                }
=======
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusflow.data.entities.Note
import com.example.campusflow.ui.components.EmptyState
import com.example.campusflow.utils.DateUtils
import com.example.campusflow.viewmodel.AppViewModelProvider
import com.example.campusflow.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToNoteEntry: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToNoteEntry(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (uiState.notes.isEmpty()) {
                EmptyState(
                    title = "No Notes",
                    message = "Write down important topics, ideas, or to-dos.",
                    icon = Icons.Default.MenuBook
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToNoteEntry(note.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.title.ifBlank { "Untitled Note" },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { noteToDelete = note },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Updated: ${DateUtils.formatDateTime(note.updatedAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
>>>>>>> Stashed changes
                            }
                        }
                    }
                }
            }
        }
<<<<<<< Updated upstream
    }

    if (showEditor) {
        NoteEditorDialog(
            note = editingNote,
            onDismiss = { showEditor = false },
            onSave = { note -> viewModel.saveNote(note); showEditor = false }
        )
    }
    deletingNote?.let { note ->
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            title = { Text("Delete note?") },
            text = { Text("This will permanently delete “${note.title}”.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNote(note); deletingNote = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingNote = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun NoteEditorDialog(note: Note?, onDismiss: () -> Unit, onSave: (Note) -> Unit) {
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var content by remember(note?.id) { mutableStateOf(note?.content.orEmpty()) }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null) },
        title = { Text(if (note == null) "New note" else "Edit note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Title") },
                    isError = titleError,
                    supportingText = if (titleError) ({ Text("Enter a title") }) else null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note") },
                    minLines = 4,
                    maxLines = 8
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) titleError = true
                else onSave(
                    (note ?: Note(subjectId = null, title = "", content = ""))
                        .copy(title = title.trim(), content = content.trim())
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
=======

        if (noteToDelete != null) {
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note?") },
                confirmButton = {
                    TextButton(onClick = { noteToDelete?.let { viewModel.deleteNote(it) }; noteToDelete = null }) { 
                        Text("Delete", color = MaterialTheme.colorScheme.error) 
                    }
                },
                dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Cancel") } }
            )
        }
    }
}
>>>>>>> Stashed changes
