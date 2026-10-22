package com.example.prestakey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.prestakey.ui.theme.PrestaKeyTheme
import com.example.prestakey.ui.theme.StatusBorrowedBg
import com.example.prestakey.ui.theme.StatusBorrowedText

/**
 * Componente Composable para mostrar el estado de una llave o préstamo.
 */
@Composable
fun StatusBadge(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(fondo, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorTexto
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBadgePreview() {
    PrestaKeyTheme {
        StatusBadge(
            texto = "2 activos",
            fondo = StatusBorrowedBg,
            colorTexto = StatusBorrowedText
        )
    }
}
