package com.heimdall.chronolog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heimdall.chronolog.util.DateConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Singleton

@Singleton // Mark as Singleton for Hilt
@Database(entities = [LogEntry::class], version = 2, exportSchema = true) // Increment version and export schema
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao

    companion object {
        // TODO: Implement your Room Migrations here.
        // Example migration from version 1 to 2:
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add your database migration logic here.
                // Example: Adding a new column to the log_entries table
                // database.execSQL("ALTER TABLE log_entries ADD COLUMN new_column_name INTEGER NOT NULL DEFAULT 0")
                // Consult the Room Migration documentation for detailed instructions:
                // https://developer.android.com/training/data-storage/room/migrations
            }
        }
    }
}