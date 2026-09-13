import React from 'react'
import { Employee, SystemRole } from '../types'

export function TopBar({ currentRole, currentUserName, loggedInEmployee, unreadCount, onOpenNotifications, onOpenAdmin, onLogout, onRoleChange, onToggleSidebar }:{
  currentRole: SystemRole
  currentUserName: string
  loggedInEmployee: Employee | null
  unreadCount: number
  onOpenNotifications: ()=>void
  onOpenAdmin: ()=>void
  onLogout: ()=>void
  onRoleChange: (r:SystemRole)=>void
  onToggleSidebar: ()=>void
}){
  const [showRoleMenu, setShowRoleMenu] = React.useState(false)
  const isAdmin = currentRole === 'ADMIN' || loggedInEmployee?.isMasterAdmin

  return (
    <div className="topbar">
      <div style={{display:'flex',alignItems:'center',gap:12}}>
        <button className="btn btn-ghost btn-icon" onClick={onToggleSidebar} style={{display:'none'}} id="mobile-menu-btn">☰</button>
        <div style={{width:36,height:36,borderRadius:12,background:'#0F52BA',display:'flex',alignItems:'center',justifyContent:'center',color:'white',fontWeight:900}}>S1</div>
        <div>
          <div style={{fontWeight:800,fontSize:14}}>{currentUserName}</div>
          <div style={{fontSize:11,color:'#64748B'}}>{loggedInEmployee?.email} • {loggedInEmployee?.companyCode}</div>
        </div>
        {isAdmin && (
          <div style={{position:'relative',marginLeft:12}}>
            <button className="chip active" onClick={()=>setShowRoleMenu(v=>!v)}>
              <span style={{width:6,height:6,borderRadius:99,background:'#10B981',display:'inline-block'}}></span>
              Modo: {currentRole} ▾
            </button>
            {showRoleMenu && (
              <div style={{position:'absolute',top:36,left:0,background:'white',border:'1px solid #E2E8F0',borderRadius:12,boxShadow:'0 12px 30px rgba(0,0,0,.12)',minWidth:280,zIndex:50,overflow:'hidden'}}>
                {(['ADMIN','MANAGER','EMPLOYEE'] as SystemRole[]).map(r=>(
                  <button key={r} onClick={()=>{onRoleChange(r); setShowRoleMenu(false)}} style={{display:'flex',width:'100%',padding:'12px 14px',border:0,background: currentRole===r ? '#F1F5F9' : 'white',textAlign:'left',cursor:'pointer',alignItems:'center',justifyContent:'space-between'}}>
                    <span><b>{r}</b><br/><span style={{fontSize:11,color:'#64748B'}}>{r==='ADMIN'?'Control total': r==='MANAGER'?'Supervisa equipos':'Vista personal'}</span></span>
                    {currentRole===r && <span>✓</span>}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
      <div style={{display:'flex',alignItems:'center',gap:8}}>
        <button className="btn btn-ghost btn-icon" onClick={onOpenNotifications} title="Notificaciones" style={{position:'relative'}}>
          🔔
          {unreadCount>0 && <span style={{position:'absolute',top:-4,right:-4,background:'#EF4444',color:'white',fontSize:10,fontWeight:800,borderRadius:99,minWidth:18,height:18,display:'flex',alignItems:'center',justifyContent:'center',padding:'0 4px'}}>{unreadCount}</span>}
        </button>
        <button className="btn btn-ghost btn-icon" onClick={onOpenAdmin} title="Admin">⚙️</button>
        <button className="btn btn-ghost btn-icon" onClick={onLogout} title="Salir">🚪</button>
      </div>
    </div>
  )
}
