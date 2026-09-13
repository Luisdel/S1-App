package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.local.AppDatabase
import com.example.data.model.SyncStatus
import com.example.data.repository.IntegrationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BackgroundSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("BackgroundSyncWorker", "WorkManager despertado: ejecutando sincronización silenciosa en segundo plano...")
        val database = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
        val integrationService = IntegrationService(applicationContext)

        if (!integrationService.isNetworkAvailable()) {
            Log.d("BackgroundSyncWorker", "Sin conexión de red todavía (Modo Offline). Reintentando cuando se recupere la señal.")
            return@withContext Result.retry()
        }

        try {
            val pendingEntries = database.timeClockDao().getPendingClockEntries()
            Log.d("BackgroundSyncWorker", "Encontrados ${pendingEntries.size} fichajes offline pendientes de sincronizar.")

            for (entry in pendingEntries) {
                database.timeClockDao().updateClockEntry(entry.copy(syncStatus = SyncStatus.SYNCING))
                val result = integrationService.syncClockEntryToCloud(entry)
                if (result.isSuccess) {
                    val serverId = result.getOrNull() ?: "SRV-CLOUD-${System.currentTimeMillis()}"
                    database.timeClockDao().updateClockEntry(
                        entry.copy(
                            syncStatus = SyncStatus.SYNCED,
                            serverSyncId = serverId,
                            lastSyncAttemptAt = System.currentTimeMillis()
                        )
                    )
                    Log.d("BackgroundSyncWorker", "Fichaje #${entry.id} sincronizado exitosamente con el servidor cloud ($serverId)")
                } else {
                    database.timeClockDao().updateClockEntry(
                        entry.copy(
                            syncStatus = SyncStatus.PENDING,
                            syncAttempts = entry.syncAttempts + 1,
                            lastSyncAttemptAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // Descarga de datos maestros (Single Source of Truth)
            integrationService.pullCloudUpdatesToLocal(database)

            Result.success()
        } catch (e: Exception) {
            Log.e("BackgroundSyncWorker", "Error inesperado en sincronización en segundo plano", e)
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_IMMEDIATE_WORK = "S1ImmediateSyncWorker"
        const val UNIQUE_PERIODIC_WORK = "S1PeriodicSyncWorker"

        /**
         * Encola una sincronización inmediata que se ejecutará en cuanto el dispositivo
         * tenga conexión a internet (Modo Online).
         */
        fun enqueueImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<BackgroundSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_IMMEDIATE_WORK,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("BackgroundSyncWorker", "WorkManager: Tarea inmediata de sincronización encolada.")
        }

        /**
         * Programa la sincronización periódica silenciosa en segundo plano
         */
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d("BackgroundSyncWorker", "WorkManager: Sincronización periódica programada cada 15 min.")
        }
    }
}
