package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class StaffRepository(private val database: AppDatabase) {

    val employees: Flow<List<Employee>> = database.employeeDao().getAllEmployees()
    val shifts: Flow<List<Shift>> = database.shiftDao().getAllShifts()
    val timeOffRequests: Flow<List<TimeOffRequest>> = database.timeOffDao().getAllRequests()
    val tasks: Flow<List<DailyTask>> = database.taskDao().getAllTasks()
    val reviews: Flow<List<PerformanceReview>> = database.performanceDao().getAllReviews()
    val notifications: Flow<List<NotificationLog>> = database.notificationDao().getRecentNotifications()

    // Employees
    suspend fun insertEmployee(employee: Employee): Long = database.employeeDao().insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = database.employeeDao().updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = database.employeeDao().deleteEmployee(employee)
    suspend fun getEmployeeByEmail(email: String): Employee? = database.employeeDao().getEmployeeByEmail(email)
    suspend fun getEmployeeCount(): Int = database.employeeDao().getEmployeeCount()
    suspend fun getMasterAdminCount(): Int = database.employeeDao().getMasterAdminCount()
    suspend fun getMasterAdmin(): Employee? = database.employeeDao().getMasterAdmin()

    // Shifts
    suspend fun insertShift(shift: Shift): Long = database.shiftDao().insertShift(shift)
    suspend fun updateShift(shift: Shift) = database.shiftDao().updateShift(shift)
    suspend fun deleteShift(shift: Shift) = database.shiftDao().deleteShift(shift)

    // Time-off
    suspend fun insertTimeOff(request: TimeOffRequest): Long = database.timeOffDao().insertRequest(request)
    suspend fun updateTimeOff(request: TimeOffRequest) = database.timeOffDao().updateRequest(request)
    suspend fun deleteTimeOff(request: TimeOffRequest) = database.timeOffDao().deleteRequest(request)

    suspend fun updateTimeOffStatus(
        requestId: Long,
        newStatus: RequestStatus,
        reviewerName: String
    ) {
        val req = database.timeOffDao().getRequestById(requestId) ?: return
        val updated = req.copy(
            status = newStatus,
            reviewedBy = reviewerName,
            reviewedAt = System.currentTimeMillis()
        )
        database.timeOffDao().updateRequest(updated)

        // If approved and it's VACACIONES or BAJA_MEDICA, optionally update employee status
        if (newStatus == RequestStatus.APROBADO) {
            val emp = database.employeeDao().getEmployeeById(req.employeeId)
            if (emp != null) {
                val newEmpStatus = when (req.type) {
                    TimeOffType.VACACIONES -> EmployeeStatus.VACACIONES
                    TimeOffType.BAJA_MEDICA -> EmployeeStatus.BAJA_MEDICA
                    else -> emp.status
                }
                if (newEmpStatus != emp.status) {
                    database.employeeDao().updateEmployee(emp.copy(status = newEmpStatus))
                }
            }
        }
    }

    // Tasks
    suspend fun insertTask(task: DailyTask): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: DailyTask) = database.taskDao().updateTask(task)
    suspend fun deleteTask(task: DailyTask) = database.taskDao().deleteTask(task)

    // Performance Reviews
    suspend fun insertReview(review: PerformanceReview): Long = database.performanceDao().insertReview(review)
    suspend fun deleteReview(review: PerformanceReview) = database.performanceDao().deleteReview(review)

    // Notifications
    suspend fun addNotification(log: NotificationLog): Long = database.notificationDao().insertNotification(log)
    suspend fun markAllNotificationsAsRead() = database.notificationDao().markAllAsRead()
    suspend fun clearNotifications() = database.notificationDao().clearAll()
}
