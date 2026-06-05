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
        val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

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
