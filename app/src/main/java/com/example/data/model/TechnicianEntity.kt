package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity to store Technician records locally.
 * Includes technician name, contact number, work trade category, and exact geolocation (latitude, longitude, address).
 */
@Entity(tableName = "technicians")
data class TechnicianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean = true,
    val rating: Float = 4.8f,
    val completedJobsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Convenience property returning contact number
     */
    val phone: String
        get() = contact

    /**
     * Pair of (latitude, longitude) representing geolocation coordinates
     */
    val geolocationCoordinates: Pair<Double, Double>
        get() = Pair(latitude, longitude)
}
