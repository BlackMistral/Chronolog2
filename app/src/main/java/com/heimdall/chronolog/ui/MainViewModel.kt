package com.heimdall.chronolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.heimdall.chronolog.data.Repository
import kotlinx.coroutines.launch

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

// ViewModelFactory to instantiate MainViewModel with Repository dependency
class MainViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
