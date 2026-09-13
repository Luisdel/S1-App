import React from 'react'
import { CompanyEnvironment, Employee, SystemRole, RolePermissions, IntegrationsConfig, StaffKpis, NavigationDestination, TimeClockEntry, ClockType, RequestStatus, TaskStatus, TimeOffRequest, DailyTask, PerformanceReview } from './types'
import { store } from './store'
import { LoginScreen } from './screens/LoginScreen'
import { DashboardScreen } from './screens/DashboardScreen'
import { PersonnelScreen } from './screens/PersonnelScreen'
import { ShiftsScreen } from './screens/ShiftsScreen'
import { TimeOffScreen } from './screens/TimeOffScreen'
import { TasksScreen } from './screens/TasksScreen'
import { PerformanceScreen } from './screens/PerformanceScreen'
import { ReportsScreen } from './screens/ReportsScreen'
import { AdminScreen } from './screens/AdminScreen'
import { Sidebar } from './components/Sidebar'
import { TopBar } from './components/TopBar'

export default function App(){
  const [companies, setCompanies] = React.useState<CompanyEnvironment[]>(()=>store.getCompanies())
  const [currentCompanyCode, setCurrentCompanyCode] = React.useState('S1-CORP')
  const [loggedInEmployee, setLoggedInEmployee] = React.useState<Employee | null>(null)
  const [currentRole, setCurrentRole] = React.useState<SystemRole>('EMPLOYEE')
  const [currentDestination, setCurrentDestination] = React.useState<NavigationDestination>('DASHBOARD')
  const [sidebarOpen, setSidebarOpen] = React.useState(false)

  const [employees, setEmployees] = React.useState<Employee[]>(()=>store.getEmployees('S1-CORP'))
  const [shifts, setShifts] = React.useState(()=>store.getShifts('S1-CORP'))
  const [timeOffRequests, setTimeOffRequests] = React.useState(()=>store.getTimeOff('S1-CORP'))
  const [tasks, setTasks] = React.useState(()=>store.getTasks('S1-CORP'))
  const [reviews, setReviews] = React.useState(()=>store.getReviews('S1-CORP'))
  const [clockEntries, setClockEntries] = React.useState(()=>store.getClockEntries('S1-CORP'))
  const [notifications, setNotifications] = React.useState(()=>store.getNotifications())

  const [selectedDept, setSelectedDept] = React.useState('Todos')
  const [isOnline, setIsOnline] = React.useState(navigator.onLine)
  const [statusMessage, setStatusMessage] = React.useState<string | null>(null)
  const [showNotifications, setShowNotifications] = React.useState(false)

  const [adminPerms] = React.useState<RolePermissions>({ canManageEmployees:true, canAssignShifts:true, canApproveTimeOff:true, canAssignTasks:true, canReviewPerformance:true, canExportReports:true, canConfigSystem:true })
  const [managerPerms] = React.useState<RolePermissions>({ canManageEmployees:false, canAssignShifts:true, canApproveTimeOff:true, canAssignTasks:true, canReviewPerformance:true, canExportReports:true, canConfigSystem:false })
  const [employeePerms] = React.useState<RolePermissions>({ canManageEmployees:false, canAssignShifts:false, canApproveTimeOff:false, canAssignTasks:false, canReviewPerformance:false, canExportReports:false, canConfigSystem:false })
  const [integrations, setIntegrations] = React.useState<IntegrationsConfig>({ slackWebhookUrl:'https://hooks.slack.com/services/T000/B000/XXXX', slackChannel:'#turnos-rrhh', emailRecipient:'rrhh@empresa.com', autoNotifyOnTimeOff:true, autoNotifyOnApproval:true })

  const refreshData = React.useCallback(()=>{
    const code = currentCompanyCode
    setEmployees(store.getEmployees(code))
    setShifts(store.getShifts(code))
    setTimeOffRequests(store.getTimeOff(code))
    setTasks(store.getTasks(code))
    setReviews(store.getReviews(code))
    setClockEntries(store.getClockEntries(code))
    setNotifications(store.getNotifications())
    setCompanies(store.getCompanies())
  },[currentCompanyCode])

  React.useEffect(()=>{
    refreshData()
    const handleOnline = () => setIsOnline(true)
    const handleOffline = () => setIsOnline(false)
    window.addEventListener('online',handleOnline)
    window.addEventListener('offline',handleOffline)
    return ()=>{ window.removeEventListener('online',handleOnline); window.removeEventListener('offline',handleOffline) }
  },[refreshData])

  React.useEffect(()=>{
    if(statusMessage){
      const t=setTimeout(()=>setStatusMessage(null),4000)
      return ()=>clearTimeout(t)
    }
  },[statusMessage])

  const kpis: StaffKpis = React.useMemo(()=>{
    const today = new Date().toISOString().slice(0,10)
    const total = employees.length
    const active = employees.filter(e=>e.status==='ACTIVO').length
    const vac = employees.filter(e=>e.status==='VACACIONES').length
    const baja = employees.filter(e=>e.status==='BAJA_MEDICA').length
    const avail = total ? Math.round(active/total*100) : 0
    const pending = timeOffRequests.filter(r=>r.status==='PENDIENTE').length
    const shiftsToday = shifts.filter(s=>s.date===today).length
    const todayTasks = tasks.filter(t=>t.date===today)
    const completed = todayTasks.filter(t=>t.status==='COMPLETADA').length
    const rate = todayTasks.length ? Math.round(completed/todayTasks.length*100) : 100
    return { totalEmployees: total, activeEmployees: active, availabilityPercentage: avail, onVacationCount: vac, onLeaveCount: baja, pendingRequestsCount: pending, shiftsTodayCount: shiftsToday, completedTasksTodayCount: completed, totalTasksTodayCount: todayTasks.length, taskCompletionRate: rate }
  },[employees,shifts,timeOffRequests,tasks])

  const currentPermissions = React.useMemo(()=>{
    if(currentRole==='ADMIN') return adminPerms
    if(currentRole==='MANAGER') return managerPerms
    return employeePerms
  },[currentRole,adminPerms,managerPerms,employeePerms])

  const handleLogin = (companyCode:string,email:string,password:string,cb:(ok:boolean,msg:string)=>void)=>{
    const code = companyCode.trim().toUpperCase()
    if(!code){ cb(false,'Código de empresa obligatorio'); return }
    const comp = store.getCompany(code)
    if(!comp){ cb(false,`Empresa ${code} no existe. Créala en pestaña Nueva Empresa.`); return }
    const emp = store.getEmployeeByEmail(code,email)
    if(!emp){ cb(false,`No existe empleado con email ${email} en ${code}`); return }
    if(emp.passwordHash !== password){ cb(false,'Contraseña incorrecta'); return }
    setCurrentCompanyCode(code)
    setLoggedInEmployee(emp)
    setCurrentRole(emp.systemRole)
    setTimeout(()=>refreshData(),0)
    cb(true,`Bienvenido ${emp.name}`)
    setStatusMessage(`✅ Sesión iniciada como ${emp.name} (${emp.systemRole}) en ${code}`)
  }

  const handleCreateCompany = (name:string,code:string,adminName:string,adminEmail:string,adminPassword:string,phone:string,cb:(ok:boolean,msg:string)=>void)=>{
    const cleanCode = code.trim().toUpperCase()
    if(cleanCode.length<3){ cb(false,'Código debe tener al menos 3 caracteres'); return }
    if(store.getCompany(cleanCode)){ cb(false,`Empresa ${cleanCode} ya existe`); return }
    const comp: CompanyEnvironment = { code: cleanCode, name, adminEmail, adminName, createdAt: Date.now(), updatedAt: Date.now() }
    store.addCompany(comp)
    const adminEmp: Employee = {
      id:0, companyCode: cleanCode, name: adminName, email: adminEmail, phone, passwordHash: adminPassword, isMasterAdmin:true, authProvider:'LOCAL',
      jobTitle:'Administrador Maestro', department:'Dirección', project:'General', functionalArea:'Administración',
      systemRole:'ADMIN', workSchedulePattern:'LUNES_A_VIERNES', status:'ACTIVO', avatarColorHex: 0xFF2563EB, hireDate: new Date().toISOString().slice(0,10), notes:'Admin maestro creado desde web', updatedAt: Date.now()
    }
    store.addEmployee(adminEmp)
    setCompanies(store.getCompanies())
    cb(true,`Empresa ${cleanCode} creada. Ya puedes acceder con ${adminEmail}`)
    setStatusMessage(`🏢 Empresa ${cleanCode} creada correctamente`)
  }

  const handleQuickLogin = (emp:Employee)=>{
    setCurrentCompanyCode(emp.companyCode)
    setLoggedInEmployee(emp)
    setCurrentRole(emp.systemRole)
    setTimeout(()=>refreshData(),0)
    setStatusMessage(`⚡ Acceso rápido como ${emp.name}`)
  }

  const handleLogout = ()=>{
    setLoggedInEmployee(null)
    setCurrentRole('EMPLOYEE')
    setCurrentDestination('DASHBOARD')
  }

  const addNotification = (title:string,message:string,channel:'SISTEMA'|'SLACK'|'EMAIL'='SISTEMA')=>{
    store.addNotification({ timestamp: Date.now(), title, message, channel, isRead:false })
    setNotifications(store.getNotifications())
  }

  const registerClock = (emp:Employee,type:ClockType,loc:string)=>{
    const now = new Date()
    const entry: TimeClockEntry = {
      id:0, companyCode: emp.companyCode, employeeId: emp.id, employeeName: emp.name, department: emp.department,
      clockType: type, timestamp: Date.now(), formattedTime: now.toTimeString().slice(0,8), formattedDate: now.toISOString().slice(0,10),
      locationTag: loc, syncStatus: isOnline ? 'SYNCING' : 'PENDING', syncAttempts: isOnline?1:0, updatedAt: Date.now()
    }
    const saved = store.addClockEntry(entry)
    // simulate sync
    if(isOnline){
      setTimeout(()=>{
        const synced = { ...saved, syncStatus:'SYNCED' as const, serverSyncId:`SRV-${Date.now().toString().slice(-6)}`, lastSyncAttemptAt: Date.now() }
        store.updateClockEntry(synced)
        setClockEntries(store.getClockEntries(currentCompanyCode))
        setStatusMessage(`✅ ${type} sincronizado con cloud (${synced.serverSyncId})`)
      },800)
      setStatusMessage(`⏳ Sincronizando fichaje ${type}...`)
    } else {
      setStatusMessage(`🏢 Fichaje guardado offline (sin cobertura). En cola para sync.`)
    }
    setClockEntries(store.getClockEntries(currentCompanyCode))
    addNotification(`Registro Jornada: ${type}`, `${emp.name} ha fichado ${type} a las ${entry.formattedTime} en ${loc}`, 'SISTEMA')
  }

  const triggerSync = ()=>{
    const pending = store.getClockEntries(currentCompanyCode).filter(c=>c.syncStatus==='PENDING'||c.syncStatus==='FAILED')
    if(pending.length===0){ setStatusMessage('✅ No hay fichajes pendientes de sincronizar'); return }
    pending.forEach(p=>{
      const upd = { ...p, syncStatus:'SYNCED' as const, serverSyncId:`SRV-SYNC-${Date.now().toString().slice(-5)}`, lastSyncAttemptAt: Date.now(), syncAttempts: p.syncAttempts+1 }
      store.updateClockEntry(upd)
    })
    setClockEntries(store.getClockEntries(currentCompanyCode))
    setStatusMessage(`🔄 Sincronizados ${pending.length} fichajes offline`)
    addNotification('Sincronización completada', `Se han sincronizado ${pending.length} fichajes pendientes`, 'SISTEMA')
  }

  const updateTimeOffStatus = (id:number,status:RequestStatus)=>{
    const req = timeOffRequests.find(r=>r.id===id)
    if(!req) return
    const upd = { ...req, status, reviewedBy: loggedInEmployee?.name || 'Sistema', reviewedAt: Date.now() }
    store.updateTimeOff(upd)
    if(status==='APROBADO' && (req.type==='VACACIONES' || req.type==='BAJA_MEDICA')){
      const emp = employees.find(e=>e.id===req.employeeId)
      if(emp){
        const newStatus = req.type==='VACACIONES' ? 'VACACIONES' : 'BAJA_MEDICA'
        store.updateEmployee({ ...emp, status: newStatus as any })
      }
    }
    refreshData()
    setStatusMessage(`${status==='APROBADO'?'✅ Aprobada':'❌ Rechazada'} solicitud de ${req.employeeName}`)
    addNotification(`Solicitud ${status}`, `${req.employeeName}: ${req.type} ${req.startDate}→${req.endDate} ha sido ${status}`, 'SISTEMA')
  }

  // Personnel handlers
  const handleAddEmployee = (e:Employee)=>{
    store.addEmployee(e)
    refreshData()
    setStatusMessage(`👤 Empleado ${e.name} creado`)
    addNotification('Nuevo empleado', `${e.name} dado de alta en ${e.department}`, 'SISTEMA')
  }
  const handleUpdateEmployee = (e:Employee)=>{ store.updateEmployee(e); refreshData(); setStatusMessage(`✏️ Empleado ${e.name} actualizado`) }
  const handleDeleteEmployee = (id:number)=>{ try{ store.deleteEmployee(id); refreshData(); setStatusMessage('🗑️ Empleado eliminado') } catch(err:any){ alert(err.message) } }

  if(!loggedInEmployee){
    return <LoginScreen companies={companies} currentCompanyCode={currentCompanyCode} employees={store.getAllEmployees().filter(e=>e.companyCode.toUpperCase()===currentCompanyCode.toUpperCase())} onSelectCompany={setCurrentCompanyCode} onLogin={handleLogin} onCreateCompany={handleCreateCompany} onQuickLogin={handleQuickLogin} />
  }

  const unread = notifications.filter(n=>!n.isRead).length

  return (
    <div className="app-shell">
      <Sidebar current={currentDestination} role={currentRole} onNavigate={setCurrentDestination} open={sidebarOpen} onClose={()=>setSidebarOpen(false)} />
      <div className="main-content">
        <TopBar currentRole={currentRole} currentUserName={loggedInEmployee.name} loggedInEmployee={loggedInEmployee} unreadCount={unread} onOpenNotifications={()=>setShowNotifications(true)} onOpenAdmin={()=>setCurrentDestination('ADMIN')} onLogout={handleLogout} onRoleChange={setCurrentRole} onToggleSidebar={()=>setSidebarOpen(v=>!v)} />
        {statusMessage && (
          <div style={{margin:'12px 24px 0',padding:'10px 14px',background:'#0F172A',color:'white',borderRadius:10,fontSize:13,display:'flex',justifyContent:'space-between',alignItems:'center'}}>
            <span>{statusMessage}</span>
            <button onClick={()=>setStatusMessage(null)} style={{background:'transparent',border:0,color:'white',cursor:'pointer'}}>✕</button>
          </div>
        )}
        <div className="page">
          {currentDestination==='DASHBOARD' && (
            <DashboardScreen kpis={kpis} employees={employees} shifts={shifts} pendingRequests={timeOffRequests.filter(r=>r.status==='PENDIENTE')} clockEntries={clockEntries} pendingClockCount={store.getPendingCount(currentCompanyCode)} isOnline={isOnline} loggedInEmployee={loggedInEmployee} onRegisterClock={registerClock} onTriggerSync={triggerSync} onApprove={id=>updateTimeOffStatus(id,'APROBADO')} onReject={id=>updateTimeOffStatus(id,'RECHAZADO')} onNavigate={setCurrentDestination} />
          )}
          {currentDestination==='PERSONNEL' && (
            <PersonnelScreen employees={employees} canManage={currentPermissions.canManageEmployees} onAdd={handleAddEmployee} onUpdate={handleUpdateEmployee} onDelete={handleDeleteEmployee} />
          )}
          {currentDestination==='SHIFTS' && (
            <ShiftsScreen shifts={shifts} employees={employees} canAssign={currentPermissions.canAssignShifts} onAdd={s=>{store.addShift(s); refreshData(); setStatusMessage(`🗓️ Turno asignado a ${s.employeeName}`)}} onUpdate={s=>{store.updateShift(s); refreshData()}} onDelete={id=>{store.deleteShift(id); refreshData()}} />
          )}
          {currentDestination==='TIMEOFF' && (
            <TimeOffScreen requests={timeOffRequests} employees={employees} loggedInEmployee={loggedInEmployee} canApprove={currentPermissions.canApproveTimeOff} onApprove={id=>updateTimeOffStatus(id,'APROBADO')} onReject={id=>updateTimeOffStatus(id,'RECHAZADO')} onRequest={r=>{store.addTimeOff(r); refreshData(); setStatusMessage(`🏖️ Solicitud enviada: ${r.daysCount} días laborables`); addNotification('Nueva solicitud','Solicitud de '+r.employeeName,'SISTEMA')}} />
          )}
          {currentDestination==='TASKS' && (
            <TasksScreen tasks={tasks} employees={employees} loggedInEmployee={loggedInEmployee} canAssign={currentPermissions.canAssignTasks} onToggle={t=>{ const next = t.status==='PENDIENTE'?'EN_PROGRESO': t.status==='EN_PROGRESO'?'COMPLETADA':'PENDIENTE'; store.updateTask({...t,status: next as TaskStatus}); refreshData()}} onAdd={t=>{store.addTask(t); refreshData(); setStatusMessage(`✅ Tarea asignada`)}} onDelete={id=>{store.deleteTask(id); refreshData()}} />
          )}
          {currentDestination==='PERFORMANCE' && (
            <PerformanceScreen reviews={reviews} employees={employees} canReview={currentPermissions.canReviewPerformance} onAdd={r=>{store.addReview(r); refreshData(); setStatusMessage('⭐ Evaluación guardada')}} onDelete={id=>{store.deleteReview(id); refreshData()}} />
          )}
          {currentDestination==='REPORTS' && (
            <ReportsScreen employees={employees} shifts={shifts} timeOffs={timeOffRequests} tasks={tasks} departmentFilter={selectedDept} />
          )}
          {currentDestination==='ADMIN' && (
            <AdminScreen currentRole={currentRole} currentUserName={loggedInEmployee.name} adminPerms={adminPerms} managerPerms={managerPerms} employeePerms={employeePerms} integrations={integrations} onRoleChange={setCurrentRole} onUpdateIntegrations={setIntegrations} onTestSlack={()=>{ console.log('Slack test',integrations); addNotification('Prueba Slack','Notificación de prueba enviada a '+integrations.slackChannel,'SLACK'); setStatusMessage('✅ Prueba Slack enviada (simulada)')}} onTestEmail={()=>{ addNotification('Correo prueba','Plantilla enviada a '+integrations.emailRecipient,'EMAIL'); setStatusMessage('✉️ Cliente correo simulado abierto'); window.open(`mailto:${integrations.emailRecipient}?subject=[S1] Prueba&body=Prueba S1 - ${new Date().toLocaleString()}`) }} />
          )}
        </div>
      </div>

      {showNotifications && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.45)',zIndex:80,display:'flex',justifyContent:'flex-end'}}>
          <div style={{width:'100%',maxWidth:420,background:'white',height:'100%',boxShadow:'-12px 0 40px rgba(0,0,0,.15)',display:'flex',flexDirection:'column'}}>
            <div style={{padding:16,borderBottom:'1px solid #E2E8F0',display:'flex',justifyContent:'space-between',alignItems:'center'}}>
              <b>Notificaciones ({notifications.length})</b>
              <div style={{display:'flex',gap:8}}>
                <button className="btn btn-ghost btn-sm" onClick={()=>{store.markAllRead(); setNotifications(store.getNotifications())}}>Marcar leídas</button>
                <button className="btn btn-ghost btn-icon" onClick={()=>setShowNotifications(false)}>✕</button>
              </div>
            </div>
            <div style={{flex:1,overflowY:'auto',padding:12,display:'grid',gap:10}}>
              {notifications.map(n=>(
                <div key={n.id} style={{padding:12,borderRadius:12,background: n.isRead?'#F8FAFC':'#EFF6FF',border:'1px solid #E2E8F0'}}>
                  <div style={{display:'flex',justifyContent:'space-between'}}><b style={{fontSize:13}}>{n.title}</b><span className="badge badge-slate">{n.channel}</span></div>
                  <div style={{fontSize:12,color:'#475569',marginTop:4}}>{n.message}</div>
                  <div style={{fontSize:11,color:'#94A3B8',marginTop:6}}>{new Date(n.timestamp).toLocaleString('es-ES')}</div>
                </div>
              ))}
            </div>
            <div style={{padding:12,borderTop:'1px solid #E2E8F0',display:'flex',gap:8}}>
              <button className="btn btn-ghost" style={{flex:1}} onClick={()=>{store.clearNotifications(); setNotifications([])}}>🗑️ Limpiar todo</button>
              <button className="btn btn-primary" style={{flex:1}} onClick={()=>setShowNotifications(false)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

      <style>{`@media(max-width:1024px){ #mobile-menu-btn{display:flex !important} }`}</style>
    </div>
  )
}
