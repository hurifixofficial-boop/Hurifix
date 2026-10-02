package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerJobDao {
    @Query("SELECT * FROM customer_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): CustomerJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: CustomerJobEntity): Long

    @Update
    suspend fun updateJob(job: CustomerJobEntity)

    @Delete
    suspend fun deleteJob(job: CustomerJobEntity)

    @Query("DELETE FROM customer_jobs WHERE id = :id")
    suspend fun deleteJobById(id: Long)

    @Query("UPDATE customer_jobs SET status = :status, assignedExpertId = :expertId, assignedExpertName = :expertName, assignedExpertPhone = :expertPhone, distanceKmAtDispatch = :distanceKm WHERE id = :jobId")
    suspend fun updateJobDispatch(
        jobId: Long,
        status: String,
        expertId: Long,
        expertName: String,
        expertPhone: String,
        distanceKm: Double
    )

    @Query("UPDATE customer_jobs SET status = :status WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: String)

    @Query("SELECT COUNT(*) FROM customer_jobs")
    suspend fun getJobCount(): Int
}
