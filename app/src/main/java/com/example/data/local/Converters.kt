package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromSystemRole(value: SystemRole): String = value.name

    @TypeConverter
    fun toSystemRole(value: String): SystemRole = runCatching { SystemRole.valueOf(value) }.getOrDefault(SystemRole.EMPLOYEE)

    @TypeConverter
    fun fromEmployeeStatus(value: EmployeeStatus): String = value.name

    @TypeConverter
    fun toEmployeeStatus(value: String): EmployeeStatus = runCatching { EmployeeStatus.valueOf(value) }.getOrDefault(EmployeeStatus.ACTIVO)

    @TypeConverter
    fun fromShiftType(value: ShiftType): String = value.name

    @TypeConverter
    fun toShiftType(value: String): ShiftType = runCatching { ShiftType.valueOf(value) }.getOrDefault(ShiftType.MANANA)

    @TypeConverter
    fun fromTimeOffType(value: TimeOffType): String = value.name

    @TypeConverter
    fun toTimeOffType(value: String): TimeOffType = runCatching { TimeOffType.valueOf(value) }.getOrDefault(TimeOffType.VACACIONES)

    @TypeConverter
    fun fromRequestStatus(value: RequestStatus): String = value.name

    @TypeConverter
    fun toRequestStatus(value: String): RequestStatus = runCatching { RequestStatus.valueOf(value) }.getOrDefault(RequestStatus.PENDIENTE)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.MEDIA)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.PENDIENTE)

    @TypeConverter
    fun fromTaskAssignmentScope(value: TaskAssignmentScope): String = value.name

    @TypeConverter
    fun toTaskAssignmentScope(value: String): TaskAssignmentScope = runCatching { TaskAssignmentScope.valueOf(value) }.getOrDefault(TaskAssignmentScope.PERSONAL)
}
