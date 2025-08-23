chmod +x gradlew
./gradlew app:dependencies
package com.heimdall.chronolog.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.heimdall.chronolog.databinding.ActivitySettingsBinding
import com.heimdall.chronolog.workers.PeriodicExportWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.concurrent.TimeUnit
import com.google.android.gms.common.api.ApiException
import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog // Consider using androidx.appcompat.app.AlertDialog with ProgressBar instead
import com.heimdall.chronolog.R // Add R import
import com.heimdall.chronolog.backup.BackupPasswordManager
import com.heimdall.chronolog.core.CryptoManager
import timber.log.Timber
import dagger.hilt.android.AndroidEntryPoint
import com.squareup.moshi.Moshi
import com.heimdall.chronolog.backup.BackupFile
import com.heimdall.chronolog.data.Repository
import com.heimdall.chronolog.backup.BackupData
import com.heimdall.chronolog.core.HashUtils
import com.heimdall.chronolog.databinding.DialogProgressBinding
import com.heimdall.chronolog.data.LogEntry
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import java.io.ByteArrayInputStream
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
import android.widget.ArrayAdapter
import android.widget.EditText
import javax.inject.Inject
import com.heimdall.chronolog.google.GoogleDriveBackupManager // Keep this import

private const val RC_SIGN_IN = 9003 // Arbitrary request code
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private var progressDialog: AlertDialog? = null
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var googleDriveBackupManager: GoogleDriveBackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        setupInitialState() // This will now include loading password state

        setGoogleSignInAccount(GoogleSignIn.getLastSignedInAccount(this)) // Set initial Google account state
        checkNotificationListenerPermission()
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission status when the Activity is resumed
        checkNotificationListenerPermission()
    }

    private fun setupViews() {
        // View Binding handles the findViewById calls
        // Add references to the new UI elements
    }

 private fun setupListeners() {
        binding.buttonNotificationListenerSettings.setOnClickListener {
 if (!isNotificationListenerEnabled()) {
 showNotificationPermissionRationale()
 } else {
                // Permission already granted, maybe indicate that or offer to revoke
                Toast.makeText(this, "Notification listener permission is already granted.", Toast.LENGTH_SHORT).show()
 }
        }

        binding.switchPeriodicBackup.setOnCheckedChangeListener { _, isChecked ->
 togglePeriodicBackup(isChecked)
        }

        // Add listeners for Google Drive backup UI elements
        binding.switchGoogleDriveBackup.setOnCheckedChangeListener { _, isChecked ->
 if (isChecked) enableGoogleDriveBackup() else disableGoogleDriveBackup() // TODO: Save state
        }
        binding.buttonChooseGoogleDriveAccount.setOnClickListener {
            Toast.makeText(this, "Choose Google Drive account (placeholder)", Toast.LENGTH_SHORT).show()
 launchGoogleSignIn()
        }

        binding.buttonExport.setOnClickListener {
            // TODO: Implement log export functionality
            Toast.makeText(this, "Export logs (placeholder)", Toast.LENGTH_SHORT).show()
        }

        binding.buttonImport.setOnClickListener {
 handleImportLogsClick()
        }
    }

    private fun setupInitialState() {
        // Load and set the initial state of settings switches
        val sharedPreferences = getSharedPreferences("chronolog_settings", Context.MODE_PRIVATE)
 binding.switchPeriodicBackup.isChecked = sharedPreferences.getBoolean("periodic_backup_enabled", false)
 // TODO: Load initial state for Google Drive backup switch
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners != null && enabledListeners.contains(packageName)
    }

    private fun checkNotificationListenerPermission() {
        if (isNotificationListenerEnabled()) {
 binding.textNotificationListenerStatus.text = getString(R.string.notification_listener_status_enabled)
 binding.buttonNotificationListenerSettings.text = getString(R.string.notification_listener_settings_button_enabled)
        } else {
 binding.textNotificationListenerStatus.text = getString(R.string.notification_listener_status_disabled)
 binding.buttonNotificationListenerSettings.text = getString(R.string.notification_listener_settings_button_disabled)
        }
    }

    private fun showNotificationPermissionRationale() {
 AlertDialog.Builder(this)
            .setTitle(R.string.notification_listener_rationale_title)
            .setMessage(R.string.notification_listener_rationale_message)
            .setPositiveButton(R.string.notification_listener_rationale_positive_button) { dialog, _ ->
 openNotificationListenerSettings()
 dialog.dismiss()
            }
            .setNegativeButton(R.string.notification_listener_rationale_negative_button) { dialog, _ ->
 dialog.dismiss()
            }
 .show()
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun togglePeriodicBackup(enable: Boolean) {
        val workManager = WorkManager.getInstance(this)
        val sharedPreferences = getSharedPreferences("chronolog_settings", Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {
 putBoolean("periodic_backup_enabled", enable)
 apply()
        }

        if (enable) {
            val periodicExportRequest = PeriodicWorkRequestBuilder<PeriodicExportWorker>(
 24, TimeUnit.HOURS // Adjust frequency as needed
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED) // Only run on unmetered network
                        .setRequiresCharging(true) // Only run when charging
 .build()
                )
 .build()
            workManager.enqueueUniquePeriodicWork("PeriodicExport", ExistingPeriodicWorkPolicy.REPLACE, periodicExportRequest)
            Toast.makeText(this, "Periodic backup enabled", Toast.LENGTH_SHORT).show()
        } else {
            workManager.cancelUniqueWork("PeriodicExport")
            Toast.makeText(this, "Periodic backup disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGoogleSignIn() {
        val signInClient = googleDriveBackupManager.getGoogleSignInClient()
        val signInIntent = signInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleImportLogsClick() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            Toast.makeText(this, "Please connect a Google account first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Google Drive Manager with the account for UI operations
        googleDriveBackupManager.initializeDriveService(account) // Initialize with the signed-in account
        showProgressDialog(getString(R.string.progress_dialog_loading_backups))

 googleDriveBackupManager.listBackupFiles()
 .addOnSuccessListener { files ->
 hideProgressDialog()
                if (files.files.isNullOrEmpty()) {
                    Toast.makeText(this, getString(R.string.no_backup_files_found), Toast.LENGTH_SHORT).show()
                } else {
                    showBackupSelectionDialog(files.files)
                }
            }
 .addOnFailureListener { exception ->
 hideProgressDialog()
 Timber.e(exception, "Failed to list backup files:")
                Toast.makeText(this, getString(R.string.failed_to_load_backup_files, exception.message), Toast.LENGTH_LONG).show()
            }
    }

 private fun showBackupSelectionDialog(files: List<com.google.api.services.drive.model.File>) {
        val fileNames = files.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, fileNames)

        AlertDialog.Builder(this)
            .setTitle(R.string.select_backup_file_title)
            .setAdapter(adapter) { dialog, which ->
                val selectedFile = files[which]
 handleBackupFileDownload(selectedFile)
 dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
 dialog.dismiss()
            }
 .show()
    }

    private fun handleBackupFileDownload(file: com.google.api.services.drive.model.File) {
        // Show a loading dialog while downloading
        showProgressDialog(getString(R.string.progress_dialog_downloading_backup))

        val account = GoogleSignIn.getLastSignedInAccount(this) ?: run { // Safety check
 hideProgressDialog()
            Toast.makeText(this, getString(R.string.google_account_not_connected), Toast.LENGTH_SHORT).show()
 return
        }
        googleDriveBackupManager.initializeDriveService(account) // Initialize with the signed-in account

        lifecycleScope.launch {
 when (val result = googleDriveBackupManager.downloadFileBytes(file.id!!)) {
                is com.heimdall.chronolog.core.Result.Success -> {
 hideProgressDialog()
                    // Now handle the downloaded content (verification, decryption, import)
 showPasswordPrompt(result.data)
                }
                is com.heimdall.chronolog.core.Result.Error -> {
 hideProgressDialog()
 Timber.e(result.exception, "Failed to download backup file:")
                    Toast.makeText(this, getString(R.string.failed_to_download_backup_file, result.exception.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showPasswordPrompt(backupFileBytes: ByteArray) {
        val passwordEditText = EditText(this)
        passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_backup_password_title)
            .setView(passwordEditText)
            .setPositiveButton(R.string.dialog_import) { dialog, _ ->
                val password = passwordEditText.text.toString()
                if (password.isNotEmpty()) {
                    // Start the import process in a coroutine
                    lifecycleScope.launch {
 handleImportProcess(backupFileBytes, password.toCharArray())
                    }
                } else {
                    Toast.makeText(this, getString(R.string.password_cannot_be_empty), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
 dialog.dismiss()
            }
 .show()
    }

    private suspend fun handleImportProcess(backupFileBytes: ByteArray, password: CharArray) {
        showProgressDialog(getString(R.string.progress_dialog_importing_data))
        try {
            // 1. Deserialize BackupFile
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val backupFileAdapter = moshi.adapter(BackupFile::class.java)
            val backupFile = withContext(Dispatchers.IO) {
 backupFileAdapter.fromJson(String(backupFileBytes))
            } ?: throw IllegalArgumentException("Invalid backup file format")

            // 2. Verify Hash
            val recalculatedHash = HashUtils.sha256(backupFile.encryptedData.toByteArray())
            if (recalculatedHash != backupFile.dataHash) {
 throw SecurityException("Backup file integrity check failed.")
            }

            // 3. Decrypt Data
            // CryptoManager is now injected
            val cryptoManager = CryptoManager(applicationContext) // TODO: Inject CryptoManager properly
            val decryptedDataJson = try {
                val decryptedBytes = withContext(Dispatchers.IO) {
 cryptoManager.decryptBackup(
 new ByteArrayInputStream(backupFile.encryptedData.toByteArray()),
 password
 )
                }
                String(decryptedBytes)
            } catch (e: Exception) {
 Timber.e(e, "Decryption failed.")
                // Check specifically for bad padding/invalid key for wrong password indication
                if (e is javax.crypto.BadPaddingException || e is java.security.InvalidKeyException) {
 throw SecurityException("Incorrect password.", e)
                } else {
 throw e
                }
            }

            // 4. Deserialize LogEntries
            val backupDataAdapter = moshi.adapter(BackupData::class.java)
            val backupData = withContext(Dispatchers.IO) {
 backupDataAdapter.fromJson(decryptedDataJson)
            } ?: throw IllegalArgumentException("Invalid data format after decryption.")

            // 5. Import into Database (Handling Conflicts)
            if (backupData.logEntries.isNotEmpty()) {
 showConflictResolutionDialog(backupData.logEntries)
            } else {
 hideProgressDialog()
 Toast.makeText(this, getString(R.string.no_entries_to_import), Toast.LENGTH_SHORT).show()
            }

        } catch (e: SecurityException) {
 hideProgressDialog()
            when (e.message) {
 "Backup file integrity check failed." -> {
 Toast.makeText(this, getString(R.string.import_failed_integrity), Toast.LENGTH_LONG).show()
                }
 "Incorrect password." -> {
 Toast.makeText(this, getString(R.string.import_failed_incorrect_password), Toast.LENGTH_SHORT).show()
                }
 else -> {
 Timber.e(e, "Security error during import:")
 Toast.makeText(this, getString(R.string.import_failed_security_error), Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: IllegalArgumentException) {
 hideProgressDialog()
 Timber.e(e, "Data format error during import:")
            Toast.makeText(this, getString(R.string.import_failed_invalid_data_format), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
 hideProgressDialog()
 Timber.e(e, "General error during import:")
            Toast.makeText(this, getString(R.string.import_failed_unexpected_error, e.message), Toast.LENGTH_LONG).show()
        } finally {
            // Ensure password char array is cleared after use
 password.fill('\u0000')
        }
    }

    enum class ImportConflictStrategy { OVERWRITE, IGNORE }

    private fun showConflictResolutionDialog(logEntries: List<LogEntry>) {
        val options = arrayOf(
 getString(R.string.conflict_resolution_ignore),
 getString(R.string.conflict_resolution_overwrite)
 )

        AlertDialog.Builder(this)
            .setTitle(R.string.import_conflict_resolution_title)
            .setItems(options) { dialog, which ->
                val importConflictStrategy = when (which) {
                    0 -> ImportConflictStrategy.IGNORE
                    1 -> ImportConflictStrategy.OVERWRITE
                    else -> ImportConflictStrategy.IGNORE // Default to ignore
                }
                // Proceed with import based on selected strategy
                lifecycleScope.launch {
                    importDataIntoDatabase(logEntries, importConflictStrategy)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                // User cancelled import
                dialog.dismiss()
                Toast.makeText(this, getString(R.string.import_cancelled), Toast.LENGTH_SHORT).show()
            }
 .show()
    }

    private suspend fun importDataIntoDatabase(logEntries: List<LogEntry>, strategy: ImportConflictStrategy) {
        showProgressDialog(getString(R.string.progress_dialog_importing_to_database))
        try {
            when (strategy) {
                ImportConflictStrategy.OVERWRITE -> {
                    repository.clearAllLogEntries()
                    repository.insertLogEntries(logEntries)
                }
                ImportConflictStrategy.IGNORE -> {
                    repository.insertLogEntries(logEntries) // Assuming repository handles ignore internally
                }
            }
            hideProgressDialog()
            Toast.makeText(this, getString(R.string.import_completed_successfully), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
 hideProgressDialog()
 Timber.e(e, "Failed to import data into database:")
            Toast.makeText(this, getString(R.string.import_failed_database_error, e.message), Toast.LENGTH_LONG).show()
        }
    }

    // Progress Dialog management
    private fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            val dialogBinding = DialogProgressBinding.inflate(layoutInflater)
            dialogBinding.progressMessage.text = message
            progressDialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .setCancelable(false)
 .create()
        } else {
 progressDialog?.findViewById<TextView>(R.id.progress_message)?.text = message
        }
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun enableGoogleDriveBackup() {
        // TODO: Implement logic to enable Google Drive backup
    }

    private fun disableGoogleDriveBackup() {
        // TODO: Implement logic to disable Google Drive backup
    }

    private fun setGoogleSignInAccount(account: GoogleSignInAccount?) {
        if (account != null) {
            binding.textGoogleDriveAccount.text = getString(R.string.google_drive_connected, account.email)
            // Drive service initialized on demand in handleImportLogsClick
            // For worker, the account is passed via InputData
        } else {
            binding.textGoogleDriveAccount.text = getString(R.string.google_drive_not_connected)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            setGoogleSignInAccount(account)
 Toast.makeText(this, getString(R.string.google_sign_in_successful), Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please see the GoogleSignInStatusCodes class reference for more information.
            Timber.w(e, "signInResult:failed code=%s", e.statusCode)
            setGoogleSignInAccount(null)
            Toast.makeText(this, getString(R.string.google_sign_in_failed, e.statusCode), Toast.LENGTH_LONG).show()
        }
    }

        }
    }

    private fun setupInitialState() {
 // TODO: Load initial state for Google Drive backup switch
        // TODO: Load initial state for the periodic backup switch if needed
        // For now, it starts unchecked
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun togglePeriodicBackup(enable: Boolean) {
        val workManager = WorkManager.getInstance(this)
        if (enable) {
            val periodicExportRequest = PeriodicWorkRequestBuilder<PeriodicExportWorker>(
 24, TimeUnit.HOURS // Adjust frequency as needed
            ).build()
            workManager.enqueueUniquePeriodicWork("PeriodicExport", ExistingPeriodicWorkPolicy.REPLACE, periodicExportRequest)
            Toast.makeText(this, "Periodic backup enabled", Toast.LENGTH_SHORT).show()
        } else {
            workManager.cancelUniqueWork("PeriodicExport")
            Toast.makeText(this, "Periodic backup disabled", Toast.LENGTH_SHORT).show()
        }
    }

 private fun launchGoogleSignIn() {
        val signInClient = googleDriveBackupManager.getGoogleSignInClient()
        val signInIntent = signInClient.signInIntent
 startActivityForResult(signInIntent, RC_SIGN_IN)
    }

 private fun handleImportLogsClick() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            Toast.makeText(this, "Please connect a Google account first.", Toast.LENGTH_SHORT).show()
            return
        }

 // Show a loading dialog while listing files
 googleDriveBackupManager.initializeDriveService(account) // Initialize with the signed-in account
 showProgressDialog("Loading backups...")

 googleDriveBackupManager.listBackupFiles()
 .addOnSuccessListener { files ->
 hideProgressDialog()
                if (files.isEmpty()) {
                    Toast.makeText(this, "No backup files found on Google Drive.", Toast.LENGTH_SHORT).show()
                } else {
                    showBackupSelectionDialog(files)
                }
            }
 .addOnFailureListener { exception ->
 hideProgressDialog()
 Timber.e(exception, "Failed to list backup files:")
                Toast.makeText(this, "Failed to load backup files: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

 private fun showBackupSelectionDialog(files: List<com.google.api.services.drive.model.File>) {
        val fileNames = files.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, fileNames)

        AlertDialog.Builder(this)
            .setTitle("Select Backup File")
            .setAdapter(adapter) { dialog, which ->
                val selectedFile = files[which]
 handleBackupFileDownload(selectedFile)
 dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
 dialog.dismiss()
            }
            .show()
    }

 private fun handleBackupFileDownload(file: com.google.api.services.drive.model.File) {
        // Show a loading dialog while downloading
 showProgressDialog("Downloading backup...")

        val googleDriveBackupManager = GoogleDriveBackupManager(this)
        val account = GoogleSignIn.getLastSignedInAccount(this) ?: return // Safety check
 googleDriveBackupManager.initializeDriveService(account) // Initialize with the signed-in account
 // Use lifecycleScope or a separate CoroutineScope for suspend functions

 .addOnSuccessListener { downloadedBytes ->
 hideProgressDialog()
 // Now handle the downloaded content (verification, decryption, import)
                showPasswordPrompt(downloadedBytes)
            }
 .addOnFailureListener { exception ->
 hideProgressDialog()
 Timber.e(exception, "Failed to download backup file:")
                Toast.makeText(this, "Failed to download backup file: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

 private fun showPasswordPrompt(backupFileBytes: ByteArray) {
        val passwordEditText = EditText(this)
 passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

 AlertDialog.Builder(this)
 .setTitle("Enter Backup Password")
 .setView(passwordEditText)
 .setPositiveButton("Import") { dialog, _ ->
                val password = passwordEditText.text.toString()
                if (password.isNotEmpty()) {
 // Start the import process in a coroutine
 lifecycleScope.launch {
 handleImportProcess(backupFileBytes, password.toCharArray())
 }
 } else {
 Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show()
 }
 dialog.dismiss()
            }
 .setNegativeButton("Cancel") { dialog, _ ->
 dialog.dismiss()
            }
 .show()
    }

 private suspend fun handleImportProcess(backupFileBytes: ByteArray, password: CharArray) {
 showProgressDialog("Importing data...")
 try {
            // 1. Deserialize BackupFile
            val moshi = Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val backupFileAdapter = moshi.adapter(BackupFile::class.java)
            val backupFile = backupFileAdapter.fromJson(String(backupFileBytes))
                ?: throw IllegalArgumentException("Invalid backup file format")

            // 2. Verify Hash
            val recalculatedHash = HashUtils.sha256(backupFile.encryptedData.toByteArray())
            if (recalculatedHash != backupFile.dataHash) {
 throw SecurityException("Backup file integrity check failed.")
            }

            // 3. Decrypt Data
            val cryptoManager = CryptoManager(applicationContext) // TODO: Inject CryptoManager
            val decryptedDataJson = try {
                val decryptedBytes = cryptoManager.decryptBackup(
 new ByteArrayInputStream(backupFile.encryptedData.toByteArray()),
 password
 )
 String(decryptedBytes)
            } catch (e: Exception) {
 Timber.e(e, "Decryption failed.")
 // Check specifically for bad padding/invalid key for wrong password indication
 if (e is javax.crypto.BadPaddingException || e is java.security.InvalidKeyException) {
 throw SecurityException("Incorrect password.", e)
                } else {
 throw e
                }
            }

            // 4. Deserialize LogEntries
            val backupDataAdapter = moshi.adapter(BackupData::class.java)
            val backupData = backupDataAdapter.fromJson(decryptedDataJson)
                ?: throw IllegalArgumentException("Invalid data format after decryption.")

            // 5. Import into Database (Handling Conflicts - currently ignoring existing)
            // Show conflict resolution dialog

 // hideProgressDialog() // Move this after import is complete
 // Toast.makeText(this, "Backup imported successfully!\", Toast.LENGTH_SHORT).show() // Move this after import is complete

        } catch (e: SecurityException) {
 hideProgressDialog()
            Timber.e(e, "Security error during import:")
 // Moved specific security error handling to a separate function or within the catch block
        } catch (e: SecurityException) {
 hideProgressDialog()
            when (e.message) {
 "Backup file integrity check failed." -> {
 Toast.makeText(this, "Import failed: Backup file is corrupted or tampered.", Toast.LENGTH_LONG).show()
                }
 "Incorrect password." -> {
 Toast.makeText(this, "Import failed: Incorrect password.", Toast.LENGTH_SHORT).show()
                }
 else -> {
 Timber.e(e, "Security error during import:")
 Toast.makeText(this, "Import failed: Security error.", Toast.LENGTH_LONG).show()
                }
            }
 Timber.e(e, "Security error during import:")
        } catch (e: IllegalArgumentException) {
 hideProgressDialog()
 Timber.e(e, "Data format error during import:")
            Toast.makeText(this, "Import failed: Invalid data format.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
 hideProgressDialog()
 Timber.e(e, "General error during import:")
            Toast.makeText(this, "Import failed: An unexpected error occurred.", Toast.LENGTH_LONG).show()
        } finally {
            // Ensure password char array is cleared after use
 password.fill('\u0000')
        }
    }

    enum class ImportConflictStrategy { OVERWRITE, IGNORE }

    private fun showConflictResolutionDialog(logEntries: List<LogEntry>) {
        val options = arrayOf("Ignore existing entries", "Overwrite existing entries")

        AlertDialog.Builder(this)
            .setTitle("Import Conflict Resolution")
            .setItems(options) { dialog, which ->
                val importConflictStrategy = when (which) {
                    0 -> ImportConflictStrategy.IGNORE
                    1 -> ImportConflictStrategy.OVERWRITE
                    else -> ImportConflictStrategy.IGNORE // Default to ignore
                }
                // Proceed with import based on selected strategy
                lifecycleScope.launch {
                    importDataIntoDatabase(logEntries, importConflictStrategy)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // User cancelled import
                dialog.dismiss()
                Toast.makeText(this, "Import cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private suspend fun importDataIntoDatabase(logEntries: List<LogEntry>, strategy: ImportConflictStrategy) {
        showProgressDialog("Importing data into database...")
        try {
            when (strategy) {
                ImportConflictStrategy.OVERWRITE -> {
                    repository.clearAllLogEntries()
                    repository.insertLogEntries(logEntries)
                }
                ImportConflictStrategy.IGNORE -> {
                    repository.insertLogEntries(logEntries) // Assuming repository handles ignore internally
                }
            }
 hideProgressDialog()
            Toast.makeText(this, "Import completed successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
 hideProgressDialog()
 Timber.e(e, "Failed to import data into database:")
            Toast.makeText(this, "Import failed: Could not save data to database.", Toast.LENGTH_LONG).show()
        }
    }

 // ... (existing showProgressDialog and hideProgressDialog methods)
 private fun enableGoogleDriveBackup() {
 // TODO: Implement logic to enable Google Drive backup
 }

 private fun disableGoogleDriveBackup() {
 // TODO: Implement logic to disable Google Drive backup
 }

 private fun setGoogleSignInAccount(account: GoogleSignInAccount?) {
 if (account != null) {
 binding.textGoogleDriveAccount.text = "Connected: ${account.email}"
 // Drive service initialized on demand in handleImportLogsClick
 // For worker, the account is passed via InputData
 } else {
 binding.textGoogleDriveAccount.text = "Not connected"
 }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
 super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
 val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
 setGoogleSignInAccount(account)
        }
    }
}