package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportData(
    val arguments: List<ArgumentExport>,
    val glossary: List<GlossaryExport>
)

@JsonClass(generateAdapter = true)
data class ArgumentExport(
    val antiMarxistStatement: String,
    val marxistCounterArgument: String,
    val category: String = "",
    val lastAccessed: Long = 0L
)

@JsonClass(generateAdapter = true)
data class GlossaryExport(
    val term: String,
    val definition: String
)
