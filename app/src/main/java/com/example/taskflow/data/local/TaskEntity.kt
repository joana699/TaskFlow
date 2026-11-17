package com.example.taskflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Estados posibles de una automatización (Reto 4). */
object TaskState {
    const val ACTIVA = "ACTIVA"
    const val PAUSADA = "PAUSADA"
    const val COMPLETADA = "COMPLETADA"
    const val ERROR = "ERROR"
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val repeatIntervalMinutes: Long,
    val createdAt: Long = System.currentTimeMillis(),

    // Tipo de ejecución: false = periódica, true = una sola vez
    val oneTime: Boolean = false,
    // Reto 4: ACTIVA / PAUSADA / COMPLETADA / ERROR
    val state: String = TaskState.ACTIVA,
    // Retos 1, 2 y 6: condiciones de ejecución
    val requiresNetwork: Boolean = false,
    val requiresCharging: Boolean = false,
    val requiresBatteryNotLow: Boolean = true
)
