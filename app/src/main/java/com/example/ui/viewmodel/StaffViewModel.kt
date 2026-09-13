package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.IntegrationService
import com.example.data.repository.ReportExporter
import com.example.data.repository.StaffRepository
import com.example.data.sync.BackgroundSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class StaffKpis(
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val availabilityPercentage: Int = 100,
    val onVacationCount: Int = 0,
    val onLeaveCount: Int = 0,
    val pendingRequestsCount: Int = 0,
    val shiftsTodayCount: Int = 0,
    val completedTasksTodayCount: Int = 0,
    val totalTasksTodayCount: Int = 0,
    val taskCompletionRate: Int = 0
)

data class IntegrationsConfig(
    val slackWebhookUrl: String = "https://hooks.slack.com/services/T000/B000/XXXX",
    val slackChannel: String = "#turnos-rrhh",
    val emailRecipient: String = "rrhh@empresa.com",
    val autoNotifyOnTimeOff: Boolean = true,
    val autoNotifyOnApproval: Boolean = true
)

class StaffViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = StaffRepository(database)
    val integrationService = IntegrationService(application)
    val reportExporter = ReportExporter(application)

    // Companies & Multi-Tenant Scoping
    val companies: StateFlow<List<CompanyEnvironment>> = repository.companies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCompanyCode = MutableStateFlow("S1-CORP")
    val currentCompanyCode: StateFlow<String> = _currentCompanyCode.asStateFlow()

    val currentCompany: StateFlow<CompanyEnvironment?> = combine(
        companies,
        _currentCompanyCode
    ) { list, code ->
        list.find { it.code.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectCompany(code: String) {
        _currentCompanyCode.value = code.trim().uppercase()
    }

    // Scoped flows from Room by currentCompanyCode
    val employees: StateFlow<List<Employee>> = combine(repository.employees, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<Shift>> = combine(repository.shifts, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeOffRequests: StateFlow<List<TimeOffRequest>> = combine(repository.timeOffRequests, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<DailyTask>> = combine(repository.tasks, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<PerformanceReview>> = combine(repository.reviews, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationLog>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clockEntries: StateFlow<List<TimeClockEntry>> = combine(repository.clockEntries, _currentCompanyCode) { list, code ->
        list.filter { it.companyCode.equals(code, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingClockCount: StateFlow<Int> = clockEntries.map { list ->
        list.count { it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.FAILED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isNetworkAvailable = MutableStateFlow(integrationService.isNetworkAvailable())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    fun refreshNetworkStatus() {
        _isNetworkAvailable.value = integrationService.isNetworkAvailable()
    }

    // Current logged-in user & active role for RBAC
    private val _loggedInEmployee = MutableStateFlow<Employee?>(null)
    val loggedInEmployee: StateFlow<Employee?> = _loggedInEmployee.asStateFlow()

    private val _currentUserRole = MutableStateFlow(SystemRole.EMPLOYEE)
    val currentUserRole: StateFlow<SystemRole> = _currentUserRole.asStateFlow()

    private val _currentUserName = MutableStateFlow("Invitado")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    // Configurable permissions
    private val _adminPermissions = MutableStateFlow(RolePermissions())
    val adminPermissions: StateFlow<RolePermissions> = _adminPermissions.asStateFlow()

    private val _managerPermissions = MutableStateFlow(
        RolePermissions(
            canManageEmployees = false,
            canAssignShifts = true,
            canApproveTimeOff = true,
            canAssignTasks = true,
            canReviewPerformance = true,
            canExportReports = true,
            canConfigSystem = false
        )
    )
    val managerPermissions: StateFlow<RolePermissions> = _managerPermissions.asStateFlow()

    private val _employeePermissions = MutableStateFlow(
        RolePermissions(
            canManageEmployees = false,
            canAssignShifts = false,
            canApproveTimeOff = false,
            canAssignTasks = false,
            canReviewPerformance = false,
            canExportReports = false,
            canConfigSystem = false
        )
    )
    val employeePermissions: StateFlow<RolePermissions> = _employeePermissions.asStateFlow()

    // Active permissions computed by role
    val currentPermissions: StateFlow<RolePermissions> = combine(
        _currentUserRole,
        _adminPermissions,
        _managerPermissions,
        _employeePermissions
    ) { role, admin, manager, emp ->
        when (role) {
            SystemRole.ADMIN -> admin
            SystemRole.MANAGER -> manager
            SystemRole.EMPLOYEE -> emp
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RolePermissions())

    // Integrations config
    private val _integrationsConfig = MutableStateFlow(IntegrationsConfig())
    val integrationsConfig: StateFlow<IntegrationsConfig> = _integrationsConfig.asStateFlow()

    // Filters
    private val _selectedDepartment = MutableStateFlow("Todos")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    private val _selectedProject = MutableStateFlow("Todos")
    val selectedProject: StateFlow<String> = _selectedProject.asStateFlow()

    private val _selectedArea = MutableStateFlow("Todos")
    val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Status messages for Snackbars / Dialogs
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Filtered employees
    val filteredEmployees: StateFlow<List<Employee>> = combine(
        employees,
        _selectedDepartment,
        _selectedProject,
        _selectedArea,
        _searchQuery
    ) { list, dept, proj, area, query ->
        list.filter { emp ->
            val matchDept = dept == "Todos" || emp.department.equals(dept, ignoreCase = true)
            val matchProj = proj == "Todos" || emp.project.equals(proj, ignoreCase = true)
            val matchArea = area == "Todos" || emp.functionalArea.equals(area, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                    emp.name.contains(query, ignoreCase = true) ||
                    emp.jobTitle.contains(query, ignoreCase = true) ||
                    emp.email.contains(query, ignoreCase = true)
            matchDept && matchProj && matchArea && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-Time KPIs
    val kpis: StateFlow<StaffKpis> = combine(
        employees,
        shifts,
        timeOffRequests,
        tasks
    ) { empList, shiftList, reqList, taskList ->
        val todayStr = LocalDate.now().toString()
        val total = empList.size
        val active = empList.count { it.status == EmployeeStatus.ACTIVO }
        val vacation = empList.count { it.status == EmployeeStatus.VACACIONES }
        val leave = empList.count { it.status == EmployeeStatus.BAJA_MEDICA }
        val availPct = if (total > 0) ((active.toFloat() / total) * 100).toInt() else 0

        val pending = reqList.count { it.status == RequestStatus.PENDIENTE }
        val shiftsToday = shiftList.count { it.date == todayStr }

        val todayTasks = taskList.filter { it.date == todayStr }
        val completedTasks = todayTasks.count { it.status == TaskStatus.COMPLETADA }
        val taskRate = if (todayTasks.isNotEmpty()) ((completedTasks.toFloat() / todayTasks.size) * 100).toInt() else 100

        StaffKpis(
            totalEmployees = total,
            activeEmployees = active,
            availabilityPercentage = availPct,
            onVacationCount = vacation,
            onLeaveCount = leave,
            pendingRequestsCount = pending,
            shiftsTodayCount = shiftsToday,
            completedTasksTodayCount = completedTasks,
            totalTasksTodayCount = todayTasks.size,
            taskCompletionRate = taskRate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StaffKpis())

    // User Role Switcher & Profile Navigation
    // Solo un Administrador puede alternar entre Modo Administrador y Modo Empleado
    fun switchRoleMode(targetRole: SystemRole): Boolean {
        val logged = _loggedInEmployee.value
        if (logged == null) {
            _currentUserRole.value = targetRole
            return true
        }
        if (logged.systemRole != SystemRole.ADMIN) {
            _statusMessage.value = "Los empleados solo pueden usar el rol asignado por el Administrador (${logged.systemRole.label})"
            return false
        }
        if (targetRole == SystemRole.ADMIN || targetRole == SystemRole.EMPLOYEE) {
            _currentUserRole.value = targetRole
            val modeTitle = if (targetRole == SystemRole.ADMIN) "Modo Administrador (Control Total)" else "Modo Empleado (personal)"
            _currentUserName.value = "${logged.name} ($modeTitle)"
            _statusMessage.value = "Vista cambiada a: $modeTitle"
            return true
        }
        return false
    }

    fun setCurrentUserRole(role: SystemRole) {
        switchRoleMode(role)
    }

    // Iniciar sesión con correo y contraseña en un entorno empresarial
    fun loginWithCredentials(
        companyCode: String,
        email: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanCode = companyCode.trim().uppercase()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanCode.isBlank()) {
            onResult(false, "Introduce el código de empresa (ej: S1-CORP)")
            return
        }
        if (cleanEmail.isBlank()) {
            onResult(false, "Introduce tu correo electrónico")
            return
        }
        if (cleanPassword.isBlank()) {
            onResult(false, "Introduce tu contraseña de acceso")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val company = repository.getCompanyByCode(cleanCode)
            if (company == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "No se encontró la empresa con código '$cleanCode'. Verifica el código con tu Administrador.")
                }
                return@launch
            }

            val existing = repository.getEmployeeByCompanyAndEmail(cleanCode, cleanEmail)
            if (existing == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "No hay ningún empleado registrado con el correo $cleanEmail en la empresa '$cleanCode'. Tu Administrador debe darte de alta previamente.")
                }
                return@launch
            }

            val validPass = existing.passwordHash.isBlank() || existing.passwordHash == cleanPassword
            if (!validPass) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Contraseña incorrecta. Consulta con el Administrador de tu empresa para verificar tus credenciales.")
                }
                return@launch
            }

            // Si Firebase Auth está habilitado y hay red, autenticar en Firebase
            if (integrationService.isFirebaseConfigured() && integrationService.isNetworkAvailable()) {
                integrationService.signInWithFirebaseAuth(cleanEmail, cleanPassword)
            }

            _currentCompanyCode.value = cleanCode
            _loggedInEmployee.value = existing
            _currentUserRole.value = existing.systemRole
            val roleDesc = if (existing.isMasterAdmin) "Administrador Maestro 👑" else existing.systemRole.label
            _currentUserName.value = "${existing.name} ($roleDesc)"
            _statusMessage.value = "Sesión iniciada en ${company.name} como ${existing.name} ($roleDesc)"
            withContext(Dispatchers.Main) {
                onResult(true, "¡Bienvenido de nuevo, ${existing.name}! Acceso verificado en ${company.name} ($roleDesc).")
            }
        }
    }

    // Sobrecarga compatible
    fun loginWithCredentials(
        email: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        loginWithCredentials(_currentCompanyCode.value, email, password, onResult)
    }

    // Crear un nuevo Entorno Empresarial con su Administrador Maestro
    fun registerCompanyAndAdmin(
        companyName: String,
        companyCode: String,
        adminName: String,
        adminEmail: String,
        adminPassword: String,
        adminPhone: String = "",
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanName = companyName.trim()
        val cleanCode = companyCode.trim().uppercase()
        val cleanAdminName = adminName.trim()
        val cleanEmail = adminEmail.trim().lowercase()
        val cleanPass = adminPassword.trim()
        val cleanPhone = adminPhone.trim()

        if (cleanName.isBlank()) {
            onResult(false, "Por favor introduce el nombre comercial de la empresa")
            return
        }
        if (cleanCode.length < 3) {
            onResult(false, "El código de empresa debe tener al menos 3 caracteres (ej: S1-CORP, ACME)")
            return
        }
        if (cleanAdminName.isBlank()) {
            onResult(false, "Introduce el nombre completo del Administrador")
            return
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onResult(false, "Introduce un correo electrónico civil o corporativo válido")
            return
        }
        if (cleanPass.length < 4) {
            onResult(false, "La contraseña debe tener al menos 4 caracteres")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existingCompany = repository.getCompanyByCode(cleanCode)
            if (existingCompany != null) {
                onResult(false, "El código de empresa '$cleanCode' ya existe. Si eres el administrador, inicia sesión.")
                return@launch
            }

            val company = CompanyEnvironment(
                code = cleanCode,
                name = cleanName,
                adminEmail = cleanEmail,
                adminName = cleanAdminName
            )
            repository.insertCompany(company)
            integrationService.syncCompanyToCloud(company)

            val admin = Employee(
                companyCode = cleanCode,
                name = cleanAdminName,
                email = cleanEmail,
                phone = cleanPhone,
                passwordHash = cleanPass,
                isMasterAdmin = true,
                authProvider = "LOCAL",
                jobTitle = "Director / Administrador",
                department = "Dirección General",
                project = "General",
                functionalArea = "Dirección",
                systemRole = SystemRole.ADMIN,
                status = EmployeeStatus.ACTIVO,
                avatarColorHex = 0xFF2563EB,
                hireDate = LocalDate.now().toString(),
                notes = "Administrador fundador de $cleanName"
            )
            val adminId = repository.insertEmployee(admin)
            val createdAdmin = admin.copy(id = adminId)

            integrationService.createFirebaseAccount(cleanEmail, cleanPass)
            integrationService.syncEmployeeToCloud(createdAdmin)

            _currentCompanyCode.value = cleanCode
            _loggedInEmployee.value = createdAdmin
            _currentUserRole.value = SystemRole.ADMIN
            _currentUserName.value = "${createdAdmin.name} (Administrador Maestro 👑)"

            repository.addNotification(
                NotificationLog(
                    title = "Entorno Empresarial Creado",
                    message = "Empresa $cleanName registrada con código $cleanCode. Como administrador, da de alta a tus empleados en el módulo Personal.",
                    channel = "SISTEMA"
                )
            )

            val welcomeMsg = "¡Entorno empresarial '$cleanName' ($cleanCode) creado con éxito! Como Administrador Maestro, ahora puedes dar de alta a tus empleados desde el módulo Personal y proporcionarles sus claves de acceso."
            _statusMessage.value = welcomeMsg
            withContext(Dispatchers.Main) {
                onResult(true, welcomeMsg)
            }
        }
    }

    // Inicio con Cuenta de Google (Validando asignación previa por el Administrador)
    fun loginWithGoogle(
        googleEmail: String,
        googleName: String,
        companyCode: String = _currentCompanyCode.value,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanEmail = googleEmail.trim().lowercase()
        val cleanName = googleName.trim()
        val cleanCode = companyCode.trim().uppercase()

        if (cleanEmail.isBlank()) {
            onResult(false, "Correo de Google no válido")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val company = repository.getCompanyByCode(cleanCode)
            if (company == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "No se encontró la empresa con código '$cleanCode'.")
                }
                return@launch
            }

            val existing = repository.getEmployeeByCompanyAndEmail(cleanCode, cleanEmail)
            if (existing != null) {
                val updated = if (existing.authProvider != "GOOGLE") {
                    val m = existing.copy(authProvider = "GOOGLE")
                    repository.updateEmployee(m)
                    m
                } else existing
                _currentCompanyCode.value = cleanCode
                _loggedInEmployee.value = updated
                _currentUserRole.value = updated.systemRole
                val roleDesc = if (updated.isMasterAdmin) "Administrador Maestro 👑" else updated.systemRole.label
                _currentUserName.value = "${updated.name} ($roleDesc)"
                _statusMessage.value = "Sesión iniciada con Google: ${updated.name} en ${company.name}"
                withContext(Dispatchers.Main) {
                    onResult(true, "Acceso con Google verificado en ${company.name}. ¡Bienvenido/a, ${updated.name}!")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, "No tienes cuenta asignada en la empresa ${company.name} ($cleanCode). Contacta con el Administrador para que cree tu perfil de empleado.")
                }
            }
        }
    }

    // Deprecated compat login wrapper
    fun login(
        name: String,
        email: String,
        phone: String = "",
        onResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        loginWithCredentials(_currentCompanyCode.value, email, "123456", onResult)
    }

    fun loginWithEmployee(employee: Employee) {
        _loggedInEmployee.value = employee
        _currentUserRole.value = employee.systemRole
        val roleDesc = if (employee.isMasterAdmin) "Administrador Maestro 👑" else employee.systemRole.label
        _currentUserName.value = "${employee.name} ($roleDesc)"
        _statusMessage.value = "Sesión iniciada: ${employee.name} ($roleDesc)"
    }

    fun logout() {
        _loggedInEmployee.value = null
        _currentUserRole.value = SystemRole.EMPLOYEE
        _currentUserName.value = "Invitado"
        _statusMessage.value = "Sesión cerrada correctamente"
    }

    fun updateIntegrationsConfig(newConfig: IntegrationsConfig) {
        _integrationsConfig.value = newConfig
        _statusMessage.value = "Configuración de integraciones actualizada"
    }

    // Filter Setters
    fun setDepartmentFilter(dept: String) { _selectedDepartment.value = dept }
    fun setProjectFilter(proj: String) { _selectedProject.value = proj }
    fun setAreaFilter(area: String) { _selectedArea.value = area }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    // Employee Actions
    fun addEmployee(employee: Employee) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedEmp = employee.copy(companyCode = _currentCompanyCode.value)
            val newId = repository.insertEmployee(scopedEmp)
            val savedEmp = scopedEmp.copy(id = newId)
            integrationService.syncEmployeeToCloud(savedEmp)

            repository.addNotification(
                NotificationLog(
                    title = "Nuevo empleado asignado",
                    message = "${savedEmp.name} registrado en ${savedEmp.department} para la empresa ${_currentCompanyCode.value}.",
                    channel = "SISTEMA"
                )
            )
            _statusMessage.value = "Empleado ${savedEmp.name} registrado correctamente con credenciales asignadas"
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedEmp = employee.copy(companyCode = _currentCompanyCode.value)
            repository.updateEmployee(scopedEmp)
            integrationService.syncEmployeeToCloud(scopedEmp)
            _statusMessage.value = "Datos de ${employee.name} actualizados"
        }
    }

    fun deleteEmployee(employee: Employee) {
        if (employee.isMasterAdmin) {
            _statusMessage.value = "Operación denegada: El Administrador Maestro es una cuenta protegida y no puede ser borrada del sistema."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEmployee(employee)
            repository.addNotification(
                NotificationLog(
                    title = "Empleado retirado",
                    message = "${employee.name} ha sido dado de baja de la empresa ${_currentCompanyCode.value}.",
                    channel = "SISTEMA"
                )
            )
            _statusMessage.value = "Empleado ${employee.name} eliminado"
        }
    }

    // Shift Actions
    fun addShift(shift: Shift) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedShift = shift.copy(companyCode = _currentCompanyCode.value)
            repository.insertShift(scopedShift)
            val msg = "Turno ${shift.shiftType.label} asignado a ${shift.employeeName} para el ${shift.date} (${shift.startTime}-${shift.endTime})."
            repository.addNotification(
                NotificationLog(
                    title = "Turno asignado",
                    message = msg,
                    channel = "SISTEMA"
                )
            )
            // Auto dispatch to Slack if enabled
            if (_integrationsConfig.value.autoNotifyOnTimeOff && _integrationsConfig.value.slackWebhookUrl.isNotBlank()) {
                integrationService.sendSlackNotification(
                    webhookUrl = _integrationsConfig.value.slackWebhookUrl,
                    channel = _integrationsConfig.value.slackChannel,
                    title = "Turno Asignado: ${shift.employeeName}",
                    message = msg
                )
            }
            _statusMessage.value = "Turno asignado correctamente"
        }
    }

    fun deleteShift(shift: Shift) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShift(shift)
            _statusMessage.value = "Turno eliminado"
        }
    }

    // Time-off Actions
    fun requestTimeOff(request: TimeOffRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedRequest = request.copy(companyCode = _currentCompanyCode.value)
            repository.insertTimeOff(scopedRequest)
            val dayTypeLabel = if (request.customDayType.isNotBlank()) request.customDayType else request.type.label
            val msg = "${request.employeeName} solicitó ${request.daysCount} días de $dayTypeLabel (${request.startDate} a ${request.endDate}). Motivo: ${request.reason}"
            repository.addNotification(
                NotificationLog(
                    title = "Solicitud de tiempo libre ($dayTypeLabel)",
                    message = msg,
                    channel = "SISTEMA"
                )
            )

            // Automatic notification to Slack
            if (_integrationsConfig.value.autoNotifyOnTimeOff && _integrationsConfig.value.slackWebhookUrl.isNotBlank()) {
                val result = integrationService.sendSlackNotification(
                    webhookUrl = _integrationsConfig.value.slackWebhookUrl,
                    channel = _integrationsConfig.value.slackChannel,
                    title = "🔔 Nueva Solicitud de Tiempo Libre",
                    message = msg
                )
                if (result.isSuccess) {
                    repository.addNotification(
                        NotificationLog(
                            title = "Notificación enviada a Slack",
                            message = "Canal ${_integrationsConfig.value.slackChannel}: $msg",
                            channel = "SLACK"
                        )
                    )
                }
            }

            _statusMessage.value = "Solicitud enviada para aprobación"
        }
    }

    fun reviewTimeOffRequest(requestId: Long, newStatus: RequestStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            val reviewer = _currentUserName.value
            repository.updateTimeOffStatus(requestId, newStatus, reviewer)

            val statusText = if (newStatus == RequestStatus.APROBADO) "APROBADA" else "RECHAZADA"
            val msg = "La solicitud #$requestId ha sido $statusText por $reviewer."
            repository.addNotification(
                NotificationLog(
                    title = "Solicitud de Tiempo Libre $statusText",
                    message = msg,
                    channel = "SISTEMA"
                )
            )

            // Automatic notification dispatch to Slack
            if (_integrationsConfig.value.autoNotifyOnApproval && _integrationsConfig.value.slackWebhookUrl.isNotBlank()) {
                integrationService.sendSlackNotification(
                    webhookUrl = _integrationsConfig.value.slackWebhookUrl,
                    channel = _integrationsConfig.value.slackChannel,
                    title = "Aprobación de Solicitud",
                    message = msg
                )
                repository.addNotification(
                    NotificationLog(
                        title = "Notificación de aprobación a Slack",
                        message = msg,
                        channel = "SLACK"
                    )
                )
            }

            _statusMessage.value = "Solicitud $statusText"
        }
    }

    // Daily Tasks
    fun addTask(task: DailyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedTask = task.copy(companyCode = _currentCompanyCode.value)
            repository.insertTask(scopedTask)
            _statusMessage.value = "Tarea asignada a ${task.employeeName}"
        }
    }

    fun toggleTaskStatus(task: DailyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextStatus = when (task.status) {
                TaskStatus.PENDIENTE -> TaskStatus.EN_PROGRESO
                TaskStatus.EN_PROGRESO -> TaskStatus.COMPLETADA
                TaskStatus.COMPLETADA -> TaskStatus.PENDIENTE
            }
            repository.updateTask(task.copy(status = nextStatus))
        }
    }

    fun deleteTask(task: DailyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
            _statusMessage.value = "Tarea eliminada"
        }
    }

    // Performance Reviews
    fun addPerformanceReview(review: PerformanceReview) {
        viewModelScope.launch(Dispatchers.IO) {
            val scopedReview = review.copy(companyCode = _currentCompanyCode.value)
            repository.insertReview(scopedReview)
            repository.addNotification(
                NotificationLog(
                    title = "Evaluación de desempeño registrada",
                    message = "Evaluación de ${review.employeeName}: ${review.overallRating}★ por ${review.reviewerName}.",
                    channel = "SISTEMA"
                )
            )
            _statusMessage.value = "Evaluación de desempeño guardada"
        }
    }

    fun deleteReview(review: PerformanceReview) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReview(review)
            _statusMessage.value = "Evaluación eliminada"
        }
    }

    // Notifications
    fun markAllNotificationsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearNotifications()
        }
    }

    // Reports Export
    fun exportPdfReport(onReady: (Uri) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = reportExporter.generateAndSharePdfReport(
                employees = employees.value,
                shifts = shifts.value,
                timeOffs = timeOffRequests.value,
                tasks = tasks.value,
                departmentFilter = _selectedDepartment.value
            )
            result.onSuccess { uri ->
                _statusMessage.value = "Informe PDF generado con éxito"
                onReady(uri)
            }.onFailure {
                _statusMessage.value = "Error al generar informe PDF: ${it.localizedMessage}"
            }
        }
    }

    fun exportExcelCsvReport(onReady: (Uri) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = reportExporter.generateAndShareExcelCsvReport(
                employees = employees.value,
                shifts = shifts.value,
                timeOffs = timeOffRequests.value,
                tasks = tasks.value,
                departmentFilter = _selectedDepartment.value
            )
            result.onSuccess { uri ->
                _statusMessage.value = "Informe Excel/CSV exportado con éxito"
                onReady(uri)
            }.onFailure {
                _statusMessage.value = "Error al exportar Excel: ${it.localizedMessage}"
            }
        }
    }

    // Integrations Test
    fun testSlackNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            val config = _integrationsConfig.value
            val result = integrationService.sendSlackNotification(
                webhookUrl = config.slackWebhookUrl,
                channel = config.slackChannel,
                title = "🧪 Notificación de Prueba — S1",
                message = "La integración de S1 con Slack está funcionando correctamente en el canal ${config.slackChannel}."
            )
            result.onSuccess {
                repository.addNotification(
                    NotificationLog(
                        title = "Prueba de Slack exitosa",
                        message = "Notificación de prueba enviada al canal ${config.slackChannel}.",
                        channel = "SLACK"
                    )
                )
                _statusMessage.value = "Prueba de Slack enviada correctamente"
            }.onFailure {
                _statusMessage.value = "Fallo al enviar a Slack: ${it.localizedMessage}"
            }
        }
    }

    fun testEmailNotification() {
        val config = _integrationsConfig.value
        val subject = "[S1] Notificación Corporativa de Personal"
        val body = """
            Estimado/a responsable de Recursos Humanos,
            
            Este es un correo de verificación del sistema S1.
            Resumen del estado del personal:
            - Total de empleados: ${employees.value.size}
            - Disponibilidad actual: ${kpis.value.availabilityPercentage}%
            - Solicitudes pendientes: ${kpis.value.pendingRequestsCount}
            
            Fecha: ${LocalDate.now()}
        """.trimIndent()

        val opened = integrationService.openEmailClient(
            recipientEmail = config.emailRecipient,
            subject = subject,
            body = body
        )
        if (opened) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addNotification(
                    NotificationLog(
                        title = "Correo de prueba preparado",
                        message = "Plantilla enviada hacia ${config.emailRecipient}.",
                        channel = "EMAIL"
                    )
                )
            }
            _statusMessage.value = "Cliente de correo abierto con la plantilla lista"
        } else {
            _statusMessage.value = "No se pudo abrir el cliente de correo"
        }
    }

    init {
        // Iniciar sincronización periódica silenciosa con WorkManager cada 15 minutos
        BackgroundSyncWorker.enqueuePeriodicSync(application)
    }

    // Registro de Jornada y Fichaje (Modo Online y Offline) con WorkManager
    fun registerClockEntry(
        employee: Employee,
        clockType: ClockType,
        locationTag: String = "Sede Central - Acceso"
    ) {
        val now = java.time.LocalDateTime.now()
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val isOnline = integrationService.isNetworkAvailable()
        _isNetworkAvailable.value = isOnline

        val entry = TimeClockEntry(
            companyCode = employee.companyCode.ifBlank { _currentCompanyCode.value },
            employeeId = employee.id,
            employeeName = employee.name,
            department = employee.department,
            clockType = clockType,
            timestamp = System.currentTimeMillis(),
            formattedTime = now.format(timeFormatter),
            formattedDate = now.format(dateFormatter),
            locationTag = locationTag,
            syncStatus = if (isOnline) SyncStatus.SYNCING else SyncStatus.PENDING
        )

        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = repository.insertClockEntry(entry)
            val insertedEntry = entry.copy(id = insertedId)

            if (isOnline) {
                val syncResult = integrationService.syncClockEntryToCloud(insertedEntry)
                if (syncResult.isSuccess) {
                    val serverId = syncResult.getOrNull() ?: "SRV-${System.currentTimeMillis()}"
                    repository.updateClockEntry(
                        insertedEntry.copy(
                            syncStatus = SyncStatus.SYNCED,
                            serverSyncId = serverId,
                            lastSyncAttemptAt = System.currentTimeMillis()
                        )
                    )
                    _statusMessage.value = "✅ ${clockType.label} registrada y sincronizada con el Servidor Cloud ($serverId)."
                } else {
                    repository.updateClockEntry(
                        insertedEntry.copy(
                            syncStatus = SyncStatus.PENDING,
                            syncAttempts = 1,
                            lastSyncAttemptAt = System.currentTimeMillis()
                        )
                    )
                    BackgroundSyncWorker.enqueueImmediateSync(getApplication())
                    _statusMessage.value = "⚠️ Fichaje guardado en local (Modo Offline). En cola para sincronización automática."
                }
            } else {
                // Sin cobertura -> Guardar silenciosamente en Room y encolar sincronización
                BackgroundSyncWorker.enqueueImmediateSync(getApplication())
                _statusMessage.value = "🏢 Fichaje registrado en local (Modo Offline). Se sincronizará automáticamente al recuperar conexión."
            }

            repository.addNotification(
                NotificationLog(
                    title = "Registro de Jornada: ${clockType.label}",
                    message = "${employee.name} ha fichado (${clockType.label}) a las ${entry.formattedTime}. Ubicación: $locationTag.",
                    channel = "SISTEMA"
                )
            )
        }
    }

    fun triggerWorkManagerSync() {
        viewModelScope.launch(Dispatchers.IO) {
            _isNetworkAvailable.value = integrationService.isNetworkAvailable()
            BackgroundSyncWorker.enqueueImmediateSync(getApplication())
            val report = integrationService.forceFullSync(database, _currentCompanyCode.value)
            _statusMessage.value = report.message
        }
    }
}
