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
import com.example.data.LiteratureExport
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

class AppViewModel(
    private val repository: AppRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {
    private val aiRepository: com.example.data.AiAssistantRepository = com.example.data.AiAssistantRepositoryImpl()

    val marxChatHistory = MutableStateFlow<List<com.example.data.ChatMessage>>(emptyList())
    val isMarxThinking = MutableStateFlow<Boolean>(false)
    val marxSpeechBubbleText = MutableStateFlow<String>("Heilige Allianz der Ausbeuter! Du hast den unerbittlichen Klassenkampf in Gang gesetzt, Genosse! Frag mich, was du willst, und ich zeige dir die marxistische Wahrheit!")

    val customMarxImagePath = MutableStateFlow<String?>(
        sharedPreferences.getString("custom_marx_image_path", null)
    )

    fun saveCustomMarxImage(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = java.io.File(context.filesDir, "custom_marx_image.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    val absolutePath = file.absolutePath
                    customMarxImagePath.value = absolutePath
                    sharedPreferences.edit().putString("custom_marx_image_path", absolutePath).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeCustomMarxImage(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val file = java.io.File(context.filesDir, "custom_marx_image.jpg")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            customMarxImagePath.value = null
            sharedPreferences.edit().remove("custom_marx_image_path").apply()
        }
    }

    fun askKarlMarx(question: String) {
        if (question.isBlank()) return
        val trimmed = question.trim()
        
        val updatedHistory = marxChatHistory.value + com.example.data.ChatMessage("user", trimmed)
        marxChatHistory.value = updatedHistory
        
        isMarxThinking.value = true
        marxSpeechBubbleText.value = "Karl Marx schärft seine Feder..."
        
        viewModelScope.launch {
            try {
                val response = aiRepository.getMarxResponse(trimmed, updatedHistory)
                marxSpeechBubbleText.value = response
                marxChatHistory.value = marxChatHistory.value + com.example.data.ChatMessage("model", response)
            } catch (e: Exception) {
                val errorMsg = "Kompagnon! Mein Gehirn streikt gerade aufgrund von akuter Ausbeutung: ${e.localizedMessage}"
                marxSpeechBubbleText.value = errorMsg
                marxChatHistory.value = marxChatHistory.value + com.example.data.ChatMessage("model", errorMsg)
            } finally {
                isMarxThinking.value = false
            }
        }
    }

    fun clearMarxChat() {
        marxChatHistory.value = emptyList()
        marxSpeechBubbleText.value = "Heilige Allianz der Ausbeuter! Du hast den unerbittlichen Klassenkampf in Gang gesetzt, Genosse! Frag mich, was du willst, und ich zeige dir die marxistische Wahrheit!"
        isMarxThinking.value = false
    }

    val isReturningFromMarx = MutableStateFlow<Boolean>(false)

    fun triggerReturnFromMarxTransition() {
        isReturningFromMarx.value = true
    }

    fun setReturningFromMarx(value: Boolean) {
        isReturningFromMarx.value = value
    }

    private val _appOverlay = MutableStateFlow<AppOverlay>(AppOverlay.None)
    val appOverlay: StateFlow<AppOverlay> = _appOverlay.asStateFlow()

    fun setOverlay(overlay: AppOverlay) {
        _appOverlay.value = overlay
    }
    
    fun closeOverlay() {
        _appOverlay.value = AppOverlay.None
    }
    val isDarkTheme: MutableStateFlow<Boolean> = MutableStateFlow(
        sharedPreferences.getBoolean("is_dark_theme", false)
    )

    val showOnboarding: MutableStateFlow<Boolean> = MutableStateFlow(
        !sharedPreferences.getBoolean("onboarding_completed", false)
    )

    fun completeOnboarding() {
        showOnboarding.value = false
        sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun resetOnboarding() {
        showOnboarding.value = true
        sharedPreferences.edit().putBoolean("onboarding_completed", false).apply()
    }

    fun toggleTheme() {
        val nextValue = !isDarkTheme.value
        isDarkTheme.value = nextValue
        sharedPreferences.edit().putBoolean("is_dark_theme", nextValue).apply()
    }

    val githubRepoPath: MutableStateFlow<String> = MutableStateFlow(
        sharedPreferences.getString("github_repo_path", "AGRONALIN/Roter-Faden-") ?: "AGRONALIN/Roter-Faden-"
    )

    fun updateGithubRepoPath(path: String) {
        val trimmed = path.trim()
        githubRepoPath.value = trimmed
        sharedPreferences.edit().putString("github_repo_path", trimmed).apply()
    }

    data class RepoConfig(
        val owner: String,
        val name: String,
        val branch: String,
        val apkPath: String
    )

    fun parseRepoPath(input: String): RepoConfig {
        var owner = "AGRONALIN"
        var name = "Roter-Faden-"
        var branch = "main"
        var apkPath = ".build-outputs/app-debug.apk"

        val sanitized = input.trim()
        if (sanitized.startsWith("http://") || sanitized.startsWith("https://")) {
            try {
                val url = java.net.URL(sanitized)
                val host = url.host
                val pathParts = url.path.split("/").filter { it.isNotEmpty() }
                if (host.contains("github.com")) {
                    if (pathParts.size >= 2) {
                        owner = pathParts[0]
                        name = pathParts[1]
                    }
                    if (pathParts.size >= 5 && pathParts[2] == "blob") {
                        branch = pathParts[3]
                        apkPath = pathParts.subList(4, pathParts.size).joinToString("/")
                    }
                } else if (host.contains("raw.githubusercontent.com")) {
                    if (pathParts.size >= 2) {
                        owner = pathParts[0]
                        name = pathParts[1]
                    }
                    if (pathParts.size >= 4) {
                        branch = pathParts[2]
                        apkPath = pathParts.subList(3, pathParts.size).joinToString("/")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val parts = sanitized.split("/")
            if (parts.size >= 2) {
                owner = parts[0]
                name = parts[1]
            }
            if (parts.size >= 3) {
                branch = parts[2]
            }
        }
        return RepoConfig(owner, name, branch, apkPath)
    }

    val recentArguments: StateFlow<List<Argument>> = repository.recentArguments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val glossaryItems: StateFlow<List<GlossaryItem>> = repository.allGlossaryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val recentGlossaryItems: StateFlow<List<GlossaryItem>> = repository.recentGlossaryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingImportData = MutableStateFlow<ExportData?>(null)
    val pendingImportData: StateFlow<ExportData?> = _pendingImportData.asStateFlow()

    fun setPendingImportData(data: ExportData?) {
        _pendingImportData.value = data
    }

    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()

    private val _collectionSelectedTabIndex = MutableStateFlow(0)
    val collectionSelectedTabIndex: StateFlow<Int> = _collectionSelectedTabIndex.asStateFlow()

    fun setCollectionSelectedTabIndex(index: Int) {
        _collectionSelectedTabIndex.value = index
    }

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

    fun insertArgument(antiMarxist: String, marxist: String, category: String, imagePath: String? = null) {
        viewModelScope.launch {
            repository.insertArgument(Argument(
                antiMarxistStatement = antiMarxist,
                marxistCounterArgument = marxist,
                category = category,
                imagePath = imagePath
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
            repository.insertArgument(argument.copy(lastEdited = System.currentTimeMillis()))
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

    fun insertGlossary(term: String, definition: String, imagePath: String? = null) {
        viewModelScope.launch {
            repository.insertGlossary(GlossaryItem(term = term, definition = definition, imagePath = imagePath))
        }
    }
    
    fun updateGlossaryLastAccessed(item: GlossaryItem) {
        viewModelScope.launch {
            repository.insertGlossary(item.copy(lastAccessed = System.currentTimeMillis()))
        }
    }
    
    fun updateGlossary(item: GlossaryItem) {
        viewModelScope.launch {
            repository.insertGlossary(item.copy(lastEdited = System.currentTimeMillis()))
        }
    }
    
    fun deleteGlossary(item: GlossaryItem) {
        viewModelScope.launch {
            repository.deleteGlossary(item)
        }
    }

    suspend fun exportData(): String {
        val arguments = recentArguments.value.map { 
            ArgumentExport(it.antiMarxistStatement, it.marxistCounterArgument, it.category, it.lastAccessed) 
        }
        val glossary = glossaryItems.value.map { 
            GlossaryExport(it.term, it.definition) 
        }
        val literature = literatureList.value.map {
            LiteratureExport(it.title, it.author, it.summary)
        }
        val data = ExportData(arguments, glossary, literature)
        return com.example.data.ExportData.adapter.toJson(data)
    }

    suspend fun exportArguments(ids: Set<Int>): String {
        val arguments = recentArguments.value.filter { it.id in ids }.map {
            ArgumentExport(it.antiMarxistStatement, it.marxistCounterArgument, it.category, it.lastAccessed)
        }
        val data = ExportData(arguments, emptyList(), emptyList())
        return com.example.data.ExportData.adapter.toJson(data)
    }

    suspend fun exportSingleArgument(id: Int): String {
        return exportArguments(setOf(id))
    }

    suspend fun exportGlossaries(ids: Set<Int>): String {
        val glossary = glossaryItems.value.filter { it.id in ids }.map {
            GlossaryExport(it.term, it.definition)
        }
        val data = ExportData(emptyList(), glossary, emptyList())
        return com.example.data.ExportData.adapter.toJson(data)
    }

    suspend fun exportSingleGlossary(id: Int): String {
        return exportGlossaries(setOf(id))
    }

    suspend fun exportSelectedItems(argumentIds: Set<Int>, glossaryIds: Set<Int>, literatureIds: Set<Int>): String {
        val arguments = recentArguments.value.filter { it.id in argumentIds }.map {
            ArgumentExport(it.antiMarxistStatement, it.marxistCounterArgument, it.category, it.lastAccessed)
        }
        val glossary = glossaryItems.value.filter { it.id in glossaryIds }.map {
            GlossaryExport(it.term, it.definition)
        }
        val literature = literatureList.value.filter { it.id in literatureIds }.map {
            LiteratureExport(it.title, it.author, it.summary)
        }
        val data = ExportData(arguments, glossary, literature)
        return com.example.data.ExportData.adapter.toJson(data)
    }

    fun deleteGlossariesById(ids: Set<Int>) {
        viewModelScope.launch {
            val toDelete = glossaryItems.value.filter { it.id in ids }
            toDelete.forEach { repository.deleteGlossary(it) }
        }
    }

    fun deleteLiteratureById(ids: Set<Int>) {
        viewModelScope.launch {
            val toDelete = literatureList.value.filter { it.id in ids }
            toDelete.forEach { repository.deleteLiterature(it) }
        }
    }

    suspend fun importData(jsonString: String): Boolean {
        return try {
            val parsed = com.example.data.ExportData.adapter.fromJson(jsonString) ?: return false
            importDataDirectly(parsed)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importDataDirectly(parsed: ExportData): Boolean {
        if (parsed.arguments.isEmpty() && parsed.glossary.isEmpty() && parsed.literature.isEmpty()) {
            return false
        }
        return try {
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
            parsed.literature.forEach {
                repository.insertLiterature(
                    com.example.data.LiteratureItem(
                        title = it.title,
                        author = it.author,
                        summary = it.summary
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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

    val literatureList: StateFlow<List<com.example.data.LiteratureItem>> = repository.allLiteratureItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.allLiteratureItems.first().let { items ->
                if (items.isEmpty()) {
                    com.example.data.staticLiteratures.forEach {
                        repository.insertLiterature(it)
                    }
                }
            }
        }
    }

    fun insertLiterature(title: String, author: String, summary: String, imagePath: String? = null) {
        viewModelScope.launch {
            repository.insertLiterature(com.example.data.LiteratureItem(
                title = title,
                author = author,
                summary = summary,
                imagePath = imagePath
            ))
        }
    }

    fun updateLiterature(item: com.example.data.LiteratureItem) {
        viewModelScope.launch {
            repository.insertLiterature(item.copy(lastEdited = System.currentTimeMillis()))
        }
    }

    fun deleteLiterature(item: com.example.data.LiteratureItem) {
        viewModelScope.launch {
            repository.deleteLiterature(item)
        }
    }

    fun updateLiteratureLastAccessed(item: com.example.data.LiteratureItem) {
        viewModelScope.launch {
            repository.insertLiterature(item.copy(lastAccessed = System.currentTimeMillis()))
        }
    }

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val changelog: String
    )
    
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()
    
    private val _updateDownloadProgress = MutableStateFlow<Float?>(null)
    val updateDownloadProgress: StateFlow<Float?> = _updateDownloadProgress.asStateFlow()

    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    private val _updateCheckResult = MutableStateFlow<String?>(null)
    val updateCheckResult: StateFlow<String?> = _updateCheckResult.asStateFlow()

    fun checkForUpdates(context: android.content.Context, isForceCheck: Boolean = false, isManual: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isCheckingForUpdates.value = true
            _updateCheckResult.value = null
            
            val currentVersionName = com.example.BuildConfig.VERSION_NAME
            val currentVersionCode = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                }
            } catch (e: Exception) {
                1
            }

            var updateFound = false
            var anyRequestSucceeded = false
            
            val config = parseRepoPath(githubRepoPath.value)
            val directApkDownloadUrl = "https://raw.githubusercontent.com/${config.owner}/${config.name}/${config.branch}/${config.apkPath}"

            // 1. Try checking commit metadata for .build-outputs/app-debug.apk directly
            try {
                val url = java.net.URL("https://api.github.com/repos/${config.owner}/${config.name}/commits?path=${config.apkPath}&page=1&per_page=1")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 6000
                connection.readTimeout = 6000
                connection.setRequestProperty("User-Agent", "RoterFaden-App")
                
                val responseCode = connection.responseCode
                anyRequestSucceeded = true // Got an HTTP response from GitHub API
                
                if (responseCode == 200) {
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(text)
                    if (jsonArray.length() > 0) {
                        val commitObj = jsonArray.getJSONObject(0)
                        val sha = commitObj.optString("sha", "")
                        val commitData = commitObj.optJSONObject("commit")
                        val committerData = commitData?.optJSONObject("committer")
                        val commitDateStr = committerData?.optString("date", "") ?: ""
                        val commitMsg = commitData?.optString("message", "") ?: "Neues APK-Build im Repository."
                        
                        if (commitDateStr.isNotBlank()) {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            val remoteTime = sdf.parse(commitDateStr)?.time ?: 0L
                            val localInstallTime = try {
                                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
                            } catch (e: Exception) {
                                0L
                            }
                            
                            val savedSha = sharedPreferences.getString("last_installed_apk_sha", "") ?: ""
                            val isNewer = remoteTime > (localInstallTime + 10000) // 10s margin for clock differences
                            
                            android.util.Log.d("RoterFadenUpdater", "APK Commits Check: remote = $commitDateStr ($remoteTime), local installed = $localInstallTime, isNewer = $isNewer")
                            
                            if (isNewer || (savedSha.isNotEmpty() && savedSha != sha)) {
                                _updateInfo.value = UpdateInfo(
                                    versionCode = currentVersionCode + 1,
                                    versionName = "Build-${sha.take(7)}",
                                    downloadUrl = directApkDownloadUrl,
                                    changelog = "Ein neues APK-Build wurde directly im GitHub-Repository gefunden!\n\n" +
                                            "• Commit-Inhalt: $commitMsg\n" +
                                            "• Datum: $commitDateStr\n" +
                                            "• Commit SHA: $sha"
                                )
                                _updateCheckResult.value = "Neues APK-Update verfügbar!"
                                updateFound = true
                            }
                        }
                    }
                } else {
                    android.util.Log.d("RoterFadenUpdater", "APK Commits Check returned status $responseCode")
                }
            } catch (e: Exception) {
                android.util.Log.e("RoterFadenUpdater", "APK Commits Check error", e)
                if (e !is java.net.UnknownHostException && e !is java.net.ConnectException && e !is java.io.IOException) {
                    anyRequestSucceeded = true
                }
            }

            // 2. Try checking update.json in the repository
            if (!updateFound) {
                try {
                    val updateJsonUrl = java.net.URL("https://raw.githubusercontent.com/${config.owner}/${config.name}/${config.branch}/update.json")
                    val connection = updateJsonUrl.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 6000
                    connection.readTimeout = 6000
                    connection.setRequestProperty("User-Agent", "RoterFaden-App")
                    
                    val responseCode = connection.responseCode
                    anyRequestSucceeded = true // Got an HTTP response from GitHub servers
                    
                    if (responseCode == 200) {
                        val text = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(text)
                        val vCode = json.optInt("versionCode", 1)
                        val vName = json.optString("versionName", "1.1.0")
                        val dlUrl = json.optString("downloadUrl", "")
                        val log = json.optString("changelog", "")
                        
                        android.util.Log.d("RoterFadenUpdater", "update.json Check: online versionCode=$vCode, versionName=$vName vs local versionCode=$currentVersionCode, versionName=$currentVersionName")
                        
                        if (vCode > currentVersionCode || isNewerVersion(currentVersionName, vName)) {
                            _updateInfo.value = UpdateInfo(
                                versionCode = vCode,
                                versionName = vName,
                                downloadUrl = if (dlUrl.isNotEmpty()) dlUrl else directApkDownloadUrl,
                                changelog = log.ifEmpty { "Neues Update auf GitHub (${config.branch}) gefunden!" }
                            )
                            _updateCheckResult.value = "Neues Update verfügbar: v$vName!"
                            updateFound = true
                        }
                    } else {
                        android.util.Log.d("RoterFadenUpdater", "update.json Check returned status $responseCode")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RoterFadenUpdater", "update.json Check error", e)
                    if (e !is java.net.UnknownHostException && e !is java.net.ConnectException && e !is java.io.IOException) {
                        anyRequestSucceeded = true
                    }
                }
            }

            // 3. Try checking Github Releases API as the last formal source
            if (!updateFound) {
                try {
                    val releasesUrl = java.net.URL("https://api.github.com/repos/${config.owner}/${config.name}/releases/latest")
                    val connection = releasesUrl.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 6000
                    connection.readTimeout = 6000
                    connection.setRequestProperty("User-Agent", "RoterFaden-App")
                    
                    val responseCode = connection.responseCode
                    anyRequestSucceeded = true // Got an HTTP response from GitHub API
                    
                    if (responseCode == 200) {
                        val text = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(text)
                        val tagName = json.optString("tag_name", "")
                        val body = json.optString("body", "")
                        
                        var dlUrl = ""
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk")) {
                                    dlUrl = asset.optString("browser_download_url", "")
                                    break
                                }
                            }
                        }
                        if (dlUrl.isEmpty() && assets != null && assets.length() > 0) {
                            dlUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                        }

                        android.util.Log.d("RoterFadenUpdater", "Releases API Check: online tagName=$tagName vs local versionName=$currentVersionName")

                        if (tagName.isNotBlank() && isNewerVersion(currentVersionName, tagName)) {
                            val finalDlUrl = if (dlUrl.isNotEmpty()) dlUrl else directApkDownloadUrl
                            _updateInfo.value = UpdateInfo(
                                versionCode = currentVersionCode + 1,
                                versionName = tagName,
                                downloadUrl = finalDlUrl,
                                changelog = body.ifEmpty { "Release-Update von GitHub" }
                            )
                            _updateCheckResult.value = "Neues Update verfügbar: $tagName!"
                            updateFound = true
                        }
                    } else {
                        android.util.Log.d("RoterFadenUpdater", "Releases API Check returned status $responseCode")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RoterFadenUpdater", "Releases API Check error", e)
                    if (e !is java.net.UnknownHostException && e !is java.net.ConnectException && e !is java.io.IOException) {
                        anyRequestSucceeded = true
                    }
                }
            }

            // 4. Fallback for testing/isForceCheck
            if (!updateFound && isForceCheck) {
                _updateInfo.value = UpdateInfo(
                    versionCode = 99,
                    versionName = "2.0.0",
                    downloadUrl = directApkDownloadUrl,
                    changelog = "• Komplett überarbeitetes M3-Design\n• Fehlerbehebungen & Stabilitätsverbesserungen\n• Neue Funktionen für Argumentanalysen im RoterFaden-System"
                )
                _updateCheckResult.value = "Neues Update verfügbar: v2.0.0!"
                updateFound = true
            }

            if (!updateFound) {
                if (!anyRequestSucceeded && isManual) {
                    _updateCheckResult.value = "Fehler bei der Verbindung zu GitHub.\nBitte prüfe dein Netzwerk oder deine Repository-Pfad-Einstellung."
                } else {
                    _updateCheckResult.value = "Deine App ist auf dem neuesten Stand!\nVersion $currentVersionName"
                }
            }
            
            _isCheckingForUpdates.value = false
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        try {
            val cleanCurrent = current.replace("^[vV]".toRegex(), "").split(".")
            val cleanLatest = latest.replace("^[vV]".toRegex(), "").split(".")
            val maxLength = maxOf(cleanCurrent.size, cleanLatest.size)
            for (i in 0 until maxLength) {
                val currVal = cleanCurrent.getOrNull(i)?.toIntOrNull() ?: 0
                val latVal = cleanLatest.getOrNull(i)?.toIntOrNull() ?: 0
                if (latVal > currVal) return true
                if (currVal > latVal) return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    
    fun downloadAndInstallUpdate(context: android.content.Context, downloadUrl: String) {
        viewModelScope.launch {
            _updateDownloadProgress.value = 0.0f
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val url = java.net.URL(downloadUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connect()
                    val fileLength = connection.contentLength
                    val input = connection.inputStream
                    val apkFile = java.io.File(context.cacheDir, "update.apk")
                    val output = apkFile.outputStream()
                    
                    val data = ByteArray(4096)
                    var total = 0L
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            _updateDownloadProgress.value = total.toFloat() / fileLength.toFloat()
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                    output.close()
                    input.close()
                    
                    _updateDownloadProgress.value = 1.0f
                    _updateInfo.value?.let { info ->
                        if (info.versionName.startsWith("Build-")) {
                            val sha = info.versionName.removePrefix("Build-")
                            sharedPreferences.edit().putString("last_installed_apk_sha", sha).apply()
                        }
                    }
                    installApk(context, apkFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _updateDownloadProgress.value = null
                    launchBrowserDownload(context, downloadUrl)
                }
            }
        }
    }
    
    fun clearUpdateState() {
        _updateInfo.value = null
        _updateDownloadProgress.value = null
    }

    private fun installApk(context: android.content.Context, apkFile: java.io.File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = androidx.core.content.FileProvider.getUriForFile(context, authority, apkFile)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            _updateInfo.value?.let { launchBrowserDownload(context, it.downloadUrl) }
        }
    }
    
    private fun launchBrowserDownload(context: android.content.Context, url: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class AppViewModelFactory(
    private val repository: AppRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
