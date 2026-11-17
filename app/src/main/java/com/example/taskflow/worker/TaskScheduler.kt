package com.example.taskflow.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.taskflow.data.local.TaskEntity
import java.util.concurrent.TimeUnit

object TaskScheduler {

    const val KEY_TASK_ID = "TASK_ID"
    const val KEY_TASK_TITLE = "TASK_TITLE"

    /** WorkManager no permite periodos menores a 15 minutos. */
    const val MIN_PERIODIC_MINUTES = 15L

    private fun workName(taskId: Int) = "task_$taskId"

    fun scheduleTask(context: Context, task: TaskEntity) {
        val inputData = workDataOf(
            KEY_TASK_ID to task.id,
            KEY_TASK_TITLE to task.title
        )
        val constraints = task.toRule().toConstraints()
        val workManager = WorkManager.getInstance(context)

        if (task.oneTime) {
            // Ejecución única: se ejecuta una vez después de N minutos (si se cumplen las condiciones)
            val request = OneTimeWorkRequestBuilder<TaskWorker>()
                .setInitialDelay(task.repeatIntervalMinutes, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniqueWork(
                workName(task.id),
                ExistingWorkPolicy.REPLACE,
                request
            )
        } else {
            val interval = task.repeatIntervalMinutes.coerceAtLeast(MIN_PERIODIC_MINUTES)
            val request = PeriodicWorkRequestBuilder<TaskWorker>(
                interval,
                TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniquePeriodicWork(
                workName(task.id),
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    fun cancelTask(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}
