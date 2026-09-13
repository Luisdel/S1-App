import React from 'react'
import { Employee, TimeOffRequest, Shift, DailyTask, TimeClockEntry, ClockType, StaffKpis } from '../types'
import { StatCard } from '../components/StatCard'

export function DashboardScreen({ kpis, employees, shifts, pendingRequests, clockEntries, pendingClockCount, isOnline, loggedInEmployee, onRegisterClock, onTriggerSync, onApprove, onReject, onNavigate }:{
  kpis: StaffKpis
  employees: Employee[]
  shifts: Shift[]
  pendingRequests: TimeOffRequest[]
  clockEntries: TimeClockEntry[]
  pendingClockCount: number
  isOnline: boolean
  loggedInEmployee: Employee | null
  onRegisterClock: (emp: Employee, type: ClockType, loc: string)=>void
  onTriggerSync: ()=>void
  onApprove: (id:number)=>void
  onReject: (id:number)=>void
  onNavigate: (d:any)=>void
}){
  const [locTag, setLocTag] = React.useState('Sede Central - Acceso Principal')
  const todayStr = new Date().toISOString().slice(0,10)
  const todayShifts = shifts.filter(s=>s.date===todayStr)
  const todayTasks = kpis.totalTasksTodayCount
  const avail = kpis.availabilityPercentage

  return (
    <div style={{display:'grid',gap:16}}>
      {/* hero */}
      <div className="card-dark" style={{padding:22,display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:16}}>
        <div style={{flex:1,minWidth:280}}>
          <div style={{display:'flex',alignItems:'center',gap:8,marginBottom:6}}>
            <span className="hero-dot"></span>
            <span style={{fontSize:11,fontWeight:700,letterSpacing:'.08em',color:'#10B981'}}>MONITORIZACIÓN EN TIEMPO REAL</span>
          </div>
          <div style={{fontSize:22,fontWeight:800}}>Disponibilidad de Plantilla</div>
          <div style={{display:'flex',alignItems:'baseline',gap:12,marginTop:8}}>
            <span style={{fontSize:42,fontWeight:900}}>{avail}%</span>
            <span style={{fontSize:13,color:'#94A3B8'}}>{kpis.activeEmployees} de {kpis.totalEmployees} activos • {kpis.onVacationCount} vacaciones • {kpis.onLeaveCount} bajas</span>
          </div>
          <div style={{marginTop:14,height:8,background:'#1E293B',borderRadius:99,overflow:'hidden',maxWidth:400}}>
            <div style={{width:`${avail}%`,height:'100%',background:'linear-gradient(90deg,#10B981,#0D9488)',borderRadius:99}}></div>
          </div>
        </div>
        <div style={{display:'flex',gap:12,flexWrap:'wrap'}}>
          <div style={{background:'#1E293B',padding:14,borderRadius:14,minWidth:140}}>
            <div style={{fontSize:11,color:'#94A3B8'}}>TURNOS HOY</div>
            <div style={{fontSize:22,fontWeight:800}}>{kpis.shiftsTodayCount}</div>
            <div style={{fontSize:11,color:'#94A3B8'}}>{todayShifts.length} programados</div>
          </div>
          <div style={{background:'#1E293B',padding:14,borderRadius:14,minWidth:140}}>
            <div style={{fontSize:11,color:'#94A3B8'}}>TAREAS HOY</div>
            <div style={{fontSize:22,fontWeight:800}}>{kpis.completedTasksTodayCount}/{todayTasks}</div>
            <div style={{fontSize:11,color:'#94A3B8'}}>{kpis.taskCompletionRate}% completado</div>
          </div>
        </div>
      </div>

      <div className="kpi-grid">
        <StatCard title="Total Plantilla" value={`${kpis.totalEmployees}`} subtitle="Empleados registrados" icon="👥" color="#0F52BA" bg="#DBEAFE" />
        <StatCard title="Pendientes" value={`${kpis.pendingRequestsCount}`} subtitle="Solicitudes por aprobar" icon="⏳" color="#D97706" bg="#FEF3C7" />
        <StatCard title="Fichajes Offline" value={`${pendingClockCount}`} subtitle={isOnline ? 'Online - Sync OK' : 'Offline - En cola'} icon="📴" color={isOnline?'#059669':'#D97706'} bg={isOnline?'#DCFCE7':'#FEF3C7'} />
        <StatCard title="Disponibilidad" value={`${avail}%`} subtitle="Personal activo" icon="📈" color="#0D9488" bg="#CCFBF1" />
      </div>

      {/* Fichaje */}
      <div className="grid2">
        <div className="card" style={{padding:18}}>
          <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:12}}>
            <h3 style={{margin:0,fontSize:16,fontWeight:800}}>Registro de Jornada (Fichaje)</h3>
            <span className={`badge ${isOnline?'badge-green':'badge-amber'}`}>{isOnline?'ONLINE':'OFFLINE'}</span>
          </div>
          <div style={{fontSize:12,color:'#64748B',marginBottom:12}}>Fichaje offline-first. Si no hay cobertura, se guarda en local y WorkManager lo sincroniza al recuperar red. Ubicación: almacén, acceso, etc.</div>
          <div style={{display:'flex',gap:8,marginBottom:12}}>
            <input className="input" value={locTag} onChange={e=>setLocTag(e.target.value)} placeholder="Ubicación" />
            <button className="btn btn-ghost" onClick={onTriggerSync}>🔄 Sincronizar</button>
          </div>
          {loggedInEmployee && (
            <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:8}}>
              {(['ENTRADA','SALIDA','PAUSA_INICIO','PAUSA_FIN'] as ClockType[]).map(t=>(
                <button key={t} className="btn btn-dark" onClick={()=>onRegisterClock(loggedInEmployee,t,locTag)} style={{padding:12}}>
                  {t==='ENTRADA'?'🟢 Entrada' : t==='SALIDA'?'🔴 Salida' : t==='PAUSA_INICIO'?'☕ Pausa' : '▶️ Fin pausa'} - {t}
                </button>
              ))}
            </div>
          )}
          <div style={{marginTop:14,maxHeight:220,overflowY:'auto',display:'grid',gap:8}}>
            {clockEntries.slice(0,6).map(c=>(
              <div key={c.id} style={{display:'flex',justifyContent:'space-between',alignItems:'center',padding:'10px 12px',background:'#F8FAFC',borderRadius:10,border:'1px solid #F1F5F9'}}>
                <div>
                  <div style={{fontWeight:600,fontSize:13}}>{c.employeeName} • {c.clockType}</div>
                  <div style={{fontSize:11,color:'#64748B'}}>{c.formattedDate} {c.formattedTime} • {c.locationTag}</div>
                </div>
                <span className={`badge ${c.syncStatus==='SYNCED'?'badge-green': c.syncStatus==='PENDING'?'badge-amber':'badge-red'}`}>{c.syncStatus} {c.serverSyncId? `• ${c.serverSyncId.slice(0,10)}`:''}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="card" style={{padding:18}}>
          <h3 style={{margin:'0 0 12px',fontSize:16,fontWeight:800}}>Solicitudes pendientes</h3>
          {pendingRequests.length===0 ? <div style={{padding:20,textAlign:'center',color:'#94A3B8'}}>✅ No hay solicitudes pendientes</div> :
            <div style={{display:'grid',gap:10,maxHeight:380,overflowY:'auto'}}>
              {pendingRequests.map(r=>(
                <div key={r.id} style={{border:'1px solid #F1F5F9',borderRadius:12,padding:12,background:'white'}}>
                  <div style={{display:'flex',justifyContent:'space-between'}}>
                    <b style={{fontSize:13}}>{r.employeeName}</b>
                    <span className="badge badge-amber">{r.type}</span>
                  </div>
                  <div style={{fontSize:12,color:'#64748B',marginTop:4}}>{r.startDate} → {r.endDate} • {r.daysCount} días laborables (+{r.legalRestDaysCount} descanso legal)</div>
                  <div style={{fontSize:12,marginTop:6}}>{r.reason}</div>
                  <div style={{display:'flex',gap:8,marginTop:10}}>
                    <button className="btn btn-primary btn-sm" onClick={()=>onApprove(r.id)}>Aprobar</button>
                    <button className="btn btn-ghost btn-sm" onClick={()=>onReject(r.id)}>Rechazar</button>
                  </div>
                </div>
              ))}
            </div>
          }
          <button className="btn btn-ghost" style={{marginTop:12,width:'100%'}} onClick={()=>onNavigate('TIMEOFF')}>Ver todas las solicitudes →</button>
        </div>
      </div>

      <div className="grid2">
        <div className="card" style={{padding:18}}>
          <h3 style={{margin:'0 0 12px',fontSize:16,fontWeight:800}}>Turnos de hoy</h3>
          <div style={{display:'grid',gap:8}}>
            {todayShifts.length===0 && <div style={{color:'#94A3B8',fontSize:13}}>Sin turnos hoy</div>}
            {todayShifts.map(s=>(
              <div key={s.id} style={{display:'flex',alignItems:'center',gap:12,padding:10,background:'#F8FAFC',borderRadius:10}}>
                <div style={{width:8,height:36,borderRadius:99,background: s.shiftType==='MANANA'?'#3B82F6': s.shiftType==='TARDE'?'#F59E0B': s.shiftType==='NOCHE'?'#6366F1':'#10B981'}}></div>
                <div style={{flex:1}}>
                  <div style={{fontWeight:600,fontSize:13}}>{s.employeeName} • {s.shiftType}</div>
                  <div style={{fontSize:11,color:'#64748B'}}>{s.startTime}-{s.endTime} • {s.department}</div>
                </div>
                <span className="badge badge-slate">{s.date}</span>
              </div>
            ))}
          </div>
          <button className="btn btn-ghost" style={{marginTop:12,width:'100%'}} onClick={()=>onNavigate('SHIFTS')}>Gestionar turnos →</button>
        </div>
        <div className="card" style={{padding:18}}>
          <h3 style={{margin:'0 0 12px',fontSize:16,fontWeight:800}}>Plantilla rápida</h3>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Empleado</th><th>Depto</th><th>Estado</th></tr></thead>
              <tbody>
                {employees.slice(0,6).map(e=>(
                  <tr key={e.id}><td><b>{e.name}</b><br/><span style={{fontSize:11,color:'#64748B'}}>{e.jobTitle}</span></td><td>{e.department}</td><td><span className="badge" style={{background: e.status==='ACTIVO'?'#DCFCE7': e.status==='VACACIONES'?'#FEF3C7':'#FEE2E2',color: e.status==='ACTIVO'?'#166534': e.status==='VACACIONES'?'#92400E':'#991B1B'}}>{e.status}</span></td></tr>
                ))}
              </tbody>
            </table>
          </div>
          <button className="btn btn-ghost" style={{marginTop:12,width:'100%'}} onClick={()=>onNavigate('PERSONNEL')}>Ver todo el personal →</button>
        </div>
      </div>
    </div>
  )
}
