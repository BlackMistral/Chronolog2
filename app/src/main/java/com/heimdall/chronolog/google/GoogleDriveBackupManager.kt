package com.heimdall.chronolog.core

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.IOException
import java.text.SimpleDateFormat
import java.io.InputStream
import java.io.FileOutputStream
import java.io.File as IoFile // Alias to avoid conflict with com.google.api.services.drive.model.File
import java.util.Collections
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import com.google.api.client.http.InputStreamContent
import java.io.ByteArrayInputStream
import timber.log.Timber

import java.util.Date
class GoogleDriveBackupManager(private val context: Context, private val executor: Executor = Executors.newSingleThreadExecutor()) {

    private var driveService: Drive? = null

    fun getGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.SCOPE_APPFOLDER)
            .requestEmail() // Requesting email is a common practice, adjust if needed
            .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    fun initializeDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPFOLDER)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("ChronoLog") // Set your app name
            .build()
    }

    fun createBackupFolder(): Task<String?> {
        return Tasks.call(executor) {
            val fileMetadata = File().apply {
                name = BACKUP_FOLDER_NAME
                mimeType = MIMETYPE_FOLDER
                parents = Collections.singletonList("appDataFolder") // Place in app data folder
            }

            try {
                driveService?.files()?.create(fileMetadata)
                    ?.setFields("id")
                    ?.execute()?.id
            } catch (e: IOException) {
 Timber.e(e, "Error creating backup folder on Google Drive")
                null
            }
        }
    }

    fun uploadBackupFile(content: ByteArray): Task<String?> {
        return Tasks.call(executor) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
            val fileMetadata = File().apply {
                name = "chronolog_backup_$timestamp.dat" // Unique name with timestamp
                parents = Collections.singletonList("appDataFolder") // Upload to app data folder
            }
            val mediaContent = InputStreamContent("application/octet-stream", ByteArrayInputStream(content))

            driveService?.files()?.create(fileMetadata, mediaContent)?.execute()?.id
        } catch (e: IOException) {
 Timber.e(e, "Error uploading backup file to Google Drive")
 null
        }

    }

    fun listBackupFiles(): Task<FileList?> {
        return Tasks.call(executor) {
            driveService?.files()?.list()
                ?.setSpaces("appDataFolder")
                ?.setFields("nextPageToken, files(id, name, modifiedTime, size)")
                ?.execute()
        } catch (e: IOException) {
 Timber.e(e, "Error listing backup files from Google Drive")
 null
        }

    }

    fun downloadFile(fileId: String, destinationFile: IoFile): Task<Void> {
        return Tasks.call(executor) {
            try {
                driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(FileOutputStream(destinationFile))
            } catch (e: IOException) {
 Timber.e(e, "Error downloading file $fileId from Google Drive")
 throw e // Re-throw to be caught by addOnFailureListener
            }
            null // Task<Void> requires returning null or Unit
        }

    fun deleteFile(fileId: String): Task<Void> {
        return Tasks.call(executor) {
            driveService?.files()?.delete(fileId)?.execute()
            null
        }
    }


}
private const val BACKUP_FOLDER_NAME = "ChronoLog_Backups"
private const val MIMETYPE_FOLDER = "application/vnd.google-apps.folder"