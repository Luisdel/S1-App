package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.NotificationLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class IntegrationService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

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
