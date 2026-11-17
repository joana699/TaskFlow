package com.example.taskflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TaskExecutionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun executionDao(): TaskExecutionDao
}

/** Singleton: una sola instancia de la base de datos para toda la app (también la usa el Worker). */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: TaskDatabase? = null

    fun getDatabase(context: Context): TaskDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java,
                "taskflow.db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
