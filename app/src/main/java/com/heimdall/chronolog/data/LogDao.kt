package com.heimdall.chronolog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLogEntry(logEntry: LogEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogEntries(logEntries: List<LogEntry>)

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogEntries(): Flow<List<LogEntry>>
    @Query("DELETE FROM log_entries")
    suspend fun deleteAllLogEntries()
}
