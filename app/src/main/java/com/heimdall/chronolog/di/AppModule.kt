package com.heimdall.chronolog.di

import android.content.Context
import com.heimdall.chronolog.data.LogDao
import com.heimdall.chronolog.data.Repository
import com.heimdall.chronolog.core.CryptoManager
import com.heimdall.chronolog.backup.BackupPasswordManager // Assumendo esista
import com.heimdall.chronolog.google.GoogleDriveBackupManager // Assumendo esista
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Il LogDao viene fornito dal DatabaseModule, quindi lo riceviamo qui
    @Provides
    @Singleton
    fun provideRepository(logDao: LogDao): Repository {
        // Hilt sa già come creare LogDao grazie al DatabaseModule
        return Repository(logDao)
    }

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }

    @Provides
    @Singleton
    fun provideBackupPasswordManager(@ApplicationContext context: Context): BackupPasswordManager {
        return BackupPasswordManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveBackupManager(@ApplicationContext context: Context): GoogleDriveBackupManager {
        // Se GoogleDriveBackupManager ha bisogno di altro, andrebbe aggiunto qui
        return GoogleDriveBackupManager(context)
    }
}
