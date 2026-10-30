package com.example.prestakey.ui.screens.nfc

import androidx.lifecycle.ViewModel
import com.example.prestakey.data.model.KeyItem
import com.example.prestakey.data.model.KeyStatus
import com.example.prestakey.data.repository.PrestaKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado observable de la pantalla NFC y Escáner de Código de Barras / QR.
 */
data class NfcUiState(
    val nfcTagScanned: String? = null,
    val matchedKey: KeyItem? = null,
    val scannedCode: String? = null,
    val shouldLaunchScanner: Boolean = false,
    val isScanning: Boolean = true,
    val statusMessage: String = "Acerca cualquier llave o tarjeta NFC al móvil"
)

/**
 * ViewModel para gestionar la lectura NFC y la apertura del escáner de códigos de barras.
 */
class NfcViewModel(
    private val repository: PrestaKeyRepository = PrestaKeyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NfcUiState())
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()

    /**
     * Se ejecuta cuando el sensor NFC del móvil lee un tag.
     * Busca la llave o la genera al vuelo, y activa la orden para abrir el escáner de códigos de barras.
     */
    fun onTagDetected(tagId: String) {
        val existingKey = repository.findKeyByNfcTag(tagId)

        val key = existingKey ?: KeyItem(
            id = "KEY-${tagId.replace(":", "")}",
            classroomCode = "NFC",
            classroomName = "Llave detectada ($tagId)",
            nfcTagId = tagId,
            status = KeyStatus.AVAILABLE,
            location = "Lector NFC",
            description = "Llave física leída en vivo"
        )

        _uiState.update {
            it.copy(
                nfcTagScanned = tagId,
                matchedKey = key,
                shouldLaunchScanner = true, // ¡Ordena abrir el escáner de código de barras!
                statusMessage = "¡Tag detectado ($tagId)! Abriendo escáner de código de barras..."
            )
        }
    }

    /**
     * Llamado por la vista para apagar la bandera y no reabrir el escáner en bucle.
     */
    fun onScannerLaunched() {
        _uiState.update { it.copy(shouldLaunchScanner = false) }
    }

    /**
     * Guarda el contenido leído desde el código de barras o QR.
     */
    fun onCodeScanned(codeContent: String?) {
        _uiState.update {
            it.copy(
                scannedCode = codeContent,
                statusMessage = if (!codeContent.isNullOrBlank()) {
                    "¡Código escaneado con éxito!"
                } else {
                    "No se detectó ningún contenido en el código"
                }
            )
        }
    }

    /**
     * Maneja el caso en que el usuario cancela o falla el escaneo.
     */
    fun onScanFailed(error: String? = null) {
        _uiState.update {
            it.copy(
                statusMessage = error ?: "Escaneo cancelado"
            )
        }
    }

    /**
     * Reinicia el estado para leer otra llave.
     */
    fun resetScan() {
        _uiState.update {
            NfcUiState()
        }
    }
}
