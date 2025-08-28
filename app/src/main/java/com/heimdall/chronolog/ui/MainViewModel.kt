package com.heimdall.chronolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heimdall.chronolog.data.LogEntry
import com.heimdall.chronolog.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val logs: StateFlow<List<LogEntry>> = repository.dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getLogsOnce(): List<LogEntry> {
        return repository.dao.getLogsAsList()
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.dao.clearAll()
        }
    }
}
