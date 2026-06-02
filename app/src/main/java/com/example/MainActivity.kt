package com.example

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
        val factory = AppViewModelFactory(repository)
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
}
