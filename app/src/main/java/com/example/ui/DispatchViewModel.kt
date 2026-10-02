package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.data.model.ServiceCategory
import com.example.data.repository.DispatchRepository
import com.example.util.LocationHelper
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern

enum class MainTab {
    EXPERTS,
    CUSTOMER_ORDERS
}

data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val serviceType: String = ServiceCategory.AC.title,
    val issueDescription: String = "",
    val address: String = "",
    val latitude: Double = 28.5708,
    val longitude: Double = 77.3261,
    val hasValidLocation: Boolean = true,
    val rawLocationInput: String = "28.5708, 77.3261"
)

class DispatchViewModel(private val repository: DispatchRepository) : ViewModel() {

    val allExperts: StateFlow<List<ExpertEntity>> = repository.allExperts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<CustomerJobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customerForm = MutableStateFlow(CustomerFormState())
    val customerForm: StateFlow<CustomerFormState> = _customerForm.asStateFlow()

    private val _filterOnlyMatchingService = MutableStateFlow(false)
    val filterOnlyMatchingService: StateFlow<Boolean> = _filterOnlyMatchingService.asStateFlow()

    private val _filterAvailableOnly = MutableStateFlow(false)
    val filterAvailableOnly: StateFlow<Boolean> = _filterAvailableOnly.asStateFlow()

    private val _selectedJobForDispatch = MutableStateFlow<CustomerJobEntity?>(null)
    val selectedJobForDispatch: StateFlow<CustomerJobEntity?> = _selectedJobForDispatch.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.CUSTOMER_ORDERS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    // Dynamically calculate nearest experts whenever customer coordinates, experts list, or filters change!
    val nearestExperts: StateFlow<List<RankedExpert>> = combine(
        allExperts,
        _customerForm,
        _filterOnlyMatchingService,
        _filterAvailableOnly
    ) { experts, form, matchService, availableOnly ->
        if (!form.hasValidLocation) {
            emptyList()
        } else {
            experts
                .filter { expert ->
                    if (availableOnly && !expert.isAvailable) return@filter false
                    if (matchService) {
                        val req = form.serviceType
                        expert.category.contains(req, ignoreCase = true) ||
                                req.contains(expert.category, ignoreCase = true) ||
                                expert.category.contains("All-Rounder", ignoreCase = true)
                    } else {
                        true
                    }
                }
                .map { expert ->
                    val dist = LocationHelper.calculateDistanceKm(
                        lat1 = form.latitude,
                        lon1 = form.longitude,
                        lat2 = expert.latitude,
                        lon2 = expert.longitude
                    )
                    val time = LocationHelper.estimateTravelTimeMinutes(dist)
                    RankedExpert(
                        expert = expert,
                        distanceKm = dist,
                        travelTimeMinutes = time
                    )
                }
                .sortedBy { it.distanceKm }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(name: String) {
        _customerForm.value = _customerForm.value.copy(name = name)
    }

    fun updatePhone(phone: String) {
        _customerForm.value = _customerForm.value.copy(phone = phone)
    }

    fun updateServiceType(service: String) {
        _customerForm.value = _customerForm.value.copy(serviceType = service)
    }

    fun updateIssue(issue: String) {
        _customerForm.value = _customerForm.value.copy(issueDescription = issue)
    }

    fun updateAddress(address: String) {
        _customerForm.value = _customerForm.value.copy(address = address)
    }

    fun updateLocationInput(input: String) {
        val parsed = LocationHelper.parseCoordinatesFromText(input)
        if (parsed != null) {
            _customerForm.value = _customerForm.value.copy(
                rawLocationInput = input,
                latitude = parsed.first,
                longitude = parsed.second,
                hasValidLocation = true
            )
        } else {
            _customerForm.value = _customerForm.value.copy(
                rawLocationInput = input,
                hasValidLocation = false
            )
        }
    }

    fun setCoordinates(lat: Double, lng: Double, address: String? = null) {
        _customerForm.value = _customerForm.value.copy(
            latitude = lat,
            longitude = lng,
            rawLocationInput = "$lat, $lng",
            hasValidLocation = true,
            address = address ?: _customerForm.value.address
        )
    }

    fun toggleFilterOnlyMatchingService() {
        _filterOnlyMatchingService.value = !_filterOnlyMatchingService.value
    }

    fun toggleFilterAvailableOnly() {
        _filterAvailableOnly.value = !_filterAvailableOnly.value
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun fetchCurrentGps(context: Context) {
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { loc ->
                setCoordinates(loc.latitude, loc.longitude)
                _statusMessage.value = "GPS Location set: ${loc.latitude}, ${loc.longitude}"
            },
            onError = { err ->
                _statusMessage.value = err
            }
        )
    }

    /**
     * Smart parser for incoming WhatsApp messages copied from customer chats.
     * E.g.
     * "Name: Rahul Sharma
     * Phone: 9876543210
     * AC repair gas leak
     * Sector 18 Noida"
     */
    fun parseAndFillFromWhatsAppText(rawText: String) {
        var detectedName = ""
        var detectedPhone = ""
        var detectedService = _customerForm.value.serviceType
        var detectedIssue = ""
        var detectedAddress = ""

        // Extract phone number (10 digits)
        val phonePattern = Pattern.compile("(?:\\+91|91|0)?[6-9]\\d{9}")
        val phoneMatcher = phonePattern.matcher(rawText)
        if (phoneMatcher.find()) {
            val fullPhone = phoneMatcher.group()
            detectedPhone = fullPhone.replace(Regex("^(\\+91|91|0)"), "")
        }

        // Check coordinates or maps link
        val parsedCoords = LocationHelper.parseCoordinatesFromText(rawText)

        // Check service type keywords
        val lower = rawText.lowercase()
        when {
            lower.contains("ac") || lower.contains("air conditioner") || lower.contains("cooling") ->
                detectedService = ServiceCategory.AC.title
            lower.contains("fan") || lower.contains("switch") || lower.contains("electric") || lower.contains("wiring") || lower.contains("light") ->
                detectedService = ServiceCategory.FAN_SWITCHBOARD.title
            lower.contains("fridge") || lower.contains("refrigerator") ->
                detectedService = ServiceCategory.REFRIGERATOR.title
            lower.contains("plumb") || lower.contains("tap") || lower.contains("pipe") || lower.contains("leak") || lower.contains("tank") ->
                detectedService = ServiceCategory.PLUMBING.title
            lower.contains("wash") || lower.contains("machine") ->
                detectedService = ServiceCategory.WASHING_MACHINE.title
        }

        // Lines analysis
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            val lineLower = line.lowercase()
            if (lineLower.startsWith("name:") || lineLower.startsWith("customer:")) {
                detectedName = line.substringAfter(":").trim()
            } else if (lineLower.startsWith("phone:") || lineLower.startsWith("mobile:") || lineLower.startsWith("number:")) {
                if (detectedPhone.isBlank()) {
                    detectedPhone = line.substringAfter(":").trim().replace(Regex("[^0-9]"), "")
                }
            } else if (lineLower.startsWith("address:") || lineLower.startsWith("location:")) {
                detectedAddress = line.substringAfter(":").trim()
            } else if (lineLower.startsWith("problem:") || lineLower.startsWith("issue:") || lineLower.startsWith("work:")) {
                detectedIssue = line.substringAfter(":").trim()
            }
        }

        if (detectedIssue.isBlank()) {
            detectedIssue = rawText.take(120)
        }

        _customerForm.value = _customerForm.value.copy(
            name = if (detectedName.isNotBlank()) detectedName else _customerForm.value.name,
            phone = if (detectedPhone.isNotBlank()) detectedPhone else _customerForm.value.phone,
            serviceType = detectedService,
            issueDescription = detectedIssue,
            address = if (detectedAddress.isNotBlank()) detectedAddress else _customerForm.value.address,
            latitude = parsedCoords?.first ?: _customerForm.value.latitude,
            longitude = parsedCoords?.second ?: _customerForm.value.longitude,
            rawLocationInput = parsedCoords?.let { "${it.first}, ${it.second}" } ?: _customerForm.value.rawLocationInput,
            hasValidLocation = true
        )
        _statusMessage.value = "WhatsApp lead parsed successfully!"
    }

    /**
     * Dispatches details to expert via WhatsApp and logs or updates job in database.
     */
    fun dispatchToExpert(
        context: Context,
        ranked: RankedExpert,
        jobToDispatch: CustomerJobEntity? = null
    ) {
        val form = _customerForm.value
        val currentJob = jobToDispatch ?: CustomerJobEntity(
            customerName = form.name.ifBlank { "WhatsApp Customer" },
            customerPhone = form.phone.ifBlank { "9876543210" },
            serviceType = form.serviceType,
            issueDescription = form.issueDescription.ifBlank { "Appliance repair / service requested" },
            address = form.address.ifBlank { "Location coordinates attached" },
            latitude = form.latitude,
            longitude = form.longitude,
            status = JobStatus.ASSIGNED.name,
            assignedExpertId = ranked.expert.id,
            assignedExpertName = ranked.expert.name,
            assignedExpertPhone = ranked.expert.phone,
            distanceKmAtDispatch = ranked.distanceKm
        )

        viewModelScope.launch {
            if (jobToDispatch == null) {
                repository.insertJob(currentJob)
            } else {
                repository.assignJobToExpert(
                    jobId = jobToDispatch.id,
                    expert = ranked.expert,
                    distanceKm = ranked.distanceKm
                )
            }
        }

        // Launch WhatsApp
        WhatsAppHelper.sendWhatsAppMessageToExpert(
            context = context,
            expert = ranked.expert,
            customer = currentJob,
            distanceKm = ranked.distanceKm,
            travelTimeMinutes = ranked.travelTimeMinutes
        )

        _statusMessage.value = "Dispatched to ${ranked.expert.name} via WhatsApp!"
    }

    fun callExpert(context: Context, phone: String) {
        WhatsAppHelper.openDialer(context, phone)
    }

    fun callCustomer(context: Context, phone: String) {
        WhatsAppHelper.openDialer(context, phone)
    }

    fun saveNewExpert(expert: ExpertEntity) {
        viewModelScope.launch {
            repository.insertExpert(expert)
            _statusMessage.value = "Expert ${expert.name} added!"
        }
    }

    fun updateExpert(expert: ExpertEntity) {
        viewModelScope.launch {
            repository.updateExpert(expert)
            _statusMessage.value = "Expert ${expert.name} updated!"
        }
    }

    fun deleteExpert(expert: ExpertEntity) {
        viewModelScope.launch {
            repository.deleteExpert(expert)
            _statusMessage.value = "Expert removed."
        }
    }

    fun updateJobStatus(jobId: Long, status: JobStatus) {
        viewModelScope.launch {
            repository.updateJobStatus(jobId, status)
            _statusMessage.value = "Order status updated to ${status.label}"
        }
    }

    fun deleteJob(job: CustomerJobEntity) {
        viewModelScope.launch {
            repository.deleteJob(job)
            _statusMessage.value = "Order deleted."
        }
    }

    fun loadJobIntoForm(job: CustomerJobEntity) {
        _customerForm.value = CustomerFormState(
            name = job.customerName,
            phone = job.customerPhone,
            serviceType = job.serviceType,
            issueDescription = job.issueDescription,
            address = job.address,
            latitude = job.latitude,
            longitude = job.longitude,
            hasValidLocation = true,
            rawLocationInput = "${job.latitude}, ${job.longitude}"
        )
        _selectedJobForDispatch.value = job
        _statusMessage.value = "Loaded order for ${job.customerName}. See nearest experts below."
    }

    fun clearForm() {
        _customerForm.value = CustomerFormState()
        _selectedJobForDispatch.value = null
    }
}

class DispatchViewModelFactory(private val repository: DispatchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DispatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DispatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
