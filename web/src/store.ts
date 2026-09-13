import { CompanyEnvironment, Employee, Shift, TimeOffRequest, DailyTask, PerformanceReview, NotificationLog, TimeClockEntry, WorkSchedulePattern } from './types'

const STORAGE_KEY = 's1_web_db_v1'

interface DB {
  companies: CompanyEnvironment[]
  employees: Employee[]
  shifts: Shift[]
  timeOffRequests: TimeOffRequest[]
  tasks: DailyTask[]
  reviews: PerformanceReview[]
  notifications: NotificationLog[]
  clockEntries: TimeClockEntry[]
  lastIds: Record<string, number>
}

const defaultCompanies: CompanyEnvironment[] = [
  { code: 'S1-CORP', name: 'S1 Corporation - Sede Central Madrid', adminEmail: 'admin@s1-corp.com', adminName: 'Administrador Maestro', createdAt: Date.now()-100000000, updatedAt: Date.now() }
]

function todayISO(): string { return new Date().toISOString().slice(0,10) }
function daysFromNow(n: number): string { const d = new Date(); d.setDate(d.getDate()+n); return d.toISOString().slice(0,10) }

function seedDB(): DB {
  const employees: Employee[] = [
    { id: 1, companyCode: 'S1-CORP', name: 'Laura Martínez Gómez', email: 'laura.martinez@s1-corp.com', phone: '612345678', passwordHash: '123456', isMasterAdmin: true, authProvider: 'LOCAL', jobTitle: 'Directora de Tecnología', department: 'Tecnología', project: 'S1 Platform', functionalArea: 'Desarrollo', systemRole: 'ADMIN', workSchedulePattern: 'LUNES_A_VIERNES', status: 'ACTIVO', avatarColorHex: 0xFF2563EB, hireDate: '2022-01-15', notes: 'Admin maestra', updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', name: 'Carlos Santana Peña', email: 'carlos.santana@s1-corp.com', phone: '623456789', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Tech Lead Frontend', department: 'Tecnología', project: 'S1 Web', functionalArea: 'Frontend', systemRole: 'MANAGER', workSchedulePattern: 'LUNES_A_VIERNES', status: 'ACTIVO', avatarColorHex: 0xFF10B981, hireDate: '2022-03-10', notes: '', updatedAt: Date.now() },
    { id: 3, companyCode: 'S1-CORP', name: 'Sofía Romero Ruiz', email: 'sofia.romero@s1-corp.com', phone: '634567890', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'UX Designer', department: 'Tecnología', project: 'S1 Mobile', functionalArea: 'Diseño', systemRole: 'EMPLOYEE', workSchedulePattern: 'LUNES_A_VIERNES', status: 'ACTIVO', avatarColorHex: 0xFFF59E0B, hireDate: '2023-02-01', notes: '', updatedAt: Date.now() },
    { id: 4, companyCode: 'S1-CORP', name: 'Alejandro Vargas León', email: 'alejandro.vargas@s1-corp.com', phone: '645678901', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Ejecutivo de Ventas', department: 'Ventas', project: 'Expansión', functionalArea: 'Comercial', systemRole: 'EMPLOYEE', workSchedulePattern: 'JUEVES_A_DOMINGO', status: 'ACTIVO', avatarColorHex: 0xFFEF4444, hireDate: '2023-05-20', notes: '', updatedAt: Date.now() },
    { id: 5, companyCode: 'S1-CORP', name: 'Elena Morales Bravo', email: 'elena.morales@s1-corp.com', phone: '656789012', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Operadora Logística', department: 'Operaciones', project: 'Logística Central', functionalArea: 'Almacén', systemRole: 'EMPLOYEE', workSchedulePattern: 'SABADO_DOMINGO_LUNES', status: 'VACACIONES', avatarColorHex: 0xFF6366F1, hireDate: '2021-11-11', notes: '', updatedAt: Date.now() },
    { id: 6, companyCode: 'S1-CORP', name: 'Javier Fernández Soto', email: 'javier.fernandez@s1-corp.com', phone: '667890123', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Jefe de Almacén', department: 'Operaciones', project: 'Logística Central', functionalArea: 'Almacén', systemRole: 'MANAGER', workSchedulePattern: 'ROTATIVO_TOTAL', status: 'ACTIVO', avatarColorHex: 0xFF0D9488, hireDate: '2020-09-01', notes: '', updatedAt: Date.now() },
    { id: 7, companyCode: 'S1-CORP', name: 'Beatriz Navarro Cano', email: 'beatriz.navarro@s1-corp.com', phone: '678901234', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Técnica RRHH', department: 'Recursos Humanos', project: 'General', functionalArea: 'RRHH', systemRole: 'MANAGER', workSchedulePattern: 'LUNES_A_VIERNES', status: 'ACTIVO', avatarColorHex: 0xFFEC4899, hireDate: '2022-07-07', notes: '', updatedAt: Date.now() },
    { id: 8, companyCode: 'S1-CORP', name: 'Miguel Torres Díaz', email: 'miguel.torres@s1-corp.com', phone: '689012345', passwordHash: '123456', isMasterAdmin: false, authProvider: 'LOCAL', jobTitle: 'Operador Turno Noche', department: 'Operaciones', project: 'Seguridad', functionalArea: 'Vigilancia', systemRole: 'EMPLOYEE', workSchedulePattern: 'ROTATIVO_TOTAL', status: 'ACTIVO', avatarColorHex: 0xFF475569, hireDate: '2023-10-10', notes: '', updatedAt: Date.now() },
  ]

  const shifts: Shift[] = [
    { id: 1, companyCode: 'S1-CORP', employeeId: 3, employeeName: 'Sofía Romero Ruiz', department: 'Tecnología', date: todayISO(), shiftType: 'MANANA', startTime: '08:00', endTime: '16:00', notes: 'Sprint review', updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', employeeId: 4, employeeName: 'Alejandro Vargas León', department: 'Ventas', date: todayISO(), shiftType: 'TARDE', startTime: '16:00', endTime: '00:00', notes: 'Visita cliente', updatedAt: Date.now() },
    { id: 3, companyCode: 'S1-CORP', employeeId: 6, employeeName: 'Javier Fernández Soto', department: 'Operaciones', date: todayISO(), shiftType: 'MANANA', startTime: '07:00', endTime: '15:00', notes: 'Inventario', updatedAt: Date.now() },
    { id: 4, companyCode: 'S1-CORP', employeeId: 8, employeeName: 'Miguel Torres Díaz', department: 'Operaciones', date: todayISO(), shiftType: 'NOCHE', startTime: '00:00', endTime: '08:00', notes: 'Ronda nocturna', updatedAt: Date.now() },
    { id: 5, companyCode: 'S1-CORP', employeeId: 5, employeeName: 'Elena Morales Bravo', department: 'Operaciones', date: daysFromNow(1), shiftType: 'GUARDIA', startTime: '08:00', endTime: '08:00', notes: 'Guardia fin de semana', updatedAt: Date.now() },
  ]

  const timeOffRequests: TimeOffRequest[] = [
    { id: 1, companyCode: 'S1-CORP', employeeId: 3, employeeName: 'Sofía Romero Ruiz', department: 'Tecnología', type: 'ASUNTOS_PROPIOS', customDayType: '', startDate: daysFromNow(2), endDate: daysFromNow(3), daysCount: 2, legalRestDaysCount: 0, schedulePattern: 'LUNES_A_VIERNES', reason: 'Gestiones notariales', status: 'PENDIENTE', requestedAt: Date.now()-7200000, updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', employeeId: 5, employeeName: 'Elena Morales Bravo', department: 'Operaciones', type: 'VACACIONES', customDayType: '', startDate: daysFromNow(-10), endDate: daysFromNow(-2), daysCount: 6, legalRestDaysCount: 2, schedulePattern: 'SABADO_DOMINGO_LUNES', reason: 'Vacaciones verano', status: 'APROBADO', requestedAt: Date.now()-86400000*5, reviewedBy: 'Beatriz Navarro', reviewedAt: Date.now()-86400000*4, updatedAt: Date.now() },
    { id: 3, companyCode: 'S1-CORP', employeeId: 4, employeeName: 'Alejandro Vargas León', department: 'Ventas', type: 'COMPENSATORIO', customDayType: '', startDate: daysFromNow(10), endDate: daysFromNow(10), daysCount: 1, legalRestDaysCount: 0, schedulePattern: 'JUEVES_A_DOMINGO', reason: 'Feria sectorial fin de semana', status: 'PENDIENTE', requestedAt: Date.now()-3600000, updatedAt: Date.now() },
  ]

  const tasks: DailyTask[] = [
    { id: 1, companyCode: 'S1-CORP', employeeId: 1, employeeName: 'Laura Martínez Gómez', department: 'Tecnología', assignmentScope: 'PERSONAL', date: todayISO(), title: 'Auditar seguridad Kubernetes', description: 'Revisar cortafuegos clúster prod', priority: 'ALTA', status: 'EN_PROGRESO', updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', employeeId: 2, employeeName: 'Carlos Santana Peña', department: 'Tecnología', assignmentScope: 'PERSONAL', date: todayISO(), title: 'PR accesibilidad Compose', description: '', priority: 'MEDIA', status: 'COMPLETADA', updatedAt: Date.now() },
    { id: 3, companyCode: 'S1-CORP', employeeId: 0, employeeName: 'Sección Tecnología', department: 'Tecnología', assignmentScope: 'SECCION', date: todayISO(), title: 'Despliegue actualización servidores', description: 'Monitorizar despliegue v2.4', priority: 'ALTA', status: 'EN_PROGRESO', updatedAt: Date.now() },
    { id: 4, companyCode: 'S1-CORP', employeeId: 6, employeeName: 'Javier Fernández Soto', department: 'Operaciones', assignmentScope: 'PERSONAL', date: todayISO(), title: 'Control inventario', description: '', priority: 'MEDIA', status: 'COMPLETADA', updatedAt: Date.now() },
    { id: 5, companyCode: 'S1-CORP', employeeId: 0, employeeName: 'Sección Operaciones', department: 'Operaciones', assignmentScope: 'SECCION', date: todayISO(), title: 'Inspección seguridad almacén', description: '', priority: 'MEDIA', status: 'PENDIENTE', updatedAt: Date.now() },
  ]

  const reviews: PerformanceReview[] = [
    { id: 1, companyCode: 'S1-CORP', employeeId: 1, employeeName: 'Laura Martínez Gómez', date: '2024-06-30', overallRating: 4.9, punctualityRating: 5, productivityRating: 4.8, teamworkRating: 5, goalsAchieved: 5, totalGoals: 5, feedback: 'Excepcional liderazgo técnico', reviewerName: 'Dirección General', updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', employeeId: 4, employeeName: 'Alejandro Vargas León', date: '2024-06-30', overallRating: 4.8, punctualityRating: 4.9, productivityRating: 5, teamworkRating: 4.5, goalsAchieved: 6, totalGoals: 6, feedback: 'Superó cuota 120%', reviewerName: 'Dirección Comercial', updatedAt: Date.now() },
  ]

  const notifications: NotificationLog[] = [
    { id: 1, timestamp: Date.now()-7200000, title: 'Nueva solicitud de tiempo libre', message: 'Sofía Romero Ruiz ha solicitado 2 días de Asuntos Propios.', channel: 'SLACK', isRead: false },
    { id: 2, timestamp: Date.now()-14400000, title: 'Vacaciones aprobadas', message: 'Se han aprobado las vacaciones de Elena Morales Bravo (8 días).', channel: 'EMAIL', isRead: true },
    { id: 3, timestamp: Date.now()-86400000, title: 'Turnos asignados', message: 'Se han publicado los turnos para la semana actual.', channel: 'SISTEMA', isRead: true },
  ]

  const clockEntries: TimeClockEntry[] = [
    { id: 1, companyCode: 'S1-CORP', employeeId: 1, employeeName: 'Laura Martínez Gómez', department: 'Tecnología', clockType: 'ENTRADA', timestamp: Date.now()-7200000, formattedTime: '08:02:15', formattedDate: todayISO(), locationTag: 'Sede Central - Acceso Principal', syncStatus: 'SYNCED', syncAttempts: 1, serverSyncId: 'SRV-CLOUD-99412', updatedAt: Date.now() },
    { id: 2, companyCode: 'S1-CORP', employeeId: 6, employeeName: 'Javier Fernández Soto', department: 'Operaciones', clockType: 'ENTRADA', timestamp: Date.now()-1800000, formattedTime: '07:31:40', formattedDate: todayISO(), locationTag: 'Almacén Logístico (Modo Offline)', syncStatus: 'PENDING', syncAttempts: 1, lastSyncAttemptAt: Date.now()-600000, updatedAt: Date.now() },
  ]

  return {
    companies: defaultCompanies,
    employees,
    shifts,
    timeOffRequests,
    tasks,
    reviews,
    notifications,
    clockEntries,
    lastIds: { employee: 8, shift: 5, timeOff: 3, task: 5, review: 2, notification: 3, clock: 2, company: 1 }
  }
}

function loadDB(): DB {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as DB
  } catch {}
  const seeded = seedDB()
  saveDB(seeded)
  return seeded
}

function saveDB(db: DB) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(db))
}

export class Store {
  private db: DB

  constructor() {
    this.db = loadDB()
  }

  private persist() { saveDB(this.db) }

  // Companies
  getCompanies(): CompanyEnvironment[] { return this.db.companies }
  getCompany(code: string): CompanyEnvironment | undefined { return this.db.companies.find(c => c.code.toUpperCase() === code.toUpperCase()) }
  addCompany(c: CompanyEnvironment) {
    this.db.companies.push(c)
    this.db.lastIds.company = (this.db.lastIds.company || 0) + 1
    this.persist()
  }

  // Employees
  getEmployees(companyCode: string): Employee[] { return this.db.employees.filter(e => e.companyCode.toUpperCase() === companyCode.toUpperCase()) }
  getAllEmployees(): Employee[] { return this.db.employees }
  addEmployee(e: Employee): Employee {
    const id = (this.db.lastIds.employee || 0) + 1
    const ne = { ...e, id, updatedAt: Date.now() }
    this.db.employees.push(ne)
    this.db.lastIds.employee = id
    this.persist()
    return ne
  }
  updateEmployee(e: Employee) {
    const idx = this.db.employees.findIndex(x => x.id === e.id)
    if (idx >=0) { this.db.employees[idx] = { ...e, updatedAt: Date.now() }; this.persist() }
  }
  deleteEmployee(id: number) {
    const emp = this.db.employees.find(x => x.id === id)
    if (emp?.isMasterAdmin) throw new Error('No se puede borrar al Administrador Maestro')
    this.db.employees = this.db.employees.filter(x => x.id !== id)
    this.persist()
  }
  getEmployeeByEmail(companyCode: string, email: string): Employee | undefined {
    return this.db.employees.find(e => e.companyCode.toUpperCase() === companyCode.toUpperCase() && e.email.toLowerCase() === email.toLowerCase())
  }

  // Shifts
  getShifts(companyCode: string): Shift[] { return this.db.shifts.filter(s => s.companyCode.toUpperCase() === companyCode.toUpperCase()).sort((a,b) => b.date.localeCompare(a.date)) }
  addShift(s: Shift): Shift {
    const id = (this.db.lastIds.shift || 0) + 1
    const ns = { ...s, id, updatedAt: Date.now() }
    this.db.shifts.push(ns)
    this.db.lastIds.shift = id
    this.persist()
    return ns
  }
  updateShift(s: Shift) {
    const idx = this.db.shifts.findIndex(x => x.id === s.id)
    if (idx >=0) { this.db.shifts[idx] = { ...s, updatedAt: Date.now() }; this.persist() }
  }
  deleteShift(id: number) { this.db.shifts = this.db.shifts.filter(x => x.id !== id); this.persist() }

  // TimeOff
  getTimeOff(companyCode: string): TimeOffRequest[] { return this.db.timeOffRequests.filter(t => t.companyCode.toUpperCase() === companyCode.toUpperCase()).sort((a,b) => b.requestedAt - a.requestedAt) }
  addTimeOff(r: TimeOffRequest): TimeOffRequest {
    const id = (this.db.lastIds.timeOff || 0) + 1
    const nr = { ...r, id, updatedAt: Date.now() }
    this.db.timeOffRequests.push(nr)
    this.db.lastIds.timeOff = id
    this.persist()
    return nr
  }
  updateTimeOff(r: TimeOffRequest) {
    const idx = this.db.timeOffRequests.findIndex(x => x.id === r.id)
    if (idx >=0) { this.db.timeOffRequests[idx] = { ...r, updatedAt: Date.now() }; this.persist() }
  }
  deleteTimeOff(id: number) { this.db.timeOffRequests = this.db.timeOffRequests.filter(x => x.id !== id); this.persist() }

  // Tasks
  getTasks(companyCode: string): DailyTask[] { return this.db.tasks.filter(t => t.companyCode.toUpperCase() === companyCode.toUpperCase()).sort((a,b) => b.date.localeCompare(a.date)) }
  addTask(t: DailyTask): DailyTask {
    const id = (this.db.lastIds.task || 0) + 1
    const nt = { ...t, id, updatedAt: Date.now() }
    this.db.tasks.push(nt)
    this.db.lastIds.task = id
    this.persist()
    return nt
  }
  updateTask(t: DailyTask) { const idx = this.db.tasks.findIndex(x => x.id === t.id); if (idx>=0){ this.db.tasks[idx] = { ...t, updatedAt: Date.now() }; this.persist() } }
  deleteTask(id: number) { this.db.tasks = this.db.tasks.filter(x => x.id !== id); this.persist() }

  // Reviews
  getReviews(companyCode: string): PerformanceReview[] { return this.db.reviews.filter(r => r.companyCode.toUpperCase() === companyCode.toUpperCase()) }
  addReview(r: PerformanceReview): PerformanceReview { const id = (this.db.lastIds.review || 0)+1; const nr = {...r, id, updatedAt: Date.now()}; this.db.reviews.push(nr); this.db.lastIds.review=id; this.persist(); return nr }
  deleteReview(id: number){ this.db.reviews = this.db.reviews.filter(x=>x.id!==id); this.persist() }

  // Notifications
  getNotifications(): NotificationLog[] { return [...this.db.notifications].sort((a,b)=>b.timestamp-a.timestamp).slice(0,50) }
  addNotification(n: Omit<NotificationLog,'id'>): NotificationLog { const id=(this.db.lastIds.notification||0)+1; const nn={...n,id} as NotificationLog; this.db.notifications.push(nn); this.db.lastIds.notification=id; this.persist(); return nn }
  markAllRead(){ this.db.notifications.forEach(n=>n.isRead=true); this.persist() }
  clearNotifications(){ this.db.notifications=[]; this.persist() }

  // Clock
  getClockEntries(companyCode: string): TimeClockEntry[] { return this.db.clockEntries.filter(c=>c.companyCode.toUpperCase()===companyCode.toUpperCase()).sort((a,b)=>b.timestamp-a.timestamp) }
  addClockEntry(e: TimeClockEntry): TimeClockEntry { const id=(this.db.lastIds.clock||0)+1; const ne={...e,id, updatedAt: Date.now()}; this.db.clockEntries.push(ne); this.db.lastIds.clock=id; this.persist(); return ne }
  updateClockEntry(e: TimeClockEntry){ const idx=this.db.clockEntries.findIndex(x=>x.id===e.id); if(idx>=0){ this.db.clockEntries[idx]={...e, updatedAt: Date.now()}; this.persist() } }
  getPendingCount(companyCode: string){ return this.db.clockEntries.filter(c=>c.companyCode.toUpperCase()===companyCode.toUpperCase() && (c.syncStatus==='PENDING'||c.syncStatus==='FAILED')).length }

  reset(){ localStorage.removeItem(STORAGE_KEY); this.db = seedDB(); this.persist() }
}

export const store = new Store()

export function seedIfNeeded() {
  // already handled
}
