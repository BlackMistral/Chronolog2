package com.heimdall.chronolog.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.heimdall.chronolog.data.Repository
import com.heimdall.chronolog.google.GoogleDriveBackupManager // Correct import
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import com.heimdall.chronolog.core.HashUtils // Import HashUtils
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class PeriodicExportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: Repository,
 private val googleDriveBackupManager: GoogleDriveBackupManager // Inject GoogleDriveBackupManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("PeriodicExportWorker started")

            // 1. Retrieve data
            val logEntries = repository.allLogEntries.first()
            Timber.d("Retrieved ${logEntries.size} log entries")

            if (logEntries.isEmpty()) {
                Timber.d("No log entries to backup, skipping.")
                return@withContext Result.success()
            }

            // 2. Serialize data to JSON
            val jsonArray = JSONArray()
            logEntries.forEach { entry ->
                val jsonObject = JSONObject().apply {
                    put("id", entry.id)
                    put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(entry.timestamp))
                    put("message", entry.message)
                    put("hashedMessage", entry.hashedMessage)
                }
                jsonArray.put(jsonObject)
            }
            val jsonData = jsonArray.toString()
            Timber.d("Serialized data to JSON.")

            // 3. Calculate hash of serialized data
 // Use the injected HashUtils or a separate instance if not injected
            val dataHash = HashUtils.sha256(jsonData)
            if (dataHash == null) {
                Timber.e("Failed to calculate hash of serialized data.")
                return@withContext Result.failure()
            }
            Timber.d("Calculated data hash: $dataHash")

            // 4. Create backup file with hash and encrypted data
            val fileName = "chronolog_backup_${System.currentTimeMillis()}.backup"
 // For simplicity, creating a byte array to represent the backup file content
 // In a real app, you'd handle encryption and file writing securely.

 // Simple wrapper format: [Hash (SHA-256)] [JSON Data]
 // In a secure implementation, you'd encrypt jsonData before concatenating with the hash
 // and the hash would likely be of the encrypted data or a more complex structure.
            val finalOutputStream = ByteArrayOutputStream()
            finalOutputStream.write(dataHash.toByteArray()) // Write hash
            finalOutputStream.write(outputStream.toByteArray()) // Write salt, iv, and encrypted data

            backupFile.writeBytes(finalOutputStream.toByteArray())
            Timber.d("Created backup file: ${backupFile.absolutePath}")

            // 5. Upload to Google Drive
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (account == null) {
                Timber.e("No Google account signed in for backup.")
                return@withContext Result.failure()
            }

 // Use GoogleDriveBackupManager to upload the byte array content
 val uploadResult = googleDriveBackupManager.uploadBackupFile(fileName, finalOutputStream.toByteArray()).await()

 if (uploadResult != null) {
                Timber.d("Backup uploaded to Google Drive successfully.")
                Result.success()
            } else {
                Timber.e("Failed to upload backup to Google Drive.")
                Result.retry() // Retry upload if failed
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during periodic backup:")
            Result.failure()
        }
    }

    // Helper function to calculate SHA-256 hash (can use HashUtils instead)
}