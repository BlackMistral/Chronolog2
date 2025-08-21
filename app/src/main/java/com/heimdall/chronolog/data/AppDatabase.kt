package com.heimdall.chronolog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heimdall.chronolog.util.DateConverter

@Database(entities = [LogEntry::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronolog_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not covered in this codelab.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
