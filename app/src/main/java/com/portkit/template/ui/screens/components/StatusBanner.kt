package com.portkit.template.ui.screens.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portkit.template.R
import com.portkit.template.branding.PortBranding
import com.portkit.template.ui.theme.GlassFill
import com.portkit.template.ui.theme.GlassBorder
import com.portkit.template.ui.theme.StatusCheck
import com.portkit.template.ui.theme.StatusError
import com.portkit.template.ui.theme.StatusOk
import com.portkit.template.ui.theme.StatusWarning
import com.portkit.template.ui.theme.TextPrimary
import com.portkit.template.ui.theme.TextSecondary
import com.portkit.template.viewmodel.DataSelectionUiState

/**
 * Área de status/aviso abaixo do botão primário (DESIGN_SPEC §6.3).
 *
 * Cada estado tem ícone, cor e microanimação próprios:
 *  • Checking      — arco giratório + pontos "…" pulsando;
 *  • NotFound      — alerta com shake horizontal na entrada;
 *  • Found         — check que se "desenha" (clip progressivo) + bounce;
 *  • Error         — ícone pulsando; tocar no banner tenta de novo.
 *
 * Transições: AnimatedContent fade + slide vertical (ou fade curto com
 * movimento reduzido).
 */
@Composable
fun StatusBanner(
    state: DataSelectionUiState,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    AnimatedContent(
        targetState = state,
        contentKey = { it::class },
        transitionSpec = {
            if (reducedMotion) {
                fadeIn(tween(120)) togetherWith fadeOut(tween(90))
            } else {
                (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 3 }) togetherWith
                    (fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it / 4 })
            }
        },
        label = "statusBanner",
        modifier = modifier,
    ) { current ->
        when (current) {
            DataSelectionUiState.Checking -> CheckingRow(reducedMotion)

            is DataSelectionUiState.NotFound -> StatusRow(
                iconRes = R.drawable.ic_warning,
                iconTint = StatusWarning,
                title = stringResource(R.string.status_not_found_title),
                detail = stringResource(
                    R.string.status_not_found_detail,
                    PortBranding.current.expectedFileName,
                ),
                detailTint = TextSecondary,
                animated = { content -> ShakeOnEnter(reducedMotion) { content() } },
            )

            is DataSelectionUiState.Found -> StatusRow(
                iconRes = R.drawable.ic_check,
                iconTint = StatusOk,
                title = stringResource(R.string.status_found_title),
                detail = stringResource(R.string.status_found_detail, current.fileName, current.sizeLabel),
                detailTint = TextSecondary,
                animated = { content -> DrawnCheck(reducedMotion) { content() } },
            )

            is DataSelectionUiState.Error -> StatusRow(
                iconRes = R.drawable.ic_error,
                iconTint = StatusError,
                title = stringResource(R.string.status_error_title),
                detail = current.message
                    ?: stringResource(R.string.status_error_detail),
                detailTint = StatusError.copy(alpha = 0.9f),
                animated = { content -> PulsingIcon(reducedMotion) { content() } },
                onClick = onRetry,
            )
        }
    }
}

// ── Linhas por estado ─────────────────────────────────────────────────────

@Composable
private fun CheckingRow(reducedMotion: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlassFill)
                .border(1.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = StatusCheck,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.status_checking),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.width(2.dp))
                AnimatedDots(StatusCheck, reducedMotion)
            }
            Text(
                text = stringResource(R.string.status_checking_detail),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun StatusRow(
    iconRes: Int,
    iconTint: Color,
    title: String,
    detail: String,
    detailTint: Color,
    animated: @Composable (@Composable () -> Unit) -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .semantics(mergeDescendants = true) { contentDescription = "$title. $detail" },
    ) {
        animated {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = iconTint,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = detailTint,
            )
        }
    }
}

// ── Microanimações por estado ─────────────────────────────────────────────

/** shake horizontal ±3 dp, 3 ciclos, na entrada do estado. */
@Composable
private fun ShakeOnEnter(reducedMotion: Boolean, content: @Composable () -> Unit = {}) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            shake.animateTo(
                0f,
                keyframes {
                    durationMillis = 340
                    0f at 0
                    -1f at 60
                    1f at 120
                    -0.7f at 180
                    0.7f at 240
                    -0.3f at 290
                    0f at 340
                },
            )
        }
    }
    Box(Modifier.graphicsLayer {
        translationX = shake.value * 3.dp.toPx()
    }) { content() }
}

/** check "desenhado" por clip progressivo + bounce do medalhão. */
@Composable
private fun DrawnCheck(reducedMotion: Boolean, content: @Composable () -> Unit = {}) {
    val reveal = remember { Animatable(0f) }
    val bounce = remember { Animatable(1.15f) }
    LaunchedEffect(Unit) {
        if (reducedMotion) {
            reveal.snapTo(1f)
            bounce.snapTo(1f)
        } else {
            reveal.snapTo(0f)
            bounce.snapTo(1.15f)
            reveal.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
            bounce.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f))
        }
    }
    Box(
        Modifier
            .graphicsLayer { scaleX = bounce.value; scaleY = bounce.value }
            .drawWithContent {
                clipRect(right = size.width * reveal.value) {
                    this@drawWithContent.drawContent()
                }
            },
    ) { content() }
}

/** pulso de opacidade (2 ciclos) para erro de permissão. */
@Composable
private fun PulsingIcon(reducedMotion: Boolean, content: @Composable () -> Unit = {}) {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            repeat(2) {
                pulse.animateTo(0.55f, tween(250, easing = FastOutSlowInEasing))
                pulse.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
            }
        }
    }
    Box(Modifier.graphicsLayer { alpha = pulse.value }) { content() }
}

/** três pontos com alpha em cascata (estado "procurando"). */
@Composable
private fun AnimatedDots(color: Color, reducedMotion: Boolean) {
    if (reducedMotion) {
        Text(text = "…", style = MaterialTheme.typography.bodyLarge, color = color)
        return
    }
    val transition = rememberInfiniteTransition(label = "dots")
    val a0 by transition.animateFloat(
        0.15f, 1f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse, StartOffset(0)),
        label = "d0",
    )
    val a1 by transition.animateFloat(
        0.15f, 1f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse, StartOffset(150)),
        label = "d1",
    )
    val a2 by transition.animateFloat(
        0.15f, 1f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse, StartOffset(300)),
        label = "d2",
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(color, a0)
        Dot(color, a1)
        Dot(color, a2)
    }
}

@Composable
private fun Dot(color: Color, alpha: Float) {
    Canvas(Modifier.size(4.dp).graphicsLayer { this.alpha = alpha }) {
        drawCircle(color = color)
    }
}
