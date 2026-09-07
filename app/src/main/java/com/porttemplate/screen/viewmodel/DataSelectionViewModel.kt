package com.porttemplate.screen.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.porttemplate.screen.config.PortBranding
import com.porttemplate.screen.data.GameDataScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel da tela de seleção de dados — arquitetura estado/UI separada:
 *
 *   SAF (Activity)  ──▶  onFolderPicked()  ──▶  validação em IO  ──▶  UiState
 *   SharedPreferences (pasta persistida)  ──▶  restauração silenciosa  ──▶  UiState
 *
 * O app abre SEMPRE em [DataPhase.Idle]: nada é procurado automaticamente.
 * A pasta persistida de uma sessão anterior é revalidada silenciosamente
 * (sem tela de "procurando"), apenas para restaurar o estado "pronto".
 *
 * A UI nunca toca no ContentResolver: ela apenas renderiza [uiState] e emite
 * eventos. Trocar Compose por Views não muda nada aqui.
 */
class DataSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(DataSelectionUiState())
    val uiState: StateFlow<DataSelectionUiState> = _uiState.asStateFlow()

    /**
     * Ponto de integração do MOTOR do port. O host (MainActivity) registra um
     * callback aqui e recebe (folderUri, fileName) quando o usuário toca em
     * "Iniciar Jogo". O template em si nunca inicia nada — é só uma tela.
     */
    var onLaunchGame: ((folderUri: String, fileName: String) -> Unit)? = null

    init {
        restoreSavedFolder()
    }

    /**
     * Fluxo de boot (sem busca automática):
     *  - pasta persistida existe  → validação silenciosa rápida (sem estado
     *    de "procurando" visível prolongado);
     *  - pasta persistida revogada/inválida → Idle (usuário escolhe outra);
     *  - nenhuma pasta salva      → Idle, direto ao ponto.
     */
    private fun restoreSavedFolder() {
        val savedUri = prefs.getString(KEY_FOLDER_URI, null) ?: return
        validateFolder(Uri.parse(savedUri))
    }

    /** Chamado pela Activity quando o SAF devolve a árvore escolhida. */
    fun onFolderPicked(treeUri: Uri) {
        val app = getApplication<Application>()
        // Persistir o grant para sobreviver a reboots (SAF padrão de ports).
        try {
            app.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            _uiState.value = DataSelectionUiState(DataPhase.PermissionError)
            return
        }
        prefs.edit().putString(KEY_FOLDER_URI, treeUri.toString()).apply()
        validateFolder(treeUri)
    }

    /** Revalida a pasta atualmente persistida (ex.: arquivo chegou depois). */
    fun rescan() {
        prefs.getString(KEY_FOLDER_URI, null)?.let { validateFolder(Uri.parse(it)) }
    }

    /** Valida a pasta em Dispatchers.IO (listFiles em SAF é I/O de verdade). */
    private fun validateFolder(uri: Uri) {
        _uiState.value = DataSelectionUiState(DataPhase.Validating)
        viewModelScope.launch {
            val fileName = withContext(Dispatchers.IO) {
                runCatching {
                    DocumentFile.fromTreeUri(getApplication(), uri)?.let { folder ->
                        GameDataScanner.findExpectedFile(folder, PortBranding.config)
                    }
                }.getOrNull()
            }
            _uiState.value = if (fileName != null) {
                DataSelectionUiState(DataPhase.Found(folderUri = uri.toString(), fileName = fileName))
            } else {
                DataSelectionUiState(DataPhase.NotFound)
            }
        }
    }

    /**
     * Apaga a pasta persistida (usado pela tela de Configurações): libera a
     * permissão persistida, limpa o prefs e volta ao estado inicial.
     */
    fun clearSavedSelection() {
        val app = getApplication<Application>()
        prefs.getString(KEY_FOLDER_URI, null)?.let { saved ->
            try {
                app.contentResolver.releasePersistableUriPermission(
                    Uri.parse(saved),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Permissão já revogada pelo sistema — nada a fazer.
            }
        }
        prefs.edit().remove(KEY_FOLDER_URI).apply()
        _uiState.value = DataSelectionUiState(DataPhase.Idle)
    }

    /** Clique no botão primário com dados prontos — entrega ao motor do port. */
    fun onStartGame() {
        val phase = _uiState.value.phase
        if (phase is DataPhase.Found) {
            onLaunchGame?.invoke(phase.folderUri, phase.fileName)
                ?: run {
                    // Demo sem motor acoplado: mostra o contrato recebido.
                    Toast.makeText(
                        getApplication(),
                        "Motor do port receberia: ${phase.fileName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private companion object {
        const val PREFS_NAME = "port_screen_prefs"
        const val KEY_FOLDER_URI = "data_folder_uri"
    }
}
