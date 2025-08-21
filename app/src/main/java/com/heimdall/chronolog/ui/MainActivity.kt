package com.heimdall.chronolog.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heimdall.chronolog.R
import com.heimdall.chronolog.data.AppDatabase
import com.heimdall.chronolog.data.Repository
import com.heimdall.chronolog.databinding.ActivityMainBinding
import com.heimdall.chronolog.databinding.DialogProgressBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(Repository(AppDatabase.getDatabase(applicationContext).logDao()))
    }

    private lateinit var logsAdapter: LogsAdapter
    private lateinit var uiStateTextView: TextView // Assuming a TextView to show UI state

    // TODO: Implement actual progress dialog management
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent screenshots of the activity
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setSupportActionBar(binding.toolbar)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.recyclerViewLogs
        logsAdapter = LogsAdapter()
        recyclerView.adapter = logsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe LiveData from ViewModel
        mainViewModel.allLogEntries.observe(this) { logEntries ->
            logEntries?.let {
                logsAdapter.submitList(it)
                // Update UI state based on whether data is LIVE or IMPORTED (placeholder logic)
                updateUiState(if (it.isNotEmpty()) "LIVE" else "LIVE (No Entries)")
            }
        }

        uiStateTextView = binding.textViewUiState // Assuming a TextView with this ID in activity_main.xml

        // TODO: Implement logic to switch between LIVE and IMPORTED states and update ViewModel/Repository accordingly
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            // TODO: Add other menu items here (e.g., clear logs)
            R.id.action_clear_logs -> {
                showClearLogsConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUiState(state: String) {
        uiStateTextView.text = "UI State: $state"
    }

    private fun showClearLogsConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Logs")
            .setMessage("Are you sure you want to clear all log entries? This action cannot be undone.")
            .setPositiveButton("Clear") { dialog, _ ->
                mainViewModel.clearAllLogs()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    // TODO: Implement showProgressDialog and hideProgressDialog
    private fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            val dialogBinding = DialogProgressBinding.inflate(layoutInflater)
            dialogBinding.progressMessage.text = message
            progressDialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()
        }
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}
