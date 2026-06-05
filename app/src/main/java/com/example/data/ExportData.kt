package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportData(
    val arguments: List<ArgumentExport>,
    val glossary: List<GlossaryExport>,
    val literature: List<LiteratureExport> = emptyList()
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

@JsonClass(generateAdapter = true)
data class LiteratureExport(
    val title: String,
    val author: String,
    val summary: String
)
