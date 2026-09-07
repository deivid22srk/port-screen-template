package com.portkit.template.ui.screens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portkit.template.R
import com.portkit.template.ui.motion.ParallaxState
import com.portkit.template.ui.theme.TextPrimary
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition

/** Modos do botão primário: selecionar dados / verificando / iniciar jogo. */
enum class PrimaryButtonMode { SELECT, LOADING, LAUNCH }

/**
 * Botão primário com glassmorphism (DESIGN_SPEC §6.2).
 *
 * O "vidro" é composto por 3 camadas:
 *  1. recorte DESFOCADO da própria arte de fundo (bitmap pré-processado uma
 *     única vez — sem custo por frame e compatível com qualquer API), que
 *     acompanha o parallax em contra-fase, como um vidro real faria;
 *  2. gradiente de preenchimento com a cor de acento da config (20% → 6%);
 *  3. borda 1 dp em gradiente (branco 28% → acento 45% → branco 10%).
 *
 * Estados:
 *  • pressed — escala 0.97 + sheen diagonal varrendo + haptic;
 *  • LOADING — spinner substitui o ícone; clique desativado;
 *  • LAUNCH  — halo respirando na borda (dados prontos).
 */
@Composable
fun GlassPrimaryButton(
    mode: PrimaryButtonMode,
    label: String,
    contentDescriptionText: String,
    onClick: () -> Unit,
    accent: Color,
    backdropImage: ImageBitmap?,
    parallax: ParallaxState,
    reducedMotion: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pressed) {
        if (pressed && hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "pressScale",
    )

    // sheen: 0 → 1 varre o brilho diagonal; volta a 0 ao soltar
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(pressed) {
        if (pressed && !reducedMotion) {
            sweep.snapTo(0f)
            sweep.animateTo(1f, tween(400, easing = LinearEasing))
        } else {
            sweep.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .widthIn(min = 280.dp, max = 320.dp)
            .height(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescriptionText
            }
            .clip(RoundedCornerShape(30.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = mode != PrimaryButtonMode.LOADING,
                onClick = onClick,
            )
            .background(
                brush = Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.20f), accent.copy(alpha = 0.06f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        accent.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.10f),
                    ),
                ),
                shape = RoundedCornerShape(30.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 1) backdrop desfocado, em contra-fase do parallax (vidro "vivo")
        if (backdropImage != null) {
            Image(
                bitmap = backdropImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.6f
                        scaleY = 1.6f
                        translationX = (-parallax.tiltX * 10f) - parallax.dragX * 0.35f
                        translationY = (parallax.tiltY * 6f) - parallax.dragY * 0.35f
                    },
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.18f)))
        } else {
            Box(Modifier.matchParentSize().background(Color.White.copy(alpha = 0.06f)))
        }

        // 2) sheen diagonal no press (desenho puro, sem recomposição)
        if (!reducedMotion) {
            val sweepState = sweep
            Box(
                Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        onDrawWithContent {
                            val p = sweepState.value
                            if (p > 0f && p < 1f) {
                                val x = -w + p * (3f * w)
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.12f),
                                            Color.Transparent,
                                        ),
                                        start = Offset(x - w * 0.25f, 0f),
                                        end = Offset(x + w * 0.25f, h),
                                    ),
                                )
                            }
                            drawContent()
                        }
                    },
            )
        }

        // 3) halo respirando quando os dados estão prontos
        if (mode == PrimaryButtonMode.LAUNCH && !reducedMotion) {
            val halo by rememberInfiniteTransition(label = "halo").animateFloat(
                initialValue = 0.20f,
                targetValue = 0.60f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "haloAlpha",
            )
            Box(
                Modifier
                    .matchParentSize()
                    .drawWithCache {
                        onDrawBehind {
                            drawRoundRect(
                                color = accent.copy(alpha = halo),
                                cornerRadius = CornerRadius(30.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    },
            )
        }

        // conteúdo
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mode == PrimaryButtonMode.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = TextPrimary,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = TextPrimary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
            )
        }
    }
}
