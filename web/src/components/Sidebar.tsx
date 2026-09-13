import { NavigationDestination, SystemRole } from '../types'

const navItems: {id: NavigationDestination, label: string, icon: string, roles: SystemRole[]}[] = [
  { id: 'DASHBOARD', label: 'Panel', icon: '📊', roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { id: 'PERSONNEL', label: 'Personal', icon: '👥', roles: ['ADMIN','MANAGER'] },
  { id: 'SHIFTS', label: 'Turnos', icon: '🗓️', roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { id: 'TIMEOFF', label: 'Vacaciones', icon: '🏖️', roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { id: 'TASKS', label: 'Tareas', icon: '✅', roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { id: 'PERFORMANCE', label: 'Desempeño', icon: '⭐', roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { id: 'REPORTS', label: 'Informes', icon: '📈', roles: ['ADMIN','MANAGER'] },
  { id: 'ADMIN', label: 'Admin', icon: '🛡️', roles: ['ADMIN','MANAGER'] },
]

export function Sidebar({ current, role, onNavigate, open, onClose }:{
  current: NavigationDestination
  role: SystemRole
  onNavigate: (d: NavigationDestination)=>void
  open: boolean
  onClose: ()=>void
}){
  const filtered = navItems.filter(i=> i.roles.includes(role))
  return (
    <aside className={`sidebar ${open?'open':''}`}>
      <div style={{padding:'22px 20px',borderBottom:'1px solid #1E293B',display:'flex',alignItems:'center',gap:12}}>
        <div style={{width:42,height:42,borderRadius:14,background:'white',color:'#0F172A',display:'flex',alignItems:'center',justifyContent:'center',fontWeight:900,fontSize:18}}>S1</div>
        <div>
          <div style={{fontWeight:800,letterSpacing:'-0.02em'}}>S1 Enterprise</div>
          <div style={{fontSize:11,color:'#94A3B8'}}>Workforce Management</div>
        </div>
        <button onClick={onClose} className="btn btn-ghost btn-icon" style={{marginLeft:'auto',background:'#1E293B',color:'white'}}>✕</button>
      </div>
      <nav style={{padding:12,flex:1}}>
        {filtered.map(item=>(
          <button key={item.id} onClick={()=>{onNavigate(item.id); onClose()}} style={{
            width:'100%',display:'flex',alignItems:'center',gap:12,padding:'12px 14px',borderRadius:12,border:0,
            background: current===item.id ? 'white' : 'transparent',
            color: current===item.id ? '#0F172A' : '#CBD5E1',
            fontWeight: current===item.id ? 700 : 500,
            cursor:'pointer',textAlign:'left',marginBottom:4,transition:'.15s'
          }}>
            <span style={{fontSize:18}}>{item.icon}</span> {item.label}
          </button>
        ))}
        <div style={{marginTop:24,padding:14,background:'#1E293B',borderRadius:14}}>
          <div style={{fontSize:12,fontWeight:700,color:'#E2E8F0',marginBottom:6}}>Entorno seguro</div>
          <div style={{fontSize:11,color:'#94A3B8',lineHeight:1.5}}>Cada empresa opera aislada. Datos en <b>localStorage</b> + sync Cloud. Modo offline listo para fichaje en sótano.</div>
          <div style={{marginTop:10,display:'flex',gap:6}}>
            <span className="badge" style={{background:'#0F172A',color:'#94A3B8',border:'1px solid #334155'}}>Multi-tenant</span>
            <span className="badge" style={{background:'#0F172A',color:'#10B981',border:'1px solid #14532D'}}>Offline</span>
          </div>
        </div>
      </nav>
      <div style={{padding:14,borderTop:'1px solid #1E293B',fontSize:11,color:'#64748B'}}>
        v1.0 Web • Basado en S1 Android<br/>© {new Date().getFullYear()} S1 Corp
      </div>
    </aside>
  )
}
