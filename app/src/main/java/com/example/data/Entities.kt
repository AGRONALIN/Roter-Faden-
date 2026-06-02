package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arguments")
data class Argument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val antiMarxistStatement: String,
    val marxistCounterArgument: String,
    val category: String = "",
    val lastAccessed: Long = System.currentTimeMillis()
)

@Entity(tableName = "glossary_items")
data class GlossaryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val term: String,
    val definition: String,
    val lastAccessed: Long = System.currentTimeMillis()
)
