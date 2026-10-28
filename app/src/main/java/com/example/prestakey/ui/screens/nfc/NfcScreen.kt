package com.example.prestakey.ui.screens.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.prestakey.R
import com.example.prestakey.data.model.KeyStatus
import com.example.prestakey.ui.components.BottomNavBar
import com.example.prestakey.ui.components.CodeBox
import com.example.prestakey.ui.components.InfoNote
import com.example.prestakey.ui.components.StatusBadge
import com.example.prestakey.ui.components.UtadTopBar
import com.example.prestakey.ui.theme.PrestaKeyTheme
import com.example.prestakey.ui.theme.PrimaryBlue
import com.example.prestakey.ui.theme.PrimaryBlueLight
import com.example.prestakey.ui.theme.SelectedBlue
import com.example.prestakey.ui.theme.StatusAvailableBg
import com.example.prestakey.ui.theme.StatusAvailableText
import com.example.prestakey.ui.theme.StatusBorrowedBg
import com.example.prestakey.ui.theme.StatusBorrowedText
import com.example.prestakey.ui.theme.StatusRevisionBg
import com.example.prestakey.ui.theme.StatusRevisionText
import com.example.prestakey.ui.theme.SurfaceCard
import com.example.prestakey.ui.theme.TextMuted
import com.example.prestakey.ui.theme.TextPrimary
import com.example.prestakey.ui.theme.TextSecondary
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Pantalla 4: Búsqueda y lectura NFC conectada con MVVM y Escáner QR.
 */
@Composable
fun NfcScreen(
    viewModel: NfcViewModel = remember { NfcViewModel() }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Configuración del escáner de códigos de Google (QR)
    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, scannerOptions)
    }

    // Función auxiliar para lanzar el escáner QR
    val lanzarEscanerQr = {
        scanner.startScan()
            .addOnSuccessListener { barcode: Barcode ->
                viewModel.onQrScanned(barcode.rawValue)
            }
            .addOnCanceledListener {
                viewModel.onQrScanFailed("Escaneo QR cancelado")
            }
            .addOnFailureListener { e: Exception ->
                viewModel.onQrScanFailed(e.localizedMessage ?: "Error al escanear QR")
            }
    }

    // 1. Cuando el ViewModel ordena abrir el escáner QR (tras detectar el tag NFC)
    LaunchedEffect(uiState.shouldLaunchQrScanner) {
        if (uiState.shouldLaunchQrScanner) {
            viewModel.onQrScannerLaunched()
            lanzarEscanerQr()
        }
    }

    // 2. Escuchar el Broadcast de NFC que emite MainActivity
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val tagId = intent?.getStringExtra("nfc_tag_id")
                if (!tagId.isNullOrEmpty()) {
                    viewModel.onTagDetected(tagId)
                }
            }
        }
        val filter = IntentFilter("com.example.prestakey.NFC_SCANNED")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        topBar = { UtadTopBar(titulo = stringResource(R.string.nfc_topbar_title)) },
        bottomBar = { BottomNavBar(seleccionada = 3) }
    ) { valoresDeRelleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(valoresDeRelleno)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título y Estado
            item {
                Column {
                    Text(
                        text = stringResource(R.string.nfc_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.nfcTagScanned != null) PrimaryBlue else TextMuted
                    )
                }
            }

            // Zona de lectura (dibujo del móvil reactivo)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlueLight, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(SelectedBlue, RoundedCornerShape(65.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.nfcTagScanned != null) Icons.Default.CheckCircle else Icons.Default.Phone,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.nfcTagScanned != null) {
                            "¡Tarjeta leída! UID: ${uiState.nfcTagScanned}"
                        } else {
                            stringResource(R.string.nfc_zone)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = if (uiState.nfcTagScanned != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Botón de acción: abrir escáner QR manualmente o reescanear
            item {
                Button(
                    onClick = { lanzarEscanerQr() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.scannedQrCode != null) "Reescanear Código QR" else "Abrir Escáner QR",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Vista previa de la llave leída por NFC
            item {
                val key = uiState.matchedKey
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.nfc_preview_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        val (badgeText, badgeBg, badgeTextColor) = when (key?.status) {
                            KeyStatus.AVAILABLE -> Triple(
                                stringResource(R.string.status_available),
                                StatusAvailableBg,
                                StatusAvailableText
                            )
                            KeyStatus.IN_REVIEW -> Triple(
                                stringResource(R.string.status_in_review),
                                StatusRevisionBg,
                                StatusRevisionText
                            )
                            else -> Triple(
                                stringResource(R.string.status_borrowed),
                                StatusBorrowedBg,
                                StatusBorrowedText
                            )
                        }
                        StatusBadge(
                            texto = badgeText,
                            fondo = badgeBg,
                            colorTexto = badgeTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CodeBox(codigo = key?.classroomCode ?: stringResource(R.string.code_b12))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = key?.classroomName ?: stringResource(R.string.key_b12_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (key != null) "UID NFC: ${key.nfcTagId}" else stringResource(R.string.nfc_preview_tag),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = key?.location ?: stringResource(R.string.nfc_preview_loan),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Resultado del Código QR escaneado (si ya se escaneó)
            if (uiState.scannedQrCode != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RESULTADO ESCÁNER QR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(
                                texto = "Verificado",
                                fondo = StatusAvailableBg,
                                colorTexto = StatusAvailableText
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.scannedQrCode!!,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Búsqueda manual por código
            item {
                Column {
                    Text(
                        text = stringResource(R.string.nfc_search_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.matchedKey?.classroomCode ?: "",
                        onValueChange = {},
                        placeholder = { Text(stringResource(R.string.code_b12)) },
                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(12.dp))
                    )
                }
            }

            item { InfoNote(texto = stringResource(R.string.nfc_note)) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NfcScreenPreview() {
    PrestaKeyTheme {
        NfcScreen()
    }
}
