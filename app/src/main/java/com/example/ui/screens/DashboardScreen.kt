package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.StaffKpis

@Composable
fun DashboardScreen(
    kpis: StaffKpis,
    employees: List<Employee>,
    shifts: List<Shift>,
    pendingRequests: List<TimeOffRequest>,
    canApproveTimeOff: Boolean,
    loggedInEmployee: Employee? = null,
    clockEntries: List<TimeClockEntry> = emptyList(),
    pendingClockCount: Int = 0,
    isNetworkOnline: Boolean = true,
    onRegisterClock: (Employee, ClockType, String) -> Unit = { _, _, _ -> },
    onTriggerSync: () -> Unit = {},
    onApproveRequest: (Long) -> Unit,
    onRejectRequest: (Long) -> Unit,
    onNavigateToPersonnel: () -> Unit,
    onNavigateToShifts: () -> Unit,
    onNavigateToTimeOff: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Availability KPI Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("availability_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Slate900
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MONITORIZACIÓN EN TIEMPO REAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Disponibilidad de Plantilla",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${kpis.activeEmployees} de ${kpis.totalEmployees} empleados operativos hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BeachAccess,
                                        contentDescription = null,
                                        tint = AmberWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${kpis.onVacationCount} Vacaciones",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = RoseError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${kpis.onLeaveCount} Bajas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(88.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { kpis.availabilityPercentage / 100f },
                            modifier = Modifier.size(84.dp),
                            color = if (kpis.availabilityPercentage >= 75) EmeraldSuccess else AmberWarning,
                            strokeWidth = 8.dp,
                            trackColor = Slate800
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${kpis.availabilityPercentage}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        // Secondary KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Turnos Hoy",
                    value = "${kpis.shiftsTodayCount}",
                    subtitle = "Turnos programados",
                    icon = Icons.Default.Schedule,
                    iconColor = PrimaryBlue,
                    iconBgColor = PrimaryBlueLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Tareas",
                    value = "${kpis.taskCompletionRate}%",
                    subtitle = "${kpis.completedTasksTodayCount}/${kpis.totalTasksTodayCount} listas",
                    icon = Icons.Default.CheckCircleOutline,
                    iconColor = TealAccent,
                    iconBgColor = TealAccentLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Control Horario & Fichaje Digital (Sincronización Automática Online / Offline)
        item {
            var selectedLocation by remember { mutableStateOf("Sede Central - Acceso") }
            val locationOptions = remember {
                listOf(
                    "Sede Central - Acceso",
                    "Almacén Logístico",
                    "Oficina Técnica",
                    "Recepción Planta Baja",
                    "En Ruta / Remoto"
                )
            }

            // Pulse animation for online/offline indicator
            val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.88f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            // Rotation animation for sync button
            var isSyncRotating by remember { mutableStateOf(false) }
            val syncRotation by animateFloatAsState(
                targetValue = if (isSyncRotating) 360f else 0f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                finishedListener = { isSyncRotating = false },
                label = "syncRotation"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .testTag("time_clock_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header with Online / Offline Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Control Horario y Fichaje",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sincronización continua online y registro seguro offline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Modern Network Connectivity Pill with glowing pulse
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isNetworkOnline) EmeraldSuccess.copy(alpha = 0.12f) else AmberWarning.copy(alpha = 0.14f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isNetworkOnline) EmeraldSuccess.copy(alpha = 0.35f) else AmberWarning.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background((if (isNetworkOnline) EmeraldSuccess else AmberWarning).copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isNetworkOnline) "Modo Online" else "Modo Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNetworkOnline) EmeraldSuccess else AmberWarning
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync queue banner with smooth animation
                    AnimatedContent(
                        targetState = pendingClockCount,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "syncBannerAnim"
                    ) { count ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (count > 0) AmberWarning.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (count > 0) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = if (count > 0) AmberWarning else EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (count > 0) {
                                            "$count fichajes en cola offline (sincronizando al conectar)"
                                        } else {
                                            "Todos los fichajes sincronizados con el servidor"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (count > 0) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isSyncRotating = true
                                        onTriggerSync()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sincronizar ahora",
                                        tint = PrimaryBlue,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .rotate(syncRotation)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logged in user work schedule badge
                    if (loggedInEmployee != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = loggedInEmployee.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = loggedInEmployee.workSchedulePattern.shortLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location Chips
                        Text(
                            text = "Punto de marcaje:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            locationOptions.forEach { loc ->
                                FilterChip(
                                    selected = selectedLocation == loc,
                                    onClick = { selectedLocation = loc },
                                    label = { Text(loc, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clock action buttons with modern ripple and elevation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onRegisterClock(loggedInEmployee, ClockType.ENTRADA, selectedLocation)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clock_in_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Entrada", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    onRegisterClock(loggedInEmployee, ClockType.PAUSA_INICIO, selectedLocation)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pausa")
                            }

                            OutlinedButton(
                                onClick = {
                                    onRegisterClock(loggedInEmployee, ClockType.SALIDA, selectedLocation)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clock_out_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salida")
                            }
                        }
                    }

                    // Recent clock logs
                    if (clockEntries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Últimos registros de jornada:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.animateContentSize()
                        ) {
                            clockEntries.take(3).forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (entry.clockType) {
                                                ClockType.ENTRADA -> Icons.Default.Login
                                                ClockType.SALIDA -> Icons.Default.Logout
                                                else -> Icons.Default.PauseCircle
                                            },
                                            contentDescription = null,
                                            tint = when (entry.clockType) {
                                                ClockType.ENTRADA -> EmeraldSuccess
                                                ClockType.SALIDA -> RoseError
                                                else -> PrimaryBlue
                                            },
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "${entry.employeeName} • ${entry.clockType.label}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${entry.formattedDate} ${entry.formattedTime} (${entry.locationTag})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (entry.syncStatus) {
                                            SyncStatus.SYNCED -> EmeraldSuccess.copy(alpha = 0.12f)
                                            SyncStatus.SYNCING -> PrimaryBlue.copy(alpha = 0.12f)
                                            SyncStatus.PENDING -> AmberWarning.copy(alpha = 0.12f)
                                            SyncStatus.FAILED -> RoseError.copy(alpha = 0.12f)
                                        }
                                    ) {
                                        Text(
                                            text = when (entry.syncStatus) {
                                                SyncStatus.SYNCED -> "☁️ Sincronizado"
                                                SyncStatus.SYNCING -> "🔄 Sincronizando"
                                                SyncStatus.PENDING -> "⏳ Pendiente Offline"
                                                SyncStatus.FAILED -> "⚠️ Reintentar"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (entry.syncStatus) {
                                                SyncStatus.SYNCED -> EmeraldSuccess
                                                SyncStatus.SYNCING -> PrimaryBlue
                                                SyncStatus.PENDING -> AmberWarning
                                                SyncStatus.FAILED -> RoseError
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pending Approvals Alert Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Solicitudes Pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (pendingRequests.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = AmberWarning) {
                            Text(pendingRequests.size.toString())
                        }
                    }
                }
                TextButton(onClick = onNavigateToTimeOff) {
                    Text("Ver todas")
                }
            }
        }

        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No hay solicitudes de tiempo libre pendientes de aprobación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(pendingRequests.take(3)) { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_request_${request.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = request.employeeName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                val dayDesc = if (request.customDayType.isNotBlank()) request.customDayType else request.type.label
                                Text(
                                    text = "${request.department} • $dayDesc",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberWarning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${request.daysCount} días",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fechas: ${request.startDate} al ${request.endDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (request.reason.isNotBlank()) {
                            Text(
                                text = "Motivo: \"${request.reason}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (canApproveTimeOff) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onRejectRequest(request.id) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                                ) {
                                    Text("Rechazar")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onApproveRequest(request.id) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                ) {
                                    Text("Aprobar")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Organizational Breakdown by Department
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal por Departamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToPersonnel) {
                    Text("Ver plantilla")
                }
            }
        }

        item {
            val departments = employees.groupBy { it.department }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    departments.forEach { (dept, emps) ->
                        val activeInDept = emps.count { it.status == EmployeeStatus.ACTIVO }
                        val deptPct = if (emps.isNotEmpty()) (activeInDept.toFloat() / emps.size) else 0f

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dept,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$activeInDept / ${emps.size} disponibles (${(deptPct * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { deptPct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when {
                                    deptPct >= 0.8f -> EmeraldSuccess
                                    deptPct >= 0.5f -> AmberWarning
                                    else -> RoseError
                                },
                                trackColor = Slate200
                            )
                        }
                    }
                }
            }
        }

        // Today's Shifts Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Turnos Programados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToShifts) {
                    Text("Ver cuadrante")
                }
            }
        }

        val todayShifts = shifts.take(4)
        if (todayShifts.isEmpty()) {
            item {
                Text(
                    text = "No hay turnos registrados para hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(todayShifts) { shift ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(shift.shiftType.colorHex).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(shift.shiftType.colorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shift.employeeName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${shift.department} • ${shift.shiftType.label} (${shift.startTime} - ${shift.endTime})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
