import React from 'react'
import { TimeOffRequest, Employee, TimeOffType, RequestStatus, WorkSchedulePattern } from '../types'
import { TimeOffTypeLabels, WorkSchedulePatternLabels } from '../types'
import { calculateBreakdown, formatDateES } from '../utils/schedule'

export function TimeOffScreen({ requests, employees, loggedInEmployee, canApprove, onApprove, onReject, onRequest }:{
  requests: TimeOffRequest[]
  employees: Employee[]
  loggedInEmployee: Employee | null
  canApprove: boolean
  onApprove: (id:number)=>void
  onReject: (id:number)=>void
  onRequest: (r:TimeOffRequest)=>void
}){
  const [filterStatus, setFilterStatus] = React.useState<RequestStatus | null>(null)
  const [showDialog, setShowDialog] = React.useState(false)
  const [form, setForm] = React.useState<Partial<TimeOffRequest>>({
    type:'VACACIONES', startDate: new Date().toISOString().slice(0,10), endDate: new Date().toISOString().slice(0,10), reason:'', customDayType:'', schedulePattern: loggedInEmployee?.workSchedulePattern || 'LUNES_A_VIERNES'
  })

  const breakdown = React.useMemo(()=>{
    if(!form.startDate || !form.endDate || !form.schedulePattern) return null
    return calculateBreakdown(form.startDate, form.endDate, form.schedulePattern as WorkSchedulePattern)
  },[form.startDate, form.endDate, form.schedulePattern])

  const filtered = filterStatus ? requests.filter(r=>r.status===filterStatus) : requests

  const handleSubmit = () => {
    if(!form.startDate || !form.endDate || !form.reason){ alert('Rellena fechas y motivo'); return }
    if(!loggedInEmployee){ alert('No hay empleado logueado'); return }
    if(!breakdown) return
    const newReq: TimeOffRequest = {
      id:0,
      companyCode: loggedInEmployee.companyCode,
      employeeId: loggedInEmployee.id,
      employeeName: loggedInEmployee.name,
      department: loggedInEmployee.department,
      type: form.type as TimeOffType || 'VACACIONES',
      customDayType: form.customDayType||'',
      startDate: form.startDate!,
      endDate: form.endDate!,
      daysCount: breakdown.workingDaysCount,
      legalRestDaysCount: breakdown.legalRestDaysCount,
      schedulePattern: form.schedulePattern as WorkSchedulePattern,
      reason: form.reason!,
      status:'PENDIENTE',
      requestedAt: Date.now(),
      updatedAt: Date.now()
    }
    onRequest(newReq)
    setShowDialog(false)
    setForm({ type:'VACACIONES', startDate: new Date().toISOString().slice(0,10), endDate: new Date().toISOString().slice(0,10), reason:'', customDayType:'', schedulePattern: loggedInEmployee.workSchedulePattern })
  }

  return (
    <div style={{display:'grid',gap:16}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:12}}>
        <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Vacaciones y ausencias</h2>
        <button className="btn btn-primary" onClick={()=>setShowDialog(true)}>🏖️ Solicitar tiempo libre</button>
      </div>

      <div style={{display:'flex',gap:8,overflowX:'auto'}}>
        <button className={`chip ${!filterStatus?'active':''}`} onClick={()=>setFilterStatus(null)}>Todas ({requests.length})</button>
        <button className={`chip ${filterStatus==='PENDIENTE'?'active':''}`} onClick={()=>setFilterStatus('PENDIENTE')}>Pendientes ({requests.filter(r=>r.status==='PENDIENTE').length})</button>
        <button className={`chip ${filterStatus==='APROBADO'?'active':''}`} onClick={()=>setFilterStatus('APROBADO')}>Aprobadas ({requests.filter(r=>r.status==='APROBADO').length})</button>
        <button className={`chip ${filterStatus==='RECHAZADO'?'active':''}`} onClick={()=>setFilterStatus('RECHAZADO')}>Rechazadas ({requests.filter(r=>r.status==='RECHAZADO').length})</button>
      </div>

      <div style={{display:'grid',gap:12}}>
        {filtered.length===0 && <div className="card" style={{padding:32,textAlign:'center',color:'#94A3B8'}}>No hay solicitudes</div>}
        {filtered.map(r=>{
          const isPending = r.status==='PENDIENTE'
          return (
            <div key={r.id} className="card" style={{padding:16,borderLeft:`4px solid ${isPending?'#F59E0B': r.status==='APROBADO'?'#10B981':'#EF4444'}`}}>
              <div style={{display:'flex',justifyContent:'space-between',flexWrap:'wrap',gap:8}}>
                <div>
                  <div style={{display:'flex',alignItems:'center',gap:8}}>
                    <b>{r.employeeName}</b>
                    <span className="badge badge-slate">{r.department}</span>
                    <span className={`badge ${r.status==='PENDIENTE'?'badge-amber': r.status==='APROBADO'?'badge-green':'badge-red'}`}>{r.status}</span>
                  </div>
                  <div style={{fontSize:13,marginTop:6}}><b>{TimeOffTypeLabels[r.type]}</b> {r.customDayType && `• ${r.customDayType}`} • {formatDateES(r.startDate)} → {formatDateES(r.endDate)}</div>
                  <div style={{fontSize:12,color:'#64748B',marginTop:4}}>📅 <b>{r.daysCount} días laborables</b> computables + {r.legalRestDaysCount} días descanso legal exentos • Patrón: {WorkSchedulePatternLabels[r.schedulePattern].shortLabel}</div>
                  <div style={{fontSize:12,marginTop:8,background:'#F8FAFC',padding:8,borderRadius:8}}>{r.reason}</div>
                  {r.reviewedBy && <div style={{fontSize:11,color:'#64748B',marginTop:6}}>Revisado por {r.reviewedBy} el {r.reviewedAt ? new Date(r.reviewedAt).toLocaleString('es-ES') : ''}</div>}
                </div>
                {canApprove && isPending && (
                  <div style={{display:'flex',gap:8,alignItems:'flex-start'}}>
                    <button className="btn btn-primary btn-sm" onClick={()=>onApprove(r.id)}>Aprobar</button>
                    <button className="btn btn-ghost btn-sm" onClick={()=>onReject(r.id)}>Rechazar</button>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>

      {showDialog && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.5)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:100,padding:16}}>
          <div className="card" style={{width:'100%',maxWidth:640,maxHeight:'90vh',overflowY:'auto',padding:20}}>
            <h3 style={{margin:'0 0 12px'}}>Solicitar tiempo libre</h3>
            <div style={{background:'#EFF6FF',border:'1px solid #DBEAFE',padding:12,borderRadius:12,fontSize:12,color:'#1E40AF',marginBottom:12}}>
              El sistema calcula automáticamente los días laborables según tu patrón horario y descuenta descansos legales (sáb/dom o según patrón). Ej: si pides L-V pero tu patrón es J-D, L-M-X son descanso legal exento.
            </div>
            <div style={{display:'grid',gap:12}}>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Tipo</label>
                  <select className="select" value={form.type} onChange={e=>setForm({...form,type:e.target.value as TimeOffType})}>
                    {Object.entries(TimeOffTypeLabels).map(([k,v])=> <option key={k} value={k}>{v}</option>)}
                  </select>
                </div>
                <div><label style={{fontSize:12,fontWeight:600}}>Patrón horario</label>
                  <select className="select" value={form.schedulePattern} onChange={e=>setForm({...form,schedulePattern:e.target.value as WorkSchedulePattern})}>
                    {Object.entries(WorkSchedulePatternLabels).map(([k,v])=> <option key={k} value={k}>{v.label}</option>)}
                  </select>
                </div>
              </div>
              <div><label style={{fontSize:12,fontWeight:600}}>Tipo día personalizado (opcional)</label><input className="input" value={form.customDayType||''} onChange={e=>setForm({...form,customDayType:e.target.value})} placeholder="Ej: Día por mudanza, boda, etc." /></div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Inicio</label><input className="input" type="date" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Fin</label><input className="input" type="date" value={form.endDate} onChange={e=>setForm({...form,endDate:e.target.value})} /></div>
              </div>
              {breakdown && (
                <div style={{background:'#F8FAFC',border:'1px solid #E2E8F0',borderRadius:12,padding:12}}>
                  <div style={{fontSize:12,fontWeight:700,marginBottom:6}}>Desglose legal:</div>
                  <div style={{fontSize:12}}>✅ <b>{breakdown.workingDaysCount} días laborables</b> computables a vacaciones/ausencia</div>
                  <div style={{fontSize:12}}>🛌 <b>{breakdown.legalRestDaysCount} días</b> descanso legal exentos (no computan)</div>
                  <div style={{display:'flex',flexWrap:'wrap',gap:4,marginTop:8}}>
                    {breakdown.dayDetails.map(d=>(
                      <span key={d.dateStr} className="badge" style={{background: d.isWorkingDay?'#DBEAFE':'#F1F5F9',color: d.isWorkingDay?'#1D4ED8':'#64748B',border:`1px solid ${d.isWorkingDay?'#93C5FD':'#E2E8F0'}`}}>{d.dateStr.slice(5)} {d.isWorkingDay?'💼':'🛌'}</span>
                    ))}
                  </div>
                </div>
              )}
              <div><label style={{fontSize:12,fontWeight:600}}>Motivo detallado *</label><textarea className="textarea" value={form.reason} onChange={e=>setForm({...form,reason:e.target.value})} placeholder="Explica motivo..." /></div>
              <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
                <button className="btn btn-ghost" onClick={()=>setShowDialog(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleSubmit}>Enviar solicitud</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
