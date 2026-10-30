package com.example.prestakey.ui.screens.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Pantalla 4: Búsqueda y lectura NFC conectada con MVVM y Escáner de Códigos de Barras / QR.
 */
@Composable
fun NfcScreen(

    viewModel: NfcViewModel = run {
        val context = LocalContext.current
        val isPreview = LocalInspectionMode.current
        if (!isPreview && context is ComponentActivity) {
            ViewModelProvider(context)[NfcViewModel::class.java]
        } else {
            remember { NfcViewModel() }
        }

},
onNavigate: (Int) -> Unit = {},
onLogout: () -> Unit = {}

) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val uiState by viewModel.uiState.collectAsState()

    // Configuración del escáner: FORMAT_ALL_FORMATS permite leer cualquier
    // código de barras (1D: EAN-13, Code 128, Code 39, UPC...) y 2D (QR, Data Matrix...)
    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .enableAutoZoom()
            .build()
    }

    // Función auxiliar para lanzar el escáner (se instancia únicamente al ejecutar,
    // evitando inicializar ML Kit durante el @Preview de Android Studio)
    val lanzarEscaner = {
        if (!isPreview) {
            val scanner = GmsBarcodeScanning.getClient(context, scannerOptions)
            scanner.startScan()
                .addOnSuccessListener { barcode: Barcode ->
                    viewModel.onCodeScanned(barcode.rawValue)
                }
                .addOnCanceledListener {
                    viewModel.onScanFailed("Escaneo cancelado")
                }
                .addOnFailureListener { e: Exception ->
                    viewModel.onScanFailed(e.localizedMessage ?: "Error al escanear código de barras")
                }
        }
    }

    // 1. Cuando el ViewModel ordena abrir el escáner (tras detectar el tag NFC)
    LaunchedEffect(uiState.shouldLaunchScanner) {
        if (!isPreview && uiState.shouldLaunchScanner) {
            viewModel.onScannerLaunched()
            lanzarEscaner()
        }
    }

    // 2. Escuchar el Broadcast de NFC que emite MainActivity (solo en dispositivo real, no en preview)
    if (!isPreview) {
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
    }


    Scaffold(
        topBar = { UtadTopBar(titulo = stringResource(R.string.nfc_topbar_title), onLogout = onLogout) },
        bottomBar = { BottomNavBar(seleccionada = 3, onNavigate = onNavigate) }
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

            // Resultado del Código de Barras o QR escaneado (centrado con tick armonizado)
            if (uiState.scannedCode != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CÓDIGO DE BARRAS / QR DETECTADO",
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(StatusAvailableBg, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusAvailableText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.scannedCode!!,
                                lineHeight = 24.sp,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 24.sp,
                                    platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                                        includeFontPadding = false
                                    ),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Proportional,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                ),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .offset(y = 12.dp)
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
