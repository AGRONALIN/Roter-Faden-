package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    private val argumentDao = database.argumentDao()
    private val glossaryDao = database.glossaryDao()

    // Arguments
    val recentArguments: Flow<List<Argument>> = argumentDao.getRecentArguments()

    suspend fun insertArgument(argument: Argument) {
        argumentDao.insertArgument(argument)
    }

    suspend fun deleteArgument(argument: Argument) {
        argumentDao.deleteArgument(argument)
    }

    fun searchArguments(query: String): Flow<List<Argument>> {
        return argumentDao.searchArguments(query)
    }

    // Glossary
    val allGlossaryItems: Flow<List<GlossaryItem>> = glossaryDao.getAllGlossaryItems()
    val recentGlossaryItems: Flow<List<GlossaryItem>> = glossaryDao.getRecentGlossaryItems()

    suspend fun insertGlossary(item: GlossaryItem) {
        glossaryDao.insertGlossary(item)
    }

    suspend fun deleteGlossary(item: GlossaryItem) {
        glossaryDao.deleteGlossary(item)
    }

    suspend fun getGlossaryItemById(id: Int): GlossaryItem? {
        return glossaryDao.getGlossaryItemById(id)
    }

    fun searchGlossary(query: String): Flow<List<GlossaryItem>> {
        return glossaryDao.searchGlossary(query)
    }
}
