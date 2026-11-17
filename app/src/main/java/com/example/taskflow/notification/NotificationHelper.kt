package com.example.taskflow.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.taskflow.R
import com.example.taskflow.ui.main.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "TASK_CHANNEL"

    /** Desde Android 8.0 (API 26) toda notificación debe pertenecer a un canal. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tareas automatizadas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de las tareas ejecutadas por TaskFlow"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, taskId: Int, title: String) {
        // Android 13+: sin el permiso, la notificación simplemente no se puede mostrar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("TaskFlow")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        // Un id distinto por tarea: cada tarea mantiene su propia notificación
        NotificationManagerCompat.from(context).notify(taskId, notification)
    }
}
