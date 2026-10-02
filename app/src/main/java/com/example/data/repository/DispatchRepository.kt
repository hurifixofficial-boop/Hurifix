package com.example.data.repository

import com.example.data.local.CustomerDao
import com.example.data.local.CustomerJobDao
import com.example.data.local.ExpertDao
import com.example.data.local.TechnicianDao
import com.example.data.model.CustomerEntity
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.data.model.ServiceCategory
import com.example.data.model.TechnicianEntity
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DispatchRepository(
    private val expertDao: ExpertDao,
    private val jobDao: CustomerJobDao,
    private val technicianDao: TechnicianDao? = null,
    private val customerDao: CustomerDao? = null
) {
    val allExperts: Flow<List<ExpertEntity>> = expertDao.getAllExperts()
    val availableExperts: Flow<List<ExpertEntity>> = expertDao.getAvailableExperts()
    val allJobs: Flow<List<CustomerJobEntity>> = jobDao.getAllJobs()

    // Technician and Customer Flow properties
    val allTechnicians: Flow<List<TechnicianEntity>>? = technicianDao?.getAllTechnicians()
    val allCustomers: Flow<List<CustomerEntity>>? = customerDao?.getAllCustomers()

    // Technician operations
    suspend fun insertTechnician(technician: TechnicianEntity): Long {
        val id = technicianDao?.insertTechnician(technician) ?: 0L
        // Also keep expertDao in sync
        expertDao.insertExpert(
            ExpertEntity(
                name = technician.name,
                phone = technician.contact,
                category = technician.category,
                address = technician.address,
                latitude = technician.latitude,
                longitude = technician.longitude,
                isAvailable = technician.isAvailable,
                rating = technician.rating,
                completedJobsCount = technician.completedJobsCount
            )
        )
        return id
    }

    suspend fun updateTechnician(technician: TechnicianEntity) {
        technicianDao?.updateTechnician(technician)
    }

    suspend fun deleteTechnician(technician: TechnicianEntity) {
        technicianDao?.deleteTechnician(technician)
    }

    suspend fun getTechnicianById(id: Long): TechnicianEntity? = technicianDao?.getTechnicianById(id)

    // Customer operations
    suspend fun insertCustomer(customer: CustomerEntity): Long =
        customerDao?.insertCustomer(customer) ?: 0L

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao?.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao?.deleteCustomer(customer)
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao?.getCustomerById(id)

    // Expert operations
    suspend fun insertExpert(expert: ExpertEntity): Long = expertDao.insertExpert(expert)

    suspend fun updateExpert(expert: ExpertEntity) = expertDao.updateExpert(expert)

    suspend fun deleteExpert(expert: ExpertEntity) = expertDao.deleteExpert(expert)

    // Customer Job / Order operations
    suspend fun insertJob(job: CustomerJobEntity): Long {
        val id = jobDao.insertJob(job)
        // Also ensure customer record is stored in customers table
        customerDao?.insertCustomer(
            CustomerEntity(
                name = job.customerName,
                contact = job.customerPhone,
                address = job.address,
                latitude = job.latitude,
                longitude = job.longitude,
                serviceRequired = job.serviceType,
                issueDescription = job.issueDescription
            )
        )
        return id
    }

    suspend fun updateJobStatus(jobId: Long, status: JobStatus) {
        jobDao.updateJobStatus(jobId, status.name)
    }

    suspend fun assignJobToExpert(
        jobId: Long,
        expert: ExpertEntity,
        distanceKm: Double
    ) {
        jobDao.updateJobDispatch(
            jobId = jobId,
            status = JobStatus.ASSIGNED.name,
            expertId = expert.id,
            expertName = expert.name,
            expertPhone = expert.phone,
            distanceKm = distanceKm
        )
    }

    suspend fun deleteJob(job: CustomerJobEntity) = jobDao.deleteJob(job)

    /**
     * Calculates distance and travel time to all experts from customer coordinates,
     * sorted in ascending order (nearest expert first).
     */
    suspend fun findNearestExperts(
        customerLat: Double,
        customerLng: Double,
        requiredService: String? = null,
        onlyAvailable: Boolean = false
    ): List<RankedExpert> {
        val experts = if (onlyAvailable) {
            availableExperts.first()
        } else {
            allExperts.first()
        }

        return experts
            .filter { expert ->
                if (requiredService.isNullOrBlank() || requiredService == "All") {
                    true
                } else {
                    expert.category.contains(requiredService, ignoreCase = true) ||
                            expert.category.contains("All-Rounder", ignoreCase = true) ||
                            requiredService.contains(expert.category, ignoreCase = true)
                }
            }
            .map { expert ->
                val distance = LocationHelper.calculateDistanceKm(
                    lat1 = customerLat,
                    lon1 = customerLng,
                    lat2 = expert.latitude,
                    lon2 = expert.longitude
                )
                val travelMinutes = LocationHelper.estimateTravelTimeMinutes(distance)
                RankedExpert(
                    expert = expert,
                    distanceKm = distance,
                    travelTimeMinutes = travelMinutes
                )
            }
            .sortedBy { it.distanceKm }
    }

    suspend fun seedSampleExpertsIfEmpty() {
        if (expertDao.getExpertCount() == 0) {
            val sampleExperts = listOf(
                ExpertEntity(
                    name = "Ramesh Sharma (Electrician)",
                    phone = "9871234560",
                    category = ServiceCategory.FAN_SWITCHBOARD.title,
                    address = "Sector 18 Market, Main Road",
                    latitude = 28.5708,
                    longitude = 77.3261,
                    isAvailable = true,
                    rating = 4.9f,
                    completedJobsCount = 142
                ),
                ExpertEntity(
                    name = "Mohammad Irfan (AC Expert)",
                    phone = "9811223340",
                    category = ServiceCategory.AC.title,
                    address = "Block C, Sector 22",
                    latitude = 28.5925,
                    longitude = 77.3392,
                    isAvailable = true,
                    rating = 4.8f,
                    completedJobsCount = 210
                ),
                ExpertEntity(
                    name = "Sunil Verma (Plumber)",
                    phone = "9899887760",
                    category = ServiceCategory.PLUMBING.title,
                    address = "Near Metro Station, Sector 29",
                    latitude = 28.5630,
                    longitude = 77.3340,
                    isAvailable = true,
                    rating = 4.7f,
                    completedJobsCount = 95
                ),
                ExpertEntity(
                    name = "Deepak Kumar (Fridge & Appliances)",
                    phone = "9810123450",
                    category = ServiceCategory.REFRIGERATOR.title,
                    address = "Chowk Bazar, Sector 12",
                    latitude = 28.6015,
                    longitude = 77.3218,
                    isAvailable = true,
                    rating = 4.9f,
                    completedJobsCount = 180
                ),
                ExpertEntity(
                    name = "Amit Singh (All-Rounder)",
                    phone = "9711224480",
                    category = ServiceCategory.ALL_ROUNDER.title,
                    address = "Central Plaza, Sector 50",
                    latitude = 28.5792,
                    longitude = 77.3620,
                    isAvailable = true,
                    rating = 4.8f,
                    completedJobsCount = 124
                ),
                ExpertEntity(
                    name = "Vikram Mistri (Washing Machine)",
                    phone = "9873456780",
                    category = ServiceCategory.WASHING_MACHINE.title,
                    address = "Commercial Complex, Sector 62",
                    latitude = 28.6280,
                    longitude = 77.3650,
                    isAvailable = false,
                    rating = 4.6f,
                    completedJobsCount = 88
                )
            )
            expertDao.insertExperts(sampleExperts)

            technicianDao?.let { tDao ->
                if (tDao.getTechnicianCount() == 0) {
                    val sampleTechs = sampleExperts.map { exp ->
                        TechnicianEntity(
                            name = exp.name,
                            contact = exp.phone,
                            category = exp.category,
                            address = exp.address,
                            latitude = exp.latitude,
                            longitude = exp.longitude,
                            isAvailable = exp.isAvailable,
                            rating = exp.rating,
                            completedJobsCount = exp.completedJobsCount
                        )
                    }
                    tDao.insertTechnicians(sampleTechs)
                }
            }
        }

        if (jobDao.getJobCount() == 0) {
            val sampleJob = CustomerJobEntity(
                customerName = "Pooja Verma",
                customerPhone = "9988776655",
                serviceType = ServiceCategory.AC.title,
                issueDescription = "AC not cooling and making rattling sound in bedroom",
                address = "Flat 402, Royal Towers, Sector 19",
                latitude = 28.5750,
                longitude = 77.3290,
                status = JobStatus.PENDING.name
            )
            jobDao.insertJob(sampleJob)

            customerDao?.let { cDao ->
                if (cDao.getCustomerCount() == 0) {
                    cDao.insertCustomer(
                        CustomerEntity(
                            name = "Pooja Verma",
                            contact = "9988776655",
                            address = "Flat 402, Royal Towers, Sector 19",
                            latitude = 28.5750,
                            longitude = 77.3290,
                            serviceRequired = ServiceCategory.AC.title,
                            issueDescription = "AC not cooling and making rattling sound in bedroom"
                        )
                    )
                }
            }
        }
    }
}
