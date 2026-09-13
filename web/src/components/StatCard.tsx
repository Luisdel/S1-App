import React from 'react'

export function StatCard({ title, value, subtitle, icon, color, bg }: { title: string, value: string, subtitle: string, icon: string, color: string, bg: string }){
  return (
    <div className="card stat-card">
      <div className="stat-icon" style={{background:bg,color}}>{icon}</div>
      <div style={{flex:1}}>
        <div className="stat-title">{title}</div>
        <div className="stat-value">{value}</div>
        <div className="stat-sub">{subtitle}</div>
      </div>
    </div>
  )
}
