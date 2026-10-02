package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.data.model.ServiceCategory
import com.example.ui.CustomerFormState
import com.example.ui.DispatchViewModel
import com.example.ui.MainTab
import com.example.ui.components.AddExpertDialog
import com.example.ui.components.ExpertCard
import com.example.ui.components.JobStatusCard
import com.example.ui.components.NearestExpertItemCard
import com.example.ui.components.WhatsAppLeadParserDialog
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.util.LocationHelper
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation state observed from ViewModel
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    var showAddExpertDialog by remember { mutableStateOf(false) }
    var expertToEdit by remember { mutableStateOf<ExpertEntity?>(null) }
    var showWhatsAppPasteDialog by remember { mutableStateOf(false) }

    val experts by viewModel.allExperts.collectAsStateWithLifecycle()
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val nearestExperts by viewModel.nearestExperts.collectAsStateWithLifecycle()
    val customerForm by viewModel.customerForm.collectAsStateWithLifecycle()
    val filterMatchingService by viewModel.filterOnlyMatchingService.collectAsStateWithLifecycle()
    val filterAvailableOnly by viewModel.filterAvailableOnly.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Handle system back navigation to return to the primary tab
    BackHandler(enabled = currentTab != MainTab.CUSTOMER_ORDERS) {
        viewModel.selectTab(MainTab.CUSTOMER_ORDERS)
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchCurrentGps(context)
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hurifix_logo_exact_1790919761852),
                                contentDescription = "Hurifix Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column {
                                Text(
                                    text = "Hurifix",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Many Problems | One Solution",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // TABBED LAYOUT: Switch between 'Experts' list and 'Customer Orders' list
                TabRow(
                    selectedTabIndex = if (currentTab == MainTab.EXPERTS) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        val index = if (currentTab == MainTab.EXPERTS) 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tab_layout_navigation")
                ) {
                    // Tab 0: Experts
                    Tab(
                        selected = currentTab == MainTab.EXPERTS,
                        onClick = { viewModel.selectTab(MainTab.EXPERTS) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Engineering,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Experts",
                                    fontWeight = if (currentTab == MainTab.EXPERTS) FontWeight.Bold else FontWeight.Medium
                                )
                                Badge(
                                    containerColor = if (currentTab == MainTab.EXPERTS) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${experts.size}",
                                        color = if (currentTab == MainTab.EXPERTS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_experts")
                    )

                    // Tab 1: Customer Orders
                    Tab(
                        selected = currentTab == MainTab.CUSTOMER_ORDERS,
                        onClick = { viewModel.selectTab(MainTab.CUSTOMER_ORDERS) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Customer Orders",
                                    fontWeight = if (currentTab == MainTab.CUSTOMER_ORDERS) FontWeight.Bold else FontWeight.Medium
                                )
                                Badge(
                                    containerColor = if (currentTab == MainTab.CUSTOMER_ORDERS) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${jobs.size}",
                                        color = if (currentTab == MainTab.CUSTOMER_ORDERS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_customer_orders")
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        floatingActionButton = {
            if (currentTab == MainTab.EXPERTS) {
                ExtendedFloatingActionButton(
                    onClick = {
                        expertToEdit = null
                        showAddExpertDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Expert") },
                    modifier = Modifier.testTag("add_expert_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.EXPERTS -> {
                    ExpertsTabContent(
                        experts = experts,
                        onEditExpert = { expert ->
                            expertToEdit = expert
                            showAddExpertDialog = true
                        },
                        onDeleteExpert = { viewModel.deleteExpert(it) },
                        onToggleAvailability = { exp, avail -> viewModel.updateExpert(exp.copy(isAvailable = avail)) }
                    )
                }
                MainTab.CUSTOMER_ORDERS -> {
                    CustomerOrdersTabContent(
                        viewModel = viewModel,
                        customerForm = customerForm,
                        nearestExperts = nearestExperts,
                        jobs = jobs,
                        filterMatchingService = filterMatchingService,
                        filterAvailableOnly = filterAvailableOnly,
                        onOpenWhatsAppParser = { showWhatsAppPasteDialog = true },
                        onRequestGps = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.fetchCurrentGps(context)
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddExpertDialog) {
        AddExpertDialog(
            initialExpert = expertToEdit,
            onDismiss = { showAddExpertDialog = false },
            onSave = { expert ->
                if (expertToEdit == null) {
                    viewModel.saveNewExpert(expert)
                } else {
                    viewModel.updateExpert(expert)
                }
                showAddExpertDialog = false
            }
        )
    }

    if (showWhatsAppPasteDialog) {
        WhatsAppLeadParserDialog(
            onDismiss = { showWhatsAppPasteDialog = false },
            onParseText = { rawText ->
                viewModel.parseAndFillFromWhatsAppText(rawText)
            }
        )
    }
}

/**
 * Experts Tab: Displays the full list of partner technicians & plumbers with search, trade filter,
 * Google location, direct call, WhatsApp, and availability controls.
 */
@Composable
private fun ExpertsTabContent(
    experts: List<ExpertEntity>,
    onEditExpert: (ExpertEntity) -> Unit,
    onDeleteExpert: (ExpertEntity) -> Unit,
    onToggleAvailability: (ExpertEntity, Boolean) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredExperts = experts.filter { expert ->
        val matchesSearch = searchQuery.isBlank() ||
                expert.name.contains(searchQuery, ignoreCase = true) ||
                expert.phone.contains(searchQuery, ignoreCase = true) ||
                expert.address.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == null || expert.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text(
                        text = "Partner Experts Directory (${experts.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${experts.count { it.isAvailable }} currently available for tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone or area...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_experts_input")
                )

                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${experts.size})") }
                    )
                    ServiceCategory.entries.forEach { cat ->
                        val count = experts.count { it.category == cat.title }
                        FilterChip(
                            selected = selectedCategoryFilter == cat.title,
                            onClick = { selectedCategoryFilter = cat.title },
                            label = { Text("${cat.title} ($count)") }
                        )
                    }
                }
            }
        }

        if (filteredExperts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No experts match the search / category criteria.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredExperts, key = { it.id }) { expert ->
                ExpertCard(
                    expert = expert,
                    onCall = { WhatsAppHelper.openDialer(context, expert.phone) },
                    onWhatsApp = {
                        val url = "https://api.whatsapp.com/send?phone=${WhatsAppHelper.formatPhoneNumberForWhatsApp(expert.phone)}"
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            WhatsAppHelper.copyToClipboard(context, "Phone", expert.phone)
                        }
                    },
                    onEdit = { onEditExpert(expert) },
                    onDelete = { onDeleteExpert(expert) },
                    onToggleAvailability = { isAvail -> onToggleAvailability(expert, isAvail) },
                    onViewMap = {
                        WhatsAppHelper.openGoogleMaps(context, expert.latitude, expert.longitude, expert.name)
                    }
                )
            }
        }
    }
}

/**
 * Customer Orders Tab: Combines customer order dispatching with instant ascending nearest expert
 * calculations (with 1-click WhatsApp & call actions) plus the live bookings list.
 */
@Composable
private fun CustomerOrdersTabContent(
    viewModel: DispatchViewModel,
    customerForm: CustomerFormState,
    nearestExperts: List<RankedExpert>,
    jobs: List<CustomerJobEntity>,
    filterMatchingService: Boolean,
    filterAvailableOnly: Boolean,
    onOpenWhatsAppParser: () -> Unit,
    onRequestGps: () -> Unit
) {
    val context = LocalContext.current
    var subSection by remember { mutableIntStateOf(0) } // 0: New Order Dispatch, 1: Orders History

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-toggle between "New Dispatch (Auto Nearest Experts)" and "All Orders & History"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { subSection = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (subSection == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (subSection == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_section_dispatch")
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Dispatch Order", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { subSection = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (subSection == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (subSection == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_section_history")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Orders (${jobs.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (subSection == 0) {
            // NEW DISPATCH SECTION WITH LIVE NEAREST EXPERTS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_entry_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title and Quick Paste Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Naya Customer Order (WhatsApp)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Location dalte hi nearest experts apne aap aayenge",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = onOpenWhatsAppParser,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WhatsAppDarkGreen
                                ),
                                modifier = Modifier.testTag("btn_paste_whatsapp")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Paste Lead", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Form Fields: Customer Name & Phone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customerForm.name,
                                onValueChange = { viewModel.updateName(it) },
                                label = { Text("Customer Name") },
                                placeholder = { Text("e.g. Rahul Sharma") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("customer_name_input")
                            )

                            OutlinedTextField(
                                value = customerForm.phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                label = { Text("WhatsApp / Mobile") },
                                placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("customer_phone_input")
                            )
                        }

                        // Service Category Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Required Service / Appliance (काम का प्रकार):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ServiceCategory.entries.forEach { cat ->
                                    val isSelected = customerForm.serviceType == cat.title
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateServiceType(cat.title) },
                                        label = { Text(cat.title, fontSize = 12.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }

                        // Problem / Issue Description
                        OutlinedTextField(
                            value = customerForm.issueDescription,
                            onValueChange = { viewModel.updateIssue(it) },
                            label = { Text("Problem Details (खराबी का विवरण)") },
                            placeholder = { Text("e.g. AC cooling nahi kar raha, Fan switch board spark") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_issue_input"),
                            singleLine = true
                        )

                        // Address / Area
                        OutlinedTextField(
                            value = customerForm.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = { Text("Customer Address / Sector / Colony") },
                            placeholder = { Text("e.g. Flat 304, Tower B, Sector 62") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_address_input")
                        )

                        // Coordinates & Location Helper
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = customerForm.rawLocationInput,
                                onValueChange = { viewModel.updateLocationInput(it) },
                                label = { Text("Exact Google Coordinates (Lat, Lng ya Maps URL)") },
                                placeholder = { Text("28.5708, 77.3261") },
                                trailingIcon = {
                                    IconButton(onClick = onRequestGps) {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Use My GPS Location", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                isError = !customerForm.hasValidLocation,
                                supportingText = {
                                    if (!customerForm.hasValidLocation) {
                                        Text("Valid Latitude & Longitude daalein (e.g. 28.57, 77.32)", color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Nearest experts isi location se calculate honge")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_coords_input")
                            )

                            // Quick Area Location Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Quick Areas:",
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                listOf(
                                    Triple("Sector 18", 28.5708, 77.3261),
                                    Triple("Sector 62", 28.6280, 77.3650),
                                    Triple("Sector 22", 28.5925, 77.3392),
                                    Triple("Indirapuram", 28.6430, 77.3780)
                                ).forEach { (areaName, lat, lng) ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            viewModel.setCoordinates(lat, lng, areaName)
                                        }
                                    ) {
                                        Text(
                                            text = areaName,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Reset button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.clearForm() }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Clear Form")
                            }
                        }
                    }
                }
            }

            // NEAREST EXPERTS RESULTS HEADER & FILTERS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Sabse Paas Wale Experts (Nearest)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Ascending order: Distance & Time k sath",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${nearestExperts.size} Experts",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Filters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterMatchingService,
                            onClick = { viewModel.toggleFilterOnlyMatchingService() },
                            label = { Text("Only ${customerForm.serviceType} Experts", fontSize = 12.sp) },
                            leadingIcon = if (filterMatchingService) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = filterAvailableOnly,
                            onClick = { viewModel.toggleFilterAvailableOnly() },
                            label = { Text("Available Only", fontSize = 12.sp) },
                            leadingIcon = if (filterAvailableOnly) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }

            // NEAREST EXPERTS LIST (Ascending order of distance)
            if (nearestExperts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = "Koi matching expert nahi mila",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Filters hatayein ya 'Experts' tab me jakar naye technician add karein.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(nearestExperts, key = { _, ranked -> ranked.expert.id }) { index, ranked ->
                    NearestExpertItemCard(
                        ranked = ranked,
                        rankIndex = index,
                        onSendWhatsApp = {
                            viewModel.dispatchToExpert(context, ranked)
                        },
                        onCall = {
                            viewModel.callExpert(context, ranked.expert.phone)
                        },
                        onViewOnMap = {
                            WhatsAppHelper.openGoogleMaps(context, ranked.expert.latitude, ranked.expert.longitude, ranked.expert.name)
                        }
                    )
                }
            }
        } else {
            // ORDERS HISTORY & STATUS SECTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Customer Orders Log (${jobs.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sabhi customer requests aur unka dispatch status track karein",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (jobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Abhi koi customer order nahi hai. 'Dispatch Order' par jakar pehla order banayein!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(jobs, key = { it.id }) { job ->
                    JobStatusCard(
                        job = job,
                        onStatusChange = { newStatus -> viewModel.updateJobStatus(job.id, newStatus) },
                        onCallCustomer = { WhatsAppHelper.openDialer(context, job.customerPhone) },
                        onCallExpert = {
                            job.assignedExpertPhone?.let { phone ->
                                WhatsAppHelper.openDialer(context, phone)
                            }
                        },
                        onReDispatch = {
                            viewModel.loadJobIntoForm(job)
                            subSection = 0
                        },
                        onDelete = { viewModel.deleteJob(job) },
                        onViewCustomerMap = {
                            WhatsAppHelper.openGoogleMaps(context, job.latitude, job.longitude, job.customerName)
                        }
                    )
                }
            }
        }
    }
}
