package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.DailyTask
import com.example.data.model.Employee
import com.example.data.model.Shift
import com.example.data.model.TimeOffRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportExporter(private val context: Context) {

    private fun getReportsDir(): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun generateAndSharePdfReport(
        employees: List<Employee>,
        shifts: List<Shift>,
        timeOffs: List<TimeOffRequest>,
        tasks: List<DailyTask>,
        departmentFilter: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val filteredEmployees = if (departmentFilter.isNullOrBlank() || departmentFilter == "Todos") {
                employees
            } else {
                employees.filter { it.department.equals(departmentFilter, ignoreCase = true) }
            }

            val total = filteredEmployees.size
            val active = filteredEmployees.count { it.status.name == "ACTIVO" }
            val onVacation = filteredEmployees.count { it.status.name == "VACACIONES" }
            val onLeave = filteredEmployees.count { it.status.name == "BAJA_MEDICA" }
            val availPercent = if (total > 0) ((active.toFloat() / total) * 100).toInt() else 0

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.parseColor("#0F172A")
            }
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.parseColor("#1E3A8A")
            }
            val bodyPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                color = Color.parseColor("#334155")
            }
            val subPaint = Paint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.parseColor("#64748B")
            }

            // Top banner
            paint.color = Color.parseColor("#0F52BA")
            canvas.drawRect(0f, 0f, 595f, 50f, paint)

            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("StaffHub — Informe Ejecutivo de Gestión de Personal", 30f, 32f, paint)

            var y = 80f

            // Date and metadata
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            canvas.drawText("Fecha de generación: $today", 30f, y, subPaint)
            canvas.drawText("Filtro aplicado: ${departmentFilter ?: "Todos los departamentos"}", 350f, y, subPaint)
            y += 25f

            // KPI Summary Cards
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(30f, y, 150f, y + 55f, 8f, 8f, paint)
            canvas.drawRoundRect(165f, y, 285f, y + 55f, 8f, 8f, paint)
            canvas.drawRoundRect(300f, y, 420f, y + 55f, 8f, 8f, paint)
            canvas.drawRoundRect(435f, y, 565f, y + 55f, 8f, 8f, paint)

            val kpiTitlePaint = Paint().apply {
                textSize = 8.5f
                color = Color.parseColor("#475569")
                isAntiAlias = true
            }
            val kpiValPaint = Paint().apply {
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.parseColor("#0F172A")
                isAntiAlias = true
            }

            canvas.drawText("Total Plantilla", 40f, y + 20f, kpiTitlePaint)
            canvas.drawText("$total empleados", 40f, y + 42f, kpiValPaint)

            canvas.drawText("Disponibilidad", 175f, y + 20f, kpiTitlePaint)
            kpiValPaint.color = Color.parseColor("#0D9488")
            canvas.drawText("$availPercent %", 175f, y + 42f, kpiValPaint)

            kpiValPaint.color = Color.parseColor("#F59E0B")
            canvas.drawText("En Vacaciones", 310f, y + 20f, kpiTitlePaint)
            canvas.drawText("$onVacation pers.", 310f, y + 42f, kpiValPaint)

            kpiValPaint.color = Color.parseColor("#EF4444")
            canvas.drawText("Bajas Médicas", 445f, y + 20f, kpiTitlePaint)
            canvas.drawText("$onLeave pers.", 445f, y + 42f, kpiValPaint)

            y += 80f

            // Section: Listado de Empleados
            canvas.drawText("1. Personal y Estado Operativo", 30f, y, headerPaint)
            y += 15f

            // Table Header
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(30f, y, 565f, y + 18f, paint)
            val thPaint = Paint().apply {
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.parseColor("#1E293B")
                isAntiAlias = true
            }
            canvas.drawText("Nombre / Cargo", 35f, y + 12f, thPaint)
            canvas.drawText("Departamento", 185f, y + 12f, thPaint)
            canvas.drawText("Proyecto", 285f, y + 12f, thPaint)
            canvas.drawText("Área", 385f, y + 12f, thPaint)
            canvas.drawText("Estado", 480f, y + 12f, thPaint)
            y += 25f

            filteredEmployees.take(14).forEach { emp ->
                canvas.drawText("${emp.name} (${emp.jobTitle})", 35f, y, bodyPaint)
                canvas.drawText(emp.department, 185f, y, bodyPaint)
                canvas.drawText(emp.project, 285f, y, bodyPaint)
                canvas.drawText(emp.functionalArea, 385f, y, bodyPaint)

                val statusColor = when (emp.status.name) {
                    "ACTIVO" -> "#10B981"
                    "VACACIONES" -> "#F59E0B"
                    "BAJA_MEDICA" -> "#EF4444"
                    else -> "#64748B"
                }
                val statusPaint = Paint(bodyPaint).apply { color = Color.parseColor(statusColor); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                canvas.drawText(emp.status.label, 480f, y, statusPaint)

                // Divider
                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawLine(30f, y + 4f, 565f, y + 4f, paint)
                y += 18f
            }

            y += 10f
            if (y < 700f) {
                // Section: Turnos y Solicitudes
                canvas.drawText("2. Turnos Programados y Solicitudes de Tiempo Libre", 30f, y, headerPaint)
                y += 15f

                val recentShifts = shifts.take(5)
                recentShifts.forEach { shift ->
                    canvas.drawText("• Turno: ${shift.employeeName} — ${shift.shiftType.label} (${shift.startTime}-${shift.endTime}) | ${shift.department}", 35f, y, bodyPaint)
                    y += 14f
                }

                val recentLeaves = timeOffs.take(4)
                recentLeaves.forEach { req ->
                    canvas.drawText("• Solicitud: ${req.employeeName} — ${req.type.label} (${req.startDate} a ${req.endDate}, ${req.daysCount} d) [${req.status.label}]", 35f, y, bodyPaint)
                    y += 14f
                }
            }

            // Footer
            paint.color = Color.parseColor("#94A3B8")
            canvas.drawText("StaffHub Enterprise Workforce Management • Documento Confidencial • Generado automáticamente", 30f, 820f, subPaint)

            pdfDocument.finishPage(page)

            val fileName = "informe_personal_${System.currentTimeMillis()}.pdf"
            val file = File(getReportsDir(), fileName)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.success(uri)
        } catch (e: Exception) {
            Log.e("ReportExporter", "Error generating PDF", e)
            Result.failure(e)
        }
    }

    suspend fun generateAndShareExcelCsvReport(
        employees: List<Employee>,
        shifts: List<Shift>,
        timeOffs: List<TimeOffRequest>,
        tasks: List<DailyTask>,
        departmentFilter: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val filteredEmployees = if (departmentFilter.isNullOrBlank() || departmentFilter == "Todos") {
                employees
            } else {
                employees.filter { it.department.equals(departmentFilter, ignoreCase = true) }
            }

            val fileName = "informe_personal_${System.currentTimeMillis()}.csv"
            val file = File(getReportsDir(), fileName)

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel opens it with proper accents/characters
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write("INFORME DE GESTIÓN DE PERSONAL - STAFFHUB\n")
                    writer.write("Fecha de Generación;${LocalDate.now()}\n")
                    writer.write("Filtro Departamento;${departmentFilter ?: "Todos"}\n\n")

                    // Section 1: Empleados
                    writer.write("--- LISTADO DE PERSONAL ---\n")
                    writer.write("ID;Nombre;Email;Teléfono;Cargo;Departamento;Proyecto;Área Funcional;Rol Sistema;Estado;Fecha Ingreso\n")
                    for (emp in filteredEmployees) {
                        writer.write("${emp.id};\"${emp.name}\";\"${emp.email}\";\"${emp.phone}\";\"${emp.jobTitle}\";\"${emp.department}\";\"${emp.project}\";\"${emp.functionalArea}\";\"${emp.systemRole.label}\";\"${emp.status.label}\";\"${emp.hireDate}\"\n")
                    }
                    writer.write("\n")

                    // Section 2: Turnos
                    writer.write("--- TURNOS REGISTRADOS ---\n")
                    writer.write("ID Turno;Empleado;Departamento;Fecha;Tipo Turno;Horario;Notas\n")
                    for (shift in shifts) {
                        writer.write("${shift.id};\"${shift.employeeName}\";\"${shift.department}\";\"${shift.date}\";\"${shift.shiftType.label}\";\"${shift.startTime} - ${shift.endTime}\";\"${shift.notes}\"\n")
                    }
                    writer.write("\n")

                    // Section 3: Vacaciones y Ausencias
                    writer.write("--- VACACIONES Y AUSENCIAS ---\n")
                    writer.write("ID Solicitud;Empleado;Departamento;Tipo;Inicio;Fin;Días;Motivo;Estado;Aprobado Por\n")
                    for (req in timeOffs) {
                        writer.write("${req.id};\"${req.employeeName}\";\"${req.department}\";\"${req.type.label}\";\"${req.startDate}\";\"${req.endDate}\";${req.daysCount};\"${req.reason}\";\"${req.status.label}\";\"${req.reviewedBy ?: "N/A"}\"\n")
                    }
                    writer.write("\n")

                    // Section 4: Tareas Diarias
                    writer.write("--- TAREAS ASIGNADAS ---\n")
                    writer.write("ID Tarea;Empleado;Fecha;Título;Prioridad;Estado\n")
                    for (task in tasks) {
                        writer.write("${task.id};\"${task.employeeName}\";\"${task.date}\";\"${task.title}\";\"${task.priority.label}\";\"${task.status.label}\"\n")
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.success(uri)
        } catch (e: Exception) {
            Log.e("ReportExporter", "Error generating CSV", e)
            Result.failure(e)
        }
    }

    fun shareFile(uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}
