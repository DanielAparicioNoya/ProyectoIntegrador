package com.example.prestakey.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.prestakey.R
import com.example.prestakey.ui.theme.PrestaKeyTheme

/**
 * Componente Composable para la barra de navegación inferior (NavigationBar).
 */
@Composable
fun BottomNavBar(
    seleccionada: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit = {}
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = seleccionada == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_keys)) }
        )
        NavigationBarItem(
            selected = seleccionada == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_nfc)) }
        )
        NavigationBarItem(
            selected = seleccionada == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_loans)) }
        )
        NavigationBarItem(
            selected = seleccionada == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_incidents)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    PrestaKeyTheme {
        BottomNavBar(seleccionada = 2)
    }
}
