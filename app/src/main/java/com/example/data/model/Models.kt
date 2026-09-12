package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SystemRole(val label: String) {
    ADMIN("Administrador"),
    MANAGER("Supervisor / Manager"),
    EMPLOYEE("Empleado / Operador")
}

enum class EmployeeStatus(val label: String, val colorHex: Long) {
    ACTIVO("Activo", 0xFF10B981),
    VACACIONES("En Vacaciones", 0xFFF59E0B),
    BAJA_MEDICA("Baja Médica", 0xFFEF4444),
    INACTIVO("Inactivo", 0xFF9CA3AF)
}

enum class ShiftType(val label: String, val defaultHours: String, val colorHex: Long) {
    MANANA("Mañana", "08:00 - 16:00", 0xFF3B82F6),
    TARDE("Tarde", "16:00 - 00:00", 0xFFF59E0B),
    NOCHE("Noche", "00:00 - 08:00", 0xFF6366F1),
    PARTIDO("Partido", "09:00 - 14:00 / 17:00 - 20:00", 0xFF10B981),
    GUARDIA("Guardia 24h", "08:00 - 08:00 (+1)", 0xFFEC4899)
}

enum class TimeOffType(val label: String) {
    VACACIONES("Vacaciones Anuales"),
    ASUNTOS_PROPIOS("Asuntos Propios"),
    DIA_ADICIONAL_GUARDIA("Día Adicional por Guardia"),
    COMPENSATORIO("Día Compensatorio"),
    BAJA_MEDICA("Baja Médica / Incapacidad"),
    MATERNIDAD_PATERNIDAD("Permiso Maternidad / Paternidad"),
    OTRO("Otro Permiso")
}

enum class RequestStatus(val label: String) {
    PENDIENTE("Pendiente de Aprobación"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado")
}

enum class TaskPriority(val label: String, val colorHex: Long) {
    ALTA("Alta", 0xFFEF4444),
    MEDIA("Media", 0xFFF59E0B),
    BAJA("Baja", 0xFF10B981)
}

enum class TaskStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_PROGRESO("En Progreso"),
    COMPLETADA("Completada")
}

enum class TaskAssignmentScope(val label: String) {
    PERSONAL("Personal"),
    SECCION("Sección de Personal")
}

enum class WorkSchedulePattern(
    val label: String,
    val shortLabel: String,
    val description: String,
    val workingDayNumbers: List<Int> // 1 for Monday .. 7 for Sunday (java.time.DayOfWeek.value)
) {
    LUNES_A_VIERNES(
        label = "Lunes a Viernes (Estándar)",
        shortLabel = "L - V",
        description = "Trabajo ordinario de Lunes a Viernes. Descanso legal: Sábado y Domingo.",
        workingDayNumbers = listOf(1, 2, 3, 4, 5)
    ),
    SABADO_DOMINGO_LUNES(
        label = "Sábado, Domingo y Lunes (Atípico Fijo)",
        shortLabel = "Sáb - Dom - Lun",
        description = "Turno fijo de fines de semana y lunes. Descanso legal: Martes, Miércoles, Jueves y Viernes.",
        workingDayNumbers = listOf(6, 7, 1)
    ),
    JUEVES_A_DOMINGO(
        label = "Jueves a Domingo (Jornada 4x3)",
        shortLabel = "Jue - Dom",
        description = "Trabajo intensivo de Jueves a Domingo. Descanso legal: Lunes, Martes y Miércoles.",
        workingDayNumbers = listOf(4, 5, 6, 7)
    ),
    ROTATIVO_TOTAL(
        label = "Turnos Continuos / 24-7",
        shortLabel = "Continuo 24/7",
        description = "Jornada rotativa continua sujeta a cuadrante de guardia semanal.",
        workingDayNumbers = listOf(1, 2, 3, 4, 5, 6, 7)
    );

    fun isWorkingDay(date: java.time.LocalDate): Boolean {
        return workingDayNumbers.contains(date.dayOfWeek.value)
    }

    fun isLegalRestDay(date: java.time.LocalDate): Boolean {
        return !isWorkingDay(date)
    }

    fun calculateBreakdown(startDate: java.time.LocalDate, endDate: java.time.LocalDate): ScheduleBreakdown {
        if (endDate.isBefore(startDate)) {
            return ScheduleBreakdown(0, 0, emptyList())
        }
        var current = startDate
        var workingCount = 0
        var restCount = 0
        val details = mutableListOf<DayScheduleDetail>()
        while (!current.isAfter(endDate)) {
            val isWork = isWorkingDay(current)
            if (isWork) {
                workingCount++
            } else {
                restCount++
            }
            details.add(
                DayScheduleDetail(
                    date = current,
                    dayOfWeek = current.dayOfWeek,
                    isWorkingDay = isWork
                )
            )
            current = current.plusDays(1)
        }
        return ScheduleBreakdown(workingCount, restCount, details)
    }
}

data class DayScheduleDetail(
    val date: java.time.LocalDate,
    val dayOfWeek: java.time.DayOfWeek,
    val isWorkingDay: Boolean
)

data class ScheduleBreakdown(
    val workingDaysCount: Int,
    val legalRestDaysCount: Int,
    val dayDetails: List<DayScheduleDetail>
)

enum class ClockType(val label: String) {
    ENTRADA("Entrada de Turno"),
    SALIDA("Salida de Turno"),
    PAUSA_INICIO("Inicio de Descanso"),
    PAUSA_FIN("Fin de Descanso")
}

enum class SyncStatus(val label: String, val colorHex: Long) {
    PENDING("Pendiente de sincronizar (Offline)", 0xFFF59E0B),
    SYNCING("Sincronizando...", 0xFF3B82F6),
    SYNCED("Sincronizado con Servidor Cloud", 0xFF10B981),
    FAILED("Error de sincronización", 0xFFEF4444)
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String, // Correo electrónico civil único
    val phone: String = "", // Número de teléfono (opcional)
    val passwordHash: String = "123456", // Contraseña para login
    val isMasterAdmin: Boolean = false, // Administrador maestro (no se puede borrar)
    val authProvider: String = "LOCAL", // "LOCAL" o "GOOGLE"
    val jobTitle: String = "Empleado",
    val department: String = "Operaciones",
    val project: String = "General",
    val functionalArea: String = "Operaciones",
    val systemRole: SystemRole = SystemRole.EMPLOYEE,
    val workSchedulePattern: WorkSchedulePattern = WorkSchedulePattern.LUNES_A_VIERNES,
    val status: EmployeeStatus = EmployeeStatus.ACTIVO,
    val avatarColorHex: Long = 0xFF2563EB,
    val hireDate: String = "2024-01-15",
    val notes: String = ""
)

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val department: String,
    val date: String, // "YYYY-MM-DD"
    val shiftType: ShiftType,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)

@Entity(tableName = "time_off_requests")
data class TimeOffRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val department: String,
    val type: TimeOffType,
    val customDayType: String = "", // Relleno a mano del tipo de día
    val startDate: String, // "YYYY-MM-DD"
    val endDate: String,   // "YYYY-MM-DD"
    val daysCount: Int,    // Días laborables computables a vacaciones
    val legalRestDaysCount: Int = 0, // Días de descanso legal exentos del cómputo
    val schedulePattern: WorkSchedulePattern = WorkSchedulePattern.LUNES_A_VIERNES,
    val reason: String, // Motivo detallado ingresado a mano
    val status: RequestStatus = RequestStatus.PENDIENTE,
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null
)

@Entity(tableName = "time_clock_entries")
data class TimeClockEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val department: String,
    val clockType: ClockType,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String, // "HH:mm:ss"
    val formattedDate: String, // "YYYY-MM-DD"
    val locationTag: String = "Sótano / Instalación sin cobertura",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncAttempts: Int = 0,
    val lastSyncAttemptAt: Long? = null,
    val serverSyncId: String? = null
)

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long = 0L,
    val employeeName: String = "",
    val department: String,
    val assignmentScope: TaskAssignmentScope = TaskAssignmentScope.PERSONAL,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIA,
    val status: TaskStatus = TaskStatus.PENDIENTE
)

@Entity(tableName = "performance_reviews")
data class PerformanceReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val date: String, // "YYYY-MM-DD"
    val overallRating: Float, // 1.0 - 5.0
    val punctualityRating: Float,
    val productivityRating: Float,
    val teamworkRating: Float,
    val goalsAchieved: Int,
    val totalGoals: Int,
    val feedback: String,
    val reviewerName: String
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val channel: String, // "SISTEMA", "SLACK", "EMAIL"
    val isRead: Boolean = false
)

data class RolePermissions(
    val canManageEmployees: Boolean = true,
    val canAssignShifts: Boolean = true,
    val canApproveTimeOff: Boolean = true,
    val canAssignTasks: Boolean = true,
    val canReviewPerformance: Boolean = true,
    val canExportReports: Boolean = true,
    val canConfigSystem: Boolean = true
)
