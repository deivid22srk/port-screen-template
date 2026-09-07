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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel da tela de seleção de dados — arquitetura estado/UI separada:
 *
 *   SAF (Activity)  ──▶  onFolderPicked()  ──▶  validação em IO  ──▶  UiState
 *   SharedPreferences (pasta persistida)  ──▶  restoreSavedFolder()  ──▶  UiState
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
        _uiState.update {
            it.copy(
                particlesEnabled = prefs.getBoolean(KEY_PARTICLES, true),
                reduceMotionOverride = prefs.getBoolean(KEY_REDUCE_MOTION, false)
            )
        }
        restoreSavedFolder()
    }

    /**
     * Fluxo de boot:
     *  - pasta persistida existe  → fase Searching durante a validação em IO;
     *  - pasta persistida revogada/inválida → NotFound (usuário escolhe outra);
     *  - nenhuma pasta salva      → Searching por um instante (a detecção
     *    automática "olha ao redor") e depois NotFound, para que a animação
     *    de entrada mostre os três estados do ciclo de vida.
     */
    private fun restoreSavedFolder() {
        val savedUri = prefs.getString(KEY_FOLDER_URI, null)
        if (savedUri == null) {
            viewModelScope.launch {
                delay(1500)
                _uiState.update { st ->
                    if (st.phase is DataPhase.Searching && !st.validating) {
                        st.copy(phase = DataPhase.NotFound)
                    } else st
                }
            }
            return
        }
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
            _uiState.update { it.copy(phase = DataPhase.PermissionError, validating = false) }
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
        _uiState.update { it.copy(validating = true, phase = DataPhase.Searching) }
        viewModelScope.launch {
            val fileName = withContext(Dispatchers.IO) {
                runCatching {
                    DocumentFile.fromTreeUri(getApplication(), uri)?.let { folder ->
                        GameDataScanner.findExpectedFile(folder, PortBranding.config)
                    }
                }.getOrNull()
            }
            _uiState.update { st ->
                st.copy(
                    validating = false,
                    phase = if (fileName != null) {
                        DataPhase.Found(folderUri = uri.toString(), fileName = fileName)
                    } else {
                        DataPhase.NotFound
                    }
                )
            }
        }
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

    // ------------------------------------------------------------------
    // Preferências do diálogo de ajustes (persistidas)
    // ------------------------------------------------------------------

    fun setParticlesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PARTICLES, enabled).apply()
        _uiState.update { it.copy(particlesEnabled = enabled) }
    }

    fun setReduceMotionOverride(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
        _uiState.update { it.copy(reduceMotionOverride = enabled) }
    }

    private companion object {
        const val PREFS_NAME = "port_screen_prefs"
        const val KEY_FOLDER_URI = "data_folder_uri"
        const val KEY_PARTICLES = "particles_enabled"
        const val KEY_REDUCE_MOTION = "reduce_motion"
    }
}
