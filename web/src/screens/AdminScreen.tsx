import React from 'react'
import { RolePermissions, SystemRole, IntegrationsConfig } from '../types'

export function AdminScreen({ currentRole, currentUserName, adminPerms, managerPerms, employeePerms, integrations, onRoleChange, onUpdateIntegrations, onTestSlack, onTestEmail }:{
  currentRole: SystemRole
  currentUserName: string
  adminPerms: RolePermissions
  managerPerms: RolePermissions
  employeePerms: RolePermissions
  integrations: IntegrationsConfig
  onRoleChange: (r:SystemRole)=>void
  onUpdateIntegrations: (c:IntegrationsConfig)=>void
  onTestSlack: ()=>void
  onTestEmail: ()=>void
}){
  const [slackUrl, setSlackUrl] = React.useState(integrations.slackWebhookUrl)
  const [slackChannel, setSlackChannel] = React.useState(integrations.slackChannel)
  const [emailRecipient, setEmailRecipient] = React.useState(integrations.emailRecipient)
  const [autoTimeOff, setAutoTimeOff] = React.useState(integrations.autoNotifyOnTimeOff)
  const [autoApproval, setAutoApproval] = React.useState(integrations.autoNotifyOnApproval)

  const save = () => {
    onUpdateIntegrations({ slackWebhookUrl: slackUrl, slackChannel, emailRecipient, autoNotifyOnTimeOff: autoTimeOff, autoNotifyOnApproval: autoApproval })
    alert('Configuración guardada')
  }

  const PermRow = ({ label, admin, manager, emp }:{label:string, admin:boolean, manager:boolean, emp:boolean}) => (
    <tr><td>{label}</td><td style={{textAlign:'center'}}>{admin?'✅':'❌'}</td><td style={{textAlign:'center'}}>{manager?'✅':'❌'}</td><td style={{textAlign:'center'}}>{emp?'✅':'❌'}</td></tr>
  )

  return (
    <div style={{display:'grid',gap:16}}>
      <div className="card-dark" style={{padding:20}}>
        <div style={{display:'flex',alignItems:'center',gap:12}}>
          <div style={{width:36,height:36,borderRadius:10,background:'#3B82F6',display:'flex',alignItems:'center',justifyContent:'center'}}>🛡️</div>
          <div>
            <div style={{fontWeight:800}}>Panel de Administración y Seguridad</div>
            <div style={{fontSize:12,color:'#94A3B8'}}>Control de Acceso Basado en Roles (RBAC) y Conexiones</div>
          </div>
        </div>
        <div style={{marginTop:16}}>
          <div style={{fontWeight:600}}>Sesión Activa: {currentUserName} • Rol actual: {currentRole}</div>
          <div style={{fontSize:12,color:'#94A3B8',marginTop:4}}>Cambia de rol para verificar permisos (solo ADMIN puede cambiar en producción)</div>
          <div style={{display:'flex',gap:8,marginTop:10}}>
            {(['ADMIN','MANAGER','EMPLOYEE'] as SystemRole[]).map(r=>(
              <button key={r} className={`chip ${currentRole===r?'active':''}`} onClick={()=>onRoleChange(r)}>{r} {currentRole===r?'✓':''}</button>
            ))}
          </div>
        </div>
      </div>

      <div className="card" style={{padding:18}}>
        <h3 style={{margin:'0 0 12px'}}>Matriz de permisos por rol</h3>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Permiso</th><th>ADMIN</th><th>MANAGER</th><th>EMPLOYEE</th></tr></thead>
            <tbody>
              <PermRow label="Gestionar empleados" admin={adminPerms.canManageEmployees} manager={managerPerms.canManageEmployees} emp={employeePerms.canManageEmployees} />
              <PermRow label="Asignar turnos" admin={adminPerms.canAssignShifts} manager={managerPerms.canAssignShifts} emp={employeePerms.canAssignShifts} />
              <PermRow label="Aprobar vacaciones" admin={adminPerms.canApproveTimeOff} manager={managerPerms.canApproveTimeOff} emp={employeePerms.canApproveTimeOff} />
              <PermRow label="Asignar tareas" admin={adminPerms.canAssignTasks} manager={managerPerms.canAssignTasks} emp={employeePerms.canAssignTasks} />
              <PermRow label="Evaluar desempeño" admin={adminPerms.canReviewPerformance} manager={managerPerms.canReviewPerformance} emp={employeePerms.canReviewPerformance} />
              <PermRow label="Exportar informes" admin={adminPerms.canExportReports} manager={managerPerms.canExportReports} emp={employeePerms.canExportReports} />
              <PermRow label="Configurar sistema" admin={adminPerms.canConfigSystem} manager={managerPerms.canConfigSystem} emp={employeePerms.canConfigSystem} />
            </tbody>
          </table>
        </div>
        <div style={{fontSize:11,color:'#64748B',marginTop:8}}>En la app Android estos permisos son MutableStateFlow editables. Aquí se muestran como referencia RBAC.</div>
      </div>

      <div className="card" style={{padding:18}}>
        <h3 style={{margin:'0 0 12px'}}>Integraciones: Slack & Email</h3>
        <div style={{display:'grid',gap:12}}>
          <div><label style={{fontSize:12,fontWeight:600}}>Slack Webhook URL</label><input className="input" value={slackUrl} onChange={e=>setSlackUrl(e.target.value)} placeholder="https://hooks.slack.com/services/..." /></div>
          <div className="grid2">
            <div><label style={{fontSize:12,fontWeight:600}}>Canal Slack</label><input className="input" value={slackChannel} onChange={e=>setSlackChannel(e.target.value)} /></div>
            <div><label style={{fontSize:12,fontWeight:600}}>Email RRHH</label><input className="input" value={emailRecipient} onChange={e=>setEmailRecipient(e.target.value)} /></div>
          </div>
          <div style={{display:'flex',gap:16}}>
            <label style={{display:'flex',alignItems:'center',gap:6,fontSize:13}}><input type="checkbox" checked={autoTimeOff} onChange={e=>setAutoTimeOff(e.target.checked)} /> Auto-notificar solicitudes</label>
            <label style={{display:'flex',alignItems:'center',gap:6,fontSize:13}}><input type="checkbox" checked={autoApproval} onChange={e=>setAutoApproval(e.target.checked)} /> Auto-notificar aprobaciones</label>
          </div>
          <div style={{display:'flex',gap:8}}>
            <button className="btn btn-primary" onClick={save}>💾 Guardar configuración</button>
            <button className="btn btn-ghost" onClick={onTestSlack}>🧪 Probar Slack</button>
            <button className="btn btn-ghost" onClick={onTestEmail}>✉️ Probar Email</button>
          </div>
          <div style={{background:'#F8FAFC',padding:12,borderRadius:10,fontSize:12,color:'#475569'}}>
            <b>Cómo funciona en Android:</b> OkHttp envía JSON a webhook. Email abre cliente con plantilla. Aquí se simula con notificación local y log en consola. Integración real usaría Firebase Cloud Functions para evitar exponer webhook en cliente.
          </div>
        </div>
      </div>

      <div className="card" style={{padding:18}}>
        <h3 style={{margin:'0 0 8px'}}>Firestore Rules & Multi-tenancy</h3>
        <pre style={{background:'#0F172A',color:'#E2E8F0',padding:12,borderRadius:10,fontSize:11,overflowX:'auto'}}>
{`rules_version = '2';
match /companies/{companyCode} {
  // Solo usuarios con custom claim companyCode
  allow read: if hasCompanyClaim(companyCode)
  // Delta sync requiere updatedAt
  match /employees/{id} { allow read: if belongsToCompany(companyCode) }
  match /shifts/{id} { allow read: if belongsToCompany(companyCode) }
}`}
        </pre>
        <div style={{fontSize:11,color:'#64748B'}}>Las reglas reales están en firestore.rules del repo Android. Esta web usa localStorage aislado por companyCode simulando opacidad total.</div>
      </div>
    </div>
  )
}
