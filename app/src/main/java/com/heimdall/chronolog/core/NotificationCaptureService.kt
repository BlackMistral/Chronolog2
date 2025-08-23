package com.heimdall.chronolog.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.heimdall.chronolog.R
import timber.log.Timber
import com.heimdall.chronolog.data.LogEntry
import com.heimdall.chronolog.data.Repository
import java.util.Date
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject // Keep this import
import androidx.lifecycle.lifecycleScope
import android.app.Notification
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


@AndroidEntryPoint
class NotificationCaptureService : NotificationListenerService() {

    @Inject
    lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    // onBind is typically not implemented for NotificationListenerService
    // override fun onBind(intent: Intent?): IBinder? {
    //     return super.onBind(intent)
    // }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName
        val postTime = Date(sbn.postTime)
        val notification = sbn.notification

        // Extract title and text from the notification extras
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()

        // Basic error handling for null or empty data
        if (title.isNullOrEmpty() && text.isNullOrEmpty()) {
            Timber.w("Received notification with no title or text from package: $packageName")
            return
        }

        val message = "${title ?: ""}: ${text ?: ""}"
        val messageToHash = "$packageName$message"
        val hashedMessage = HashUtils.sha256(messageToHash) ?: "ERROR_HASHING" // Handle hashing errors

        Timber.d("Captured notification: $message at $postTime")

        val logEntry =
            LogEntry(timestamp = postTime, message = message, hashedMessage = hashedMessage)
        lifecycleScope.launch { repository.insertLogEntry(logEntry) }
        // TODO: Implement starting this service as a foreground service in a real app
        // This requires showing a persistent notification to the user.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "ChronoLog Service Channel",
                NotificationManager.IMPORTANCE_LOW // Use IMPORTANCE_LOW to minimize disruption
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ChronoLog is running")
            .setContentText("Capturing notifications...")
            .setSmallIcon(R.drawable.ic_notification_bell) // Use an appropriate icon
            .setOngoing(true) // Makes the notification ongoing
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "chronolog_service_channel"
        private const val NOTIFICATION_ID = 1 // Unique ID for the foreground notification
    }
}



