import { WorkSchedulePattern, WorkSchedulePatternLabels } from '../types'

export interface DayDetail {
  date: Date
  dateStr: string
  dayOfWeek: number // 1 Mon - 7 Sun
  isWorkingDay: boolean
}

export interface ScheduleBreakdown {
  workingDaysCount: number
  legalRestDaysCount: number
  dayDetails: DayDetail[]
}

export function parseLocalDate(str: string): Date {
  const [y,m,d] = str.split('-').map(Number)
  return new Date(y, m-1, d)
}

export function toISODate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth()+1).padStart(2,'0')
  const day = String(d.getDate()).padStart(2,'0')
  return `${y}-${m}-${day}`
}

export function getDayOfWeekValue(date: Date): number {
  const js = date.getDay() // 0 Sun - 6 Sat
  return js === 0 ? 7 : js
}

export function isWorkingDay(date: Date, pattern: WorkSchedulePattern): boolean {
  const dow = getDayOfWeekValue(date)
  return WorkSchedulePatternLabels[pattern].days.includes(dow)
}

export function calculateBreakdown(startStr: string, endStr: string, pattern: WorkSchedulePattern): ScheduleBreakdown {
  const start = parseLocalDate(startStr)
  const end = parseLocalDate(endStr)
  if (end < start) return { workingDaysCount: 0, legalRestDaysCount: 0, dayDetails: [] }
  let cur = new Date(start)
  let working = 0
  let rest = 0
  const details: DayDetail[] = []
  while (cur <= end) {
    const isWork = isWorkingDay(cur, pattern)
    if (isWork) working++; else rest++
    details.push({
      date: new Date(cur),
      dateStr: toISODate(cur),
      dayOfWeek: getDayOfWeekValue(cur),
      isWorkingDay: isWork
    })
    cur.setDate(cur.getDate()+1)
  }
  return { workingDaysCount: working, legalRestDaysCount: rest, dayDetails: details }
}

export function formatDateES(iso: string): string {
  try {
    const d = parseLocalDate(iso)
    return d.toLocaleDateString('es-ES', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' })
  } catch { return iso }
}

export function dayNameFromValue(v: number): string {
  const map: Record<number,string> = {1:'Lun',2:'Mar',3:'Mié',4:'Jue',5:'Vie',6:'Sáb',7:'Dom'}
  return map[v] || ''
}
