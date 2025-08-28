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

data class SettingsUiState(
    val isNotificationListenerEnabled: Boolean = false,
    val googleAccount: GoogleSignInAccount? = null,
    val isPeriodicBackupEnabled: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application
    // Inietta qui altri repository se necessario, es. per DataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        // Aggiungi qui altri scope se ti servono, es. Drive
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(app, gso)

    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient

    fun checkInitialState() {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(app)
        // TODO: Leggi lo stato di isPeriodicBackupEnabled da DataStore
        _uiState.update {
            it.copy(
                googleAccount = lastSignedInAccount,
                isPeriodicBackupEnabled = false // Sostituisci con il valore da DataStore
            )
        }
        checkNotificationListenerPermission()
    }
    
    fun checkNotificationListenerPermission() {
        // La logica per controllare il permesso...
        _uiState.update { it.copy(isNotificationListenerEnabled = true /* ... */) }
    }

    fun handleSignInSuccess(account: GoogleSignInAccount?) {
        _uiState.update {
            it.copy(
                googleAccount = account,
                toastMessage = if (account != null) "Accesso riuscito" else null
            )
        }
    }
    
    fun handleSignInFailure() {
        _uiState.update { it.copy(toastMessage = "Accesso fallito") }
    }

    fun setPeriodicBackupState(isEnabled: Boolean) {
        // TODO: Salva questo stato su DataStore
        _uiState.update { it.copy(isPeriodicBackupEnabled = isEnabled) }
    }

    fun toastMessageShown() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
