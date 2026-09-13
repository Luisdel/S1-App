import React from 'react'
import { CompanyEnvironment, Employee } from '../types'
import { store } from '../store'

type Tab = 'LOGIN' | 'CREATE'

export function LoginScreen({ companies, currentCompanyCode, employees, onSelectCompany, onLogin, onCreateCompany, onQuickLogin }:{
  companies: CompanyEnvironment[]
  currentCompanyCode: string
  employees: Employee[]
  onSelectCompany: (code:string)=>void
  onLogin: (companyCode:string,email:string,password:string, cb:(ok:boolean,msg:string)=>void)=>void
  onCreateCompany: (name:string,code:string,adminName:string,adminEmail:string,adminPassword:string,phone:string, cb:(ok:boolean,msg:string)=>void)=>void
  onQuickLogin: (emp: Employee)=>void
}){
  const [tab, setTab] = React.useState<Tab>('LOGIN')
  const [loginCompany, setLoginCompany] = React.useState(currentCompanyCode)
  const [loginEmail, setLoginEmail] = React.useState('')
  const [loginPass, setLoginPass] = React.useState('')
  const [showPass, setShowPass] = React.useState(false)

  const [cName, setCName] = React.useState('')
  const [cCode, setCCode] = React.useState('')
  const [aName, setAName] = React.useState('')
  const [aEmail, setAEmail] = React.useState('')
  const [aPass, setAPass] = React.useState('')
  const [aPhone, setAPhone] = React.useState('')

  const [error, setError] = React.useState<string|null>(null)
  const [info, setInfo] = React.useState<string|null>(null)
  const [loading, setLoading] = React.useState(false)

  React.useEffect(()=>{ if(!loginCompany) setLoginCompany(currentCompanyCode) },[currentCompanyCode])

  const handleLogin = () => {
    setError(null); setInfo(null); setLoading(true)
    onLogin(loginCompany, loginEmail, loginPass, (ok,msg)=>{
      setLoading(false)
      if(!ok) setError(msg); else setInfo(msg)
    })
  }

  const handleCreate = () => {
    setError(null); setInfo(null); setLoading(true)
    if(!cName || !cCode || !aName || !aEmail || !aPass){ setError('Rellena todos los campos obligatorios'); setLoading(false); return }
    onCreateCompany(cName,cCode,aName,aEmail,aPass,aPhone,(ok,msg)=>{
      setLoading(false)
      if(!ok) setError(msg); else { setInfo(msg); setTab('LOGIN'); setLoginCompany(cCode.toUpperCase()); setLoginEmail(aEmail) }
    })
  }

  return (
    <div className="login-shell">
      <div className="login-card">
        <div className="login-left">
          <div style={{position:'relative',zIndex:2}}>
            <div style={{width:64,height:64,borderRadius:18,background:'white',color:'#0F172A',display:'flex',alignItems:'center',justifyContent:'center',fontWeight:900,fontSize:22}}>S1</div>
            <h1 style={{fontSize:36,fontWeight:900,margin:'18px 0 8px',lineHeight:1}}>Gestión integral de personal</h1>
            <p style={{color:'#94A3B8',lineHeight:1.5}}>Turnos, control horario offline, ausencias con cómputo legal, tareas y desempeño. Entorno multi-empresa seguro y opaco.</p>

            <div style={{marginTop:24,display:'grid',gap:12}}>
              <div style={{background:'rgba(255,255,255,0.06)',border:'1px solid rgba(255,255,255,0.1)',borderRadius:14,padding:14}}>
                <div style={{fontWeight:700,marginBottom:4}}>🏢 Multi-tenant real</div>
                <div style={{fontSize:12,color:'#94A3B8'}}>Cada empresa: <code>companies/{"{code}"}</code> aislada. Sin fugas entre organizaciones.</div>
              </div>
              <div style={{background:'rgba(255,255,255,0.06)',border:'1px solid rgba(255,255,255,0.1)',borderRadius:14,padding:14}}>
                <div style={{fontWeight:700,marginBottom:4}}>📴 Offline first</div>
                <div style={{fontSize:12,color:'#94A3B8'}}>Fichaje en sótano sin cobertura. Cola WorkManager + sync diferida.</div>
              </div>
              <div style={{background:'rgba(255,255,255,0.06)',border:'1px solid rgba(255,255,255,0.1)',borderRadius:14,padding:14}}>
                <div style={{fontWeight:700,marginBottom:4}}>⚖️ Cómputo legal</div>
                <div style={{fontSize:12,color:'#94A3B8'}}>L-V, S-D-L, J-D, Rotativo. Descuento automático de descansos legales.</div>
              </div>
            </div>

            <div style={{marginTop:28,padding:12,background:'#0F172A',borderRadius:12,border:'1px solid #1E293B'}}>
              <div style={{fontSize:11,color:'#64748B',fontWeight:700,letterSpacing:'.06em'}}>CREDENCIALES DEMO</div>
              <div style={{fontSize:12,marginTop:6}}>Empresa: <b>S1-CORP</b><br/>Admin: <b>laura.martinez@s1-corp.com / 123456</b><br/>Manager: <b>carlos.santana@s1-corp.com / 123456</b></div>
            </div>
          </div>
          <div style={{position:'absolute',right:-60,top:120,width:300,height:300,background:'radial-gradient(circle,#3B82F6 0%, transparent 70%)',opacity:.25}}></div>
        </div>

        <div className="login-right">
          <div style={{display:'flex',gap:8,marginBottom:18,background:'#F1F5F9',padding:4,borderRadius:12}}>
            <button onClick={()=>{setTab('LOGIN'); setError(null); setInfo(null)}} className="btn" style={{flex:1,background: tab==='LOGIN'?'white':'transparent',boxShadow: tab==='LOGIN'?'0 2px 8px rgba(0,0,0,.06)':''}}>Acceso a Empresa</button>
            <button onClick={()=>{setTab('CREATE'); setError(null); setInfo(null)}} className="btn" style={{flex:1,background: tab==='CREATE'?'white':'transparent',boxShadow: tab==='CREATE'?'0 2px 8px rgba(0,0,0,.06)':''}}>Nueva Empresa</button>
          </div>

          {error && <div style={{background:'#FEE2E2',color:'#991B1B',padding:12,borderRadius:10,fontSize:13,marginBottom:12,display:'flex',gap:8}}><span>⚠️</span><span>{error}</span></div>}
          {info && <div style={{background:'#DCFCE7',color:'#166534',padding:12,borderRadius:10,fontSize:13,marginBottom:12,display:'flex',gap:8}}><span>✅</span><span>{info}</span></div>}

          {tab==='LOGIN' ? (
            <div style={{display:'grid',gap:14}}>
              <div>
                <label style={{fontSize:12,fontWeight:600}}>Código de Empresa</label>
                <div style={{display:'flex',gap:8,marginTop:6}}>
                  <input className="input" value={loginCompany} onChange={e=>{setLoginCompany(e.target.value.toUpperCase()); onSelectCompany(e.target.value.toUpperCase())}} placeholder="S1-CORP" />
                  <select className="select" style={{maxWidth:160}} value={loginCompany} onChange={e=>{setLoginCompany(e.target.value); onSelectCompany(e.target.value)}}>
                    {companies.map(c=><option key={c.code} value={c.code}>{c.code}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label style={{fontSize:12,fontWeight:600}}>Email corporativo</label>
                <input className="input" style={{marginTop:6}} value={loginEmail} onChange={e=>setLoginEmail(e.target.value)} placeholder="tu@email.com" />
              </div>
              <div>
                <label style={{fontSize:12,fontWeight:600}}>Contraseña</label>
                <div style={{display:'flex',gap:8,marginTop:6}}>
                  <input className="input" type={showPass?'text':'password'} value={loginPass} onChange={e=>setLoginPass(e.target.value)} placeholder="••••••" />
                  <button className="btn btn-ghost" onClick={()=>setShowPass(v=>!v)}>{showPass?'🙈':'👁️'}</button>
                </div>
              </div>
              <button className="btn btn-primary" disabled={loading} onClick={handleLogin} style={{marginTop:4,padding:12}}>{loading?'Accediendo...':'🔐 Iniciar sesión'}</button>

              <div className="divider"></div>
              <div>
                <div style={{fontSize:12,fontWeight:700,marginBottom:8}}>Acceso rápido (demo)</div>
                <div style={{display:'grid',gap:8,maxHeight:220,overflowY:'auto'}}>
                  {employees.slice(0,8).map(emp=>(
                    <button key={emp.id} onClick={()=>onQuickLogin(emp)} style={{display:'flex',alignItems:'center',gap:10,padding:'10px 12px',border:'1px solid #E2E8F0',borderRadius:12,background:'white',cursor:'pointer',textAlign:'left'}}>
                      <div className="avatar" style={{background: `hsl(${emp.id*47 % 360} 70% 50%)`}}>{emp.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</div>
                      <div style={{flex:1}}>
                        <div style={{fontWeight:600,fontSize:13}}>{emp.name}</div>
                        <div style={{fontSize:11,color:'#64748B'}}>{emp.jobTitle} • {emp.systemRole}</div>
                      </div>
                      <span className="badge" style={{background: emp.status==='ACTIVO'?'#DCFCE7':'#FEF3C7',color: emp.status==='ACTIVO'?'#15803D':'#B45309'}}>{emp.status}</span>
                    </button>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <div style={{display:'grid',gap:12}}>
              <div style={{background:'#EFF6FF',border:'1px solid #DBEAFE',padding:12,borderRadius:12,fontSize:12,color:'#1E40AF'}}>
                Crea un entorno privado y opaco para tu organización. Serás Administrador Maestro (no borrable).
              </div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Nombre empresa *</label><input className="input" style={{marginTop:6}} value={cName} onChange={e=>setCName(e.target.value)} placeholder="Acme SL" /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Código empresa * (mayúsculas)</label><input className="input" style={{marginTop:6}} value={cCode} onChange={e=>setCCode(e.target.value.toUpperCase())} placeholder="ACME-CORP" /></div>
              </div>
              <div><label style={{fontSize:12,fontWeight:600}}>Tu nombre completo *</label><input className="input" style={{marginTop:6}} value={aName} onChange={e=>setAName(e.target.value)} placeholder="Nombre Apellidos" /></div>
              <div className="grid2">
                <div><label style={{fontSize:12,fontWeight:600}}>Email admin *</label><input className="input" style={{marginTop:6}} value={aEmail} onChange={e=>setAEmail(e.target.value)} placeholder="admin@acme.com" /></div>
                <div><label style={{fontSize:12,fontWeight:600}}>Teléfono</label><input className="input" style={{marginTop:6}} value={aPhone} onChange={e=>setAPhone(e.target.value)} placeholder="612..." /></div>
              </div>
              <div><label style={{fontSize:12,fontWeight:600}}>Contraseña admin *</label><input className="input" style={{marginTop:6}} type="password" value={aPass} onChange={e=>setAPass(e.target.value)} placeholder="Mín 6 caracteres" /></div>
              <button className="btn btn-primary" onClick={handleCreate} disabled={loading} style={{padding:12,marginTop:4}}>{loading?'Creando...':'🏢 Crear entorno empresarial'}</button>
              <div style={{fontSize:11,color:'#64748B',textAlign:'center'}}>Se creará en localStorage y quedará aislado. Podrás añadir empleados después.</div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
