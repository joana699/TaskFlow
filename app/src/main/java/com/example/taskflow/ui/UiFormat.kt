package com.example.taskflow.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.taskflow.R
import com.example.taskflow.data.local.ExecutionStatus
import com.example.taskflow.data.local.TaskEntity
import com.example.taskflow.data.local.TaskState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UiFormat {

    private fun duration(minutes: Long): String = when {
        minutes >= 1440 && minutes % 1440 == 0L -> "${minutes / 1440} día(s)"
        minutes >= 60 && minutes % 60 == 0L -> "${minutes / 60} hora(s)"
        else -> "$minutes minutos"
    }

    fun interval(task: TaskEntity): String =
        if (task.oneTime) "Una sola vez, en ${duration(task.repeatIntervalMinutes)}"
        else "Cada ${duration(task.repeatIntervalMinutes)}"

    fun date(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

    fun stateColor(context: Context, state: String): Int = ContextCompat.getColor(
        context,
        when (state) {
            TaskState.ACTIVA, ExecutionStatus.COMPLETADA -> R.color.state_active
            TaskState.PAUSADA -> R.color.state_paused
            ExecutionStatus.REINTENTO -> R.color.state_retry
            else -> R.color.state_error
        }
    )
}
