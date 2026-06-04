package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Argument::class, GlossaryItem::class, SongEntity::class, LiteratureSummary::class, LiteratureItem::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun argumentDao(): ArgumentDao
    abstract fun glossaryDao(): GlossaryDao
    abstract fun songDao(): SongDao
    abstract fun literatureSummaryDao(): LiteratureSummaryDao
    abstract fun literatureDao(): LiteratureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debate_glossary_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // ensures upgrades during dev don't crash
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
