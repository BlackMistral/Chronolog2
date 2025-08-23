package com.heimdall.chronolog.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class BackupPasswordManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferencesName = "backup_prefs"
    private val encryptedPasswordKey = "encrypted_backup_password"

    private val sharedPreferences = EncryptedSharedPreferences.create(
        sharedPreferencesName,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Saves the encrypted backup password securely.
     * @param encryptedPassword The password bytes encrypted by CryptoManager.
     */
    fun saveEncryptedPassword(encryptedPassword: ByteArray) {
        sharedPreferences.edit()
            .putString(encryptedPasswordKey, encryptedPassword.encodeToString())
            .apply()
    }

    /**
     * Retrieves the encrypted backup password.
     * @return The encrypted password bytes, or null if not found.
     */
    fun getEncryptedPassword(): ByteArray? {
        val encryptedPasswordString = sharedPreferences.getString(encryptedPasswordKey, null)
        return encryptedPasswordString?.decodeToByteArray()
    }

    /**
     * Clears the stored encrypted password.
     */
    fun clearEncryptedPassword() {
        sharedPreferences.edit()
            .remove(encryptedPasswordKey)
            .apply()
    }
}