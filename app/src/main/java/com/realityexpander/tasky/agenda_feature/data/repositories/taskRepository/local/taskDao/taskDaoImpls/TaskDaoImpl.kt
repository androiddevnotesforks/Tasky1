package com.realityexpander.tasky.agenda_feature.data.repositories.taskRepository.local.taskDao.taskDaoImpls

import androidx.room.*
import com.realityexpander.tasky.agenda_feature.common.util.TaskId
import com.realityexpander.tasky.agenda_feature.data.repositories.taskRepository.local.ITaskDao
import com.realityexpander.tasky.agenda_feature.data.repositories.taskRepository.local.entities.TaskEntity
import com.realityexpander.tasky.core.util.DAY_IN_SECONDS
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime


@Dao
interface TaskDaoImpl : ITaskDao {

    // • CREATE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun createTask(task: TaskEntity)


    // • UPSERT

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTask(task: TaskEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update2Task(task: TaskEntity)

    @Transaction
    override fun upsertTask(task: TaskEntity) {
        val id = insertTask(task)
        if (id == -1L) {
            update2Task(task)
        }
    }


    // • READ

    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")  // only returns the tasks that are *NOT* marked as deleted.
    override suspend fun getTaskById(taskId: TaskId): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")  // only returns the tasks that are *NOT* marked as deleted
    override suspend fun getTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks")                      // returns all tasks (marked deleted or not)
    override suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")  // only returns the tasks that are *NOT* marked as deleted.
    override fun getTasksFlow(): Flow<List<TaskEntity>>

    @Query(getTasksForDayQuery)
    override suspend fun getTasksForDay(zonedDateTime: ZonedDateTime): List<TaskEntity>  // note: ZonedDateTime gets converted to UTC EpochSeconds for storage in the DB.

    @Query(getTasksForDayQuery)
    override fun getTasksForDayFlow(zonedDateTime: ZonedDateTime): Flow<List<TaskEntity>>  // note: ZonedDateTime gets converted to UTC EpochSeconds for storage in the DB.


    // • UPDATE

    @Update
    override suspend fun updateTask(task: TaskEntity): Int


    // • DELETE

    @Query("UPDATE tasks SET isDeleted = 1 WHERE id = :taskId")
    override suspend fun markTaskDeletedById(taskId: TaskId): Int   // only marks the task as deleted.

    @Query("SELECT id FROM tasks WHERE isDeleted = 1")
    override suspend fun getMarkedDeletedTaskIds(): List<TaskId>

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    override suspend fun deleteFinallyByTaskIds(taskIds: List<TaskId>): Int  // completely deletes the tasks.

    @Delete
    override suspend fun deleteTask(task: TaskEntity): Int  // completely deletes the task.

    @Query("DELETE FROM tasks")
    override suspend fun clearAllTasks(): Int  // completely deletes all tasks.

    @Query(deleteTasksForDayQuery)
    override suspend fun clearAllTasksForDay(zonedDateTime: ZonedDateTime): Int // completely deletes all UNDELETED tasks for the given day.

    companion object {

        const val getTasksForDayQuery =
            """
            SELECT * FROM tasks WHERE isDeleted = 0 
                AND 
                    ( ( `time` >= :zonedDateTime) AND (`time` < :zonedDateTime + ${DAY_IN_SECONDS}) ) -- task starts this day
                
            """

        const val deleteTasksForDayQuery =
            """
            DELETE FROM tasks WHERE isDeleted = 0 
                AND 
                    ( ( `time` >= :zonedDateTime) AND (`time` < :zonedDateTime + ${DAY_IN_SECONDS}) ) -- `time` start this today
            """
    }
}