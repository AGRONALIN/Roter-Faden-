package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    private val argumentDao = database.argumentDao()
    private val glossaryDao = database.glossaryDao()
    private val songDao = database.songDao()
    private val literatureSummaryDao = database.literatureSummaryDao()
    private val literatureDao = database.literatureDao()

    // Literature items
    val allLiteratureItems: Flow<List<LiteratureItem>> = literatureDao.getAllLiterature()

    suspend fun insertLiterature(item: LiteratureItem) {
        literatureDao.insertLiterature(item)
    }

    suspend fun deleteLiterature(item: LiteratureItem) {
        literatureDao.deleteLiterature(item)
    }

    suspend fun getLiteratureItemById(id: Int): LiteratureItem? {
        return literatureDao.getLiteratureItemById(id)
    }

    // Songs
    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()

    suspend fun insertSong(song: SongEntity) {
        songDao.insertSong(song)
    }

    suspend fun deleteSong(id: String) {
        songDao.deleteSongById(id)
    }

    // Literature summaries
    val allLiteratureSummaries: Flow<List<LiteratureSummary>> = literatureSummaryDao.getAllSummaries()

    suspend fun insertLiteratureSummary(summary: LiteratureSummary) {
        literatureSummaryDao.insertSummary(summary)
    }

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
