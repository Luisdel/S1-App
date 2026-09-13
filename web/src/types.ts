export type SystemRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
export type EmployeeStatus = 'ACTIVO' | 'VACACIONES' | 'BAJA_MEDICA' | 'INACTIVO'
export type ShiftType = 'MANANA' | 'TARDE' | 'NOCHE' | 'PARTIDO' | 'GUARDIA'
export type TimeOffType = 'VACACIONES' | 'ASUNTOS_PROPIOS' | 'DIA_ADICIONAL_GUARDIA' | 'COMPENSATORIO' | 'BAJA_MEDICA' | 'MATERNIDAD_PATERNIDAD' | 'OTRO'
export type RequestStatus = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO'
export type TaskPriority = 'ALTA' | 'MEDIA' | 'BAJA'
export type TaskStatus = 'PENDIENTE' | 'EN_PROGRESO' | 'COMPLETADA'
export type TaskAssignmentScope = 'PERSONAL' | 'SECCION'
export type WorkSchedulePattern = 'LUNES_A_VIERNES' | 'SABADO_DOMINGO_LUNES' | 'JUEVES_A_DOMINGO' | 'ROTATIVO_TOTAL'
export type ClockType = 'ENTRADA' | 'SALIDA' | 'PAUSA_INICIO' | 'PAUSA_FIN'
export type SyncStatus = 'PENDING' | 'SYNCING' | 'SYNCED' | 'FAILED'

export interface CompanyEnvironment {
  code: string
  name: string
  adminEmail: string
  adminName: string
  createdAt: number
  updatedAt: number
}

export interface Employee {
  id: number
  companyCode: string
  name: string
  email: string
  phone: string
  passwordHash: string
  isMasterAdmin: boolean
  authProvider: 'LOCAL' | 'GOOGLE'
  jobTitle: string
  department: string
  project: string
  functionalArea: string
  systemRole: SystemRole
  workSchedulePattern: WorkSchedulePattern
  status: EmployeeStatus
  avatarColorHex: number
  hireDate: string
  notes: string
  updatedAt: number
}

export interface Shift {
  id: number
  companyCode: string
  employeeId: number
  employeeName: string
  department: string
  date: string
  shiftType: ShiftType
  startTime: string
  endTime: string
  notes: string
  updatedAt: number
}

export interface TimeOffRequest {
  id: number
  companyCode: string
  employeeId: number
  employeeName: string
  department: string
  type: TimeOffType
  customDayType: string
  startDate: string
  endDate: string
  daysCount: number
  legalRestDaysCount: number
  schedulePattern: WorkSchedulePattern
  reason: string
  status: RequestStatus
  requestedAt: number
  reviewedBy?: string
  reviewedAt?: number
  updatedAt: number
}

export interface TimeClockEntry {
  id: number
  companyCode: string
  employeeId: number
  employeeName: string
  department: string
  clockType: ClockType
  timestamp: number
  formattedTime: string
  formattedDate: string
  locationTag: string
  syncStatus: SyncStatus
  syncAttempts: number
  lastSyncAttemptAt?: number
  serverSyncId?: string
  notes?: string
  updatedAt: number
}

export interface DailyTask {
  id: number
  companyCode: string
  employeeId: number
  employeeName: string
  department: string
  assignmentScope: TaskAssignmentScope
  date: string
  title: string
  description: string
  priority: TaskPriority
  status: TaskStatus
  updatedAt: number
}

export interface PerformanceReview {
  id: number
  companyCode: string
  employeeId: number
  employeeName: string
  date: string
  overallRating: number
  punctualityRating: number
  productivityRating: number
  teamworkRating: number
  goalsAchieved: number
  totalGoals: number
  feedback: string
  reviewerName: string
  updatedAt: number
}

export interface NotificationLog {
  id: number
  timestamp: number
  title: string
  message: string
  channel: 'SISTEMA' | 'SLACK' | 'EMAIL'
  isRead: boolean
}

export interface RolePermissions {
  canManageEmployees: boolean
  canAssignShifts: boolean
  canApproveTimeOff: boolean
  canAssignTasks: boolean
  canReviewPerformance: boolean
  canExportReports: boolean
  canConfigSystem: boolean
}

export interface IntegrationsConfig {
  slackWebhookUrl: string
  slackChannel: string
  emailRecipient: string
  autoNotifyOnTimeOff: boolean
  autoNotifyOnApproval: boolean
}

export interface StaffKpis {
  totalEmployees: number
  activeEmployees: number
  availabilityPercentage: number
  onVacationCount: number
  onLeaveCount: number
  pendingRequestsCount: number
  shiftsTodayCount: number
  completedTasksTodayCount: number
  totalTasksTodayCount: number
  taskCompletionRate: number
}

export type NavigationDestination = 'DASHBOARD' | 'PERSONNEL' | 'SHIFTS' | 'TIMEOFF' | 'TASKS' | 'PERFORMANCE' | 'REPORTS' | 'ADMIN'

export const SystemRoleLabels: Record<SystemRole, string> = {
  ADMIN: 'Administrador',
  MANAGER: 'Supervisor / Manager',
  EMPLOYEE: 'Empleado / Operador'
}

export const EmployeeStatusLabels: Record<EmployeeStatus, {label: string, color: string}> = {
  ACTIVO: { label: 'Activo', color: '#10B981' },
  VACACIONES: { label: 'En Vacaciones', color: '#F59E0B' },
  BAJA_MEDICA: { label: 'Baja Médica', color: '#EF4444' },
  INACTIVO: { label: 'Inactivo', color: '#9CA3AF' }
}

export const ShiftTypeLabels: Record<ShiftType, {label: string, hours: string, color: string}> = {
  MANANA: { label: 'Mañana', hours: '08:00 - 16:00', color: '#3B82F6' },
  TARDE: { label: 'Tarde', hours: '16:00 - 00:00', color: '#F59E0B' },
  NOCHE: { label: 'Noche', hours: '00:00 - 08:00', color: '#6366F1' },
  PARTIDO: { label: 'Partido', hours: '09:00-14:00 / 17:00-20:00', color: '#10B981' },
  GUARDIA: { label: 'Guardia 24h', hours: '08:00 - 08:00 (+1)', color: '#EC4899' }
}

export const TimeOffTypeLabels: Record<TimeOffType, string> = {
  VACACIONES: 'Vacaciones Anuales',
  ASUNTOS_PROPIOS: 'Asuntos Propios',
  DIA_ADICIONAL_GUARDIA: 'Día Adicional por Guardia',
  COMPENSATORIO: 'Día Compensatorio',
  BAJA_MEDICA: 'Baja Médica / Incapacidad',
  MATERNIDAD_PATERNIDAD: 'Permiso Maternidad / Paternidad',
  OTRO: 'Otro Permiso'
}

export const ClockTypeLabels: Record<ClockType, string> = {
  ENTRADA: 'Entrada de Turno',
  SALIDA: 'Salida de Turno',
  PAUSA_INICIO: 'Inicio de Descanso',
  PAUSA_FIN: 'Fin de Descanso'
}

export const WorkSchedulePatternLabels: Record<WorkSchedulePattern, {label: string, shortLabel: string, desc: string, days: number[]}> = {
  LUNES_A_VIERNES: { label: 'Lunes a Viernes (Estándar)', shortLabel: 'L - V', desc: 'Trabajo ordinario L-V. Descanso: Sáb y Dom.', days: [1,2,3,4,5] },
  SABADO_DOMINGO_LUNES: { label: 'Sábado, Domingo y Lunes (Atípico Fijo)', shortLabel: 'Sáb - Dom - Lun', desc: 'Turno fijo fines de semana y lunes.', days: [6,7,1] },
  JUEVES_A_DOMINGO: { label: 'Jueves a Domingo (Jornada 4x3)', shortLabel: 'Jue - Dom', desc: 'Intensivo J-D. Descanso L-M-X.', days: [4,5,6,7] },
  ROTATIVO_TOTAL: { label: 'Turnos Continuos / 24-7', shortLabel: 'Continuo 24/7', desc: 'Rotativa continua sujeta a cuadrante.', days: [1,2,3,4,5,6,7] }
}
