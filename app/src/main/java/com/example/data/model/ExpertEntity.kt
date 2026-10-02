package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experts")
data class ExpertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean = true,
    val rating: Float = 4.8f,
    val completedJobsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class RankedExpert(
    val expert: ExpertEntity,
    val distanceKm: Double,
    val travelTimeMinutes: Int
)
