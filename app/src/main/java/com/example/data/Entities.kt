package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arguments")
data class Argument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val antiMarxistStatement: String,
    val marxistCounterArgument: String,
    val category: String = "",
    val lastAccessed: Long = System.currentTimeMillis(),
    val lastEdited: Long = lastAccessed
)

@Entity(tableName = "glossary_items")
data class GlossaryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val term: String,
    val definition: String,
    val lastAccessed: Long = System.currentTimeMillis(),
    val lastEdited: Long = lastAccessed
)

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uriString: String
)

@Entity(tableName = "literature_summaries")
data class LiteratureSummary(
    @PrimaryKey val id: Int,
    val summary: String
)
