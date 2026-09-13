package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CompanyEnvironment::class,
        Employee::class,
        Shift::class,
        TimeOffRequest::class,
        DailyTask::class,
        PerformanceReview::class,
        NotificationLog::class,
        TimeClockEntry::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun shiftDao(): ShiftDao
    abstract fun timeOffDao(): TimeOffDao
    abstract fun taskDao(): TaskDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun notificationDao(): NotificationDao
    abstract fun timeClockDao(): TimeClockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staffhub_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val companyDao = database.companyDao()
            val employeeDao = database.employeeDao()
            val shiftDao = database.shiftDao()
            val timeOffDao = database.timeOffDao()
            val taskDao = database.taskDao()
            val performanceDao = database.performanceDao()
            val notificationDao = database.notificationDao()

            // Entorno Empresarial por Defecto
            val defaultCompany = CompanyEnvironment(
                code = "S1-CORP",
                name = "S1 Corporación Tecnológica",
                adminEmail = "laura.martinez@empresa.com",
                adminName = "Laura Martínez Gómez"
            )
            companyDao.insertCompany(defaultCompany)

            val employees = listOf(
                Employee(
                    name = "Laura Martínez Gómez",
                    email = "laura.martinez@empresa.com",
                    phone = "+34 612 345 678",
                    passwordHash = "admin123",
                    isMasterAdmin = true,
                    jobTitle = "Directora de Tecnología",
                    department = "Tecnología",
                    project = "Migración Cloud",
                    functionalArea = "Ingeniería",
                    systemRole = SystemRole.ADMIN,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFF2563EB,
                    hireDate = "2021-03-01",
                    notes = "Líder de arquitectura y equipo DevOps • Administrador Maestro"
                ),
                Employee(
                    name = "Carlos Santana Peña",
                    email = "carlos.santana@empresa.com",
                    phone = "+34 623 456 789",
                    passwordHash = "manager123",
                    jobTitle = "Líder Técnico Frontend",
                    department = "Tecnología",
                    project = "Proyecto Core",
                    functionalArea = "Ingeniería",
                    systemRole = SystemRole.MANAGER,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFF0D9488,
                    hireDate = "2022-06-15",
                    notes = "Especialista en Jetpack Compose y React"
                ),
                Employee(
                    name = "Sofía Romero Ruiz",
                    email = "sofia.romero@empresa.com",
                    phone = "+34 634 567 890",
                    jobTitle = "Desarrolladora Backend Senior",
                    department = "Tecnología",
                    project = "Expansión Digital",
                    functionalArea = "Ingeniería",
                    systemRole = SystemRole.EMPLOYEE,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFF7C3AED,
                    hireDate = "2023-01-10",
                    notes = "Microservicios Kotlin y Cloud SQL"
                ),
                Employee(
                    name = "Alejandro Vargas León",
                    email = "alejandro.vargas@empresa.com",
                    phone = "+34 645 678 901",
                    jobTitle = "Gerente de Ventas Corporativas",
                    department = "Ventas",
                    project = "Expansión Digital",
                    functionalArea = "Marketing y Ventas",
                    systemRole = SystemRole.MANAGER,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFFEA580C,
                    hireDate = "2022-09-01",
                    notes = "Gestión de cuentas clave en España y LATAM"
                ),
                Employee(
                    name = "Elena Morales Bravo",
                    email = "elena.morales@empresa.com",
                    phone = "+34 656 789 012",
                    jobTitle = "Especialista en Ventas Senior",
                    department = "Ventas",
                    project = "Proyecto Core",
                    functionalArea = "Marketing y Ventas",
                    systemRole = SystemRole.EMPLOYEE,
                    status = EmployeeStatus.VACACIONES,
                    avatarColorHex = 0xFFF59E0B,
                    hireDate = "2023-04-18",
                    notes = "En período de vacaciones anuales"
                ),
                Employee(
                    name = "Javier Fernández Soto",
                    email = "javier.fernandez@empresa.com",
                    phone = "+34 667 890 123",
                    jobTitle = "Responsable de Operaciones y Logística",
                    department = "Operaciones",
                    project = "Soporte 24/7",
                    functionalArea = "Logística",
                    systemRole = SystemRole.MANAGER,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFF059669,
                    hireDate = "2021-11-20",
                    notes = "Coordinación de cadenas de distribución"
                ),
                Employee(
                    name = "Beatriz Navarro Cano",
                    email = "beatriz.navarro@empresa.com",
                    phone = "+34 678 901 234",
                    jobTitle = "Coordinadora de Recursos Humanos",
                    department = "Recursos Humanos",
                    project = "General",
                    functionalArea = "Administración",
                    systemRole = SystemRole.ADMIN,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFFDB2777,
                    hireDate = "2020-05-10",
                    notes = "Gestión de nóminas, talento y beneficios"
                ),
                Employee(
                    name = "Marcos Delgado Cruz",
                    email = "marcos.delgado@empresa.com",
                    phone = "+34 689 012 345",
                    jobTitle = "Especialista de Soporte Técnico 24/7",
                    department = "Soporte",
                    project = "Soporte 24/7",
                    functionalArea = "Atención al Cliente",
                    systemRole = SystemRole.EMPLOYEE,
                    workSchedulePattern = WorkSchedulePattern.JUEVES_A_DOMINGO,
                    status = EmployeeStatus.BAJA_MEDICA,
                    avatarColorHex = 0xFFEF4444,
                    hireDate = "2023-08-01",
                    notes = "Turno intensivo de Jueves a Domingo • Baja médica temporal"
                ),
                Employee(
                    name = "Pepe García",
                    email = "pepe@ejemplo.com",
                    phone = "+34 690 123 456",
                    passwordHash = "pepe123",
                    jobTitle = "Operador de Logística y Guardias",
                    department = "Operaciones",
                    project = "Soporte 24/7",
                    functionalArea = "Logística",
                    systemRole = SystemRole.EMPLOYEE,
                    workSchedulePattern = WorkSchedulePattern.SABADO_DOMINGO_LUNES,
                    status = EmployeeStatus.ACTIVO,
                    avatarColorHex = 0xFF0284C7,
                    hireDate = "2024-01-10",
                    notes = "Jornada atípica fija de Sábados, Domingos y Lunes (Descanso Mar a Vie)"
                )
            )

            val insertedIds = mutableListOf<Long>()
            for (emp in employees) {
                val id = employeeDao.insertEmployee(emp)
                insertedIds.add(id)
            }

            // Shifts for today and upcoming
            val todayStr = java.time.LocalDate.now().toString()
            val tomorrowStr = java.time.LocalDate.now().plusDays(1).toString()
            val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()

            val shifts = listOf(
                Shift(employeeId = insertedIds[0], employeeName = "Laura Martínez Gómez", department = "Tecnología", date = todayStr, shiftType = ShiftType.MANANA, startTime = "08:00", endTime = "16:00", notes = "Revisión infraestructura cloud"),
                Shift(employeeId = insertedIds[1], employeeName = "Carlos Santana Peña", department = "Tecnología", date = todayStr, shiftType = ShiftType.MANANA, startTime = "08:00", endTime = "16:00", notes = "Sprint planning v2"),
                Shift(employeeId = insertedIds[2], employeeName = "Sofía Romero Ruiz", department = "Tecnología", date = todayStr, shiftType = ShiftType.TARDE, startTime = "16:00", endTime = "00:00", notes = "Despliegue nocturno microservicios"),
                Shift(employeeId = insertedIds[3], employeeName = "Alejandro Vargas León", department = "Ventas", date = todayStr, shiftType = ShiftType.PARTIDO, startTime = "09:00", endTime = "19:00", notes = "Ronda de llamadas clientes VIP"),
                Shift(employeeId = insertedIds[5], employeeName = "Javier Fernández Soto", department = "Operaciones", date = todayStr, shiftType = ShiftType.MANANA, startTime = "07:30", endTime = "15:30", notes = "Supervisión de almacén central"),
                Shift(employeeId = insertedIds[6], employeeName = "Beatriz Navarro Cano", department = "Recursos Humanos", date = todayStr, shiftType = ShiftType.MANANA, startTime = "09:00", endTime = "17:00", notes = "Entrevistas candidatos"),
                // Tomorrow
                Shift(employeeId = insertedIds[1], employeeName = "Carlos Santana Peña", department = "Tecnología", date = tomorrowStr, shiftType = ShiftType.MANANA, startTime = "08:00", endTime = "16:00"),
                Shift(employeeId = insertedIds[2], employeeName = "Sofía Romero Ruiz", department = "Tecnología", date = tomorrowStr, shiftType = ShiftType.MANANA, startTime = "08:00", endTime = "16:00")
            )
            for (shift in shifts) {
                shiftDao.insertShift(shift)
            }

            // Time-off requests
            val requests = listOf(
                TimeOffRequest(
                    employeeId = insertedIds[4],
                    employeeName = "Elena Morales Bravo",
                    department = "Ventas",
                    type = TimeOffType.VACACIONES,
                    startDate = todayStr,
                    endDate = java.time.LocalDate.now().plusDays(7).toString(),
                    daysCount = 8,
                    reason = "Vacaciones de verano programadas",
                    status = RequestStatus.APROBADO,
                    reviewedBy = "Beatriz Navarro Cano",
                    reviewedAt = System.currentTimeMillis() - 86400000L
                ),
                TimeOffRequest(
                    employeeId = insertedIds[7],
                    employeeName = "Marcos Delgado Cruz",
                    department = "Soporte",
                    type = TimeOffType.BAJA_MEDICA,
                    startDate = yesterdayStr,
                    endDate = java.time.LocalDate.now().plusDays(3).toString(),
                    daysCount = 5,
                    reason = "Reposo prescrito por facultativo médico",
                    status = RequestStatus.APROBADO,
                    reviewedBy = "Beatriz Navarro Cano",
                    reviewedAt = System.currentTimeMillis() - 43200000L
                ),
                TimeOffRequest(
                    employeeId = insertedIds[2],
                    employeeName = "Sofía Romero Ruiz",
                    department = "Tecnología",
                    type = TimeOffType.ASUNTOS_PROPIOS,
                    startDate = java.time.LocalDate.now().plusDays(4).toString(),
                    endDate = java.time.LocalDate.now().plusDays(5).toString(),
                    daysCount = 2,
                    reason = "Trámites administrativos notariales y mudanza",
                    status = RequestStatus.PENDIENTE
                ),
                TimeOffRequest(
                    employeeId = insertedIds[3],
                    employeeName = "Alejandro Vargas León",
                    department = "Ventas",
                    type = TimeOffType.COMPENSATORIO,
                    startDate = java.time.LocalDate.now().plusDays(10).toString(),
                    endDate = java.time.LocalDate.now().plusDays(10).toString(),
                    daysCount = 1,
                    reason = "Compensación por asistencia a feria sectorial en fin de semana",
                    status = RequestStatus.PENDIENTE
                )
            )
            for (req in requests) {
                timeOffDao.insertRequest(req)
            }

            // Daily Tasks (Individuales y de Sección de Personal)
            val tasks = listOf(
                DailyTask(employeeId = insertedIds[0], employeeName = "Laura Martínez Gómez", department = "Tecnología", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Auditar seguridad y cortafuegos en clúster Kubernetes", priority = TaskPriority.ALTA, status = TaskStatus.EN_PROGRESO),
                DailyTask(employeeId = insertedIds[1], employeeName = "Carlos Santana Peña", department = "Tecnología", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Revisar pull request de accesibilidad en UI Compose", priority = TaskPriority.MEDIA, status = TaskStatus.COMPLETADA),
                DailyTask(employeeId = 0L, employeeName = "Sección Tecnología", department = "Tecnología", assignmentScope = TaskAssignmentScope.SECCION, date = todayStr, title = "Despliegue y monitorización de actualización en servidores", priority = TaskPriority.ALTA, status = TaskStatus.EN_PROGRESO),
                DailyTask(employeeId = insertedIds[2], employeeName = "Sofía Romero Ruiz", department = "Tecnología", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Implementar webhook con firma criptográfica", priority = TaskPriority.ALTA, status = TaskStatus.EN_PROGRESO),
                DailyTask(employeeId = insertedIds[3], employeeName = "Alejandro Vargas León", department = "Ventas", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Presentación de propuesta a cliente multinacional", priority = TaskPriority.ALTA, status = TaskStatus.PENDIENTE),
                DailyTask(employeeId = 0L, employeeName = "Sección Operaciones", department = "Operaciones", assignmentScope = TaskAssignmentScope.SECCION, date = todayStr, title = "Inspección semanal de seguridad y protocolos en almacén", priority = TaskPriority.MEDIA, status = TaskStatus.PENDIENTE),
                DailyTask(employeeId = insertedIds[5], employeeName = "Javier Fernández Soto", department = "Operaciones", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Control de inventario y reconciliación de envíos", priority = TaskPriority.MEDIA, status = TaskStatus.COMPLETADA),
                DailyTask(employeeId = insertedIds[6], employeeName = "Beatriz Navarro Cano", department = "Recursos Humanos", assignmentScope = TaskAssignmentScope.PERSONAL, date = todayStr, title = "Procesar altas de nuevos ingresos para el Q4", priority = TaskPriority.ALTA, status = TaskStatus.EN_PROGRESO)
            )
            for (task in tasks) {
                taskDao.insertTask(task)
            }

            // Performance reviews
            val reviews = listOf(
                PerformanceReview(
                    employeeId = insertedIds[0],
                    employeeName = "Laura Martínez Gómez",
                    date = "2024-06-30",
                    overallRating = 4.9f,
                    punctualityRating = 5.0f,
                    productivityRating = 4.8f,
                    teamworkRating = 5.0f,
                    goalsAchieved = 5,
                    totalGoals = 5,
                    feedback = "Excepcional liderazgo técnico y cumplimiento de hitos de migración a tiempo.",
                    reviewerName = "Dirección General"
                ),
                PerformanceReview(
                    employeeId = insertedIds[1],
                    employeeName = "Carlos Santana Peña",
                    date = "2024-06-30",
                    overallRating = 4.7f,
                    punctualityRating = 4.8f,
                    productivityRating = 4.6f,
                    teamworkRating = 4.8f,
                    goalsAchieved = 4,
                    totalGoals = 5,
                    feedback = "Gran proactividad y mentoría a desarrolladores junior del equipo.",
                    reviewerName = "Laura Martínez Gómez"
                ),
                PerformanceReview(
                    employeeId = insertedIds[3],
                    employeeName = "Alejandro Vargas León",
                    date = "2024-06-30",
                    overallRating = 4.8f,
                    punctualityRating = 4.9f,
                    productivityRating = 5.0f,
                    teamworkRating = 4.5f,
                    goalsAchieved = 6,
                    totalGoals = 6,
                    feedback = "Superó el objetivo de cuota de ventas en un 120% durante el último trimestre.",
                    reviewerName = "Dirección Comercial"
                )
            )
            for (rev in reviews) {
                performanceDao.insertReview(rev)
            }

            // Notifications
            notificationDao.insertNotification(
                NotificationLog(
                    timestamp = System.currentTimeMillis() - 7200000L,
                    title = "Nueva solicitud de tiempo libre",
                    message = "Sofía Romero Ruiz ha solicitado 2 días de Asuntos Propios.",
                    channel = "SLACK",
                    isRead = false
                )
            )
            notificationDao.insertNotification(
                NotificationLog(
                    timestamp = System.currentTimeMillis() - 14400000L,
                    title = "Vacaciones aprobadas",
                    message = "Se han aprobado las vacaciones de Elena Morales Bravo (8 días).",
                    channel = "EMAIL",
                    isRead = true
                )
            )
            notificationDao.insertNotification(
                NotificationLog(
                    timestamp = System.currentTimeMillis() - 86400000L,
                    title = "Turnos asignados",
                    message = "Se han publicado los turnos para la semana actual.",
                    channel = "SISTEMA",
                    isRead = true
                )
            )

            // Seed initial TimeClockEntries (including one offline in basement pending WorkManager sync)
            val timeClockDao = database.timeClockDao()
            timeClockDao.insertClockEntry(
                TimeClockEntry(
                    employeeId = insertedIds[0],
                    employeeName = "Laura Martínez Gómez",
                    department = "Tecnología",
                    clockType = ClockType.ENTRADA,
                    timestamp = System.currentTimeMillis() - 7200000L,
                    formattedTime = "08:02:15",
                    formattedDate = todayStr,
                    locationTag = "Sede Central - Acceso Principal",
                    syncStatus = SyncStatus.SYNCED,
                    serverSyncId = "SRV-CLOUD-99412"
                )
            )
            timeClockDao.insertClockEntry(
                TimeClockEntry(
                    employeeId = insertedIds[5], // Javier Fernández
                    employeeName = "Javier Fernández Soto",
                    department = "Operaciones",
                    clockType = ClockType.ENTRADA,
                    timestamp = System.currentTimeMillis() - 1800000L,
                    formattedTime = "07:31:40",
                    formattedDate = todayStr,
                    locationTag = "Almacén Logístico (Modo Offline)",
                    syncStatus = SyncStatus.PENDING,
                    syncAttempts = 1,
                    lastSyncAttemptAt = System.currentTimeMillis() - 600000L
                )
            )
        }
    }
}
