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
    val customDayType: String = "", // Relleno a mano del tipo de día (ej: "Asuntos propios", "Día adicional generado por guardia")
    val startDate: String, // "YYYY-MM-DD"
    val endDate: String,   // "YYYY-MM-DD"
    val daysCount: Int,
    val reason: String, // Motivo detallado ingresado a mano
    val status: RequestStatus = RequestStatus.PENDIENTE,
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null
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
