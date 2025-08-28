package com.heimdall.chronolog.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ... (La classe data SettingsUiState rimane invariata) ...

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application
    // Inietta qui altri repository se Hilt sa come fornirli
) : ViewModel() {

    // ... (Il resto della logica del ViewModel rimane invariata) ...
}
