package com.heimdall.chronolog.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.heimdall.chronolog.R
import com.heimdall.chronolog.core.PeriodicExportWorker
import com.heimdall.chronolog.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val vm: SettingsViewModel by viewModels() // Usa un ViewModel dedicato

    // Moderno Activity Result Launcher per il Google Sign-In
    private val googleSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    vm.handleSignInSuccess(account)
                } catch (e: ApiException) {
                    Timber.e(e, "Google Sign-In fallito")
                    vm.handleSignInFailure()
                }
            } else {
                vm.handleSignInFailure()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setupToolbar()
        setupListeners()
        observeViewModel()

        vm.checkInitialState() // Controlla lo stato iniziale (listener, Google Sign-In)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar) // Assumendo che tu abbia una Toolbar nel layout
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.notification_settings_title)
    }

    private fun setupListeners() {
        binding.buttonListenerSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.switchPeriodicBackup.setOnCheckedChangeListener { _, isChecked ->
            togglePeriodicBackup(isChecked)
        }

        binding.buttonChooseGoogleAccount.setOnClickListener {
            launchGoogleSignIn()
        }
        
        // Aggiungi qui altri listener per import/export se necessario
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.uiState.collect { state ->
                // Aggiorna la UI in base allo stato del ViewModel
                
                // Stato del listener di notifiche
                binding.textViewListenerStatus.text = getString(
                    if (state.isNotificationListenerEnabled) R.string.notification_listener_status_enabled
                    else R.string.notification_listener_status_disabled
                )

                // Stato dell'account Google
                binding.textViewGoogleAccount.text = if (state.googleAccount != null) {
                    getString(R.string.google_drive_account_connected, state.googleAccount.email)
                } else {
                    getString(R.string.google_drive_not_connected)
                }
                
                // Abilita/Disabilita i pulsanti che dipendono da Google Sign-In
                binding.buttonImport.isEnabled = state.googleAccount != null
                binding.buttonExport.isEnabled = state.googleAccount != null

                // Stato dello switch per il backup periodico
                // Usiamo setOnCheckedChangeListener(null) per evitare di ri-triggerare il listener
                binding.switchPeriodicBackup.setOnCheckedChangeListener(null)
                binding.switchPeriodicBackup.isChecked = state.isPeriodicBackupEnabled
                binding.switchPeriodicBackup.setOnCheckedChangeListener { _, isChecked ->
                    togglePeriodicBackup(isChecked)
                }
                
                // Gestione dei messaggi "Toast" una tantum
                state.toastMessage?.let {
                    Toast.makeText(this@SettingsActivity, it, Toast.LENGTH_SHORT).show()
                    vm.toastMessageShown() // Notifica il ViewModel che il messaggio è stato mostrato
                }
            }
        }
    }

    private fun togglePeriodicBackup(enable: Boolean) {
        val workManager = WorkManager.getInstance(applicationContext)
        val workName = "PeriodicLogExport"
        
        if (enable) {
            val periodicExportRequest = PeriodicWorkRequestBuilder<PeriodicExportWorker>(24, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.REPLACE, // REPLACE è spesso meglio di KEEP
                periodicExportRequest
            )
            Toast.makeText(this, "Backup periodico abilitato", Toast.LENGTH_SHORT).show()
        } else {
            workManager.cancelUniqueWork(workName)
            Toast.makeText(this, "Backup periodico disabilitato", Toast.LENGTH_SHORT).show()
        }
        vm.setPeriodicBackupState(enable) // Salva lo stato
    }
    
    private fun launchGoogleSignIn() {
        val signInIntent = vm.getGoogleSignInClient().signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gestisce il pulsante "indietro" nella toolbar
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
