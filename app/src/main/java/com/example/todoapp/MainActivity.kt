package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.todoapp.model.TodoItem
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TodoApp() } }
    }
}

@Composable
fun TodoApp() {
    val todoListSaver = listSaver<MutableList<TodoItem>, Any>(
        save = { list -> list.flatMap { listOf(it.id, it.title, it.isDone) } },
        restore = { flat ->
            val restored = mutableStateListOf<TodoItem>()
            flat.chunked(3).forEach { c ->
                restored += TodoItem(c[0] as Long, c[1] as String, c[2] as Boolean)
            }
            restored
        }
    )
    val items = rememberSaveable(saver = todoListSaver) { mutableStateListOf<TodoItem>() }
    var nextId by rememberSaveable { mutableLongStateOf(1L) }

    // --- Event handlers (state hoisted here) ---
    fun addItem(label: String) {
        val title = label.trim()
        if (title.isNotEmpty()) {
            items += TodoItem(id = nextId++, title = title, isDone = false)
        }
    }
    fun toggleItem(id: Long, checked: Boolean) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx != -1) items[idx] = items[idx].copy(isDone = checked)
    }
    fun deleteItem(id: Long) {
        items.removeAll { it.id == id }
    }

    TodoScreen(
        items = items,
        onAdd = ::addItem,
        onToggle = ::toggleItem,
        onDelete = ::deleteItem
    )
}

@Composable
fun TodoScreen(
    items: List<TodoItem>,
    onAdd: (String) -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    val (active, completed) = items.partition { !it.isDone }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("TODO List", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        AddRow(onAdd = onAdd)

        Spacer(Modifier.height(16.dp))

        // Active section
        if (active.isNotEmpty()) {
            Text("Items", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            TodoList(active, onToggle, onDelete)
            Spacer(Modifier.height(16.dp))
        } else {
            Text("No items yet", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
        }

        // Completed section
        if (completed.isNotEmpty()) {
            Text("Completed Items", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            TodoList(completed, onToggle, onDelete)
        }
    }
}

@Composable
fun AddRow(onAdd: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                if (showError) showError = false
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Enter the task name") },
            singleLine = true,
            isError = showError,
            supportingText = { if (showError) Text("Please enter a task") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (text.trim().isEmpty()) showError = true else {
                        onAdd(text); text = ""
                    }
                }
            )
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            if (text.trim().isEmpty()) showError = true else {
                onAdd(text); text = ""
            }
        }) { Text("Add") }
    }
}

@Composable
fun TodoList(
    data: List<TodoItem>,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(data, key = { it.id }) { item ->
            TodoRow(item, onToggle, onDelete)
        }
    }
}

@Composable
fun TodoRow(
    item: TodoItem,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.title, Modifier.weight(1f))
            Checkbox(checked = item.isDone, onCheckedChange = { onToggle(item.id, it) })
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Filled.Close, contentDescription = "Delete")
            }
        }
    }
}
