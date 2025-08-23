package com.heimdall.chronolog.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import java.io.InputStream
import java.io.OutputStream
import java.security.NoSuchAlgorithmException
import java.security.KeyStore
import java.security.spec.InvalidKeySpecException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import timber.log.Timber

class CryptoManager(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

 private fun getBackupPasswordKey(): SecretKey {
 val existingKey = keyStore.getEntry(BACKUP_PASSWORD_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
 return existingKey?.secretKey ?: createBackupPasswordKey()
 }

 private val backupPasswordEncryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
 init(Cipher.ENCRYPT_MODE, getBackupPasswordKey())
 }


    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
            )
        }.generateKey()
    }

 private fun createBackupPasswordKey(): SecretKey {
 return KeyGenerator.getInstance(ALGORITHM).apply {
 init(
 KeyGenParameterSpec.Builder(
 BACKUP_PASSWORD_KEY_ALIAS,
 KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
 )
 .setBlockModes(BLOCK_MODE)
 .setEncryptionPaddings(PADDING)
 .setUserAuthenticationRequired(false) // Key does not require user authentication
 .build()
 )
 }.generateKey()
 }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun deriveKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_SIZE)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
 return factory.generateSecret(spec)
    }
    
    fun encryptBackup(bytes: ByteArray, password: CharArray, outputStream: OutputStream): Result<Pair<ByteArray, ByteArray>> {
        return try {
            val salt = ByteArray(SALT_SIZE).also {
                java.security.SecureRandom().nextBytes(it)
            }
            val derivedKey = deriveKeyFromPassword(password, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, derivedKey)
            }
            val iv = cipher.iv

            outputStream.use {
                it.write(salt) // Write salt first
                it.write(iv) // Then write IV
                val encryptedBytes = cipher.doFinal(bytes)
                it.write(encryptedBytes)
                Result.success(Pair(salt, iv))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error encrypting backup data")
            Result.failure(e)
        }
    }

    fun decryptBackup(inputStream: InputStream, password: CharArray): Result<ByteArray> {
        return try {
            inputStream.use {
                val saltSize = SALT_SIZE
                val salt = ByteArray(saltSize)
                val readSalt = it.read(salt)
                if (readSalt != saltSize) {
                    throw IOException("Failed to read salt from input stream.")
                }

                val ivSize = Cipher.getInstance(TRANSFORMATION).iv.size
                val iv = ByteArray(ivSize)
                val readIv = it.read(iv)
                 if (readIv != ivSize) {
                    throw IOException("Failed to read IV from input stream.")
                }

                val derivedKey = deriveKeyFromPassword(password, salt)
                val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.DECRYPT_MODE, derivedKey, IvParameterSpec(iv))
                }
                Result.success(cipher.doFinal(it.readBytes()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error decrypting backup data")
            Result.failure(e)
        }
    }

    fun encryptBackupPassword(password: String): Result<ByteArray> {
        return try {
            val bytes = password.toByteArray()
            val outputStream = ByteArrayOutputStream()
            outputStream.use {
                val iv = backupPasswordEncryptCipher.iv
                it.write(iv)
                val encryptedBytes = backupPasswordEncryptCipher.doFinal(bytes)
                it.write(encryptedBytes)
            }
            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Timber.e(e, "Error encrypting backup password")
            Result.failure(e)
        }
    }

    fun decryptBackupPassword(encryptedPassword: ByteArray): Result<String> {
        return try {
            val inputStream = ByteArrayInputStream(encryptedPassword)
            inputStream.use {
                val ivSize = backupPasswordEncryptCipher.iv.size // Use iv size from encrypt cipher for consistency
                val iv = ByteArray(ivSize)
                val readIv = it.read(iv)
                 if (readIv != ivSize) {
                    throw IOException("Failed to read IV from input stream during password decryption.")
                }

                val encryptedBytes = it.readBytes()
                val decryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.DECRYPT_MODE, getBackupPasswordKey(), IvParameterSpec(iv))
                }
                Result.success(String(decryptCipher.doFinal(encryptedBytes)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error decrypting backup password")
            Result.failure(e)
        }
    }

    /**
     * Encrypts data using the key that requires user authentication.
     * @param bytes The data to encrypt.
     * @param outputStream The output stream to write the encrypted data to.
     * @return A Result containing the IV if successful, or a failure.
     */
    fun encrypt(bytes: ByteArray, outputStream: OutputStream): Result<ByteArray> {
        return try {
            val iv = encryptCipher.iv
            outputStream.use {
                it.write(iv)
                val encryptedBytes = encryptCipher.doFinal(bytes)
                it.write(encryptedBytes)
                Result.success(iv)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error encrypting data with user authenticated key")
            Result.failure(e)
        }
    }

    /**
     * Decrypts data using the key that requires user authentication.
     * @param inputStream The input stream containing the encrypted data.
     * @return A Result containing the decrypted data if successful, or a failure.
     */
    fun decrypt(inputStream: InputStream): Result<ByteArray> {
        return try {
            inputStream.use {
            val ivSize = encryptCipher.iv.size
            val iv = ByteArray(ivSize)
                val readIv = it.read(iv)
                if (readIv != ivSize) {
                    throw IOException("Failed to read IV from input stream during decryption.")
                }
            val encryptedBytes = it.readBytes()
                Result.success(getDecryptCipherForIv(iv).doFinal(encryptedBytes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error decrypting data with user authenticated key")
            Result.failure(e)
        }
    }

    /**
     * Provides the encrypt cipher for use with BiometricPrompt.CryptoObject.
     * Note: This cipher's init method has already been called with ENCRYPT_MODE and the authenticated key.
     */
    fun getEncryptCipher(): Cipher {
 return encryptCipher
    }

    /**
     * Provides a decrypt cipher initialized with a specific IV for use with BiometricPrompt.CryptoObject.
     * @param iv The Initialization Vector to use for decryption.
     * @return A Cipher instance initialized for decryption.
     */
    fun getDecryptCipher(iv: ByteArray): Cipher {
 return getDecryptCipherForIv(iv)
    }

    companion object {
        private const val KEY_ALIAS = "chronolog_secret_key" // Key for live data, requires user auth
        private const val BACKUP_PASSWORD_KEY_ALIAS = "chronolog_backup_password_key" // Key for backup password, no user auth
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10000 // Recommended minimum is 10,000, higher is better
        private const val KEY_SIZE = 256 // AES key size in bits
        private const val SALT_SIZE = 16 // Salt size in bytes (128 bits)
    }
}
