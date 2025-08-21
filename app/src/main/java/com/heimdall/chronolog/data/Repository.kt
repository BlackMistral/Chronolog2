package com.heimdall.chronolog.data

import kotlinx.coroutines.flow.Flow

class Repository(private val logDao: LogDao) {

    val allLogEntries: Flow<List<LogEntry>> = logDao.getAllLogEntries()

    suspend fun insertLogEntry(logEntry: LogEntry): Long {
        return logDao.insert(logEntry)
    }

    suspend fun getLogEntryById(id: Long): LogEntry? {
        return logDao.getLogEntryById(id)
    }

    suspend fun clearAllLogEntries() {
        logDao.clearAll()
    }
}
