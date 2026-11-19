package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.data.repository.UserPreferencesStore
import com.example.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsStore = remember(applicationContext) {
                UserPreferencesStore(applicationContext)
            }
            AppNavigation(prefsStore = prefsStore)
        }
    }
}
