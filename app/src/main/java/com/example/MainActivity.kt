package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.RoterFadenApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ImmersiveBackground

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestHighRefreshRate()
        
        // Initialize Database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "roter_faden_db"
        )
        // Add fallback destructor since we changed the schema
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

        val repository = AppRepository(database)
        
        // Initialize ViewModel
        val sharedPreferences = getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
        val factory = AppViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

        handleIntent(intent, viewModel)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ImmersiveBackground
                ) {
                    RoterFadenApp(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::viewModel.isInitialized) {
            handleIntent(intent, viewModel)
        }
    }

    private fun getFileName(uri: android.net.Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun handleIntent(intent: android.content.Intent?, viewModel: AppViewModel) {
        val uri = intent?.data ?: return
        
        if (uri.scheme == "roterfaden") {
            // Handle roterfaden://import?data=...
            if (uri.host == "import") {
                val base64Data = uri.getQueryParameter("data")
                if (!base64Data.isNullOrEmpty()) {
                    try {
                        val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT or android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        
                        // Try GZIP decompress, fallback to raw string
                        val jsonStr = try {
                            val byteStream = java.io.ByteArrayInputStream(decodedBytes)
                            val gzipStream = java.util.zip.GZIPInputStream(byteStream)
                            gzipStream.bufferedReader(java.nio.charset.StandardCharsets.UTF_8).use { it.readText() }
                        } catch (e: Exception) {
                            String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8)
                        }

                        val moshi = com.squareup.moshi.Moshi.Builder().build()
                        val adapter = moshi.adapter(com.example.data.ExportData::class.java)
                        val parsed = adapter.fromJson(jsonStr)
                        if (parsed != null && (parsed.arguments.isNotEmpty() || parsed.glossary.isNotEmpty() || parsed.literature.isNotEmpty())) {
                            viewModel.setPendingImportData(parsed)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (uri.scheme == "content" || uri.scheme == "file") {
            val fileName = getFileName(uri)
            if (fileName != null && fileName.endsWith(".roterfaden", ignoreCase = true)) {
                // Handle file open (.roterfaden)
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonStr = inputStream.bufferedReader(java.nio.charset.StandardCharsets.UTF_8).use { it.readText() }
                        val moshi = com.squareup.moshi.Moshi.Builder().build()
                        val adapter = moshi.adapter(com.example.data.ExportData::class.java)
                        val parsed = adapter.fromJson(jsonStr)
                        if (parsed != null && (parsed.arguments.isNotEmpty() || parsed.glossary.isNotEmpty() || parsed.literature.isNotEmpty())) {
                            viewModel.setPendingImportData(parsed)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestHighRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val modes = display?.supportedModes
                if (modes != null) {
                    val highestMode = modes.maxByOrNull { it.refreshRate }
                    if (highestMode != null && highestMode.refreshRate > 60f) {
                        val params = window.attributes
                        params.preferredDisplayModeId = highestMode.modeId
                        window.attributes = params
                    }
                }
            } catch (e: Exception) {
                // Ignore if unsupported by device
            }
        }
    }
}
