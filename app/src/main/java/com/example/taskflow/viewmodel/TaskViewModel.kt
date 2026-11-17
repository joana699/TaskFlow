package com.example.taskflow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskflow.data.local.DatabaseProvider
import com.example.taskflow.data.local.TaskEntity
import com.example.taskflow.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks = repository.tasks
    val history = repository.history

    suspend fun getTask(id: Int): TaskEntity? = repository.getTask(id)

    /** Crea (id = 0) o edita una tarea. */
    fun save(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0) repository.insert(task) else repository.update(task)
        }
    }

    fun setEnabled(task: TaskEntity, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(task, enabled) }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = DatabaseProvider.getDatabase(appContext)
            val repository = TaskRepository(db.taskDao(), db.executionDao(), appContext)
            return TaskViewModel(repository) as T
        }
    }
}
