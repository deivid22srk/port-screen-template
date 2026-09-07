package com.portkit.template.ui.screens.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.portkit.template.ui.motion.ParallaxState
import kotlinx.coroutines.launch

/**
 * Camada 1–2 da composição: arte de fundo com parallax + scrim de contraste.
 *
 * Parallax combinado (ver ParallaxState):
 *  • tilt do acelerômetro — amplitude 10 dp, suavização EMA;
 *  • arraste de toque — amplitude 22 dp, retorno por spring ao soltar.
 *
 * A imagem é desenhada com escala 1.12 para ter "sobra" nas bordas quando as
 * camadas se deslocam. As leituras do estado de parallax acontecem DENTRO do
 * bloco graphicsLayer: mudanças de offset atualizam só a camada, sem recompor.
 */
@Composable
fun ParallaxBackground(
    @DrawableRes backgroundRes: Int,
    parallax: ParallaxState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ampTiltPx = with(density) { 10.dp.toPx() }
    val ampDragPx = with(density) { 22.dp.toPx() }
    val dragLimitPx = with(density) { 56.dp.toPx() }
    val scope = rememberCoroutineScope()

    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(parallax.active) {
                if (!parallax.active) return@pointerInput
                detectDragGestures(
                    onDrag = { change, amount ->
                        change.consume()
                        parallax.dragX = (parallax.dragX + amount.x).coerceIn(-dragLimitPx, dragLimitPx)
                        parallax.dragY = (parallax.dragY + amount.y).coerceIn(-dragLimitPx, dragLimitPx)
                    },
                    onDragEnd = {
                        scope.launch {
                            animate(
                                parallax.dragX, 0f,
                                spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                            ) { v, _ -> parallax.dragX = v }
                            animate(
                                parallax.dragY, 0f,
                                spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                            ) { v, _ -> parallax.dragY = v }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            animate(
                                parallax.dragX, 0f,
                                spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                            ) { v, _ -> parallax.dragX = v }
                            animate(
                                parallax.dragY, 0f,
                                spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                            ) { v, _ -> parallax.dragY = v }
                        }
                    },
                )
            },
    ) {
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.12f
                    scaleY = 1.12f
                    translationX = parallax.offsetX(ampTiltPx, ampDragPx)
                    translationY = parallax.offsetY(ampTiltPx, ampDragPx)
                },
        )

        // Scrim inferior — garante contraste do rodapé sobre qualquer arte.
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to Color(0x73000000),
                ),
            )
        }
    }
}
