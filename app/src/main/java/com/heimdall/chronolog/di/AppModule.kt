package com.heimdall.chronolog.di

import android.content.Context
import androidx.room.Room
import com.heimdall.chronolog.data.AppDatabase
import com.heimdall.chronolog.data.LogDao
import com.heimdall.chronolog.data.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.heimdall.chronolog.core.CryptoManager
import com.heimdall.chronolog.backup.BackupPasswordManager
import com.heimdall.chronolog.google.GoogleDriveBackupManager
import com.heimdall.chronolog.core.NotificationCaptureService // Although service is entry point, its dependencies can be provided
import javax.inject.Singleton
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chronolog_database"
        ).build()
        // TODO: Implement database migrations instead of relying on destructive migration.
        // Currently, schema changes will result in data loss.
    }

    @Provides
    fun provideLogDao(appDatabase: AppDatabase): LogDao {
        return appDatabase.logDao()
    }

    @Provides
    @Singleton
    fun provideRepository(logDao: LogDao): Repository {
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
        return GoogleDriveBackupManager(context, Executors.newSingleThreadExecutor())
    }
}
