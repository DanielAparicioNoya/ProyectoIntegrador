package com.utad.gestionllaves

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.utad.gestionllaves.ui.screens.LoginScreen
import com.utad.gestionllaves.ui.theme.GestionLlavesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionLlavesTheme {
                LoginScreen()
            }
        }
    }
}
