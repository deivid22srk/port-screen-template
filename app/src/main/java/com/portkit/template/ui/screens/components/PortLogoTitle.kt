package com.portkit.template.ui.screens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.portkit.template.R
import com.portkit.template.branding.PortBrandingConfig
import com.portkit.template.ui.theme.TextPrimary
import com.portkit.template.ui.theme.TextSecondary

/**
 * Camada de título/logo em área dedicada (topo da tela).
 *
 *  • Com logo: imagem + halo "respirando" do acento por trás (glow pulse 4 s);
 *  • Sem logo ([PortBrandingConfig.logoRes] = null): nome do port em display;
 *  • Tagline sempre abaixo, caixa alta, tracking largo;
 *  • Entrada: fade + slide-up 24 dp (600 ms, spring suave, delay 80 ms).
 *    Com "reduzir movimento": aparece direto, sem halo animado.
 */
@Composable
fun PortLogoTitle(
    config: PortBrandingConfig,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (reducedMotion) appear.snapTo(1f)
        else appear.animateTo(1f, tween(600, delayMillis = 80, easing = FastOutSlowInEasing))
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        if (!reducedMotion) BreathingGlow(config.accentPrimary)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 24.dp.toPx()
            },
        ) {
            val logo = config.logoRes
            if (logo != null) {
                Image(
                    painter = painterResource(logo),
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier.heightIn(max = 96.dp),
                    colorFilter = if (config.tintLogoWithAccent) {
                        ColorFilter.tint(config.accentPrimary)
                    } else {
                        null
                    },
                )
            } else {
                Text(
                    text = config.portDisplayName.uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = config.tagline.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 0.3.em,
            )
        }
    }
}

/** Halo radial com pulso lento por trás do logo (não compõe quando reduzido). */
@Composable
private fun BreathingGlow(color: Color) {
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.10f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )
    Canvas(Modifier.size(240.dp)) {
        val brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = glow), Color.Transparent),
            center = center,
            radius = size.minDimension / 2f,
        )
        drawCircle(
            brush = brush,
            radius = size.minDimension / 2f,
            center = center,
        )
    }
}

/** Anel de ênfase respirando (usado no botão de pasta em estado de atenção). */
@Composable
internal fun EmphasisRing(accent: Color) {
    val alpha by rememberInfiniteTransition(label = "emphasis").animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emphasisAlpha",
    )
    Canvas(Modifier.size(48.dp)) {
        drawCircle(
            color = accent.copy(alpha = alpha),
            style = Stroke(width = 1.5.dp.toPx()),
            radius = size.minDimension / 2f - 0.75.dp.toPx(),
        )
    }
}
