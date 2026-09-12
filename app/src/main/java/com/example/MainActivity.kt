package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RequestStatus
import com.example.data.model.SystemRole
import com.example.ui.components.NotificationDialog
import com.example.ui.components.StaffTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StaffViewModel

enum class NavigationDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Panel", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    PERSONNEL("Personal", Icons.Filled.People, Icons.Outlined.People),
    SHIFTS("Turnos", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    TIMEOFF("Vacaciones", Icons.Filled.BeachAccess, Icons.Outlined.BeachAccess),
    TASKS("Tareas", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    PERFORMANCE("Desempeño", Icons.Filled.Star, Icons.Outlined.StarOutline),
    REPORTS("Informes", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    ADMIN("Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: StaffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: StaffViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf(NavigationDestination.DASHBOARD) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // State collections
    val loggedInEmployee by viewModel.loggedInEmployee.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val permissions by viewModel.currentPermissions.collectAsStateWithLifecycle()
    val adminPermissions by viewModel.adminPermissions.collectAsStateWithLifecycle()
    val managerPermissions by viewModel.managerPermissions.collectAsStateWithLifecycle()
    val employeePermissions by viewModel.employeePermissions.collectAsStateWithLifecycle()
    val integrationsConfig by viewModel.integrationsConfig.collectAsStateWithLifecycle()

    val kpis by viewModel.kpis.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val timeOffRequests by viewModel.timeOffRequests.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val selectedDept by viewModel.selectedDepartment.collectAsStateWithLifecycle()
    val selectedProj by viewModel.selectedProject.collectAsStateWithLifecycle()
    val selectedArea by viewModel.selectedArea.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (loggedInEmployee == null) {
        LoginScreen(
            employees = employees,
            onLoginWithCredentials = { email, password, onResult ->
                viewModel.loginWithCredentials(email, password, onResult)
            },
            onRegister = { name, email, password, phone, onResult ->
                viewModel.registerAccount(name, email, password, phone, onResult)
            },
            onGoogleSignIn = { email, name, onResult ->
                viewModel.loginWithGoogle(email, name, onResult)
            },
            onQuickLogin = { emp ->
                viewModel.loginWithEmployee(emp)
            }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                StaffTopBar(
                    currentRole = currentRole,
                    currentUserName = currentUserName,
                    unreadNotificationsCount = unreadNotificationsCount,
                    loggedInEmployee = loggedInEmployee,
                    onRoleChange = { viewModel.switchRoleMode(it) },
                    onOpenNotifications = { showNotificationsDialog = true },
                    onOpenAdmin = { currentDestination = NavigationDestination.ADMIN },
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                if (!isExpanded) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("mobile_bottom_navigation")
                    ) {
                        // Tailor mobile navigation depending on employee vs admin/manager role
                        val mobileDestinations = if (currentRole == SystemRole.EMPLOYEE) {
                            listOf(
                                NavigationDestination.DASHBOARD,
                                NavigationDestination.SHIFTS,
                                NavigationDestination.TIMEOFF,
                                NavigationDestination.TASKS,
                                NavigationDestination.PERFORMANCE
                            )
                        } else {
                            listOf(
                                NavigationDestination.DASHBOARD,
                                NavigationDestination.PERSONNEL,
                                NavigationDestination.SHIFTS,
                                NavigationDestination.TIMEOFF,
                                NavigationDestination.REPORTS
                            )
                        }
                        mobileDestinations.forEach { destination ->
                            val selected = currentDestination == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = { Text(destination.title) },
                                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Adaptive layout: NavigationRail for tablet/desktop
                if (isExpanded) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("desktop_navigation_rail")
                    ) {
                        NavigationDestination.values().forEach { destination ->
                            val selected = currentDestination == destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = { Text(destination.title) },
                                modifier = Modifier.testTag("rail_item_${destination.name.lowercase()}")
                            )
                        }
                    }
                }

                // Main screen view content
                Box(modifier = Modifier.weight(1f)) {
                    when (currentDestination) {
                        NavigationDestination.DASHBOARD -> {
                            DashboardScreen(
                                kpis = kpis,
                                employees = employees,
                                shifts = shifts,
                                pendingRequests = timeOffRequests.filter { it.status == RequestStatus.PENDIENTE },
                                canApproveTimeOff = permissions.canApproveTimeOff,
                                onApproveRequest = { id -> viewModel.reviewTimeOffRequest(id, RequestStatus.APROBADO) },
                                onRejectRequest = { id -> viewModel.reviewTimeOffRequest(id, RequestStatus.RECHAZADO) },
                                onNavigateToPersonnel = { currentDestination = NavigationDestination.PERSONNEL },
                                onNavigateToShifts = { currentDestination = NavigationDestination.SHIFTS },
                                onNavigateToTimeOff = { currentDestination = NavigationDestination.TIMEOFF }
                            )
                        }
                        NavigationDestination.PERSONNEL -> {
                            PersonnelScreen(
                                employees = filteredEmployees,
                                canManageEmployees = permissions.canManageEmployees,
                                selectedDepartment = selectedDept,
                                selectedProject = selectedProj,
                                selectedArea = selectedArea,
                                searchQuery = searchQuery,
                                onDepartmentSelected = { viewModel.setDepartmentFilter(it) },
                                onProjectSelected = { viewModel.setProjectFilter(it) },
                                onAreaSelected = { viewModel.setAreaFilter(it) },
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onAddEmployee = { viewModel.addEmployee(it) },
                                onUpdateEmployee = { viewModel.updateEmployee(it) },
                                onDeleteEmployee = { viewModel.deleteEmployee(it) }
                            )
                        }
                        NavigationDestination.SHIFTS -> {
                            ShiftsScreen(
                                shifts = shifts,
                                employees = employees,
                                tasks = tasks,
                                loggedInEmployee = loggedInEmployee,
                                canAssignShifts = permissions.canAssignShifts,
                                onAddShift = { viewModel.addShift(it) },
                                onDeleteShift = { viewModel.deleteShift(it) },
                                onToggleTaskStatus = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                        NavigationDestination.TIMEOFF -> {
                            TimeOffScreen(
                                requests = timeOffRequests,
                                employees = employees,
                                loggedInEmployee = loggedInEmployee,
                                canApprove = permissions.canApproveTimeOff,
                                onApprove = { id -> viewModel.reviewTimeOffRequest(id, RequestStatus.APROBADO) },
                                onReject = { id -> viewModel.reviewTimeOffRequest(id, RequestStatus.RECHAZADO) },
                                onRequestTimeOff = { viewModel.requestTimeOff(it) }
                            )
                        }
                        NavigationDestination.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                employees = employees,
                                loggedInEmployee = loggedInEmployee,
                                canAssignTasks = permissions.canAssignTasks,
                                onToggleTaskStatus = { viewModel.toggleTaskStatus(it) },
                                onAddTask = { viewModel.addTask(it) },
                                onDeleteTask = { viewModel.deleteTask(it) }
                            )
                        }
                        NavigationDestination.PERFORMANCE -> {
                            PerformanceScreen(
                                reviews = reviews,
                                employees = employees,
                                canReview = permissions.canReviewPerformance,
                                onAddReview = { viewModel.addPerformanceReview(it) },
                                onDeleteReview = { viewModel.deleteReview(it) }
                            )
                        }
                        NavigationDestination.REPORTS -> {
                            ReportsScreen(
                                employees = employees,
                                shifts = shifts,
                                timeOffs = timeOffRequests,
                                tasks = tasks,
                                selectedDepartment = selectedDept,
                                onDepartmentSelected = { viewModel.setDepartmentFilter(it) },
                                onExportPdf = { onReady -> viewModel.exportPdfReport(onReady) },
                                onExportExcel = { onReady -> viewModel.exportExcelCsvReport(onReady) },
                                onShareFile = { uri, mime, title -> viewModel.reportExporter.shareFile(uri, mime, title) }
                            )
                        }
                        NavigationDestination.ADMIN -> {
                            AdminScreen(
                                currentRole = currentRole,
                                currentUserName = currentUserName,
                                adminPermissions = adminPermissions,
                                managerPermissions = managerPermissions,
                                employeePermissions = employeePermissions,
                                integrationsConfig = integrationsConfig,
                                onRoleChange = { viewModel.setCurrentUserRole(it) },
                                onUpdateIntegrations = { viewModel.updateIntegrationsConfig(it) },
                                onTestSlack = { viewModel.testSlackNotification() },
                                onTestEmail = { viewModel.testEmailNotification() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onClearAll = { viewModel.clearAllNotifications() }
        )
    }
}
