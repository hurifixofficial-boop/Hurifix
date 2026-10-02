package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_jobs")
data class CustomerJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val serviceType: String,
    val issueDescription: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val status: String = JobStatus.PENDING.name,
    val assignedExpertId: Long? = null,
    val assignedExpertName: String? = null,
    val assignedExpertPhone: String? = null,
    val distanceKmAtDispatch: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class JobStatus(val label: String, val hindiLabel: String) {
    PENDING("Pending Dispatch", "भेजना बाकी है"),
    ASSIGNED("Assigned", "एक्सपर्ट को भेजा गया"),
    IN_PROGRESS("In Progress", "काम चल रहा है"),
    COMPLETED("Completed", "पूरा हो गया"),
    CANCELLED("Cancelled", "रद्द किया गया")
}
