package com.healthcare.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.healthcare.app.MainActivity
import com.healthcare.app.R
import com.healthcare.app.data.api.AuthApi
import com.healthcare.app.data.dto.FcmTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject lateinit var authApi: AuthApi
    @Inject lateinit var tokenManager: TokenManager

    companion object {
        private const val CHANNEL_ID = "appointments"
        private const val CHANNEL_NAME = "Appointment Reminders"
        private const val TAG = "FCMService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: ${token.take(10)}...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = tokenManager.getAccessToken()
                if (!accessToken.isNullOrBlank()) {
                    authApi.updateFcmToken(FcmTokenRequest(token))
                    Log.d(TAG, "FCM token sent to backend")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send FCM token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val type = data["type"] ?: ""
        val title = message.notification?.title ?: data["title"] ?: "HealthSuite"
        val body = message.notification?.body ?: data["body"] ?: ""

        Log.d(TAG, "FCM received: type=$type title=$title")

        val pendingIntent = when (type) {
            "appointment_reminder_1h", "token_update" -> {
                val lat = data["latitude"]?.toDoubleOrNull()
                val lng = data["longitude"]?.toDoubleOrNull()
                if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                    buildNavigationIntent(lat, lng, data["locationName"] ?: "Clinic")
                } else {
                    buildAppIntent()
                }
            }
            else -> buildAppIntent()
        }

        val notificationId = System.currentTimeMillis().toInt()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_login_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun buildNavigationIntent(lat: Double, lng: Double, label: String): PendingIntent {
        val uri = Uri.parse("google.navigation:q=$lat,$lng&label=${Uri.encode(label)}")
        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        // Fallback: if Google Maps not installed, open in browser
        val chooser = Intent.createChooser(mapsIntent, "Navigate to $label")
        return PendingIntent.getActivity(
            this,
            lat.hashCode(),
            chooser,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Appointment reminders and token updates"
            enableVibration(true)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
