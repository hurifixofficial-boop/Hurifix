package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertDao {
    @Query("SELECT * FROM experts ORDER BY name ASC")
    fun getAllExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE id = :id")
    suspend fun getExpertById(id: Long): ExpertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpert(expert: ExpertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperts(experts: List<ExpertEntity>)

    @Update
    suspend fun updateExpert(expert: ExpertEntity)

    @Delete
    suspend fun deleteExpert(expert: ExpertEntity)

    @Query("DELETE FROM experts WHERE id = :id")
    suspend fun deleteExpertById(id: Long)

    @Query("SELECT COUNT(*) FROM experts")
    suspend fun getExpertCount(): Int
}
