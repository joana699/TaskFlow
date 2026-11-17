package com.example.taskflow.worker

import androidx.work.Constraints
import androidx.work.NetworkType
import com.example.taskflow.data.local.TaskEntity

/**
 * Reto 6: una regla "SI ... Y ... ENTONCES ejecutar tarea" representada como objeto Kotlin.
 * Se traduce directamente a las Constraints de WorkManager.
 */
data class AutomationRule(
    val requiresInternet: Boolean = false,
    val batteryNotLow: Boolean = true,
    val requiresCharging: Boolean = false
) {

    fun toConstraints(): Constraints {
        val builder = Constraints.Builder()
            .setRequiresBatteryNotLow(batteryNotLow)
            .setRequiresCharging(requiresCharging)
        if (requiresInternet) {
            builder.setRequiredNetworkType(NetworkType.CONNECTED)
        }
        return builder.build()
    }

    /** Texto legible, p. ej.: "SI hay Internet Y batería no está baja ENTONCES ejecutar". */
    fun describe(): String {
        val conditions = buildList {
            if (requiresInternet) add("hay Internet")
            if (batteryNotLow) add("batería no está baja")
            if (requiresCharging) add("el teléfono está cargando")
        }
        return if (conditions.isEmpty()) {
            "Sin condiciones: ejecutar siempre"
        } else {
            "SI " + conditions.joinToString(" Y ") + " ENTONCES ejecutar"
        }
    }
}

fun TaskEntity.toRule() = AutomationRule(
    requiresInternet = requiresNetwork,
    batteryNotLow = requiresBatteryNotLow,
    requiresCharging = requiresCharging
)
