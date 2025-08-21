package com.heimdall.chronolog.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heimdall.chronolog.R
import com.heimdall.chronolog.data.LogEntry
import java.text.SimpleDateFormat
import java.util.Locale

class LogsAdapter : ListAdapter<LogEntry, LogsAdapter.LogEntryViewHolder>(LogEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return LogEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogEntryViewHolder, position: Int) {
        val logEntry = getItem(position)
        holder.bind(logEntry)
    }

    class LogEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestampTextView: TextView = itemView.findViewById(R.id.text_view_timestamp)
        private val messageTextView: TextView = itemView.findViewById(R.id.text_view_log_message)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(logEntry: LogEntry) {
            timestampTextView.text = dateFormat.format(logEntry.timestamp)
            messageTextView.text = logEntry.message // Displaying original message for now
            // TODO: Consider if displaying the original message is appropriate for a "secure" app,
            // or if a summary/indicator should be used, or require authentication to view details.
        }
    }

    private class LogEntryDiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem == newItem
        }
    }
}
