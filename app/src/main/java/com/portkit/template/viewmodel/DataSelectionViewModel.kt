package com.portkit.template.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.portkit.template.branding.PortBranding
import com.portkit.template.data.DataResolution
import com.portkit.template.data.GameDataRepository
import com.portkit.template.data.PortPreferences
import com.portkit.template.util.humanizeBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado completo da tela de seleção de dados — separado da camada visual. */
sealed interface DataSelectionUiState {

    /** Verificando a existência dos dados (auto-check no boot e após escolher pasta). */
    data object Checking : DataSelectionUiState

    /** Dados ainda não localizados — usuário precisa selecionar. */
    data class NotFound(val folderName: String?) : DataSelectionUiState

    /** Dados válidos encontrados — pronto para iniciar. */
    data class Found(
        val fileName: String,
        val sizeLabel: String,
    ) : DataSelectionUiState

    /** Permissão expirada/revogada ou falha de acesso — pede nova seleção. */
    data class Error(val message: String? = null) : DataSelectionUiState
}

/** Preferências de apresentação controladas pela aba de configurações. */
data class ScreenSettings(
    val particlesEnabled: Boolean = true,
    val parallaxEnabled: Boolean = true,
    val reducedMotionManual: Boolean = false,
)

/**
 * Controlador da tela: máquina de estados + integração com o repositório SAF.
 * A camada visual apenas renderiza [uiState] e delega eventos para cá.
 */
class DataSelectionViewModel(app: Application) : AndroidViewModel(app) {

    val config = PortBranding.current

    private val prefs = PortPreferences(app)
    private val repo = GameDataRepository(app, config)

    private val _uiState = MutableStateFlow<DataSelectionUiState>(DataSelectionUiState.Checking)
    val uiState: StateFlow<DataSelectionUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(
        ScreenSettings(
            particlesEnabled = prefs.particlesEnabled,
            parallaxEnabled = prefs.parallaxEnabled,
            reducedMotionManual = prefs.reducedMotionManual,
        )
    )
    val settings: StateFlow<ScreenSettings> = _settings.asStateFlow()

    /** Nome amigável da pasta persistida (ou null). */
    private val _folderName = MutableStateFlow<String?>(null)
    val folderName: StateFlow<String?> = _folderName.asStateFlow()

    init {
        _folderName.value = prefs.folderUri?.let { queryTreeDisplayName(Uri.parse(it)) }
        revalidate()
    }

    /**
     * Re-executa a verificação (boot da tela, "tentar de novo", escolha de pasta).
     * Toda E/S acontece em Dispatchers.IO; a main thread só recebe o resultado.
     */
    fun revalidate() {
        _uiState.value = DataSelectionUiState.Checking
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repo.resolveSaved(prefs) }
            apply(result)
        }
    }

    /** Usuário escolheu um ARQUIVO via ACTION_OPEN_DOCUMENT. */
    fun onFilePicked(uri: Uri) {
        persistPermission(uri)
        prefs.fileUri = uri.toString()
        _uiState.value = DataSelectionUiState.Checking
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repo.inspectFile(uri) }
            apply(result)
        }
    }

    /** Usuário escolheu uma PASTA via ACTION_OPEN_DOCUMENT_TREE. */
    fun onFolderPicked(uri: Uri) {
        persistPermission(uri)
        prefs.fileUri = null // pasta passa a ser a fonte da verdade
        prefs.folderUri = uri.toString()
        _folderName.value = queryTreeDisplayName(uri)
        revalidate()
    }

    /** Limpa a seleção e pede nova verificação (usado quando a permissão expira). */
    fun clearSelectionAndRetry() {
        prefs.clearSelection()
        _folderName.value = null
        revalidate()
    }

    fun setParticlesEnabled(value: Boolean) {
        _settings.update { it.copy(particlesEnabled = value) }
        prefs.particlesEnabled = value
    }

    fun setParallaxEnabled(value: Boolean) {
        _settings.update { it.copy(parallaxEnabled = value) }
        prefs.parallaxEnabled = value
    }

    fun setReducedMotionManual(value: Boolean) {
        _settings.update { it.copy(reducedMotionManual = value) }
        prefs.reducedMotionManual = value
    }

    // ── Internos ──────────────────────────────────────────────────────────

    private fun apply(result: DataResolution) {
        _uiState.value = when (result) {
            is DataResolution.Found -> DataSelectionUiState.Found(
                fileName = result.info.fileName,
                sizeLabel = humanizeBytes(result.info.sizeBytes),
            )
            DataResolution.NotFound -> DataSelectionUiState.NotFound(_folderName.value)
            is DataResolution.PermissionLost -> DataSelectionUiState.Error()
        }
    }

    /**
     * Persiste a permissão de leitura da URI para sobreviver a reinícios do app.
     * ACTION_OPEN_DOCUMENT e ACTION_OPEN_DOCUMENT_TREE concedem flags persistíveis;
     * se o provider não conceder, seguimos sem persistir (sessão única).
     */
    private fun persistPermission(uri: Uri) {
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    private fun queryTreeDisplayName(uri: Uri): String? = runCatching {
        getApplication<Application>().contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull()
}
