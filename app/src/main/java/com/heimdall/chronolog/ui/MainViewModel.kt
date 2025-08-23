package com.heimdall.chronolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.heimdall.chronolog.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
@HiltViewModel
class MainViewModel(private val repository: Repository) : ViewModel() {

    // Using asLiveData() to convert Flow<List<LogEntry>> to LiveData<List<LogEntry>>
    val allLogEntries = repository.allLogEntries.asLiveData()

    /**
     * Clears all log entries from the database.
     */
    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogEntries()
        }
    }
}
