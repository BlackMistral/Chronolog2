package com.heimdall.chronolog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heimdall.chronolog.util.DateConverter
import javax.inject.Singleton

@Singleton // Mark as Singleton for Hilt
@Database(entities = [LogEntry::class], version = 2, exportSchema = true) // Increment version and export schema
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao

 @Volatile
 private var INSTANCE: AppDatabase? = null
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

 fun getDatabase(context: Context): AppDatabase {
 // if the INSTANCE is not null, then return it,
 // if it is, then create the database
 return INSTANCE ?: synchronized(this) {
 val instance = Room.databaseBuilder(
 context.applicationContext,
 AppDatabase::class.java,
 "chronolog_database"
 )
 //.addMigrations(MIGRATION_1_2) // Add your migrations here
 .build()
 INSTANCE = instance
 // return instance
 return instance
 }
 }
    }
}