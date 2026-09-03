package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, id DESC")
    fun getTasksForDate(date: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC, id ASC")
    fun getTasksForMonth(monthPrefix: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks ORDER BY date DESC, id DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAll(): Int

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}
