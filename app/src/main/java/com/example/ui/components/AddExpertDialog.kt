package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpertEntity
import com.example.data.model.ServiceCategory
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpertDialog(
    initialExpert: ExpertEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ExpertEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialExpert?.name ?: "") }
    var phone by remember { mutableStateOf(initialExpert?.phone ?: "") }
    var category by remember { mutableStateOf(initialExpert?.category ?: ServiceCategory.AC.title) }
    var address by remember { mutableStateOf(initialExpert?.address ?: "") }
    var rawLocation by remember {
        mutableStateOf(
            if (initialExpert != null) "${initialExpert.latitude}, ${initialExpert.longitude}" else "28.5708, 77.3261"
        )
    }
    var latitude by remember { mutableDoubleStateOf(initialExpert?.latitude ?: 28.5708) }
    var longitude by remember { mutableDoubleStateOf(initialExpert?.longitude ?: 77.3261) }
    var isAvailable by remember { mutableStateOf(initialExpert?.isAvailable ?: true) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialExpert == null) "Add New Expert (एक्सपर्ट जोड़ें)" else "Edit Expert Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expert Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expert_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (WhatsApp)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expert_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Work / Skill Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        ServiceCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cat.title, fontWeight = FontWeight.SemiBold)
                                        Text(cat.hindiTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    category = cat.title
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop / Area / Landmark") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expert_address_input"),
                    singleLine = true
                )

                // Location / GPS input
                Column {
                    OutlinedTextField(
                        value = rawLocation,
                        onValueChange = { input ->
                            rawLocation = input
                            val parsed = LocationHelper.parseCoordinatesFromText(input)
                            if (parsed != null) {
                                latitude = parsed.first
                                longitude = parsed.second
                                locationError = null
                            } else {
                                locationError = "Valid Lat, Lng or Google Maps link daalein"
                            }
                        },
                        label = { Text("Exact Google Location (Lat, Lng or Maps URL)") },
                        trailingIcon = {
                            IconButton(onClick = {
                                LocationHelper.fetchCurrentLocation(
                                    context = context,
                                    onSuccess = { loc ->
                                        latitude = loc.latitude
                                        longitude = loc.longitude
                                        rawLocation = "${loc.latitude}, ${loc.longitude}"
                                        locationError = null
                                    },
                                    onError = { err ->
                                        locationError = err
                                    }
                                )
                            }) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Use GPS")
                            }
                        },
                        isError = locationError != null,
                        supportingText = {
                            if (locationError != null) {
                                Text(locationError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Format: 28.5708, 77.3261 ya Maps link")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expert_location_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Availability Status", fontWeight = FontWeight.Medium)
                        Text(
                            if (isAvailable) "Ready for work (उपलब्ध)" else "Currently busy / off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        modifier = Modifier.testTag("expert_available_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val expert = (initialExpert ?: ExpertEntity(
                            name = name.trim(),
                            phone = phone.trim(),
                            category = category,
                            address = address.trim().ifBlank { "Local Area" },
                            latitude = latitude,
                            longitude = longitude,
                            isAvailable = isAvailable
                        )).copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            category = category,
                            address = address.trim().ifBlank { "Local Area" },
                            latitude = latitude,
                            longitude = longitude,
                            isAvailable = isAvailable
                        )
                        onSave(expert)
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.testTag("save_expert_button")
            ) {
                Text("Save Expert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
