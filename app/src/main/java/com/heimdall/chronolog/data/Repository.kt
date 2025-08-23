package com.heimdall.chronolog.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import androidx.room.OnConflictStrategy
import javax.inject.Singleton
import timber.log.Timber

class Repository(private val logDao: LogDao) {

    val allLogEntries: Flow<List<LogEntry>> = logDao.getAllLogEntries()

    suspend fun insertLogEntry(logEntry: LogEntry): Long {
 try {
 return logDao.insert(logEntry)
 } catch (e: Exception) {
 Timber.e(e, "Error inserting log entry: ${logEntry.message}")
 throw e // Re-throw the exception to be handled by the caller
 }
    }

    suspend fun getLogEntryById(id: Long): LogEntry? {
 try {
 return logDao.getLogEntryById(id)
 } catch (e: Exception) {
 Timber.e(e, "Error getting log entry by ID: $id")
 throw e // Re-throw the exception
 }
    }

 suspend fun insertLogEntries(logEntries: List<LogEntry>, conflictStrategy: Int = OnConflictStrategy.IGNORE) {
 try {
 logDao.insertAll(logEntries, conflictStrategy)
 } catch (e: Exception) {
 Timber.e(e, "Error inserting multiple log entries. Count: ${logEntries.size}")
 throw e // Re-throw the exception
 }
 }

    suspend fun clearAllLogEntries() {
 try {
 logDao.clearAll()
 } catch (e: Exception) {
 Timber.e(e, "Error clearing all log entries.")
 throw e // Re-throw the exception
 }
    }

    @Inject
 @Singleton
 constructor(logDao: LogDao) : this(logDao)
}
