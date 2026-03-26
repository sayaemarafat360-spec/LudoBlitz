package com.ludoblitz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ludoblitz.app.ui.navigation.LudoBlitzNavigation
import com.ludoblitz.app.ui.theme.LudoBlitzTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LudoBlitzTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LudoBlitzNavigation()
                }
            }
        }
    }
}
