package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TechnicianEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Technician records.
 */
@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians ORDER BY name ASC")
    fun getAllTechnicians(): Flow<List<TechnicianEntity>>

    @Query("SELECT * FROM technicians WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableTechnicians(): Flow<List<TechnicianEntity>>

    @Query("SELECT * FROM technicians WHERE id = :id")
    suspend fun getTechnicianById(id: Long): TechnicianEntity?

    @Query("SELECT * FROM technicians WHERE contact = :contact LIMIT 1")
    suspend fun getTechnicianByContact(contact: String): TechnicianEntity?

    @Query("SELECT * FROM technicians WHERE category = :category ORDER BY name ASC")
    fun getTechniciansByCategory(category: String): Flow<List<TechnicianEntity>>

    @Query("SELECT * FROM technicians WHERE name LIKE '%' || :query || '%' OR contact LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchTechnicians(query: String): Flow<List<TechnicianEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: TechnicianEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnicians(technicians: List<TechnicianEntity>)

    @Update
    suspend fun updateTechnician(technician: TechnicianEntity)

    @Delete
    suspend fun deleteTechnician(technician: TechnicianEntity)

    @Query("DELETE FROM technicians WHERE id = :id")
    suspend fun deleteTechnicianById(id: Long)

    @Query("SELECT COUNT(*) FROM technicians")
    suspend fun getTechnicianCount(): Int
}
