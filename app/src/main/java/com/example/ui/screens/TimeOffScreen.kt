package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeOffScreen(
    requests: List<TimeOffRequest>,
    employees: List<Employee>,
    loggedInEmployee: Employee?,
    canApprove: Boolean,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onRequestTimeOff: (TimeOffRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<RequestStatus?>(null) }
    var showRequestDialog by remember { mutableStateOf(false) }

    val filtered = if (filterStatus != null) {
        requests.filter { it.status == filterStatus }
    } else {
        requests
    }

    Scaffold(
        modifier = modifier.testTag("time_off_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRequestDialog = true },
                icon = { Icon(Icons.Default.BeachAccess, contentDescription = null) },
                text = { Text("Solicitar Tiempo Libre") },
                containerColor = TealAccent,
                contentColor = Color.White,
                modifier = Modifier.testTag("request_time_off_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick = { filterStatus = null },
                        label = { Text("Todas (${requests.size})") }
                    )
                    FilterChip(
                        selected = filterStatus == RequestStatus.PENDIENTE,
                        onClick = { filterStatus = RequestStatus.PENDIENTE },
                        label = { Text("Pendientes (${requests.count { it.status == RequestStatus.PENDIENTE }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                            selectedLabelColor = AmberWarning
                        )
                    )
                    FilterChip(
                        selected = filterStatus == RequestStatus.APROBADO,
                        onClick = { filterStatus = RequestStatus.APROBADO },
                        label = { Text("Aprobadas (${requests.count { it.status == RequestStatus.APROBADO }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldSuccess
                        )
                    )
                    FilterChip(
                        selected = filterStatus == RequestStatus.RECHAZADO,
                        onClick = { filterStatus = RequestStatus.RECHAZADO },
                        label = { Text("Rechazadas (${requests.count { it.status == RequestStatus.RECHAZADO }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseError.copy(alpha = 0.2f),
                            selectedLabelColor = RoseError
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay solicitudes registradas con este filtro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { req ->
                    TimeOffCard(
                        request = req,
                        canApprove = canApprove && req.status == RequestStatus.PENDIENTE,
                        onApprove = { onApprove(req.id) },
                        onReject = { onReject(req.id) }
                    )
                }
            }
        }
    }

    if (showRequestDialog) {
        RequestTimeOffDialog(
            employees = employees,
            loggedInEmployee = loggedInEmployee,
            onDismiss = { showRequestDialog = false },
            onSave = { req ->
                onRequestTimeOff(req)
                showRequestDialog = false
            }
        )
    }
}

@Composable
fun TimeOffCard(
    request: TimeOffRequest,
    canApprove: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDayType = if (request.customDayType.isNotBlank()) request.customDayType else request.type.label

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("time_off_card_${request.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.employeeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request.department,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (request.status) {
                        RequestStatus.PENDIENTE -> AmberWarning.copy(alpha = 0.15f)
                        RequestStatus.APROBADO -> EmeraldSuccess.copy(alpha = 0.15f)
                        RequestStatus.RECHAZADO -> RoseError.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = request.status.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (request.status) {
                            RequestStatus.PENDIENTE -> AmberWarning
                            RequestStatus.APROBADO -> EmeraldSuccess
                            RequestStatus.RECHAZADO -> RoseError
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specific Day Type Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TealAccent.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TealAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayDayType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TealAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates & count with Atypical Schedule breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.startDate} al ${request.endDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${request.daysCount} días hábiles computados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (request.legalRestDaysCount > 0) {
                        Text(
                            text = "+ ${request.legalRestDaysCount} descansos legales exentos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TealAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horario Atípico Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Patrón laboral: ${request.schedulePattern.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (request.legalRestDaysCount > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "🛡️ Descanso respetado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            // Motivo detallado ingresado a mano
            if (request.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Motivo / Justificación:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = request.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (request.reviewedBy != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Revisado por: ${request.reviewedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canApprove) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                    ) {
                        Text("Rechazar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("Aprobar Solicitud")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTimeOffDialog(
    employees: List<Employee>,
    loggedInEmployee: Employee? = null,
    onDismiss: () -> Unit,
    onSave: (TimeOffRequest) -> Unit
) {
    val isEmployeeUser = loggedInEmployee != null && loggedInEmployee.systemRole == SystemRole.EMPLOYEE

    var selectedEmployee by remember {
        mutableStateOf(
            loggedInEmployee ?: employees.firstOrNull()
        )
    }

    var selectedPattern by remember(selectedEmployee) {
        mutableStateOf(selectedEmployee?.workSchedulePattern ?: WorkSchedulePattern.LUNES_A_VIERNES)
    }

    var type by remember { mutableStateOf(TimeOffType.ASUNTOS_PROPIOS) }
    var customDayType by remember { mutableStateOf("Asuntos propios") }
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var reason by remember { mutableStateOf("") }

    val scheduleBreakdown = remember(startDate, endDate, selectedPattern) {
        val sDate = runCatching { LocalDate.parse(startDate) }.getOrNull()
        val eDate = runCatching { LocalDate.parse(endDate) }.getOrNull()
        if (sDate != null && eDate != null && !eDate.isBefore(sDate)) {
            selectedPattern.calculateBreakdown(sDate, eDate)
        } else {
            ScheduleBreakdown(0, 0, emptyList())
        }
    }

    var daysCount by remember(scheduleBreakdown.workingDaysCount) {
        mutableStateOf(scheduleBreakdown.workingDaysCount.coerceAtLeast(1).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BeachAccess, contentDescription = null, tint = TealAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solicitar Días de Permiso")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Empleado solicitante
                if (isEmployeeUser && loggedInEmployee != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Solicitante:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${loggedInEmployee.name} (${loggedInEmployee.department})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    Text("Empleado Solicitante:", style = MaterialTheme.typography.labelMedium)
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
                }

                // Selección de Tipo de Permiso (Categoría)
                Text("Selecciona el Tipo de Día:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = type == TimeOffType.ASUNTOS_PROPIOS,
                        onClick = {
                            type = TimeOffType.ASUNTOS_PROPIOS
                            customDayType = "Asuntos propios"
                        },
                        label = { Text("Asuntos Propios") }
                    )
                    FilterChip(
                        selected = type == TimeOffType.DIA_ADICIONAL_GUARDIA,
                        onClick = {
                            type = TimeOffType.DIA_ADICIONAL_GUARDIA
                            customDayType = "Día Adicional generado por guardia"
                        },
                        label = { Text("Día Adicional (Guardia)") }
                    )
                    FilterChip(
                        selected = type == TimeOffType.VACACIONES,
                        onClick = {
                            type = TimeOffType.VACACIONES
                            customDayType = "Vacaciones anuales"
                        },
                        label = { Text("Vacaciones") }
                    )
                    FilterChip(
                        selected = type == TimeOffType.COMPENSATORIO,
                        onClick = {
                            type = TimeOffType.COMPENSATORIO
                            customDayType = "Día compensatorio por horas extras"
                        },
                        label = { Text("Compensatorio") }
                    )
                    FilterChip(
                        selected = type == TimeOffType.BAJA_MEDICA,
                        onClick = {
                            type = TimeOffType.BAJA_MEDICA
                            customDayType = "Baja médica / Cita médica"
                        },
                        label = { Text("Baja Médica") }
                    )
                    FilterChip(
                        selected = type == TimeOffType.OTRO,
                        onClick = {
                            type = TimeOffType.OTRO
                            customDayType = "Permiso especial"
                        },
                        label = { Text("Otro") }
                    )
                }

                // Relleno a mano del Tipo de Día
                OutlinedTextField(
                    value = customDayType,
                    onValueChange = { customDayType = it },
                    label = { Text("Tipo de día específico (rellenar a mano) *") },
                    placeholder = { Text("Ej: Asuntos propios, Día Adicional generado por guardia...") },
                    supportingText = { Text("Especifica si son Asuntos propios, Día adicional generado por guardia, etc.") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_day_type_input")
                )

                // Fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Fecha Inicio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Fecha Fin") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Selector de Patrón de Jornada Laboral (Horarios Atípicos Fijos)
                Column {
                    Text("Patrón laboral del empleado:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WorkSchedulePattern.values().forEach { pat ->
                            FilterChip(
                                selected = selectedPattern == pat,
                                onClick = { selectedPattern = pat },
                                label = { Text(pat.shortLabel, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Text(
                        text = selectedPattern.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Desglose Inteligente de Días Computables vs Descanso Legal
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cómputo Legal según Jornada",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Días laborables (vacaciones):",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "${scheduleBreakdown.workingDaysCount} días a descontar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Descanso semanal exento:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "${scheduleBreakdown.legalRestDaysCount} días protegidos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TealAccent
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = daysCount,
                    onValueChange = { daysCount = it },
                    label = { Text("Número de Días Computables a Vacaciones") },
                    supportingText = { Text("Calculado automáticamente respetando tus días de descanso legal.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Motivo / Justificación
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo / Justificación *") },
                    placeholder = { Text("Ej: Guardia realizada el fin de semana del 15 de marzo cubriendo soporte de emergencias...") },
                    supportingText = { Text("Describe el motivo o causa del día de permiso solicitado") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("time_off_reason_input"),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val emp = selectedEmployee
                    if (emp != null && reason.isNotBlank() && customDayType.isNotBlank()) {
                        val days = daysCount.toIntOrNull() ?: scheduleBreakdown.workingDaysCount
                        onSave(
                            TimeOffRequest(
                                employeeId = emp.id,
                                employeeName = emp.name,
                                department = emp.department,
                                type = type,
                                customDayType = customDayType.trim(),
                                startDate = startDate,
                                endDate = endDate,
                                daysCount = days,
                                legalRestDaysCount = scheduleBreakdown.legalRestDaysCount,
                                schedulePattern = selectedPattern,
                                reason = reason.trim()
                            )
                        )
                    }
                },
                enabled = selectedEmployee != null && reason.isNotBlank() && customDayType.isNotBlank(),
                modifier = Modifier.testTag("submit_time_off_button")
            ) {
                Text("Enviar Solicitud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
