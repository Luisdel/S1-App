import React from 'react'
import { Employee, SystemRole, WorkSchedulePattern } from '../types'
import { WorkSchedulePatternLabels } from '../types'

export function PersonnelScreen({ employees, canManage, onAdd, onUpdate, onDelete }:{
  employees: Employee[]
  canManage: boolean
  onAdd: (e: Employee)=>void
  onUpdate: (e: Employee)=>void
  onDelete: (id:number)=>void
}){
  const [dept, setDept] = React.useState('Todos')
  const [search, setSearch] = React.useState('')
  const [showAdd, setShowAdd] = React.useState(false)
  const [editing, setEditing] = React.useState<Employee | null>(null)

  const departments = React.useMemo(()=> ['Todos', ...Array.from(new Set(employees.map(e=>e.department)))], [employees])

  const filtered = employees.filter(e=>{
    const mDept = dept==='Todos' || e.department===dept
    const mSearch = !search || e.name.toLowerCase().includes(search.toLowerCase()) || e.jobTitle.toLowerCase().includes(search.toLowerCase()) || e.email.toLowerCase().includes(search.toLowerCase())
    return mDept && mSearch
  })

  const [form, setForm] = React.useState<Partial<Employee>>({
    name:'', email:'', phone:'', jobTitle:'', department:'Operaciones', project:'General', functionalArea:'Operaciones',
    systemRole:'EMPLOYEE', workSchedulePattern:'LUNES_A_VIERNES', status:'ACTIVO', passwordHash:'123456', companyCode:'S1-CORP'
  })

  const openAdd = () => { setForm({ name:'', email:'', phone:'', jobTitle:'Empleado', department:'Operaciones', project:'General', functionalArea:'Operaciones', systemRole:'EMPLOYEE', workSchedulePattern:'LUNES_A_VIERNES', status:'ACTIVO', passwordHash:'123456', companyCode: employees[0]?.companyCode || 'S1-CORP' }); setEditing(null); setShowAdd(true) }
  const openEdit = (e:Employee)=>{ setForm({...e}); setEditing(e); setShowAdd(true) }

  const handleSave = () => {
    if(!form.name || !form.email){ alert('Nombre y email obligatorios'); return }
    if(editing){
      onUpdate(form as Employee)
    } else {
      onAdd({ id:0, companyCode: form.companyCode || 'S1-CORP', name: form.name!, email: form.email!, phone: form.phone||'', passwordHash: form.passwordHash||'123456', isMasterAdmin:false, authProvider:'LOCAL', jobTitle: form.jobTitle||'Empleado', department: form.department||'Operaciones', project: form.project||'General', functionalArea: form.functionalArea||'Operaciones', systemRole: form.systemRole as SystemRole || 'EMPLOYEE', workSchedulePattern: form.workSchedulePattern as WorkSchedulePattern || 'LUNES_A_VIERNES', status: (form.status as any)||'ACTIVO', avatarColorHex: Math.floor(Math.random()*0xFFFFFF), hireDate: new Date().toISOString().slice(0,10), notes:'', updatedAt: Date.now() })
    }
    setShowAdd(false)
  }

  return (
    <div style={{display:'grid',gap:16}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:12}}>
        <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Personal ({filtered.length})</h2>
        <div style={{display:'flex',gap:8,alignItems:'center',flexWrap:'wrap'}}>
          <input className="input" placeholder="Buscar nombre, cargo, email..." value={search} onChange={e=>setSearch(e.target.value)} style={{minWidth:240}} />
          <select className="select" value={dept} onChange={e=>setDept(e.target.value)} style={{minWidth:160}}>
            {departments.map(d=><option key={d} value={d}>{d}</option>)}
          </select>
          {canManage && <button className="btn btn-primary" onClick={openAdd}>+ Nuevo empleado</button>}
        </div>
      </div>

      <div className="card" style={{padding:0,overflow:'hidden'}}>
        <div className="table-wrap" style={{border:0}}>
          <table>
            <thead><tr><th>Empleado</th><th>Cargo</th><th>Departamento</th><th>Patrón horario</th><th>Rol</th><th>Estado</th>{canManage && <th>Acciones</th>}</tr></thead>
            <tbody>
              {filtered.map(emp=>(
                <tr key={emp.id}>
                  <td>
                    <div style={{display:'flex',alignItems:'center',gap:10}}>
                      <div className="avatar" style={{background:`hsl(${emp.id*47%360} 70% 50%)`}}>{emp.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</div>
                      <div><div style={{fontWeight:700}}>{emp.name} {emp.isMasterAdmin && '👑'}</div><div style={{fontSize:11,color:'#64748B'}}>{emp.email} • {emp.phone}</div></div>
                    </div>
                  </td>
                  <td>{emp.jobTitle}<br/><span style={{fontSize:11,color:'#64748B'}}>{emp.project} / {emp.functionalArea}</span></td>
                  <td>{emp.department}</td>
                  <td><span className="badge badge-slate">{WorkSchedulePatternLabels[emp.workSchedulePattern].shortLabel}</span><br/><span style={{fontSize:10,color:'#64748B'}}>{WorkSchedulePatternLabels[emp.workSchedulePattern].label}</span></td>
                  <td><span className={`badge ${emp.systemRole==='ADMIN'?'badge-blue': emp.systemRole==='MANAGER'?'badge-amber':'badge-slate'}`}>{emp.systemRole}</span></td>
                  <td><span className="badge" style={{background: emp.status==='ACTIVO'?'#DCFCE7': emp.status==='VACACIONES'?'#FEF3C7': emp.status==='BAJA_MEDICA'?'#FEE2E2':'#F1F5F9',color: emp.status==='ACTIVO'?'#166534': emp.status==='VACACIONES'?'#92400E': emp.status==='BAJA_MEDICA'?'#991B1B':'#475569'}}>{emp.status}</span></td>
                  {canManage && <td>
                    <div style={{display:'flex',gap:6}}>
                      <button className="btn btn-ghost btn-sm" onClick={()=>openEdit(emp)}>✏️</button>
                      <button className="btn btn-ghost btn-sm" disabled={emp.isMasterAdmin} onClick={()=>{ if(confirm(`¿Borrar a ${emp.name}?`)) onDelete(emp.id) }}>🗑️</button>
                    </div>
                  </td>}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showAdd && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.5)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:100,padding:16}}>
          <div className="card" style={{width:'100%',maxWidth:720,maxHeight:'90vh',overflowY:'auto',padding:20}}>
            <h3 style={{margin:'0 0 16px'}}>{editing?'Editar empleado':'Nuevo empleado'}</h3>
            <div style={{display:'grid',gap:12}}>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Nombre *</label><input className="input" value={form.name||''} onChange={e=>setForm({...form,name:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Email *</label><input className="input" value={form.email||''} onChange={e=>setForm({...form,email:e.target.value})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Teléfono</label><input className="input" value={form.phone||''} onChange={e=>setForm({...form,phone:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Contraseña</label><input className="input" value={form.passwordHash||''} onChange={e=>setForm({...form,passwordHash:e.target.value})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Puesto</label><input className="input" value={form.jobTitle||''} onChange={e=>setForm({...form,jobTitle:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Departamento</label><input className="input" value={form.department||''} onChange={e=>setForm({...form,department:e.target.value})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Proyecto</label><input className="input" value={form.project||''} onChange={e=>setForm({...form,project:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Área funcional</label><input className="input" value={form.functionalArea||''} onChange={e=>setForm({...form,functionalArea:e.target.value})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Rol sistema</label>
                  <select className="select" value={form.systemRole} onChange={e=>setForm({...form,systemRole:e.target.value as SystemRole})}>
                    <option value="ADMIN">ADMIN</option><option value="MANAGER">MANAGER</option><option value="EMPLOYEE">EMPLOYEE</option>
                  </select>
                </div>
                <div><label style={{fontSize:12,fontWeight:600}}>Estado</label>
                  <select className="select" value={form.status} onChange={e=>setForm({...form,status:e.target.value as any})}>
                    <option value="ACTIVO">ACTIVO</option><option value="VACACIONES">VACACIONES</option><option value="BAJA_MEDICA">BAJA_MEDICA</option><option value="INACTIVO">INACTIVO</option>
                  </select>
                </div>
              </div>
              <div>
                <label style={{fontSize:12,fontWeight:600}}>Patrón horario (cómputo legal)</label>
                <select className="select" value={form.workSchedulePattern} onChange={e=>setForm({...form,workSchedulePattern:e.target.value as WorkSchedulePattern})}>
                  {Object.entries(WorkSchedulePatternLabels).map(([k,v])=> <option key={k} value={k}>{v.label} - {v.desc}</option>)}
                </select>
              </div>
              <div style={{display:'flex',gap:8,justifyContent:'flex-end',marginTop:8}}>
                <button className="btn btn-ghost" onClick={()=>setShowAdd(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleSave}>{editing?'Guardar':'Crear empleado'}</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
