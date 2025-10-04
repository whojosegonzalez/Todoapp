package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.mutableStateListOf
import com.example.todoapp.model.TodoItem


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoApp()
            }
        }
    }
}

@Composable
fun TodoApp() {
    // Saver for a list of TodoItem -> flat Any list, and back again
    val todoListSaver = listSaver<MutableList<TodoItem>, Any>(
        save = { list -> list.flatMap { listOf(it.id, it.title, it.isDone) } },
        restore = { flat ->
            val restored = mutableStateListOf<TodoItem>()
            flat.chunked(3).forEach { chunk ->
                restored += TodoItem(
                    id = chunk[0] as Long,
                    title = chunk[1] as String,
                    isDone = chunk[2] as Boolean
                )
            }
            restored
        }
    )

    val items = rememberSaveable(saver = todoListSaver) {
        mutableStateListOf<TodoItem>()
    }

    Column(Modifier.padding(16.dp)) {
        Text("TODO List", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("You have ${items.size} task(s)")
    }
}
