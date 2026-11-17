package com.example.taskflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskExecutionDao {

    @Query(
        """
        SELECT e.id AS id, e.taskId AS taskId, e.executedAt AS executedAt,
               e.status AS status, t.title AS taskTitle
        FROM task_executions e
        LEFT JOIN tasks t ON t.id = e.taskId
        ORDER BY e.executedAt DESC
        """
    )
    fun getHistory(): Flow<List<ExecutionItem>>

    @Insert
    suspend fun insert(execution: TaskExecutionEntity)

    @Query("DELETE FROM task_executions")
    suspend fun clear()
}
