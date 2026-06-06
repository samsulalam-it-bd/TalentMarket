package com.example.data

import kotlinx.coroutines.flow.Flow

class TalentRepository(private val db: TalentDatabase) {
    val allJobs: Flow<List<Job>> = db.jobDao().getAllJobs()
    val allWorkers: Flow<List<Worker>> = db.workerDao().getAllWorkers()

    suspend fun insertJob(job: Job) = db.jobDao().insertJob(job)
    suspend fun insertWorker(worker: Worker) = db.workerDao().insertWorker(worker)
    
    suspend fun updateJob(job: Job) = db.jobDao().updateJob(job)
    suspend fun updateWorker(worker: Worker) = db.workerDao().updateWorker(worker)

    suspend fun clearAllJobs() = db.jobDao().deleteAllJobs()
    suspend fun clearAllWorkers() = db.workerDao().deleteAllWorkers()

    suspend fun deleteJobById(id: String) = db.jobDao().deleteJobById(id)
    suspend fun deleteWorkerById(id: String) = db.workerDao().deleteWorkerById(id)

    fun getJobById(id: String) = db.jobDao().getJobById(id)
    fun getWorkerById(id: String) = db.workerDao().getWorkerById(id)

    val favoriteJobs: Flow<List<Job>> = db.jobDao().getFavoriteJobs()
    val favoriteWorkers: Flow<List<Worker>> = db.workerDao().getFavoriteWorkers()
}
