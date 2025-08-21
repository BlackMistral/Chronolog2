package com.heimdall.chronolog.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import timber.log.Timber
import com.heimdall.chronolog.data.LogEntry
import java.util.Date

class NotificationCaptureService : NotificationListenerService() {

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

        val message = "[$packageName] Title: ${title ?: "N/A"}, Text: ${text ?: "N/A"}"

        // TODO: Implement hashing of the message and saving to the repository
        Timber.d("Captured notification: $message at $postTime")

        // Placeholder for saving the captured notification data
        // val logEntry = LogEntry(timestamp = postTime, message = message, hashedMessage = "") // Hashing will be done before saving
        // repository.insertLogEntry(logEntry) // Assuming 'repository' is available (e.g., injected)

        // TODO: Implement starting this service as a foreground service in a real app
        // This requires showing a persistent notification to the user.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }

    // TODO: Implement onBind and other service lifecycle methods as necessary
}
