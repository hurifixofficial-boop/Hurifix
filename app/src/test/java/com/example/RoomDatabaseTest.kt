package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerDao
import com.example.data.local.TechnicianDao
import com.example.data.model.CustomerEntity
import com.example.data.model.TechnicianEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var technicianDao: TechnicianDao
    private lateinit var customerDao: CustomerDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        technicianDao = database.technicianDao()
        customerDao = database.customerDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testTechnicianEntityAndDao() = runBlocking {
        val technician = TechnicianEntity(
            name = "Ramesh Plumber",
            contact = "9876543210",
            category = "Plumbing",
            address = "Sector 18 Market",
            latitude = 28.5708,
            longitude = 77.3261
        )
        val id = technicianDao.insertTechnician(technician)
        val retrieved = technicianDao.getTechnicianById(id)

        assertNotNull(retrieved)
        assertEquals("Ramesh Plumber", retrieved!!.name)
        assertEquals("9876543210", retrieved.contact)
        assertEquals("Plumbing", retrieved.category)
        assertEquals(28.5708, retrieved.latitude, 0.0001)
        assertEquals(77.3261, retrieved.longitude, 0.0001)
        assertEquals("Sector 18 Market", retrieved.address)

        val list = technicianDao.getAllTechnicians().first()
        assertEquals(1, list.size)
    }

    @Test
    fun testCustomerEntityAndDao() = runBlocking {
        val customer = CustomerEntity(
            name = "Sunita Rao",
            contact = "9812345678",
            address = "Flat 101, Green View Apartments",
            latitude = 28.5900,
            longitude = 77.3400,
            serviceRequired = "AC Repair",
            issueDescription = "Not cooling"
        )
        val id = customerDao.insertCustomer(customer)
        val retrieved = customerDao.getCustomerById(id)

        assertNotNull(retrieved)
        assertEquals("Sunita Rao", retrieved!!.name)
        assertEquals("9812345678", retrieved.contact)
        assertEquals(28.5900, retrieved.latitude, 0.0001)
        assertEquals(77.3400, retrieved.longitude, 0.0001)
        assertEquals("Flat 101, Green View Apartments", retrieved.address)

        val list = customerDao.getAllCustomers().first()
        assertEquals(1, list.size)
    }
}
