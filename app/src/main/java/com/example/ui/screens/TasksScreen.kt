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
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.theme.*
import java.time.LocalDate

enum class TaskFilterCategory(val label: String) {
    TODAS("Todas"),
    MIS_TAREAS("Mis Tareas"),
    SECCION("De Sección"),
    PERSONAL("Personales")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<DailyTask>,
    employees: List<Employee>,
    loggedInEmployee: Employee? = null,
    canAssignTasks: Boolean,
    onToggleTaskStatus: (DailyTask) -> Unit,
    onAddTask: (DailyTask) -> Unit,
    onDeleteTask: (DailyTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today.toString()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf(TaskFilterCategory.TODAS) }

    val dateTasks = tasks.filter { it.date == selectedDate }
    val filteredTasks = remember(dateTasks, filterCategory, loggedInEmployee) {
        when (filterCategory) {
            TaskFilterCategory.TODAS -> dateTasks
            TaskFilterCategory.MIS_TAREAS -> {
                if (loggedInEmployee != null) {
                    dateTasks.filter { task ->
                        (task.assignmentScope == TaskAssignmentScope.PERSONAL && task.employeeId == loggedInEmployee.id) ||
                        (task.assignmentScope == TaskAssignmentScope.SECCION && task.department.equals(loggedInEmployee.department, ignoreCase = true))
                    }
                } else dateTasks
            }
            TaskFilterCategory.SECCION -> dateTasks.filter { it.assignmentScope == TaskAssignmentScope.SECCION }
            TaskFilterCategory.PERSONAL -> dateTasks.filter { it.assignmentScope == TaskAssignmentScope.PERSONAL }
        }
    }

    val completedCount = filteredTasks.count { it.status == TaskStatus.COMPLETADA }
    val progressPct = if (filteredTasks.isNotEmpty()) completedCount.toFloat() / filteredTasks.size else 1f

    Scaffold(
        modifier = modifier.testTag("tasks_screen"),
        floatingActionButton = {
            if (canAssignTasks) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                    text = { Text("Asignar Tarea") },
                    containerColor = PurpleMetric,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_task_fab")
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
            // Day selector tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Fecha de Tareas:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val daysList = remember {
                        listOf(
                            today.minusDays(1) to "Ayer",
                            today to "Hoy",
                            today.plusDays(1) to "Mañana",
                            today.plusDays(2) to "En 2 días",
                            today.plusDays(3) to "En 3 días"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        daysList.forEach { (dateObj, label) ->
                            val dateStr = dateObj.toString()
                            FilterChip(
                                selected = selectedDate == dateStr,
                                onClick = { selectedDate = dateStr },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Scope filter row (Todas / Mis Tareas / De Sección / Personales)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilterCategory.values().forEach { cat ->
                        FilterChip(
                            selected = filterCategory == cat,
                            onClick = { filterCategory = cat },
                            leadingIcon = {
                                when (cat) {
                                    TaskFilterCategory.TODAS -> Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    TaskFilterCategory.MIS_TAREAS -> Icon(Icons.Default.PersonPin, contentDescription = null, modifier = Modifier.size(16.dp))
                                    TaskFilterCategory.SECCION -> Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                    TaskFilterCategory.PERSONAL -> Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Progress Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso de Tareas del Día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount de ${filteredTasks.size} completadas",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldSuccess,
                            trackColor = Slate200
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay tareas para el filtro seleccionado en esta fecha",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (canAssignTasks) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showAddDialog = true }) {
                                    Text("Asignar nueva tarea (Personal o Sección)")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    DailyTaskCard(
                        task = task,
                        canDelete = canAssignTasks,
                        onToggle = { onToggleTaskStatus(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            employees = employees,
            initialDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = { task ->
                onAddTask(task)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DailyTaskCard(
    task: DailyTask,
    canDelete: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSectionTask = task.assignmentScope == TaskAssignmentScope.SECCION

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSectionTask) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = when (task.status) {
                        TaskStatus.COMPLETADA -> Icons.Default.CheckCircle
                        TaskStatus.EN_PROGRESO -> Icons.Default.Timelapse
                        TaskStatus.PENDIENTE -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = "Cambiar estado",
                    tint = when (task.status) {
                        TaskStatus.COMPLETADA -> EmeraldSuccess
                        TaskStatus.EN_PROGRESO -> AmberWarning
                        TaskStatus.PENDIENTE -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Assignment Scope Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSectionTask) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlueLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sección: ${task.department}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Personal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.status == TaskStatus.COMPLETADA) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isSectionTask) {
                        "Asignada colectivamente a la sección ${task.department}"
                    } else {
                        "Asignada a: ${task.employeeName} (${task.department})"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(task.priority.colorHex).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Prioridad ${task.priority.label}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(task.priority.colorHex)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = task.status.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar tarea",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    employees: List<Employee>,
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (DailyTask) -> Unit
) {
    var assignmentScope by remember { mutableStateOf(TaskAssignmentScope.PERSONAL) }
    var selectedEmployee by remember { mutableStateOf(employees.firstOrNull()) }

    // Department/Section choices
    val availableDepartments = remember(employees) {
        val depts = employees.map { it.department }.distinct().filter { it.isNotBlank() }
        if (depts.isEmpty()) listOf("Tecnología", "Operaciones", "Ventas", "Recursos Humanos", "Finanzas", "Soporte")
        else depts
    }
    var selectedDepartment by remember { mutableStateOf(availableDepartments.firstOrNull() ?: "Tecnología") }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf(initialDate) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddTask, contentDescription = null, tint = PurpleMetric)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asignar Nueva Tarea")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scope selector: Personal vs Sección
                Text("Tipo de Asignación:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = assignmentScope == TaskAssignmentScope.PERSONAL,
                        onClick = { assignmentScope = TaskAssignmentScope.PERSONAL },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Personal (A un Empleado)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = assignmentScope == TaskAssignmentScope.SECCION,
                        onClick = { assignmentScope = TaskAssignmentScope.SECCION },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("A una Sección") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Conditional Target Selector
                if (assignmentScope == TaskAssignmentScope.PERSONAL) {
                    Text("Seleccionar Empleado:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        employees.forEach { emp ->
                            FilterChip(
                                selected = selectedEmployee?.id == emp.id,
                                onClick = { selectedEmployee = emp },
                                label = { Text(emp.name, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    selectedEmployee?.let {
                        Text(
                            text = "Sección/Dpto del empleado: ${it.department}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text("Seleccionar Sección / Departamento:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableDepartments.forEach { dept ->
                            FilterChip(
                                selected = selectedDepartment == dept,
                                onClick = { selectedDepartment = dept },
                                label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Esta tarea será visible para todos los integrantes de la sección '$selectedDepartment'.",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlueDark
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título o Asunto de la Tarea *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detalles adicionales / Instrucciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = taskDate,
                    onValueChange = { taskDate = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Nivel de Prioridad:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = if (assignmentScope == TaskAssignmentScope.PERSONAL && selectedEmployee != null) {
                            DailyTask(
                                employeeId = selectedEmployee!!.id,
                                employeeName = selectedEmployee!!.name,
                                department = selectedEmployee!!.department,
                                assignmentScope = TaskAssignmentScope.PERSONAL,
                                date = taskDate,
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                status = TaskStatus.PENDIENTE
                            )
                        } else {
                            DailyTask(
                                employeeId = 0L,
                                employeeName = "Sección $selectedDepartment",
                                department = selectedDepartment,
                                assignmentScope = TaskAssignmentScope.SECCION,
                                date = taskDate,
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                status = TaskStatus.PENDIENTE
                            )
                        }
                        onSave(task)
                    }
                },
                enabled = title.isNotBlank() && (assignmentScope == TaskAssignmentScope.SECCION || selectedEmployee != null)
            ) {
                Text("Asignar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

