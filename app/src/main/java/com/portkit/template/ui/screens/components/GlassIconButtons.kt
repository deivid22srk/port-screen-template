package com.portkit.template.ui.screens.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portkit.template.R
import com.portkit.template.ui.theme.GlassBorder
import com.portkit.template.ui.theme.GlassFill
import com.portkit.template.ui.theme.GlassFillSoft
import com.portkit.template.ui.theme.TextPrimary
import com.portkit.template.ui.theme.TextSecondary

/**
 * Botão circular de vidro 48 dp (alvo de toque AA) para os cantos inferiores.
 *
 *  • Desabilitado: vidro quase apagado, sem borda — "em espera", não quebrado;
 *  • Ênfase (ex.: pasta quando dados não encontrados): borda/ícone no acento
 *    com halo respirando;
 *  • Pressed: escala 0.94 + haptic.
 */
@Composable
fun GlassIconButton(
    @DrawableRes iconRes: Int,
    contentDescriptionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    accent: Color,
    hapticsEnabled: Boolean = true,
    reducedMotion: Boolean = false,
    iconOverlay: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pressed) {
        if (pressed && enabled && hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "iconScale",
    )

    val borderBrush: Color = when {
        !enabled -> Color.Transparent
        emphasized -> accent.copy(alpha = 0.55f)
        else -> GlassBorder
    }
    val iconTint = when {
        !enabled -> TextSecondary.copy(alpha = 0.38f)
        emphasized -> accent
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescriptionText
            }
            .clip(CircleShape)
            .background(if (enabled) GlassFill else GlassFillSoft)
            .border(1.dp, borderBrush, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (emphasized && enabled && !reducedMotion) {
            EmphasisRing(accent)
        }
        if (iconOverlay != null) {
            iconOverlay()
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTint,
            )
        }
    }
}

/**
 * Botão de configurações: engrenagem com rotação sutil acumulativa (−90° por
 * toque, spring) antes de abrir o sheet.
 */
@Composable
fun SettingsIconButton(
    contentDescriptionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color,
    hapticsEnabled: Boolean = true,
    reducedMotion: Boolean = false,
) {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = taps * -90f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 380f),
        label = "gearRotation",
    )

    GlassIconButton(
        iconRes = R.drawable.ic_settings,
        contentDescriptionText = contentDescriptionText,
        onClick = {
            taps += 1
            onClick()
        },
        modifier = modifier,
        accent = accent,
        hapticsEnabled = hapticsEnabled,
        reducedMotion = reducedMotion,
        iconOverlay = {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { rotationZ = rotation },
                tint = TextPrimary,
            )
        },
    )
}
