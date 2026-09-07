package com.portkit.template.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.portkit.template.R
import com.portkit.template.branding.PortBranding
import com.portkit.template.branding.PortBrandingConfig
import com.portkit.template.ui.motion.rememberParallaxState
import com.portkit.template.ui.motion.rememberReducedMotion
import com.portkit.template.ui.screens.components.GlassIconButton
import com.portkit.template.ui.screens.components.GlassPrimaryButton
import com.portkit.template.ui.screens.components.ParallaxBackground
import com.portkit.template.ui.screens.components.ParticleField
import com.portkit.template.ui.screens.components.PortLogoTitle
import com.portkit.template.ui.screens.components.PrimaryButtonMode
import com.portkit.template.ui.screens.components.SettingsIconButton
import com.portkit.template.ui.screens.components.SettingsSheet
import com.portkit.template.ui.screens.components.StatusBanner
import com.portkit.template.ui.screens.components.TechChip
import com.portkit.template.ui.screens.components.VignetteAndGrain
import com.portkit.template.ui.theme.BgDeep
import com.portkit.template.ui.theme.PortTemplateTheme
import com.portkit.template.ui.util.BitmapFx
import com.portkit.template.viewmodel.DataSelectionUiState
import com.portkit.template.viewmodel.DataSelectionViewModel
import com.portkit.template.viewmodel.ScreenSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tela de seleção de dados do jogo — primeira tela do app do port.
 *
 * Orquestra: fundo com parallax → partículas → vinheta/grain → conteúdo.
 * Toda a regra de estado mora em [DataSelectionViewModel]; esta camada só
 * renderiza e delega eventos. Os launchers do Storage Access Framework
 * (arquivo + pasta) vivem aqui, acoplados aos callbacks do ViewModel.
 */
@Composable
fun DataSelectionScreen(
    onLaunchGame: () -> Unit,
    viewModel: DataSelectionViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // SAF: arquivo único (extensões da config são validadas depois)
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::onFilePicked) }

    // SAF: pasta de dados com permissão persistente
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let(viewModel::onFolderPicked) }

    DataSelectionContent(
        uiState = uiState,
        settings = settings,
        config = viewModel.config,
        onLaunchGame = onLaunchGame,
        onPickFile = { filePicker.launch(arrayOf("*/*")) },
        onPickFolder = { folderPicker.launch(null) },
        onParticles = viewModel::setParticlesEnabled,
        onParallax = viewModel::setParallaxEnabled,
        onReducedMotion = viewModel::setReducedMotionManual,
        onRetry = viewModel::revalidate,
    )
}

/**
 * Composição pura da tela (sem ViewModel) — reutilizável em @Preview.
 */
@Composable
fun DataSelectionContent(
    uiState: DataSelectionUiState,
    settings: ScreenSettings,
    config: PortBrandingConfig,
    onLaunchGame: () -> Unit,
    onPickFile: () -> Unit,
    onPickFolder: () -> Unit,
    onParticles: (Boolean) -> Unit,
    onParallax: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    val reducedMotion = rememberReducedMotion(settings)
    val parallax = rememberParallaxState(settings.parallaxEnabled && !reducedMotion)
    val backdrop = rememberBlurredBackdrop(config.backgroundRes)

    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        val compact = maxHeight < 420.dp // aparelho em paisagem

        // ── camadas atmosféricas ─────────────────────────────────────────
        ParallaxBackground(config.backgroundRes, parallax)
        ParticleField(
            style = config.particleStyle,
            active = settings.particlesEnabled,
            reducedMotion = reducedMotion,
        )
        VignetteAndGrain(reducedMotion)

        // ── conteúdo (contra-movimento sutil de parallax) ────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -parallax.tiltX * 4f
                    translationY = parallax.tiltY * 2.4f
                }
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // topo: logo/título em área dedicada
            Entrance(order = 0, reducedMotion = reducedMotion) {
                PortLogoTitle(
                    config = config,
                    reducedMotion = reducedMotion,
                    modifier = Modifier.padding(top = if (compact) 20.dp else 52.dp),
                )
            }

            // meio: botão primário + banner de status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Entrance(order = 1, reducedMotion = reducedMotion) {
                    val mode = when {
                        uiState is DataSelectionUiState.Checking -> PrimaryButtonMode.LOADING
                        uiState is DataSelectionUiState.Found -> PrimaryButtonMode.LAUNCH
                        else -> PrimaryButtonMode.SELECT
                    }
                    val (label, cd) = when (mode) {
                        PrimaryButtonMode.LOADING ->
                            stringResource(R.string.primary_button_loading) to
                                stringResource(R.string.cd_primary_button)
                        PrimaryButtonMode.LAUNCH ->
                            stringResource(R.string.primary_button_launch) to
                                stringResource(R.string.cd_primary_button_launch)
                        PrimaryButtonMode.SELECT ->
                            stringResource(R.string.primary_button_select) to
                                stringResource(R.string.cd_primary_button)
                    }
                    GlassPrimaryButton(
                        mode = mode,
                        label = label,
                        contentDescriptionText = cd,
                        onClick = {
                            when (mode) {
                                PrimaryButtonMode.LAUNCH -> onLaunchGame()
                                PrimaryButtonMode.SELECT -> onPickFile()
                                PrimaryButtonMode.LOADING -> Unit
                            }
                        },
                        accent = config.accentPrimary,
                        backdropImage = backdrop,
                        parallax = parallax,
                        reducedMotion = reducedMotion,
                        hapticsEnabled = config.enableHaptics,
                    )
                }

                Spacer(Modifier.height(if (compact) 14.dp else 22.dp))

                Entrance(order = 2, reducedMotion = reducedMotion) {
                    StatusBanner(
                        state = uiState,
                        reducedMotion = reducedMotion,
                        onRetry = onRetry,
                    )
                }
            }

            // rodapé: chip técnico (esquerda) · pasta + configurações (direita)
            Entrance(order = 3, reducedMotion = reducedMotion) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (config.showTechChip) {
                        TechChip(config)
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassIconButton(
                            iconRes = R.drawable.ic_folder,
                            contentDescriptionText = stringResource(R.string.cd_folder_button),
                            onClick = onPickFolder,
                            enabled = uiState !is DataSelectionUiState.Checking,
                            emphasized = uiState is DataSelectionUiState.NotFound ||
                                uiState is DataSelectionUiState.Error,
                            accent = config.accentPrimary,
                            hapticsEnabled = config.enableHaptics,
                            reducedMotion = reducedMotion,
                        )
                        SettingsIconButton(
                            contentDescriptionText = stringResource(R.string.cd_settings_button),
                            onClick = { settingsOpen = true },
                            accent = config.accentPrimary,
                            hapticsEnabled = config.enableHaptics,
                            reducedMotion = reducedMotion,
                        )
                    }
                }
            }
        }
    }

    SettingsSheet(
        visible = settingsOpen,
        settings = settings,
        versionLabel = config.versionLabel,
        accent = config.accentPrimary,
        onDismiss = { settingsOpen = false },
        onParticles = onParticles,
        onParallax = onParallax,
        onReducedMotion = onReducedMotion,
    )
}

// ── Auxiliares ────────────────────────────────────────────────────────────

/** Entrada em stagger: fade + slide-up com atraso por ordem (ou fade curto). */
@Composable
private fun Entrance(
    order: Int,
    reducedMotion: Boolean,
    content: @Composable () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!visible) {
            if (reducedMotion) {
                visible = true
            } else {
                delay(order * 120L)
                visible = true
            }
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = if (reducedMotion) {
            fadeIn(tween(120))
        } else {
            fadeIn(tween(400)) +
                slideInVertically(tween(500, easing = FastOutSlowInEasing)) { it / 4 }
        },
        exit = fadeOut(tween(120)),
    ) {
        content()
    }
}

/** Bitmap desfocado do fundo para o vidro do botão — gerado 1× em IO. */
@Composable
private fun rememberBlurredBackdrop(@androidx.annotation.DrawableRes res: Int): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, res) {
        value = withContext(Dispatchers.IO) {
            BitmapFx.decodeBlurredBackdrop(context, res)
        }
    }.value
}

// ── Previews (4 densidades/orientações — DESIGN_SPEC §9) ──────────────────

@Preview(name = "Phone 411×891", widthDp = 411, heightDp = 891, showBackground = true)
@Preview(name = "Compacto 360×640", widthDp = 360, heightDp = 640, showBackground = true)
@Preview(name = "Tablet 768×1024", widthDp = 768, heightDp = 1024, showBackground = true)
@Preview(name = "Paisagem 891×411", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
private fun PreviewDataSelection() {
    PortTemplateTheme {
        DataSelectionContent(
            uiState = DataSelectionUiState.NotFound(folderName = null),
            settings = ScreenSettings(),
            config = PortBranding.current,
            onLaunchGame = {},
            onPickFile = {},
            onPickFolder = {},
            onParticles = {},
            onParallax = {},
            onReducedMotion = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Encontrado", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun PreviewFoundState() {
    PortTemplateTheme {
        DataSelectionContent(
            uiState = DataSelectionUiState.Found("default.xex", "712.4 MB"),
            settings = ScreenSettings(),
            config = PortBranding.current,
            onLaunchGame = {},
            onPickFile = {},
            onPickFolder = {},
            onParticles = {},
            onParallax = {},
            onReducedMotion = {},
            onRetry = {},
        )
    }
}
