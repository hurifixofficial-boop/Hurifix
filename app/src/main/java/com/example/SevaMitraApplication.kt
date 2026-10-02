package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.DispatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SevaMitraApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        DispatchRepository(
            expertDao = database.expertDao(),
            jobDao = database.customerJobDao(),
            technicianDao = database.technicianDao(),
            customerDao = database.customerDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.seedSampleExpertsIfEmpty()
        }
    }
}
