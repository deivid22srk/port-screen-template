package com.porttemplate.screen.ui

import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.porttemplate.screen.config.PortBranding
import com.porttemplate.screen.ui.background.AmbientParticles
import com.porttemplate.screen.ui.background.GrainOverlay
import com.porttemplate.screen.ui.background.ParallaxBackground
import com.porttemplate.screen.ui.background.rememberParallaxOffset
import com.porttemplate.screen.ui.components.AnimatedTitle
import com.porttemplate.screen.ui.components.FolderButton
import com.porttemplate.screen.ui.components.PrimarySelectButton
import com.porttemplate.screen.ui.components.SettingsButton
import com.porttemplate.screen.ui.components.SettingsDialog
import com.porttemplate.screen.ui.components.StatusArea
import com.porttemplate.screen.ui.components.TechStatusChip
import com.porttemplate.screen.viewmodel.DataPhase
import com.porttemplate.screen.viewmodel.DataSelectionUiState
import com.porttemplate.screen.viewmodel.DataSelectionViewModel

/**
 * Roteador da tela: conecta ViewModel, SAF (OpenDocumentTree) e o estado
 * de "reduzir movimento" (sistema OU override manual do usuário).
 */
@Composable
fun DataSelectionRoute(viewModel: DataSelectionViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) viewModel.onFolderPicked(uri)
    }

    // Respeita a preferência de acessibilidade do sistema: escalas de
    // animação zeradas = "remover animações" ativo no Android.
    val systemReducedMotion = remember {
        val resolver = context.contentResolver
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f ||
            Settings.Global.getFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f) == 0f
    }
    val reduceMotion = systemReducedMotion || state.reduceMotionOverride

    var settingsOpen by remember { mutableStateOf(false) }

    DataSelectionScreen(
        state = state,
        reduceMotion = reduceMotion,
        onSelectData = {
            if (state.phase is DataPhase.Found) viewModel.onStartGame() else folderPicker.launch(null)
        },
        onSelectFolder = { folderPicker.launch(null) },
        onOpenSettings = { settingsOpen = true },
        onDismissSettings = { settingsOpen = false },
        onParticlesEnabled = viewModel::setParticlesEnabled,
        onReduceMotionOverride = viewModel::setReduceMotionOverride
    )
}

/**
 * Composição da cena AAA:
 *
 *   [fundo parallax em camadas] → [partículas ambiente] → [conteúdo central]
 *   → [grain de filme por cima de tudo] → [diálogo de ajustes]
 *
 * Layout fluido com pesos verticais: funciona de telas 16:9 até 21:9 e em
 * landscape (modo compacto reduz tamanhos abaixo de 520dp de altura).
 */
@Composable
fun DataSelectionScreen(
    state: DataSelectionUiState,
    reduceMotion: Boolean,
    onSelectData: () -> Unit,
    onSelectFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onParticlesEnabled: (Boolean) -> Unit,
    onReduceMotionOverride: (Boolean) -> Unit,
) {
    val config = PortBranding.config

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF07070C))
    ) {
        val compact = maxHeight < 520.dp
        val parallax = rememberParallaxOffset(reduceMotion)

        ParallaxBackground(parallax = parallax, compact = compact)
        AmbientParticles(
            reducedMotion = reduceMotion,
            enabled = state.particlesEnabled && config.particlesEnabled,
            compact = compact
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.05f))

            AnimatedTitle(compact = compact, reduceMotion = reduceMotion)

            Spacer(Modifier.weight(0.85f))

            StatusArea(state.phase, compact, reduceMotion)

            Spacer(Modifier.height(if (compact) 18.dp else 26.dp))

            PrimarySelectButton(
                phase = state.phase,
                validating = state.validating,
                compact = compact,
                reduceMotion = reduceMotion,
                onClick = onSelectData,
                modifier = Modifier.widthIn(max = 420.dp)
            )

            FolderButton(
                icon = Icons.Filled.Folder,
                enabled = state.phase !is DataPhase.Found && !state.validating,
                compact = compact,
                reduceMotion = reduceMotion,
                onClick = onSelectFolder
            )

            Spacer(Modifier.weight(1.25f))

            // ---- Barra inferior: chip técnico | engrenagem --------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showTechChip) {
                    TechStatusChip()
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                SettingsButton(onClick = onOpenSettings)
            }
        }

        // Grain de filme acima de TODA a composição (inclusive conteúdo).
        GrainOverlay()

        if (settingsOpen) {
            SettingsDialog(
                particlesEnabled = state.particlesEnabled,
                reduceMotionEnabled = state.reduceMotionOverride,
                onParticlesEnabled = onParticlesEnabled,
                onReduceMotionEnabled = onReduceMotionOverride,
                onDismiss = onDismissSettings
            )
        }
    }
}
