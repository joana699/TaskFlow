package com.example.taskflow.data.repository

import android.content.Context
import com.example.taskflow.data.local.TaskDao
import com.example.taskflow.data.local.TaskEntity
import com.example.taskflow.data.local.TaskExecutionDao
import com.example.taskflow.data.local.TaskState
import com.example.taskflow.worker.TaskScheduler

/**
 * Única puerta de entrada a los datos y a la automatización:
 * la interfaz no sabe si las tareas viven en Room ni cómo se programa WorkManager.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val executionDao: TaskExecutionDao,
    private val appContext: Context
) {
    val tasks = taskDao.getAllTasks()
    val history = executionDao.getHistory()

    suspend fun getTask(id: Int): TaskEntity? = taskDao.getById(id)

    suspend fun insert(task: TaskEntity) {
        val newId = taskDao.insert(task).toInt()
        if (task.enabled) {
            TaskScheduler.scheduleTask(appContext, task.copy(id = newId))
        }
    }

    suspend fun update(task: TaskEntity) {
        taskDao.update(task)
        // Reprogramar: se cancela el trabajo anterior y se crea otro con los datos nuevos
        TaskScheduler.cancelTask(appContext, task.id)
        if (task.enabled) {
            TaskScheduler.scheduleTask(appContext, task)
        }
    }

    /** Activar / desactivar: cambia el estado y programa o cancela el trabajo. */
    suspend fun setEnabled(task: TaskEntity, enabled: Boolean) {
        update(
            task.copy(
                enabled = enabled,
                state = if (enabled) TaskState.ACTIVA else TaskState.PAUSADA
            )
        )
    }

    suspend fun delete(task: TaskEntity) {
        TaskScheduler.cancelTask(appContext, task.id)
        taskDao.delete(task)
    }

    suspend fun clearHistory() = executionDao.clear()
}
