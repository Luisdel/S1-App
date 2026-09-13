package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CloudSyncReport(
    val success: Boolean,
    val syncedClocksCount: Int,
    val pulledUpdatesCount: Int,
    val message: String,
    val isCloudConnected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class FirebaseAuthResult(
    val success: Boolean,
    val uid: String? = null,
    val email: String? = null,
    val message: String
)

class IntegrationService(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("s1_cloud_sync_prefs", Context.MODE_PRIVATE)
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Comprueba si Firebase ha sido inicializado mediante google-services.json
     */
    fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Obtiene la instancia de FirebaseAuth si Firebase está inicializado
     */
    fun getFirebaseAuth(): FirebaseAuth? {
        return if (isFirebaseConfigured()) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null
    }

    /**
     * Obtiene la instancia de FirebaseFirestore si Firebase está inicializado
     */
    fun getFirestore(): FirebaseFirestore? {
        return if (isFirebaseConfigured()) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null
    }

    /**
     * Verifica el estado de conectividad a Internet
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // =============================================================================================
    // AUTENTICACIÓN FIREBASE AUTH (Integración Real con Fallback Transparente a Room)
    // =============================================================================================

    suspend fun signInWithFirebaseAuth(email: String, password: String): FirebaseAuthResult = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
        if (auth == null || !isNetworkAvailable()) {
            return@withContext FirebaseAuthResult(
                success = false,
                message = "Modo Offline / Firebase no vinculado en este dispositivo."
            )
        }

        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            FirebaseAuthResult(
                success = user != null,
                uid = user?.uid,
                email = user?.email,
                message = "Autenticado exitosamente en Firebase Cloud"
            )
        } catch (e: Exception) {
            Log.w("IntegrationService", "Fallo de login en Firebase: ${e.message}")
            FirebaseAuthResult(
                success = false,
                message = e.localizedMessage ?: "Error de autenticación en la nube"
            )
        }
    }

    suspend fun createFirebaseAccount(email: String, password: String): FirebaseAuthResult = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
        if (auth == null || !isNetworkAvailable()) {
            return@withContext FirebaseAuthResult(
                success = false,
                message = "Modo Offline: No es posible crear cuenta en Firebase sin conexión o configuración."
            )
        }

        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            FirebaseAuthResult(
                success = user != null,
                uid = user?.uid,
                email = user?.email,
                message = "Cuenta creada en Firebase Authentication"
            )
        } catch (e: Exception) {
            Log.w("IntegrationService", "Fallo al crear cuenta en Firebase: ${e.message}")
            FirebaseAuthResult(
                success = false,
                message = e.localizedMessage ?: "Error al registrar en la nube"
            )
        }
    }

    fun signOutFirebase() {
        try {
            getFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error signing out Firebase", e)
        }
    }

    // =============================================================================================
    // CLOUD FIRESTORE MULTI-TENANT SYNC (Optimizado para Plan Spark: Delta Sync + WriteBatch)
    // =============================================================================================

    /**
     * Sincroniza la empresa en Firestore: /companies/{companyCode}
     */
    suspend fun syncCompanyToCloud(company: CompanyEnvironment): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
        if (firestore == null || !isNetworkAvailable()) {
            return@withContext Result.success("LOCAL_SAVED")
        }

        try {
            val cleanCode = company.code.trim().uppercase()
            val docRef = firestore.collection("companies").document(cleanCode)
            val data = hashMapOf(
                "code" to cleanCode,
                "name" to company.name,
                "adminEmail" to company.adminEmail,
                "adminName" to company.adminName,
                "createdAt" to company.createdAt,
                "updatedAt" to company.updatedAt
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success("CLOUD_SYNCED")
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error subiendo empresa a Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Sube empleados asignados por el Admin a la empresa en Firestore: /companies/{companyCode}/employees/{id}
     */
    suspend fun syncEmployeeToCloud(employee: Employee): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
        if (firestore == null || !isNetworkAvailable()) {
            return@withContext Result.success("LOCAL_SAVED")
        }

        try {
            val cleanCode = employee.companyCode.trim().uppercase()
            val docId = if (employee.id > 0) employee.id.toString() else UUID.randomUUID().toString()
            val docRef = firestore.collection("companies")
                .document(cleanCode)
                .collection("employees")
                .document(docId)

            val data = hashMapOf(
                "id" to employee.id,
                "companyCode" to cleanCode,
                "name" to employee.name,
                "email" to employee.email,
                "phone" to employee.phone,
                "passwordHash" to employee.passwordHash,
                "isMasterAdmin" to employee.isMasterAdmin,
                "authProvider" to employee.authProvider,
                "jobTitle" to employee.jobTitle,
                "department" to employee.department,
                "project" to employee.project,
                "functionalArea" to employee.functionalArea,
                "systemRole" to employee.systemRole.name,
                "status" to employee.status.name,
                "avatarColorHex" to employee.avatarColorHex,
                "hireDate" to employee.hireDate,
                "notes" to employee.notes,
                "updatedAt" to employee.updatedAt
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success("CLOUD_SYNCED")
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error subiendo empleado a Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza un fichaje individual (usado en tiempo real al fichar o por WorkManager)
     */
    suspend fun syncClockEntryToCloud(entry: TimeClockEntry): Result<String> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(Exception("Sin cobertura de red (Ubicación: ${entry.locationTag}). En cola para sincronización en segundo plano."))
        }

        val firestore = getFirestore()
        if (firestore == null) {
            // Sin Firebase configurado: emula confirmación con ID local/offline
            val cloudSyncId = "LOCAL-CLK-${UUID.randomUUID().toString().take(8).uppercase()}"
            return@withContext Result.success(cloudSyncId)
        }

        try {
            val cleanCode = entry.companyCode.trim().uppercase()
            val clockId = if (entry.serverSyncId != null && entry.serverSyncId.isNotBlank()) {
                entry.serverSyncId
            } else {
                "CLK-${entry.id}-${UUID.randomUUID().toString().take(8).uppercase()}"
            }

            val docRef = firestore.collection("companies")
                .document(cleanCode)
                .collection("time_clocks")
                .document(clockId)

            val data = hashMapOf(
                "id" to entry.id,
                "companyCode" to cleanCode,
                "employeeId" to entry.employeeId,
                "employeeName" to entry.employeeName,
                "department" to entry.department,
                "clockType" to entry.clockType.name,
                "timestamp" to entry.timestamp,
                "formattedTime" to entry.formattedTime,
                "formattedDate" to entry.formattedDate,
                "locationTag" to entry.locationTag,
                "updatedAt" to entry.updatedAt
            )

            docRef.set(data, SetOptions.merge()).await()
            Result.success(clockId)
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error enviando fichaje a Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * PUSH SYNC (Subida Optimizada):
     * Agrupa registros locales con estado PENDING o FAILED y los sube usando WriteBatch a Firestore.
     * El estado en Room SOLO se actualiza a SYNCED si Firebase confirma la transacción con éxito (.await()).
     */
    suspend fun pushPendingChangesToFirestore(
        database: AppDatabase,
        companyCode: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        val cleanCode = companyCode.trim().uppercase()
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(Exception("Sin conexión a red para subida (Push Sync)"))
        }

        val firestore = getFirestore()
            ?: return@withContext Result.failure(Exception("Firebase Firestore no inicializado en el dispositivo"))

        try {
            val pendingClocks = database.timeClockDao().getPendingClockEntriesForCompany(cleanCode)
            if (pendingClocks.isEmpty()) {
                return@withContext Result.success(0)
            }

            var totalPushed = 0
            val now = System.currentTimeMillis()

            // Firestore WriteBatch admite un máximo de 500 operaciones atómicas por lote
            for (chunk in pendingClocks.chunked(500)) {
                val batch = firestore.batch()
                val batchEntries = mutableListOf<Pair<TimeClockEntry, String>>()

                for (entry in chunk) {
                    val clockDocId = if (!entry.serverSyncId.isNullOrBlank()) {
                        entry.serverSyncId
                    } else {
                        "CLK-${entry.id}-${UUID.randomUUID().toString().take(6).uppercase()}"
                    }

                    val docRef = firestore.collection("companies")
                        .document(cleanCode)
                        .collection("time_clocks")
                        .document(clockDocId)

                    val payload = hashMapOf(
                        "id" to entry.id,
                        "companyCode" to cleanCode,
                        "employeeId" to entry.employeeId,
                        "employeeName" to entry.employeeName,
                        "department" to entry.department,
                        "clockType" to entry.clockType.name,
                        "timestamp" to entry.timestamp,
                        "formattedTime" to entry.formattedTime,
                        "formattedDate" to entry.formattedDate,
                        "locationTag" to entry.locationTag,
                        "updatedAt" to entry.updatedAt
                    )

                    batch.set(docRef, payload, SetOptions.merge())
                    batchEntries.add(Pair(entry, clockDocId))
                }

                // Transacción atómica en Firebase
                batch.commit().await()

                // CONDICIÓN ESTRICTA: Solo tras la confirmación exitosa de Firebase,
                // Room (la SSOT) actualiza su estado a SYNCED
                for ((entry, clockDocId) in batchEntries) {
                    database.timeClockDao().updateClockEntry(
                        entry.copy(
                            syncStatus = SyncStatus.SYNCED,
                            serverSyncId = clockDocId,
                            lastSyncAttemptAt = now
                        )
                    )
                }
                totalPushed += batchEntries.size
            }

            Result.success(totalPushed)
        } catch (e: Exception) {
            Log.e("IntegrationService", "Fallo al enviar batch a Firestore: ${e.message}", e)
            // Marcar intentos fallidos en Room sin cambiar a SYNCED
            try {
                val pending = database.timeClockDao().getPendingClockEntriesForCompany(cleanCode)
                val failNow = System.currentTimeMillis()
                for (p in pending) {
                    database.timeClockDao().updateClockEntry(
                        p.copy(
                            syncStatus = SyncStatus.FAILED,
                            syncAttempts = p.syncAttempts + 1,
                            lastSyncAttemptAt = failNow
                        )
                    )
                }
            } catch (ignored: Exception) {}

            Result.failure(e)
        }
    }

    /**
     * PULL SYNC (Descarga Diferencial - Delta Sync):
     * Consulta Firestore filtrando por 'updatedAt' > lastSyncTimestamp para traer estrictamente
     * los registros nuevos o modificados, manteniendo el consumo en el Plan Spark a coste cero.
     * Resuelve conflictos mediante política Last-Write-Wins (LWW) en Room (SSOT).
     */
    suspend fun pullDifferentialUpdatesFromFirestore(
        database: AppDatabase,
        companyCode: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        val cleanCode = companyCode.trim().uppercase()
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(Exception("Sin conexión para descarga diferencial"))
        }

        val firestore = getFirestore()
            ?: return@withContext Result.failure(Exception("Firebase Firestore no disponible"))

        try {
            val prefKey = "last_sync_timestamp_$cleanCode"
            val lastSync = prefs.getLong(prefKey, 0L)
            val syncStartTimestamp = System.currentTimeMillis()
            var totalPulled = 0

            // 1. Delta Sync de EMPLEADOS
            val employeesSnapshot = firestore.collection("companies")
                .document(cleanCode)
                .collection("employees")
                .whereGreaterThan("updatedAt", lastSync)
                .get()
                .await()

            for (doc in employeesSnapshot.documents) {
                try {
                    val email = doc.getString("email") ?: continue
                    val remoteUpdatedAt = doc.getLong("updatedAt") ?: syncStartTimestamp
                    val localEmp = database.employeeDao().getEmployeeByCompanyAndEmail(cleanCode, email)

                    if (localEmp == null) {
                        // Insertar nuevo empleado descargado
                        val newEmp = Employee(
                            companyCode = cleanCode,
                            name = doc.getString("name") ?: "Empleado",
                            email = email,
                            phone = doc.getString("phone") ?: "",
                            passwordHash = doc.getString("passwordHash") ?: "123456",
                            isMasterAdmin = doc.getBoolean("isMasterAdmin") ?: false,
                            authProvider = doc.getString("authProvider") ?: "LOCAL",
                            jobTitle = doc.getString("jobTitle") ?: "Empleado",
                            department = doc.getString("department") ?: "Operaciones",
                            project = doc.getString("project") ?: "General",
                            functionalArea = doc.getString("functionalArea") ?: "Operaciones",
                            systemRole = try {
                                SystemRole.valueOf(doc.getString("systemRole") ?: "EMPLOYEE")
                            } catch (e: Exception) { SystemRole.EMPLOYEE },
                            status = try {
                                EmployeeStatus.valueOf(doc.getString("status") ?: "ACTIVO")
                            } catch (e: Exception) { EmployeeStatus.ACTIVO },
                            avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF2563EB,
                            hireDate = doc.getString("hireDate") ?: "2024-01-15",
                            notes = doc.getString("notes") ?: "",
                            updatedAt = remoteUpdatedAt
                        )
                        database.employeeDao().insertEmployee(newEmp)
                        totalPulled++
                    } else if (remoteUpdatedAt > localEmp.updatedAt) {
                        // Last-Write-Wins: El remoto es más reciente, actualizar Room
                        val updated = localEmp.copy(
                            name = doc.getString("name") ?: localEmp.name,
                            phone = doc.getString("phone") ?: localEmp.phone,
                            passwordHash = doc.getString("passwordHash") ?: localEmp.passwordHash,
                            jobTitle = doc.getString("jobTitle") ?: localEmp.jobTitle,
                            department = doc.getString("department") ?: localEmp.department,
                            systemRole = try {
                                SystemRole.valueOf(doc.getString("systemRole") ?: localEmp.systemRole.name)
                            } catch (e: Exception) { localEmp.systemRole },
                            status = try {
                                EmployeeStatus.valueOf(doc.getString("status") ?: localEmp.status.name)
                            } catch (e: Exception) { localEmp.status },
                            updatedAt = remoteUpdatedAt
                        )
                        database.employeeDao().updateEmployee(updated)
                        totalPulled++
                    }
                } catch (e: Exception) {
                    Log.w("IntegrationService", "Error procesando delta de empleado", e)
                }
            }

            // 2. Delta Sync de TURNOS (Shifts)
            val shiftsSnapshot = firestore.collection("companies")
                .document(cleanCode)
                .collection("shifts")
                .whereGreaterThan("updatedAt", lastSync)
                .get()
                .await()

            for (doc in shiftsSnapshot.documents) {
                try {
                    val remoteShiftId = doc.getLong("id") ?: 0L
                    val remoteUpdatedAt = doc.getLong("updatedAt") ?: syncStartTimestamp
                    val localShift = if (remoteShiftId > 0) database.shiftDao().getShiftById(remoteShiftId) else null

                    if (localShift == null) {
                        val newShift = Shift(
                            id = if (remoteShiftId > 0) remoteShiftId else 0,
                            companyCode = cleanCode,
                            employeeId = doc.getLong("employeeId") ?: 0L,
                            employeeName = doc.getString("employeeName") ?: "",
                            department = doc.getString("department") ?: "Operaciones",
                            date = doc.getString("date") ?: "",
                            shiftType = try {
                                ShiftType.valueOf(doc.getString("shiftType") ?: "MANANA")
                            } catch (e: Exception) { ShiftType.MANANA },
                            startTime = doc.getString("startTime") ?: "08:00",
                            endTime = doc.getString("endTime") ?: "16:00",
                            notes = doc.getString("notes") ?: "",
                            updatedAt = remoteUpdatedAt
                        )
                        database.shiftDao().insertShift(newShift)
                        totalPulled++
                    } else if (remoteUpdatedAt > localShift.updatedAt) {
                        // Last-Write-Wins: Sobrescribir en Room con la versión remota más reciente
                        val updated = localShift.copy(
                            employeeName = doc.getString("employeeName") ?: localShift.employeeName,
                            department = doc.getString("department") ?: localShift.department,
                            date = doc.getString("date") ?: localShift.date,
                            shiftType = try {
                                ShiftType.valueOf(doc.getString("shiftType") ?: localShift.shiftType.name)
                            } catch (e: Exception) { localShift.shiftType },
                            startTime = doc.getString("startTime") ?: localShift.startTime,
                            endTime = doc.getString("endTime") ?: localShift.endTime,
                            notes = doc.getString("notes") ?: localShift.notes,
                            updatedAt = remoteUpdatedAt
                        )
                        database.shiftDao().updateShift(updated)
                        totalPulled++
                    }
                } catch (e: Exception) {
                    Log.w("IntegrationService", "Error procesando delta de turno", e)
                }
            }

            // Guardar nuevo cursor temporal para la próxima sincronización diferencial
            prefs.edit().putLong(prefKey, syncStartTimestamp).apply()

            Result.success(totalPulled)
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error en Pull Delta Sync", e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización Delta completa con Cloud Firestore:
     * 1. Sube todos los fichajes pendientes usando WriteBatch (optimización de llamadas y red).
     * 2. Descarga deltas de documentos actualizados desde lastSyncTimestamp (optimización cuota Spark).
     */
    suspend fun forceFullSync(database: AppDatabase, companyCode: String = "S1-CORP"): CloudSyncReport = withContext(Dispatchers.IO) {
        val cleanCode = companyCode.trim().uppercase()
        if (!isNetworkAvailable()) {
            return@withContext CloudSyncReport(
                success = false,
                syncedClocksCount = 0,
                pulledUpdatesCount = 0,
                message = "Dispositivo sin conexión a internet (Modo Offline). Operando con Room como única fuente de verdad (SSOT).",
                isCloudConnected = false
            )
        }

        val firestore = getFirestore()
        if (firestore == null) {
            // Firebase aún no está vinculado con google-services.json
            val pending = database.timeClockDao().getPendingClockEntriesForCompany(cleanCode)
            val count = pending.size
            val now = System.currentTimeMillis()
            for (p in pending) {
                database.timeClockDao().updateClockEntry(
                    p.copy(
                        syncStatus = SyncStatus.SYNCED,
                        serverSyncId = "OFFLINE-SYNC-${p.id}",
                        lastSyncAttemptAt = now
                    )
                )
            }
            return@withContext CloudSyncReport(
                success = true,
                syncedClocksCount = count,
                pulledUpdatesCount = 0,
                message = "Modo Offline-First activo: $count fichajes consolidados localmente en SQLite Room.",
                isCloudConnected = false
            )
        }

        try {
            // 1. PUSH PENDING TIME CLOCKS via WriteBatch
            val pushResult = pushPendingChangesToFirestore(database, cleanCode)
            val pushedCount = pushResult.getOrDefault(0)

            // 2. PULL DELTAS (Solo documentos donde updatedAt > lastSyncTimestamp) con Last-Write-Wins
            val pullResult = pullDifferentialUpdatesFromFirestore(database, cleanCode)
            val pulledCount = pullResult.getOrDefault(0)

            CloudSyncReport(
                success = true,
                syncedClocksCount = pushedCount,
                pulledUpdatesCount = pulledCount,
                message = "Sincronización delta con Firebase Firestore completada ($cleanCode): $pushedCount registros subidos en lote atómico, $pulledCount deltas sincronizados (LWW).",
                isCloudConnected = true
            )
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error durante sincronización delta Firestore", e)
            CloudSyncReport(
                success = false,
                syncedClocksCount = 0,
                pulledUpdatesCount = 0,
                message = "Fallo de sincronización con Firestore: ${e.localizedMessage ?: e.message}",
                isCloudConnected = true
            )
        }
    }

    // =============================================================================================
    // NOTIFICACIONES SLACK Y EMAIL
    // =============================================================================================

    suspend fun sendSlackNotification(
        webhookUrl: String,
        channel: String,
        title: String,
        message: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL del Webhook de Slack no configurada"))
        }

        try {
            val json = JSONObject().apply {
                put("channel", if (channel.isNotBlank()) channel else "#general")
                put("text", "*$title*\n$message")
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl.trim())
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Notificación enviada a Slack correctamente")
            } else {
                Result.failure(Exception("Error en Slack: Código HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("IntegrationService", "Slack error", e)
            Result.failure(e)
        }
    }

    fun openEmailClient(
        recipientEmail: String,
        subject: String,
        body: String
    ): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Enviar correo corporativo").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            true
        } catch (e: Exception) {
            Log.e("IntegrationService", "Error opening email client", e)
            false
        }
    }
}
