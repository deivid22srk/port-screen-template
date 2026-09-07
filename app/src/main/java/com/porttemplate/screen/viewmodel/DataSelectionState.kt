package com.porttemplate.screen.viewmodel

/**
 * Fases da detecção de dados do jogo. Cada fase tem visual, cor, ícone e
 * microanimação próprios na [com.porttemplate.screen.ui.components.StatusArea].
 */
sealed interface DataPhase {

    /** Detecção automática em andamento (validando pasta salva anteriormente). */
    data object Searching : DataPhase

    /** Nenhuma pasta válida / nenhum arquivo esperado encontrado. */
    data object NotFound : DataPhase

    /** Pasta validada: [fileName] é o arquivo que deu match no config. */
    data class Found(
        val folderUri: String,
        val fileName: String
    ) : DataPhase

    /** A permissão persistida foi revogada ou a escolha falhou. */
    data object PermissionError : DataPhase
}

/**
 * Estado imutável observado pela UI. Toda a lógica vive no ViewModel —
 * a tela é uma função pura deste estado.
 */
data class DataSelectionUiState(
    val phase: DataPhase = DataPhase.Searching,
    /** Botão primário mostra spinner enquanto a pasta recém-escolhida é validada. */
    val validating: Boolean = false,
    /** Preferência do usuário: desligar partículas (persistida). */
    val particlesEnabled: Boolean = true,
    /** Preferência do usuário: forçar modo "reduzir movimento" (persistida). */
    val reduceMotionOverride: Boolean = false,
)
