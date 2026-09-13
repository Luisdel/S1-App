import React from 'react'
import { DailyTask, Employee, TaskPriority, TaskStatus, TaskAssignmentScope } from '../types'

export function TasksScreen({ tasks, employees, loggedInEmployee, canAssign, onToggle, onAdd, onDelete }:{
  tasks: DailyTask[]
  employees: Employee[]
  loggedInEmployee: Employee | null
  canAssign: boolean
  onToggle: (t:DailyTask)=>void
  onAdd: (t:DailyTask)=>void
  onDelete: (id:number)=>void
}){
  const today = new Date().toISOString().slice(0,10)
  const [selectedDate, setSelectedDate] = React.useState(today)
  const [filter, setFilter] = React.useState<'TODAS'|'MIS_TAREAS'|'SECCION'|'PERSONAL'>('TODAS')
  const [showAdd, setShowAdd] = React.useState(false)
  const [form, setForm] = React.useState<Partial<DailyTask>>({ date: today, title:'', description:'', priority:'MEDIA', assignmentScope:'PERSONAL', department:'Operaciones', employeeId: loggedInEmployee?.id || employees[0]?.id })

  const dateTasks = tasks.filter(t=>t.date===selectedDate)
  const filtered = React.useMemo(()=>{
    switch(filter){
      case 'MIS_TAREAS': return dateTasks.filter(t=> (t.assignmentScope==='PERSONAL' && t.employeeId===loggedInEmployee?.id) || (t.assignmentScope==='SECCION' && t.department===loggedInEmployee?.department))
      case 'SECCION': return dateTasks.filter(t=>t.assignmentScope==='SECCION')
      case 'PERSONAL': return dateTasks.filter(t=>t.assignmentScope==='PERSONAL')
      default: return dateTasks
    }
  },[dateTasks,filter,loggedInEmployee])

  const completed = filtered.filter(t=>t.status==='COMPLETADA').length
  const progress = filtered.length ? completed/filtered.length : 1

  const handleSave = () => {
    if(!form.title){ alert('Título obligatorio'); return }
    const emp = employees.find(e=>e.id===form.employeeId) || loggedInEmployee
    onAdd({
      id:0,
      companyCode: emp?.companyCode || 'S1-CORP',
      employeeId: form.assignmentScope==='SECCION'?0: (form.employeeId||0),
      employeeName: form.assignmentScope==='SECCION'? `Sección ${form.department}` : (emp?.name||''),
      department: form.department||'Operaciones',
      assignmentScope: form.assignmentScope as TaskAssignmentScope || 'PERSONAL',
      date: form.date||today,
      title: form.title!,
      description: form.description||'',
      priority: form.priority as TaskPriority || 'MEDIA',
      status:'PENDIENTE',
      updatedAt: Date.now()
    })
    setShowAdd(false)
    setForm({ date: today, title:'', description:'', priority:'MEDIA', assignmentScope:'PERSONAL', department:'Operaciones', employeeId: loggedInEmployee?.id })
  }

  const days = React.useMemo(()=>{
    const base = new Date()
    return [
      {d: new Date(base.getTime()-86400000), label:'Ayer'},
      {d: base, label:'Hoy'},
      {d: new Date(base.getTime()+86400000), label:'Mañana'},
      {d: new Date(base.getTime()+86400000*2), label:'En 2 días'},
      {d: new Date(base.getTime()+86400000*3), label:'En 3 días'},
    ]
  },[])

  return (
    <div style={{display:'grid',gap:16}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:12}}>
        <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Tareas diarias</h2>
        {canAssign && <button className="btn btn-primary" onClick={()=>setShowAdd(true)}>+ Asignar tarea</button>}
      </div>

      <div className="card" style={{padding:16}}>
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:12,flexWrap:'wrap',gap:8}}>
          <div style={{display:'flex',gap:8,overflowX:'auto'}}>
            {days.map(({d,label})=>{
              const ds = d.toISOString().slice(0,10)
              const isSel = ds===selectedDate
              return <button key={ds} className="chip" style={{background: isSel?'#0F172A':'white',color: isSel?'white':'#334155'}} onClick={()=>setSelectedDate(ds)}>{label} • {ds}</button>
            })}
          </div>
          <div style={{fontSize:12,color:'#64748B'}}>{completed}/{filtered.length} completadas • {Math.round(progress*100)}%</div>
        </div>
        <div style={{height:8,background:'#F1F5F9',borderRadius:99,overflow:'hidden',marginBottom:16}}>
          <div style={{width:`${progress*100}%`,height:'100%',background:'#7C3AED',borderRadius:99,transition:'.3s'}}></div>
        </div>
        <div style={{display:'flex',gap:8,flexWrap:'wrap'}}>
          {(['TODAS','MIS_TAREAS','SECCION','PERSONAL'] as const).map(f=>(
            <button key={f} className={`chip ${filter===f?'active':''}`} onClick={()=>setFilter(f)}>{f==='TODAS'?'Todas': f==='MIS_TAREAS'?'Mis tareas': f==='SECCION'?'De sección':'Personales'}</button>
          ))}
        </div>
      </div>

      <div style={{display:'grid',gap:10}}>
        {filtered.length===0 && <div className="card" style={{padding:24,textAlign:'center',color:'#94A3B8'}}>No hay tareas para este filtro</div>}
        {filtered.map(t=>(
          <div key={t.id} className="card" style={{padding:14,display:'flex',gap:12,alignItems:'flex-start',borderLeft:`4px solid ${t.priority==='ALTA'?'#EF4444': t.priority==='MEDIA'?'#F59E0B':'#10B981'}`}}>
            <button onClick={()=>onToggle(t)} style={{width:28,height:28,borderRadius:99,border:`2px solid ${t.status==='COMPLETADA'?'#10B981':'#E2E8F0'}`,background: t.status==='COMPLETADA'?'#10B981':'white',color:'white',cursor:'pointer',display:'flex',alignItems:'center',justifyContent:'center'}}>{t.status==='COMPLETADA'?'✓':''}</button>
            <div style={{flex:1}}>
              <div style={{display:'flex',gap:8,alignItems:'center',flexWrap:'wrap'}}>
                <b style={{textDecoration: t.status==='COMPLETADA'?'line-through':'none'}}>{t.title}</b>
                <span className="badge" style={{background: t.priority==='ALTA'?'#FEE2E2': t.priority==='MEDIA'?'#FEF3C7':'#DCFCE7',color: t.priority==='ALTA'?'#991B1B': t.priority==='MEDIA'?'#92400E':'#166534'}}>{t.priority}</span>
                <span className="badge badge-slate">{t.assignmentScope}</span>
                <span className="badge badge-slate">{t.department}</span>
                <span style={{fontSize:11,color:'#64748B'}}>{t.employeeName}</span>
              </div>
              {t.description && <div style={{fontSize:12,color:'#64748B',marginTop:4}}>{t.description}</div>}
              <div style={{fontSize:11,color:'#94A3B8',marginTop:6}}>{t.date} • {t.status}</div>
            </div>
            {canAssign && <button className="btn btn-ghost btn-sm" onClick={()=>{if(confirm('¿Borrar tarea?')) onDelete(t.id)}}>🗑️</button>}
          </div>
        ))}
      </div>

      {showAdd && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.5)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:100,padding:16}}>
          <div className="card" style={{width:'100%',maxWidth:560,padding:20}}>
            <h3 style={{margin:'0 0 12px'}}>Asignar tarea</h3>
            <div style={{display:'grid',gap:12}}>
              <div><label style={{fontSize:12,fontWeight:600}}>Título *</label><input className="input" value={form.title||''} onChange={e=>setForm({...form,title:e.target.value})} /></div>
              <div><label style={{fontSize:12,fontWeight:600}}>Descripción</label><textarea className="textarea" value={form.description||''} onChange={e=>setForm({...form,description:e.target.value})} /></div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Fecha</label><input className="input" type="date" value={form.date} onChange={e=>setForm({...form,date:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Prioridad</label>
                  <select className="select" value={form.priority} onChange={e=>setForm({...form,priority:e.target.value as TaskPriority})}>
                    <option value="ALTA">ALTA</option><option value="MEDIA">MEDIA</option><option value="BAJA">BAJA</option>
                  </select>
                </div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Alcance</label>
                  <select className="select" value={form.assignmentScope} onChange={e=>setForm({...form,assignmentScope:e.target.value as TaskAssignmentScope})}>
                    <option value="PERSONAL">Personal</option><option value="SECCION">Sección de Personal</option>
                  </select>
                </div>
                <div><label style={{fontSize:12,fontWeight:600}}>Departamento</label><input className="input" value={form.department||''} onChange={e=>setForm({...form,department:e.target.value})} /></div>
              </div>
              {form.assignmentScope==='PERSONAL' && (
                <div><label style={{fontSize:12,fontWeight:600}}>Asignar a empleado</label>
                  <select className="select" value={form.employeeId} onChange={e=>setForm({...form,employeeId:Number(e.target.value)})}>
                    {employees.map(emp=> <option key={emp.id} value={emp.id}>{emp.name} • {emp.department}</option>)}
                  </select>
                </div>
              )}
              <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
                <button className="btn btn-ghost" onClick={()=>setShowAdd(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleSave}>Asignar</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
