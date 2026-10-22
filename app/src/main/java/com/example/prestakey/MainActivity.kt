package com.example.prestakey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.prestakey.ui.screens.auth.AuthScreen
import com.example.prestakey.ui.theme.PrestaKeyTheme

/**
 * Entrega UD2: mockups estáticos, sin navegación real.
 * Al arrancar se ve el Login; el resto de pantallas se ven con sus @Preview
 * (o cambiando AuthScreen() por KeysScreen(), LoansScreen(), NfcScreen() o IncidentsScreen()).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrestaKeyTheme {
                AuthScreen()
            }
        }
    }
}
