package com.example.taskflow.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Reto 3: historial de ejecuciones (tabla TASK_EXECUTIONS). */
@Entity(tableName = "task_executions", indices = [Index("taskId")])
data class TaskExecutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val executedAt: Long,
    val status: String
)

object ExecutionStatus {
    const val COMPLETADA = "COMPLETADA"
    const val REINTENTO = "REINTENTO"
    const val ERROR = "ERROR"
}

/** Resultado del JOIN entre task_executions y tasks, para mostrar en el historial. */
data class ExecutionItem(
    val id: Int,
    val taskId: Int,
    val executedAt: Long,
    val status: String,
    val taskTitle: String?
)
