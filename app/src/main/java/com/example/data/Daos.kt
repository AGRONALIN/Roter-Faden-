package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArgumentDao {
    @Query("SELECT * FROM arguments ORDER BY lastAccessed DESC")
    fun getRecentArguments(): Flow<List<Argument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArgument(argument: Argument)

    @Delete
    suspend fun deleteArgument(argument: Argument)

    @Query("SELECT * FROM arguments WHERE antiMarxistStatement LIKE '%' || :query || '%' OR marxistCounterArgument LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY lastAccessed DESC")
    fun searchArguments(query: String): Flow<List<Argument>>
}

@Dao
interface GlossaryDao {
    @Query("SELECT * FROM glossary_items ORDER BY term ASC")
    fun getAllGlossaryItems(): Flow<List<GlossaryItem>>

    @Query("SELECT * FROM glossary_items ORDER BY lastAccessed DESC")
    fun getRecentGlossaryItems(): Flow<List<GlossaryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlossary(item: GlossaryItem)

    @Delete
    suspend fun deleteGlossary(item: GlossaryItem)

    @Query("SELECT * FROM glossary_items WHERE id = :id")
    suspend fun getGlossaryItemById(id: Int): GlossaryItem?

    @Query("SELECT * FROM glossary_items WHERE term LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' ORDER BY term ASC")
    fun searchGlossary(query: String): Flow<List<GlossaryItem>>
}

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: String)
}

@Dao
interface LiteratureSummaryDao {
    @Query("SELECT * FROM literature_summaries")
    fun getAllSummaries(): Flow<List<LiteratureSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: LiteratureSummary)
}

