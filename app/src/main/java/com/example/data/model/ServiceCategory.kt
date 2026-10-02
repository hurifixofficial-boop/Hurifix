package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class ServiceCategory(
    val title: String,
    val hindiTitle: String,
    val iconName: String
) {
    AC("AC Repair & Service", "एसी सर्विस / रिपेयर", "ac_unit"),
    FAN_SWITCHBOARD("Fan & Switch Board", "पंखा व स्विच बोर्ड (इलेक्ट्रिशियन)", "electric_bolt"),
    REFRIGERATOR("Refrigerator / Fridge", "फ्रिज रिपेयर", "kitchen"),
    PLUMBING("Plumbing & Pipes", "नल व प्लंबिंग फिटिंग", "plumbing"),
    WASHING_MACHINE("Washing Machine", "वॉशिंग मशीन", "waves"),
    ALL_ROUNDER("All-Rounder Technician", "ऑल-राउंडर एक्सपर्ट", "handyman");

    companion object {
        fun fromString(value: String): ServiceCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: ALL_ROUNDER
        }
    }
}
