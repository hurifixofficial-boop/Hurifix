package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {

    /**
     * Calculates distance in kilometers between two coordinates using the Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Estimates travel time in minutes based on urban technician bike travel (average 24 km/h + 3 min traffic/prep buffer).
     */
    fun estimateTravelTimeMinutes(distanceKm: Double): Int {
        if (distanceKm <= 0.2) return 2
        val avgSpeedKmh = 24.0
        val travelHours = distanceKm / avgSpeedKmh
        val minutes = (travelHours * 60).toInt() + 3
        return minutes.coerceAtLeast(3)
    }

    /**
     * Formats distance nicely (e.g. "850 m" or "2.4 km").
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).toInt()
            "$meters m"
        } else {
            String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    /**
     * Generates a direct Google Maps pin link.
     */
    fun createGoogleMapsUrl(latitude: Double, longitude: Double): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }

    /**
     * Smart parser to extract Latitude and Longitude from raw text, coordinates, or Google Maps URL.
     * Supports formats like:
     * - "28.5708, 77.3261"
     * - "https://maps.google.com/?q=28.5708,77.3261"
     * - "https://www.google.com/maps/@28.5708,77.3261,15z"
     * - "lat: 28.5708 lng: 77.3261"
     */
    fun parseCoordinatesFromText(input: String): Pair<Double, Double>? {
        if (input.isBlank()) return null

        // 1. Check for lat/lng query in URL e.g. query=lat,lng or q=lat,lng
        val urlQueryPattern = Pattern.compile("[?&](?:q|query|ll)=([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+)")
        val urlMatcher = urlQueryPattern.matcher(input)
        if (urlMatcher.find()) {
            val lat = urlMatcher.group(1)?.toDoubleOrNull()
            val lng = urlMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) return Pair(lat, lng)
        }

        // 2. Check for @lat,lng in google maps URLs
        val atPattern = Pattern.compile("@([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+)")
        val atMatcher = atPattern.matcher(input)
        if (atMatcher.find()) {
            val lat = atMatcher.group(1)?.toDoubleOrNull()
            val lng = atMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null) return Pair(lat, lng)
        }

        // 3. Check for standard "28.1234, 77.5678" or "28.1234 77.5678"
        val generalPattern = Pattern.compile("([+-]?\\d{1,2}\\.\\d{2,10})\\s*[,\\s]\\s*([+-]?\\d{1,3}\\.\\d{2,10})")
        val generalMatcher = generalPattern.matcher(input)
        if (generalMatcher.find()) {
            val lat = generalMatcher.group(1)?.toDoubleOrNull()
            val lng = generalMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                return Pair(lat, lng)
            }
        }

        return null
    }

    /**
     * Gets device current GPS location if location permission is granted.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        onSuccess(loc)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onSuccess(lastLoc)
                            } else {
                                onError("GPS location not available, using default city center.")
                            }
                        }.addOnFailureListener {
                            onError("Could not get location: ${it.localizedMessage}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    onError("Failed to get GPS location: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            onError("GPS Error: ${e.message}")
        }
    }
}
