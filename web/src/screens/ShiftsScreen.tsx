import React from 'react'
import { Shift, Employee, ShiftType } from '../types'
import { ShiftTypeLabels } from '../types'

export function ShiftsScreen({ shifts, employees, canAssign, onAdd, onUpdate, onDelete }:{
  shifts: Shift[]
  employees: Employee[]
  canAssign: boolean
  onAdd: (s:Shift)=>void
  onUpdate: (s:Shift)=>void
  onDelete: (id:number)=>void
}){
  const [selectedDate, setSelectedDate] = React.useState(new Date().toISOString().slice(0,10))
  const [showAdd, setShowAdd] = React.useState(false)
  const [form, setForm] = React.useState<Partial<Shift>>({ date: selectedDate, shiftType:'MANANA', startTime:'08:00', endTime:'16:00', department:'Operaciones', employeeId: employees[0]?.id, employeeName: employees[0]?.name })

  React.useEffect(()=>{ setForm(f=>({...f,date:selectedDate})) },[selectedDate])

  const filtered = shifts.filter(s=> s.date===selectedDate)
  const days = React.useMemo(()=>{
    const base = new Date()
    return Array.from({length:7},(_,i)=>{ const d=new Date(base); d.setDate(base.getDate()+i-2); return d.toISOString().slice(0,10) })
  },[])

  const handleSave = () => {
    if(!form.employeeId || !form.date) { alert('Falta empleado o fecha'); return }
    const emp = employees.find(e=>e.id===form.employeeId)
    if(!emp) return
    onAdd({ id:0, companyCode: emp.companyCode, employeeId: emp.id, employeeName: emp.name, department: form.department||emp.department, date: form.date!, shiftType: form.shiftType as ShiftType || 'MANANA', startTime: form.startTime||'08:00', endTime: form.endTime||'16:00', notes: form.notes||'', updatedAt: Date.now() })
    setShowAdd(false)
  }

  return (
    <div style={{display:'grid',gap:16}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:12}}>
        <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Turnos y cuadrantes</h2>
        {canAssign && <button className="btn btn-primary" onClick={()=>setShowAdd(true)}>+ Asignar turno</button>}
      </div>

      <div style={{display:'flex',gap:8,overflowX:'auto',paddingBottom:4}}>
        {days.map(d=>{
          const isSel = d===selectedDate
          const dayShifts = shifts.filter(s=>s.date===d).length
          return <button key={d} onClick={()=>setSelectedDate(d)} className="chip" style={{background: isSel?'#0F172A':'white',color: isSel?'white':'#334155',borderColor: isSel?'#0F172A':'#E2E8F0',padding:'10px 14px',flexDirection:'column',alignItems:'flex-start'}}>
            <span style={{fontWeight:700,fontSize:12}}>{new Date(d).toLocaleDateString('es-ES',{weekday:'short',day:'2-digit',month:'short'})}</span>
            <span style={{fontSize:11,opacity:.8}}>{dayShifts} turnos</span>
          </button>
        })}
      </div>

      <div className="kpi-grid">
        {Object.entries(ShiftTypeLabels).map(([k,v])=>{
          const count = filtered.filter(s=>s.shiftType===k).length
          return <div key={k} className="card" style={{padding:14,borderLeft:`4px solid ${v.color}`}}><div style={{fontSize:12,color:'#64748B'}}>{v.label}</div><div style={{fontWeight:800,fontSize:18}}>{count} • {v.hours}</div></div>
        })}
      </div>

      <div className="card" style={{padding:0,overflow:'hidden'}}>
        <div className="table-wrap" style={{border:0}}>
          <table>
            <thead><tr><th>Empleado</th><th>Turno</th><th>Horario</th><th>Depto</th><th>Notas</th>{canAssign && <th></th>}</tr></thead>
            <tbody>
              {filtered.length===0 && <tr><td colSpan={6} style={{textAlign:'center',padding:24,color:'#94A3B8'}}>No hay turnos para {selectedDate}</td></tr>}
              {filtered.map(s=>{
                const label = ShiftTypeLabels[s.shiftType]
                return <tr key={s.id}>
                  <td><b>{s.employeeName}</b></td>
                  <td><span className="badge" style={{background:label.color+'22',color:label.color,border:`1px solid ${label.color}44`}}>{label.label}</span></td>
                  <td>{s.startTime} - {s.endTime}</td>
                  <td>{s.department}</td>
                  <td style={{fontSize:12,color:'#64748B'}}>{s.notes}</td>
                  {canAssign && <td><button className="btn btn-ghost btn-sm" onClick={()=>{if(confirm('¿Borrar turno?')) onDelete(s.id)}}>🗑️</button></td>}
                </tr>
              })}
            </tbody>
          </table>
        </div>
      </div>

      {showAdd && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.5)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:100,padding:16}}>
          <div className="card" style={{width:'100%',maxWidth:560,padding:20}}>
            <h3 style={{margin:'0 0 12px'}}>Asignar turno</h3>
            <div style={{display:'grid',gap:12}}>
              <div><label style={{fontSize:12,fontWeight:600}}>Fecha</label><input className="input" type="date" value={form.date} onChange={e=>setForm({...form,date:e.target.value})} /></div>
              <div><label style={{fontSize:12,fontWeight:600}}>Empleado</label>
                <select className="select" value={form.employeeId} onChange={e=>{ const emp=employees.find(x=>x.id===Number(e.target.value)); setForm({...form,employeeId:Number(e.target.value),employeeName:emp?.name,department:emp?.department}) }}>
                  {employees.map(emp=> <option key={emp.id} value={emp.id}>{emp.name} • {emp.department}</option>)}
                </select>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Tipo turno</label>
                  <select className="select" value={form.shiftType} onChange={e=>{
                    const t=e.target.value as ShiftType
                    const hours = ShiftTypeLabels[t].hours.split(' - ')
                    setForm({...form,shiftType:t,startTime:hours[0]?.split(' ')[0]||'08:00',endTime:hours[1]?.split(' ')[0]||'16:00'})
                  }}>
                    {Object.entries(ShiftTypeLabels).map(([k,v])=> <option key={k} value={k}>{v.label} - {v.hours}</option>)}
                  </select>
                </div>
                <div><label style={{fontSize:12,fontWeight:600}}>Departamento</label><input className="input" value={form.department||''} onChange={e=>setForm({...form,department:e.target.value})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Inicio</label><input className="input" type="time" value={form.startTime} onChange={e=>setForm({...form,startTime:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Fin</label><input className="input" type="time" value={form.endTime} onChange={e=>setForm({...form,endTime:e.target.value})} /></div>
              </div>
              <div><label style={{fontSize:12,fontWeight:600}}>Notas</label><textarea className="textarea" value={form.notes||''} onChange={e=>setForm({...form,notes:e.target.value})} placeholder="Instrucciones especiales..." /></div>
              <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
                <button className="btn btn-ghost" onClick={()=>setShowAdd(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleSave}>Asignar turno</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
