package com.heimdall.chronolog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

@Entity(tableName = "log_entries", indices = [Index(value = ["timestamp"])])
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val message: String,
    val hashedMessage: String // Store a hash of the original message
) {
    // Note: Hashing mechanism will be implemented elsewhere (e.g., Repository or Worker)
    // to keep this data class clean and focused on data structure.
    // The hashedMessage field is included here to ensure it's stored in the database.
}
