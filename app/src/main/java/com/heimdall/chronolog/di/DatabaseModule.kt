package com.heimdall.chronolog.di

import android.content.Context
import androidx.room.Room
import com.heimdall.chronolog.data.AppDatabase
import com.heimdall.chronolog.data.LogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "chronolog_database"
        )
        // Add your database migrations here.
        // For now, keeping the structure for MIGRATION_1_2 defined in AppDatabase
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    }

    @Provides
    fun provideLogDao(db: AppDatabase): LogDao {
        return db.logDao()
    }
}