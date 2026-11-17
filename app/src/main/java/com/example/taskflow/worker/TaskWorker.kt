package com.example.taskflow.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskflow.data.local.DatabaseProvider
import com.example.taskflow.data.local.ExecutionStatus
import com.example.taskflow.data.local.TaskExecutionEntity
import com.example.taskflow.data.local.TaskState
import com.example.taskflow.notification.NotificationHelper
import java.io.IOException
import kotlin.random.Random

class TaskWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt(TaskScheduler.KEY_TASK_ID, -1)
        val taskTitle = inputData.getString(TaskScheduler.KEY_TASK_TITLE) ?: "Tarea"

        val db = DatabaseProvider.getDatabase(applicationContext)
        val taskDao = db.taskDao()
        val executionDao = db.executionDao()

        return try {
            simulateWork()

            NotificationHelper.showNotification(applicationContext, taskId, taskTitle)

            // Reto 3: registrar la ejecución en el historial
            executionDao.insert(
                TaskExecutionEntity(
                    taskId = taskId,
                    executedAt = System.currentTimeMillis(),
                    status = ExecutionStatus.COMPLETADA
                )
            )

            // Reto 4: actualizar el estado de la tarea
            val task = taskDao.getById(taskId)
            if (task != null) {
                when {
                    task.oneTime ->
                        taskDao.update(task.copy(state = TaskState.COMPLETADA, enabled = false))
                    task.state == TaskState.ERROR ->
                        taskDao.update(task.copy(state = TaskState.ACTIVA))
                }
            }
            Result.success()
        } catch (e: IOException) {
            // Reto 5: error temporal -> reintentar con backoff; si se agotan los intentos, ERROR
            if (runAttemptCount < MAX_ATTEMPTS) {
                executionDao.insert(
                    TaskExecutionEntity(
                        taskId = taskId,
                        executedAt = System.currentTimeMillis(),
                        status = ExecutionStatus.REINTENTO
                    )
                )
                Result.retry()
            } else {
                executionDao.insert(
                    TaskExecutionEntity(
                        taskId = taskId,
                        executedAt = System.currentTimeMillis(),
                        status = ExecutionStatus.ERROR
                    )
                )
                taskDao.getById(taskId)?.let {
                    taskDao.update(it.copy(state = TaskState.ERROR))
                }
                Result.failure()
            }
        }
    }

    /** Simula una operación (p. ej. sincronizar) que a veces falla por un error temporal. */
    private fun simulateWork() {
        if (Random.nextInt(100) < FAILURE_PERCENT) {
            throw IOException("Fallo temporal simulado")
        }
    }

    companion object {
        /** Máximo de intentos antes de marcar la tarea en ERROR. */
        const val MAX_ATTEMPTS = 3

        /** Probabilidad (0-100) de fallo simulado. Ponla en 0 para evitar fallos o en 100 para forzarlos. */
        const val FAILURE_PERCENT = 20
    }
}
