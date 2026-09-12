package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(
    shifts: List<Shift>,
    employees: List<Employee>,
    tasks: List<DailyTask> = emptyList(),
    loggedInEmployee: Employee? = null,
    canAssignShifts: Boolean,
    onAddShift: (Shift) -> Unit,
    onDeleteShift: (Shift) -> Unit,
    onToggleTaskStatus: (DailyTask) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today.toString()) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var showAddDialog by remember { mutableStateOf(false) }

    val spanishLocale = remember { Locale("es", "ES") }

    val dayShifts = shifts.filter { it.date == selectedDate }
    val dayTasks = tasks.filter { it.date == selectedDate }

    Scaffold(
        modifier = modifier.testTag("shifts_screen"),
        floatingActionButton = {
            if (canAssignShifts) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.AddAlarm, contentDescription = null) },
                    text = { Text("Asignar Turno") },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_shift_fab")
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header and Navigation Controls
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
                            Column {
                                val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, spanishLocale)
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
                                Text(
                                    text = "$monthName ${currentYearMonth.year}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = "Calendario mensual de turnos y cuadrante",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
                                }
                                FilledTonalButton(
                                    onClick = {
                                        currentYearMonth = YearMonth.from(today)
                                        selectedDate = today.toString()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Hoy", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(
                                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar Grid Component
                        MonthCalendarGrid(
                            yearMonth = currentYearMonth,
                            selectedDate = selectedDate,
                            today = today,
                            shifts = shifts,
                            tasks = tasks,
                            loggedInEmployee = loggedInEmployee,
                            onDateSelected = { selectedDate = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Legend with colors
                        CalendarLegend()
                    }
                }
            }

            // Selected Day Inspection Section
            item {
                val selectedLocalDate = runCatching { LocalDate.parse(selectedDate) }.getOrDefault(today)
                val dayOfWeekName = selectedLocalDate.dayOfWeek.getDisplayName(TextStyle.FULL, spanishLocale)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
                val monthName = selectedLocalDate.month.getDisplayName(TextStyle.FULL, spanishLocale)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$dayOfWeekName, ${selectedLocalDate.dayOfMonth} de $monthName",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detalle del día seleccionado ($selectedDate)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (selectedDate == today.toString()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryBlue
                                ) {
                                    Text(
                                        text = "HOY",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Staffing Shortage / Carencia de Personal Banner
                        StaffingShortageBanner(
                            shiftCount = dayShifts.size,
                            canAssignShifts = canAssignShifts,
                            onAddShiftClick = { showAddDialog = true }
                        )
                    }
                }
            }

            // Tasks scheduled for the selected day
            if (dayTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Tareas Programadas para este Día (${dayTasks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(dayTasks, key = { "day_task_${it.id}" }) { task ->
                    val isMyTask = loggedInEmployee != null && (
                        (task.assignmentScope == TaskAssignmentScope.PERSONAL && task.employeeId == loggedInEmployee.id) ||
                        (task.assignmentScope == TaskAssignmentScope.SECCION && task.department.equals(loggedInEmployee.department, ignoreCase = true))
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMyTask) PurpleMetric.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isMyTask) androidx.compose.foundation.BorderStroke(1.dp, PurpleMetric.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onToggleTaskStatus(task) }) {
                                Icon(
                                    imageVector = if (task.status == TaskStatus.COMPLETADA) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (task.status == TaskStatus.COMPLETADA) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isMyTask) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PurpleMetric
                                        ) {
                                            Text(
                                                text = "🎯 Tu Tarea",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (task.assignmentScope == TaskAssignmentScope.SECCION) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryBlueLight
                                        ) {
                                            Text(
                                                text = "Sección: ${task.department}",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryBlueDark,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Para: ${task.employeeName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Shifts list for the selected day
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turnos Cubiertos (${dayShifts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (canAssignShifts) {
                        TextButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir turno")
                        }
                    }
                }
            }

            if (dayShifts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = RoseError,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay ningún turno asignado en esta fecha",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (canAssignShifts) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Asignar personal ahora")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(dayShifts, key = { it.id }) { shift ->
                    ShiftCard(
                        shift = shift,
                        canDelete = canAssignShifts,
                        onDelete = { onDeleteShift(shift) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddShiftDialog(
            employees = employees,
            initialDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = { shift ->
                onAddShift(shift)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: String,
    today: LocalDate,
    shifts: List<Shift>,
    tasks: List<DailyTask>,
    loggedInEmployee: Employee?,
    onDateSelected: (String) -> Unit
) {
    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")

    // First day of month and days count
    val firstDay = yearMonth.atDay(1)
    val startDayOfWeek = firstDay.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val daysInMonth = yearMonth.lengthOfMonth()

    val totalCells = ((startDayOfWeek - 1 + daysInMonth + 6) / 7) * 7

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday header row
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid cells in rows of 7
        val rows = totalCells / 7
        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNum = cellIndex - (startDayOfWeek - 1) + 1

                    if (dayNum in 1..daysInMonth) {
                        val cellDate = yearMonth.atDay(dayNum)
                        val cellDateStr = cellDate.toString()
                        val isSelected = cellDateStr == selectedDate
                        val isToday = cellDate == today

                        val cellShifts = shifts.filter { it.date == cellDateStr }
                        val cellTasks = tasks.filter { it.date == cellDateStr }
                        val hasMyTasks = loggedInEmployee != null && cellTasks.any {
                            (it.assignmentScope == TaskAssignmentScope.PERSONAL && it.employeeId == loggedInEmployee.id) ||
                            (it.assignmentScope == TaskAssignmentScope.SECCION && it.department.equals(loggedInEmployee.department, ignoreCase = true))
                        }

                        CalendarDayCell(
                            dayNum = dayNum,
                            isSelected = isSelected,
                            isToday = isToday,
                            shiftCount = cellShifts.size,
                            taskCount = cellTasks.size,
                            hasMyTasks = hasMyTasks,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDateSelected(cellDateStr) }
                        )
                    } else {
                        // Empty slot
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNum: Int,
    isSelected: Boolean,
    isToday: Boolean,
    shiftCount: Int,
    taskCount: Int,
    hasMyTasks: Boolean,
    modifier: Modifier = Modifier
) {
    // Understaffing indicator: 0 shifts = critical shortage, 1-2 shifts = low staff, 3+ = ok
    val isShortage = shiftCount in 1..2
    val isNoStaff = shiftCount == 0

    val borderColor = when {
        isSelected -> PrimaryBlue
        isToday -> PrimaryBlueDark.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val backgroundColor = when {
        isSelected -> PrimaryBlue.copy(alpha = 0.12f)
        isToday -> PrimaryBlueLight.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected || isToday) 2.dp else 0.5.dp,
                color = if (isSelected || isToday) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Day number
            Text(
                text = "$dayNum",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    isSelected -> PrimaryBlue
                    isToday -> PrimaryBlueDark
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Staffing visual indicator (Color & small text / dot)
            when {
                isNoStaff -> {
                    // Carencia crítica: 0 turnos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(RoseError.copy(alpha = 0.15f))
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(RoseError)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "0 pers.",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseError,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                isShortage -> {
                    // Poco personal (1 o 2 empleados)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(AmberWarning.copy(alpha = 0.15f))
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(AmberWarning)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$shiftCount poco",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309), // Dark amber
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                else -> {
                    // Personal adecuado (3 o más)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.12f))
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$shiftCount tur.",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }

            // Tasks Reference (Line of text or badge)
            if (hasMyTasks) {
                // User has personal or section task
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(PurpleMetric)
                        .padding(horizontal = 1.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 Tu tarea",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            } else if (taskCount > 0) {
                // Other tasks scheduled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryBlueLight)
                        .padding(horizontal = 1.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 $taskCount t.",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlueDark,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Referencias y Guía Visual:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendItem(color = EmeraldSuccess, label = "Personal adecuado (3+ turnos)")
            LegendItem(color = AmberWarning, label = "Poco personal (1-2)")
            LegendItem(color = RoseError, label = "Sin personal (Carencia 0)")
            LegendItem(color = PurpleMetric, label = "🎯 Tarea personal / de sección")
            LegendItem(color = PrimaryBlueDark, label = "📝 Tareas programadas")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StaffingShortageBanner(
    shiftCount: Int,
    canAssignShifts: Boolean,
    onAddShiftClick: () -> Unit
) {
    when {
        shiftCount == 0 -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Alerta de Carencia Total de Personal!",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoseError
                        )
                        Text(
                            text = "No hay ningún empleado cubriendo turnos en esta fecha. El servicio está sin personal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (canAssignShifts) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAddShiftClick,
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Cubrir Turno", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        shiftCount in 1..2 -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aviso: Poco Personal en Cuadrante",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                        Text(
                            text = "Solo hay $shiftCount persona(s) en turno. Podría haber sobrecarga operativa según la demanda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (canAssignShifts) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onAddShiftClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Reforzar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        else -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dotación de Personal Adecuada",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "$shiftCount empleados están asignados a turnos en este día.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShiftCard(
    shift: Shift,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shift_card_${shift.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(shift.shiftType.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (shift.shiftType) {
                        ShiftType.MANANA -> Icons.Default.WbSunny
                        ShiftType.TARDE -> Icons.Default.WbTwilight
                        ShiftType.NOCHE -> Icons.Default.NightsStay
                        ShiftType.PARTIDO -> Icons.Default.AccessTime
                        ShiftType.GUARDIA -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = Color(shift.shiftType.colorHex),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift.employeeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${shift.department} • Turno ${shift.shiftType.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${shift.startTime} - ${shift.endTime}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (shift.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nota: ${shift.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar turno",
                        tint = RoseError
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftDialog(
    employees: List<Employee>,
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (Shift) -> Unit
) {
    var selectedEmployee by remember { mutableStateOf(employees.firstOrNull()) }
    var shiftDate by remember { mutableStateOf(initialDate) }
    var shiftType by remember { mutableStateOf(ShiftType.MANANA) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("16:00") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Nuevo Turno") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Empleado:", style = MaterialTheme.typography.labelMedium)
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

                OutlinedTextField(
                    value = shiftDate,
                    onValueChange = { shiftDate = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Tipo de Turno:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShiftType.values().forEach { type ->
                        FilterChip(
                            selected = shiftType == type,
                            onClick = {
                                shiftType = type
                                when (type) {
                                    ShiftType.MANANA -> { startTime = "08:00"; endTime = "16:00" }
                                    ShiftType.TARDE -> { startTime = "16:00"; endTime = "00:00" }
                                    ShiftType.NOCHE -> { startTime = "00:00"; endTime = "08:00" }
                                    ShiftType.PARTIDO -> { startTime = "09:00"; endTime = "19:00" }
                                    ShiftType.GUARDIA -> { startTime = "08:00"; endTime = "08:00" }
                                }
                            },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Hora Inicio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Hora Fin") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Guardias") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedEmployee != null) {
                        onSave(
                            Shift(
                                employeeId = selectedEmployee!!.id,
                                employeeName = selectedEmployee!!.name,
                                department = selectedEmployee!!.department,
                                date = shiftDate,
                                shiftType = shiftType,
                                startTime = startTime,
                                endTime = endTime,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = selectedEmployee != null
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

