package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity to store Customer records locally.
 * Includes customer name, contact number, and exact geolocation (latitude, longitude, address).
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val serviceRequired: String = "",
    val issueDescription: String = "",
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
