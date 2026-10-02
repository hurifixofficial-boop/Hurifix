package com.example

import com.example.data.model.ExpertEntity
import com.example.data.model.RankedExpert
import com.example.util.LocationHelper
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DispatchLogicTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Sector 18 (28.5708, 77.3261) and Sector 22 (28.5925, 77.3392) ~ 2.7 km
        val distance = LocationHelper.calculateDistanceKm(28.5708, 77.3261, 28.5925, 77.3392)
        assertTrue(distance > 2.0 && distance < 4.0)

        // Same location distance should be 0.0
        val zeroDist = LocationHelper.calculateDistanceKm(28.5708, 77.3261, 28.5708, 77.3261)
        assertEquals(0.0, zeroDist, 0.001)
    }

    @Test
    fun testTravelTimeEstimate() {
        val timeFor5Km = LocationHelper.estimateTravelTimeMinutes(5.0)
        assertTrue(timeFor5Km in 10..20)
    }

    @Test
    fun testCoordinateParsingFromGoogleMapsUrl() {
        val url = "https://maps.google.com/?q=28.5708,77.3261"
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)

        val rawCoords = "28.6280, 77.3650"
        val parsedRaw = LocationHelper.parseCoordinatesFromText(rawCoords)
        assertNotNull(parsedRaw)
        assertEquals(28.6280, parsedRaw!!.first, 0.0001)
        assertEquals(77.3650, parsedRaw.second, 0.0001)
    }

    @Test
    fun testPhoneFormattingForWhatsApp() {
        val formatted = WhatsAppHelper.formatPhoneNumberForWhatsApp("9876543210")
        assertEquals("919876543210", formatted)

        val alreadyWithCode = WhatsAppHelper.formatPhoneNumberForWhatsApp("919876543210")
        assertEquals("919876543210", alreadyWithCode)
    }

    @Test
    fun testAscendingSortOrder() {
        val expert1 = ExpertEntity(id = 1, name = "Far Expert", phone = "111", category = "AC", address = "Far", latitude = 28.7, longitude = 77.5)
        val expert2 = ExpertEntity(id = 2, name = "Near Expert", phone = "222", category = "AC", address = "Near", latitude = 28.571, longitude = 77.327)

        val customerLat = 28.5708
        val customerLng = 77.3261

        val rankedList = listOf(expert1, expert2).map {
            val dist = LocationHelper.calculateDistanceKm(customerLat, customerLng, it.latitude, it.longitude)
            val time = LocationHelper.estimateTravelTimeMinutes(dist)
            RankedExpert(it, dist, time)
        }.sortedBy { it.distanceKm }

        // Closest expert must be #1
        assertEquals("Near Expert", rankedList.first().expert.name)
        assertTrue(rankedList.first().distanceKm < rankedList.last().distanceKm)
    }
}
