import React from 'react'
import { PerformanceReview, Employee } from '../types'

export function PerformanceScreen({ reviews, employees, canReview, onAdd, onDelete }:{
  reviews: PerformanceReview[]
  employees: Employee[]
  canReview: boolean
  onAdd: (r:PerformanceReview)=>void
  onDelete: (id:number)=>void
}){
  const [showAdd, setShowAdd] = React.useState(false)
  const [form, setForm] = React.useState<Partial<PerformanceReview>>({
    employeeId: employees[0]?.id, date: new Date().toISOString().slice(0,10), overallRating:4, punctualityRating:4, productivityRating:4, teamworkRating:4, goalsAchieved:4, totalGoals:5, feedback:'', reviewerName:''
  })

  const handleSave = () => {
    if(!form.employeeId || !form.feedback){ alert('Empleado y feedback obligatorios'); return }
    const emp = employees.find(e=>e.id===form.employeeId)
    if(!emp) return
    onAdd({
      id:0,
      companyCode: emp.companyCode,
      employeeId: emp.id,
      employeeName: emp.name,
      date: form.date||new Date().toISOString().slice(0,10),
      overallRating: Number(form.overallRating)||4,
      punctualityRating: Number(form.punctualityRating)||4,
      productivityRating: Number(form.productivityRating)||4,
      teamworkRating: Number(form.teamworkRating)||4,
      goalsAchieved: Number(form.goalsAchieved)||0,
      totalGoals: Number(form.totalGoals)||5,
      feedback: form.feedback||'',
      reviewerName: form.reviewerName||'',
      updatedAt: Date.now()
    })
    setShowAdd(false)
  }

  return (
    <div style={{display:'grid',gap:16}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
        <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Desempeño y evaluaciones</h2>
        {canReview && <button className="btn btn-primary" onClick={()=>setShowAdd(true)}>+ Nueva evaluación</button>}
      </div>

      <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(320px,1fr))',gap:16}}>
        {reviews.map(r=>(
          <div key={r.id} className="card" style={{padding:16}}>
            <div style={{display:'flex',justifyContent:'space-between'}}>
              <div><b>{r.employeeName}</b><br/><span style={{fontSize:11,color:'#64748B'}}>{r.date} • por {r.reviewerName}</span></div>
              <div style={{fontSize:20,fontWeight:800,color:'#7C3AED'}}>{r.overallRating}★</div>
            </div>
            <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:8,marginTop:12,fontSize:12}}>
              <div>Puntualidad: {r.punctualityRating}/5</div>
              <div>Productividad: {r.productivityRating}/5</div>
              <div>Trabajo equipo: {r.teamworkRating}/5</div>
              <div>Objetivos: {r.goalsAchieved}/{r.totalGoals}</div>
            </div>
            <div style={{marginTop:10,background:'#F8FAFC',padding:10,borderRadius:10,fontSize:12}}>{r.feedback}</div>
            {canReview && <button className="btn btn-ghost btn-sm" style={{marginTop:10}} onClick={()=>{if(confirm('¿Borrar evaluación?')) onDelete(r.id)}}>🗑️ Eliminar</button>}
          </div>
        ))}
        {reviews.length===0 && <div className="card" style={{padding:24,color:'#94A3B8'}}>Sin evaluaciones aún</div>}
      </div>

      {showAdd && (
        <div style={{position:'fixed',inset:0,background:'rgba(15,23,42,.5)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:100,padding:16}}>
          <div className="card" style={{width:'100%',maxWidth:600,maxHeight:'90vh',overflowY:'auto',padding:20}}>
            <h3 style={{margin:'0 0 12px'}}>Nueva evaluación</h3>
            <div style={{display:'grid',gap:12}}>
              <div><label style={{fontSize:12,fontWeight:600}}>Empleado</label>
                <select className="select" value={form.employeeId} onChange={e=>setForm({...form,employeeId:Number(e.target.value)})}>
                  {employees.map(emp=> <option key={emp.id} value={emp.id}>{emp.name}</option>)}
                </select>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Fecha</label><input className="input" type="date" value={form.date} onChange={e=>setForm({...form,date:e.target.value})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Evaluador</label><input className="input" value={form.reviewerName||''} onChange={e=>setForm({...form,reviewerName:e.target.value})} placeholder="Nombre revisor" /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Rating general (1-5)</label><input className="input" type="number" min={1} max={5} step={0.1} value={form.overallRating} onChange={e=>setForm({...form,overallRating:Number(e.target.value)})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Puntualidad</label><input className="input" type="number" min={1} max={5} step={0.1} value={form.punctualityRating} onChange={e=>setForm({...form,punctualityRating:Number(e.target.value)})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Productividad</label><input className="input" type="number" min={1} max={5} step={0.1} value={form.productivityRating} onChange={e=>setForm({...form,productivityRating:Number(e.target.value)})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Trabajo en equipo</label><input className="input" type="number" min={1} max={5} step={0.1} value={form.teamworkRating} onChange={e=>setForm({...form,teamworkRating:Number(e.target.value)})} /></div>
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Objetivos logrados</label><input className="input" type="number" value={form.goalsAchieved} onChange={e=>setForm({...form,goalsAchieved:Number(e.target.value)})} /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Total objetivos</label><input className="input" type="number" value={form.totalGoals} onChange={e=>setForm({...form,totalGoals:Number(e.target.value)})} /></div>
              </div>
              <div><label style={{fontSize:12,fontWeight:600}}>Feedback</label><textarea className="textarea" value={form.feedback||''} onChange={e=>setForm({...form,feedback:e.target.value})} /></div>
              <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
                <button className="btn btn-ghost" onClick={()=>setShowAdd(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleSave}>Guardar evaluación</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
