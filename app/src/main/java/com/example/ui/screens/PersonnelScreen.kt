package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelScreen(
    employees: List<Employee>,
    canManageEmployees: Boolean,
    selectedDepartment: String,
    selectedProject: String,
    selectedArea: String,
    searchQuery: String,
    onDepartmentSelected: (String) -> Unit,
    onProjectSelected: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddEmployee: (Employee) -> Unit,
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    // Grouping dimension selector: "Departamento", "Proyecto", "Área Funcional"
    var groupDimension by remember { mutableStateOf("Departamento") }

    Scaffold(
        modifier = modifier.testTag("personnel_screen"),
        floatingActionButton = {
            if (canManageEmployees) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Añadir Personal") },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_employee_fab")
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_search_field"),
                    placeholder = { Text("Buscar por nombre, cargo o email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }

            // Organizational grouping tab chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Agrupar y Filtrar por:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = groupDimension == "Departamento",
                            onClick = { groupDimension = "Departamento" },
                            label = { Text("Departamentos") },
                            leadingIcon = if (groupDimension == "Departamento") {
                                { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = groupDimension == "Proyecto",
                            onClick = { groupDimension = "Proyecto" },
                            label = { Text("Proyectos") },
                            leadingIcon = if (groupDimension == "Proyecto") {
                                { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = groupDimension == "Área Funcional",
                            onClick = { groupDimension = "Área Funcional" },
                            label = { Text("Áreas Funcionales") },
                            leadingIcon = if (groupDimension == "Área Funcional") {
                                { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    // Sub-chips for the chosen dimension
                    val subOptions = when (groupDimension) {
                        "Departamento" -> listOf("Todos", "Tecnología", "Ventas", "Operaciones", "Recursos Humanos", "Finanzas", "Soporte")
                        "Proyecto" -> listOf("Todos", "Proyecto Core", "Expansión Digital", "Migración Cloud", "Soporte 24/7", "General")
                        else -> listOf("Todos", "Ingeniería", "Marketing y Ventas", "Logística", "Atención al Cliente", "Administración")
                    }
                    val currentSelected = when (groupDimension) {
                        "Departamento" -> selectedDepartment
                        "Proyecto" -> selectedProject
                        else -> selectedArea
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subOptions.forEach { opt ->
                            FilterChip(
                                selected = currentSelected == opt,
                                onClick = {
                                    when (groupDimension) {
                                        "Departamento" -> onDepartmentSelected(opt)
                                        "Proyecto" -> onProjectSelected(opt)
                                        else -> onAreaSelected(opt)
                                    }
                                },
                                label = { Text(opt) }
                            )
                        }
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Directorio (${employees.size} personas)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (employees.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No se encontraron empleados con los filtros aplicados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(employees, key = { it.id }) { emp ->
                    EmployeeCard(
                        employee = emp,
                        canManage = canManageEmployees,
                        onEdit = { employeeToEdit = emp },
                        onDelete = { employeeToDelete = emp }
                    )
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || employeeToEdit != null) {
        EmployeeFormDialog(
            initialEmployee = employeeToEdit,
            onDismiss = {
                showAddDialog = false
                employeeToEdit = null
            },
            onSave = { emp ->
                if (employeeToEdit != null) {
                    onUpdateEmployee(emp)
                } else {
                    onAddEmployee(emp)
                }
                showAddDialog = false
                employeeToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (employeeToDelete != null) {
        val emp = employeeToDelete!!
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Eliminar Empleado") },
            text = { Text("¿Deseas dar de baja a ${emp.name}? Esta acción eliminará su registro de la base de datos.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEmployee(emp)
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("employee_card_${employee.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Avatar
                val initials = employee.name.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(employee.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (employee.isMasterAdmin) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberWarning.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "👑 Admin Maestro (Protegido)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (employee.systemRole == SystemRole.ADMIN) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = employee.jobTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(employee.status.colorHex).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(employee.status.colorHex))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = employee.status.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(employee.status.colorHex)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Organizational Chips: Dept, Project, Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(employee.department, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(employee.project, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(employee.functionalArea, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(employee.workSchedulePattern.shortLabel, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = employee.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = employee.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (canManage) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    if (employee.isMasterAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Protegido (no borrable)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = RoseError)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Baja")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeFormDialog(
    initialEmployee: Employee?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(initialEmployee?.name ?: "") }
    var email by remember { mutableStateOf(initialEmployee?.email ?: "") }
    var phone by remember { mutableStateOf(initialEmployee?.phone ?: "") }
    var jobTitle by remember { mutableStateOf(initialEmployee?.jobTitle ?: "") }
    var department by remember { mutableStateOf(initialEmployee?.department ?: "Tecnología") }
    var project by remember { mutableStateOf(initialEmployee?.project ?: "Proyecto Core") }
    var functionalArea by remember { mutableStateOf(initialEmployee?.functionalArea ?: "Ingeniería") }
    var systemRole by remember { mutableStateOf(initialEmployee?.systemRole ?: SystemRole.EMPLOYEE) }
    var workSchedulePattern by remember { mutableStateOf(initialEmployee?.workSchedulePattern ?: WorkSchedulePattern.LUNES_A_VIERNES) }
    var status by remember { mutableStateOf(initialEmployee?.status ?: EmployeeStatus.ACTIVO) }
    var notes by remember { mutableStateOf(initialEmployee?.notes ?: "") }
    var accessPassword by remember { mutableStateOf(if (initialEmployee != null) (initialEmployee.passwordHash.ifBlank { "123456" }) else "123456") }
    var showPassword by remember { mutableStateOf(false) }

    val isMasterAdmin = initialEmployee?.isMasterAdmin == true
    val departmentsList = listOf("Tecnología", "Ventas", "Operaciones", "Recursos Humanos", "Finanzas", "Soporte")
    val projectsList = listOf("Proyecto Core", "Expansión Digital", "Migración Cloud", "Soporte 24/7", "General")
    val areasList = listOf("Ingeniería", "Marketing y Ventas", "Logística", "Atención al Cliente", "Administración")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialEmployee != null) "Editar Empleado" else "Registrar Nuevo Empleado")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isMasterAdmin) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberWarning.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AmberWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Administrador Maestro de la organización. Cuenta protegida: no se puede eliminar ni cambiar de rol.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico Civil *") },
                    placeholder = { Text("ej: pepe@ejemplo.com") },
                    supportingText = { Text("Identificador civil único para acceso al sistema sin perfiles redundantes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de Teléfono (opcional)") },
                    placeholder = { Text("ej: +34 690 123 456") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Cargo o Puesto *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Department dropdown or selector
                Text("Departamento:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departmentsList.forEach { dept ->
                        FilterChip(
                            selected = department == dept,
                            onClick = { department = dept },
                            label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Project
                Text("Proyecto Asignado:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    projectsList.forEach { proj ->
                        FilterChip(
                            selected = project == proj,
                            onClick = { project = proj },
                            label = { Text(proj, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Functional Area
                Text("Área Funcional:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    areasList.forEach { area ->
                        FilterChip(
                            selected = functionalArea == area,
                            onClick = { functionalArea = area },
                            label = { Text(area, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Status & System Role
                Text("Estado Operativo:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EmployeeStatus.values().forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = if (isMasterAdmin) "Rol en el Sistema (Fijado por seguridad):" else "Rol en el Sistema (Asignado por Administrador):",
                    style = MaterialTheme.typography.labelSmall
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isMasterAdmin) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("👑 Administrador Maestro") }
                        )
                    } else {
                        SystemRole.values().forEach { r ->
                            FilterChip(
                                selected = systemRole == r,
                                onClick = { systemRole = r },
                                label = { Text(r.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Patrón Laboral Fijo / Atípico
                Text("Patrón de Jornada Laboral (Horarios Atípicos Fijos):", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WorkSchedulePattern.values().forEach { pat ->
                        FilterChip(
                            selected = workSchedulePattern == pat,
                            onClick = { workSchedulePattern = pat },
                            label = { Text(pat.shortLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    text = workSchedulePattern.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Password field for initial setup or modification
                OutlinedTextField(
                    value = accessPassword,
                    onValueChange = { accessPassword = it },
                    label = { Text("Contraseña de Acceso al Sistema") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    supportingText = {
                        Text(
                            text = if (initialEmployee == null) {
                                "Contraseña inicial que usará el empleado para iniciar sesión (por defecto: 123456)."
                            } else {
                                "Modificar contraseña de acceso de este usuario."
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && jobTitle.isNotBlank()) {
                        val effectiveRole = if (isMasterAdmin) SystemRole.ADMIN else systemRole
                        val pass = accessPassword.trim().ifBlank { "123456" }
                        val emp = initialEmployee?.copy(
                            name = name.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            passwordHash = pass,
                            jobTitle = jobTitle.trim(),
                            department = department,
                            project = project,
                            functionalArea = functionalArea,
                            systemRole = effectiveRole,
                            workSchedulePattern = workSchedulePattern,
                            status = status,
                            notes = notes.trim()
                        ) ?: Employee(
                            name = name.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            passwordHash = pass,
                            isMasterAdmin = false,
                            jobTitle = jobTitle.trim(),
                            department = department,
                            project = project,
                            functionalArea = functionalArea,
                            systemRole = effectiveRole,
                            workSchedulePattern = workSchedulePattern,
                            status = status,
                            avatarColorHex = 0xFF2563EB,
                            notes = notes.trim()
                        )
                        onSave(emp)
                    }
                },
                enabled = name.isNotBlank() && jobTitle.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
