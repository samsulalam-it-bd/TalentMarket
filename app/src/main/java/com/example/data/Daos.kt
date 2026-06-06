package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobs(): Flow<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    @androidx.room.Update
    suspend fun updateJob(job: Job)

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getJobById(id: String): Flow<Job?>

    @Query("SELECT * FROM jobs WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteJobs(): Flow<List<Job>>

    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()
    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJobById(id: String)
}

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY timestamp DESC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @androidx.room.Update
    suspend fun updateWorker(worker: Worker)

    @Query("SELECT * FROM workers WHERE id = :id")
    fun getWorkerById(id: String): Flow<Worker?>

    @Query("SELECT * FROM workers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWorkers(): Flow<List<Worker>>

    @Query("DELETE FROM workers")
    suspend fun deleteAllWorkers()

    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorkerById(id: String)
}
