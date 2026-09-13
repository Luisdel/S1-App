import React from 'react'
import { Employee, Shift, TimeOffRequest, DailyTask } from '../types'
import jsPDF from 'jspdf'

export function ReportsScreen({ employees, shifts, timeOffs, tasks, departmentFilter }:{
  employees: Employee[]
  shifts: Shift[]
  timeOffs: TimeOffRequest[]
  tasks: DailyTask[]
  departmentFilter: string
}){
  const filteredEmployees = departmentFilter==='Todos' ? employees : employees.filter(e=>e.department===departmentFilter)

  const exportPDF = () => {
    const doc = new jsPDF()
    doc.setFillColor(15,82,186)
    doc.rect(0,0,210,20,'F')
    doc.setTextColor(255,255,255)
    doc.setFontSize(14)
    doc.setFont('helvetica','bold')
    doc.text('S1 - Informe Ejecutivo de Gestion de Personal', 14, 13)
    doc.setTextColor(0,0,0)
    doc.setFontSize(10)
    doc.setFont('helvetica','normal')
    let y=30
    doc.text(`Fecha: ${new Date().toLocaleDateString('es-ES')} - Filtro: ${departmentFilter}`,14,y); y+=8
    doc.setFont('helvetica','bold'); doc.text(`KPIs: Total ${filteredEmployees.length} | Activos ${filteredEmployees.filter(e=>e.status==='ACTIVO').length} | Vacaciones ${filteredEmployees.filter(e=>e.status==='VACACIONES').length} | Bajas ${filteredEmployees.filter(e=>e.status==='BAJA_MEDICA').length}`,14,y); y+=10
    doc.setFont('helvetica','bold'); doc.text('1. Personal y Estado Operativo',14,y); y+=6
    doc.setFont('helvetica','normal'); doc.setFontSize(8)
    filteredEmployees.slice(0,20).forEach(emp=>{
      if(y>270){ doc.addPage(); y=20 }
      doc.text(`${emp.name} (${emp.jobTitle}) - ${emp.department} - ${emp.status}`,14,y); y+=5
    })
    y+=6
    if(y<250){
      doc.setFont('helvetica','bold'); doc.setFontSize(10); doc.text('2. Turnos y Solicitudes',14,y); y+=6
      doc.setFont('helvetica','normal'); doc.setFontSize(8)
      shifts.slice(0,5).forEach(s=>{ if(y>270){doc.addPage(); y=20} doc.text(`Turno: ${s.employeeName} - ${s.shiftType} ${s.startTime}-${s.endTime} ${s.department}`,14,y); y+=5 })
      timeOffs.slice(0,5).forEach(r=>{ if(y>270){doc.addPage(); y=20} doc.text(`Solicitud: ${r.employeeName} - ${r.type} ${r.startDate} a ${r.endDate} ${r.daysCount}d [${r.status}]`,14,y); y+=5 })
    }
    doc.setFontSize(7); doc.setTextColor(100,100,100); doc.text('S1 Enterprise Workforce Management - Documento Confidencial',14,285)
    doc.save(`informe_personal_${Date.now()}.pdf`)
  }

  const exportCSV = () => {
    const headers = ['Nombre','Email','Departamento','Proyecto','Area','Cargo','Rol','Estado','Patron','FechaAlta']
    const rows = filteredEmployees.map(e=> [e.name,e.email,e.department,e.project,e.functionalArea,e.jobTitle,e.systemRole,e.status,e.workSchedulePattern,e.hireDate].map(v=>`"${String(v).replace(/"/g,'""')}"`).join(','))
    const csv = '\uFEFF' + [headers.join(','),...rows].join('\n')
    const blob = new Blob([csv],{type:'text/csv;charset=utf-8;'})
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href=url; a.download=`informe_personal_${Date.now()}.csv`; a.click(); URL.revokeObjectURL(url)
  }

  const total = filteredEmployees.length
  const active = filteredEmployees.filter(e=>e.status==='ACTIVO').length
  const vac = filteredEmployees.filter(e=>e.status==='VACACIONES').length
  const baja = filteredEmployees.filter(e=>e.status==='BAJA_MEDICA').length
  const avail = total ? Math.round(active/total*100) : 0

  return (
    <div style={{display:'grid',gap:16}}>
      <h2 style={{margin:0,fontSize:22,fontWeight:800}}>Informes y exportación</h2>

      <div className="kpi-grid">
        <div className="card" style={{padding:16}}><div style={{fontSize:11,color:'#64748B'}}>TOTAL PLANTILLA</div><div style={{fontSize:22,fontWeight:800}}>{total} empleados</div></div>
        <div className="card" style={{padding:16}}><div style={{fontSize:11,color:'#64748B'}}>DISPONIBILIDAD</div><div style={{fontSize:22,fontWeight:800,color:'#0D9488'}}>{avail}%</div></div>
        <div className="card" style={{padding:16}}><div style={{fontSize:11,color:'#64748B'}}>EN VACACIONES</div><div style={{fontSize:22,fontWeight:800,color:'#D97706'}}>{vac} pers.</div></div>
        <div className="card" style={{padding:16}}><div style={{fontSize:11,color:'#64748B'}}>BAJAS MÉDICAS</div><div style={{fontSize:22,fontWeight:800,color:'#DC2626'}}>{baja} pers.</div></div>
      </div>

      <div className="grid2">
        <div className="card" style={{padding:18}}>
          <h3 style={{margin:'0 0 8px'}}>Exportar informe</h3>
          <p style={{fontSize:12,color:'#64748B'}}>Genera PDF ejecutivo con KPIs, listado personal, turnos y solicitudes. O CSV compatible Excel con BOM UTF-8 para acentos.</p>
          <div style={{display:'flex',gap:10,marginTop:14}}>
            <button className="btn btn-dark" onClick={exportPDF}>📄 Exportar PDF</button>
            <button className="btn btn-ghost" onClick={exportCSV}>📊 Exportar Excel/CSV</button>
          </div>
          <div style={{marginTop:14,background:'#F8FAFC',padding:12,borderRadius:10,fontSize:12}}>
            <div><b>Filtro activo:</b> {departmentFilter}</div>
            <div><b>Turnos:</b> {shifts.length} • <b>Solicitudes:</b> {timeOffs.length} • <b>Tareas:</b> {tasks.length}</div>
          </div>
        </div>
        <div className="card" style={{padding:18}}>
          <h3 style={{margin:'0 0 8px'}}>Vista previa datos</h3>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Nombre</th><th>Depto</th><th>Estado</th></tr></thead>
              <tbody>
                {filteredEmployees.slice(0,8).map(e=> <tr key={e.id}><td>{e.name}</td><td>{e.department}</td><td><span className="badge" style={{background: e.status==='ACTIVO'?'#DCFCE7':'#FEF3C7',color: e.status==='ACTIVO'?'#166534':'#92400E'}}>{e.status}</span></td></tr>)}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
