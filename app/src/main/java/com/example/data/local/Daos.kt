package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getEmployeeByEmail(email: String): Employee?

    @Query("SELECT * FROM employees WHERE department = :dept ORDER BY name ASC")
    fun getEmployeesByDepartment(dept: String): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE project = :project ORDER BY name ASC")
    fun getEmployeesByProject(project: String): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE functionalArea = :area ORDER BY name ASC")
    fun getEmployeesByArea(area: String): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Query("SELECT COUNT(*) FROM employees WHERE isMasterAdmin = 1")
    suspend fun getMasterAdminCount(): Int

    @Query("SELECT * FROM employees WHERE isMasterAdmin = 1 LIMIT 1")
    suspend fun getMasterAdmin(): Employee?
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY date DESC, startTime ASC")
    fun getAllShifts(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE date = :date ORDER BY startTime ASC")
    fun getShiftsByDate(date: String): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE employeeId = :empId ORDER BY date DESC")
    fun getShiftsForEmployee(empId: Long): Flow<List<Shift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)
}

@Dao
interface TimeOffDao {
    @Query("SELECT * FROM time_off_requests ORDER BY requestedAt DESC")
    fun getAllRequests(): Flow<List<TimeOffRequest>>

    @Query("SELECT * FROM time_off_requests WHERE status = 'PENDIENTE' ORDER BY requestedAt DESC")
    fun getPendingRequests(): Flow<List<TimeOffRequest>>

    @Query("SELECT * FROM time_off_requests WHERE employeeId = :empId ORDER BY requestedAt DESC")
    fun getRequestsForEmployee(empId: Long): Flow<List<TimeOffRequest>>

    @Query("SELECT * FROM time_off_requests WHERE id = :id")
    suspend fun getRequestById(id: Long): TimeOffRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: TimeOffRequest): Long

    @Update
    suspend fun updateRequest(request: TimeOffRequest)

    @Delete
    suspend fun deleteRequest(request: TimeOffRequest)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM daily_tasks ORDER BY date DESC, priority ASC")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY priority ASC")
    fun getTasksByDate(date: String): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE employeeId = :empId ORDER BY date DESC")
    fun getTasksForEmployee(empId: Long): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Update
    suspend fun updateTask(task: DailyTask)

    @Delete
    suspend fun deleteTask(task: DailyTask)
}

@Dao
interface PerformanceDao {
    @Query("SELECT * FROM performance_reviews ORDER BY date DESC")
    fun getAllReviews(): Flow<List<PerformanceReview>>

    @Query("SELECT * FROM performance_reviews WHERE employeeId = :empId ORDER BY date DESC")
    fun getReviewsForEmployee(empId: Long): Flow<List<PerformanceReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: PerformanceReview): Long

    @Delete
    suspend fun deleteReview(review: PerformanceReview)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentNotifications(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(log: NotificationLog): Long

    @Query("UPDATE notification_logs SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()
}

@Dao
interface TimeClockDao {
    @Query("SELECT * FROM time_clock_entries ORDER BY timestamp DESC")
    fun getAllClockEntries(): Flow<List<TimeClockEntry>>

    @Query("SELECT * FROM time_clock_entries WHERE employeeId = :empId ORDER BY timestamp DESC")
    fun getClockEntriesForEmployee(empId: Long): Flow<List<TimeClockEntry>>

    @Query("SELECT * FROM time_clock_entries WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' ORDER BY timestamp ASC")
    suspend fun getPendingClockEntries(): List<TimeClockEntry>

    @Query("SELECT COUNT(*) FROM time_clock_entries WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM time_clock_entries WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClockEntry(entry: TimeClockEntry): Long

    @Update
    suspend fun updateClockEntry(entry: TimeClockEntry)

    @Delete
    suspend fun deleteClockEntry(entry: TimeClockEntry)

    @Query("SELECT * FROM time_clock_entries WHERE id = :id")
    suspend fun getClockEntryById(id: Long): TimeClockEntry?
}
