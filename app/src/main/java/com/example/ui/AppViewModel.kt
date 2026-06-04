package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.Argument
import com.example.data.GlossaryItem
import com.squareup.moshi.Moshi
import com.example.data.ArgumentExport
import com.example.data.GlossaryExport
import com.example.data.ExportData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TooltipState(
    val isVisible: Boolean = false,
    val definition: String = "",
    val wordBounds: Rect = Rect.Zero
)

sealed class AppOverlay {
    object None : AppOverlay()
    data class ArgumentDetail(val argId: Int) : AppOverlay()
    data class GlossaryDetail(val termId: Int) : AppOverlay()
    data class LiteratureDetail(val litId: Int) : AppOverlay()
    object Mediathek : AppOverlay()
}

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    private val _appOverlay = MutableStateFlow<AppOverlay>(AppOverlay.None)
    val appOverlay: StateFlow<AppOverlay> = _appOverlay.asStateFlow()

    fun setOverlay(overlay: AppOverlay) {
        _appOverlay.value = overlay
    }
    
    fun closeOverlay() {
        _appOverlay.value = AppOverlay.None
    }
    val isDarkTheme: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    val recentArguments: StateFlow<List<Argument>> = repository.recentArguments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val glossaryItems: StateFlow<List<GlossaryItem>> = repository.allGlossaryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val recentGlossaryItems: StateFlow<List<GlossaryItem>> = repository.recentGlossaryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()

    fun setBottomBarVisible(visible: Boolean) {
        _isBottomBarVisible.value = visible
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _focusSearchEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val focusSearchEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _focusSearchEvent.asSharedFlow()

    fun triggerSearchFocus() {
        viewModelScope.launch {
            _focusSearchEvent.emit(Unit)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Argument>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchArguments(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchGlossaryResults: StateFlow<List<GlossaryItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchGlossary(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tooltipState = MutableStateFlow(TooltipState())
    val tooltipState: StateFlow<TooltipState> = _tooltipState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showTooltip(definition: String, wordBounds: Rect) {
        _tooltipState.value = TooltipState(isVisible = true, definition = definition, wordBounds = wordBounds)
    }

    fun hideTooltip() {
        _tooltipState.value = TooltipState(isVisible = false)
    }

    fun insertArgument(antiMarxist: String, marxist: String, category: String) {
        viewModelScope.launch {
            repository.insertArgument(Argument(
                antiMarxistStatement = antiMarxist,
                marxistCounterArgument = marxist,
                category = category
            ))
        }
    }

    fun updateArgumentLastAccessed(argument: Argument) {
        viewModelScope.launch {
            repository.insertArgument(argument.copy(lastAccessed = System.currentTimeMillis()))
        }
    }

    fun updateCategoryLastAccessed(category: String) {
        viewModelScope.launch {
            val mostRecentArg = recentArguments.value
                .filter { it.category == category }
                .maxByOrNull { it.lastAccessed }
            if (mostRecentArg != null) {
                repository.insertArgument(mostRecentArg.copy(lastAccessed = System.currentTimeMillis()))
            }
        }
    }
    
    fun updateArgument(argument: Argument) {
        viewModelScope.launch {
            repository.insertArgument(argument)
        }
    }
    
    fun deleteArgument(argument: Argument) {
        viewModelScope.launch {
            repository.deleteArgument(argument)
        }
    }

    fun deleteArgumentsById(ids: Set<Int>) {
        viewModelScope.launch {
            val toDelete = recentArguments.value.filter { it.id in ids }
            toDelete.forEach { repository.deleteArgument(it) }
        }
    }

    fun insertGlossary(term: String, definition: String) {
        viewModelScope.launch {
            repository.insertGlossary(GlossaryItem(term = term, definition = definition))
        }
    }
    
    fun updateGlossaryLastAccessed(item: GlossaryItem) {
        viewModelScope.launch {
            repository.insertGlossary(item.copy(lastAccessed = System.currentTimeMillis()))
        }
    }
    
    fun updateGlossary(item: GlossaryItem) {
        viewModelScope.launch {
            repository.insertGlossary(item)
        }
    }
    
    fun deleteGlossary(item: GlossaryItem) {
        viewModelScope.launch {
            repository.deleteGlossary(item)
        }
    }

    suspend fun exportData(): String {
        val moshi = Moshi.Builder().build()
        val arguments = recentArguments.value.map { 
            ArgumentExport(it.antiMarxistStatement, it.marxistCounterArgument, it.category, it.lastAccessed) 
        }
        val glossary = glossaryItems.value.map { 
            GlossaryExport(it.term, it.definition) 
        }
        val data = ExportData(arguments, glossary)
        val adapter = moshi.adapter(ExportData::class.java)
        return adapter.toJson(data)
    }

    suspend fun exportArguments(ids: Set<Int>): String {
        val moshi = Moshi.Builder().build()
        val arguments = recentArguments.value.filter { it.id in ids }.map {
            ArgumentExport(it.antiMarxistStatement, it.marxistCounterArgument, it.category, it.lastAccessed)
        }
        val data = ExportData(arguments, emptyList())
        val adapter = moshi.adapter(ExportData::class.java)
        return adapter.toJson(data)
    }

    suspend fun exportSingleArgument(id: Int): String {
        return exportArguments(setOf(id))
    }

    suspend fun exportGlossaries(ids: Set<Int>): String {
        val moshi = Moshi.Builder().build()
        val glossary = glossaryItems.value.filter { it.id in ids }.map {
            GlossaryExport(it.term, it.definition)
        }
        val data = ExportData(emptyList(), glossary)
        val adapter = moshi.adapter(ExportData::class.java)
        return adapter.toJson(data)
    }

    suspend fun exportSingleGlossary(id: Int): String {
        return exportGlossaries(setOf(id))
    }

    suspend fun importData(jsonString: String) {
        try {
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(ExportData::class.java)
            val parsed = adapter.fromJson(jsonString) ?: return
            
            parsed.arguments.forEach {
                repository.insertArgument(
                    Argument(
                        antiMarxistStatement = it.antiMarxistStatement,
                        marxistCounterArgument = it.marxistCounterArgument,
                        category = it.category,
                        lastAccessed = it.lastAccessed
                    )
                )
            }
            parsed.glossary.forEach {
                repository.insertGlossary(
                    GlossaryItem(
                        term = it.term,
                        definition = it.definition
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val songsList: StateFlow<List<com.example.data.SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertSong(id: String, title: String, artist: String, durationMs: Long, uriString: String) {
        viewModelScope.launch {
            repository.insertSong(com.example.data.SongEntity(id, title, artist, durationMs, uriString))
        }
    }

    fun deleteSong(id: String) {
        viewModelScope.launch {
            repository.deleteSong(id)
        }
    }

    val literatureSummaries: StateFlow<List<com.example.data.LiteratureSummary>> = repository.allLiteratureSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLiteratureSummary(id: Int, summary: String) {
        viewModelScope.launch {
            repository.insertLiteratureSummary(com.example.data.LiteratureSummary(id, summary))
        }
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
