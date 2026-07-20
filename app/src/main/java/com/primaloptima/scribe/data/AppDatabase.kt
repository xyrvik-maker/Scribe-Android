package com.primaloptima.scribe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class, Folder::class, WorldEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun worldEntryDao(): WorldEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scribe.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Insert the "/" root folder on first creation.
                            db.execSQL("INSERT OR IGNORE INTO folders (path) VALUES ('/')")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
