package com.heimdall.chronolog.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heimdall.chronolog.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var exportButton: Button
    private lateinit var importButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        notificationSwitch = findViewById(R.id.switch_notification)
        exportButton = findViewById(R.id.button_export)
        importButton = findViewById(R.id.button_import)

        // TODO: Load initial settings state (e.g., notification enabled/disabled)
        // notificationSwitch.isChecked = settingsManager.isNotificationEnabled()

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification setting
            // settingsManager.setNotificationEnabled(isChecked)
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        exportButton.setOnClickListener {
            // TODO: Implement log export functionality
            Toast.makeText(this, "Export logs (placeholder)", Toast.LENGTH_SHORT).show()
        }

        importButton.setOnClickListener {
            // TODO: Implement log import functionality
            Toast.makeText(this, "Import logs (placeholder)", Toast.LENGTH_SHORT).show()
        }
    }
}
